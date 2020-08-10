package com.spfz.alpha;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.uwsoft.editor.renderer.components.*;
import com.uwsoft.editor.renderer.components.label.LabelComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationComponent;
import com.uwsoft.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import com.uwsoft.editor.renderer.data.CompositeItemVO;
import com.uwsoft.editor.renderer.data.FrameRange;
import com.uwsoft.editor.renderer.data.ProjectInfoVO;
import com.uwsoft.editor.renderer.resources.IResourceRetriever;
import com.uwsoft.editor.renderer.scripts.IScript;
import com.uwsoft.editor.renderer.systems.action.Actions;
import com.uwsoft.editor.renderer.utils.ItemWrapper;


public class SPFZStage extends Stage
{

  boolean isMoving, switchp1, switchp2, faceleft1, faceleft2, faceright1, faceright2, eoround,
    finishedrd, pausetime, gameover, standby, damagedealt, training, pauseconfirm, show, boxes, pauseset, strt1,
    initcheck, setneut, sigp1lock, shake, p1charzoom, p2charzoom;

  Camera cam;

  double timeleftdbl;

  float p1xattr, p1yattr, p2xattr, p2yattr, p1HPpercent, p2HPpercent,
    p1SPpercent, p2SPpercent, begpercent, begsuperpct, stagetempx, stagetempy, reflPCT;

  float[] camboundary, stageboundary = {-240, 880};

  // values necessary for stage processing
  final float GROUND = 120f, WALLJRES = 20f, STAGE_CENTER = 320f, CHAR_SPACE = 100f,
  HALF_WORLDH = 200, HALF_WORLDW = 320, SPECIAL_WINDOW = 15f, SWAP_WINDOW = 15f,
  SCALE_TEXT = .2f;


  long time, timeElapsed, pausedElapsed, showtime;

  static final int MAX_SUPER = 600, MAX_CHARS = 6;

  short btnupdwn, btnlftrgt, camcon;
  int p1, p2, switchcount, timeleft, p1health, p2health, p1spec, p2spec, rdcount, optime, p1rdcount, p2rdcount,
    startp1, startp2, device;

  spfzTrial access;

  Sprite pausetex;

  List<List<ArrayList<Double>>> player1data = new ArrayList<List<ArrayList<Double>>>();
  List<HashMap<String, int[]>> player1anims = new ArrayList<HashMap<String, int[]>>();
  List<ArrayList<String>> player1moves = new ArrayList<ArrayList<String>>();

  List<List<ArrayList<Double>>> player2data = new ArrayList<List<ArrayList<Double>>>();
  List<HashMap<String, int[]>> player2anims = new ArrayList<HashMap<String, int[]>>();
  List<ArrayList<String>> player2moves = new ArrayList<ArrayList<String>>();

  List<LabelComponent> charnames = new ArrayList<LabelComponent>();
  List<String> characters = new ArrayList<String>();
  List<Object> arrscripts = new ArrayList<Object>();

  // Scripts for each character

  SPFZP1Movement spfzp1move;
  SPFZP2Movement spfzp2move;

  String anim, firstpause;

  String[] stagebtntags = {"pausetag", "upbutton", "rightupbutton", "rightbutton", "downrightbutton", "downbutton",
    "downleftbutton", "leftbutton", "leftupbutton", "punch", "kick", "mmbutton", "resbutton", "button"};

  //                  0      1      2      3      4      5      6      7      8     9       10     11    12      13
  String[] normals = {"CLP", "CMP", "CHP", "CLK", "CMK", "CHK", "SLP", "SMP", "SHP", "SLK", "SMK", "SHK", "BJP", "NJP",
    "FJP", "BJK", "NJK", "FJK"};
  // 14     15     16     17


  public SPFZStage(IResourceRetriever i, Camera camera, String stage, int device, float[] bounds,
                   List<String> characters, spfzTrial trial, boolean training)
  {
    this.training = training;
    this.device = device;
    // setup stage
    initStage(i, bounds, camera, stage, trial, characters);


    // setup character scripts
    initScripts();
    charspicked(characters);
    setupcharacters(characters);
    totalhealth();

    setactors();

  }

  public void initStage(IResourceRetriever i, float[] bounds, Camera camera, String stage, spfzTrial trial,
                        List<String> chars)
  {
    ItemWrapper scene = access.root.getChild("ctrlandhud"), timer = scene.getChild("timeranim");

    Entity time = timer.getChild("time").getEntity(), ones = timer.getChild("ones").getEntity(),
      tenths = timer.getChild("tenths").getEntity();

    camboundary = bounds;
    cam = camera;
    access = trial;
    standby = true;
    this.characters = chars;

    timeleft = 10;
    Preferences spfzpref = Gdx.app.getPreferences("spfzfile");
    optime = spfzpref.getInteger("time");
    //timeleft = optime

    Gdx.input.setInputProcessor(this);
    resettimer();

    if (training)
    {
      //Hide the timer
      time.getComponent(TintComponent.class).color.a = 0f;
      /*ones.getComponent(TintComponent.class).color.a = 0f;
      tenths.getComponent(TintComponent.class).color.a = 0f;*/
      ones.getComponent(SpriteAnimationStateComponent.class).paused = true;
      tenths.getComponent(SpriteAnimationStateComponent.class).paused = true;

    }
    else
    {
      scene.getChild("infinite").getComponent(TintComponent.class).color.a = 0f;
    }

    Entity fader = access.root.getChild("fader").getEntity();
    access.setSettings();
    Actions.addAction(fader, Actions.sequence(Actions.delay(1f), Actions.fadeOut(.3f), Actions.run(new Runnable()
    {
      @Override
      public void run()
      {

        roundtextset();

      }
    })));

  }

  public void resettimer()
  {
    // New timer
    Entity tenths = access.root.getChild("ctrlandhud").getChild("timeranim").getChild("tenths").getEntity();
    Entity ones = access.root.getChild("ctrlandhud").getChild("timeranim").getChild("ones").getEntity();

    tenths.getComponent(SpriteAnimationStateComponent.class)
      .set(new FrameRange("tens", (optime % 100) / 10, (optime % 100) / 10), 1, Animation.PlayMode.NORMAL);
    ones.getComponent(SpriteAnimationStateComponent.class)
      .set(new FrameRange("ones" + optime + "", optime % 10, optime % 10), 1, Animation.PlayMode.NORMAL);
    ones.getComponent(SpriteAnimationStateComponent.class).paused = true;
    tenths.getComponent(SpriteAnimationStateComponent.class).paused = true;
  }

  public void initScripts()
  {
    spfzp1move = new SPFZP1Movement(this);

    spfzp2move = new SPFZP2Movement(this);
  }

  public void charspicked(List<String> sprites)
  {
    int keep = 0;
    for (String sprite : sprites)
    {

      // Set ArrayLists to null for initialization

      if (arrscripts.size() < MAX_CHARS)
      {
        arrscripts.add(null);
      }

      // Assign the player one script to belong to the first 3 characters
      // selected
      if (keep <= 2)
      {
        arrscripts.set(keep, spfzp1move);
      }
      // Assign the player two script to belong to the last 3 characters
      // selected
      else
      {
        if (keep <= 5)
        {
          arrscripts.set(keep, spfzp2move);
        }
      }
      keep++;
    }
  }

  public void setShader(String shader, Entity obj)
  {
    String shadersPath = "shaders";

    ShaderProgram alt = new ShaderProgram(Gdx.files.internal(shadersPath + File.separator + shader + ".vert"), Gdx.files.internal(shadersPath + File.separator + shader + ".frag"));
    obj.add(new ShaderComponent());
    obj.getComponent(ShaderComponent.class).setShader(shader, alt);
  }

  public void setupcharacters(List<String> chars)
  {
    p1 = 0;
    p2 = 3;
    playerToStage(chars.get(0), p1).addScript((IScript) arrscripts.get(p1));
    playerToStage(chars.get(3), p2).addScript((IScript) arrscripts.get(p2));


    // this for loop will set the booleans for each directional input for each
    // character
    // These boolean values will be needed to gather input as well as control
    // the CPU

    int inputs = 8;
    // LEFT = 0 UP = 2 LEFT & UP = 4 RIGHT & DOWN = 6
    // RIGHT = 1 DOWN = 3 RIGHT & UP = 5 LEFT & DOWN = 7
    // for (int i = 0; i < chars.size() + 2; i++)
    for (int i = 0; i < inputs; i++)
    {
      spfzp1move.p1movement.add(false);
      spfzp1move.lastp1movement.add(null);
      spfzp2move.p2movement.add(false);
      spfzp2move.lastp2movement.add(null);
    }

    time = System.currentTimeMillis();
  }

  ItemWrapper playerToStage(String player, int playnum)
  {
    Entity hud = access.root.getEntity();
    CompositeItemVO playerComposite = access.land.loadVoFromLibrary(player);
    Entity play;
    ItemWrapper playWrap;
    playerComposite.zIndex  = 0;
    playerComposite.layerName = "players";
    play = access.land.entityFactory.createEntity(hud, playerComposite);
    access.land.entityFactory.initAllChildren(access.land.getEngine(), play, playerComposite.composite);

    play.getComponent(MainItemComponent.class).itemIdentifier = player;

    access.land.getEngine().addEntity(play);

    if (playnum > 2)
    {
      setShader("newdef", play);
    }
    playWrap = new ItemWrapper(play);
    return playWrap;
  }

  public void setactors()
  {
    ItemWrapper controller = access.root.getChild("controller");

    for (String buttontags : stagebtntags)
    {
      access.update(access.view).addComponentsByTagName(buttontags, SPFZStageComponent.class);
    }

    pausebtn();
    updateclock();

    upcontrols(controller);
    upleftcontrols(controller);
    uprightcontrols(controller);
    downcontrols(controller);
    downleftcontrols(controller);
    downrightcontrols(controller);
    rightcontrols(controller);
    leftcontrols(controller);
    attackcontrols(controller);

  }

  public void controls()
  {
    if (Gdx.input.isKeyPressed(Keys.UP))
    {
      spfzp1move.isUp = true;
    }

    else
    {
      spfzp1move.isUp = false;
    }
    if (Gdx.input.isKeyPressed(Keys.DOWN))
    {
      spfzp1move.isDown = true;
    }
    else
    {
      spfzp1move.isDown = false;
    }
    if (Gdx.input.isKeyPressed(Keys.RIGHT))
    {
      spfzp1move.isRight = true;
    }
    else
    {
      spfzp1move.isRight = false;
      if (spfzp1move.ltstuck && spfzp1move.attributes().scaleX < 0)
      {
        spfzp1move.ltstuck = false;
      }
    }

    if (Gdx.input.isKeyPressed(Keys.LEFT))
    {
      spfzp1move.isLeft = true;
    }
    else
    {
      spfzp1move.isLeft = false;
      if (spfzp1move.ltstuck && spfzp1move.attributes().scaleX > 0)
      {
        spfzp1move.ltstuck = false;
      }
    }

    if (!spfzp1move.isPunch && Gdx.input.isKeyJustPressed(Keys.Q))
    {
      spfzp1move.isPunch = true;
    }
    else
    {
      spfzp1move.isPunch = false;
    }

    if (!spfzp1move.isPunch && !spfzp1move.isKick && Gdx.input.isKeyJustPressed(Keys.W))
    {
      spfzp1move.isKick = true;
    }
    else
    {
      spfzp1move.isKick = false;
    }
    if (Gdx.input.isKeyJustPressed(Keys.B))
    {
      if (!boxes)
      {
        boxes = true;
      }
      else
      {
        boxes = false;
      }
    }
    if (Gdx.input.isKeyJustPressed(Keys.S) && spfzp1move.swap)
    {
      switchp1 = true;
      switchcount = 0;
    }
    if (Gdx.input.isKeyJustPressed(Keys.P))
    {
      switchp2 = true;
      switchcount = 0;
      p2spec -= 100f;
    }
    if (Gdx.input.isKeyJustPressed(Keys.Z))
    {
      if (!shake)
      {
        shake = true;
      }
      else
      {
        shake = false;
      }
    }
    if (Gdx.input.isKeyJustPressed(Keys.A))
    {
      if (!p1charzoom && !p2charzoom)
      {
        p1charzoom = true;
      }
      else
      {
        p1charzoom = false;
      }
    }
  }

  public void pausebtn()
  {
    Entity pausebtn = access.root.getChild("ctrlandhud").getChild("pausebutton").getEntity();

    pausebtn.getComponent(SPFZStageComponent.class).addListener(new SPFZStageComponent.ButtonListener()
    {
      @Override
      public void touchUp()
      {}

      @Override
      public void touchDown()
      {}

      @Override
      public void clicked()
      {
        if (access.draggedfrmbtn("pausebutton", true, "ctrlandhud"))
        {
          // DO NOT PROCESS BUTTON
        }
        else
        {
          access.paused = true;
          if (!standby)
          {
            access.pause();
          }
        }
      }
    });
  }

  public void upleftcontrols(ItemWrapper controller)
  {
    controller.getChild("diagupleftbtn").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {
        @Override
        public void touchUp()
        {
          if (btnupdwn == 0)
          {
            spfzp1move.isUp = false;
          }
          if (btnlftrgt == 0)
          {
            spfzp1move.isLeft = false;
          }
          isMoving = false;
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isUp = true;
          spfzp1move.isLeft = true;
        }

        @Override
        public void clicked()
        {}
      });
  }

  public void leftcontrols(ItemWrapper controller)
  {
    controller.getChild("leftbutton").getEntity().getComponent(SPFZStageComponent.class).addListener(new SPFZStageComponent.ButtonListener()
    {

      @Override
      public void touchUp()
      {
        spfzp1move.isLeft = false;
        isMoving = false;
        spfzp1move.ltstuck = false;
        btnlftrgt = 0;
      }

      @Override
      public void touchDown()
      {
        spfzp1move.isRight = false;
        spfzp1move.isLeft = true;
        btnlftrgt = 1;
      }

      @Override
      public void clicked()
      {}
    });

  }

  public void downleftcontrols(ItemWrapper controller)
  {
    controller.getChild("diagdownleftbtn").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {
          if (btnupdwn == 0)
          {
            spfzp1move.isDown = false;
          }
          if (btnlftrgt == 0)
          {
            spfzp1move.isLeft = false;
          }
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isDown = true;
          spfzp1move.isLeft = true;
        }

        @Override
        public void clicked()
        {}
      });
  }

  public void downcontrols(ItemWrapper controller)
  {
    controller.getChild("downbutton").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {
          spfzp1move.isDown = false;
          btnupdwn = 0;
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isDown = true;
          btnupdwn = 1;
        }

        @Override
        public void clicked()
        {}
      });
  }

  public void downrightcontrols(ItemWrapper controller)
  {
    controller.getChild("diagdownrightbtn").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {
          if (btnupdwn == 0)
          {
            spfzp1move.isDown = false;
          }
          if (btnlftrgt == 0)
          {
            spfzp1move.isRight = false;
          }
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isDown = true;
          spfzp1move.isRight = true;
        }

        @Override
        public void clicked()
        {}
      });
  }

  public void rightcontrols(ItemWrapper controller)
  {
    controller.getChild("rightbutton").getEntity().getComponent(SPFZStageComponent.class).addListener(new SPFZStageComponent.ButtonListener()
    {

      @Override
      public void touchUp()
      {
        spfzp1move.isRight = false;
        isMoving = false;
        btnlftrgt = 0;
        spfzp1move.ltstuck = false;
      }

      @Override
      public void touchDown()
      {
        spfzp1move.isLeft = false;
        spfzp1move.isRight = true;
        btnlftrgt = 1;
      }

      @Override
      public void clicked()
      {}
    });
  }

  public void uprightcontrols(ItemWrapper controller)
  {
    controller.getChild("diaguprightbtn").getEntity().getComponent(SPFZStageComponent.class).addListener(new SPFZStageComponent.ButtonListener()
    {

      @Override
      public void touchUp()
      {
        if (btnlftrgt == 0)
        {
          spfzp1move.isRight = false;
        }
        if (btnupdwn == 0)
        {
          spfzp1move.isUp = false;
        }
        isMoving = false;
      }

      @Override
      public void touchDown()
      {
        spfzp1move.isUp = true;
        spfzp1move.isRight = true;
        isMoving = true;
      }

      @Override
      public void clicked()
      {}
    });
  }

  public void upcontrols(ItemWrapper controller)
  {
    controller.getChild("upbutton").getEntity().getComponent(SPFZStageComponent.class).addListener(new SPFZStageComponent.ButtonListener()
    {

      @Override
      public void touchUp()
      {
        spfzp1move.isUp = false;
        btnupdwn = 0;
      }

      @Override
      public void touchDown()
      {
        spfzp1move.isUp = true;
        btnupdwn = 1;
      }

      @Override
      public void clicked()
      {}
    });
  }

  public void attackcontrols(ItemWrapper controller)
  {
    controller.getChild("punchbutton").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {
          spfzp1move.isPunch = false;
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isPunch = true;
        }

        @Override
        public void clicked()
        {}
      });

    controller.getChild("kickbutton").getEntity().getComponent(SPFZStageComponent.class)
      .addListener(new SPFZStageComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {
          spfzp1move.isKick = false;
        }

        @Override
        public void touchDown()
        {
          spfzp1move.isKick = true;
        }

        @Override
        public void clicked()
        {}
      });
  }

  public void updateclock()
  {
    Entity p1round1, p1round2, p2round1, p2round2;

    p1round1 = access.root.getChild("ctrlandhud").getChild("roundonep1").getEntity();
    p1round2 = access.root.getChild("ctrlandhud").getChild("roundtwop1").getEntity();
    p2round1 = access.root.getChild("ctrlandhud").getChild("roundonep2").getEntity();
    p2round2 = access.root.getChild("ctrlandhud").getChild("roundtwop2").getEntity();

    setuprounds(p1round1);
    setuprounds(p1round2);
    setuprounds(p2round1);
    setuprounds(p2round2);
  }

  public void setuprounds(Entity round)
  {
    float origScaleX = round.getComponent(TransformComponent.class).scaleX;
    float origScaleY = round.getComponent(TransformComponent.class).scaleY;

    if (origScaleX < 0)
    {
      origScaleX *= -1f;
    }
    float width = round.getComponent(DimensionsComponent.class).width * origScaleX;
    float height = round.getComponent(DimensionsComponent.class).height * origScaleY;

    round.getComponent(TransformComponent.class).originX = width * .5f;
    round.getComponent(TransformComponent.class).originY = height * .5f;
  }

  public void timer()
  {
    Entity tenths = access.root.getChild("ctrlandhud").getChild("timeranim").getChild("tenths").getEntity();
    Entity ones = access.root.getChild("ctrlandhud").getChild("timeranim").getChild("ones").getEntity();

    int tempten = (timeleft % 100) / 10;
    int tempone = timeleft % 10;
    double tedbl;

    if (timeleft > 0 && !pausetime && !standby)
    {
      setneut = false;
      timeElapsed = (System.currentTimeMillis() - time) / 1000;
      timeleft = (int) (optime - timeElapsed);

      // Double time is correct. Need to figure out way to translate it into the
      // timer
      tedbl = ((System.currentTimeMillis() - (long) time) / 1000.0);
      timeleftdbl = (double) optime - tedbl;

      tenths.getComponent(SpriteAnimationStateComponent.class).paused = false;
      ones.getComponent(SpriteAnimationStateComponent.class).paused = false;

      tenths.getComponent(SpriteAnimationStateComponent.class)
        .set(new FrameRange("tens", (timeleft % 100) / 10, (timeleft % 100) / 10), 1, Animation.PlayMode.NORMAL);
      ones.getComponent(SpriteAnimationStateComponent.class)
        .set(new FrameRange("ones" + timeleft + "", timeleft % 10, timeleft % 10), 1, Animation.PlayMode.NORMAL);

      ones.getComponent(SpriteAnimationStateComponent.class).paused = true;
      tenths.getComponent(SpriteAnimationStateComponent.class).paused = true;

      if (((timeleft % 100) / 10) != tempten)
      {
        animatenum(tenths);
      }
      if ((timeleft % 10) != tempone)
      {
        animatenum(ones);
      }
    }
    if (timeleft == 0)
    {
      if (!standby)
      {
        eoround = true;
      }
    }
  }

  public void animatenum(Entity num)
  {
    num.getComponent(TransformComponent.class).originY = (num.getComponent(DimensionsComponent.class).height * .5f) *
      num.getComponent(TransformComponent.class).scaleY;

    Actions.addAction(num, Actions.sequence(Actions.scaleTo(0, 0, 0f),
      Actions.parallel(Actions.rotateBy(720f, .360f), Actions.scaleBy(1f, 1f, .6f, Interpolation.elastic))));
  }

  public void animatecount(Entity num)
  {
    Actions.addAction(num, Actions.scaleTo(1f, 1f, .3f, Interpolation.elastic));
  }

  public void totalhealth()
  {
    ItemWrapper controller = access.root.getChild("controller");
    Entity p1hbar = access.root.getChild("ctrlandhud").getChild("healthcheck1").getEntity();
    Entity p2hbar = access.root.getChild("ctrlandhud").getChild("healthcheck2").getEntity();
    Entity p1meter = controller.getChild("supbarone").getEntity();
    Entity p2meter = controller.getChild("supbartwo").getEntity();
    Entity p1out = controller.getChild("sprmtrone").getEntity();
    Entity p2out = controller.getChild("sprmtrtwo").getEntity();

    for (int i = 0; i < arrscripts.size(); i++)
    {
      // CharAttributes healthgetter = new CharAttributes(null);
      if (i < 3)
      {
        //p1health += healthgetter.getHealth();
        p1health += 1000;
      }
      else
      {
        //p2health += healthgetter.getHealth();
        p2health += 1000;
      }
    }

    startp1 = p1health;
    startp2 = p2health;

    //to ensure that we have the correct width amounts

    if (p1hbar.getComponent(TransformComponent.class).scaleX > 0)
    {
      p1HPpercent = p1hbar.getComponent(DimensionsComponent.class).width;
    }
    else
    {
      //perform checks to see if the health bars are equal
      if (p1hbar.getComponent(DimensionsComponent.class).width < p2hbar.getComponent(DimensionsComponent.class).width)
      {
        p1hbar.getComponent(DimensionsComponent.class).width = p2hbar.getComponent(DimensionsComponent.class).width;
      }
    }

    begpercent = p2hbar.getComponent(DimensionsComponent.class).width;

    begsuperpct = controller.getChild("supbarone").getEntity()
      .getComponent(DimensionsComponent.class).width;


    setShader("health", p1hbar);
    setShader("health", p2hbar);
    setShader("super", p1meter);
    setShader("super", p2meter);
    setShader("supout", p1out);
    setShader("supout", p2out);

  }

  public void collision(int i)
  {

    Entity hit = access.root.getChild("p1hit").getEntity();
    Entity hitpart = access.root.getChild("p1hit").getChild("p1confirm").getEntity();
    Entity block = access.root.getChild("block").getEntity();
    Entity blockpart = access.root.getChild("block").getChild("block").getEntity();

    setcollisionboxes(i);

    if (boxes)
    {
      showcollisionboxes(i);
    }

    // Collision boxes that will process the health as well as other
    // functionalities
    // when dealing with hitboxes
    if (hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)
    {
      if (hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.get(0).isComplete())
      {
        hit.getComponent(TransformComponent.class).x = 0;
        hit.getComponent(TransformComponent.class).y = -20f;
        hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.removeValue(hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.get(0), true);
      }
    }

    if (hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)
    {
      if (hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.get(0).isComplete())
      {
        hit.getComponent(TransformComponent.class).x = 0;
        hit.getComponent(TransformComponent.class).y = -20f;
        hitpart.getComponent(SPFZParticleComponent.class).
          pooledeffects.removeValue(hitpart.getComponent(SPFZParticleComponent.class).pooledeffects.get(0), true);
      }
    }

    if (spfzp1move.attacking() || spfzp1move.projact() || spfzp2move.attacking())
    {
      hitboxprocessing();
      if (spfzp1move.projact() && spfzp1move.projectile.hit)
      {
        spfzp1move.projectile.setActive();
      }
    }

    /*if(access.root.getChild("block").getEntity() != null)
    {
      showPartBox();
    }*/

  }

  public void hitboxprocessing()
  {
    Entity hit = access.root.getChild("p1hit").getEntity();
    Entity hitpart = access.root.getChild("p1hit").getChild("p1confirm").getEntity();
    Entity block = access.root.getChild("block").getEntity();
    Entity blockpart = access.root.getChild("block").getChild("block").getEntity();
    Vector2 hitconfirm = new Vector2();
    Vector2 halfofhit = new Vector2(hit.getComponent(DimensionsComponent.class).width * .5f, hit.getComponent(DimensionsComponent.class).height * .5f);
    Vector2 halfofblock = new Vector2(block.getComponent(DimensionsComponent.class).width * .5f, block.getComponent(DimensionsComponent.class).height * .5f);
    float firpoint = (spfzp1move.spfzattribute.x + spfzp1move.setcross().x) * .5f;
    float secpoint = (spfzp2move.spfzattribute.x + spfzp2move.setcross().x) * .5f;
    float hitY = spfzp1move.spfzhitrect.y;
    float spawnHeight = spfzp1move.spfzhitrect.height;
    float partSpawnY = hitY + (spawnHeight * .5f);

    if (spfzp1move.projhit())
    {
      hitY = spfzp1move.projectile.hitbox.y;
      spawnHeight = spfzp1move.projectile.hitbox.height;
      partSpawnY = hitY + (spawnHeight * .5f);
    }

    //Projectile check

    // If player one attacked player 2
    //if (spfzp1move.spfzhitrect.overlaps(spfzp2move.setcharbox()) && spfzp1move.attacking && !spfzp1move.getboxconfirm()
    if (spfzp1move.spfzhitrect.overlaps(spfzp2move.setcharbox()) && !spfzp1move.getboxconfirm() || spfzp1move.projhit())
    {
      spfzp2move.attacked = true;
      spfzp1move.wallb = false;
      spfzp2move.hitProcess();

      if (spfzp1move.bouncer)
      {
        spfzp2move.bounced = true;
        spfzp1move.wallb = true;
        spfzp1move.bouncer = false;
      }

        /*if (!spfzp1move.projconfirm())
        {
          //pausechar();
        }*/

      float hitX = spfzp1move.spfzhitrect.x;
      float hitWidth = spfzp1move.spfzhitrect.width;
      float partSpawnX = hitX + hitWidth;

      if (spfzp1move.projhit())
      {
        spfzp1move.hitboxconfirm(false);
        hitX = spfzp1move.projectile.hitbox.x;
        hitWidth = spfzp1move.projectile.hitbox.width;
        partSpawnX = hitX + hitWidth;
        partSpawnX = (secpoint + partSpawnX) * .5f;
      }
      else
      {
        spfzp1move.hitboxconfirm(true);
      }

      float tempflip;
      float scaleY;
      float scaleX = scaleY = 1;

      if (spfzp1move.center() < spfzp2move.center())
      {
        tempflip = 1f;
        hitconfirm.set(partSpawnX, partSpawnY);
      }
      else
      {
        tempflip = -1f;
        hitconfirm.set(partSpawnX - hitWidth, partSpawnY);
      }

      halfofhit.scl(tempflip, tempflip);
      halfofblock.scl(tempflip, tempflip);

      //Set particle effects to appropriate scaling based on hitboxsize that the opponent was attacked by
      hitpart.getComponent(SPFZParticleComponent.class).worldMultiplyer = tempflip;
      // hit.getComponent(TransformComponent.class).scaleX = (spfzp1move.hitboxsize.y / 50f) * tempflip;
      hit.getComponent(TransformComponent.class).scaleX = scaleX * tempflip;
      //hit.getComponent(TransformComponent.class).scaleY = (spfzp1move.hitboxsize.y / 50f) * tempflip;
      hit.getComponent(TransformComponent.class).scaleY = scaleY * tempflip;

      blockpart.getComponent(TransformComponent.class).scaleX = scaleX * tempflip;
      blockpart.getComponent(TransformComponent.class).scaleY = scaleY * tempflip;

      if (spfzp1move.attributes().y > spfzp1move.charGROUND())
      {
        hit.getComponent(TransformComponent.class).rotation = -45 * tempflip;
        blockpart.getComponent(TransformComponent.class).rotation = -45 * tempflip;

      }
      else
      {
        blockpart.getComponent(TransformComponent.class).rotation = 0;
        hit.getComponent(TransformComponent.class).rotation = 0;
      }

      // Set the positioning of the particle effects and handle hit events
      if (spfzp2move.attacked && spfzp2move.hit())
      {
        //calculate hit particle effect positioning

        hit.getComponent(TransformComponent.class).x = hitconfirm.x - halfofhit.x;
        hit.getComponent(TransformComponent.class).y = hitconfirm.y - halfofhit.y;


        hitpart.getComponent(SPFZParticleComponent.class).startEffect();

        if (!damagedealt)
        {
          Entity Hit = access.root.getChild("ctrlandhud").getChild("p1himg").getEntity();
          Entity cc = access.root.getChild("ctrlandhud").getChild("p1cc").getEntity();
          spfzp2move.setcombonum(spfzp2move.combonum() + 1);

          if (spfzp2move.combonum() >= 2)
          {
            if (spfzp2move.combonum() == 2 && Hit.getComponent(TransformComponent.class).scaleY == 0
              && cc.getComponent(TransformComponent.class).scaleY == 0)
            {
              Actions.addAction(Hit, Actions.scaleBy(0, SCALE_TEXT, .6f, Interpolation.elastic));
              Actions.addAction(cc, Actions.scaleBy(0, SCALE_TEXT, .6f, Interpolation.elastic));
            }

            Entity parent = access.root.getChild("ctrlandhud").getChild("p1cc").getEntity();
            Entity p2cntTEN = access.root.getChild("ctrlandhud").getChild("p1cc").getChild("tenths").getEntity();
            Entity p2cntONE = access.root.getChild("ctrlandhud").getChild("p1cc").getChild("ones").getEntity();
            LabelComponent combocount1;
            combocount1 = access.root.getChild("ctrlandhud").getChild("combocount1").getEntity()
              .getComponent(LabelComponent.class);

            // combocount1.setText(Integer.toString(spfzp2move.combonum()) + "
            // HITS");

            combocounter(parent, p2cntTEN, p2cntONE, spfzp2move.combonum());
          }
          else if (spfzp2move.combonum() == 1 && Hit.getComponent(TransformComponent.class).scaleY >= 0 &&
            cc.getComponent(TransformComponent.class).scaleY >= 0)
          {
            Actions.removeActions(Hit);
            Actions.removeActions(cc);
            Actions.addAction(Hit, Actions.scaleBy(0, 0, .01f, Interpolation.elastic));
            Actions.addAction(cc, Actions.scaleBy(0, 0, .01f, Interpolation.elastic));
          }

          if (spfzp1move.input == -1)
          {
            p2health -= 200f;
            p1spec += 120f;
            p2spec += 120f;

          }
          else
          {
            //p2health -= player1data.get(p1).get(spfzp1move.HITDMG).get(spfzp1move.move).intValue();
            //p1spec += player1data.get(p1).get(spfzp1move.HITMTR).get(spfzp1move.move).intValue();
            //p2spec += player1data.get(p1).get(spfzp1move.HITMTR).get(spfzp1move.move).intValue() / 2;
          }
          damagedealt = true;
        }
      }
      else
      {
        block.getComponent(TransformComponent.class).x = hitconfirm.x - halfofblock.x;
        block.getComponent(TransformComponent.class).y = ((hitconfirm.y - halfofblock.y) + spfzp2move.spfzattribute.y) * .5f;
        //shake = true;
      }

      blockpart.getComponent(SpriteAnimationStateComponent.class).set(blockpart.getComponent(SpriteAnimationComponent.class).frameRangeMap.get("Default"), 25, Animation.PlayMode.NORMAL);

      float duration = blockpart.getComponent(SpriteAnimationStateComponent.class).currentAnimation.getAnimationDuration();
      Actions.removeActions(block);
      Actions.addAction(block, Actions.parallel(Actions.sequence(Actions.fadeIn(.01f), Actions.delay(duration * .75f), Actions.fadeOut(.1f)), Actions.moveBy(-10, 0, duration)));
    }
    // If player two attacked player 1
    else
    {
      if ((spfzp2move.sethitbox().overlaps(spfzp1move.setcharbox()) && spfzp2move.attacking() && !spfzp2move.getboxconfirm()) || spfzp2move.projhit)
      {
        // spfzp2move.hit();
        if (spfzp2move.hitboxsize.x > 0 || spfzp2move.projhit)
        {
          spfzp2move.hitboxconfirm(true);
          spfzp2move.sethitbox().getCenter(hitconfirm);

          hitconfirm.set((spfzp1move.setcharbox().x + spfzp2move.sethitbox().x + spfzp2move.sethitbox().width) * .5f,
            hitconfirm.y);


          if (spfzp1move.hit())
          {
            spfzp1move.attacked = true;
            spfzp1move.blocking = false;
          }
          else
          {
            //spfzp1move.attacked = true;
            spfzp1move.blocking = true;

          }

          if (spfzp2move.projhit)
          {
            spfzp2move.projhit = false;

          }

          float tempflip;
          if (spfzp2move.center() < spfzp1move.center())
          {
            tempflip = 1f;
          }
          else
          {
            tempflip = -1f;
          }
          //Set particle effects to appropriate scaling based on hitboxsize that the opponent was attacked by
          hitpart
            .getComponent(SPFZParticleComponent.class).worldMultiplyer = tempflip;
          hit.getComponent(TransformComponent.class).scaleX = (spfzp2move.hitboxsize.y / 100f) * tempflip;
          hit.getComponent(TransformComponent.class).scaleY =
            (spfzp2move.hitboxsize.y / 100f) * tempflip;


          blockpart.getComponent(SPFZParticleComponent.class).worldMultiplyer = tempflip;
          //block.getComponent(TransformComponent.class).scaleX = (spfzp2move.hitboxsize.y / 100f) * tempflip;
        /*block.getComponent(TransformComponent.class).scaleY =
          (spfzp2move.hitboxsize.y / 100f) * tempflip;*/

          if (spfzp2move.attributes().y > spfzp2move.charGROUND())
          {
            hit.getComponent(TransformComponent.class).rotation = -45 * tempflip;
            block.getComponent(TransformComponent.class).rotation = -45 * tempflip;

          }
          else
          {
            hit.getComponent(TransformComponent.class).rotation = 0;
            block.getComponent(TransformComponent.class).rotation = 0;
          }

          // Set the positioning of the particle effects and handle hit events
          if (spfzp1move.attacked)
          {
            if (spfzp1move.projectile.hit)
            {
              if (spfzp1move.projectile.composite.getComponent(TransformComponent.class).scaleX > 0)
              {
                hit
                  .getComponent(TransformComponent.class).x = spfzp1move.projectile.spfzattribute.x
                  + spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).width;
              }
              else
              {
                hit
                  .getComponent(TransformComponent.class).x = spfzp1move.projectile.spfzattribute.x
                  - spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).width;
              }

              hit
                .getComponent(TransformComponent.class).y = spfzp1move.projectile.spfzattribute.y
                + spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).height * .5f;
              spfzp1move.projectile.hit = false;
            }
            else
            {
              hit.getComponent(TransformComponent.class).x = hitconfirm.x;

              hit.getComponent(TransformComponent.class).y = hitconfirm.y;
            }

            hitpart.getComponent(SPFZParticleComponent.class)
              .startEffect();
            // shake = true;

            if (!damagedealt)
            {
              Entity Hit = access.root.getChild("ctrlandhud").getChild("p2himg").getEntity();
              Entity cc = access.root.getChild("ctrlandhud").getChild("p2cc").getEntity();
              spfzp2move.setcombonum(spfzp2move.combonum() + 1);

              // Hit.getComponent(TransformComponent.class).originY =
              // Hit.getComponent(DimensionsComponent.class).height * .5f;
              if (spfzp2move.combonum() >= 2)
              {
                if (spfzp2move.combonum() == 2 && Hit.getComponent(TransformComponent.class).scaleY == 0
                  && cc.getComponent(TransformComponent.class).scaleY == 0)
                {
                  Actions.addAction(Hit, Actions.scaleBy(0, SCALE_TEXT, .6f, Interpolation.elastic));
                  Actions.addAction(cc, Actions.scaleBy(0, SCALE_TEXT, .6f, Interpolation.elastic));
                }

                //handle combo counter
                Entity parent = access.root.getChild("ctrlandhud").getChild("p2cc").getEntity();
                Entity p2cntTEN = access.root.getChild("ctrlandhud").getChild("p2cc").getChild("tenths").getEntity();
                Entity p2cntONE = access.root.getChild("ctrlandhud").getChild("p2cc").getChild("ones").getEntity();
                LabelComponent combocount2;
                combocount2 = access.root.getChild("ctrlandhud").getChild("combocount2").getEntity()
                  .getComponent(LabelComponent.class);
                combocounter(parent, p2cntTEN, p2cntONE, spfzp2move.combonum());
              }

              if (spfzp2move.move == -1)
              {
                p1health -= 200f;
                p2spec += 120f;
                p2spec += 120f;
              }
              else
              {
                //p1health -= player2data.get(p2).get(spfzp2move.HITDMG).get(spfzp2move.move).intValue();
                //p2spec += player2data.get(p2).get(spfzp2move.HITMTR).get(spfzp2move.move).intValue();
                //p1spec += player2data.get(p2).get(spfzp2move.HITMTR).get(spfzp2move.move).intValue() / 2;
              }
              damagedealt = true;
            }
          }
          else
          {
            if (spfzp1move.projectile.hit)
            {
              if (spfzp1move.projectile.composite.getComponent(TransformComponent.class).scaleX > 0)
              {
               /* block
                  .getComponent(TransformComponent.class).x = spfzp1move.projectile.spfzattribute.x
                  + spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).width;*/
              }
              else
              {
                /*block
                  .getComponent(TransformComponent.class).x = spfzp1move.projectile.spfzattribute.x
                  - spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).width;*/
              }

              /*block
                .getComponent(TransformComponent.class).y = spfzp1move.projectile.spfzattribute.y
                + spfzp1move.projectile.composite.getComponent(DimensionsComponent.class).height * .5f;*/
              spfzp1move.projectile.hit = false;
            }
            else
            {
              block.getComponent(TransformComponent.class).x = hitconfirm.x;
              block.getComponent(TransformComponent.class).y = hitconfirm.y;
            }
            blockpart.getComponent(SPFZParticleComponent.class).startEffect();
          }
        }
      }
    }
  }

  public void combocounter(Entity parent, Entity tens, Entity ones, int cc)
  {
    parent.getComponent(TransformComponent.class).originY = parent.getComponent(DimensionsComponent.class).height * .5f;

    tens.getComponent(SpriteAnimationStateComponent.class).paused = false;
    ones.getComponent(SpriteAnimationStateComponent.class).paused = false;

    tens.getComponent(SpriteAnimationStateComponent.class).set(new FrameRange("tens", (cc % 100) / 10, (cc % 100) / 10),
      1, Animation.PlayMode.NORMAL);
    ones.getComponent(SpriteAnimationStateComponent.class).set(new FrameRange("ones" + cc + "", cc % 10, cc % 10), 1,
      Animation.PlayMode.NORMAL);

    ones.getComponent(SpriteAnimationStateComponent.class).paused = true;
    tens.getComponent(SpriteAnimationStateComponent.class).paused = true;

    // if(((timeleft % 100) / 10) != tempten)
    // {
    // animatenum(tens);
    // }
    animatecount(ones);
    Entity Hit = access.root.getChild("ctrlandhud").getChild("p2himg").getEntity();
    parent.getComponent(TransformComponent.class).y = Hit.getComponent(TransformComponent.class).y;
  }

  public void setface()
  {
    if (spfzp1move.center() < spfzp2move.center())
    {
      if (spfzp1move.attributes().scaleX < 0 && !faceright1)
      {
        faceright1 = true;
      }
      if (spfzp2move.attributes().scaleX > 0 && !faceleft2)
      {
        faceleft2 = true;
      }
    }

    if (spfzp1move.center() > spfzp2move.center())
    {
      if (spfzp1move.attributes().scaleX > 0 && !faceleft1 && spfzp1move.setrect().y <= GROUND)
      {
        faceleft1 = true;
      }
      if (spfzp2move.attributes().scaleX < 0 && !faceright2 && spfzp2move.setrect().y <= GROUND)
      {
        faceright2 = true;
      }
    }
    setfacingp1();
    setfacingp2();
  }

  public void setfacingp1()
  {
    if (faceright1)
    {
      faceleft1 = false;
      if (spfzp1move.attributes().y <= spfzp1move.charGROUND() && spfzp1move.attributes().scaleX < 0)
      {
        spfzp1move.attributes().scaleX *= -1f;
        //scale change plays a factor is maintaining the character's "center".
        //for characters that have a center that is behind the center of the dimension box(green box)
        //we adjust the x value by the dimension box, the adjust amount as well as half of the strike box

        //otherwise, we adjust by the dimension box, the adjust amount, and the remaining space in front of the
        //strike box
        if (spfzp1move.center() < spfzp1move.attributes().x + (spfzp1move.dimrect.width * .5f))
        {
          spfzp1move.attributes().x -= spfzp1move.dimrect.width - (spfzp1move.adjustX + (spfzp1move.setrect().width * .5f));
        }
        else
        {
          spfzp1move.attributes().x -= spfzp1move.dimrect.width + (spfzp1move.adjustX -
            (spfzp1move.dimrect.width - (spfzp1move.adjustX + spfzp1move.setrect().width)));
        }
        faceright1 = false;
      }
    }
    if (faceleft1)
    {
      faceright1 = false;
      if (spfzp1move.attributes().y <= spfzp1move.charGROUND() && spfzp1move.attributes().scaleX > 0)
      {
        spfzp1move.attributes().scaleX *= -1f;
        if (spfzp1move.center() > spfzp1move.attributes().x + (spfzp1move.dimrect.width * .5f))
        {
          spfzp1move.attributes().x += spfzp1move.dimrect.width - (spfzp2move.adjustX + (spfzp2move.setrect().width * .5f));
        }
        else
        {
          spfzp1move.attributes().x += spfzp1move.dimrect.width + (spfzp1move.adjustX -
            (spfzp1move.dimrect.width - (spfzp1move.adjustX + spfzp1move.setrect().width)));
        }
        faceleft1 = false;
      }
    }
  }

  public void setfacingp2()
  {
    if (faceright2)
    {
      if (spfzp2move.attributes().y <= spfzp2move.charGROUND() && spfzp2move.attributes().scaleX < 0)
      {
        spfzp2move.attributes().scaleX *= -1f;
        if (spfzp2move.center() < spfzp2move.attributes().x + (spfzp2move.dimrect.width * .5f))
        {
          spfzp2move.attributes().x -= spfzp2move.dimrect.width - (spfzp2move.adjustX + (spfzp2move.setrect().width * .5f));
        }
        else
        {
          spfzp2move.attributes().x -= spfzp2move.dimrect.width + (spfzp2move.adjustX -
            (spfzp2move.dimrect.width - (spfzp2move.adjustX + spfzp2move.setrect().width)));
        }
        //functions for Zaine (middle of spfzrect box less than center)
        faceright2 = false;
      }
    }
    if (faceleft2)
    {
      if (spfzp2move.attributes().y <= spfzp2move.charGROUND() && spfzp2move.attributes().scaleX > 0)
      {
        if (spfzp2move.center() < spfzp2move.attributes().x + (spfzp2move.dimrect.width * .5f))
        {
          spfzp2move.attributes().x += spfzp2move.dimrect.width - (spfzp2move.adjustX + (spfzp2move.setrect().width * .5f));
        }
        else
        {
          spfzp2move.attributes().x += spfzp2move.dimrect.width + (spfzp2move.adjustX -
            (spfzp2move.dimrect.width - (spfzp2move.adjustX + spfzp2move.setrect().width)));
        }
        spfzp2move.attributes().scaleX *= -1f;
        faceleft2 = false;
      }
    }
  }

  public void setcollisionboxes(int i)
  {
    float reversebox;
    // temp reach will be the box length incoming
    float tempreach = 10;

    // used to determine how to position hitbox when facing left or right
    if (((Attribs) arrscripts.get(i)).attributes().scaleX > 0)
    {
      reversebox = 0;
      tempreach *= 1;
    }
    else
    {
      reversebox = ((Attribs) arrscripts.get(i)).hitboxsize().x;
      tempreach *= -1;
    }

    setstrkbox(i);
    setcrossbox(i);
    sethitbox(i, reversebox, tempreach);

    if (spfzp2move.reflect)
    {
      spfzp2move.setreflect();
    }
  }

  public void setstrkbox(int i)
  {
    // set character full strike box

    if (!((Attribs) arrscripts.get(i)).invul())
    {
      ((Attribs) arrscripts.get(i)).setcharbox();
    }
    else
    {
      ((Attribs) arrscripts.get(i)).setcharbox().set(0, 0, 0, 0);
    }
  }

  public void shwstrkbox(int i)
  {
    Rectangle box = ((Attribs) arrscripts.get(i)).setcharbox();
    ShapeRenderer visual = ((Attribs) arrscripts.get(i)).drawcharbox();
    // Show the character hit box

    visual.setProjectionMatrix(access.viewportland.getCamera().combined);
    visual.begin(ShapeType.Line);

    if (i == p1)
    {
      visual.setColor(Color.RED);
    }

    else
    {
      visual.setColor(Color.ROYAL);
    }

    visual.rect(box.x, box.y, box.width, box.height);

    visual.end();
  }

  public void setcrossbox(int i)
  {
    // set character cross box
    ((Attribs) arrscripts.get(i)).setcross();
    spfzp1move.dimrectangle();
    spfzp2move.dimrectangle();

  }

  public void shwcrossbox(int i)
  {
    Rectangle box = ((Attribs) arrscripts.get(i)).setcross();
    ShapeRenderer visual = ((Attribs) arrscripts.get(i)).drawrect();
    // Shows the cross box that keeps characters from crossing each other

    visual.setProjectionMatrix(access.viewportland.getCamera().combined);
    visual.begin(ShapeType.Line);

    if (i == p1)
    {
      visual.setColor(Color.WHITE);
    }

    else
    {
      visual.setColor(Color.WHITE);
    }

    visual.rect(box.x, box.y, box.width, box.height);
    visual.end();
  }

  public void sethitbox(int i, float reversebox, float tempreach)
  {
    float currentframe = ((Attribs) arrscripts.get(i)).currentframe();
    float begActFrame = ((Attribs) arrscripts.get(i)).activeframes()[0];
    float endActFrame = ((Attribs) arrscripts.get(i)).activeframes()[1];
    // When setting boxes(position x, position y, width, height)

    // Sets the hitbox for attacks.
    // if the character is attacking, and the current frame of animation is
    // the active frames based on the
    // activeframe array, draw the hitbox
    if (((Attribs) arrscripts.get(i)).attacking())
    {
      if (damagedealt)
      {
        damagedealt = false;
      }

      //reset the hitbox and call sethitbox method to call function that created the hitbox values coming in from database
      ((Attribs) arrscripts.get(i)).sethitbox().set(0, 0, 0, 0);

      if (currentframe >= begActFrame && currentframe <= endActFrame)
      {
        if (((Attribs) arrscripts.get(i)).attributes().y > GROUND)
        {
          spfzp1move.inair = true;
        }

        ((Attribs) arrscripts.get(i)).sethitbox();
      }
      else if (currentframe > endActFrame)
      {
        if (i == p1)
        {
          spfzp1move.inair = false;
        }

        ((Attribs) arrscripts.get(i)).hitboxpos().setZero();
        ((Attribs) arrscripts.get(i)).hitboxsize().setZero();
        ((Attribs) arrscripts.get(i)).hitboxconfirm(false);
      }
    }
  }

  public void shwhitbox(int i)
  {
    // Projectile Hitboxes
    if (spfzp1move.projectile != null)
    {
      spfzp1move.projectile.hitbox();
    }
    // Shows the hitbox for attacks.
    // if the character is attacking, and the current frame of animation is
    // the active frames based on the
    // activeframe array, draw the hitbox

    if (((Attribs) arrscripts.get(i)).attacking())
    {
      int currframe = ((Attribs) arrscripts.get(i)).currentframe();
      int actfrmsbeg = ((Attribs) arrscripts.get(i)).activeframes()[0];
      int actfrmsend = ((Attribs) arrscripts.get(i)).activeframes()[1];


      if (currframe >= actfrmsbeg && currframe <= actfrmsend)
      {
        Rectangle coords = ((Attribs) arrscripts.get(i)).sethitbox();
        ShapeRenderer hitbox = ((Attribs) arrscripts.get(i)).drawhitbox();
        // update hitbox positioning based on character
        // This will eventually need to be a method that will update hitbox
        // positions due to the variation of characters

        hitbox.setProjectionMatrix(access.viewportland.getCamera().combined);
        hitbox.begin(ShapeType.Filled);
        hitbox.setColor(Color.ORANGE);
        hitbox.rect(coords.x, coords.y, coords.width, coords.height);
        hitbox.end();
      }
    }

    if (((Attribs) arrscripts.get(i)).currproj() != null)
    {
      if (((Attribs) arrscripts.get(i)).currproj().active)
      {
        if (i == p1)
        {
          showProjP1(spfzp1move);
        }
        else if (i == p2)
        {
          showProjP2(spfzp2move);
        }
      }
      else
      {
        if (((Attribs) arrscripts.get(i)).currproj().hitbox.perimeter() != 0)
        {
          if (i == p1 && spfzp1move.projectile.hit)
          {
            spfzp1move.projectile.sethit();
            spfzp1move.projectile.setHit();
          }
          else if (i == p2)
          {

          }
        }
      }
    }

  }

  public void showProjP1(SPFZP1Movement player)
  {
    if (player.projectile != null)
    {
      Rectangle hitrect = player.projectile.sethit();
      ShapeRenderer hitbox = player.projectile.hitbox();

      hitbox.setProjectionMatrix(access.viewportland.getCamera().combined);
      hitbox.begin(ShapeType.Line);
      hitbox.setColor(Color.WHITE);
      hitbox.rect(hitrect.x, hitrect.y, hitrect.width, hitrect.height);
      hitbox.end();
    }
  }

  public void showProjP2(SPFZP2Movement player)
  {
    Rectangle hitrect = player.projectile.sethit();
    ShapeRenderer hitbox = player.projectile.hitbox();

    hitbox.setProjectionMatrix(access.viewportland.getCamera().combined);
    hitbox.begin(ShapeType.Line);
    hitbox.setColor(Color.WHITE);
    hitbox.rect(hitrect.x, hitrect.y, hitrect.width, hitrect.height);
    hitbox.end();
  }

  public void showPartBox()
  {
    Entity block = access.root.getChild("block").getEntity();
    Rectangle partrect = new Rectangle();
    ShapeRenderer partshape = new ShapeRenderer();

    partrect.set(block.getComponent(TransformComponent.class).x, block.getComponent(TransformComponent.class).y,
      block.getComponent(DimensionsComponent.class).width, block.getComponent(DimensionsComponent.class).height);

    partshape.setProjectionMatrix(access.viewportland.getCamera().combined);
    partshape.begin(ShapeType.Line);
    partshape.setColor(Color.WHITE);
    partshape.rect(partrect.x, partrect.y, partrect.width, partrect.height);
    partshape.end();
  }


  public void showcollisionboxes(int i)
  {
    shwstrkbox(i);
    shwcrossbox(i);
    shwhitbox(i);

    if (spfzp2move.reflect)
    {
      spfzp2move.shwreflect();
    }
  }

  public void checkneutral()
  {
    float p1center = spfzp1move.center();
    float p2center = spfzp2move.center();
    float p1x = spfzp1move.attributes().x;
    float p2x = spfzp2move.attributes().x;
    float p1rectX = spfzp1move.setrect().x;
    float p2rectX = spfzp2move.setrect().x;
    float p1width = spfzp1move.setrect().width;
    float p2width = spfzp2move.setrect().width;


    if (p1center > p2center || p1rectX + p1width > p2center)
    {
      if (p1rectX + (p1width * .5f) > p2rectX + (p2width * .5f))
      {
        p1rectX += 1.2f;
        p2rectX -= 1.2f;
      }
      else
      {
        p1rectX -= 1.2f;
        p2rectX += 1.2f;
      }
    }

    if (p2center > p1center || p2x + p2width > p1center)
    {
      if (p2rectX + (p2width * .5f) > p1x + (p1width * .5f))
      {
        p2x += 1.2f;
        p1x -= 1.2f;
      }
      else
      {
        p2x -= 1.2f;
        p1x += 1.2f;
      }
    }

    spfzp1move.attributes().x = p1x;
    spfzp2move.attributes().x = p2x;
  }

  public void newcam()
  {
    Vector3 movecamera = new Vector3();
    //STAGE BOUNDS
    float[] bounds = {80, 560};
    float camX, camY;
    //player values
    float p1center = spfzp1move.center();
    float p2center = spfzp2move.center();
    float p1x = spfzp1move.setrect().x;
    float p1y = spfzp1move.setrect().y;
    float p2x = spfzp2move.setrect().x;
    float p2y = spfzp2move.setrect().y;
    float p1height = spfzp1move.setrect().height * .5f;
    float p2height = spfzp2move.setrect().height * .5f;
    float p1full = spfzp1move.spfzrect.x + spfzp1move.spfzrect.width;
    float p2full = spfzp2move.spfzrect.x + spfzp2move.spfzrect.width;
    float fixed = access.viewportland.getCamera().position.x;
    float leftend = access.viewportland.getCamera().position.x - HALF_WORLDW;
    float rightend = access.viewportland.getCamera().position.x + HALF_WORLDW;

    // "camX" will be placed between player one and player two. It will stop
    // whenever it has reached either the left
    // or right bound.

    camX = (p1center + p2center) * .5f;
    camY = ((p1y + p1height) + (p2y + p2height)) * .5f;

    //bound checking for center point of camera
    if (camX < bounds[0])
    {
      camX = bounds[0];
    }
    else if (camX > bounds[1])
    {
      camX = bounds[1];
    }
    //stop the characters from moving the camera when they are at the barriers
    else if ((p1 <= leftend && p2full >= rightend) || (p2 <= leftend && p1full >= rightend))
    {
      camX = fixed;
    }

    //bound checking for the players
    if (p1 > bounds[0] || p1full < bounds[1] || p2 > bounds[0] || p2full < bounds[1])
    {
      if (camY < HALF_WORLDH)
      {
        movecamera.set(camX, HALF_WORLDH, 0);
      }
      else
      {
        movecamera.set(camX, camY, 0);
      }
    }
    if (!p1charzoom && !p2charzoom)
    {
      if (((OrthographicCamera) access.viewportland.getCamera()).zoom != 1f)
      {
        access.Zoom(1f, .3f, movecamera.x, movecamera.y);

        if (((OrthographicCamera) access.viewportland.getCamera()).zoom > .998f)
        {
          ((OrthographicCamera) access.viewportland.getCamera()).zoom = 1f;
        }
      }
      cam.position.lerp(movecamera, .3f);
      if (camcon == 3)
      {
        camcon = 0;
      }
    }
    else
    {
      if (p1charzoom)
      {
        if (p2charzoom)
        {
          p2charzoom = false;
        }

        if (spfzp1move.attributes().scaleX > 0)
        {
          if (camcon == 0 || camcon == 2)
          {
            access.Zoom(.25f, .2f, spfzp1move.attributes().x + (spfzp1move.spfzrect.width),
              spfzp1move.attributes().y + (spfzp1move.spfzrect.height * .5f));

            if (camcon != 2)
            {
              if (((OrthographicCamera) access.viewportland.getCamera()).zoom <= .26f)
              {
                camcon = 1;
              }
            }
            else
            {
              if (((OrthographicCamera) access.viewportland.getCamera()).zoom <= .26f)
              {
                camcon = 3;
              }
            }
          }
          else
          {
            if (camcon != 3)
            {
              access.Zoom(.75f, .1f, movecamera.x, movecamera.y);
              if (((OrthographicCamera) access.viewportland.getCamera()).zoom > .749f)
              {
                camcon = 2;
              }
            }
          }
        }
        else
        {
          access.Zoom(.25f, .5f, spfzp1move.attributes().x - (spfzp1move.spfzrect.width),
            spfzp1move.attributes().y + (spfzp1move.spfzrect.height * .5f));
        }
      }
      else if (p2charzoom)
      {
        p1charzoom = false;
        if (spfzp2move.attributes().scaleX > 0)
        {
          access.Zoom(.25f, .5f, spfzp2move.attributes().x + (spfzp2move.spfzrect.width),
            spfzp2move.attributes().y + (spfzp2move.spfzrect.height * .5f));
        }
        else
        {
          access.Zoom(.25f, .5f, spfzp2move.attributes().x - (spfzp2move.spfzrect.width),
            spfzp2move.attributes().y + (spfzp2move.spfzrect.height * .5f));
        }
      }
    }

    if (shake)
    {
      int rand = MathUtils.random(1);
      float incrementx = MathUtils.random(1);
      float incrementy = MathUtils.random(2);
      float newx, newy = HALF_WORLDH;
      if (rand == 0)
      {
        newx = cam.position.x + incrementx;
        newy = cam.position.y + incrementy;
      }
      else
      {
        newx = cam.position.x - incrementx;
        newy = cam.position.y - incrementy;
      }
      if (newy < HALF_WORLDH)

      {
        movecamera.set(newx, HALF_WORLDH, 0);
      }
      else
      {
        movecamera.set(newx, newy, 0);

      }
      if (!spfzp1move.bouncer)
      {
        cam.position.lerp(movecamera, 1f);
      }
    }
  }

  public void sendToBack(Entity attacking, Entity attacked)
  {
    int attack = attacking.getComponent(ZIndexComponent.class).getZIndex();
    int atkd = attacked.getComponent(ZIndexComponent.class).getZIndex();

    if (attack < atkd)
    {
      int temp;

      temp = attack;
      attack = atkd;
      atkd = attack;

      attacking.getComponent(ZIndexComponent.class).setZIndex(attack);
      attacked.getComponent(ZIndexComponent.class).setZIndex(atkd);
    }
  }

  public void switchp1()
  {
    ItemWrapper hud = access.root;
    CompositeItemVO player;
    Entity swappart = access.root.getChild("p1swap").getEntity();
    Entity swappool = access.root.getChild("p1swap").getChild("swapp1").getEntity();

    float facing;
    p1spec -= 100f;
    sigp1lock = true;
    strt1 = true;
    spfzp1move.attacking = false;

    if (spfzp1move.attributes().scaleX > 0)
    {
      facing = spfzp1move.attributes().scaleX;
    }
    else
    {
      facing = spfzp1move.attributes().scaleX * -1;
    }
    p1xattr = spfzp1move.attributes().x;
    p1yattr = spfzp1move.attributes().y;

    access.land.getEngine().removeEntity(hud.getChild(characters.get(p1)).getEntity());

    if (p1 == 3)
    {
      p1 = 0;
    }
    else
    {
      p1++;
    }

    playerToStage(characters.get(p1), p1).addScript((IScript) arrscripts.get(p1));

    // Ensure collision box is setup correctly

    if (facing > 0)
    {
      spfzp1move.setrect().set(spfzp1move.setrect().x + spfzp1move.adjustX,
        spfzp1move.attributes().y + spfzp1move.adjustY, spfzp1move.setrect().width,
        spfzp1move.setrect().height);
    }
    else
    {
      spfzp1move.setrect().set(spfzp1move.setrect().x - spfzp1move.adjustX,
        spfzp1move.attributes().y + spfzp1move.adjustY, spfzp1move.setrect().width, spfzp1move.setrect().height);
    }
    spfzp1move.attributes().scaleX = facing;

    // Start swap particle effect

    if (swappool.getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)

    {
      swappool.getComponent(SPFZParticleComponent.class).pooledeffects.removeValue(swappool
        .getComponent(SPFZParticleComponent.class).pooledeffects.get(0), true);
    }
    swappart.getComponent(TransformComponent.class).x = spfzp1move.center();
    swappart.getComponent(TransformComponent.class).y = spfzp1move.attributes().y
      + spfzp1move.setrect().height * .5f;
    swappool.getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
    swappool.getComponent(TransformComponent.class).scaleX = 1f;
    swappool.getComponent(SPFZParticleComponent.class).startEffect();
    switchcount++;
    switchp1 = false;
  }

  public void switchp2()
  {

    ItemWrapper hud = access.root;
    CompositeItemVO player;
    Entity swappart = access.root.getChild("p2swap").getEntity();
    Entity swappool = access.root.getChild("p2swap").getChild("swapp2").getEntity();

    if (switchp2 & switchcount == 0)
    {
      if (spfzp2move.attributes().scaleX > 0)
      {
        p2xattr = spfzp2move.attributes().x;
      }
      else
      {
        p2xattr = spfzp2move.attributes().x - spfzp2move.setrect().width;
      }
      p2yattr = spfzp2move.attributes().y;
      access.land.getEngine().removeEntity(hud.getChild(characters.get(p1)).getEntity());

      if (p2 == 5)
      {
        p2 = 3;
      }
      else
      {
        p2++;
      }
      //player = access.land.loadVoFromLibrary(characters.get(p2))
      playerToStage(characters.get(p2), p2).addScript((IScript) arrscripts.get(p2));
      switchcount++;
    }

  }

  public void roundover()
  {
    final Entity p1hbar = access.root.getChild("ctrlandhud").getChild("healthcheck1").getEntity();
    final Entity p2hbar = access.root.getChild("ctrlandhud").getChild("healthcheck2").getEntity();

    if (!standby)
    {
      rdcount++;
      if (p1HPpercent == p2HPpercent && rdcount != 3)
      {
        rdcount++;
      }
      animateround(rdcount);
      standby = true;
    }
    // set characters back to neutral animation when round is over
    if (!setneut)
    {
      anim = "IDLE";
      setneut = true;
    }

    if (finishedrd && !gameover)
    {
      Vector3 CENTER = new Vector3(320, 200, 0);
      Entity fader;

      fader = access.root.getChild("fader").getEntity();

      eoround = false;
      resettimer();
      standby = true;
      finishedrd = false;

      // reset clock back to the round time then trigger the timer for the
      // next round
      timeleft = optime;
      time = System.currentTimeMillis();
      // timer.setText(Integer.toString(timeleft));
      Actions.addAction(fader, Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          if (!gameover)
          {
            if (p1health != startp1)
            {
              p1health = startp1;
              // p1spec = 0;

              p1HPpercent = p1hbar.getComponent(DimensionsComponent.class).width;
            }

            if (p2health != startp2)
            {
              p2health = startp2;
              // p2spec = 0;

              p2HPpercent = p2hbar.getComponent(DimensionsComponent.class).width;
            }

            timeElapsed = 0;
            timeleft = 99;
            time = System.currentTimeMillis();

            finishedrd = true;

          }

        }
      }));
      access.root.getChild("ctrlandhud").getChild("pausebutton").getEntity()
        .getComponent(TransformComponent.class).scaleX = 1f;
      access.root.getChild("ctrlandhud").getChild("pausebutton").getEntity()
        .getComponent(TransformComponent.class).scaleY = 0f;

      roundtextset();

      // center stage and characters

      access.viewportland.getCamera().position.set(CENTER);
      access.viewportland.getCamera().update();

      if (access.viewportland.getCamera().position.idt(CENTER))
      {
        spfzp1move.setPos();
        spfzp2move.setPos();
      }
    }
    else if (finishedrd && gameover)
    {
      roundtextset();
    }
  }

  public void animateround(int roundcount)
  {
    switch (roundcount)
    {
      case 1:
        if (p1HPpercent > p2HPpercent)
        {
          p1round1();
          p1rdcount++;
        }

        else if (p2HPpercent > p1HPpercent)
        {
          p2round1();
          p2rdcount++;
        }

        else
        {
          p1round1();
          p2round1();
          p1rdcount++;
          p2rdcount++;
          roundcount++;
        }
        roundcount++;
        break;
      case 2:
        if (p1HPpercent > p2HPpercent)
        {
          if (p1rdcount == 0)
          {
            p1round1();
          }
          else
          {
            p1round2();
            gameover = true;
          }
          p1rdcount++;
        }
        else if (p2HPpercent > p1HPpercent)
        {
          if (p2rdcount == 0)
          {
            p2round1();
          }
          else
          {
            p2round2();
            gameover = true;
          }
          p2rdcount++;
        }
        else
        {
          if (p1rdcount == 0)
          {
            p1round1();
          }
          else
          {
            p1round2();
            gameover = true;
          }

          if (p2rdcount == 0)
          {
            p2round1();
          }
          else
          {
            p2round2();
            gameover = true;
          }

          p1rdcount++;
          p2rdcount++;
        }
        break;

      case 3:
        if (p1HPpercent > p2HPpercent)
        {
          p1round2();
          p1rdcount++;
        }
        else if (p2HPpercent > p1HPpercent)
        {
          p2round2();
          p2rdcount++;
        }
        else
        {
          p1round2();
          p2round2();
          p1rdcount++;
          p2rdcount++;
        }

        gameover = true;

        break;
      default:

        break;
    }

  }

  public void roundtextset()
  {

    Entity roundimg = access.root.getChild("ctrlandhud").getChild("round").getEntity();
    final Entity rct = access.root.getChild("ctrlandhud").getChild("round").getChild("rdcnt").getEntity();

    if (finishedrd && gameover)
    {
      if (p1rdcount == 2 && p2rdcount != 2)
      {
        if (access.isArcade)
        {
          //roundtext.getComponent(LabelComponent.class).setText("YOU WIN");
        }
        else
        {
          //roundtext.getComponent(LabelComponent.class).setText("PLAYER ONE WINS");
        }
      }
      else if (p2rdcount == 2 && p1rdcount != 2)
      {
        if (access.isArcade)
        {
          //roundtext.getComponent(LabelComponent.class).setText("YOU LOSE");
        }
        else
        {
          //roundtext.getComponent(LabelComponent.class).setText("PLAYER TWO WINS");
        }
      }
      else if (p1rdcount == 2 && p2rdcount == 2)
      {
        if (access.isArcade)
        {
          // roundtext.getComponent(LabelComponent.class).setText("DRAW GAME\n YOU LOSE");
        }
        else
        {
          // roundtext.getComponent(LabelComponent.class).setText("DRAW GAME");
        }
      }

      Entity fader;
      fader = access.root.getChild("fader").getEntity();

      Actions.addAction(fader, Actions.sequence(Actions.delay(3f), Actions.fadeIn(.3f), Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          access.paused = false;
          access.pause();
        }
      }), Actions.fadeOut(.01f)));
    }

    else
    {

      String round = "";

      switch (rdcount)
      {
        case 0:
          round = "one";
          break;
        case 1:
          round = "two";
          break;
        case 2:
          round = "three";
          break;
      }

      rct.getComponent(SpriteAnimationComponent.class).currentAnimation = round;
      rct.getComponent(SpriteAnimationStateComponent.class).set(rct.getComponent(SpriteAnimationComponent.class).frameRangeMap.get(round), 60, Animation.PlayMode.NORMAL);

      Actions.addAction(roundimg, Actions.sequence(Actions.scaleTo(1f, 1f, .3f, Interpolation.elastic),
        Actions.delay(.6f), Actions.scaleTo(1f, 0f, .4f, Interpolation.elastic), Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            // Entity roundtext =
            // access.root.getChild("ctrlandhud").getChild("roundtext").getEntity();;

            Entity fightimg = access.root.getChild("ctrlandhud").getChild("fightimg").getEntity();

            Actions.addAction(fightimg, Actions.sequence(Actions.scaleTo(1f, 1f, .3f, Interpolation.elastic),
              Actions.delay(.5f), Actions.scaleTo(1f, 0f, .4f, Interpolation.elastic), Actions.run(new Runnable()
              {
                @Override
                public void run()
                {
                  Actions.addAction(startgame(), Actions.scaleTo(.8f, .8f, .3f, Interpolation.circle));
                  timeleft = optime;
                  time = System.currentTimeMillis();
                  standby = false;
                  initcheck = true;
                }
              })));

            // roundtext.getComponent(LabelComponent.class).setText("FIGHT");
            // Actions.addAction(roundtext, Actions.fadeIn(.8f));;


          }

        })));
    }
  }

  public void p1round1()
  {
    Entity p1round1;

    p1round1 = access.root.getChild("ctrlandhud").getChild("roundonep1").getEntity();


    // if the player one percentage is greater than player 2's. Increment
    // Player 1's round count. else, opposite.

    Actions.addAction(p1round1,
      Actions.sequence(Actions.parallel(Actions.rotateBy(720, 1f),
        Actions.sequence(Actions.scaleBy(-.6f, -.6f, .5f), Actions.scaleBy(.6f, .6f, .5f)),
        Actions.color(Color.BLUE, 1f)), Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          Entity fader;
          // fader =
          // access.root.getChild("ctrlandhud").getChild("fader").getEntity();
          fader = access.root.getChild("fader").getEntity();
          if (p2rdcount != 2)
          {
            Actions.addAction(fader, Actions.sequence(Actions.fadeIn(1f), Actions.run(new Runnable()
            {

              @Override
              public void run()
              {
                Entity fader;
                fader = access.root.getChild("fader").getEntity();

                finishedrd = true;
                Actions.addAction(fader, Actions.fadeOut(1f));
              }
            })));
          }

        }
      })));

  }

  public void p2round1()
  {
    Entity p2round1;

    p2round1 = access.root.getChild("ctrlandhud").getChild("roundonep2").getEntity();

    Actions.addAction(p2round1,
      Actions.sequence(Actions.parallel(Actions.rotateBy(-720, 1f),
        Actions.sequence(Actions.scaleBy(.6f, -.6f, .5f), Actions.scaleBy(-.6f, .6f, .5f)),
        Actions.color(Color.BLUE, 1f)), Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          Entity fader;
          fader = access.root.getChild("fader").getEntity();
          ;
          if (p1rdcount != 2)
          {
            Actions.addAction(fader, Actions.sequence(Actions.fadeIn(1f), Actions.run(new Runnable()
            {

              @Override
              public void run()
              {
                Entity fader;
                fader = access.root.getChild("fader").getEntity();

                finishedrd = true;
                Actions.addAction(fader, Actions.fadeOut(1f));
              }
            })));
          }
        }
      })));

  }

  public void p1round2()
  {
    Entity p1round2;

    p1round2 = access.root.getChild("ctrlandhud").getChild("roundtwop1").getEntity();

    Actions.addAction(p1round2,
      Actions.sequence(Actions.parallel(Actions.rotateBy(720, 1f),
        Actions.sequence(Actions.scaleBy(-.6f, -.6f, .5f), Actions.scaleBy(.6f, .6f, .5f)),
        Actions.color(Color.BLUE, 1f)), Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          finishedrd = true;
          gameover = true;
        }
      })));
  }

  public void p2round2()
  {
    Entity p2round2;

    p2round2 = access.root.getChild("ctrlandhud").getChild("roundtwop2").getEntity();

    Actions.addAction(p2round2,
      Actions.sequence(Actions.parallel(Actions.rotateBy(-720, 1f),
        Actions.sequence(Actions.scaleBy(.6f, -.6f, .5f), Actions.scaleBy(-.6f, .6f, .5f)),
        Actions.color(Color.BLUE, 1f)), Actions.run(new Runnable()
      {

        @Override
        public void run()
        {
          finishedrd = true;
          gameover = true;
        }
      })));

  }

  /**
   * Method is responsible for triggering the displaying of the character's attack, crossing, and hurt boxes
   **/
  public void trainBoxes()
  {
    if (Gdx.input.isTouched())
    {
      if (!show)
      {
        show = true;
      }
    }
    else
    {
      if (show)
      {
        show = false;
      }
      if (showtime != 0)
      {
        showtime = 0;
      }
    }

    if (show)
    {
      if (showtime == 0)
      {
        showtime = System.currentTimeMillis();
      }

      if ((System.currentTimeMillis() - showtime) * .001f >= 1f)
      {
        if (!access.paused)
        {
          if (boxes)
          {
            boxes = false;
            showtime = System.currentTimeMillis();
          }
          else
          {
            boxes = true;
            showtime = System.currentTimeMillis();
          }
        }

      }
    }
  }

  public void lifeandround()
  {
    ItemWrapper controller = access.root.getChild("controller");
    Entity p1hbar = access.root.getChild("ctrlandhud").getChild("healthcheck1").getEntity();
    Entity p2hbar = access.root.getChild("ctrlandhud").getChild("healthcheck2").getEntity();
    Entity p1meter = controller.getChild("supbarone").getEntity();
    Entity p2meter = controller.getChild("supbartwo").getEntity();
    Entity p1out = controller.getChild("sprmtrone").getEntity();
    Entity p2out = controller.getChild("sprmtrtwo").getEntity();

    Entity[] health = {p1hbar, p2hbar};
    Entity[] meter = {p1meter, p2meter, p1out, p2out};

    if (initcheck)
    {
      initcheck = false;
    }

    if (training)
    {
      trainBoxes();
    }
    // p1HPpercent and p2HPpercent reflect actual health that is kept based on
    // p1 and p2
    // health
    // reflPCT is needed to be the "in between" variable to maintain the proper
    // visual of the percentage through the lifebar.
    // begpercent contains the lifebar's width which is necessary to reflect the
    // damage based on how much is
    // stored within p1 and p2health.
    if (p1spec >= MAX_SUPER)
    {
      p1spec = MAX_SUPER;
    }
    if (p2spec >= MAX_SUPER)
    {
      p2spec = MAX_SUPER;
    }

    reflPCT = begpercent * ((float) p1health / (float) startp1);
    p1HPpercent = reflPCT;

    reflPCT = begpercent * ((float) p2health / (float) startp2);
    p2HPpercent = reflPCT;

    reflPCT = begsuperpct * ((float) p1spec / (float) MAX_SUPER);
    p1SPpercent = reflPCT;

    reflPCT = begsuperpct * ((float) p2spec / (float) MAX_SUPER);
    p2SPpercent = reflPCT;


    if (p1hbar.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("health", p1hbar);
    }
    if (p2hbar.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("health", p2hbar);
    }
    if (p1meter.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("super", p1meter);
    }
    if (p2meter.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("super", p2meter);
    }
    if (p1out.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("supout", p1out);
    }
    if (p2out.getComponent(ShaderComponent.class).getShader() == null)
    {
      setShader("supout", p2out);
    }


    int i = 1;
    for (Entity bar : health)
    {
      float hp;

      if (i == 1)
      {
        hp = p1HPpercent;
      }
      else
      {
        hp = p2HPpercent;
      }

      bar.getComponent(ShaderComponent.class).getShader().begin();
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("beg_health", (begpercent * bar.getComponent(TransformComponent.class).scaleX));
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("curr_health", (hp * bar.getComponent(TransformComponent.class).scaleX));
      bar.getComponent(ShaderComponent.class).getShader().setUniformi("player", i);
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("u_resolution", new Vector2(access.viewportland.getScreenWidth(), access.viewportland.getScreenHeight()));
      bar.getComponent(ShaderComponent.class).getShader().setUniformMatrix("u_projTrans", access.viewportland.getCamera().combined);
      bar.getComponent(ShaderComponent.class).getShader().end();
      i++;
    }

    i = 1;
    for (Entity bar : meter)
    {
      float mtr;

      if (i == 1 || i == 3)
      {
        mtr = p1SPpercent;
      }
      else
      {
        mtr = p2SPpercent;
      }

      bar.getComponent(ShaderComponent.class).getShader().begin();
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("full_s", (begsuperpct * bar.getComponent(TransformComponent.class).scaleX));
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("curr_super", (mtr * bar.getComponent(TransformComponent.class).scaleX));
      bar.getComponent(ShaderComponent.class).getShader().setUniformi("player", i);
      bar.getComponent(ShaderComponent.class).getShader().setUniformf("u_resolution", new Vector2(access.viewportland.getScreenWidth(), access.viewportland.getScreenHeight()));
      bar.getComponent(ShaderComponent.class).getShader().setUniformMatrix("u_projTrans", access.viewportland.getCamera().combined);
      bar.getComponent(ShaderComponent.class).getShader().end();
      i++;

    }


    if (p1HPpercent <= 0 || p2HPpercent <= 0)
    {
      if (!standby)
      {
        eoround = true;
      }
    }

    if (!gameover)
    {
      if (!eoround && !training)
      {
        timer();
      }

      if (eoround && !training)
      {
        roundover();
      }
    }
  }

  public Entity startgame()
  {
    Entity pausebtn = access.root.getChild("ctrlandhud").getChild("pausebutton").getEntity();

    float scale = .8f;
    pausebtn.getComponent(
      TransformComponent.class).scaleX = scale;

    pausebtn.getComponent(
      TransformComponent.class).originY = (pausebtn.getComponent(DimensionsComponent.class).height * .5f) * scale;

    return pausebtn;

  }

  public float p1percent()
  {
    return p1HPpercent;
  }

  public float p2percent()
  {
    return p2HPpercent;
  }

  @Override
  public void dispose()
  {

    arrscripts.clear();
    access.stageback.dispose();

    access.testregion = null;

    characters = null;
    charnames = null;
    // spfzp1move.dispose();
    // spfzp2move.dispose();

    super.dispose();
  }
}
