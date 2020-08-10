package com.spfz.alpha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import com.uwsoft.editor.renderer.components.ActionComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.components.TintComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.ZIndexComponent;
import com.uwsoft.editor.renderer.components.label.LabelComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import com.uwsoft.editor.renderer.data.CompositeItemVO;
import com.uwsoft.editor.renderer.data.FrameRange;
import com.uwsoft.editor.renderer.scripts.IScript;
import com.uwsoft.editor.renderer.systems.action.Actions;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.renderer.utils.ItemWrapper;

public class SPFZP1Movement implements IScript, Attribs, BufferandInput
{

  boolean special, swap, hit, blk, dblk, dash, left, right, isJumping, jumpdir, attacking, attacked, confirm, walljump,
    blocking, punchstuck, kickstuck, runscript, cancel, isRight, isLeft, isUp, isDown, isPunch, isKick, pushed,
    createbox, projact, pausefrm, inair, stwlk, ltstuck, ownatk, projhit, bouncer, wallb, invul, stop, ease;

  CharAttributes spfzp1vals;

  SPFZStage stage;
  Vector2 walkandjump, dashpoints;

  float stun, tempfdur, gravity, jumpspeed, walkspeed, dashspeed, tempspeed, tempdist, tempdur, pauseTime, movX, easing,
    stateTime = 0, intpush, spectime = 0, swaptime = 0, duration = .13f, distance = 1f, startpt, adjustX, adjustY, origStart;

  int combocount, buff, last, dashdir, lastcount, buffsize = 60, move, input, lastfps;

  int[] activeframes, currinput;

  final int BLKSTN = 0, HITSTN = 1, BLKDIST = 2, HITDIST = 3, ACTSTARTBOX = 4, ACTENDBOX = 5, BOXX = 6,
  BOXY = 7, BOXWIDTH = 8, BOXHEIGHT = 9, BLKDMG = 10, HITDMG = 11, BLKMTR = 12, HITMTR = 13, FMOVE = 14,
  BMOVE = 15, JUGG = 16, BACK_START = 17, FWD_START = 18, BACK_ACTIVE = 19, FWD_ACTIVE = 20 ,BACK_RECOV = 21,
    FWD_RECOV = 22, PROJ_STARTUP_START = 23, PROJ_STARTUP_END = 24, PROJ_LOOP_START = 25, PROJ_LOOP_END = 26,
    PROJ_END_START = 27, PROJ_END_FINAL = 28, PROJ_TYPE = 29, PROJ_SPEED = 30, PROJ_POSX = 31, PROJ_POSY = 32,
    PROJ_POS_DIM_W = 33, PROJ_POS_DIM_H = 34, PROJ_POS_DIM_X = 35, PROJ_POS_DIM_Y = 36, PROJ_SPAWN = 37;

  int NEUTRAL = 0;
  ArrayList<int[]> inputs = new ArrayList<int[]>();

  List<Boolean> p1movement = new ArrayList<Boolean>(), lastp1movement = new ArrayList<Boolean>();
  List<Integer> buffer = new ArrayList<Integer>(), last16 = new ArrayList<Integer>();

  Entity spfzentity;

  ActionComponent spfzaction;
  TransformComponent spfzattribute;
  DimensionsComponent spfzdim;

  float intpol;

  Rectangle spfzrect, spfzhitrect, spfzcharrect, crossrect, dimrect;

  ShapeRenderer spfzsr, spfzhitbox, spfzcharbox, spfzdimbox;
  short cancelled, speccount;

  int pauseframe;
  SpriteAnimationComponent spfzanimation;
  SpriteAnimationStateComponent spfzanimationstate;
  String lastanim, weight;
  String[] loopanims = {"FWLK", "BWLK", "IDLE", "CRCH"};

  Vector2 hitboxsize, posofhitbox;

  //SPFZProjScript projectile;
  SPFZProjectile projectile;

  public SPFZP1Movement(SPFZStage screen)
  {
    stage = screen;
  }

  @Override
  public void act(float delta)
  {
    if (!stage.gameover)
    {
      movement(delta);

      stage.setface();
      if (createbox)
      {
        createHitBox();
        createbox = false;
      }
    }

    if (currproj() != null)
    {
      checkproj();
    }
    animation();
    boundlogic();
    jumplogic(delta);
  }

  @Override
  public int[] activeframes()
  {
    return activeframes;
  }

  @Override
  public SpriteAnimationComponent animationcomponent()
  {
    return spfzanimation;
  }

  @Override
  public SpriteAnimationStateComponent animationstate()
  {
    return spfzanimationstate;
  }

  public void animation()
  {
    if (!stage.standby)
    {
      if (spfzrect.y <= stage.GROUND && !isUp && !stop)
      {
        ground();

        if (lastanim != spfzanimation.currentAnimation)
        {
          ease = false;
          boolean loopfound = false;
          for (String anim : loopanims)
          {
            if (spfzanimation.currentAnimation == anim)
            {
              loopfound = true;
            }
          }

          if (loopfound)
          {
            if (spfzanimation.currentAnimation != null)
            {
              spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
                Animation.PlayMode.LOOP);
            }
          }
          else
          {
            if (spfzanimation.currentAnimation != null)
            {
              spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
                Animation.PlayMode.NORMAL);
            }
          }
          if (!loopfound)
          {
            stateTime = 0;
          }
          lastanim = spfzanimation.currentAnimation;
          spfzanimationstate.currentAnimation.setFrameDuration(1 / (float) spfzanimation.fps);
        }
      }
      else
      {
        inair();

        if (lastanim != spfzanimation.currentAnimation)
        {
          stateTime = 0;
          boolean loopfound = false;
          for (String anim : loopanims)
          {
            if (spfzanimation.currentAnimation.equals(anim))
            {
              loopfound = true;
            }
          }

          if (loopfound)
          {

            if (spfzanimation.currentAnimation != null)
            {
              spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
                Animation.PlayMode.LOOP);
            }
          }
          else
          {
            /*boolean prejump = false;

            //if the opponent is jumping
            if (isUp)
            {
              String[] jumps = {"NJMP", "BJMP", "FJMP", "WJMP"};


              for (String jump : jumps)
              {
                if (spfzanimation.currentAnimation == jump)
                {
                  prejump = true;
                }
              }

            }

            if (prejump)
            {
              spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), 60,
                Animation.PlayMode.NORMAL);
            }
            else
            {*/
            spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
              Animation.PlayMode.NORMAL);

          }

          lastanim = spfzanimation.currentAnimation;

        }

        if (!attacking && !hitboxsize.isZero())
        {
          hitboxsize.setZero();
        }
      }

      checkputp1();


      // grab the current frame of animation

      if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime) && spfzanimation.currentAnimation != "SATKD"
        && spfzanimation.currentAnimation != "SBLK" && spfzanimation.currentAnimation != "CBLK"
        && spfzanimation.currentAnimation != "ABLK")
      {
        if (spfzanimation.currentAnimation != "FTRANS")
        {
          if (attacking && !spfzanimationstate.paused)
          {
            attacking = false;
          }
          if (spfzrect.y <= stage.GROUND)
          {
            setneutral();
          }
        }
        else
        {
          stwlk = true;
        }
      }
      if (spfzanimationstate.currentAnimation.getPlayMode() == Animation.PlayMode.NORMAL && !spfzanimationstate.paused)
      {
        stateTime += Gdx.graphics.getDeltaTime();
      }
    }

  }

  public void inair()
  {
    if (attacked && !attacking)
    {
      airatkedanim();
    }
    else if (attacking && !attacked)
    {
      airatkanim();
    }
    else if (!attacking && !attacked)
    {
      // conditioning for movement in air animation
      airmvmtanim();
    }
    if (lastanim != spfzanimation.currentAnimation)
    {
      boolean prejump = false;

      /*//if the opponent is jumping
      if (isUp)
      {
        String[] jumps = {"NJMP", "BJMP", "FJMP", "WJMP"};


        for (String jump : jumps)
        {
          if (spfzanimation.currentAnimation == jump)
          {
            prejump = true;
          }
        }

      }

      if (prejump)
      {
        lastfps = spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
        spfzanimation.fps = 30;
      }
      else
      {*/
      spfzanimation.fps = spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
      lastfps = spfzanimation.fps;
      // }

    }
  }

  public void ground()
  {
    if (attacked || blocking)
    {
      grdatkedanim();
    }
    else if (attacking)
    {
      grdatkanim();
    }
    else if (!attacking && !attacked)
    {
      grdmvmtanim();
    }
    if (lastanim != spfzanimation.currentAnimation)
    {
      spfzanimation.fps = spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
      lastfps = spfzanimation.fps;
    }
  }

  public void airatkedanim()
  {

  }

  public void airatkanim()
  {

    //spfzanimation.fps = spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
    // Normal has connected.
    if (spfzanimationstate.paused && cancelled == 0 || pausefrm)
    {
      // using this process to get the exact frame in order to setup the
      // recovery animation
      if (!pausefrm)
      {
        pauseframe = currentframe();
        pausefrm = true;
        pauseTime = 0;
      }
      cancel = true;

      if (!attacking)
      {
        confirm = false;
      }
      if (special && isPunch && cancelled == 0 && cancel)
      {
        cancelled = 1;
        special = false;
      }
      // .10 will need to be a value coming in from the file

      if (pauseTime >= rtnFrametime(10f))
      {
        if (stage.shake)
        {
          stage.shake = false;
        }
        confirm = false;
        pauseTime = 0f;
        spfzanimationstate.paused = false;
        pausefrm = false;
        if (cancelled != 1)
        {
          if (spfzanimation.currentAnimation != null && spfzanimation.currentAnimation != "recovery")
          {
            spfzanimationstate.set(
              new FrameRange("recovery", pauseframe,
                spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).endFrame),
              60, Animation.PlayMode.NORMAL);
            stateTime = 0;
            spfzanimation.currentAnimation = "recovery";
            lastanim = spfzanimation.currentAnimation;
            cancel = false;
          }
          // set recovery frame animation here

        }
      }
      pauseTime += rtnFrametime(5);
    }
  }

  public void airmvmtanim()
  {
    // check jumping and jump direction variables
    if (isJumping)
    {
      // if jumpdir true, means jumping in right diagonal
      if (jumpdir)
      {
        if (spfzattribute.scaleX > 0)
        {
          spfzanimation.currentAnimation = "FJMP";
        }
        else
        {
          spfzanimation.currentAnimation = "BJMP";
          if (walljump)
          {
            spfzanimation.currentAnimation = "WJMP";
          }
        }
      }
      else
      {
        if (spfzattribute.scaleX > 0)
        {
          spfzanimation.currentAnimation = "BJMP";
          if (walljump)
          {
            spfzanimation.currentAnimation = "WJMP";
          }
        }
        else
        {
          spfzanimation.currentAnimation = "FJMP";
        }
      }
    }
    else
    {
      spfzanimation.currentAnimation = "NJMP";
    }
  }

  public void grdatkedanim()
  {
    //////////// ATTACKED
    if (spfzanimation.currentAnimation == "IDLE" || spfzanimation.currentAnimation == "movement" || attacked)
    {
      attacking = false;
    }

    if (ownatk)
    {
      confirm = true;
      ownatk = false;
    }
    // Check if player one is attacked
    if (((Attribs) stage.arrscripts.get(stage.p2)).getboxconfirm() && !ownatk)
    {
      // This combo counter is the combo counter for player 2
      hit = true;
      stun = 0;
      lastcount = combocount;
      intpush = 1;
      attacking = false;
      // if (!stage.spfzp2move.projact)
      // {
      // stage.spfzp2move.spfzanimationstate.paused = true;
      // }
      if (blocking)
      {
        if (spfzrect.y == stage.GROUND)
        {
          if (isDown)
          {
            spfzanimation.currentAnimation = "CBLK";
          }
          else
          {
            spfzanimation.currentAnimation = "SBLK";
          }
        }
        else
        {
          spfzanimation.currentAnimation = "ABLK";
        }
      }
      else
      {
        spfzanimation.currentAnimation = "SATKD";
      }
      setstun();
      stateTime = 0;
    }

    if (stateTime >= stun)
    {
      attacked = false;
      spfzanimation.currentAnimation = "IDLE";
      stateTime = 0;
      pushed = false;
      if (blocking)
      {
        blocking = false;
      }

      if (combocount != 0)
      {
        LabelComponent combocount1;
        combocount1 = stage.access.root.getChild("ctrlandhud").getChild("combocount1").getEntity()
          .getComponent(LabelComponent.class);

        Entity Hit = stage.access.root.getChild("ctrlandhud").getChild("p2himg").getEntity();
        Entity cc = stage.access.root.getChild("ctrlandhud").getChild("p2cc").getEntity();

        Actions.addAction(Hit, Actions.scaleTo(stage.SCALE_TEXT, 0, .3f, Interpolation.elastic));
        Actions.addAction(cc, Actions.scaleTo(stage.SCALE_TEXT, 0, .3f, Interpolation.elastic));

        combocount1.setText(" ");
        combocount = 0;
      }

      if (stateTime != 0)
      {
        stateTime = 0;
      }
      if (stage.training)
      {
        stage.p1health = stage.startp1;
      }
    }
    else
    {
      if (pushed && !hit)
      {

      }
      else
      {
        hit = false;
      }

      if ((spfzanimation.currentAnimation == "SATKD" || spfzanimation.currentAnimation == "SBLK"
        || spfzanimation.currentAnimation == "ABLK" || spfzanimation.currentAnimation == "CBLK")
        && stateTime < stun)

      {
        // modifier from file
        float pushback = 2f;
        float progress = Math.min(pushback, intpush * pushback);

        pushback = progress;

        if (intpush > 0f)
        {
          intpush -= .03f;
        }

        // pushback after hit
        if (spfzattribute.scaleX > 0)
        {
          if (stage.cam.position.x <= stage.camboundary[0] + 1 && spfzrect.x <= stage.stageboundary[0])
          {
            spfzattribute.x -= pushback;
          }
          else
          {
            spfzattribute.x -= pushback;
          }
        }
        // pushback if facing the opposite direction
        else
        {
          if (stage.cam.position.x + 1 >= stage.camboundary[1] && spfzrect.x >= stage.stageboundary[1])
          {
            spfzattribute.x += pushback;
          }
          else
          {
            spfzattribute.x += pushback;
          }
        }

        pushed = true;

      }
      else
      {
        stun = 0;

      }
    }

    // once the animation is complete, return the character back to the
    // neutral state
    if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime) && spfzanimation.currentAnimation != "SATKD"
      && spfzanimation.currentAnimation != "SBLK" && spfzanimation.currentAnimation != "ABLK"
      && spfzanimation.currentAnimation != "CBLK")
    {

      attacking = false;
      spfzanimation.currentAnimation = "IDLE";
      spfzanimationstate.set(spfzanimation.frameRangeMap.get("IDLE"), spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation)), Animation.PlayMode.LOOP);
      stateTime = 0;
    }
  }

  public void grdatkanim()
  {
    if (cancel && !hitboxsize.isZero())
    {
      hitboxsize.setZero();
    }
    // Normal has connected.
    if (spfzanimationstate.paused && cancelled == 0 || pausefrm)
    {
      // using this process to get the exact frame in order to setup the
      // recovery animation
      if (!pausefrm)
      {
        pauseframe = currentframe();
        pausefrm = true;
        pauseTime = 0;
      }
      cancel = true;
      // pauseTime += .015f;

      if (!attacking)
      {
        confirm = false;
      }
      if (special && isPunch && cancelled == 0 && cancel)
      {
        cancelled = 1;
      }
      /*// .10 will need to be a value coming in from the file
      // if (pauseTime >= .08f)
      if (pauseTime >= rtnFrametime(25f))
      {
        if (stage.shake)
        {
          stage.shake = false;
        }
        confirm = false;
        pauseTime = 0f;
        spfzanimationstate.paused = false;
        pausefrm = false;
        if (cancelled != 1)
        {
          if (spfzanimation.currentAnimation != null && spfzanimation.currentAnimation != "recovery")
          {

            spfzanimationstate.set(
              new FrameRange("recovery", pauseframe,
                spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).endFrame),
              60, Animation.PlayMode.NORMAL);
            stateTime = 0;
            spfzanimation.currentAnimation = "recovery";
            lastanim = spfzanimation.currentAnimation;
            cancel = false;
          }
          // set recovery frame animation here

        }
      }
      pauseTime += rtnFrametime(5);*/
    }
  }

  public void grdmvmtanim()
  {
    if (cancel)
    {
      cancel = false;
    }
    if (!isDown && !isUp && (isLeft || isRight || dash) && spfzrect.y == stage.GROUND && !ltstuck)
    {

      if (!stwlk)
      {

        spfzanimation.currentAnimation = "FTRANS";

      }
      else
      {
        if (!dash)
        {

          if (spfzrect.y <= stage.GROUND)
          {
            if (isLeft)
            {
              if (spfzattribute.scaleX > 0)
              {
                spfzanimation.currentAnimation = "BWLK";
              }
              else
              {
                spfzanimation.currentAnimation = "FWLK";
              }
            }
            else if (isRight)
            {
              if (spfzattribute.scaleX > 0)
              {
                spfzanimation.currentAnimation = "FWLK";
              }
              else
              {
                spfzanimation.currentAnimation = "BWLK";
              }
            }
            if (isLeft && isRight)
            {
              spfzanimation.currentAnimation = "IDLE";
            }
          }
        }
      }

      if (dash)
      {
        if (dashdir == 0 && spfzattribute.scaleX > 0 ||
          dashdir == 1 && spfzattribute.scaleX < 0)
        {
          spfzanimation.currentAnimation = "BDASH";
        }
        else
        {
          spfzanimation.currentAnimation = "FDASH";
        }
      }

    }
    else
    {
      if (!dash)
      {
        if (spfzrect.y <= stage.GROUND)
        {
          if (stwlk)
          {
            if (spfzanimation.currentAnimation != "STPTRANS")
            {
              stateTime = 0;
            }
            spfzanimation.currentAnimation = "STPTRANS";
            if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime))
            {
              stwlk = false;
              setneutral();
            }
          }
          else
          {
            spfzanimation.currentAnimation = "IDLE";
          }
        }

        if (isDown)
        {
          spfzanimation.currentAnimation = "CRCH";
        }
      }
    }
  }

  @Override
  public boolean attacked()
  {
    return attacked;
  }

  @Override
  public boolean attacking()
  {
    return attacking;
  }

  @Override
  public TransformComponent attributes()
  {

    return spfzattribute;
  }

  public void boundlogic()
  {
    setrect();
    float playerRB = spfzrect.x + spfzrect.width;
    float playerLB = spfzrect.x;
    float leftside = stage.cam.position.x - stage.HALF_WORLDW;
    float rightside = stage.cam.position.x + stage.HALF_WORLDW;

    // If the player has reached the left bound facing right
    if (playerLB < leftside)
    {
      spfzattribute.x = (leftside * spfzattribute.x) / playerLB;
    }
    // If the player has reached the right bound facing right
    else if (playerRB > rightside)
    {
      spfzattribute.x = (rightside * spfzattribute.x) / playerRB;
    }
  }

  @Override
  public float center()
  {
    return setrect().x + spfzrect.width * .5f;
  }

  public void checkputp1()
  {
    if (!lastp1movement.equals(p1movement))
    {
      // set flag for check scripts to be called
      for (int i = 0; i < p1movement.size(); i++)
      {
        if (lastp1movement.get(i) != p1movement.get(i))
        {
          lastp1movement.set(i, p1movement.get(i));
        }
      }
    }

    if (dash && (spfzanimation.currentAnimation == "BDASH" || spfzanimation.currentAnimation == "FDASH") &&
      spfzanimationstate.currentAnimation.isAnimationFinished(stateTime) && !stage.standby)
    {
      dash = false;
    }
  }

  @Override
  public int combonum()
  {
    return combocount;
  }

  public void createHitBox()
  {
    hitboxsize.setZero();
    float sizeW;
    float sizeH;

    if (spfzattribute.scaleX > 0)
    {

      sizeW = stage.player1data.get(stage.p1).get(BOXWIDTH).get(move).floatValue();
      sizeH = stage.player1data.get(stage.p1).get(BOXHEIGHT).get(move).floatValue();
    }
    else
    {
      sizeW = stage.player1data.get(stage.p1).get(BOXWIDTH).get(move).floatValue();
      sizeH = stage.player1data.get(stage.p1).get(BOXHEIGHT).get(move).floatValue();
    }
    /*
     * New hitbox logic:
     *
     * 1. get the animation based on user input
     *
     * 2. pass animation and character into process that will then pass back
     * these values: - active frames beginning to end - hitbox size - hitbox
     * position - amount of stun move will do
     *
     */

    activeframes[0] = stage.player1data.get(stage.p1).get(ACTSTARTBOX).get(move).intValue() - spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).startFrame;
    activeframes[1] = stage.player1data.get(stage.p1).get(ACTENDBOX).get(move).intValue() - spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).startFrame;
    hitboxsize.x = sizeW;
    hitboxsize.y = sizeH;
  }

  public void updboxpos()
  {
    float center = center();
    posofhitbox.setZero();
    if (move != -1)
    {
      float boxX;
      float boxY = stage.player1data.get(stage.p1).get(BOXY).get(move).floatValue();

      if (spfzattribute.scaleX > 0)
      {
        boxX = center + stage.player1data.get(stage.p1).get(BOXX).get(move).floatValue();

      }
      else
      {
        boxX = center - stage.player1data.get(stage.p1).get(BOXX).get(move).floatValue() - stage.player1data.get(stage.p1).get(BOXWIDTH).get(move).floatValue();
      }

      posofhitbox.x = boxX;
      posofhitbox.y = boxY + spfzrect.y;
    }
  }

  @Override
  public int currentframe()
  {
    return spfzanimationstate.currentAnimation.getKeyFrameIndex(spfzanimationstate.time);
  }

  @Override
  public DimensionsComponent dimensions()
  {

    return spfzdim;
  }

  @Override
  public void dispose()
  {

  }

  public void down(float delta)
  {
    // if user is pressing down on the control pad

    p1movement.set(0, false);
    p1movement.set(1, false);
    p1movement.set(3, true);
    p1movement.set(6, false);
    p1movement.set(7, false);

    if (isLeft)
    {
      p1movement.set(3, false);
      p1movement.set(6, false);
      p1movement.set(7, true);
    }
    else if (isRight)
    {
      p1movement.set(3, false);
      p1movement.set(6, true);
      p1movement.set(7, false);
    }

  }

  public ShapeRenderer drawhitbox()
  {
    return spfzhitbox;
  }

  public ShapeRenderer drawrect()
  {
    return spfzsr;
  }

  @Override
  public boolean getboxconfirm()
  {
    return confirm;
  }

  public TransformComponent getspfzattribute()
  {
    return spfzattribute;
  }

  public float getWalkspeed()
  {
    return walkspeed;
  }

  @Override
  public void hitboxconfirm(boolean confirm)
  {
    this.confirm = confirm;
  }

  @Override
  public Vector2 hitboxpos()
  {
    return posofhitbox;
  }

  @Override
  public Vector2 hitboxsize()
  {
    return hitboxsize;
  }

  @Override
  public void init(Entity entity)
  {
    ComponentMapper<MainItemComponent> mc = ComponentMapper.getFor(MainItemComponent.class);
    mc.get(entity);

    if (stage.device == 0)
    {
      spfzp1vals = new CharAttributes(mc.get(entity).itemIdentifier, stage.player1data.size(),
        stage.device, stage.access.android);
    }
    else if (stage.device == 1)
    {
      spfzp1vals = new CharAttributes(mc.get(entity).itemIdentifier, stage.player1data.size());
    }


    if (stage.player1data.size() != 3)
    {
      stage.player1data.add(spfzp1vals.tempplayer);
    }

    spfzsr = new ShapeRenderer();
    spfzhitbox = new ShapeRenderer();
    spfzcharbox = new ShapeRenderer();
    spfzdimbox = new ShapeRenderer();

    //spfzrect = new Rectangle();
    spfzrect = new Rectangle(spfzp1vals.getCharDims());
    adjustX = spfzrect.x;
    adjustY = spfzrect.y;
    spfzhitrect = new Rectangle();
    spfzcharrect = new Rectangle();
    dimrect = new Rectangle();
    crossrect = new Rectangle();
    hitboxsize = new Vector2();
    posofhitbox = new Vector2();
    activeframes = new int[]{0, 0};
    hitboxsize = new Vector2(0, 0);
    posofhitbox = new Vector2(0, 0);
    spfzentity = entity;
    setanimations();

    // get the values of the character from the Character attributes class
    gravity = spfzp1vals.getGravity();
    jumpspeed = spfzp1vals.getJump();
    walkspeed = spfzp1vals.getWalkspeed();
    walkandjump = spfzp1vals.getWandj();
    inputs = spfzp1vals.getmoveinputs();
    dashspeed = spfzp1vals.getdashadv();
    tempspeed = walkandjump.x;


    setPos();

  }

  @Override
  public boolean isplayerone()
  {
    return true;
  }

  public void jumplogic(float delta)
  {
    if (currentframe() == 1 && stop || isUp && setrect().y == stage.GROUND)
    {
      if (!stop)
      {
        stop = true;
      }
      //delay the jump frames
      if (currentframe() == 1)
      {
        processJump();
        stop = false;
        //spfzanimation.fps = lastfps;
        //stateTime = (2/(float) lastfps);
        //spfzanimationstate.currentAnimation.setFrameDuration(1/(float) lastfps);
        /*stage.access.root.getChild("zaine").getEntity().getComponent(SpriteAnimationStateComponent.class).time = stateTime;
        stage.access.root.getChild("p1land").getChild("landp1").getEntity()
          .getComponent(TransformComponent.class).scaleX = 1f;*/

      }
    }

    if (!stop)
    {
      // Apply gravity for spfzattribute calculations
      if (setrect().y > stage.GROUND)
      {
        if (!spfzanimationstate.paused)
        {
          walkandjump.y += gravity * delta;
        }
      }
      // assign the new jump value to the spfzattribute attribute to
      // apply gravity to the spfzattribute

      if (!spfzanimationstate.paused)
      {

        spfzattribute.y += walkandjump.y;
      }


      if (isJumping)
      {
        if (spfzrect.y > stage.GROUND)
        {
          if (spfzrect.y > stage.GROUND + stage.WALLJRES
            && (stage.cam.position.x <= stage.camboundary[0] + 1 || stage.cam.position.x + 1 >= stage.camboundary[1])
            && ((spfzattribute.x + adjustX <= stage.stageboundary[0] && spfzattribute.scaleX > 0)
            || spfzattribute.x - adjustX + 1 >= stage.stageboundary[1] && spfzattribute.scaleX < 0))
          {
            if (isLeft && isUp && !isRight && spfzattribute.scaleX < 0
              || isRight && isUp && !isLeft && spfzattribute.scaleX > 0)
            {
              // Needs modification, character keeps riding up on wall
              walljump = true;
              spfzattribute.y += walkandjump.y;
            }
          }
          else if (spfzrect.y < stage.GROUND + stage.WALLJRES)
          {
            walljump = false;
          }
          // if Jump direction is true(right) it will advance the player
          // to the
          // right
          // else Jump direction is false(left) it will advance the player
          // to the
          // left
          if (jumpdir)
          {
            if (walljump)
            {
              if (!spfzanimationstate.paused)
              {

                //walkandjump.lerp()
               /* if(!ease)
                {
                  spfzattribute.x += valueEase(walkandjump.x * .0150f);
                }
                else
                {*/
                spfzattribute.x += walkandjump.x * .0150f;
                //}

              }
              // code for wall jump particle effect
            }
            else
            {
              if (!spfzanimationstate.paused)
              {

                spfzattribute.x += walkandjump.x * .0150f;

              }
            }
          }
          else
          {
            if (walljump)
            {
              // wall jump particle effect will be here

              if (!spfzanimationstate.paused)
              {
                spfzattribute.x -= walkandjump.x * .0150f;
              }
            }
            else
            {
              if (!spfzanimationstate.paused)
              {
                spfzattribute.x -= walkandjump.x * .0150f;
              }
            }
          }
        }
        else
        {
          if (!isUp && (!isRight || !isLeft) && isJumping)
          {
            isJumping = false;
          }
        }
      }

      // If spfzattribute has reached the boundary of the ground, set to the
      // boundary of the ground
      if (spfzrect.y < stage.GROUND)
      {
        if (stop)
        {
          stop = false;
        }
        if (stage.access.root.getChild("p1land").getChild("landp1").getEntity()
          .getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)
        {
          stage.access.root.getChild("p1land").getChild("landp1").getEntity()
            .getComponent(SPFZParticleComponent.class).pooledeffects
            .removeValue(stage.access.root.getChild("p1land").getChild("landp1").getEntity()
              .getComponent(SPFZParticleComponent.class).pooledeffects.get(0), true);
        }
        stage.access.root.getChild("p1land").getEntity().getComponent(TransformComponent.class).x = center();
        stage.access.root.getChild("p1land").getEntity().getComponent(TransformComponent.class).y = stage.GROUND;
        stage.access.root.getChild("p1land").getChild("landp1").getEntity()
          .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
        stage.access.root.getChild("p1land").getChild("landp1").getEntity()
          .getComponent(TransformComponent.class).scaleX = 1f;
        stage.access.root.getChild("p1land").getChild("landp1").getEntity().getComponent(SPFZParticleComponent.class)
          .startEffect();
        attacking = false;
        confirm = false;
        hitboxsize.setZero();

        walkandjump.y = 0;
        spfzattribute.y = charGROUND();
        setneutral();
      }
    }
  }

  public float valueEase(float velocity)
  {
    if (!ease)
    {
      easing = 0;
      ease = true;
    }
    if (easing != velocity)
    {
      easing += velocity * .125;
    }

    if (easing >= velocity)
    {
      easing = velocity;
    }
    return easing;

  }

  public float easeToZero(float velocity)
  {
    if (easing > 0)
    {
      easing -= velocity * .20;
    }

    if (easing < 0)
    {
      easing = 0;
    }

    return easing;
  }


  public float charX()
  {
    if (spfzattribute.scaleX < 0)
    {
      return spfzattribute.x - spfzrect.x;
    }
    else
    {
      return spfzrect.x - spfzattribute.x;
    }

  }

  public float charY()
  {
    return spfzrect.y - spfzattribute.y;
  }

  public float charGROUND()
  {
    return stage.GROUND - (setrect().y - spfzattribute.y);
  }

  public void kick()
  {
    stage.sendToBack(spfzentity, stage.spfzp2move.spfzentity);
    spfzanimation.currentAnimation = null;

    if (swap && stage.p1spec >= 100f)
    {
      // if height restriction or on ground allow,
      if (spfzrect.y == stage.GROUND && projact() || spfzrect.y >= stage.GROUND + 30f)
      {
        stage.switchp1 = true;
        stage.sigp1lock = true;
        stage.switchcount = 0;
        swap = false;
        isPunch = false;
        spectime = 0;
        swaptime = 0;
        System.out.println("****************************** SWAP USED ******************************");
      }
      else
      {
        swap = false;
        System.out
          .println("****************************** SWAP LOST - HEIGHT RESTRICTION  ******************************");
      }
    }
    else
    {
      move = -1;

      if (spfzattribute.scaleX > 0)
      {
        if (!special || spfzrect.y > stage.GROUND)
        {
          if (spfzrect.y == stage.GROUND)
          {
            if (isLeft)
            {
              input = 9;
              if (isDown)
              {
                input = 3;
              }
              weight = "L";

            }
            else if (isRight)
            {
              input = 11;
              if (isDown)
              {
                input = 5;
              }
              weight = "H";


            }
            else if (!isLeft && !isRight)
            {
              input = 10;
              if (isDown)
              {
                input = 4;
              }
              weight = "M";

            }
          }
          else
          {
            // if isJumping is true, means we are jumping either
            // forwards or
            // backwards.
            if (isJumping)
            {
              // if jumpdir is true, means we are jumping forwrds,
              // otherwise, we
              // are jumping backwards
              if (jumpdir)
              {
                input = 17;
              }
              else
              {
                input = 15;
              }
            }
            else
            {
              input = 16;
            }
            weight = "H";
          }
        }
      }
      else
      {
        if (!special || spfzrect.y > stage.GROUND)
        {
          // Ground Kicks
          if (spfzrect.y == stage.GROUND)
          {
            if (isLeft)
            {
              input = 11;
              if (isDown)
              {
                input = 5;
              }
              weight = "H";

            }
            else if (isRight)
            {
              input = 9;
              if (isDown)
              {
                input = 3;
              }
              weight = "L";
            }
            else if (!isLeft && !isRight)

            {
              input = 10;
              if (isDown)
              {
                input = 4;
              }
              weight = "M";
            }
          }
          // Air Kicks
          else
          {
            // if isJumping is true, means we are jumping either
            // forwards or
            // backwards.
            if (isJumping)
            {
              // if jumpdir is true, means we are jumping
              // backwards, otherwise,
              // we are jumping forwards
              if (jumpdir)
              {
                input = 15;
              }
              else
              {
                input = 17;
              }
            }
            else
            {
              input = 16;
            }
            weight = "H";
          }
        }

      }

      //attacking = true;
      // if the move is not null technically
      move = spfzp1vals.moveset.indexOf(stage.normals[input]);

      if (move != -1)
      {
        spfzanimation.currentAnimation = spfzp1vals.moveset.get(move);
        spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation)),
          Animation.PlayMode.NORMAL);
        attackMove();
      }

    }

    attacking = true;

    if (spfzanimation.currentAnimation != null)
    {
      spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation)),
        Animation.PlayMode.NORMAL);
    }
  }

  @Override
  public boolean invul()
  {
    return invul;
  }

  public void attackMove()
  {

    Actions.removeActions(spfzentity);
    //set the durations of the 3 stages of movement as well as the amount to push or pull the character.
    float strtupmov = 0;
    float actmov = 0;
    float recovmov = 0;

    float firstdur = rtnFrametime(stage.player1data.get(stage.p1).get(ACTSTARTBOX).get(move).floatValue() - (float) spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).startFrame);
    float seconddur = rtnFrametime(stage.player1data.get(stage.p1).get(ACTENDBOX).get(move).floatValue() - stage.player1data.get(stage.p1).get(ACTSTARTBOX).get(move).floatValue());
    float thirddur = rtnFrametime(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation).endFrame - stage.player1data.get(stage.p1).get(ACTENDBOX).get(move).floatValue());

    if (stage.player1data.get(stage.p1).get(BACK_START).get(move).doubleValue() == 1)
    {
      strtupmov = stage.player1data.get(stage.p1).get(BMOVE).get(move).floatValue();

      if (spfzattribute.scaleX > 0)
      {
        strtupmov *= -1f;
      }
    }
    if (stage.player1data.get(stage.p1).get(FWD_START).get(move).doubleValue() == 1)
    {
      strtupmov = stage.player1data.get(stage.p1).get(FMOVE).get(move).floatValue();

      if (spfzattribute.scaleX < 0)
      {
        strtupmov *= -1f;
      }
    }

    if (stage.player1data.get(stage.p1).get(BACK_START).get(move).doubleValue() == 0 &&
      stage.player1data.get(stage.p1).get(FWD_START).get(move).doubleValue() == 0)
    {
      strtupmov = 0;
      //firstdur = 0;
    }

    if (stage.player1data.get(stage.p1).get(BACK_ACTIVE).get(move).doubleValue() == 1)
    {
      actmov = stage.player1data.get(stage.p1).get(BMOVE).get(move).floatValue();

      if (spfzattribute.scaleX > 0)
      {
        actmov *= -1f;
      }
    }
    if (stage.player1data.get(stage.p1).get(FWD_ACTIVE).get(move).doubleValue() == 1)
    {
      actmov = stage.player1data.get(stage.p1).get(FMOVE).get(move).floatValue();

      if (spfzattribute.scaleX < 0)
      {
        actmov *= -1f;
      }
    }

    if (stage.player1data.get(stage.p1).get(BACK_ACTIVE).get(move).doubleValue() == 0 &&
      stage.player1data.get(stage.p1).get(FWD_ACTIVE).get(move).doubleValue() == 0)
    {
      actmov = 0;
      //seconddur = 0;
    }

    if (stage.player1data.get(stage.p1).get(BACK_RECOV).get(move).doubleValue() == 1)
    {
      recovmov = stage.player1data.get(stage.p1).get(BMOVE).get(move).floatValue();

      if (spfzattribute.scaleX > 0)
      {
        recovmov *= -1f;
      }
    }
    if (stage.player1data.get(stage.p1).get(FWD_RECOV).get(move).doubleValue() == 1)
    {
      recovmov = stage.player1data.get(stage.p1).get(FMOVE).get(move).floatValue();

      if (spfzattribute.scaleX < 0)
      {
        recovmov *= -1f;
      }
    }

    if (stage.player1data.get(stage.p1).get(BACK_RECOV).get(move).doubleValue() == 0 &&
      stage.player1data.get(stage.p1).get(FWD_RECOV).get(move).doubleValue() == 0)
    {
      recovmov = 0;
      //thirddur = 0;
    }

    Actions.addAction(spfzentity,
      Actions.sequence(
        Actions.moveBy(strtupmov, 0, firstdur, Interpolation.sineOut),
        Actions.moveBy(actmov, 0, seconddur, Interpolation.sineOut),
        Actions.moveBy(recovmov, 0, thirddur, Interpolation.sineOut)));
    createbox = true;
  }

  public float left(float delta, float move)
  {
    // If sprite is on the ground
    if (spfzrect.y == stage.GROUND)
    {

      // move sprite horizontally
      if (!dash && !stop)
      {
        return move -= valueEase(walkandjump.x * delta);
      }
      else
      {
        return move;
      }

    }
    else
    {
      return move;
    }
  }

  @Override
  public Vector2 moveandjump()
  {

    return walkandjump;
  }

  public void movement(float delta)
  {
    movX = spfzattribute.x;
    float lastmovX = spfzattribute.x;
    storeinputs();

    for (int i = 0; i < inputs.size(); i++)
    {
      if (i >= 2)
      {
        getmove(inputs.get(i));
      }
      else
      {
        if (!dash)
        {
          execuniv(inputs.get(i));
        }
      }
    }

    stage.p1xattr = spfzattribute.x;

    if (isLeft)
    {
      p1movement.set(0, true);
      if (!attacking && !attacked && !isDown && !stage.standby && !ltstuck && !blocking)
      {

        // If Player 2 is on the right side, and their center is greater
        // than
        // Player 2's center, make player 2 block

        if (stage.spfzp2move.center() > center())
        {
          blk = true;
        }
        else
        {
          blk = false;
        }
        movX = left(delta, movX);

      }
    }
    else
    {
      p1movement.set(0, false);

    }

    if (isRight)
    {
      p1movement.set(1, true);
      if (!attacking && !attacked && !isDown && !stage.standby && !ltstuck && !blocking)
      {
        if (!attacking && !attacked)
        {
          // If Player one is on the left side, and their center is greater
          // than
          // Player 2's center, make player 2 block
          if (stage.spfzp2move.center() < center())
          {
            blk = true;
          }
          else
          {
            blk = false;
          }

        }
        movX = right(delta, movX);

      }
    }
    else
    {
      p1movement.set(1, false);
    }
    if (isUp)
    {
      up(delta);
    }
    else
    {
      p1movement.set(2, false);
      p1movement.set(4, false);
      p1movement.set(5, false);
    }

    if (isDown)
    {

      down(delta);
    }
    else
    {
      p1movement.set(3, false);
      p1movement.set(6, false);
      p1movement.set(7, false);
    }

    if (cancelled == 1)
    {
      isPunch = true;
      special = true;
      attacking = false;
      punchstuck = false;
      kickstuck = false;
      spfzanimationstate.paused = false;

    }
    if (isPunch && !attacked && !dash && !stage.standby)
    {
      if (attacking)
      {
        punchstuck = true;
      }
      if (!punchstuck)
      {
        punch();
      }
    }
    else
    {
      if (!isPunch)
      {

        punchstuck = false;
      }
    }
    if (isKick && !attacked && !dash && !stage.standby)
    {
      if (attacking)
      {
        if (!swap)
        {
          kickstuck = true;
        }
      }
      if (!kickstuck)
      {
        kick();
      }
    }
    else
    {
      if (!isKick)
      {
        kickstuck = false;
      }
    }

    if (!stage.standby)
    {
      if (intpol != 1 && !dash)
      {
        intpol = 1;
      }


      if (dash)
      {
        dashlogic();
      }
      else if (spfzanimation.currentAnimation.substring(0, 3).equals("PRO"))
      {
      }

      else
      {

        spfzattribute.x = movX;

      }
      crosslogic(movX, lastmovX);
      //crosslogic();

      if (special)
      {
        spectime += Gdx.graphics.getDeltaTime();
        if (spectime >= rtnFrametime(stage.SPECIAL_WINDOW))
        {
          System.out.println("****************************** SPECIAL NOT AVAILABLE ******************************");
          special = false;
          spectime = 0;
          speccount = 0;
        }
      }
      if (swap)
      {
        swaptime += Gdx.graphics.getDeltaTime();
        if (swaptime >= rtnFrametime(stage.SWAP_WINDOW))
        {
          System.out.println("****************************** SWAP NOT AVAILABLE ******************************");
          swap = false;
          swaptime = 0;
          speccount = 0;
        }
      }
    }
  }

  public void crosslogic(float curpos, float last)
  {
    if (setcross().overlaps(stage.spfzp2move.setcross()))
    {
      float right = crossrect.x + (crossrect.width * .75f);
      float left = crossrect.x + (crossrect.width * .25f);
      float p2 = stage.spfzp2move.crossrect.x;
      float p1center = center();
      float p2center = stage.spfzp2move.center();
      float p2full = stage.spfzp2move.crossrect.x + stage.spfzp2move.crossrect.width;

      if (p2 > stage.stageboundary[0] && p2full < stage.stageboundary[1])
      {
        if (p1center > p2center && p2full > left)
        {
          //push right
          curpos += (p2full - left) * .5f;
        }
        else if (p1center < p2center && p2 < right)
        {
          //push left
          curpos -= (right - p2) * .5f;
        }
        //curpos +=  curpos - stage.spfzp2move.lastmovX;
      }

      spfzattribute.x = curpos;

    }
  }

  public void dashlogic()
  {


    if ((spfzanimation.currentAnimation == "BDASH" || spfzanimation.currentAnimation == "FDASH") ||
      dash && spfzrect.y == stage.GROUND && currentframe() >= 3 && currentframe() <= currTotalFrames() - 3)
    {
      float totalFrames = currTotalFrames();
      float totalFrmTime = rtnFrametime(totalFrames);
      float progress = stateTime / totalFrmTime;

      if (startpt == 0)
      {
        startpt = spfzattribute.x;
        dashpoints = new Vector2(startpt, 0);
      }

      if (dashdir == 0)
      {

        dashpoints.interpolate(new Vector2(startpt - dashspeed, 0), progress, Interpolation.exp10In);

      }
      else
      {

        dashpoints.interpolate(new Vector2(startpt + dashspeed, 0), progress, Interpolation.exp10In);

      }

      spfzattribute.x = dashpoints.x;

      if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime))
      {
        walkandjump.x = tempspeed;
        startpt = 0;
      }

    }
  }

  public void punch()
  {
    stage.sendToBack(spfzentity, stage.spfzp2move.spfzentity);
    // "null" move
    bouncer = false;
    move = -1;

    if (stage.p1charzoom)
    {
      stage.p1charzoom = false;
    }
    if (spfzattribute.scaleX > 0)
    {
      if (!special || spfzrect.y > stage.GROUND)
      {
        if (spfzrect.y == stage.GROUND)
        {
          if (isLeft)
          {

            input = 6;
            if (isDown)
            {
              input = 0;
            }
            weight = "L";
            ltstuck = true;
          }
          else if (isRight)
          {
            input = 8;
            if (isDown)
            {
              input = 2;
              bouncer = true;
            }
            weight = "H";
          }
          else if (!isLeft && !isRight)
          {
            input = 7;
            if (isDown)
            {
              input = 1;
            }
            weight = "M";
          }
        }
        else
        {
          // if isJumping is true, means we are jumping either
          // forwards or
          // backwards.
          if (isJumping)
          {
            // if jumpdir is true, means we are jumping forwards,
            // otherwise, we
            // are jumping backwards
            if (jumpdir)
            {
              input = 14;
            }
            else
            {
              input = 12;
            }
          }
          else
          {
            input = 13;
          }

          if (spfzrect.y > stage.GROUND)
          {
            weight = "H";
          }
          else
          {
            createbox = false;
          }
        }
      }
      else
      {
        if (special && spfzrect.y == stage.GROUND && projectile == null)
        {
          move = -1;
          spfzanimation.currentAnimation = "PROJ" + String.valueOf(currinput[currinput.length - 1]);

          if (currproj() == null)
          {
            if (special)
            {
              spwnPrj(spfzanimation.currentAnimation);
              weight = "H";
              if (speccount >= 2)
              {
                stage.p1charzoom = true;
                // speccount = 0;
              }
              else
              {
                // speccount = 0;
              }
              speccount = 0;
              createbox = false;
              if (cancelled == 1)
              {
                stateTime = 0;
                cancelled = 0;
                spfzanimationstate.paused = false;
              }
            }
          }
        }
      }
    }
    else
    {
      if (!special || spfzrect.y > stage.GROUND)
      {

        if (spfzrect.y == stage.GROUND)
        {
          if (isLeft)
          {
            input = 8;
            weight = "H";
            if (isDown)
            {
              input = 2;
              bouncer = true;
            }
          }
          else if (isRight)
          {
            input = 6;
            if (isDown)
            {
              input = 0;
            }
            weight = "L";
            ltstuck = true;
          }
          else if (!isRight && !isLeft)
          {
            weight = "M";
            input = 7;
            if (isDown)
            {
              input = 1;
            }
          }
        }
        else
        {
          // if isJumping is true, means we are jumping either
          // forwards or
          // backwards.
          if (isJumping)
          {
            // if jumpdir is true, means we are jumping backwards,
            // otherwise, we
            // are jumping forwards
            if (jumpdir)
            {
              input = 12;
            }
            else
            {
              input = 14;
            }
          }
          else
          {
            input = 13;
          }
          if (spfzrect.y > stage.GROUND)
          {
            weight = "H";
            createbox = true;
          }
        }

      }
      else
      {
        if (special && spfzrect.y == stage.GROUND && currproj() == null)
        {
          move = -1;
          spfzanimation.currentAnimation = "PROJ" + String.valueOf(currinput[currinput.length - 1]);

          spwnPrj(spfzanimation.currentAnimation);
          weight = "H";
          createbox = false;

          if (cancelled == 1)
          {
            stateTime = 0;
            cancelled = 0;
          }
        }
      }
    }


    // if the move is not null technically
    if (!spfzanimation.currentAnimation.substring(0, 4).equals("PROJ"))
    {
      move = spfzp1vals.moveset.indexOf(stage.normals[input]);
      if (move != -1)
      {
        attacking = true;
        spfzanimation.currentAnimation = spfzp1vals.moveset.get(move);
        attackMove();
      }
    }

    spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation)),
      Animation.PlayMode.NORMAL);
  }

  public void execuniv(int[] move)
  {
    // LEFT = 0 UP = 2 LEFT & UP = 4 RIGHT & DOWN = 6
    // RIGHT = 1 DOWN = 3 RIGHT & UP = 5 LEFT & DOWN = 7 NEUTRAL = 8

    // special = false;
    int buffmove = 12;

    // passed in array
    if (buffer.get(buffer.size() - 1) == move[3] && buffer.size() >= 16)
    {

      if (buffer.get(buffer.size() - 2) == move[2])
      {

        for (int i = buffer.size() - 3; i > buffer.size() - 3 - (buffmove / 2); i--)
        {

          if (buffer.get(i) == move[1])
          {

            for (int k = i - 1; k > i - (buffmove / 2); k--)
            {
              if (buffer.get(k) == move[0] && !attacking && !dash)
              {

                dash = true;
                dashdir = move[1];
                k = i - (buffmove / 2);
                i = buffer.size() - 2 - (buffmove / 2);

                // line is executing twice. means this code is
                // executing more
                // than it should
                System.out.println("dash executed ------------------------------------");
              }
            }
          }
        }

      }

    }
  }

  public void getmove(int[] move)
  {
    // LEFT = 0 UP = 2 LEFT & UP = 4 RIGHT & DOWN = 6
    // RIGHT = 1 DOWN = 3 RIGHT & UP = 5 LEFT & DOWN = 7 NEUTRAL = 8

    // special = false;
    int buffmove = 12;

    // firstread(move, buffmove);
    inputread(move, buffmove);

  }

  public void inputread(int[] move, int buffmove)
  {
    int check = 1;

    for (int i = move.length - 2; i >= 0; i--)
    {
      if (buffer.size() - check > 0)
      {
        if (i != 0)
        {
          if (buffer.get(buffer.size() - check) == move[i])
          {
            check++;
            continue;
          }
          else
          {
            i = -1;
          }
        }
        else
        {
          // for loop checking for last input
          for (int j = buffer.size() - check; j > buffer.size() - check - buffmove; j--)
          {
            if (j >= 0)
            {

              if (buffer.get(j) == move[i])
              {
                if (move[move.length - 1] == 1 && spfzattribute.scaleX > 0
                  || move[move.length - 1] == 0 && spfzattribute.scaleX < 0)
                {
                  special = true;
                  swap = true;
                  speccount++;
                  System.out
                    .println("****************************** SPECIAL - BUFFER FOR ******************************");
                  j = buffer.size() - check - buffmove;
                  i = -1;
                  currinput = move;
                }
              }
            }
            else
            {

              for (int k = last16.size() - 1; k > last16.size() - 1 - buffmove; k--)
              {
                if (last16.size() >= 16)
                {
                  if (last16.get(k) == move[i])
                  {
                    if (move[move.length - 1] == 1 && spfzattribute.scaleX > 0
                      || move[move.length - 1] == 0 && spfzattribute.scaleX < 0)
                    {
                      special = true;
                      swap = true;
                      speccount++;

                      System.out.println(
                        "****************************** SPECIAL - LAST16 FOR MID BUFF ******************************");
                      k = k - 1 - buffmove;
                      j = buffer.size() - check - buffmove;
                      i = -1;
                      currinput = move;
                    }
                  }
                }
              }
            }
          }
        }
      }
      else
      {
        // check needs to be reset back to 1 to start at the "top" of
        // the last16
        // input buffer list.
        check = 1;
        if (last16.size() > 0)
        {
          for (int k = i; k > 0; k--)
          {
            if (k != 0)
            {
              if (last16.get(last16.size() - check) == move[k])
              {
                check++;
                continue;
              }
              else
              {
                k = -1;
                i = -1;
              }
            }
            else
            {
              for (int j = last16.size() - 1; j > last16.size() - 1 - buffmove; j--)
              {

                if (last16.get(j) == move[i])
                {
                  if (move[move.length - 1] == 1 && spfzattribute.scaleX > 0
                    || move[move.length - 1] == 0 && spfzattribute.scaleX < 0)
                  {
                    special = true;
                    swap = true;
                    speccount++;

                    System.out.println(
                      "****************************** SPECIAL - LAST16 OUT BUFF ******************************");
                    j = last16.size() - 1 - buffmove;
                    k = -1;
                    i = -1;
                    currinput = move;
                  }
                }
                else
                {
                  j = last16.size() - 1 - buffmove;
                }
              }
            }
          }
        }
      }
    }

  }

  public void setneutral()
  {

    ease = false;
    stateTime = 0;
    attacking = false;
    attacked = false;
    spfzanimation.currentAnimation = "IDLE";
    lastanim = "IDLE";
    if (isJumping)
    {
      isJumping = false;
    }
    if (isDown)
    {
      spfzanimation.currentAnimation = "CRCH";
    }

    spfzanimation.fps = spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
    spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
      Animation.PlayMode.LOOP);

  }

  public float right(float delta, float move)
  {
    // if user is pressing down RIGHT on the control pad

    // If sprite is on the ground
    if (spfzrect.y == stage.GROUND)
    {

      // move sprite horizontally
      if (!dash && !stop)
      {

        return move += valueEase(walkandjump.x * delta);

      }
      else
      {
        return move;
      }
    }
    else
    {
      return move;
    }
  }

  public void setanimations()
  {
    NodeComponent nc;

    nc = ComponentRetriever.get(spfzentity, NodeComponent.class);
    spfzaction = ComponentRetriever.get(spfzentity, ActionComponent.class);
    spfzattribute = ComponentRetriever.get(spfzentity, TransformComponent.class);
    //spfzattribute.x = stage.STAGE_CENTER - 200f;


    spfzdim = ComponentRetriever.get(spfzentity, DimensionsComponent.class);
    spfzanimation = ComponentRetriever.get(nc.children.get(0), SpriteAnimationComponent.class);
    spfzanimationstate = ComponentRetriever.get(nc.children.get(0), SpriteAnimationStateComponent.class);
    List<String> keys;

    if (stage.player1anims.size() < 3)
    {
      stage.player1anims.add(spfzp1vals.animations);
      stage.player1moves.add(spfzp1vals.moveset);
      keys = new ArrayList<String>(spfzp1vals.animations.keySet());
    }
    else
    {
      keys = new ArrayList<String>(stage.player1anims.get(stage.p1).keySet());
    }

    // create frame ranges for all animations listed for each character

    if (stage.player1data.size() < 3)
    {
      for (int i = 0; i < spfzp1vals.getAnimations().size(); i++)
      {

        spfzanimation.frameRangeMap.put(keys.get(i), new FrameRange(keys.get(i),
          spfzp1vals.animations.get(keys.get(i))[0], spfzp1vals.getAnimations().get(keys.get(i))[1]));
      }
    }
    else
    {
      for (int i = 0; i < stage.player1anims.get(stage.p1).size(); i++)
      {

        spfzanimation.frameRangeMap.put(keys.get(i),
          new FrameRange(keys.get(i), stage.player1anims.get(stage.p1).get(keys.get(i))[0],
            stage.player1anims.get(stage.p1).get(keys.get(i))[1]));
      }
    }

    setneutral();
  }

  public void setcombonum(int comboint)
  {
    combocount = comboint;
  }

  public Rectangle sethitbox()
  {
    updboxpos();
    spfzhitrect.set(posofhitbox.x, posofhitbox.y, hitboxsize.x, hitboxsize.y);
    return spfzhitrect;
  }

  public void setPos()
  {
    if (stage.switchp1)
    {
      spfzattribute.x = stage.p1xattr;
      spfzattribute.y = stage.p1yattr;
      attacking = false;
    }
    else
    {

      float startpos;
      if (origStart == 0)
      {
        startpos = stage.STAGE_CENTER - stage.CHAR_SPACE;
        startpos = (startpos - adjustX) - (spfzrect.width * .5f);
        origStart = startpos;
      }
      else
      {
        startpos = origStart;
      }

      spfzattribute.x = startpos;
      spfzattribute.y = charGROUND();

    }
  }

  public void setstun()
  {
    if (blk || dblk)
    {
      if (projhit)
      {
        if (stage.spfzp2move.move != -1 && stun == 0)
        {
          stun = rtnFrametime(stage.player2data.get(stage.p2).get(BLKSTN).get(stage.spfzp1move.move).floatValue());
        }
        else
        {
          if (stun == 0)
          {
            stun = .5f;
          }
        }
      }
      else
      {
        if (stage.spfzp1move.move != -1 && stun == 0)
        {
          stun = rtnFrametime(stage.player2data.get(stage.p2).get(BLKSTN).get(stage.spfzp2move.move).floatValue());
        }
        else
        {
          if (stun == 0)
          {
            stun = .5f;
          }
        }
      }

    }
    else
    {
      if (projhit)
      {
        if (stage.spfzp1move.move != -1 && stun == 0)
        {
          stun = rtnFrametime(stage.player1data.get(stage.p1).get(HITSTN).get(stage.spfzp1move.move).floatValue());
        }
        else
        {
          if (stun == 0)
          {
            stun = .5f;
          }
        }
      }
      else
      {
        if (stage.spfzp1move.move != -1 && stun == 0)
        {
          stun = rtnFrametime(stage.player2data.get(stage.p2).get(HITSTN).get(stage.spfzp2move.move).floatValue());
        }
        else
        {
          if (stun == 0)
          {
            stun = .5f;
          }
        }
      }
    }
  }

  public boolean projact()
  {
    boolean act;
    if (currproj() != null)
    {
      act = projectile.active;

     /* if(projectile.hit)
      {
        act = true;
      }*/
    }
    else
    {
      act = false;
    }


    return act;
  }

  public boolean projhit()
  {
    if (currproj() != null)
    {
      return projectile.hit;
    }
    else
    {
      return false;
    }
  }

  public void checkproj()
  {
    //spawn projectile on the appropriate frame of animation
    if (projectile.spawn == currentframe() && !projectile.start)
    {
      projectile.start = true;
      projectile.composite.getComponent(TintComponent.class).color.a = 1f;
    }
    //checking to see if projectile is out of screen view, checking to see if projectile connected. if so, kill projectile
    if (projconfirm() )
    {
      if (projectile.composite.getComponent(TransformComponent.class).x <= stage.access.viewportland.getCamera().position.x - 320f
        || projectile.composite.getComponent(TransformComponent.class).x >= stage.access.viewportland.getCamera().position.x + 320f)
      {
        projectile.dispose();
      }
    }
    else
    {
      if (special)
      {
        special = false;
      }
    }

  }

  public boolean projconfirm()
  {
    if (currproj() != null)
    {
      if (projectile.hitbox.overlaps(stage.spfzp2move.setcharbox()) && !projectile.hit && projectile.active)
      {
        projectile.hitAnim();
      }
      return projectile.hit;
    }
    else
    {
      return false;
    }
  }


  public Rectangle setrect()
  {
    if (spfzattribute.scaleX > 0)
    {
      spfzrect.x = spfzattribute.x + adjustX;
    }
    else
    {
      spfzrect.x = (spfzattribute.x - adjustX) - spfzrect.width;
    }
    spfzrect.y = spfzattribute.y + adjustY;

    return spfzrect;
  }

  public Rectangle setcross()
  {
    float box = spfzrect.width * .5f;
    crossrect.set(spfzrect.x + (box * .5f),
      spfzrect.y, spfzrect.width * .5f,
      spfzrect.height);

    return crossrect;
  }

  @Override
  public void storeinputs()
  {
    // iterate through the buffer and store the integer value which
    // signifies
    // the inputs
    // LEFT = 0 UP = 2 LEFT & UP = 4 RIGHT & DOWN = 6
    // RIGHT = 1 DOWN = 3 RIGHT & UP = 5 LEFT & DOWN = 7 NEUTRAL = 8

    if (buff == 15)
    {
      last16.clear();
      last = 0;
    }
    if (buffer.size() == buffsize)
    {
      buffer.clear();
      buff = 0;
      System.out.println("buffer has been cleared");

    }
    buffer.add(buff, null);

    for (int j = 0; j < p1movement.size(); j++)
    {
      if (buffer.size() > 0)
      {

        if (p1movement.get(j))
        {
          buffer.set(buff, j);

          j = p1movement.size();

        }

      }
    }
    if (buffer.get(buff) == null)
    {
      buffer.set(buff, 8);
    }

    if (buff >= 43 && buff <= 59)
    {
      last16.add(buffer.get(buff));
      /*
       * System.out.println(buffer.get(buff) + " added to buffer. ---- index[" +
       * (buffer.size() - 1) + "] ------------ last stored input -----------" +
       * (last16.get(last)));
       */
      last++;
    }
    else
    {

      /*
       * System.out.println(buffer.get(buff) + " added to buffer. ---- index[" +
       * (buffer.size() - 1) + "]");
       */
    }
    System.out.println("Buffer input ----- " + buffer.get(buff));
    buff++;
  }

  public void spwnPrj(String projectile)
  {
    int move = 0;

    String charac = stage.characters.get(stage.p1).substring(0, 1) + "proj";
    float boxX = stage.player1data.get(stage.p1).get(PROJ_POS_DIM_X).get(move).floatValue();
    float boxY = stage.player1data.get(stage.p1).get(PROJ_POS_DIM_Y).get(move).floatValue();
    float boxW = stage.player1data.get(stage.p1).get(PROJ_POS_DIM_W).get(move).floatValue();
    float boxH = stage.player1data.get(stage.p1).get(PROJ_POS_DIM_H).get(move).floatValue();

    int[] projdata = {stage.player1data.get(stage.p1).get(PROJ_STARTUP_START).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_STARTUP_END).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_LOOP_START).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_LOOP_END).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_END_START).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_END_FINAL).get(move).intValue() - 1,
      stage.player1data.get(stage.p1).get(PROJ_TYPE).get(move).intValue(),
      stage.player1data.get(stage.p1).get(PROJ_SPEED).get(move).intValue(),
      stage.player1data.get(stage.p1).get(PROJ_SPAWN).get(move).intValue(),};


    Rectangle hitbox = new Rectangle(boxX, boxY, boxW, boxH);


    Entity project, proj;

    CompositeItemVO projVO = stage.access.land.loadVoFromLibrary(charac);
    projVO.layerName = "players";
    project = stage.access.land.entityFactory.createEntity(stage.access.root.getEntity(), projVO);
    project.getComponent(ZIndexComponent.class).setZIndex(100);
    project.getComponent(ZIndexComponent.class).needReOrder = false;
    project.getComponent(ZIndexComponent.class).layerName = "players";
    project.getComponent(TintComponent.class).color.a = 0f;
    stage.access.land.entityFactory.initAllChildren(stage.access.land.getEngine(), project, projVO.composite);
    stage.access.land.getEngine().addEntity(project);
    ItemWrapper projwrapper = new ItemWrapper(project);
    proj = projwrapper.getChild("projectile").getEntity();


    if (spfzattribute.scaleX > 0)
    {
      project.getComponent(TransformComponent.class).x = spfzattribute.x + stage.player1data.get(stage.p1).get(PROJ_POSX).get(move).floatValue();
      hitbox.x += project.getComponent(TransformComponent.class).x;
    }
    else
    {
      project.getComponent(TransformComponent.class).x = spfzattribute.x - stage.player1data.get(stage.p1).get(PROJ_POSX).get(move).floatValue();
    }
    project.getComponent(TransformComponent.class).y = spfzattribute.y + stage.player1data.get(stage.p1).get(PROJ_POSY).get(move).floatValue();
    hitbox.y += project.getComponent(TransformComponent.class).y;

    SPFZProjectile newproj = new SPFZProjectile(hitbox, projdata, proj, stage.access.land);
    projwrapper.addScript((IScript) newproj);
    this.projectile = newproj;
    attacking = true;
    //projact = true;
    special = false;


  }


  public void up(float delta)
  {
    // if user is pressing down UP on the control pad

    p1movement.set(0, false);
    p1movement.set(1, false);
    p1movement.set(2, true);
    p1movement.set(4, false);
    p1movement.set(5, false);

    if (isLeft)
    {
      p1movement.set(2, false);
      p1movement.set(4, true);
      p1movement.set(5, false);
    }
    else if (isRight)
    {
      p1movement.set(2, false);
      p1movement.set(4, false);
      p1movement.set(5, true);
    }
  }

  public void processJump()
  {
    if (!attacking && !attacked && !dash && !stage.standby)
    {

      if (stage.GROUND == spfzrect.y || walljump)
      {
        if (!walljump)
        {
          walkandjump.y = jumpspeed;
        }

        if (isRight)
        {
          jumpdir = true;
          isJumping = true;

          if (walljump && spfzattribute.scaleX > 0)
          {
            walkandjump.y = jumpspeed / 4;
            walljump = false;
          }
        }
        else if (isLeft)
        {
          jumpdir = false;
          isJumping = true;

          if (walljump && spfzattribute.scaleX < 0)
          {
            walkandjump.y = jumpspeed / 4;
            walljump = false;
          }
        }
      }
    }


  }

  public SPFZProjectile currproj()
  {

    if (projectile != null)
    {
      if (projectile.composite == null)
      {
        projectile = null;
      }
      return projectile;
    }
    else
    {
      return null;
    }
  }

  public float rtnFrametime(float frames)
  {
    //return frames / 60f;
    return frames / spfzp1vals.animFPS.get(spfzp1vals.anims.indexOf(spfzanimation.currentAnimation));
  }

  @Override
  public void update()
  {

  }

  @Override
  public void returnmove()
  {

  }

  @Override
  public boolean hit()
  {
    if (stage.spfzp1move.blk || stage.spfzp1move.dblk)
    {
      hit = false;
    }
    else
    {
      hit = true;
    }
    return hit;
  }

  public void reflect()
  {

  }

  public void parry()
  {

  }

  public float currTotalFrames()
  {
    return spfzanimationstate.currentAnimation.getKeyFrames().length;
  }

  @Override
  public Rectangle setcharbox()
  {
    spfzcharrect = spfzrect;
    return spfzcharrect;
  }

  public Rectangle dimrectangle()
  {
    if (spfzattribute.scaleX > 0)
    {
      dimrect.set(spfzattribute.x, spfzattribute.y, spfzdim.width, spfzdim.height);
    }
    else
    {
      dimrect.set(spfzattribute.x - spfzdim.width, spfzattribute.y, spfzdim.width, spfzdim.height);
    }
    return dimrect;
  }

  @Override
  public ShapeRenderer drawcharbox()
  {
    return spfzcharbox;
  }

  @Override
  public Rectangle setrflbox()
  {
    return null;
  }

  @Override
  public ShapeRenderer drawrflbox()
  {
    return null;
  }
}