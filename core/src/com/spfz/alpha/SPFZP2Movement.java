package com.spfz.alpha;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.uwsoft.editor.renderer.components.ActionComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.LayerMapComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.ZIndexComponent;
import com.uwsoft.editor.renderer.components.label.LabelComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import com.uwsoft.editor.renderer.data.FrameRange;
import com.uwsoft.editor.renderer.scripts.IScript;
import com.uwsoft.editor.renderer.systems.action.Actions;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

public class SPFZP2Movement implements IScript, Attribs, BufferandInput
{

  boolean isRight, isLeft, isUp, isDown, isPunch, isKick, special, hit, blk, dblk, left, pushed, right, isJumping,
    jumpdir, attacking, attacked, blocking, ownatk, projhit, projact, confirm, walljump, kickstuck, punchstuck,
    reflect, dash, bounced, roll, invul, stop;

  CharAttributes spfzp2vals;

  SPFZStage stage;
  Vector2 walkandjump, dashpoints;

  float stun, gravity, jumpspeed, walkspeed, stateTime = 0, rfltime, intpush, adjustX, adjustY, movX, startpt, dashspeed, tempspeed, lastmovX, origStart;

  final float REFLECT = 1f, COMBO_PUSH_L = 4f, COMBO_PUSH_M = 3f, COMBO_PUSH_H = 2f, GRAV_MULT_L = .90f,
    GRAV_MULT_M = .80f, GRAV_MULT_H = .75f, FL_L = .5f, FL_M = .6f, FL_H = .7f;

  int combocount, currentframe, lastcount, buff, move, buffsize = 60, dashdir;

  int[] activeframes;

  final int BLKSTN = 0;
  final int HITSTN = 1;
  final int BLKDIST = 2;
  final int HITDIST = 3;
  final int ACTSTARTBOX = 4;
  final int ACTENDBOX = 5;
  final int BOXX = 6;
  final int BOXY = 7;
  final int BOXWIDTH = 8;
  final int BOXHEIGHT = 9;
  final int BLKDMG = 10;
  final int HITDMG = 11;
  final int BLKMTR = 12;
  final int HITMTR = 13;
  final int FMOVE = 14;
  final int BMOVE = 15;
  final int JUGG = 16;
  final int BACK_START = 17;
  final int FWD_START = 18;
  final int BACK_ACTIVE = 19;
  final int FWD_ACTIVE = 20;
  final int BACK_RECOV = 21;
  final int FWD_RECOV = 22;
  final int PROJ_STARTUP_START = 23;
  final int PROJ_STARTUP_END = 24;
  final int PROJ_LOOP_START = 25;
  final int PROJ_LOOP_END = 26;
  final int PROJ_END_START = 27;
  final int PROJ_END_FINAL = 28;
  final int PROJ_TYPE = 29;
  final int PROJ_SPEED = 30;
  final int PROJ_POSX = 31;
  final int PROJ_POSY = 32;
  final int PROJ_POS_DIM_W = 33;
  final int PROJ_POS_DIM_H = 34;
  final int PROJ_POS_DIM_X = 35;
  final int PROJ_POS_DIM_Y = 36;
  final int PROJ_SPAWN = 37;

  List<Boolean> p2movement = new ArrayList<>();
  List<Boolean> lastp2movement = new ArrayList<>();
  List<Integer> buffer = new ArrayList<>();
  List<Integer> last16 = new ArrayList<>();

  Entity spfzentity;

  ActionComponent spfzaction;
  TransformComponent spfzattribute;
  DimensionsComponent spfzdim;

  Rectangle spfzrect, spfzhitrect, spfzcharrect, spfzrflrect, crossrect, dimrect;
  ShapeRenderer spfzsr, spfzhitbox, spfzcharbox, spfzrflbox, spfzdimbox;

  SPFZProjectile projectile;

  SpriteAnimationComponent spfzanimation;
  SpriteAnimationStateComponent spfzanimationstate;

  String[] loopanims = {"FWLK", "BWLK", "IDLE", "CRCH"};

  String lastanim;

  Vector2 hitboxsize, posofhitbox;

  public SPFZP2Movement(SPFZStage screen)
  {
    stage = screen;
  }

  @Override
  public void act(float delta)
  {
    if (reflect)
    {
      //setreflect();
    }

    if (!reflect && rfltime < REFLECT)
    {
      //rfltime += Gdx.graphics.getDeltaTime();
    }
    else
    {
      if (rfltime >= REFLECT)
      {
        reflect = true;
      }

      if (reflect && rfltime > 0f)
      {
        // rfltime -= Gdx.graphics.getDeltaTime();

        if (rfltime <= 0)
        {
          //reflect = false;
          //isUp = true;
        }
      }
    }

    if (!stage.gameover)
    {

      movement(delta);

      if (isUp)
      {
        isUp = false;
      }
      stage.setface();

    }

    animlogic();

    if(attacked)
    {
      setstun();
    }

    boundlogic();
    jumplogic(delta);

    // storeinputs();

    if (buffer.size() == 60)
    {
      returnmove();
    }
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

  public void animlogic()
  {
    String tempanim = spfzanimation.currentAnimation;
    boolean looper = false;
    float currAnimTime = spfzanimationstate.time;

    for (String anim : loopanims)
    {
      if (tempanim.equals(anim))
      {
        looper = true;
      }
    }

    if (attacked)
    {
      lastcount = combocount;
      intpush = 1;

      attacking = false;
      tempanim = attackedlogic(spfzanimation.currentAnimation);

      if (tempanim.equals(spfzanimation.currentAnimation))
      {
        spfzanimation.currentAnimation = tempanim;
      }


    }

    // If current animation is anything other than "neutral" or "movement", it
    // must be
    // timed as it is not looping

    if (!looper)
    {
      //if is any of the non listed animations within the method
      if (attackedStatus())
      {
        setneutral();
        if (roll)
        {
          roll = false;
        }
      }
      else
      {
        if (spfzrect.y <= stage.GROUND && spfzanimation.currentAnimation.equals("RROLL"))
        {
          recovery(spfzanimation.currentAnimation);
        }
      }

      // grab the current frame of animation
      //currentframe = spfzanimationstate.currentAnimation.getKeyFrameIndex(stateTime);
      currentframe = spfzanimationstate.currentAnimation.getKeyFrameIndex(currAnimTime);

      // once the animation is complete, return the character back to the
      // neutral state
      //if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime))
      if (spfzanimationstate.currentAnimation.isAnimationFinished(currAnimTime))
      {
        setneutral();
        if (roll)
        {
          roll = false;
        }
      }
    }

    if (tempanim.equals(lastanim))
    {
      lastanim = tempanim;
      spfzanimation.fps = spfzp2vals.animFPS.get(spfzp2vals.anims.indexOf(spfzanimation.currentAnimation));

      if(!looper)
      {
        spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps, Animation.PlayMode.NORMAL);
      }
      else
      {
        spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps, Animation.PlayMode.LOOP);
      }
    }
  }

  //CONVERT METHOD TO STAGE CLASS
  public void combocount()
  {
    // combo counter and state time should remain 0 until an event occurs such
    // as
    // This class ing, or being attacked
    if (stage.shake)
    {
      stage.shake = false;
    }
    if (combocount != 0)
    {
      LabelComponent count;
      count = stage.access.root.getChild("ctrlandhud").getChild("combocount2").getEntity()
        .getComponent(LabelComponent.class);

      Entity Hit = stage.access.root.getChild("ctrlandhud").getChild("p1himg").getEntity();
      Entity cc = stage.access.root.getChild("ctrlandhud").getChild("p1cc").getEntity();

      Actions.addAction(Hit, Actions.sequence(Actions.delay(.5f), Actions.scaleTo(stage.SCALE_TEXT, 0, .3f, Interpolation.elastic)));
      Actions.addAction(cc, Actions.sequence(Actions.delay(.5f), Actions.scaleTo(stage.SCALE_TEXT, 0, .3f, Interpolation.elastic)));

      count.setText(" ");
      combocount = 0;
    }
  }
  public void recovery(String tempanim)
  {

      stun = 0;

      if (stage.spfzp1move.wallb || stage.spfzp1move.bouncer)
      {
        stage.spfzp1move.wallb = false;
        stage.spfzp1move.bouncer = false;
      }
      bounced = false;

  }

  public void hitProcess()
  {
    spfzanimationstate.time = 0;
    recoilLogic();

  }

  public String attackedlogic(String tempanim)
  {
    //test blocking
    //blocking = true;
    //blk = true;

    if (blocking)
    {
      if (spfzrect.y == stage.GROUND)
      {
        if (isDown)
        {
          tempanim = "CBLK";
        }
        else
        {
          tempanim = "SBLK";
        }
      }
      else
      {
        tempanim = "ABLK";
      }
    }
    else
    {
      tempanim = "SATKD";
      if (spfzrect.y > stage.GROUND)
      {
        tempanim = "AATKD";
        floatchar();
      }
    }
    return tempanim;
  }

  public void recoilLogic()
  {
    if (!stage.spfzp1move.wallb)
    {
      //pushback logic
      if (spfzrect.y <= stage.GROUND)
      {
        pushback();
      }
      else
      {
        float switcher;

        if (center() < stage.spfzp1move.center())
        {
          switcher = -1f;
        }
        else
        {
          switcher = 1f;
        }
        spfzattribute.x += (COMBO_PUSH_M * switcher);
      }
    }
    else
    {
      if (bounced)
      {
        wallbouncelogic(true);
      }
      else
      {
        wallbouncelogic(false);
      }
    }

  }

  public void damage()
  {
    // Set counter display to 0
    if (stage.spfzp1move.combonum() == 0)
    {
      LabelComponent combocount1;
      combocount1 = stage.access.root.getChild("ctrlandhud").getChild("combocount1").getEntity()
        .getComponent(LabelComponent.class);
      combocount1.setText(" ");
      if (stage.training)
      {
        stage.p2health = stage.startp2;
      }
    }
  }

  public boolean attackedStatus()
  {
    boolean notany = false;
    boolean onehit;
    String[] anims = {"SBLK", "CBLK", "ABLK", "SATKD", "CATKD", "AATKD", "RROLL"};

    for (int i = 0; i < anims.length; i++)
    {
      if (anims[i].equals(spfzanimation.currentAnimation))
      {
        notany = true;
      }
      else
      {
        i = anims.length;
      }
    }
    return notany;
  }

  public void pushback()
  {
    // modifier from file
    float pushback = 0;
    float stun = 0;

    if (blk || dblk)
    {
      if (stage.spfzp1move.move != -1)
      {
        pushback = rtnFrametime(stage.player1data.get(stage.p1).get(BLKDIST).get(stage.spfzp1move.move).floatValue());
        stun = rtnFrametime(stage.player1data.get(stage.p1).get(BLKSTN).get(stage.spfzp1move.move).floatValue());
      }

    }
    else
    {
      if (stage.spfzp1move.move != -1)
      {

        pushback = rtnFrametime(stage.player1data.get(stage.p1).get(HITDIST).get(stage.spfzp1move.move).floatValue());
        stun = rtnFrametime(stage.player1data.get(stage.p1).get(BLKDIST).get(stage.spfzp1move.move).floatValue());
      }
      else
      {
        //pushback = rtnFrametime(110f) - stateTime;
        pushback = rtnFrametime(110f) - spfzanimationstate.time;
      }
    }
    //pushed = true;

    //if character is attacked while still in blockstun, reset the actions(pushback)
    Actions.removeActions(spfzentity);

    float phase1 = .05f;
    float phase2 = .08f;
    float phase3 = .01f;

    if (stage.spfzp1move.center() > center())
    {
      pushback *= -1;
    }

    Actions.addAction(spfzentity,
      Actions.sequence(
        Actions.moveBy(pushback * phase1, 0, stun * phase1, Interpolation.sineOut),
        Actions.moveBy(pushback * phase2, 0, stun * phase2, Interpolation.sineOut),
        Actions.moveBy(pushback * phase3, 0, stun * phase3, Interpolation.sineOut)));

  }

  private void wallbouncelogic(boolean push)
  {
    float side;

    if (spfzattribute.scaleX < 0)
    {
      side = 1f;
    }
    else
    {
      side = -1f;
    }
    if (push)
    {
      spfzattribute.x += 25f * side;
      if (spfzrect.y == stage.GROUND)
      {
        Actions.addAction(spfzentity, Actions.moveBy(0, 50f, .5f));
      }

    }
    else
    {
      if (side > 0)
      {
        side = -1f;
      }
      else
      {
        side = 1f;
      }
      spfzattribute.x += 7.5f * side;
    }
  }

  //mechanic that activates when character lands from being attacked in air
  private void rollback(String roll)
  {
    float switcher;
    float rollamt = 0;

    if (spfzattribute.scaleX > 0)
    {
      switcher = -1f;
    }
    else
    {
      switcher = 1f;
    }

    switch (roll)
    {
      case "L":
        rollamt = 20f;
        break;
      case "M":
        rollamt = 30f;
        break;
      case "H":
        rollamt = 40f;
        break;

    }

    Actions.addAction(spfzentity,
      Actions.sequence(Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            spfzanimation.currentAnimation = "RROLL";
          }
        }), Actions.moveBy(rollamt * switcher, 0, .5f, Interpolation.circleOut),
        Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            invul = false;
            setneutral();
          }
        })
      ));

  }

  public void setstun()
  {
    float totalStun = stage.spfzp1move.rtnFrametime(stage.spfzp1move.currTotalFrames()) - stage.spfzp1move.rtnFrametime(stage.spfzp1move.currentframe());

    if (blk || dblk)
    {
      if (stage.spfzp1move.move != -1 && stun == 0)
      {

        stun = rtnFrametime(stage.player1data.get(stage.p1).get(BLKSTN).get(stage.spfzp1move.move).floatValue() + totalStun);
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
      if (spfzrect.y == stage.GROUND)
      {
        if (stage.spfzp1move.move != -1 && stun == 0)
        {
          stun = rtnFrametime(stage.player1data.get(stage.p1).get(HITSTN).get(stage.spfzp1move.move).floatValue() + totalStun);
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
        //character being air attacked
        stun = 2f;
      }
    }

    //set the animation frame rate based on the "stun" value.
    float frameRate = currTotalFrames() / stun;

    spfzanimation.fps = (int) frameRate;

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

  public void crosslogic(float curpos, float last)
  {
    float right = crossrect.x + (crossrect.width * .7f);
    float left = crossrect.x + (crossrect.width * .3f);
    float p1 = stage.spfzp1move.crossrect.x;
    float p2center = center();
    float p1center = stage.spfzp1move.center();
    float p1full = stage.spfzp1move.crossrect.x + stage.spfzp1move.crossrect.width;

    if (p1 > stage.stageboundary[0] && p1full < stage.stageboundary[1])
    {
      if (p2center > p1center && p1full > left)
      {
        //push right
        curpos += (p1full - left) * .5f;
      }
      else if (p2center < p1center && p1 < right)
      {
        //push left
        curpos -= (right - p1) * .5f;
      }
      spfzattribute.x = curpos;
    }
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

  @Override
  public int combonum()
  {
    return combocount;
  }


  public void createHitBox()
  {
    posofhitbox.setZero();
    hitboxsize.setZero();
    float boxX;
    float boxY = spfzattribute.y + stage.player1data.get(stage.p1).get(BOXY).get(move).floatValue();
    float sizeW;
    float sizeH;

    if (spfzattribute.scaleX > 0)
    {
      boxX = this.center() + stage.player1data.get(stage.p1).get(BOXX).get(move).floatValue();
      sizeW = stage.player1data.get(stage.p1).get(BOXWIDTH).get(move).floatValue();
      sizeH = stage.player1data.get(stage.p1).get(BOXHEIGHT).get(move).floatValue();
    }
    else
    {
      boxX = this.center() - stage.player1data.get(stage.p1).get(BOXX).get(move).floatValue() - stage.player1data.get(stage.p1).get(BOXWIDTH).get(move).floatValue();
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

    posofhitbox.x = boxX;
    posofhitbox.y = boxY + (spfzrect.y - boxY);
    hitboxsize.x = sizeW;
    hitboxsize.y = sizeH;


  }

  @Override
  public int currentframe()
  {
    return currentframe;
  }

  public SPFZProjectile currproj()
  {
    if (projectile != null)
    {
      return projectile;
    }
    else
    {
      return null;
    }
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

    p2movement.set(0, false);
    p2movement.set(1, false);
    p2movement.set(3, true);
    p2movement.set(6, false);
    p2movement.set(7, false);

    if (isLeft)
    {
      p2movement.set(3, false);
      p2movement.set(6, false);
      p2movement.set(7, true);
    }
    else if (isRight)
    {
      p2movement.set(3, false);
      p2movement.set(6, true);
      p2movement.set(7, false);
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

  public float charY()
  {
    return spfzrect.y - spfzattribute.y;
  }

  public float charGROUND()
  {
    return stage.GROUND - (spfzrect.y - spfzattribute.y);
  }

  @Override
  public boolean getboxconfirm()
  {

    return confirm;
  }

  @Override
  public boolean invul()
  {
    return invul;
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
      spfzp2vals = new CharAttributes(mc.get(entity).itemIdentifier, stage.player2data.size(),
        stage.device, stage.access.android);

    }
    else if (stage.device == 1)
    {
      spfzp2vals = new CharAttributes(mc.get(entity).itemIdentifier, stage.player2data.size());
    }
    spfzsr = new ShapeRenderer();
    spfzhitbox = new ShapeRenderer();
    spfzcharbox = new ShapeRenderer();
    spfzrflbox = new ShapeRenderer();
    spfzdimbox = new ShapeRenderer();
    dimrect = new Rectangle();
    spfzrect = new Rectangle(spfzp2vals.getCharDims());
    adjustX = spfzrect.x;
    adjustY = spfzrect.y;
    //spfzrect = new Rectangle();
    crossrect = new Rectangle();
    spfzhitrect = new Rectangle();
    spfzcharrect = new Rectangle();
    spfzrflrect = new Rectangle();

    activeframes = new int[]{0, 0};
    hitboxsize = new Vector2(0, 0);
    posofhitbox = new Vector2(0, 0);

    spfzentity = entity;
    setanimations();

    // get the values of the character from the Character attributes class
    gravity = spfzp2vals.getGravity();
    jumpspeed = spfzp2vals.getJump();
    walkspeed = spfzp2vals.getWalkspeed();
    walkandjump = spfzp2vals.getWandj();

    setPos();

    // System.out.println("character created");

  }

  @Override
  public boolean isplayerone()
  {
    return false;
  }

  public void jumplogic(float delta)
  {
    if (!attacked)
    {
      // Apply gravity for spfzattribute calculations
      if (spfzrect.y > stage.GROUND)
      {
        walkandjump.y += gravity;
      }
      // assign the new jump value to the . attribute to
      // apply gravity to the spfzattribute

      if (isJumping)
      {
        if (spfzrect.y > stage.GROUND)
        {
          if (spfzrect.y > stage.GROUND + stage.WALLJRES
            && (stage.cam.position.x <= stage.camboundary[0] + 1 || stage.cam.position.x + 1 >= stage.camboundary[1])
            && ((spfzattribute.x <= stage.stageboundary[0] && spfzattribute.scaleX > 0)
            || spfzattribute.x + 1 >= stage.stageboundary[1] && spfzattribute.scaleX < 0))
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
                spfzattribute.x += walkandjump.x * .0150f;
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
          isJumping = false;
        }
      }

      // If spfzattribute has reached the boundary of the ground, set to the
      // boundary of the ground
      if (spfzrect.y < stage.GROUND)
      {
        if (spfzanimation.currentAnimation.equals("AATKD"))
        {
          if (!roll)
          {
            spfzanimation.currentAnimation = "RROLL";
            rollback(stage.spfzp1move.weight);
            roll = true;
          }
        }
        else
        {
          if (stage.access.root.getChild("p2land").getChild("landp2").getEntity()
            .getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)
          {
            stage.access.root.getChild("p2land").getChild("landp2").getEntity()
              .getComponent(SPFZParticleComponent.class).pooledeffects
              .removeValue(stage.access.root.getChild("p2land").getChild("landp2").getEntity()
                .getComponent(SPFZParticleComponent.class).pooledeffects.get(0), true);
          }
          stage.access.root.getChild("p2land").getEntity().getComponent(TransformComponent.class).x = this.center();
          stage.access.root.getChild("p2land").getEntity().getComponent(TransformComponent.class).y = stage.GROUND;
          stage.access.root.getChild("p2land").getChild("landp2").getEntity()
            .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
          stage.access.root.getChild("p2land").getChild("landp2").getEntity()
            .getComponent(TransformComponent.class).scaleX = 1f;
          stage.access.root.getChild("p2land").getChild("landp2").getEntity().getComponent(SPFZParticleComponent.class)
            .startEffect();
          attacking = false;
          confirm = false;

        }
      }
    }
    else
    {
      //Apply gravity to the opponent as they are being attacked

      if (spfzrect.y > stage.GROUND)
      {
        // all air logic for when character is being attacked
        if (stage.spfzp1move.weight != null)
        {
          applygrav();

        }
        if (spfzrect.y <= stage.GROUND + 30 && walkandjump.y < 0)
        {

          invul = true;
        }
        else if (spfzrect.y <= stage.GROUND + 30 && walkandjump.y > 0)
        {
          invul = false;
        }
      }
    }

    if (spfzrect.y == stage.GROUND && spfzanimation.currentAnimation.equals("AATKD") && !roll)
    {
      spfzanimation.currentAnimation = "RROLL";
      rollback(stage.spfzp1move.weight);
      roll = true;

    }
    if (spfzrect.y < stage.GROUND)
    {
      walkandjump.y = 0;
      spfzattribute.y = charGROUND();
      if (spfzanimation.currentAnimation.equals("AATKD"))
      {
        // set code to perform ground recovery animation
        // set code to also push character back said distance based on the
        // weight of the last used move
        //stateTime = stun;

        spfzanimation.currentAnimation = "RROLL";
        rollback(stage.spfzp1move.weight);
        roll = true;

      }
    }
    // apply y value after all calculations
    spfzattribute.y += walkandjump.y;

  }

  public void kick()
  {

    spfzanimation.currentAnimation = "attack";
    attacking = true;

    createHitBox();

  }

  public float left(float delta, float move)
  {
    // If sprite is on the ground
    if (spfzrect.y == stage.GROUND)
    {

      // move sprite horizontally
      if (!dash && !stop)
      {
        return move -= walkandjump.x * delta;
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
    // isRight = true;
    // isLeft = true;
    // isDown = true;
    movX = spfzattribute.x;
    lastmovX = spfzattribute.x;

    stage.p2xattr = spfzattribute.x;

    if (!stage.standby)
    {
      if (isLeft)
      {
        p2movement.set(0, true);
        //if (!attacking && !attacked && !isDown)
        if (!attacking && attacked)
        {
          // If Player one is on the right side, and their center is greater
          // than
          // Player 2's center, make player 2 block

          if (stage.spfzp1move.center() > center())
          {
            blocking = true;
            blk = true;
            if (isDown)
            {
              dblk = true;
              blk = false;
            }
          }
          else
          {
            blk = false;
          }
        }
        else if (!attacking && !attacked)
        {
          //left(delta);
          movX = left(delta, movX);
        }

      }
      else
      {
        p2movement.set(0, false);
        // blk needs to be reset here
        if (blk)
        {
          blk = false;
        }
      }

      if (isRight)
      {
        p2movement.set(1, true);

        // If Player one is on the left side, and their center is greater
        // than
        // Player 2's center, make player 2 block
        if (!attacking && attacked)
        {
          // If Player one is on the right side, and their center is greater
          // than
          // Player 2's center, make player 2 block

          if (stage.spfzp1move.center() < center())
          {
            blocking = true;
            blk = true;
            if (isDown)
            {
              dblk = true;
              blk = false;
            }
          }
          else
          {
            blk = false;
          }
        }
        else if (!attacking && !attacked)
        {
          //right(delta);
          movX = right(delta, movX);
        }
      }
    }
    else
    {
      p2movement.set(1, false);
    }

    if (isUp)
    {
      up(delta);
    }
    else
    {
      p2movement.set(2, false);
      p2movement.set(4, false);
      p2movement.set(5, false);
    }

    if (isDown)
    {
      down(delta);
    }
    else
    {
      p2movement.set(3, false);
      p2movement.set(6, false);
      p2movement.set(7, false);
    }

    if (isPunch && !attacked)
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
    if (isKick && !attacked)
    {
      if (attacking)
      {
        kickstuck = true;
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
    //}


    if (dash)
    {
      dashlogic();
    }
    else
    {
      spfzattribute.x = movX;
    }

    if (setcross().overlaps(stage.spfzp1move.setcross()))
    {
      crosslogic(movX, lastmovX);
    }


  }

  public void punch()
  {
    // if user is pressing down punch on the control pad

    if (special)
    {
      stage.switchp1 = true;
      stage.switchcount = 0;
    }
    else
    {

      // if(spfzattribute.y > stage.GROUND)
      // {
      spfzanimation.currentAnimation = "attack";
      // }
      attacking = true;

      createHitBox();

    }
  }

  @Override
  public void returnmove()
  {
    int[] qcf = {3, 6, 1};
    special = false;
    int maxdur = 60;
    for (int i = 0; i < maxdur - (qcf.length - 1); i++)
    {

      int j = 0;
      if (buffer.get(i) != null)
      {
        if (buffer.get(i) == qcf[j])
        {
          j++;
          if (buffer.get(i + j) == qcf[j])
          {
            j++;
            if (buffer.get(i + j) == qcf[j])
            {
              special = true;
              i = maxdur - (qcf.length - 1);
            }
          }
        }
      }
    }
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
        return move += walkandjump.x * delta;
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
    NodeComponent nc = new NodeComponent();
    // int start;
    // int end;

    nc = ComponentRetriever.get(spfzentity, NodeComponent.class);
    spfzaction = ComponentRetriever.get(spfzentity, ActionComponent.class);
    spfzattribute = ComponentRetriever.get(spfzentity, TransformComponent.class);
    spfzattribute.y = charGROUND();

    spfzdim = ComponentRetriever.get(spfzentity, DimensionsComponent.class);
    spfzattribute.x = stage.STAGE_CENTER + stage.CHAR_SPACE;
    spfzanimation = ComponentRetriever.get(nc.children.get(0), SpriteAnimationComponent.class);
    spfzanimationstate = ComponentRetriever.get(nc.children.get(0), SpriteAnimationStateComponent.class);

    List<String> keys = new ArrayList<>(spfzp2vals.animations.keySet());

    // create frame ranges for all animations listed for each character
    for (int i = 0; i < spfzp2vals.getAnimations().size(); i++)
    {

      spfzanimation.frameRangeMap.put(keys.get(i), new FrameRange(keys.get(i),
        spfzp2vals.animations.get(keys.get(i))[0], spfzp2vals.getAnimations().get(keys.get(i))[1]));
    }
    /*spfzanimation.currentAnimation = "IDLE";
    spfzanimationstate.set(spfzanimation.frameRangeMap.get("IDLE"), 60, Animation.PlayMode.LOOP);*/
    setneutral();
  }

  public void setcombonum(int comboint)
  {
    combocount = comboint;
  }

  @Override
  public Rectangle sethitbox()
  {
    return spfzhitrect;
  }

  public void setPos()
  {

    if (stage.switchp2)
    {
      spfzattribute.x = stage.p2xattr;
    }
    else
    {
      float startpos;

      if (origStart == 0)
      {
        startpos = stage.STAGE_CENTER + stage.CHAR_SPACE;
        startpos = (startpos + adjustX) + (spfzrect.width * .5f);
        origStart = startpos;
      }
      else
      {
        startpos = origStart;
      }

      spfzattribute.x = startpos;
      spfzattribute.y = charGROUND();
      if (spfzattribute.scaleX > 0)
      {
        spfzattribute.scaleX *= -1f;
      }
    }

  }

  public void dashlogic()
  {


    if ((spfzanimation.currentAnimation.equals("BDASH") || spfzanimation.currentAnimation.equals("FDASH")) ||
      dash && spfzrect.y == stage.GROUND && currentframe() >= 3 && currentframe() <= currTotalFrames() - 3)
    {
      float totalFrames = currTotalFrames();
      float totalFrmTime = rtnFrametime(totalFrames);
      //float progress = stateTime / totalFrmTime;
      float progress = spfzanimationstate.time / totalFrmTime;

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

      //if (spfzanimationstate.currentAnimation.isAnimationFinished(stateTime))
      if (spfzanimationstate.currentAnimation.isAnimationFinished(spfzanimationstate.time))
      {
        walkandjump.x = tempspeed;
        startpt = 0;
      }

    }
  }

  public void setneutral()
  {

    //stateTime = 0;
    attacking = false;
    attacked = false;
    pushed = false;
    spfzanimation.currentAnimation = "IDLE";
    if (isDown)
    {
      spfzanimation.currentAnimation = "CRCH";
    }

    spfzanimation.fps = spfzp2vals.animFPS.get(spfzp2vals.anims.indexOf(spfzanimation.currentAnimation));
    spfzanimationstate.set(spfzanimation.frameRangeMap.get(spfzanimation.currentAnimation), spfzanimation.fps,
      Animation.PlayMode.LOOP);
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
    crossrect.set(spfzrect.x + (box * .3f),
      spfzrect.y, spfzrect.width * .7f,
      spfzrect.height);

    return crossrect;
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

  @Override
  public void storeinputs()
  {
    // iterate through the buffer and store the integer value which signifies
    // the inputs
    // LEFT = 0 UP = 2 LEFT & UP = 4 RIGHT & DOWN = 6
    // RIGHT = 1 DOWN = 3 RIGHT & UP = 5 LEFT & DOWN = 7 NEUTRAL = 8
    // for (int i = 0; i < buffsize; i++)
    // {
    if (buffer.size() == buffsize)
    {
      buffer.clear();
      buff = 0;
      System.out.println("buffer has been cleared");
    }
    buffer.add(buff, null);

    for (int j = 0; j < stage.spfzp1move.p1movement.size(); j++)
    {
      if (buffer.size() > 0)
      {

        if (stage.spfzp1move.p1movement.get(j))
        {
          buffer.set(buff, j);

          j = stage.spfzp1move.p1movement.size();
          // i = buffsize;
        }

      }
    }
    if (buffer.get(buff) == null)
    {
      buffer.set(buff, 8);
    }
    // }
    System.out.println(buffer.get(buff) + " added to buffer");
    buff++;
  }

  public void up(float delta)
  {
    // if user is pressing down UP on the control pad

    p2movement.set(0, false);
    p2movement.set(1, false);
    p2movement.set(2, true);
    p2movement.set(4, false);
    p2movement.set(5, false);

    if (isLeft)
    {
      p2movement.set(2, false);
      p2movement.set(4, true);
      p2movement.set(5, false);
    }
    else if (isRight)
    {
      p2movement.set(2, false);
      p2movement.set(4, false);
      p2movement.set(5, true);
    }

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

  public void setreflect()
  {
    spfzrflrect.width = spfzdim.width * 1.25f;
    spfzrflrect.height = spfzdim.height;
    spfzrflrect.x = center() - (spfzrflrect.width * .5f);
    spfzrflrect.y = spfzattribute.y;

  }

  public void applygrav()
  {

    //works alongside floatchar()
    switch (stage.spfzp1move.weight)
    {
      case "L":
        walkandjump.y += gravity * GRAV_MULT_L;
        break;
      case "M":
        walkandjump.y += gravity * GRAV_MULT_M;
        break;
      case "H":
        walkandjump.y += gravity * GRAV_MULT_H;
        break;
      default:
        walkandjump.y += gravity * GRAV_MULT_H;
        break;
    }
  }

  public void floatchar()
  {
    // Apply y value each time character is attacked. works alongside applygrav()
    walkandjump.y = 0;


    switch (stage.spfzp1move.weight)
    {
      case "L":
        walkandjump.y = jumpspeed * FL_L;
        break;
      case "M":
        walkandjump.y = jumpspeed * FL_M;
        break;
      case "H":
        walkandjump.y = jumpspeed * FL_H;
        break;
      default:
        walkandjump.y = jumpspeed * FL_H;
        break;
    }
  }

  public void shwreflect()
  {
    spfzrflbox.setProjectionMatrix(stage.access.viewportland.getCamera().combined);
    spfzrflbox.begin(ShapeType.Filled);

    spfzrflbox.setColor(Color.PURPLE);

    spfzrflbox.rect(spfzrflrect.x, spfzrflrect.y, spfzrflrect.width, spfzrflrect.height);

    spfzrflbox.end();

    // Reflect logic

    if (stage.spfzp1move.projectile != null)
    {
      if (stage.spfzp1move.projectile.hitbox.overlaps(spfzrflrect))
      {
        stage.spfzp1move.projectile.projectile.getComponent(TransformComponent.class).scaleX *= -1f;
        reflect = false;
      }
    }

  }

  public float currTotalFrames()
  {
    return spfzanimationstate.currentAnimation.getKeyFrames().length;
  }

  public void parry()
  {

  }

  public float rtnFrametime(float frames)
  {
    return frames / 60f;
  }


  @Override
  public void update()
  {

  }

  @Override
  public boolean hit()
  {
    //blk = true;
    if (blk || dblk)
    {
      hit = false;
    }
    else
    {
      hit = true;
    }

    return hit;
  }

  @Override
  public Rectangle setcharbox()
  {
    spfzcharrect = spfzrect;
    return spfzcharrect;
  }

  @Override
  public ShapeRenderer drawcharbox()
  {
    return spfzcharbox;
  }

  @Override
  public Rectangle setrflbox()
  {
    return spfzrflrect;
  }

  @Override
  public ShapeRenderer drawrflbox()
  {
    return spfzrflbox;
  }
}