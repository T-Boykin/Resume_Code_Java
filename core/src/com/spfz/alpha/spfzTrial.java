package com.spfz.alpha;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Logger;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.*;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncTask;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.uwsoft.editor.renderer.components.ActionComponent;
import com.uwsoft.editor.renderer.components.DimensionsComponent;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.ShaderComponent;
import com.uwsoft.editor.renderer.components.TextureRegionComponent;
import com.uwsoft.editor.renderer.components.TintComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.additional.ButtonComponent;
import com.uwsoft.editor.renderer.components.label.LabelComponent;
import com.uwsoft.editor.renderer.data.CompositeItemVO;
import com.uwsoft.editor.renderer.data.ProjectInfoVO;
import com.uwsoft.editor.renderer.data.SimpleImageVO;
import com.uwsoft.editor.renderer.systems.action.Actions;
import com.uwsoft.editor.renderer.systems.action.data.ParallelData;
import com.uwsoft.editor.renderer.systems.action.data.SequenceData;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.renderer.utils.ItemWrapper;


public class spfzTrial extends ApplicationAdapter implements InputProcessor, GestureListener, java.sql.Driver, AsyncTask
{

  static final byte ANDROID = 0, DESKTOP = 1, SCALE_UP = 1;

  static final float OPT_TIME = .5f, STAGE_MULT = 1.75f, SCALE_DOWN = .00001f, ZOOMCIN = .37f, ZOOMCOUT = 1f,
    ZOOMCIDUR = .75f, ZOOMCODUR = .5f, PAUSE_BTN_SPD = .10f, APP_SPD = .7f;

  ActionComponent action;
  // interface allows for the application to utilize the Android System
  // Settings, etc. Must be setup within custom interface
  AsyncExecutor exec = new AsyncExecutor(1);
  AndroidInterfaceLIBGDX android;
  // AndroidInterfaceLIBGDX reader;
  AssetManager loader;

  boolean init, isArcade, mode, uicomplete, dialog, charpicked, mute, stageconfirmed, transition, flingup, flingdown,
    inhelp, mnuscn, exit, paused, pmenuloaded, setupstage, optionsup, gamestart, deselect, longclear, partstartone,
    partstarttwo, partstartthree, partstartfour, partstartfive, partstartsix, adjustbright, adjustsound, frmresume,
    stageset, fromss, restart, istraining, moveright, credpress, isloading, strt1, strt2, charfound, inact, slowtime,
    pickedup, stoprender, setcred;

  byte selecttype, charname;
  BufferedImage bufferedImage;

  ComponentMapper<TransformComponent> tc = ComponentMapper.getFor(TransformComponent.class);
  ComponentMapper<SPFZParticleComponent> pc = ComponentMapper.getFor(SPFZParticleComponent.class);
  ComponentMapper<ActionComponent> ac = ComponentMapper.getFor(ActionComponent.class);
  ComponentMapper<BoundingBoxComponent> boundingBox = ComponentMapper.getFor(BoundingBoxComponent.class);
  ComponentMapper<MainItemComponent> mc = ComponentMapper.getFor(MainItemComponent.class);

  CompositeItemVO player1char1, player1char2, player1char3, player2char1, player2char2, player2char3;

  //Driver driver;

  List<CompositeItemVO> charcomposites = new ArrayList<CompositeItemVO>();

  Entity p1c1, p1c2, p1c3, p2c1, p2c2, p2c3, fader;
  List<Entity> charentities = new ArrayList<Entity>();

  float soundamount, brightamount, tmpsound, tmpbright, camzoom, zoompoint, endzoom, targetduration, ctargetduration,
    startingduration, BEAT_DROP = 9.4f, FOUR_COUNT = 9.35f, END_OF_THEME = 44f;
  FPSLogger logger;

  int runningby, savebright, stageTime, savescene, pmenuopt, rcount, scenesel, level, paragraph, gWidth,
    gHeight, INTRO = 0, randsecs = 10, button;

  GestureDetector gd;

  // GLProfiler informant;

  InputMultiplexer im;

  // ItemWrapper grabs all of the entities created within the Overlap2d
  // Application
  ItemWrapper root, pauseroot, landroot;

  long credittime, cleartime, restarttime, adtime, sectime;

  Music mainmenu;

  OrthographicCamera camera;

  Pixmap pixmap, pixmap1, pixmap2, ssmap;

  ProjectInfoVO projectinfo;

  Rectangle rect;

  SequenceData deselbtn, okbtn, pausebtn;
  ShapeRenderer shapeRenderer;

  short stagect;
  SPFZSceneLoader port, land, pause;

  Sound portbtns, landbtns, optin, optout, desel, back, ok;

  SPFZCharButtonSystem spfzbsystem;
  SPFZStageSystem stagesystem;
  SPFZParticleDrawableLogic logic;
  // Stage needed for Controls when within the fight interface
  SPFZResourceManager rm;
  SPFZStage stage;
  public SPFZState state;
  Sprite texhel1, texhel2;

  //ShaderProgram shaderProgram;

  String curScene, prevScene, landscene, view, selectedStage, brightstr, soundstr, p1char1, p1char2,
    p1char3, p2char1, p2char2, p2char3, character, storyline, displaytext, constel = "";

  String[] savedvals, storysplit;

  List<String> characters = new ArrayList<String>();

  List<String> charsselected = new ArrayList<String>();

  Texture health1, healthout1, tex, stageback, storytex;

  TextureAtlas healthatlas;

  TextureRegion healthy, testregion;

  TextureRegionComponent trc = new TextureRegionComponent();

  SimpleImageVO stageimg = new SimpleImageVO();

  MainItemComponent main;

  TintComponent restarttint = new TintComponent();

  TransformComponent transform;

  Viewport viewportland, viewportport;

  Vector2 arcplacer = new Vector2();
  Vector3 credits = new Vector3(320, 400 + (int) (400 * .5), 0), tomenu = new Vector3(320, 400 * .5f, 0), vec3 = new Vector3(0, 0, 0);


  // Default Constructor
  public spfzTrial()
  {
    rm = new SPFZResourceManager(this);
  }

  //Constructor set to receive the custom interfacing for Android System
  //Utilization
  public spfzTrial(AndroidInterfaceLIBGDX tools)
  {
    android = tools;
    // reader = tools;
    android.setHelper();
    rm = new SPFZResourceManager(this);
  }

  // Constructor set to receive the custom interfacing for Android System
  // Utilization
  public spfzTrial(AndroidInterfaceLIBGDX tools, SPFZResourceManager resources)
  {
    android = tools;
    rm = resources;


  }

  public void animateland()
  {

    String[] cdtcomponents = {"ttcimage", "swypefrmbtm", "swypefrmtop"};
    update(view).getEngine().removeEntity(root.getChild("animscn").getEntity());
    update(view).getEngine().removeEntity(root.getChild("lintro").getEntity());
    update(view).getEngine().removeEntity(root.getChild("ttcimage").getEntity());

    for (int i = 0; i < cdtcomponents.length; i++)
    {
      Actions.addAction(root.getChild(cdtcomponents[i]).getEntity(), Actions.fadeOut(0f));
    }

    Actions.addAction(fader, Actions.sequence(Actions.delay(.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
    {

      @Override
      public void run()
      {

        float firstRowY = 240f;
        float secondRowY = 150f;
        float thirdRowY = 60f;
        Actions.addAction(root.getChild("larcbutton").getEntity(), Actions.moveTo(240f, firstRowY, OPT_TIME));
        Actions.addAction(root.getChild("lvsbutton").getEntity(), Actions.moveTo(48f, secondRowY, OPT_TIME));
        Actions.addAction(root.getChild("ltrnbutton").getEntity(), Actions.moveTo(433f, secondRowY, OPT_TIME));
        Actions.addAction(root.getChild("loptbutton").getEntity(), Actions.moveTo(364f, thirdRowY, OPT_TIME));
        Actions.addAction(root.getChild("lhlpbutton").getEntity(), Actions.moveTo(121f, thirdRowY, OPT_TIME));

        Actions.addAction(root.getChild("soundbutton").getEntity(),
          Actions.sequence(
            Actions.parallel(Actions.moveTo(5f, 350f, OPT_TIME * 1.05f), Actions.scaleTo(SCALE_UP + .2f, SCALE_UP + .2f, OPT_TIME * 1.05f)),
            Actions.parallel(Actions.scaleTo(SCALE_UP * .75f, SCALE_UP * .75f, .3f))));

        Actions.addAction(root.getChild("brightbutton").getEntity(),
          Actions.sequence(
            Actions.parallel(Actions.moveTo(55f, 350f, OPT_TIME * 1.15f), Actions.scaleTo(SCALE_UP + .2f, SCALE_UP + .2f, OPT_TIME * 1.15f)),
            Actions.parallel(Actions.scaleTo(SCALE_UP * .75f, SCALE_UP * .75f, .4f))));

        Actions.addAction(root.getChild("exitbutton").getEntity(),
          Actions.sequence(
            Actions.parallel(Actions.moveTo(572f, 333f, OPT_TIME * 1.25f), Actions.scaleTo(SCALE_UP + .2f, SCALE_UP + .2f, OPT_TIME * 1.25f)),
            Actions.parallel(Actions.scaleTo(SCALE_UP, SCALE_UP, .5f))));

        Actions.addAction(root.getChild("pods").getEntity(),
          Actions.parallel(Actions.scaleTo(SCALE_UP, SCALE_UP, OPT_TIME), Actions.moveTo(0, 0, OPT_TIME)));

      }
    })));

  }

  public void animatemainmenu(String view)
  {


    if (view == "portrait")
    {
      buttonsup();
      flypods();
    }
    else
    {
      animateland();
    }
  }

  /**
   * Method determines the position for each character selected in Arcade mode
   */
  public void arcselposition(int player)
  {
    // Set the sprite positions for the character select
    switch (player)
    {
      case 0:

        charentities.get(player).getComponent(TransformComponent.class).scaleX = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).scaleY = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).x = 75f;
        charentities.get(player).getComponent(TransformComponent.class).y = 280f;

        break;

      case 1:

        charentities.get(player).getComponent(TransformComponent.class).scaleX = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).scaleY = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).x = 190f;
        charentities.get(player).getComponent(TransformComponent.class).y = 170f;

        break;

      case 2:

        charentities.get(player).getComponent(TransformComponent.class).scaleX = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).scaleY = 1.25f;
        charentities.get(player).getComponent(TransformComponent.class).x = 55f;
        charentities.get(player).getComponent(TransformComponent.class).y = 60f;

        break;

    }
  }

  public void arcadeinit()
  {
    short WORLD_WIDTH = 640;
    short WORLD_HEIGHT = 400;
    String storypath;
    if (isloading)
    {

      if (loader.update())
      {
        isloading = false;
        if (runningby == ANDROID)
        {
          // android.toast("Arcade textures loaded");
        }
        else
        {
          System.out.print("Arcade textures loaded" + "\n");
        }
        if (runningby == ANDROID)
        {
          android.lockOrientation(mode, view);

        }

        curScene = "storyscene";

        // in testing, re-initializing the SceneLoader made
        // this work when switching between landscape
        // scenes.
        land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");

        update(view).loadScene(curScene, viewportland);
        root = new ItemWrapper(update(view).getRoot());
        transform = tc.get(root.getEntity());
        action = ac.get(root.getEntity());
        setSettings();
        level++;
        // load the 1st texture that will appear on the default layer
        storypath = "arcade/" + storyline + "/" + level + ".png";
        Pixmap pixmap = new Pixmap(Gdx.files.internal(storypath));
        // Pixmap pixmap = new Pixmap((FileHandle) loader.get(storypath,
        // Texture.class));

        Pixmap pixmap2 = new Pixmap((int) WORLD_WIDTH * rm.projectVO.pixelToWorld,
          (int) WORLD_HEIGHT * rm.projectVO.pixelToWorld, pixmap.getFormat());

        pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap2.getWidth(),
          pixmap2.getHeight());
        storytex = new Texture(pixmap2);

        pixmap2.dispose();
        pixmap.dispose();

        trc.regionName = storypath;
        testregion = new TextureRegion(storytex, (int) WORLD_WIDTH * rm.projectVO.pixelToWorld,
          (int) WORLD_HEIGHT * rm.projectVO.pixelToWorld);

        setupstage = true;
        trc.region = testregion;

        stageimg.layerName = "Default";
        stageimg.zIndex = 0;

        stageimg.scaleX = 1f;
        stageimg.scaleY = 1f;

        stageimg.x = 0f;
        stageimg.y = 0f;
        if (storysplit == null)
        {
          getStoryText("arcade/story");
        }

        update(view).entityFactory.createEntity(root.getEntity(), stageimg);
        Actions.addAction(root.getChild("fader").getEntity(), Actions.fadeOut(1f * APP_SPD));
        Actions.addAction(root.getChild("textbackground").getEntity(), Actions.scaleTo(3.8f, 1.0f, 1.5f));
        inact = true;
        continuestory();

      }

    }
  }

  public void arcadeprocessing()
  {
    boolean contin = false;

    // Need to set a timer to advance the scene text
    if (Gdx.input.justTouched() && inact)
    {
      contin = true;
    }
    else
    {
      contin = false;
    }
    arcadeinit();

    if (contin && isArcade && root.getChild("arcadetext").getEntity().getComponent(TintComponent.class).color.a == 1.0)
    {
      continuestory();
    }
  }

  public void setupArcade(String story)
  {
    String storypath;
    // fade in transition slide, fade character select music. load character's
    // story textures

    isloading = true;
    paragraph = 0;
    storyline = story;
    for (int i = 1; i < 6; i++)
    {
      storypath = "arcade/" + story + "/" + i + ".png";

      loader.load(storypath, Texture.class);

    }
    // loader.finishLoading();

  }

  private void backprocessing()
  {
    if (mode)
    {
      // prevScene = curScene;
      if (curScene == "stagescene")
      {
        // rm.unloadstage();
      }
      else if (curScene == "charselscene")
      {
        // rm.unloadsix();
      }
      else if (curScene == "arcadeselscn")
      {
        // rm.unloadarcade();
      }

      // stage select back processing
      //if (curScene == "stageselscene" && !stageconfirmed)
      if (curScene == "newstagesel" && !stageconfirmed)
      {

        root.getEntity().removeAll();

        // new sceneloader has to be created after removing entities
        // from
        // last screen
        // land = new SceneLoader(rm);
        land = new SPFZSceneLoader(rm, this, "", "");
        // land.engine.removeSystem(update(view).engine.getSystem(ScriptSystem.class));
        // land.engine.removeSystem(update(view).engine.getSystem(PhysicsSystem.class));

        update(view).loadScene(prevScene, viewportland);
        curScene = prevScene;
        inMode();

      }
      else
      {
        if (selectedStage != null && stageconfirmed)
        {
          stage.dispose();
          stage = null;
        }

        selectedStage = null;
        stageconfirmed = false;
        mode = false;

        scenesel = 1;

        root.getEntity().removeAll();

        if (Gdx.graphics.getWidth() > Gdx.graphics.getHeight())
        {


          view = "landscape";
          curScene = "landscene";

          // This may be bad. However it is
          // causing the application to process as
          // expected. Need to figure out the
          // issue
          // as to why the landscape view cannot
          // move to the next scene properly
          // without an initialization.
          land = new SPFZSceneLoader(rm, this, "", "");
          // land.engine.removeSystem(update(view).engine.getSystem(ScriptSystem.class));
          // land.engine.removeSystem(update(view).engine.getSystem(PhysicsSystem.class));

          update(view).loadScene(curScene, viewportland);
          root = new ItemWrapper(update(view).getRoot());
          setMainMenu(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        else
        {
          scenesel = 1;
          view = "portrait";

          curScene = "sceneone";

          update(view).loadScene(curScene, viewportport);
          scenesel++;
          setMainMenu(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
      }
    }
  }

  public void buttonsapt()
  {
    Actions.addAction(root.getChild("larcbutton").getEntity(), Actions.moveBy(0, 200, OPT_TIME, Interpolation.linear));

    Actions.addAction(root.getChild("lvsbutton").getEntity(),
      Actions.moveBy(-250, 250, OPT_TIME, Interpolation.linear));

    Actions.addAction(root.getChild("ltrnbutton").getEntity(),
      Actions.moveBy(250, 250, OPT_TIME, Interpolation.linear));

    Actions.addAction(root.getChild("loptbutton").getEntity(),
      Actions.moveBy(200, -200, OPT_TIME, Interpolation.linear));

    Actions.addAction(root.getChild("lhlpbutton").getEntity(),
      Actions.moveBy(-200, -200, OPT_TIME, Interpolation.linear));
  }

  public void buttonssqz()
  {
    Actions.addAction(root.getChild("larcbutton").getEntity(),

      Actions.moveBy(0, -200, OPT_TIME, Interpolation.linear));
    Actions.addAction(root.getChild("lvsbutton").getEntity(),

      Actions.moveBy(250, -250, OPT_TIME, Interpolation.linear));
    Actions.addAction(root.getChild("ltrnbutton").getEntity(),

      Actions.moveBy(-250, -250, OPT_TIME, Interpolation.linear));
    Actions.addAction(root.getChild("loptbutton").getEntity(),

      Actions.moveBy(-200, 200, OPT_TIME, Interpolation.linear));
    Actions.addAction(root.getChild("lhlpbutton").getEntity(),

      Actions.moveBy(200, 200, OPT_TIME, Interpolation.linear));
  }

  public void bringscreen()
  {
    Actions.addAction(root.getChild("ctrlandhud").getChild("faderscreen").getEntity(),
      Actions.sequence(Actions.fadeIn(2f * APP_SPD), Actions.fadeOut(2f * APP_SPD)));
  }

  public void bringupconfirm()
  {
    Entity resume = pauseroot.getChild("pausemenu").getChild("resumebutton").getEntity();
    Entity charsel = pauseroot.getChild("pausemenu").getChild("charselbutton").getEntity();
    Entity mm = pauseroot.getChild("pausemenu").getChild("mainmenubutton").getEntity();
    Entity yes = pauseroot.getChild("pausemenu").getChild("yesbtn").getEntity();
    Entity no = pauseroot.getChild("pausemenu").getChild("nobtn").getEntity();


    stage.pauseconfirm = true;
    Actions.addAction(resume, Actions.fadeOut(.01F * APP_SPD));
    Actions.addAction(charsel, Actions.fadeOut(PAUSE_BTN_SPD * APP_SPD));
    Actions.addAction(mm, Actions.fadeOut(PAUSE_BTN_SPD * APP_SPD));

    Actions.addAction(yes, Actions.fadeIn(PAUSE_BTN_SPD * APP_SPD));
    Actions.addAction(no, Actions.fadeIn(PAUSE_BTN_SPD * APP_SPD));
  }

  public void buttonsdown()
  {
    float push;
    float SCALE_BTN = 1.8f;
    byte SCALE_DURATION = 1;
    float separator = .2f;
    float buttontime = .8f;
    if (view == "portrait")
    {
      String[] portbtntags = {"arcbutton", "vsbutton", "trnbutton", "helpbutton", "optbutton", "brightnessbtn",
        "soundbutton", "exitbutton", "yes", "no", "thirtytime", "sixtytime", "ninetytime", "slidebright",
        "slidesound", "revert"};

      if (!dialog || optionsup)
      {
        Actions.addAction(root.getChild("controlboard").getEntity(),
          Actions.sequence(Actions.moveBy(0, 90f, 1f, Interpolation.fade)));
      }

      // adjust the buttons to make them a bit more animated than what they
      // seem

      for (int i = 0; i < 5; i++)
      {

        Actions.addAction(root.getChild(portbtntags[i]).getEntity(),
          Actions.parallel(Actions.moveBy(0, -570f, buttontime, Interpolation.swing), Actions.sequence(
            Actions.parallel(Actions.scaleTo(SCALE_BTN, 2f, SCALE_DURATION), Actions.moveBy(0, 0, SCALE_DURATION)))));

        buttontime = buttontime + separator;
      }

      if (!optionsup)
      {
        Actions.addAction(root.getChild("exitdialog").getEntity(),
          Actions.sequence(Actions.moveBy(0, -570f, 1f, Interpolation.swing)));
      }
    }
    else
    {
      Actions.addAction(root.getChild("larcbutton").getEntity(),
        Actions.moveBy(0, 200, OPT_TIME, Interpolation.linear));

      Actions.addAction(root.getChild("lvsbutton").getEntity(),
        Actions.moveBy(-250, 250, OPT_TIME, Interpolation.linear));

      Actions.addAction(root.getChild("ltrnbutton").getEntity(),
        Actions.moveBy(250, 250, OPT_TIME, Interpolation.linear));

      Actions.addAction(root.getChild("loptbutton").getEntity(),
        Actions.moveBy(200, -200, OPT_TIME, Interpolation.linear));

      Actions.addAction(root.getChild("lhlpbutton").getEntity(),
        Actions.moveBy(-200, -200, OPT_TIME, Interpolation.linear));

      if (!dialog)
      {
        Actions.addAction(root.getChild("optdialog").getEntity(),
          Actions.parallel(Actions.scaleTo(SCALE_UP, SCALE_UP, OPT_TIME),
            Actions.moveBy(-268f, -171f, OPT_TIME, Interpolation.linear)));
      }
      Actions.addAction(root.getChild("soundbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(33f, 366f, OPT_TIME * 1.05f), Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, OPT_TIME * 1.05f)),
          Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, .3f))));

      Actions.addAction(root.getChild("brightbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(103f, 366f, OPT_TIME * 1.15f), Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, OPT_TIME * 1.15f)),
          Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, .4f))));

      Actions.addAction(root.getChild("exitbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(606f, 366f, OPT_TIME * 1.25f), Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, OPT_TIME * 1.25f)),
          Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, .5f))));

      // push the exit dialog box up for confirmation
      if (!optionsup && dialog)
      {
        Actions.addAction(root.getChild("exitdialog").getEntity(),
          Actions.sequence(Actions.moveBy(0, 400f, 1f, Interpolation.swing)));
      }
    }
  }

  /**
   * method contains actions for pushing buttons up for both main menu screens
   */
  public void buttonsup()
  {
    float push;
    if (view == "portrait")
    {
      byte SCALE_DURATION = 1;
      float SCALE_BTN = 1.8f;
      float separator = .2f;
      float buttontime = 1.2f;

      String[] portbtntags = {"arcbutton", "vsbutton", "trnbutton", "helpbutton", "optbutton", "brightnessbtn",
        "soundbutton", "exitbutton", "yes", "no", "thirtytime", "sixtytime", "ninetytime", "slidebright",
        "slidesound", "revert"};
      if (dialog || optionsup)
      {
        Actions.addAction(root.getChild("controlboard").getEntity(),
          Actions.sequence(Actions.moveBy(0, -90f, 1f * APP_SPD, Interpolation.fade)));
      }
      else
      {
        Actions.addAction(root.getChild("controlboard").getEntity(),
          Actions.sequence(Actions.moveBy(0, 90f, 1f * APP_SPD, Interpolation.fade)));
      }

      if (curScene == "sceneone")
      {

        update(view).getEngine().removeEntity(root.getChild("animcircle").getEntity());
        update(view).getEngine().removeEntity(root.getChild("introcircle").getEntity());
        update(view).getEngine().removeEntity(root.getChild("ttcimage").getEntity());

      }

      // adjust the buttons to make them a bit more animated than what they
      // seem

     /* for (int i = 0; i < 5; i++)
      {

        Actions.addAction(root.getChild(portbtntags[i]).getEntity(),
          Actions.parallel(Actions.moveBy(0, 570f, buttontime, Interpolation.swing), Actions.sequence(
            Actions.parallel(Actions.scaleBy(-2, -2f, SCALE_DURATION), Actions.moveBy(75f, 0, SCALE_DURATION)),
            Actions.parallel(Actions.scaleBy(2, 2f, SCALE_DURATION), Actions.moveBy(-75f, 0, SCALE_DURATION)))));

        buttontime = buttontime + separator;
      }*/

      for (int i = 0; i < 5; i++)
      {
        push = root.getChild(portbtntags[i]).getEntity().getComponent(DimensionsComponent.class).width *
          SCALE_BTN;


        if (dialog || optionsup)
        {
          Actions.addAction(root.getChild(portbtntags[i]).getEntity(),
            Actions.parallel(Actions.moveBy(0, 570f, buttontime, Interpolation.swing), Actions.sequence(Actions.delay(.8f),
              Actions.parallel(Actions.scaleTo(SCALE_BTN, 2f, SCALE_DURATION)))));// Actions.moveBy(0, 0, SCALE_DURATION)))));
        }
        else
        {
          Actions.addAction(root.getChild(portbtntags[i]).getEntity(),
            Actions.parallel(Actions.moveBy(0, 570f, buttontime, Interpolation.swing), Actions.sequence(Actions.delay(.8f),
              Actions.parallel(Actions.scaleTo(SCALE_BTN, 2f, SCALE_DURATION)))));//, Actions.moveBy(-(push * .5f), 0, SCALE_DURATION)))));

        }
        buttontime = buttontime + separator;
      }

      buttontime = 2f;

      if (!optionsup)
      {
        Actions.addAction(root.getChild("exitdialog").getEntity(),
          Actions.sequence(Actions.moveBy(0, 570f, 2f * APP_SPD, Interpolation.swing)));
      }
    }
    else
    {
      float ADDITION = .2f;
      float firstRowY = -200f;
      float secondRowY = -250f;
      float thirdRowY = 200f;

      Actions.addAction(root.getChild("larcbutton").getEntity(),
        //Actions.moveBy(0, -200, OPT_TIME, Interpolation.linear));
      Actions.moveBy(0, firstRowY, OPT_TIME, Interpolation.linear));

      Actions.addAction(root.getChild("lvsbutton").getEntity(),
        //Actions.moveBy(250, -250, OPT_TIME, Interpolation.linear));
        Actions.moveBy(250, secondRowY, OPT_TIME, Interpolation.linear));
      Actions.addAction(root.getChild("ltrnbutton").getEntity(),

        //Actions.moveBy(-250, -250, OPT_TIME, Interpolation.linear));
        Actions.moveBy(-250, secondRowY, OPT_TIME, Interpolation.linear));
      Actions.addAction(root.getChild("loptbutton").getEntity(),

        //Actions.moveBy(-200, 200, OPT_TIME, Interpolation.linear));
        Actions.moveBy(-200, thirdRowY, OPT_TIME, Interpolation.linear));
      Actions.addAction(root.getChild("lhlpbutton").getEntity(),

        Actions.moveBy(200, thirdRowY, OPT_TIME, Interpolation.linear));

      if (!dialog)
      {
        Actions.addAction(root.getChild("optdialog").getEntity(),
          Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, OPT_TIME), Actions.moveBy(268, 171f, OPT_TIME)));
      }
      Actions.addAction(root.getChild("soundbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(5, 350f, .6f), Actions.scaleTo(SCALE_UP + ADDITION, SCALE_UP + ADDITION, .6f)),
          Actions.scaleTo(SCALE_UP * .75f, SCALE_UP * .75f, .3f)));

      Actions.addAction(root.getChild("brightbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(55f, 350f, .7f), Actions.scaleTo(SCALE_UP + ADDITION, SCALE_UP + ADDITION, .7f)),
         Actions.scaleTo(SCALE_UP * .75f, SCALE_UP * .75f, .4f)));

      Actions.addAction(root.getChild("exitbutton").getEntity(),
        Actions.sequence(
          Actions.parallel(Actions.moveTo(572f, 333f, .8f), Actions.scaleTo(SCALE_UP + ADDITION, SCALE_UP + ADDITION, .8f)),
         Actions.scaleTo(SCALE_UP, SCALE_UP, .5f)));

      // push the exit dialog back down
      if (!optionsup && dialog)
      {
        Actions.addAction(root.getChild("exitdialog").getEntity(),
          Actions.sequence(Actions.moveBy(0, -400f, .5f * APP_SPD, Interpolation.swing)));
      }
    }

  }

  public void charSel(boolean arcade)
  {
    spfzbsystem.priority = 0;

    if (!arcade)
    {

      fader = root.getChild("mainslide").getEntity();
    }
    else
    {
      fader = root.getChild("transition").getEntity();
      Actions.addAction(fader, Actions.sequence(Actions.fadeOut(.3f * APP_SPD)));
    }
    update(view).engine.addSystem(spfzbsystem);

    final boolean arc = arcade;

    for (int i = 0; i < 6; i++)
    {

      charsselected.set(i, null);
      charcomposites.set(i, null);
      charentities.set(i, null);
    }

    p1char1 = null;
    p1char2 = null;
    p1char3 = null;
    p2char1 = null;
    p2char2 = null;
    p2char3 = null;

    update(view).addComponentsByTagName("button", ButtonComponent.class);
    update(view).addComponentsByTagName("charbtn", SPFZCharButtonComponent.class);

    root.getChild("charobject").getChild("spriteselbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("spriteselbutton", false, null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            //setcharasprites("spriteball");
            setcharasprites("rappa");
          }
          else
          {
            if (!pickedup)
            {
              //setcharsprites("spriteball", "spriteselbutton");
              setcharsprites("rappa", "spriteselbutton");
            }
            pickedup = true;
          }

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });

    root.getChild("charobject").getChild("spriteredbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("spriteredbutton", false, null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            //setcharasprites("spriteballred");
            setcharasprites("zaine");
          }
          else
          {
            if (!pickedup)
            {
              //setcharsprites("spriteballred", "spriteredbutton");
              setcharsprites("zaine", "spriteredbutton");
              pickedup = true;
            }
          }

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });
    /*root.getChild("charobject").getChild("blotchbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("blotchbutton", false, null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            setcharasprites("redblotch");
          }
          else
          {
            if (!pickedup)
            {
              setcharsprites("redblotch", "blotchbutton");
            }
            pickedup = true;
          }

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });
    root.getChild("charobject").getChild("purplexbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("purplexbutton", false, null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            setcharasprites("spritepurplex");
          }
          else
          {
            if (!pickedup)
            {
              setcharsprites("spritepurplex", "purplexbutton");
            }
            pickedup = true;

          }


        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });

    root.getChild("charobject").getChild("spriteblackbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("spriteblackbutton", false,
          // null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;


          if (arc)
          {
            setcharasprites("spriteballblack");
          }
          else
          {
            if (!pickedup)
            {
              setcharsprites("spriteballblack", "spriteblackbutton");
            }
            pickedup = true;
          }


        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });*/
    root.getChild("charobject").getChild("blockbutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("blockbutton", false, null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            setcharasprites("hynryck");
          }
          else
          {
            if (!pickedup)
            {
              setcharsprites("hynryck", "blockbutton");
            }
            pickedup = true;
          }


        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });

    /*root.getChild("charobject").getChild("walkspritebutton").getEntity().getComponent(SPFZCharButtonComponent.class)
      .addListener(new SPFZCharButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          // if(draggedfrmbtn("walkspritebutton", false,
          // null))
          // {
          // DO NOT PROCESS BUTTON
          // }
          // else
          // {
          charpicked = false;

          if (arc)
          {
            setcharasprites("walksprite");
          }
          else
          {
            if (!pickedup)
            {
              setcharsprites("walksprite", "walkspritebutton");
            }
            pickedup = true;
          }
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {
          pickedup = false;
        }
      });*/

    root.getChild("deselbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          if (!partstartone && !partstarttwo && !partstartthree && !partstartfour && !partstartfive && !partstartsix)
          {
            longclear = false;
            // if(draggedfrmbtn("walkspritebutton", false,
            // null))
            // {
            // DO NOT PROCESS BUTTON
            // }
            // else
            // {
            charpicked = false;
            deselect = true;
            for (int i = charsselected.size() - 1; i >= 0; i--)
            {
              if (charsselected.get(i) != null && charentities.get(i) != null && deselect)
              {


                update(view).getEngine().removeEntity(charentities.get(i));

                if (i < 3)
                {
                  root.getChild(charsselected.get(i) + "one").getEntity().getComponent(TransformComponent.class).x = -200f;
                }
                else
                {
                  root.getChild(charsselected.get(i) + "two").getEntity().getComponent(TransformComponent.class).x = 700f;
                }

                switch (i)
                {
                  case 0:

                    //root.getChild("charonelbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("firstnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("firstnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                    break;
                  case 1:
                    //root.getChild("chartwolbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("secondnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("secondnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                    break;
                  case 2:
                    //root.getChild("charthreelbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("thirdnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("thirdnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                    for (int j = 1; j >= 0; j--)
                    {
                      root.getChild(charsselected.get(j) + "one").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                    }
                    /*root.getChild("charonelbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                    root.getChild("chartwolbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;*/

                    root.getChild("playerpng").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                    // root.getChild("playerlbl").getEntity().getComponent(TintComponent.class).color
                    // = Color.WHITE;
                    break;
                  case 3:
                    // root.getChild("charfourlbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("fourthnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("fourthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                    break;
                  case 4:
                    //root.getChild("charfivelbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("fifthnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("fifthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                    break;
                  case 5:
                    //root.getChild("charsixlbl").getEntity().getComponent(LabelComponent.class).setText(null);
                    root.getChild("sixthnullpart").getEntity()
                      .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                    root.getChild("sixthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                    for (int j = 4; j >= 3; j--)
                    {
                      root.getChild(charsselected.get(j) + "two").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                    }

                    root.getChild("cpupng").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                    break;
                  default:
                    break;
                }
                deselect = false;
                desel.play(1.0f);
                charsselected.set(i, null);
                charcomposites.set(i, null);
              }
            }

          }
        }

        @Override
        public void touchDown()
        {
          longclear = true;
          cleartime = System.currentTimeMillis();

        }

        @Override
        public void touchUp()
        {
          longclear = false;

        }
      });

  }

  /**
   * Character Select intro animation
   */
  public void charselIntro()
  {
    String[] slides = {"translideone", "translidetwo", "translidethree", "translidefour", "translidefive",
      "translidesix", "translideseven", "translideeight", "translidenine", "translideten", "translideeleven",
      "translidetwelve"};
    // Character Select Transition

    if (INTRO == 2)
    {
      transform = tc.get(root.getEntity());
      action = ac.get(root.getEntity());

      for (int i = 0; i < 4; i++)
      {
        Actions.addAction(root.getChild(slides[i]).getEntity(), Actions.parallel(Actions.moveBy(800f, -800f, 1f * APP_SPD),
          Actions.sequence(Actions.color(Color.WHITE, .3f), Actions.color(Color.BLACK, .3f))));
      }
      for (int i = 4; i < 8; i++)
      {
        Actions.addAction(root.getChild(slides[i]).getEntity(), Actions.parallel(Actions.moveBy(-800f, -800f, 1f * APP_SPD),
          Actions.sequence(Actions.color(Color.BLACK, .3f), Actions.color(Color.RED, .3f))));
      }

      for (int i = 8; i < 12; i++)
      {
        Actions.addAction(root.getChild(slides[i]).getEntity(), Actions.parallel(Actions.moveBy(0, 800f, 1f * APP_SPD),
          Actions.sequence(Actions.color(Color.RED, .3f), Actions.color(Color.WHITE, .3f))));
      }


      Actions.addAction(root.getChild("mainslide").getEntity(), Actions.fadeOut(1.5f * APP_SPD));
      // Actions.addAction(root.getChild("mainslide").getEntity(), Actions.sequence(Actions.color(Color.WHITE, .3f)));
      //   Actions.fadeOut(1.5  * APP_SPDf)));


    }
  }

  public void chkrstrt()
  {
    if ((System.currentTimeMillis() - restarttime) * .001f >= 1f)
    {
      restart = false;
      bringupconfirm();
      pmenuopt = 0;
    }
    else
    {
      if (restarttint.color.a != 1f)
      {
        restarttint.color.a += .015f;
      }
    }
  }

  //clears the character names as well as the character's idle entity represenation on the screen.
  public void clearAll()
  {

    if ((System.currentTimeMillis() - cleartime) * .001f >= 1f && longclear
      && charentities.get(0) != null)
    {
      for (int i = charsselected.size() - 1; i >= 0; i--)
      {
        if (charsselected.get(i) != null)
        {

          update(view).getEngine().removeEntity(charentities.get(i));
          if (i < 3)
          {
            root.getChild(charsselected.get(i) + "one").getEntity().getComponent(TransformComponent.class).x = -200f;
          }
          else
          {
            root.getChild(charsselected.get(i) + "two").getEntity().getComponent(TransformComponent.class).x = 700f;
          }


          switch (i)
          {
            case 0:

              root.getChild("firstnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("firstnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

              break;
            case 1:

              root.getChild("secondnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("secondnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

              break;
            case 2:

              root.getChild("thirdnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("thirdnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              break;
            case 3:

              root.getChild("fourthnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("fourthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              break;
            case 4:

              root.getChild("fifthnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("fifthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

              break;
            case 5:

              root.getChild("sixthnullpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("sixthnullpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              break;
            default:
              break;
          }

          charsselected.set(i, null);
          charcomposites.set(i, null);
        }
      }

      Actions.addAction(fader, Actions.sequence(Actions.color(Color.WHITE, .0001f),
        Actions.alpha(1f, .0001f), Actions.alpha(0f, 1f), Actions.run(new Runnable()
        {
          @Override
          public void run()
          {
            fader.getComponent(TintComponent.class).color.set(0);
          }
        })));

      longclear = false;
    }

  }

  public void closebtns(String view)
  {
    ParallelData lb1 = null, lb2 = null, lb3 = null, lb4 = null, lb5 = null;
    String btn1, btn2, btn3, btn4, btn5;

    if (view == "portrait")
    {
      btn1 = "arcbutton";
      btn2 = "vsbutton";
      btn3 = "trnbutton";
      btn4 = "optbutton";
      btn5 = "helpbutton";
    }
    else
    {
      btn1 = "larcbutton";
      btn2 = "lvsbutton";
      btn3 = "ltrnbutton";
      btn4 = "loptbutton";
      btn5 = "lhlpbutton";
    }

    lb1 = createactions(root.getChild(btn1).getEntity());
    lb2 = createactions(root.getChild(btn2).getEntity());
    lb3 = createactions(root.getChild(btn3).getEntity());
    lb4 = createactions(root.getChild(btn4).getEntity());
    lb5 = createactions(root.getChild(btn5).getEntity());


    Actions.addAction(root.getChild(btn1).getEntity(), Actions.sequence(Actions.delay(.01f), lb1));
    Actions.addAction(root.getChild(btn2).getEntity(), Actions.sequence(Actions.delay(.02f), lb2));
    Actions.addAction(root.getChild(btn3).getEntity(), Actions.sequence(Actions.delay(.03f), lb3));
    Actions.addAction(root.getChild(btn4).getEntity(), Actions.sequence(Actions.delay(.04f), lb4));
    Actions.addAction(root.getChild(btn5).getEntity(), Actions.sequence(Actions.delay(.05f), lb5));
  }

  /**
   * DisplayAD will process ads when necessary based on parameters received(process called while "running")
   * scene - current screen within SWAP FYTERZ
   * lastgen - last time ad generated in milliseconds
   * seconds - next time in seonds when the ad will display
   *
   * @param scene
   * @param lastgen
   * @param seconds
   */
  public void displayAD(String scene, long lastgen, int seconds)
  {
    Random randtime = new Random();

    if (runningby == ANDROID)
    {
      final int AD_GEN_MIX = 3;
      final int AD_GEN_MAX = 8;

      if ((System.currentTimeMillis() - lastgen) * .001f >= seconds)
      {
        switch (scene)
        {
          case "landscene":
            //if the last generated ad is now past the timer(seconds til the next AD generation), generate new AD

            //check if AD not generated, if not, generate AD

            android.NEW_SPFZ_AD("banner");
            adtime = System.currentTimeMillis();
            randsecs = (randtime.nextInt(AD_GEN_MAX - AD_GEN_MIX) + 1) + AD_GEN_MAX;


            break;
          case "sceneone":
            //if the last generated ad is now past the timer(seconds til the next AD generation), generate new AD

            //check if AD not generated, if not, generate AD

            android.NEW_SPFZ_AD("banner");
            adtime = System.currentTimeMillis();
            randsecs = (randtime.nextInt(AD_GEN_MAX - AD_GEN_MIX) + 1) + AD_GEN_MAX;

            break;
          case "training":
            break;
          case "storyscene":
            switch (level)
            {
              case 2:
                android.NEW_SPFZ_AD("inter");
                break;
              case 4:
                android.NEW_SPFZ_AD("inter");
                break;
              case 99:
                android.NEW_SPFZ_AD("interv");
                break;
            }
            break;
          default:
            break;

        }
      }
    }
    else
    {

      final int AD_GEN_MAX = 15;
      final int AD_GEN_MIX = 10;

      String AdType = "";
      if ((System.currentTimeMillis() - lastgen) * .001f >= seconds)
      {
        switch (scene)
        {

          case "landscene":

            //if the last generated ad is now past the timer(seconds til the next AD generation), generate new AD


            AdType = "banner";
            adtime = System.currentTimeMillis();
            randsecs = (randtime.nextInt(10 - 5) + 1) + 10;


            break;
          case "sceneone":

            adtime = System.currentTimeMillis();
            randsecs = (randtime.nextInt(10 - 5) + 1) + 10;

            AdType = "banner";
            break;
          case "training":
            AdType = "banner";
            break;
          case "storyscene":

            switch (level)
            {
              case 2:
                AdType = "InterStit";
                break;
              case 4:
                AdType = "InterStit";
                break;
              case 99:
                AdType = "InterStitVid";
              default:
                break;
            }
            break;
          default:
            break;

        }
        if (AdType != "")
        {
          System.out.println("displaying " + AdType + " for " + scene + "");
        }
      }
    }
  }

  /**
   * method runs credit process every 5 seconds
   */
  public void controlCredits()
  {

    if (curScene == "landscene" || view == "portrait")
    {
      //banner logic meshed with fade credits
      if ((System.currentTimeMillis() - credittime) * .001f >= 10 && !credpress)
      {
        fadeCredit();
        credittime = System.currentTimeMillis();
        System.out.println("fade in credits");
      }


    }
  }

  public void controlLights()
  {
    TransformComponent lighttranscomp = root.getChild("mainlight").getEntity().getComponent(TransformComponent.class);
    if (curScene == "landscene")
    {
      if (lighttranscomp.x >= 160f && moveright)
      {
        lighttranscomp.x += .5f;

        if (lighttranscomp.x >= 480f)
        {
          moveright = false;
        }
      }
      if (lighttranscomp.x <= 480f && !moveright)
      {
        lighttranscomp.x -= .5f;

        if (lighttranscomp.x <= 160f)
        {
          moveright = true;
        }
      }
    }

  }


  @Override
  public void create()
  {
    // During initialization, figure out how we are running this application
    switch (Gdx.app.getType())
    {
      case Android:
        runningby = ANDROID;
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        break;
      case Desktop:
        runningby = DESKTOP;
        break;
      default:
        break;
    }
    /*// Setup connection to SPFZ database
    String url;

    if (runningby == ANDROID)
    {

      //FileHandle file = Gdx.files.internal("charinfo/spfz.sqlite");
      //url = "jdbc:sqlite:" + file.file().getAbsolutePath();
      //url = "jdbc:sqlite:" + Gdx.files.getLocalStoragePath() + "assets/charinfo/spfz.sqlite";
      //url = "jdbc:sqlite:" + Gdx.files.getExternalStoragePath() + "Android/data/assets/charinfo/spfz.sqlite";
      //url = "jdbc:sqlite:spfz.sqlite";
    }
    else if (runningby == DESKTOP)
    {

      //url = "jdbc:sqlite:C:/SPFZ Code/spfzalpha/android/assets/databases/spfz.sqlite";
      url = "jdbc:sqlite:S:/SPFZPROJBACKUP/spfzalpha/android/assets/charinfo/spfz.sqlite";


      //DriverManager.registerDriver(driver);
      Connection c = null;
      try
      {
        Class.forName("org.sqlite.JDBC");
        c = DriverManager.getConnection(url, "", "");

      *//*Class.forName("org.postgresql.Driver");
      c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/spfz",
      "postgres", "!nma0118prawn");*//*
      }
      catch (Exception e)
      {
        e.printStackTrace();
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
        System.exit(0);
      }
      finally
      {
        System.out.println("1st open successful");
        init();
      }

    }
    else
    {
      url = "N/A";
    }*/


  }

  public void animsel(Entity entity)
  {
    float DURATION = .1f;
    float scaleY = .43f;
    TransformComponent transform = new TransformComponent();
    DimensionsComponent dimension = new DimensionsComponent();
    ComponentMapper<TransformComponent> tc = ComponentMapper.getFor(TransformComponent.class);
    ComponentMapper<DimensionsComponent> dc = ComponentMapper.getFor(DimensionsComponent.class);
    transform = tc.get(entity);
    dimension = dc.get(entity);


    float origX = transform.scaleX;
    float origY = transform.scaleY;

    float increase = origY + (origY * .5f);


    Actions.addAction(entity,
      Actions.sequence(Actions.parallel(Actions.scaleTo(origX, increase, DURATION, Interpolation.swing)),
        Actions.scaleTo(origX, origY, DURATION, Interpolation.swing)));


  }

  public ParallelData createactions(Entity entity)
  {
    //SequenceData data = null;
    float DURATION = .2f;
    ParallelData data = null;
    TransformComponent transform = new TransformComponent();
    DimensionsComponent dimension = new DimensionsComponent();
    ComponentMapper<TransformComponent> tc = ComponentMapper.getFor(TransformComponent.class);
    ComponentMapper<DimensionsComponent> dc = ComponentMapper.getFor(DimensionsComponent.class);
    transform = tc.get(entity);
    dimension = dc.get(entity);

    transform.originX -= transform.originX * .5f;

    switch (curScene)
    {
      case "landscene":
        data = Actions.parallel(Actions.scaleTo(transform.scaleX, 0f, DURATION, Interpolation.swing),
          Actions.moveBy(0f, ((dimension.height * transform.scaleY) * .5f), DURATION, Interpolation.swing));
        //Actions.rotateTo(360f, DURATION));
        // Actions.moveBy(0f, 0f, .175f, Interpolation.circle));
        //data = Actions.sequence(Actions.parallel(Actions.scaleTo(transform.scaleX, 0f, .175f, Interpolation.circle)));
        break;
      case "sceneone":
        data = Actions.parallel(Actions.scaleTo(transform.scaleX, 0f, DURATION, Interpolation.swing),
          Actions.moveBy(0f, ((dimension.height * transform.scaleY) * .5f), DURATION, Interpolation.swing));
        break;
      default:
        data = Actions.sequence(Actions.parallel(Actions.scaleBy(2f, 2f, .2f), Actions.color(Color.RED, .2f)));
        break;
    }

    return data;
  }

  void debugRender()
  {
    if (view == "portrait")
    {
      shapeRenderer.setProjectionMatrix(viewportport.getCamera().combined);
      camera = (OrthographicCamera) viewportport.getCamera();
    }
    else
    {
      shapeRenderer.setProjectionMatrix(viewportland.getCamera().combined);
      camera = (OrthographicCamera) viewportland.getCamera();
    }
    shapeRenderer.begin(ShapeType.Line);
    shapeRenderer.setColor(Color.RED);

    @SuppressWarnings("unchecked")
    Family bounding = Family.all(BoundingBoxComponent.class).get();
    ImmutableArray<Entity> entities = update(view).engine.getEntitiesFor(bounding);

    for (Entity entity : entities)
    {
      BoundingBoxComponent boundingbox = boundingBox.get(entity);
      rect = boundingbox.getBoundingRect();
      shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
      for (int i = 0; i < 4; i++)
      {
        shapeRenderer.rect(boundingbox.points[i].x - 2, boundingbox.points[i].y - 2, 4, 4);
      }
    }
    shapeRenderer.setColor(Color.BLUE);

    shapeRenderer.setProjectionMatrix(viewportland.getCamera().combined);
    shapeRenderer.rect(camera.position.x - ((camera.viewportWidth * camera.zoom) / 4),
      camera.position.y - ((camera.viewportHeight * camera.zoom) / 4), (camera.viewportWidth * camera.zoom) / 2,
      (camera.viewportHeight * camera.zoom) / 2);

    shapeRenderer.end();

  }

  public boolean concheck(String scene)
  {
    fader = root.getChild("transition").getEntity();

    if (scene == "landscene" || scene == "sceneone")
    {
      if (fader.getComponent(TintComponent.class).color.a < .8f)
      {
        return true;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return false;
    }
  }

  /**
   * Method returns a boolean stating that the user has dragged off of the
   * button so we do not process the button
   */
  public boolean draggedfrmbtn(String button, boolean haschildren, String parent)
  {
    vec3 = new Vector3();
    Vector3 transpar = new Vector3(0, 0, 0);
    TransformComponent transcomponent;
    DimensionsComponent dimcomponent;
    Vector2 dimwh = new Vector2();
    if (haschildren)
    {
      if (root.getChild(parent).getEntity() != null)
      {

        transcomponent = ComponentRetriever.get(root.getChild(parent).getEntity(), TransformComponent.class);

        transpar.x = transcomponent.x;
        transpar.y = transcomponent.y;

        transcomponent = ComponentRetriever.get(root.getChild(parent).getChild(button).getEntity(),
          TransformComponent.class);
        dimcomponent = ComponentRetriever.get(root.getChild(parent).getChild(button).getEntity(),
          DimensionsComponent.class);
      }
      else
      {
        transcomponent = ComponentRetriever.get(pauseroot.getChild(parent).getEntity(), TransformComponent.class);

        transpar.x = transcomponent.x;
        transpar.y = transcomponent.y;

        transcomponent = ComponentRetriever.get(pauseroot.getChild(parent).getChild(button).getEntity(),
          TransformComponent.class);
        dimcomponent = ComponentRetriever.get(pauseroot.getChild(parent).getChild(button).getEntity(),
          DimensionsComponent.class);
      }
    }
    else
    {
      if (root.getChild(button).getEntity() != null)
      {
        transcomponent = ComponentRetriever.get(root.getChild(button).getEntity(), TransformComponent.class);
        dimcomponent = ComponentRetriever.get(root.getChild(button).getEntity(), DimensionsComponent.class);
        transpar.x = 0;
        transpar.y = 0;
      }
      else
      {
        transcomponent = new TransformComponent();
        dimcomponent = new DimensionsComponent();
      }

    }

    if (view.equals("portrait"))

    {
      viewportport.getCamera().update();
      vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      viewportport.unproject(vec3);
    }
    else

    {
      viewportland.getCamera().update();
      vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
      viewportland.unproject(vec3);
    }


    dimwh.x = dimcomponent.width * transcomponent.scaleX;
    dimwh.y = dimcomponent.height * transcomponent.scaleY;


    if (vec3.x >= transcomponent.x + transpar.x && vec3.x <= (transcomponent.x + dimwh.x + transpar.x)
      && vec3.y >= transcomponent.y + transpar.y && vec3.y <= (transcomponent.y + dimwh.y + transpar.y))

    {
      transcomponent = null;
      dimcomponent = null;
      dimwh = null;
      vec3 = null;
      transpar = null;
      return false;
    }
    else

    {
      transcomponent = null;
      dimcomponent = null;
      dimwh = null;
      vec3 = null;
      transpar = null;
      return true;
    }

  }

  /**
   * method fades the credits and arrows in and out
   */
  public void fadeCredit()
  {
    String[] cdtcomponents = {"swypefrmbtm", "swypefrmtop"};
    String[] constellations = {"terrence", "trey", "ahmed", "poweredby"}; //, "miklo", "miguel"};
    String[] creds = {"tocreditsone", "tocreditstwo"};
    String c = "const";
    String child = "draw";

    // move the credit text and arrows downwards
    if (view.equals("landscape"))
    {
      // move the credit text and arrows downwards
      Actions.addAction(root.getChild(cdtcomponents[1]).getEntity(), Actions.sequence(Actions.fadeIn(.05f * APP_SPD),
        Actions.parallel(Actions.moveBy(0, -20f, 1f * APP_SPD), Actions.fadeOut(1f * APP_SPD)), Actions.moveBy(0, 20f, .01f * APP_SPD)));

      // move the credit text and arrows upwards
      Actions.addAction(root.getChild(cdtcomponents[0]).getEntity(), Actions.sequence(Actions.fadeIn(.05f * APP_SPD),
        Actions.parallel(Actions.moveBy(0, 20f, 1f * APP_SPD), Actions.fadeOut(1f * APP_SPD)), Actions.moveBy(0, -20f, .01f * APP_SPD)));

      if (!credpress)
      {
        for (int i = 0; i < constellations.length; i++)
        {
          Actions.addAction(root.getChild(constellations[i] + c).getChild(child).getEntity(),
            Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.fadeOut(.3f * APP_SPD), Actions.fadeIn(.3f * APP_SPD), Actions.fadeOut(.3f * APP_SPD)));
        }
      }
    }
    else
    {
      for (int i = 0; i < creds.length; i++)
      {
        Actions.addAction(root.getChild(creds[i]).getEntity(),
          Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.fadeOut(.3f * APP_SPD), Actions.fadeIn(.3f * APP_SPD), Actions.fadeOut(.3f * APP_SPD)));
      }
    }
  }

  @Override
  public boolean fling(float velocityX, float velocityY, int button)
  {
    // The fling process controls when the user can go to the credits or back to
    // the main menu
    // Current process will allow you to fling up and down only when the user is
    // at the main menu,
    // and if the user has not pressed any of the constellations.

    if (!optionsup && view == "landscape" && ac != null && curScene == "landscene")
    {
      if (velocityY > 1000f && !credpress && !transition)
      {
        flingup = true;
        flingdown = false;
      }
      else if (velocityY < -1000f && !credpress && !transition)
      {
        flingdown = true;
        flingup = false;
      }
    }
    return true;
  }

  public void flypods()
  {
    scenesel++;
    if (view == "portrait")
    {
      if (!gamestart)
      {
        switch (scenesel)
        {
          case 1:

            Actions.addAction(root.getChild("toprightspfz").getEntity(),
              Actions.sequence(Actions.moveTo(0f, 340f, .8f, Interpolation.fade)));
            Actions.addAction(root.getChild("bottomrightspfz").getEntity(),
              Actions.sequence(Actions.moveTo(-25f, 21, 1.1f, Interpolation.fade)));
            Actions.addAction(root.getChild("topleftspfz").getEntity(),
              Actions.sequence(Actions.moveTo(266f, 354f, 1.4f, Interpolation.fade)));
            Actions.addAction(root.getChild("bottomspfz").getEntity(),
              Actions.sequence(Actions.moveTo(270f, 120f, 1.7f, Interpolation.fade)));

            break;
          case 2:
            Actions.addAction(root.getChild("bottomrightspfz").getEntity(),
              Actions.sequence(Actions.moveTo(-71f, -19, 1.4f, Interpolation.fade)));
            Actions.addAction(root.getChild("topleftspfz").getEntity(),
              Actions.sequence(Actions.moveTo(307f, 390f, 1.7f, Interpolation.fade)));
            Actions.addAction(root.getChild("bottomspfz").getEntity(),
              Actions.sequence(Actions.moveTo(292f, 164, 2f, Interpolation.fade)));
            break;
          case 3:
            Actions.addAction(root.getChild("topleftspfz").getEntity(),
              Actions.sequence(Actions.moveTo(295f, 413f, 1.7f, Interpolation.fade)));
            Actions.addAction(root.getChild("bottomspfz").getEntity(),
              Actions.sequence(Actions.moveTo(231f, 161f, 2f, Interpolation.fade)));
            break;
          case 4:
            Actions.addAction(root.getChild("bottomspfz").getEntity(),
              Actions.sequence(Actions.moveTo(202f, 8f, 2f, Interpolation.fade)));
            break;
          default:
            break;

        }
      }
      else
      {
        switch (scenesel)
        {
          case 1:
            Actions.addAction(fader, Actions.sequence(Actions.delay(.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
            {

              @Override
              public void run()
              {

                Actions.addAction(root.getChild("toprightspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(-127f, 340f, 2f, Interpolation.fade)));
                Actions.addAction(root.getChild("bottomrightspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(-10f, 60f, 2.3f, Interpolation.fade)));
                Actions.addAction(root.getChild("topleftspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(330f, 400f, 2.6f, Interpolation.fade)));
                Actions.addAction(root.getChild("bottomspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(300f, 95f, 2.9f, Interpolation.fade)));

              }
            })));

            break;
          case 2:
            Actions.addAction(fader, Actions.sequence(Actions.delay(.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
            {

              @Override
              public void run()
              {

                Actions.addAction(root.getChild("bottomrightspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(-71f, -19, 3f, Interpolation.fade)));
                Actions.addAction(root.getChild("topleftspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(307f, 390f, 3.3f, Interpolation.fade)));
                Actions.addAction(root.getChild("bottomspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(292f, 164, 3.6f, Interpolation.fade)));

              }
            })));
            break;
          case 3:
            Actions.addAction(fader, Actions.sequence(Actions.delay(.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
            {
              @Override
              public void run()
              {

                Actions.addAction(root.getChild("topleftspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(295f, 413f, 3f, Interpolation.fade)));
                Actions.addAction(root.getChild("bottomspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(231f, 161f, 3.3f, Interpolation.fade)));

              }
            })));
            break;
          case 4:
            Actions.addAction(fader, Actions.sequence(Actions.delay(.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
            {
              @Override
              public void run()
              {
                Actions.addAction(root.getChild("bottomspfz").getEntity(),
                  Actions.sequence(Actions.moveTo(2f, 8f, 3f, Interpolation.fade)));
              }
            })));
            break;
          default:
            break;

        }
      }


    }
  }

  public void genrand()
  {
    Random arcsel = new Random();
    byte randchar;
    byte randstg;

    randchar = 0;
    randstg = 0;
    String[] opponents = {"spriteball", "spriteballred", "spriteballblack", "spriteblock", "redblotch",
      "spritepurplex", "walksprite"};
    String[] stages = {"halloweenstage", "cathedralstage", "clubstage", "egyptstage", "futurestage", "gargoyle",
      "junglestage", "skullstage", "undergroundstage"};

    while (randchar == 0 || randchar == 7 && randstg == 0 || randstg == 9)
    {
      randchar = (byte) ((byte) arcsel.nextInt(6) + 1);
      randstg = (byte) ((byte) arcsel.nextInt(8) + 1);
    }

    p1char1 = charsselected.get(0);
    p1char2 = charsselected.get(1);
    p1char3 = charsselected.get(2);

    charsselected.set(3, opponents[randchar]);
    charsselected.set(4, opponents[randchar]);
    charsselected.set(5, opponents[randchar]);

    p2char1 = opponents[randchar];
    p2char2 = opponents[randchar];
    p2char3 = opponents[randchar];

    charcomposites.set(3, update(view).loadVoFromLibrary(charsselected.get(3)));
    // charcomposites.set(3,
    // update(view).loadVoFromLibrary(charsselected.get(3))).layerName =
    // MAIN_LAYER;

    charcomposites.set(4, update(view).loadVoFromLibrary(charsselected.get(4)));
    // charcomposites.set(4,
    // update(view).loadVoFromLibrary(charsselected.get(4))).layerName =
    // MAIN_LAYER;

    charcomposites.set(5, update(view).loadVoFromLibrary(charsselected.get(5)));
    // charcomposites.set(5,
    // update(view).loadVoFromLibrary(charsselected.get(5))).layerName =
    // MAIN_LAYER;

    charentities.set(3, update(view).entityFactory.createEntity(root.getEntity(), charcomposites.get(3)));
    charentities.set(4, update(view).entityFactory.createEntity(root.getEntity(), charcomposites.get(4)));
    charentities.set(5, update(view).entityFactory.createEntity(root.getEntity(), charcomposites.get(5)));

    selectedStage = stages[randstg];
    stageconfirmed = true;

    arcsel = null;
  }

  public void getscreenshot()
  {
    int gWidth = Gdx.graphics.getWidth();
    int gHeight = Gdx.graphics.getHeight();
    Texture pausescn = new Texture(gWidth, gHeight, Pixmap.Format.RGB888);
    Sprite pausetex = new Sprite(pausescn);
    byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(),
      Gdx.graphics.getBackBufferHeight(), true);

    // this loop makes sure the whole screenshot is opaque and looks exactly
    // like what the user is seeing
    for (int i = 4; i < pixels.length; i += 4)
    {
      pixels[i - 1] = (byte) 255;
    }

    Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(),
      Pixmap.Format.RGBA8888);
    BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);

    pausescn = new Texture(pixmap);
    pixmap.dispose();

    stage.pausetex = pausetex;

    if (pausetex.getWidth() != gWidth || pausetex.getHeight() != gHeight)
    {
      pausetex.setSize(gWidth, gHeight);
    }
    pausetex.setTexture(pausescn);

  }

  public void init()
  {

    logger = new FPSLogger();
    //GLProfiler.enable();

    byte AFTER_INTRO = 8;
    short WORLD_WIDTH = 640;
    short WORLD_HEIGHT = 400;
    gWidth = Gdx.graphics.getWidth();
    gHeight = Gdx.graphics.getHeight();
    storyline = null;
    /*// During initialization, figure out how we are running this application
    switch (Gdx.app.getType())
    {
      case Android:
        runningby = ANDROID;
        Gdx.input.setCatchBackKey(true);
        Gdx.input.setCatchMenuKey(true);
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        break;
      case Desktop:
        runningby = DESKTOP;
        break;
      default:
        break;
    }*/

    state = SPFZState.RUNNING;
    mainmenu = Gdx.audio.newMusic(Gdx.files.internal("music/Heclecta Main Menu.mp3"));
    desel = Gdx.audio.newSound(Gdx.files.internal("sound/deselect.ogg"));
    portbtns = Gdx.audio.newSound(Gdx.files.internal("sound/portconfirm.ogg"));
    back = Gdx.audio.newSound(Gdx.files.internal("sound/backbtn1.ogg"));
    ok = Gdx.audio.newSound(Gdx.files.internal("sound/okconfirm.ogg"));

    //if (runningby == ANDROID)
    //{
    // On initial install. Create the spfzfile by writing values.

    //}

    // Initializations
    init = true;

    //if (mainmenu.getVolume() > 0f)
   // {
     // mute = true;
    //}

    // set screen for inputs and set the starting point for the main menu
    // screen
    // music and start music


    loader = new AssetManager();
    rm.setManager(loader);
    // initialize the resource manager

    // spfzbsystem = new SPFZButtonSystem(this);
    spfzbsystem = new SPFZCharButtonSystem();
    stagesystem = new SPFZStageSystem();

    // rm = new SPFZResourceManager(this);
    // initialize viewports
    viewportland = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT);
    viewportport = new StretchViewport(WORLD_HEIGHT, WORLD_WIDTH);
    curScene = "landscene";

    port = new SPFZSceneLoader(rm, this, "", "");
    land = new SPFZSceneLoader(rm, this, "", "");
    pause = new SPFZSceneLoader(rm, this, "", "");
    logic = (SPFZParticleDrawableLogic) land.renderer.drawableLogicMapper.getDrawable(7);


    // initialize character arrays for Character Select Processing
    for (int i = 0; i < 6; i++)
    {
      charsselected.add(null);
      charcomposites.add(null);
      charentities.add(null);
    }

    // initialize characters
    for (int i = 0; i < 6; i++)
    {
      characters.add(null);
    }


    //}

    // Set the GestureDetector in order to utilize the camera lerping function
    // for Credits
    im = new InputMultiplexer();
    gd = new GestureDetector(this);

    im.addProcessor(gd);
    im.addProcessor(this);

    // initialize all resources
    //load process here

    rm.initAllResources();

    // Initialize the first scene

    prevScene = "landscene";

  }

  public void inMode()
  {

    // the view will have to be in landscape therefore we will be
    // setting the landscape view up for the back button.
    root.getEntity().removeAll();
    root = new ItemWrapper(update(view).getRoot());
    main = mc.get(root.getEntity());

    update(view).addComponentsByTagName("button", ButtonComponent.class);

    setSettings();
    // When we are at the character select scene we want to process so
    // we can
    // move forward
    // to the Stage Select scene
    //if (curScene != "arcadeselscn" && curScene != "stageselscene")
    if (curScene != "arcadeselscn" && curScene != "newstagesel")
    {
      float in = 350;
      float out = 420;
      fader = root.getChild("transition").getEntity();

      //process will determine which title will be set between Training Mode and Character Select Screen
      if (selecttype == 0)
      {
        root.getChild("csspng").getEntity().getComponent(TransformComponent.class).y = in;
        root.getChild("tmpng").getEntity().getComponent(TransformComponent.class).y = out;
      }
      else
      {
        root.getChild("tmpng").getEntity().getComponent(TransformComponent.class).y = in;
        root.getChild("csspng").getEntity().getComponent(TransformComponent.class).y = out;
      }

      charselIntro();
      charSel(false);
      root.getChild("okaybutton").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {

            if (curScene == "charselscene")
            {
              p1char1 = charsselected.get(0);
              p1char2 = charsselected.get(1);
              p1char3 = charsselected.get(2);
              p2char1 = charsselected.get(3);
              p2char2 = charsselected.get(4);
              p2char3 = charsselected.get(5);

              if (p2char3 == null)
              {

              }
              else
              {
                ok.play(1.0f);
                Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                {

                  @Override
                  public void run()
                  {

                    root.getEntity().removeAll();
                    //curScene = "stageselscene";
                    curScene = "newstagesel";

                    land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");
                    // land.engine.removeSystem(update(view).engine.getSystem(ScriptSystem.class));
                    // land.engine.removeSystem(update(view).engine.getSystem(PhysicsSystem.class));

                    //load process here
                    update(view).loadScene(curScene, viewportland);
                    root = new ItemWrapper(update(view).getRoot());
                    stageSel();
                  }
                })));

              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });
    }
    else if (curScene == "arcadeselscn")
    {

      charSel(true);
    }

    // If a stage has been selected and "OK" has been pressed, setup for
    // the
    // stage selected
    // and set the stage run variable to true to process the camera
    // within the
    // screen

    root.getChild("backbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          back.play(1.0f);
          Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
          {

            @Override
            public void run()
            {

              state = SPFZState.RESUME;
              backprocessing();
            }
          })));

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });

  }

  /**
   * method contains the intro animation
   */
  public void Intro()
  {
    mainmenu.setPosition(0);
    String[] cdtcomponents = {"ttcimage", "swypefrmbtm", "swypefrmtop"};
    if (curScene == "sceneone" && init == true)
    {
      Actions.addAction(root.getChild("ttcimage").getEntity(), Actions.fadeOut(0f));

      // screen transition
      Actions.addAction(fader,
        Actions.sequence(Actions.delay(3f), Actions.parallel(Actions.fadeOut(0f), Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            Actions.addAction(root.getChild("animcircle").getEntity(), Actions.sequence(Actions.delay(1.5f),
              Actions.parallel(Actions.run(new Runnable()
              {
                @Override
                public void run()
                {
                  mainmenu.play();
                }
              }), Actions.scaleBy(6f, 6f, .7f), Actions.moveBy(0, 0, .3f), Actions.fadeOut(2f))));
            Actions.addAction(root.getChild("introcircle").getEntity(), Actions.sequence(Actions.delay(3.5f), Actions
              .parallel(Actions.scaleBy(40f, 40f, 2.2f), Actions.moveBy(0, -2400f, 2.2f), Actions.run(new Runnable()
              {

                @Override
                public void run()
                {
                  uicomplete = true;
                }
              }))));

            Actions.addAction(root.getChild("ttcimage").getEntity(),
              Actions.sequence(Actions.delay(8f), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
                Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
                Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(1f * APP_SPD)));
          }
        }))));


      init = false;
    }

    if (curScene == "landscene" && init == true)
    {

      for (int i = 0; i < cdtcomponents.length; i++)
      {
        Actions.addAction(root.getChild(cdtcomponents[i]).getEntity(), Actions.fadeOut(0f));
      }


      // screen transition
      Actions.addAction(fader,
        Actions.sequence(Actions.delay(3f), Actions.parallel(Actions.fadeOut(0f), Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            mainmenu.play();
          }
        })), Actions.run(new Runnable()
        {

          @Override
          public void run()
          {
            Actions.addAction(root.getChild("animscn").getEntity(), Actions.sequence(Actions.delay(1.5f),
              Actions.parallel(Actions.scaleBy(6f, 6f, .7f),
                Actions.run(new Runnable()
                {
                  @Override
                  public void run()
                  {
                    mainmenu.play();
                  }
                }), Actions.moveBy(0f, 0f, .3f), Actions.fadeOut(1f))));
            Actions.addAction(root.getChild("lintro").getEntity(),
              Actions.sequence(Actions.delay(3.5f), Actions.parallel(Actions.scaleBy(100f, 100f, 2.2f),
                Actions.moveBy(-150f, -200f, 2.2f), Actions.run(new Runnable()
                {

                  @Override
                  public void run()
                  {
                    uicomplete = true;
                  }
                }))));

            Actions.addAction(root.getChild("ttcimage").getEntity(),
              Actions.sequence(Actions.delay(8f), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
                Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
                Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(1f * APP_SPD)));
          }
        })));


      init = false;
    }

    INTRO += 1;
  }

  @Override
  public boolean keyDown(int keycode)
  {
    return false;
  }

  @Override
  public boolean keyTyped(char character)
  {
    return false;
  }

  @Override
  public boolean keyUp(int keycode)
  {
    return false;
  }

  public void loadslot(int position, boolean arcade)
  {
    //String[] platforms = {"p1platone", "p1plattwo", "plplatthree", "p2platone","p2plattwo", "p2platthree"};

    // first half of loading is handle within the setcharsprites() method
    // however the first half is commented out and kept here for reference

    // charsselected.set(slot, character);
    // charcomposites.set(position,
    // update(view).loadVoFromLibrary(charsselected.get(position)));
    // charcomposites.set(position,
    // update(view).loadVoFromLibrary(charsselected.get(position))).layerName =
    // MAIN_LAYER;

    // if (charentities.get(i) == null)
    // {
    charentities.set(position, update(view).entityFactory.createEntity(root.getEntity(), charcomposites.get(position)));
    update(view).entityFactory.initAllChildren(update(view).getEngine(), charentities.get(position),
      charcomposites.get(position).composite);
    update(view).getEngine().addEntity(charentities.get(position));

    if (arcade)
    {
      arcselposition(position);
    }
    else
    {
      ssposition(position);
    }

  }

  @Override
  public boolean longPress(float x, float y)
  {

    return false;
  }

  @Override
  public boolean mouseMoved(int screenX, int screenY)
  {

    return false;
  }

  /**
   * method controls the elements within the intro
   */
  public void omitintro()
  {

    update(view).getEngine().removeEntity(root.getChild("animcircle").getEntity());
    update(view).getEngine().removeEntity(root.getChild("introcircle").getEntity());
    // rmvintro = true;

  }

  @Override
  public boolean pan(float x, float y, float deltaX, float deltaY)
  {

    return false;
  }

  @Override

  public boolean panStop(float x, float y, int pointer, int button)
  {

    return false;
  }

  @Override
  public void pause()
  {
    if (!exit)
    {
      savescene = scenesel;
      if (stage != null)
      {
        if (!paused && !stage.gameover)
        {
          paused = true;
        }
        stage.pauseset = false;
        stage.pausetime = true;
        if (!stage.gameover)
        {
          // Call the Life and Special system update methods
          // prior to retrieving the screenshot in order to
          // have the special and life bars present during
          // the pause menu display
          /*update(view).getEngine().getSystem(LifeSystem.class).update(Gdx.graphics.getDeltaTime());
          update(view).getEngine().getSystem(SpecialSystem.class).update(Gdx.graphics.getDeltaTime());*/

          getscreenshot();
        }
        stageset = false;
        state = SPFZState.PAUSE;
        pausing();
      }
    }
  }

  public void pauseoptions()
  {

    pauseroot = new ItemWrapper(pause.getRoot());
    transform = tc.get(pauseroot.getEntity());
    action = ac.get(pauseroot.getEntity());

    pause.addComponentsByTagName("button", ButtonComponent.class);

    if (paused)
    {
      pauseroot.getChild("endoffightmenu").getEntity().getComponent(TintComponent.class).color.a = 0f;
      pauseroot.getChild("pausemenu").getEntity().getComponent(TintComponent.class).color.a = 1f;

      pauseroot.getChild("pausemenu").getChild("resumebutton").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {
            if (!stage.pauseconfirm)
            {
              if (draggedfrmbtn("resumebutton", true, "pausemenu"))
              {

              }
              else
              {
                resumefrmpause();
              }
            }
          }

          @Override
          public void touchDown()
          {
            restart = true;
            restarttime = System.currentTimeMillis();
            restarttint = pauseroot.getChild("pausemenu").getChild("resumebutton").getChild("restart").getEntity()
              .getComponent(TintComponent.class);

          }

          @Override
          public void touchUp()
          {
            if (restarttint.color.a <= .9f)
            {
              restart = false;
              Actions.addAction(
                pauseroot.getChild("pausemenu").getChild("resumebutton").getChild("restart").getEntity(),
                Actions.fadeOut(.01f * APP_SPD));
            }
          }
        });
      pauseroot.getChild("pausemenu").getChild("charselbutton").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {
            // Bring up a confirmation to ensure user wants to go to character
            // select
            if (!stage.pauseconfirm)
            {
              if (draggedfrmbtn("charselbutton", true, "pausemenu"))
              {

              }
              else
              {
                pmenuopt = 1;
                bringupconfirm();
              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });

/*      pauseroot.getChild("pausemenu").getChild("movesetbtn").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {
            if (!stage.pauseconfirm)
            {
              // Bring up the moveset for each character that has been
              // selected
              // for the fight
              if (draggedfrmbtn("movesetbtn", true, "pausemenu"))
              {

              }
              else
              {

              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });*/

      pauseroot.getChild("pausemenu").getChild("mainmenubutton").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {

            if (!stage.pauseconfirm)
            {
              if (draggedfrmbtn("mainmenubutton", true, "pausemenu"))
              {

              }
              else
              {
                pmenuopt = 2;
                bringupconfirm();
              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });
      pauseroot.getChild("pausemenu").getChild("yesbtn").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {
            fader = pauseroot.getChild("pausemenu").getChild("fader").getEntity();
            if (stage.pauseconfirm)
            {
              if (draggedfrmbtn("yesbtn", true, "pausemenu"))
              {

              }
              else
              {
                switch (pmenuopt)
                {
                  case 0:

                    Actions.addAction(fader, Actions.sequence(Actions.fadeIn(1f * APP_SPD), Actions.run(new Runnable()
                    {

                      @Override
                      public void run()
                      {
                        stoprender = true;
                        Gdx.gl.glClearColor(0, 0, 0, 1);
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                        restartmatch();
                      }
                    })));

                    break;
                  case 1:
                    Actions.addAction(fader, Actions.sequence(Actions.fadeIn(1f * APP_SPD), Actions.run(new Runnable()
                    {

                      @Override
                      public void run()
                      {
                        stoprender = true;
                        Gdx.gl.glClearColor(0, 0, 0, 1);
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                        toCharSel();
                      }
                    })));
                    break;
                  case 2:
                    Actions.addAction(fader, Actions.sequence(Actions.fadeIn(1f * APP_SPD), Actions.run(new Runnable()
                    {

                      @Override
                      public void run()
                      {
                        stoprender = true;
                        Gdx.gl.glClearColor(0, 0, 0, 1);
                        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                        toMenu();
                      }
                    })));
                    break;
                  default:
                    break;
                }
              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });
      pauseroot.getChild("pausemenu").getChild("nobtn").getEntity().getComponent(ButtonComponent.class)
        .addListener(new ButtonComponent.ButtonListener()
        {

          @Override
          public void clicked()
          {

            if (stage.pauseconfirm)
            {
              if (draggedfrmbtn("nobtn", true, "pausemenu"))
              {

              }
              else
              {
                Actions.addAction(pauseroot.getChild("pausemenu").getChild("resumebutton").getEntity(),
                  Actions.fadeIn(PAUSE_BTN_SPD * APP_SPD));
                Actions.addAction(pauseroot.getChild("pausemenu").getChild("charselbutton").getEntity(),
                  Actions.fadeIn(PAUSE_BTN_SPD * APP_SPD));
                //Actions.addAction(pauseroot.getChild("pausemenu").getChild("movesetbtn").getEntity(),
                //  Actions.fadeIn(PAU * APP_SPDSE_BTN_SPD));
                Actions.addAction(pauseroot.getChild("pausemenu").getChild("mainmenubutton").getEntity(),
                  Actions.fadeIn(PAUSE_BTN_SPD * APP_SPD));

                Actions.addAction(pauseroot.getChild("pausemenu").getChild("yesbtn").getEntity(),
                  Actions.fadeOut(PAUSE_BTN_SPD * APP_SPD));
                Actions.addAction(pauseroot.getChild("pausemenu").getChild("nobtn").getEntity(),
                  Actions.fadeOut(PAUSE_BTN_SPD * APP_SPD));

                stage.pauseconfirm = false;
              }
            }
          }

          @Override
          public void touchDown()
          {

          }

          @Override
          public void touchUp()
          {

          }
        });

    }
    else
    {
      pauseroot.getChild("pausemenu").getEntity().getComponent(TintComponent.class).color.a = 0f;
      pauseroot.getChild("endoffightmenu").getEntity().getComponent(TintComponent.class).color.a = 1f;
      seteofbtns();
    }
  }

  public void pausing()
  {
    short HALF_WORLDW = 320;
    short HALF_WORLDH = 200;

    if (pauseroot != null && !stage.gameover)
    {
      pauseroot.getChild("endoffightmenu").getEntity().getComponent(TintComponent.class).color.a = 0;
    }

    if (!pmenuloaded)
    {

      scenesel = 5;

      if (!stageset)
      {
        stage.stagetempx = viewportland.getCamera().position.x;
        stage.stagetempy = viewportland.getCamera().position.y;

        stageset = true;
      }

      pause.loadScene("pausescene", viewportland);
      viewportland.getCamera().position.set(stage.stagetempx, stage.stagetempy, 0);
      viewportland.getCamera().update();
      pauseoptions();

      // Set the pause menus and screen transition to the correct positioning

      pauseroot.getChild("pausemenu").getEntity()
        .getComponent(TransformComponent.class).x = viewportland.getCamera().position.x - HALF_WORLDW;
      pauseroot.getChild("pausemenu").getEntity()
        .getComponent(TransformComponent.class).y = viewportland.getCamera().position.y - HALF_WORLDH;

      pauseroot.getChild("endoffightmenu").getEntity()
        .getComponent(TransformComponent.class).x = viewportland.getCamera().position.x - HALF_WORLDW;
      pauseroot.getChild("endoffightmenu").getEntity()
        .getComponent(TransformComponent.class).y = viewportland.getCamera().position.y - HALF_WORLDH;

      if (stage.gameover && !isArcade)
      {

        pauseroot.getChild("endoffightmenu").getChild("eof").getEntity().getComponent(TintComponent.class).color.a = 1;
        pauseroot.getChild("endoffightmenu").getChild("pabtn").getEntity()
          .getComponent(TintComponent.class).color.a = 1;
        pauseroot.getChild("endoffightmenu").getChild("csbtn").getEntity()
          .getComponent(TintComponent.class).color.a = 1;
        pauseroot.getChild("endoffightmenu").getChild("mmbtn").getEntity()
          .getComponent(TintComponent.class).color.a = 1;
        // Add Arcade end of game functionality

      }
      pmenuloaded = true;
    }

    else
    {

      if (!stoprender && !stage.gameover)
      {
        renderss();
      }

      if (restart && !isArcade)
      {
        chkrstrt();
      }
      pause.getEngine().update(Gdx.graphics.getDeltaTime());
    }

    if (stage != null)
    {

      stage.pausedElapsed = System.currentTimeMillis();
      stage.firstpause = String.valueOf(stage.pausedElapsed).substring(0, 1);
      // Begin next arcade stage
      if (isArcade && stage.gameover && Gdx.input.isTouched())
      {
        paused = false;
        pause.engine.removeAllEntities();
        pause.entityFactory.clean();
        land.engine.removeAllEntities();
        land.entityFactory.clean();
        root.getEntity().removeAll();

        land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");
        stage = null;

        state = SPFZState.RESUME;
        pmenuloaded = false;
        isloading = false;
        setupArcade(charsselected.get(0));
        genrand();
      }
    }
  }


  @Override
  public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2)
  {

    return false;
  }

  public void pinchStop()
  {

  }

  public void processback()
  {
    if (credpress)
    {
      credpress = false;
      credittime = System.currentTimeMillis();
      //constel = "";
    }

    if (!optionsup)
    {
      backprocessing();
    }

  }

  public void processcharselect()
  {
    if (partstartone)
    {

      if (root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(0, isArcade);
        partstartone = false;
      }
    }
    if (partstarttwo)
    {

      if (root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(1, isArcade);
        partstarttwo = false;
      }
    }
    if (partstartthree)
    {
      if (root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(2, isArcade);
        partstartthree = false;
      }
    }
    if (partstartfour)
    {
      if (root.getChild("fourthcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(3, isArcade);
        partstartfour = false;
      }
    }
    if (partstartfive)
    {
      if (root.getChild("fifthcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(4, isArcade);
        partstartfive = false;
      }
    }
    if (partstartsix)
    {
      if (root.getChild("sixthcharpart").getEntity().getComponent(SPFZParticleComponent.class).pooledeffects.get(0)
        .isComplete())
      {

        loadslot(5, isArcade);
        partstartsix = false;
      }
    }
  }

  public void getStoryText(String File)
  {
    FileHandle file = Gdx.files.internal(File + ".txt");
    String text = file.readString();

    storysplit = text.split("\r\n");

  }

  /**
   * display next paragraph from text file
   */
  public void readnext(String storychar)
  {
    charfound = false;
    displaytext = "";
    paragraph += 1;

    for (int i = 0; i < storysplit.length; i++)
    {
      if (storysplit[i].equals(storychar))
      {
        charfound = true;
      }

      if (charfound)
      {
        if (storysplit[i].contains(Integer.toString(level) + Integer.toString(paragraph)))
        {
          for (int j = i + 1; j < i + 5; j++)
          {
            displaytext += storysplit[j] + "\n";
          }
          i = storysplit.length;
        }
      }
    }
  }

  public void getAnimData()
  {

  }

  public void reloadchars()
  {

    for (byte i = 0; i < charsselected.size(); i++)
    {

      // slot = i;

      if (charsselected.get(i) == null)
      {

        if (!charpicked)
        {

          // charcomposites.set(i,
          // update(view).loadVoFromLibrary(charsselected.get(i)));
          // charcomposites.set(i,
          // update(view).loadVoFromLibrary(charsselected.get(i))).layerName =
          // MAIN_LAYER;

          // second half of character processing is commented out here but
          // executes within the
          // loadslot() method. The method executes when the character slot
          // particle is finished

          // charentities.set(i,
          // update(view).entityFactory.createEntity(root.getEntity(),
          // charcomposites.get(slot)));
          // update(view).entityFactory.initAllChildren(update(view).getEngine(),
          // charentities.get(i),
          // charcomposites.get(i).composite);


          switch (i)
          {
            case 0:
              root.getChild("charonelbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("charonelbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
              root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              partstartone = true;
              break;
            case 1:
              root.getChild("chartwolbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("chartwolbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
              root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              partstarttwo = true;
              break;
            case 2:
              root.getChild("charthreelbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

              root.getChild("charonelbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
              root.getChild("chartwolbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;

              root.getChild("playerlbl").getEntity().getComponent(TintComponent.class).color = Color.RED;
              partstartthree = true;
              break;
            case 3:
              root.getChild("charfourlbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("charfourlbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
              root.getChild("fourthcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("fourthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              partstartfour = true;
              break;
            case 4:
              root.getChild("charfivelbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("charfivelbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
              root.getChild("fifthcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("fifthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
              partstartfive = true;
              break;
            case 5:
              root.getChild("charsixlbl").getEntity().getComponent(LabelComponent.class).setText(charsselected.get(i));
              root.getChild("sixthcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
              root.getChild("sixthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

              root.getChild("charfourlbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
              root.getChild("charfivelbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
              root.getChild("cpulbl").getEntity().getComponent(TintComponent.class).color = Color.BLUE;
              root.getChild("cpupng").getEntity().getComponent(TintComponent.class).color = Color.BLUE;

              partstartsix = true;
              break;
            default:
              break;

          }
        }
      }
    }
  }

  public void render()
  {

    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (Gdx.input.getInputProcessor() != im && curScene != "stageselscn")
    {
      Gdx.input.setInputProcessor(im);
    }

    if (paused)
    {
      state = SPFZState.PAUSE;
    }

    switch (state)
    {
      case RUNNING:

        running();
        break;

      case STOPPED:

        break;

      case PAUSE:
        /*if (stage == null)
        {
          state = SPFZState.RESUME;
        }
        else
        {
          pausing();
        }*/
        if (stage != null)
        {
          pausing();
        }
        break;

      case RESUME:
        state = SPFZState.RUNNING;
        break;

    }
    if (stage == null)
    {
      UIProcessing();
    }
    else
    {
      if (Gdx.input.isKeyJustPressed(Keys.BACK))
      {
        paused = true;
      }
    }

    logger.log();


    // logger.log();
    // System.ou.println("Number of draw calls: " + GLProfiler.drawCalls);
    // System.out.println("Number of texture bindings: " +
    // GLProfiler.textureBindings);
    // System.out.println("Number of vertices rendered: " +
    // GLProfiler.vertexCount.total);
    // GLProfiler.reset();

  }

  public void renderss()
  {

    if(stage.getBatch().getColor().r != .75f)
    {
      stage.getBatch().setColor(.75f, .75f, .75f, 1f);
    }

    stage.getBatch().begin();
    stage.getBatch().draw(stage.pausetex, 0, 0);
    stage.getBatch().end();
  }

  public void continuestory()
  {
    readnext(storyline);

    if (displaytext != "")
    {
      if (paragraph == 1)
      {
        Actions.addAction(root.getChild("arcadetext").getEntity(),
          Actions.sequence(Actions.delay(1.5f), Actions.fadeOut(.3f * APP_SPD), Actions.run(new Runnable()
          {

            @Override
            public void run()
            {
              root.getChild("arcadetext").getEntity().getComponent(LabelComponent.class).setText(displaytext);

            }
          }), Actions.fadeIn(1f * APP_SPD)));
      }
      else
      {
        Actions.addAction(root.getChild("arcadetext").getEntity(),
          Actions.sequence(Actions.fadeOut(.8f * APP_SPD), Actions.run(new Runnable()
          {

            @Override
            public void run()
            {
              root.getChild("arcadetext").getEntity().getComponent(LabelComponent.class).setText(displaytext);

            }
          }), Actions.fadeIn(1f * APP_SPD)));
      }
    }
    else
    {
      //close the storytext scene and create the new level for the main character(1st character selected)
      Actions.addAction(root.getChild("fader").getEntity(), Actions.sequence(Actions.fadeIn(1f * APP_SPD),
        Actions.delay(.2f), Actions.run(new Runnable()
        {
          @Override
          public void run()
          {
            createstage();
          }
        })));

      // isArcade = false;
      // isArcade = true;
      inact = false;
    }
  }

  public void procname(int name, int pos)
  {
    if (pos <= 3)
    {

    }
    else
    {

    }
  }

  public void resize(int width, int height)
  {

    if (runningby == ANDROID && !android.banner_null())
    {
      android.hideAD();
    }
    if (viewportport == null || viewportland == null)
    {
      init();
    }
    int gWidth;
    int gHeight;
    gWidth = width;
    gHeight = height;

    // needs to be commented out once testing is done
    uicomplete = true;
    if (state != SPFZState.PAUSE)
    {

      if (width > height)
      {
        view = "landscape";

        if (stage != null)
        {
          stage.getViewport().update(width, height);
        }

        // for now we are setting uicomplete to true to clear events for
        // the
        // portrait view
        // uicomplete = true;

        // If the user is within the main menu screens
        if (!frmresume)
        {
          if (!mode)
          {

            curScene = "landscene";

            update(view).loadScene(curScene, viewportland);

            root = new ItemWrapper(update(view).getRoot());
            setMainMenu(gWidth, gHeight);

          }
          else
          {

            land.engine.getSystem(SPFZParticleSystem.class).setscene(curScene);
            update(view).loadScene(curScene, viewportland);
            inMode();
          }
        }
        else
        {
          frmresume = false;
        }

      }
      else
      {

        prevScene = curScene;
        view = "portrait";

        // Main Menu Portrait Screen processing to switch to the five
        // different
        // views
        // Setup for new SceneLoader is needed for whenever the game is
        // resetting
        // back to the portrait
        // version of the main menu
        if (!mode)
        {

          if (scenesel == 4)
          {
            scenesel = 0;

          }
          if (frmresume)
          {
            scenesel = savescene;
            frmresume = false;
          }
          else
          {
            curScene = "sceneone";

            // port SceneLoader must be re-initialized in order for the
            // portrait
            // view to recover from the landscape lock

            if (viewportport != null)
            {
              port = new SPFZSceneLoader(rm, this, "", "");
              // port.engine.removeSystem(update(view).engine.getSystem(ScriptSystem.class));
              // port.engine.removeSystem(update(view).engine.getSystem(PhysicsSystem.class));
              // port.engine.removeSystem(update(view).engine.getSystem(CompositeSystem.class));
              update(view).loadScene(curScene, viewportport);
              // rm.unLoadScene(prevScene);
              // rm.unLoadSceneVO(prevScene);

              // rm.initScene("landscene");
              root = new ItemWrapper(update(view).getRoot());
              prevScene = curScene;

              setMainMenu(width, height);
            }
            // if (gamestart)
            // {
            // scenesel++;
            // }

          }
        }
      }

    }
    else
    {
      pause.loadScene("pausescene", viewportland);

    }

    if (viewportport != null || viewportland != null)
    {
      logic.setscene(curScene);
    }
    /*
     * @SuppressWarnings("unchecked") ImmutableArray<Entity> dimensionEntities =
     * update(view).engine.getEntitiesFor(Family.all(DimensionsComponent.class).
     * get()); for (Entity entity : dimensionEntities) { entity.add(new
     * BoundingBoxComponent()); }
     */


  }

  public void restartmatch()
  {
    float[] bounds = {80, 560};
    // Match restart logic here

    stage.dispose();
    resumefrmpause();

    if (stage != null)
    {
      stage = null;
    }

    update(view).loadScene("stagescene", viewportland);

    root = new ItemWrapper(update(view).getRoot());
    transform = tc.get(root.getEntity());
    action = ac.get(root.getEntity());

    setupstage();

    stage = new SPFZStage(update(view).getRm(), viewportland.getCamera(), selectedStage, runningby, bounds, characters,
      spfzTrial.this, istraining);
  }

  public void resume()
  {
    pmenuloaded = false;
    if (!paused)
    {
      state = SPFZState.RESUME;
      frmresume = true;
      if (stage != null)
      {
        // if (stage.pausecam != null)
        // {
        // viewportland.getCamera().position.set(stage.pausecam);
        // }
        // stage.time = System.currentTimeMillis();
        stage.time = System.currentTimeMillis() - (Long.parseLong(stage.firstpause) - stage.pausedElapsed);
      }
    }
  }

  // method handles what actions will take place when the back option is
  // selected

  // method handles the pause menu when in a match

  public void resumefrmpause()
  {
    paused = false;
    pause.engine.removeAllEntities();
    pause.entityFactory.clean();
    state = SPFZState.RESUME;
    pmenuloaded = false;
    stage.pausetime = false;

    // not correct exactly but is working for now set the time back to the
    // correct seconds for the unpausing
    stage.time = System.currentTimeMillis() - ((stage.optime - stage.timeleft) * 1000);

    // stage.time = System.currentTimeMillis() -
    // (Long.parseLong(stage.firstpause) - stage.pausedElapsed);

  }

  public void running()
  {
    if (isArcade)
    {
      arcadeprocessing();
    }

    // update the camera
    if (view == "landscape")
    {
      viewportland.apply();
    }
    else
    {
      viewportport.apply();
    }

    if (state != SPFZState.PAUSE)
    {

      //if(adtime < System.currentTimeMillis() + Math.random(20))
      //{

      displayAD(curScene, adtime, randsecs);
      if (runningby == ANDROID && Gdx.input.isTouched())
      {
        android.hideAD();
      }

      //}
      // > ----------- RENDER AND UPDATE ENGINE LINE
      // ------------ <

      // update method returns an Overlap2d Scene and renders each entity

      // slow downs will need to be implemented by multiplying an amount * Delta
      if (slowtime)
      {
        update(view).getEngine().update(Gdx.graphics.getDeltaTime() * 0.8f);
      }
      else
      {

        update(view).getEngine().update(Gdx.graphics.getDeltaTime());
      }

      // > ----------------------------------- PROCESS STAGE
      // ---------------------------------- <

      // STAGE SHOULD BE RENDERED LAST AS THE STAGE NEEDS TO BE OVER THE STAGE
      // BACKGROUND

      // STAGE BACKGROUND IS AN ENTITY THAT IS ADDED TO THE SCENE

      if (stage != null)
      {
        stageprocessing();

        if (Gdx.input.isKeyJustPressed(Keys.T))
        {
          if (!slowtime)
          {
            slowtime = true;
            stage.spfzp1move.animationstate().paused = true;
          }
          else
          {
            slowtime = false;
            stage.spfzp1move.animationstate().paused = false;
          }
        }
      }

      // debugRender();
      // > ---------------------------- END OF STAGE PROCESSING
      // ------------------------------- <
    }

    // loop main menu music
     if (mainmenu.getPosition() > END_OF_THEME)
     {
       mainmenu.setPosition(BEAT_DROP);
     }

    // > ------------------- HANDLE INITIALIZATION PROCESS
    // ---------------------------------- <

    if(mainmenu.getPosition() >= FOUR_COUNT && mainmenu.isPlaying())
    {
      mainmenu.pause();
    }
    if (Gdx.input.isTouched() && INTRO == 1 && uicomplete == true || Gdx.input.isTouched() && !gamestart && INTRO == 2 || Gdx.input.isKeyJustPressed(Keys.SPACE))
    {
      gamestart = true;
      // recall setmainmenu method to pull up the buttons after intro
      // stuff
      // has
      // been handled

      setMainMenu(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }


    // > --------------------- END OF INITIALIZATION PROCESS
    // -------------------------------- <

    // > ------------------------PROCESS CHARACTER SELECT SCREEN
    // ---------------------------- <
    if (curScene == "charselscene" || curScene == "arcadeselscn")
    {
      processcharselect();

      if (longclear)
      {
        clearAll();
      }
    }

    // > ----------------- END OF CHARACTER SELECT SCREEN PROCESSING
    // ------------------------ <

  }

  @Override
  public boolean scrolled(int amount)
  {
    return false;
  }

  /**
   * Method sets up the sprites and loads them onto the platforms during the
   * Arcade mode
   */
  public void setcharasprites(String string)
  {
    String MAIN_LAYER = "Default";

    // Set and add the sprites to the character select screen
    for (int i = 0; i < charsselected.size() - 3; i++)
    {
      if (charsselected.get(i) == string)
      {
        i = 3;
        charpicked = true;
      }
      else
      {
        if (charsselected.get(i) == null)
        {

          if (!charpicked)
          {
            charsselected.set(i, string);
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i) + "idle"));
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i))).layerName = MAIN_LAYER;
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i) + "idle")).layerName = MAIN_LAYER;
            /*
             * charentities.set(i,
             * update(view).entityFactory.createEntity(root.getEntity(),
             * charcomposites.get(i)));
             * update(view).entityFactory.initAllChildren(update(view).getEngine
             * (), charentities.get(i), charcomposites.get(i).composite);
             * update(view).getEngine().addEntity(charentities.get(i));
             *
             * arcselposition(i);
             */

            switch (i)
            {
              case 0:
                root.getChild("charonelbl").getEntity().getComponent(LabelComponent.class).setText(string);
                root.getChild("charonelbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstartone = true;
                break;
              case 1:
                root.getChild("chartwolbl").getEntity().getComponent(LabelComponent.class).setText(string);
                root.getChild("chartwolbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild("secondcharpart").getEntity()
                  .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstarttwo = true;
                break;
              case 2:
                root.getChild("charthreelbl").getEntity().getComponent(LabelComponent.class).setText(string);
                root.getChild("charonelbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                root.getChild("chartwolbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1;
                root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstartthree = true;
                break;
              default:
                break;
            }
            i = 3;
          }
        }
      }
    }

  }

  /**
   * Method adds the sprites to the character select screen
   */
  public void setcharsprites(String string, String button)
  {
    String MAIN_LAYER = "Default";

    String left = "one";
    String right = "two";
    for (int i = 0; i < charsselected.size(); i++)
    {
      // Check allows the same character to be selected on each side.
      if (charsselected.get(i) == string && charsselected.get(2) == null || i >= 3 && charsselected.get(i) == string)
      {
        i = 6;
        charpicked = true;
      }
      else
      {

        character = string;



        if (charsselected.get(i) == null)
        {

          if (!charpicked)
          {
            charsselected.set(i, string);
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i) + "idle"));
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i))).layerName = MAIN_LAYER;
            charcomposites.set(i, update(view).loadVoFromLibrary(charsselected.get(i) + "idle")).layerName = MAIN_LAYER;


            // second half of character processing is commented out here but
            // executes within the
            // loadslot() method. The method executes when the character slot
            // particle is finished

            // charentities.set(i,
            // update(view).entityFactory.createEntity(root.getEntity(),
            // charcompositevs.get(slot)));
            // update(view).entityFactory.initAllChildren(update(view).getEngine(),
            // charentities.get(i),
            // charcomposites.get(i).composite);


            switch (i)
            {
              case 0:

                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p1platone").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p1platone").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).width * .5f;
                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p1platone").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).height;

                root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("firstcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstartone = true;
                break;
              case 1:

                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p1plattwo").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p1plattwo").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).width * .5f;
                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p1plattwo").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).height;

                root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("secondcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstarttwo = true;
                break;
              case 2:

                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p1platthree").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p1platthree").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).width * .5f;

                root.getChild(charsselected.get(i) + left).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p1platthree").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + left).getEntity().getComponent(DimensionsComponent.class).height;

                root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("thirdcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

                //set graphic colors to white
                for (int j = 0; j <= 2; j++)
                {
                  root.getChild(charsselected.get(j) + left).getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                }

                root.getChild("playerpng").getEntity().getComponent(TintComponent.class).color = Color.RED;
                partstartthree = true;
                break;
              case 3:
               /* root.getChild("charfourlbl").getEntity().getComponent(LabelComponent.class).setText(string);
                root.getChild("charfourlbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;*/
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p2platone").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p2platone").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).width * .5f;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p2platone").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).height;

                root.getChild("fourthcharpart").getEntity()
                  .getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("fourthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstartfour = true;
                break;
              case 4:
                /*root.getChild("charfivelbl").getEntity().getComponent(LabelComponent.class).setText(string);
                root.getChild("charfivelbl").getEntity().getComponent(TintComponent.class).color = Color.GRAY;*/
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p2plattwo").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p2plattwo").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).width * .5f;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p2plattwo").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).height;

                root.getChild("fifthcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("fifthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();
                partstartfive = true;
                break;
              case 5:
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TintComponent.class).color = Color.GRAY;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).x =
                  root.getChild("p2platthree").getEntity().getComponent(TransformComponent.class).x +
                    root.getChild("p2platthree").getEntity().getComponent(DimensionsComponent.class).width * .5f -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).width * .5f;
                root.getChild(charsselected.get(i) + right).getEntity().getComponent(TransformComponent.class).y =
                  root.getChild("p2platthree").getEntity().getComponent(TransformComponent.class).y -
                    root.getChild(charsselected.get(i) + right).getEntity().getComponent(DimensionsComponent.class).height;
                /* root.getChild("charsixlbl").getEntity().getComponent(LabelComponent.class).setText(string);*/
                root.getChild("sixthcharpart").getEntity().getComponent(SPFZParticleComponent.class).worldMultiplyer = 1f;
                root.getChild("sixthcharpart").getEntity().getComponent(SPFZParticleComponent.class).startEffect();

               /* root.getChild("charfourlbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                root.getChild("charfivelbl").getEntity().getComponent(TintComponent.class).color = Color.WHITE;*/
                for (int j = 3; j <= 5; j++)
                {
                  root.getChild(charsselected.get(j) + right).getEntity().getComponent(TintComponent.class).color = Color.WHITE;
                }
                //root.getChild("cpulbl").getEntity().getComponent(TintComponent.class).color = Color.BLUE;
                root.getChild("cpupng").getEntity().getComponent(TintComponent.class).color = Color.BLUE;
                partstartsix = true;
                break;
              default:
                break;

            }

            i = 6;

            animsel(root.getChild("charobject").getChild(button).getEntity());

          }
        }
      }
    }
  }

  public void seteofbtns()
  {
    pauseroot.getChild("endoffightmenu").getChild("pabtn").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void clicked()
        {
          restartmatch();
        }
      });
    pauseroot.getChild("endoffightmenu").getChild("csbtn").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void clicked()
        {
          toCharSel();
        }
      });

    pauseroot.getChild("endoffightmenu").getChild("mmbtn").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void touchUp()
        {

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void clicked()
        {
          toMenu();
        }
      });

  }

  public void setMainMenu(int w, int h)
  {
    istraining = false;
    optionsup = false;
    dialog = false;
    exit = false;
    transition = false;
    flingup = false;
    flingdown = false;
    credittime = System.currentTimeMillis();
    adtime = System.currentTimeMillis();
    sectime = System.currentTimeMillis();
    // When coming back to the main menu, unlock the orientation
    if (runningby == ANDROID)
    {
      android.lockOrientation(false, view);
    }
    // setup the buttons for the portrait view
    if (h > w)
    {
      setupportrait();
    }
    // Setup the buttons for the landscape view
    else
    {
      setuplandscape();
      //im.removeProcessor(gd);
      //im.addProcessor(gd);
    }

  }

  public void setSettings()
  {
    readOut("spfzfile");
    mainmenu.setVolume(soundamount);
    adjustBrightness(brightamount);
  }

  public void setsliders(String composite)
  {

    Vector2 dimwhs = new Vector2();
    Vector2 dimwh = new Vector2();
    Vector3 transpar = new Vector3(0, 0, 0);


    TransformComponent transcomponent = ComponentRetriever
      .get(root.getChild(composite).getChild("brightslider").getEntity(), TransformComponent.class);
    DimensionsComponent dimcomponent = ComponentRetriever
      .get(root.getChild(composite).getChild("brightslider").getEntity(), DimensionsComponent.class);
    TransformComponent transcomp = ComponentRetriever.get(root.getChild(composite).getChild("brightbar").getEntity(),
      TransformComponent.class);
    DimensionsComponent dimcompon = ComponentRetriever.get(root.getChild(composite).getChild("brightbar").getEntity(),
      DimensionsComponent.class);

    //transcomponent brightslider transformcomponent
    //transpar.x = ComponentRetriever.get(root.getChild(composite).getEntity(), TransformComponent.class).x;
    /*transpar.x = ComponentRetriever.get(root.getChild(composite).getEntity(), TransformComponent.class).x *
      ComponentRetriever.get(root.getChild(composite).getEntity(), TransformComponent.class).scaleX;*/

    //dimcomponent brightslider dimensionscomponent
    dimwh.x = dimcomponent.width * transcomponent.scaleX;
    dimwh.y = dimcomponent.height * transcomponent.scaleY;

    //dimcompon brightbar dimensionscomponent, transcomp brightbar transformcomponent
    dimwhs.x = dimcompon.width * transcomp.scaleX;
    dimwhs.y = dimcompon.height * transcomp.scaleY;

    final short MAX_VOL = 1;
    final short MAX_BRIGHT = 255;

    if (brightamount > MAX_BRIGHT)
    {
      brightamount = MAX_BRIGHT;
    }
    else if (brightamount < 0)
    {
      brightamount = 1;
    }

    if (soundamount > MAX_VOL)
    {
      soundamount = MAX_VOL;
    }
    else if (soundamount < 0.1)
    {
      soundamount = 0.1f;
    }

    float barX = transcomp.x * transcomp.scaleX;
    float amtPercent = brightamount / MAX_BRIGHT;
    float halfSlide = dimwh.x * .5f;
    float fullbarpercent;

    // set bright
    // slider---------------------------------------------------------------

    // bar full percentage
    fullbarpercent = dimwhs.x;

    // brightamount is received from the flavor in prefs, convert it to percentage of brightamount to Max brightness
    //brightamount = 100 * amtPercent;

    //X VALUE goes in this order of operation
    //set the X value to the beginning of the bar and centers the slider
    //adds the brightness "percentage" to the x Value in order to appropriately position the slider


    transcomponent.x = (barX - halfSlide) + (amtPercent * fullbarpercent);/* - transpar.x*/
    ;

    //convert brightamount back to stored value
    //brightamount = amtPercent * (MAX_BRIGHT * .01f);
    // set sound
    // slider----------------------------------------------------------------
    //transcomponent = ComponentRetriever.get(root.getChild(composite).getEntity(), TransformComponent.class);


    //transpar.x = transcomponent.x * transcomponent.scaleX;
    //transpar.y = transcomponent.y * transcomponent.scaleY;

    transcomponent = ComponentRetriever.get(root.getChild(composite).getChild("soundslider").getEntity(),
      TransformComponent.class);
    dimcomponent = ComponentRetriever.get(root.getChild(composite).getChild("soundslider").getEntity(),
      DimensionsComponent.class);
    transcomp = ComponentRetriever.get(root.getChild(composite).getChild("soundbar").getEntity(),
      TransformComponent.class);
    dimcompon = ComponentRetriever.get(root.getChild(composite).getChild("soundbar").getEntity(),
      DimensionsComponent.class);

    dimwh.x = dimcomponent.width * transcomponent.scaleX;
    dimwh.y = dimcomponent.height * transcomponent.scaleY;
    dimwhs.x = dimcompon.width * transcomp.scaleX;
    dimwhs.y = dimcompon.height * transcomp.scaleY;

    barX = transcomp.x * transcomp.scaleX - transpar.x;
    amtPercent = soundamount / MAX_VOL;
    halfSlide = dimwh.x * .5f;

    // bar full percentageU
    fullbarpercent = dimwhs.x;

    //soundamount = 50;
    // soundamount is received from the file - see readFile(String File)
    //soundamount = 100 * amtPercent;

    //get the x value of the slider and subtract half of the slider(to center the slider)
    //add the "percentage" amount of brightamount and multiply it by the fullbarpercent value(full length of the bar)
    transcomponent.x = (barX - halfSlide) + (amtPercent * fullbarpercent);

    soundamount = soundamount * (MAX_VOL * .01f);

    transcomponent = null;
    dimcomponent = null;
    transcomp = null;
    dimcompon = null;
    dimwh = null;
    dimwhs = null;
    transpar = null;
  }

  // ------------- END OF CODE BEFORE GESTURE DETECTOR METHODS
  // ----------------------- //[

  /**
   * Method sets up landscape main menu buttons
   */
  public void setuplandscape()
  {
    //final float RIFT = 14.5f;

    String[] cdtcomponents = {"ttcimage", "swypefrmbtm", "swypefrmtop"};
    // lighttranscomp =
    // root.getChild("mainlight").getEntity().getComponent(TransformComponent.class);
    fader = root.getChild("transition").getEntity();
    Preferences spfzpref = Gdx.app.getPreferences("spfzfile");
    if (spfzpref.getBoolean("initialize") == false)
    {
      writeOut("1.0\n255");
      setrdtime(99);
    }
    setSettings();
    if (!gamestart && INTRO >= 1)
    {
      Actions.addAction(fader, Actions.fadeOut(0f));
    }
    credittime = System.currentTimeMillis();

    view = "landscape";

    transform = tc.get(root.getEntity());
    action = ac.get(root.getEntity());

    // Intro Animation only starts up once
    if (INTRO == 0)
    {
      Intro();
    }

    else
    {
      INTRO = 2;

      // After intro animation, setup the menu as usual
      if (INTRO >= 2 && uicomplete == true)
      {

        if (!gamestart)
        {
          for (int i = 0; i < cdtcomponents.length; i++)
          {
            Actions.addAction(root.getChild(cdtcomponents[i]).getEntity(), Actions.fadeOut(0f));
          }

          update(view).getEngine().removeEntity(root.getChild("animscn").getEntity());
          update(view).getEngine().removeEntity(root.getChild("lintro").getEntity());

          Actions.addAction(root.getChild("ttcimage").getEntity(),
            Actions.sequence(Actions.delay(.5f), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
              Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
              Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(1f * APP_SPD)));
        }
        // remove the touch to continue entity once the user has pressed
        // the
        // screen and
        // position the music to the correct position a.k.a the rift
        // position
        if (INTRO == 2 && Gdx.input.isTouched() || gamestart)
        {
          if(mainmenu.getPosition() < BEAT_DROP)
          {
            mainmenu.setPosition(BEAT_DROP);
            mainmenu.play();
          }
          gamestart = true;

          Actions.addAction(root.getChild("ttcimage").getEntity(), Actions.fadeOut(0f));

          update(view).getEngine().removeEntity(root.getChild("ttcimage").getEntity());


          // If this boolean is set, setup the main menu.
          if (uicomplete == true)
          {
            if (!dialog)
            {

              update(view).addComponentsByTagName("button", ButtonComponent.class);

              // animate the landscape scene
              animateland();

              root.getChild("larcbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("larcbutton", false, null))
                    {
                      // WE DO NOT PROCESS THE BUTTON
                    }
                    else
                    {

                      boolean testVersion = true;

                      if (!testVersion)
                      {
                        if (concheck(curScene))
                        {

                          if (!flingup && !flingdown && !transition)
                          {
                            portbtns.play(1.0f);
                            transition = true;

                            closebtns(view);

                            Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD),
                              Actions.run(new Runnable()
                                          {

                                            @Override
                                            public void run()
                                            {
                                              isArcade = true;
                                              mode = true;

                                              if (runningby == ANDROID)
                                              {
                                                android.lockOrientation(true, view);

                                              }

                                              curScene = "arcadeselscn";
                                              // in testing, re-initializing the SceneLoader
                                              // made
                                              // this work when switching between landscape
                                              // scenes.
                                              land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");

                                              update(view).loadScene(curScene, viewportland);

                                              inMode();
                                            }
                                          }
                              )));
                          }
                        }
                      }
                      else
                      {
                        if (runningby == ANDROID)
                        {
                          android.toast();
                        }
                        else
                        {
                          System.out.println("Arcade version is not available at this time.");
                        }
                      }
                    }

                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("lvsbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {

                    if (draggedfrmbtn("lvsbutton", false, null))
                    {
                      // WE DO NOT PROCESS THE BUTTON
                    }
                    else
                    {
                      if (concheck(curScene))
                      {
                        if (!flingup && !flingdown && !transition)
                        {
                          portbtns.play(1.0f);
                          closebtns(view);
                          transition = true;
                          Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                          {

                            @Override
                            public void run()
                            {
                              // rm.initselect();
                              isArcade = false;
                              mode = true;

                              if (runningby == ANDROID)
                              {
                                android.lockOrientation(true, view);

                              }

                              curScene = "charselscene";
                              prevScene = "charselscene";
                              selecttype = 0;

                              land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");

                              // rm.unloadGame();
                              // rm.initsix();
                              // rm.loadSpriteAnimations();

                              update(view).loadScene(curScene, viewportland);
                              inMode();
                            }
                          })));

                        }
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("ltrnbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("ltrnbutton", false, null))
                    {
                      // DO NOT PROCESS BUTTON
                    }
                    else
                    {
                      if (concheck(curScene))
                      {


                        if (!flingup && !flingdown && !transition)
                        {
                          portbtns.play(1.0f);
                          closebtns(view);
                          transition = true;
                          Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                          {

                            @Override
                            public void run()
                            {
                              istraining = true;
                              isArcade = false;
                              mode = true;

                              root.getEntity().removeAll();

                              if (runningby == ANDROID)
                              {
                                android.lockOrientation(true, view);

                              }
                              curScene = "charselscene";
                              prevScene = "charselscene";
                              selecttype = 1;
                              // rm.unloadGame();
                              // rm.initsix();
                              // rm.loadSpriteAnimations();
                              land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");
                              update(view).loadScene(curScene, viewportland);
                              transition = true;
                              inMode();
                            }
                          })));
                        }
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("lhlpbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("lhlpbutton", false, null))
                    {
                      // WE DO NOT PROCESS THE BUTTON
                    }
                    else
                    {

                      if (!flingup && !flingdown && !transition)
                      {
                        if (runningby == ANDROID)
                        {
                          /*try
                          {
                            String website = "https://www.youtube.com/channel/UCEtdag60UhFwMISKpPZjG2w";
                            Method link = AndroidInterfaceLIBGDX.class.getDeclaredMethod("url", String.class);
                            link.setAccessible(true);
                            link.invoke(android, website);
                          }
                          catch (NoSuchMethodException e)
                          {
                            e.printStackTrace();
                          }
                          catch (IllegalArgumentException e)
                          {
                            e.printStackTrace();
                          }
                          catch (IllegalAccessException e)
                          {
                            e.printStackTrace();
                          }
                          catch (InvocationTargetException e)
                          {
                            e.printStackTrace();
                          }
                        }*/
                        }
                      }

                      //buttonsapt();
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("loptbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    String FILE_NAME = "spfzfile";
                    if (draggedfrmbtn("loptbutton", false, null))
                    {

                    }
                    else
                    {
                      if (!flingup && !flingdown && !transition)
                      {
                        // set buttons apart for exit menu
                        buttonsdown();

                        optionsup = true;

                        if (runningby == ANDROID)
                        {
                          android.lockOrientation(true, view);
                        }

                        readOut(FILE_NAME);
                        tmpsound = soundamount;
                        tmpbright = brightamount;


                        setsliders("optdialog");
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("optdialog").getChild("optback").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    // if (draggedfrmbtn("optback", true, "optdialog"))
                    // {

                    // }
                    // else
                    // {

                    // needs a better way of checking to ensure back
                    // button is pressed once

                    if (root.getChild("optdialog").getEntity().getComponent(TransformComponent.class).y <= 50f)
                    {
                      // move buttons back in place
                      buttonsup();

                      optionsup = false;
                      if (runningby == ANDROID)
                      {
                        android.lockOrientation(false, view);
                      }
                      if (tmpsound != soundamount || tmpbright != brightamount)
                      {
                        writeOut(Float.toString(soundamount) + "\n" + Float.toString(brightamount));
                      }
                      else
                      {
                        soundamount = tmpsound;
                        brightamount = tmpbright;
                      }

                    }

                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("brightbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("brightbutton", false, null))
                    {
                      // DO NOT PROCESS BUTTON
                    }
                    else
                    {
                      if (!flingup && !flingdown && !transition)
                      {
                        //if (runningby == ANDROID)
                        // {

                        brightamount += 51;
                        if (brightamount >= 255)
                        {
                          brightamount = 51;
                        }
                        else if (brightamount < 51)
                        {
                          brightamount = 51;
                        }
                        writeOut(Float.toString(soundamount) + "\n" + Float.toString(brightamount));
                        adjustBrightness(brightamount);

                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("soundbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("soundbutton", false, null))
                    {
                      // DO NOT PROCESS BUTTON
                    }
                    else
                    {
                      if (!flingup && !flingdown && !transition)
                      {
                        if (mute)
                        {
                          mainmenu.setVolume(0f);
                          //if (runningby == ANDROID)
                          //
                          writeOut(Float.toString(mainmenu.getVolume()) + "\n" + Float.toString(brightamount));

                          // }
                          mute = false;
                        }
                        else
                        {
                          mainmenu.setVolume(1f);
                          //if (runningby == ANDROID)
                          //{
                          writeOut(Float.toString(mainmenu.getVolume()) + "\n" + Float.toString(brightamount));
                          //}
                          mute = true;
                        }
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("exitbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("exitbutton", false, null))
                    {
                      // DO NOT PROCESS BUTTON
                    }
                    else
                    {
                      if (!flingup && !flingdown && !transition)
                      {
                        // Reset the phone settings back to what they were
                        // before
                        // The Application was started
                        dialog = true;
                        if (!exit)
                        {
                          buttonsdown();
                          Actions.addAction(root.getChild("pods").getEntity(),
                            Actions.sequence(
                              Actions.parallel(Actions.scaleTo(SCALE_UP * 15f, SCALE_UP * 15f, OPT_TIME),
                                Actions.moveTo(-3800f, -3000f, OPT_TIME)),
                              Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_DOWN, .0001f),
                                Actions.moveTo(320f, 200f, .0001f))));
                        }
                        exit = true;
                        // exit code
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("optdialog").getChild("thirtytime").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  public void clicked()
                  {
                    // if (draggedfrmbtn("thirtytime ", true, "optdialog"))
                    // {
                    // DO NOT PROCESS BUTTON
                    // }
                    // else
                    // {

                    // Reset the phone settings back to what they were
                    // before
                    // The Application was started

                    //stageTime = 11;
                    setrdtime(11);
                    //}
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("optdialog").getChild("sixtytime").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  public void clicked()
                  {
                    // if (draggedfrmbtn("sixtytime ", true, "optdialog"))
                    // {
                    // DO NOT PROCESS BUTTON
                    // }
                    // else
                    // {

                    // Reset the phone settings back to what they were
                    // before
                    // The Application was started

                    //stageTime = 60;
                    setrdtime(60);
                    // }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("optdialog").getChild("ninetytime").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  public void clicked()
                  {
                    // if (draggedfrmbtn("ninetytime ", true, "optdialog"))
                    // {
                    // DO NOT PROCESS BUTTON
                    // else
                    // {

                    // Reset the phone settings back to what they were
                    // before
                    // The Application was started

                    //stageTime = 99;
                    setrdtime(99);
                    // }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("optdialog").getChild("brightslider").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  public void clicked()
                  {

                  }

                  @Override
                  public void touchDown()
                  {
                    // TransformComponent transcomponent =
                    // ComponentRetriever.get(root.getChild("optdialog").getChild("brightslider").getEntity(),
                    // TransformComponent.class);
                    // DimensionsComponent dimcomponent =
                    // ComponentRetriever.get(root.getChild("optdialog").getChild("brightslider").getEntity(),
                    // DimensionsComponent.class);
                    // TransformComponent transcomp =
                    // ComponentRetriever.get(root.getChild("optdialog").getChild("brightbar").getEntity(),
                    // TransformComponent.class);
                    // DimensionsComponent dimcompon =
                    // ComponentRetriever.get(root.getChild("optdialog").getChild("brightbar").getEntity(),DimensionsComponent.class);

                    adjustbright = true;


                  }

                  @Override
                  public void touchUp()
                  {

                    // adjustbright = false;
                  }
                });
              root.getChild("optdialog").getChild("soundslider").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  public void clicked()
                  {

                  }

                  @Override
                  public void touchDown()
                  {

                    adjustsound = true;

                  }

                  @Override
                  public void touchUp()
                  {

                    // adjustsound = false;
                  }
                });

              root.getChild("terrenceconst").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("terrenceconst", false, null))
                    {

                    }
                    else
                    {
                      constel = "terrence";
                      credpress = true;
                      setcred = true;
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("treyconst").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("treyconst", false, null))
                    {

                    }
                    else
                    {
                      constel = "trey";
                      credpress = true;
                      setcred = true;
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("ahmedconst").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("ahmedconst", false, null))
                    {

                    }
                    else
                    {
                      constel = "ahmed";
                      credpress = true;
                      setcred = true;
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("mikloconst").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("mikloconst", false, null))
                    {

                    }
                    else
                    {
                      constel = "miklo";
                      credpress = true;
                      setcred = true;
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });
              root.getChild("poweredbyconst").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("poweredbyconst", false, null))
                    {

                    }
                    else
                    {

                      constel = "poweredby";
                      credpress = true;
                      setcred = true;
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("exitdialog").getChild("yesbutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("yesbutton", true, "exitdialog"))
                    {

                    }
                    else
                    {
                      if (exit)
                      {
                        Gdx.app.exit();
                      }
                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

              root.getChild("exitdialog").getChild("nobutton").getEntity().getComponent(ButtonComponent.class)
                .addListener(new ButtonComponent.ButtonListener()
                {

                  @Override
                  public void clicked()
                  {
                    if (draggedfrmbtn("nobutton", true, "exitdialog"))
                    {

                    }
                    else
                    {
                      buttonsup();
                      Actions.addAction(root.getChild("pods").getEntity(),
                        Actions.parallel(Actions.scaleTo(SCALE_UP, SCALE_UP, OPT_TIME), Actions.moveTo(0, 0, OPT_TIME)));
                      dialog = false;
                      exit = false;

                    }
                  }

                  @Override
                  public void touchDown()
                  {

                  }

                  @Override
                  public void touchUp()
                  {

                  }
                });

            }

          }
        }
      }
    }
  }

  public void unsetpress()
  {
    im.clear();
  }

  /**
   * Method sets up portrait main menu buttons
   */
  public void setupportrait()
  {

    view = "portrait";

    // Retriever the components needed to trigger Actions
    transform = tc.get(root.getEntity());
    action = ac.get(root.getEntity());

    fader = root.getChild("transition").getEntity();

    Preferences spfzpref = Gdx.app.getPreferences("spfzfile");
    if (spfzpref.getBoolean("initialize") == false)
    {
      writeOut("1.0\n255");
      setrdtime(99);
    }
    setSettings();

    Actions.addAction(root.getChild("tocreditsone").getEntity(), Actions.fadeOut(0f));
    Actions.addAction(root.getChild("tocreditstwo").getEntity(), Actions.fadeOut(0f));
    if (!gamestart && INTRO >= 1)
    {
      Actions.addAction(fader, Actions.fadeOut(0f));
    }
    // Intro Animation only starts up once
    if (INTRO == 0)
    {
      Intro();
    }
    else
    {
      INTRO = 2;

      // After intro animation, setup the menu as usual
      if (INTRO >= 2 && uicomplete == true)
      {

        if (!gamestart)
        {
          Actions.addAction(root.getChild("ttcimage").getEntity(), Actions.fadeOut(0f));
          update(view).getEngine().removeEntity(root.getChild("animcircle").getEntity());
          update(view).getEngine().removeEntity(root.getChild("introcircle").getEntity());

          Actions.addAction(root.getChild("ttcimage").getEntity(),
            Actions.sequence(Actions.delay(.5f), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
              Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD), Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(.4f * APP_SPD),
              Actions.fadeOut(.4f * APP_SPD), Actions.fadeIn(1f * APP_SPD)));
        }

        // remove the touch to continue entity once the user has pressed
        // the
        // screen and
        // position the music to the correct position a.k.a the rift
        // position.

        if (INTRO == 2 && Gdx.input.isTouched() || gamestart)
        {
          gamestart = true;
          mainmenu.setPosition(BEAT_DROP);
          mainmenu.play();
          if (scenesel == 1 || curScene == "landscene")
          {
            Actions.addAction(root.getChild("ttcimage").getEntity(), Actions.fadeOut(0f));

          }

          update(view).getEngine().removeEntity(root.getChild("ttcimage").getEntity());


          // If this boolean is set, setup the main menu.
          if (uicomplete == true)
          {
            if (!dialog)
            {
              animatemainmenu(view);
            }

            // buttons must be added by tag name before assigning
            // listener
            // actions

            update(view).addComponentsByTagName("button", ButtonComponent.class);

            root.getChild("arcbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("arcbutton", false, null))
                  {

                  }
                  else
                  {
                    boolean testVersion = true;

                    if (!testVersion)
                    {
                      if (concheck(curScene))
                      {
                        closebtns(view);
                        curScene = "arcadeselscn";
                        prevScene = "arcadeselscn";
                        isArcade = true;
                        mode = true;
                        portbtns.play(1.0f);
                        Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                        {

                          @Override
                          public void run()
                          {
                            view = "landscape";
                            selecttype = 0;

                            if (runningby == ANDROID)
                            {
                              android.lockOrientation(true, view);
                            }

                          }
                        })));


                      }

                    }
                    else
                    {
                      if (runningby == ANDROID)
                      {
                        android.toast();
                      }
                      else
                      {
                        System.out.println("Arcade version is not available at this time.");
                      }

                    }

                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("vsbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {

                  if (draggedfrmbtn("vsbutton", false, null))
                  {

                  }
                  else
                  {
                    if (concheck(curScene))
                    {
                      isArcade = false;
                      mode = true;
                      portbtns.play(1.0f);
                      closebtns(view);
                      Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                      {

                        @Override
                        public void run()
                        {
                          view = "landscape";
                          curScene = "charselscene";
                          prevScene = "charselscene";
                          selecttype = 0;
                          // rm.unloadGame();
                          // rm.initsix();
                          if (runningby == ANDROID)
                          {
                            android.lockOrientation(true, view);
                          }

                        }
                      })));

                    }

                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("trnbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()

              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("trnbutton", false, null))
                  {

                  }
                  else
                  {
                    if (concheck(curScene))
                    {
                      isArcade = false;
                      mode = true;
                      portbtns.play(1.0f);
                      closebtns(view);
                      Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
                      {

                        @Override
                        public void run()
                        {
                          view = "landscape";
                          istraining = true;
                          curScene = "charselscene";
                          prevScene = "charselscene";
                          selecttype = 1;
                          // rm.unloadGame();
                          // rm.initsix();

                          if (runningby == ANDROID)
                          {
                            android.lockOrientation(true, view);
                          }

                        }
                      })));
                    }
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }

              });

            root.getChild("helpbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("helpbutton", false, null))
                  {

                  }
                  else
                  {
                    /*if (runningby == ANDROID)
                    {
                      try
                      {
                        String website = "https://www.youtube.com/channel/UCEtdag60UhFwMISKpPZjG2w";
                        Method link = AndroidInterfaceLIBGDX.class.getDeclaredMethod("url", String.class);
                        link.setAccessible(true);
                        link.invoke(android, website);
                      }
                      catch (NoSuchMethodException e)
                      {
                        e.printStackTrace();
                      }
                catch (IllegalArgumentException e)
                      {
                        e.printStackTrace();
                      }
                catch (IllegalAccessException e)
                      {
                        e.printStackTrace();
                      }
                catch (InvocationTargetException e)
                      {
                        e.printStackTrace();
                      }
                    }
*/
                    /*optionsup = true;
                    buttonsup();

                    Actions.addAction(root.getChild("mnuscnbutton").getEntity(),
                      Actions.sequence(Actions.delay(1.5f), Actions.parallel(Actions.scaleBy(2.5551f, 0, OPT_TIME),
                        Actions.moveBy(-113f, 0f, OPT_TIME, Interpolation.linear))));
                    Actions.addAction(root.getChild("ingamebutton").getEntity(),
                      Actions.sequence(Actions.delay(1.5f), Actions.parallel(Actions.scaleBy(2.5551f, 0, OPT_TIME),
                        Actions.moveBy(-119f, 0f, OPT_TIME, Interpolation.linear))));

                    Actions.addAction(root.getChild("hlpbackbutton").getEntity(),
                      Actions.sequence(Actions.delay(1.5f), Actions.parallel(Actions.scaleBy(.5f, 0f, OPT_TIME),
                        Actions.moveBy(-29f, 0f, OPT_TIME, Interpolation.linear))));*/
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("optbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  String FILE_NAME = "spfzfile";

                  if (draggedfrmbtn("optbutton", false, null))
                  {

                  }
                  else
                  {
                    if (runningby == ANDROID)
                    {

                    }
                    optionsup = true;
                    portbtns.play(1.0f);
                    buttonsup();

                    Actions.addAction(root.getChild("optionscreen").getEntity(),
                      Actions.sequence(Actions.delay(1.5f),
                        Actions.parallel(Actions.scaleTo(SCALE_UP, SCALE_UP, OPT_TIME),
                          Actions.moveBy(-143f, 0f, OPT_TIME, Interpolation.linear))));

                    if (runningby == ANDROID)
                    {
                      android.lockOrientation(true, view);
                    }

                    readOut(FILE_NAME);

                    tmpsound = soundamount;
                    tmpbright = brightamount;

                    setsliders("optionscreen");
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });
            root.getChild("mnuscnbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("mnuscnbutton", false, null))
                  {

                  }
                  else
                  {
                    if (runningby == ANDROID)
                    {

                    }
                    inhelp = true;
                    mnuscn = true;
                    portbtns.play(1.0f);

                    Actions.addAction(root.getChild("mnuscnbutton").getEntity(),
                      Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                        Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                          Actions.moveBy(113f, 0f, OPT_TIME / 6, Interpolation.linear)))));

                    Actions.addAction(root.getChild("ingamebutton").getEntity(),
                      Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                        Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                          Actions.moveBy(119f, 0f, OPT_TIME / 6, Interpolation.linear)))));

                    Actions.addAction(root.getChild("charselhelp").getEntity(), Actions.sequence(Actions.delay(.5f),
                      Actions.parallel(Actions.scaleBy(3.3f, 0f, OPT_TIME / 3))));
                    Actions.addAction(root.getChild("pausehelp").getEntity(), Actions.sequence(Actions.delay(.5f),
                      Actions.parallel(Actions.scaleBy(3.3f, 0f, OPT_TIME / 3))));

                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("ingamebutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("ingamebutton", false, null))
                  {

                  }
                  else
                  {
                    if (runningby == ANDROID)
                    {

                    }
                    inhelp = true;
                    mnuscn = false;
                    portbtns.play(1.0f);

                    Actions.addAction(root.getChild("mnuscnbutton").getEntity(),
                      Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                        Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                          Actions.moveBy(113f, 0f, OPT_TIME / 6, Interpolation.linear)))));

                    Actions.addAction(root.getChild("ingamebutton").getEntity(),
                      Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                        Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                          Actions.moveBy(119f, 0f, OPT_TIME / 6, Interpolation.linear)))));
                    Actions.addAction(root.getChild("ingamehelp").getEntity(), Actions.sequence(Actions.delay(.5f),
                      Actions.parallel(Actions.scaleBy(1.8f, 0f, OPT_TIME / 3))));
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("hlpbackbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("hlpbackbutton", false, null))
                  {

                  }
                  else
                  {
                    if (runningby == ANDROID)
                    {

                    }

                    if (inhelp)
                    {
                      Actions.addAction(root.getChild("mnuscnbutton").getEntity(),
                        Actions.sequence(Actions.delay(.5f),
                          Actions.parallel(Actions.scaleBy(2.5551f, 0, OPT_TIME / 8),
                            Actions.moveBy(-113f, 0f, OPT_TIME / 8, Interpolation.linear))));
                      Actions.addAction(root.getChild("ingamebutton").getEntity(),
                        Actions.sequence(Actions.delay(.5f),
                          Actions.parallel(Actions.scaleBy(2.5551f, 0, OPT_TIME / 8),
                            Actions.moveBy(-119f, 0f, OPT_TIME / 8, Interpolation.linear))));

                      if (mnuscn)
                      {
                        Actions.addAction(root.getChild("charselhelp").getEntity(),
                          Actions.sequence(Actions.parallel(Actions.scaleBy(-3.3f, 0f, OPT_TIME / 3))));
                        Actions.addAction(root.getChild("pausehelp").getEntity(),
                          Actions.sequence(Actions.parallel(Actions.scaleBy(-3.3f, 0f, OPT_TIME / 3))));
                      }
                      else
                      {
                        Actions.addAction(root.getChild("ingamehelp").getEntity(),
                          Actions.sequence(Actions.parallel(Actions.scaleBy(-1.8f, 0f, OPT_TIME / 3))));
                      }
                      inhelp = false;
                    }
                    else
                    {
                      Actions.addAction(root.getChild("mnuscnbutton").getEntity(),
                        Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                          Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                            Actions.moveBy(113f, 0f, OPT_TIME / 6, Interpolation.linear)))));

                      Actions.addAction(root.getChild("ingamebutton").getEntity(),
                        Actions.sequence(Actions.parallel(Actions.scaleBy(.5f, 2f, OPT_TIME / 8),
                          Actions.parallel(Actions.scaleBy(-3.0551f, -2f, OPT_TIME / 6),
                            Actions.moveBy(119f, 0f, OPT_TIME / 6, Interpolation.linear)))));

                      Actions.addAction(root.getChild("hlpbackbutton").getEntity(),
                        Actions.sequence(Actions.parallel(Actions.scaleBy(-.5f, 0f, OPT_TIME / 8),
                          Actions.moveBy(29f, 0f, OPT_TIME / 8, Interpolation.linear))));

                      buttonsdown();
                      optionsup = false;
                    }

                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("optionscreen").getChild("thirtytime").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                public void clicked()
                {
                  // if (draggedfrmbtn("thirtytime ", true, "optdialog"))
                  // {
                  // DO NOT PROCESS BUTTON
                  // }
                  // else
                  // {

                  // Reset the phone settings back to what they were
                  // before
                  // The Application was started

                  //stageTime = 11;
                  setrdtime(11);
                  // }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("optionscreen").getChild("sixtytime").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                public void clicked()
                {
                  // if (draggedfrmbtn("sixtytime ", true, "optdialog"))
                  // {
                  // DO NOT PROCESS BUTTON
                  // }
                  // else
                  // {

                  // Reset the phone settings back to what they were
                  // before
                  // The Application was started

                  //stageTime = 60;
                  setrdtime(60);
                  // }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("optionscreen").getChild("ninetytime").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                public void clicked()
                {
                  // if (draggedfrmbtn("ninetytime ", true, "optdialog"))
                  // {
                  // DO NOT PROCESS BUTTON
                  // else
                  // {

                  // Reset the phone settings back to what they were
                  // before
                  // The Application was started

                  //stageTime = 99;
                  setrdtime(99);
                  // }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("optionscreen").getChild("brightslider").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                public void clicked()
                {

                }

                @Override
                public void touchDown()
                {
                  // TransformComponent transcomponent;

                  adjustbright = true;

                  // transcomponent =
                  // ComponentRetriever.get(root.getChild("optionscreen").getEntity(),
                  // TransformComponent.class);

                  // transpar.x = transcomponent.x;
                  // transpar.y = transcomponent.y;

                }

                @Override
                public void touchUp()
                {

                  // adjustbright = false;
                }
              });

            root.getChild("optionscreen").getChild("soundslider").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                public void clicked()
                {

                }

                @Override
                public void touchDown()
                {

                  adjustsound = true;


                }

                @Override
                public void touchUp()
                {

                  // adjustbright = false;
                }
              });

            root.getChild("optionscreen").getChild("optback").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  // if (draggedfrmbtn("optback", true, "optdialog"))
                  // {

                  // }
                  // else
                  // {

                  // needs a better way of checking to ensure back
                  // button is pressed once

                  if (root.getChild("optionscreen").getEntity().getComponent(TransformComponent.class).x <= 60f)
                  {

                    Actions.addAction(root.getChild("optionscreen").getEntity(),
                      Actions.parallel(Actions.scaleTo(SCALE_DOWN, SCALE_UP, OPT_TIME),
                        Actions.moveBy(143f, 0f, OPT_TIME, Interpolation.linear)));

                    buttonsdown();

                    optionsup = false;

                    if (runningby == ANDROID)
                    {
                      android.lockOrientation(false, view);
                    }
                    if (tmpsound != soundamount || tmpbright != brightamount)
                    {
                      writeOut(Float.toString(soundamount) + "\n" + Float.toString(brightamount));
                    }
                    else
                    {
                      soundamount = tmpsound;
                      brightamount = tmpbright;
                    }


                  }

                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("controlboard").getChild("brightnessbtn").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("brightnessbtn", true, "controlboard"))
                  {

                  }
                  else
                  {

                    //if (runningby == ANDROID)
                    //{
                    brightamount += 51;
                    if (brightamount >= 255)
                    {
                      brightamount = 51;
                    }
                    else if (brightamount < 51)
                    {
                      brightamount = 51;
                    }


                    writeOut(Float.toString(soundamount) + "\n" + Float.toString(brightamount));
                    adjustBrightness(brightamount);
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("controlboard").getChild("soundbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {

                  if (mute)
                  {
                    mainmenu.setVolume(0f);
                    mute = false;
                  }
                  else
                  {
                    mainmenu.setVolume(1f);
                    mute = true;
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("controlboard").getChild("exitbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("exitbutton", true, "controlboard"))
                  {

                  }
                  else
                  {
                    dialog = true;
                    if (!exit)
                    {
                      buttonsup();
                    }
                    exit = true;
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("exitdialog").getChild("yesbutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("yesbutton", true, "exitdialog"))
                  {

                  }
                  else
                  {
                    if (exit)
                    {
                      Gdx.app.exit();
                    }
                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });

            root.getChild("exitdialog").getChild("nobutton").getEntity().getComponent(ButtonComponent.class)
              .addListener(new ButtonComponent.ButtonListener()
              {

                @Override
                public void clicked()
                {
                  if (draggedfrmbtn("nobutton", true, "exitdialog"))
                  {

                  }
                  else
                  {
                    dialog = false;
                    buttonsdown();
                    exit = false;

                  }
                }

                @Override
                public void touchDown()
                {

                }

                @Override
                public void touchUp()
                {

                }
              });
          }
        }
      }
    }
  }

  public void setupstage()
  {
    float texWidth;
    float texHeight;
    short WORLD_WIDTH = 640;
    short WORLD_HEIGHT = 400;
    String stagepath = "stages/" + selectedStage + ".png";
    stoprender = false;
    texWidth = (WORLD_WIDTH * STAGE_MULT) * rm.projectVO.pixelToWorld;
    texHeight = (WORLD_HEIGHT * 2) * rm.projectVO.pixelToWorld;

    pixmap = new Pixmap(Gdx.files.internal(stagepath));

    pixmap2 = new Pixmap((int) texWidth, (int) texHeight, pixmap.getFormat());

    pixmap2.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, pixmap2.getWidth(),
      pixmap2.getHeight());
    stageback = new Texture(pixmap2);

    pixmap2.dispose();
    pixmap.dispose();

    trc.regionName = selectedStage;
    testregion = new TextureRegion(stageback, (int) texWidth, (int) texHeight);

    setupstage = true;
    trc.region = testregion;

    stageimg.layerName = "Default";
    stageimg.zIndex = 0;

    stageimg.scaleX = 1f;
    stageimg.scaleY = 1f;

    // stageimg.x = 200f;
    // stageimg.y = -120f;
    stageimg.x = -240f;
    stageimg.y = 0f;

    update(view).entityFactory.createEntity(root.getEntity(), stageimg);

  }

  public String setworkingres()
  {

    String lResolution;

    if ((gWidth >= 100 && gWidth <= 800) && (gHeight >= 100 && gHeight <= 480))
    {
      lResolution = "smallland";
    }

    else if ((gWidth >= 801 && gWidth <= 1040) && (gHeight >= 481 && gHeight <= 600))
    {
      lResolution = "smalnormland";
    }

    else if ((gWidth >= 801 && gWidth <= 1280) && (gHeight >= 481 && gHeight <= 720))
    {
      lResolution = "normalland";
    }
    else if ((gWidth >= 1281 && gWidth <= 1600) && (gHeight >= 721 && gHeight <= 960))
    {
      lResolution = "largeland";
    }

    else
    {
      lResolution = "orig";
    }

    return lResolution;
  }

  public void ssposition(int player)
  {
    String left = "one";
    String right = "two";
    Entity character = charentities.get(player);

    if (player < 3)
    {

      character.getComponent(TransformComponent.class).x =
        root.getChild(charsselected.get(player) + left).getEntity().getComponent(TransformComponent.class).x +
          (root.getChild(charsselected.get(player) + left).getEntity().getComponent(DimensionsComponent.class).width * .5f) -
          ((character.getComponent(DimensionsComponent.class).width * .5f) * .6f);

      character.getComponent(TransformComponent.class).y =
        root.getChild(charsselected.get(player) + left).getEntity().getComponent(TransformComponent.class).y +
          root.getChild(charsselected.get(player) + left).getEntity().getComponent(DimensionsComponent.class).height * 1.5f;

    }
    else
    {
      setShader("newdef", character);
      character.getComponent(TransformComponent.class).scaleX *= -1f;
      character.getComponent(TransformComponent.class).x =
        root.getChild(charsselected.get(player) + right).getEntity().getComponent(TransformComponent.class).x +
          (root.getChild(charsselected.get(player) + right).getEntity().getComponent(DimensionsComponent.class).width * .5f) +
          ((character.getComponent(DimensionsComponent.class).width * .5f) * .6f);

      character.getComponent(TransformComponent.class).y =
        root.getChild(charsselected.get(player) + right).getEntity().getComponent(TransformComponent.class).y +
          root.getChild(charsselected.get(player) + right).getEntity().getComponent(DimensionsComponent.class).height * 1.5f;
    }
  }

  public void setShader(String shader, Entity obj)
  {
    String shadersPath = "shaders";

    ShaderProgram alt = new ShaderProgram(Gdx.files.internal(shadersPath + File.separator + shader + ".vert"), Gdx.files.internal(shadersPath + File.separator + shader + ".frag"));
    obj.add(new ShaderComponent());
    obj.getComponent(ShaderComponent.class).setShader(shader, alt);
  }

  public void stageprocessing()
  {
    Entity p1swap = root.getChild("p1swap").getEntity();
    Entity swappool1 = root.getChild("p1swap").getChild("swapp1").getEntity();
    Entity hud = root.getChild("ctrlandhud").getEntity();
    Entity control = root.getChild("controller").getEntity();
    float remainderX = control.getComponent(TransformComponent.class).x - hud.getComponent(TransformComponent.class).x;
    float remainderY = control.getComponent(TransformComponent.class).y - hud.getComponent(TransformComponent.class).y;

    short HALF_WORLDH = 200;
    short HALF_WORLDW = 320;

    if (runningby == DESKTOP)
    {
      stage.controls();
    }

    if (swappool1.getComponent(SPFZParticleComponent.class).pooledeffects != null)
    {
      if (swappool1.getComponent(SPFZParticleComponent.class).pooledeffects.size != 0)
      {
        if ((!swappool1.getComponent(SPFZParticleComponent.class).pooledeffects.get(0).isComplete()) && stage.strt1)
        {
          p1swap.getComponent(TransformComponent.class).x = stage.spfzp1move.center();
          p1swap.getComponent(TransformComponent.class).y = stage.spfzp1move.attributes().y + stage.spfzp1move.dimensions().height * .5f;
        }
      }
    }

    stage.lifeandround();

    stage.newcam();

    hud.getComponent(TransformComponent.class).x = viewportland.getCamera().position.x - (HALF_WORLDW);

    hud.getComponent(TransformComponent.class).y = viewportland.getCamera().position.y - (HALF_WORLDH);

    control.getComponent(TransformComponent.class).x = remainderX + hud.getComponent(TransformComponent.class).x;

    control.getComponent(TransformComponent.class).y = remainderY + hud.getComponent(TransformComponent.class).y;

    viewportland.getCamera().update();

    if (stage.arrscripts != null)
    {
      for (int i = 0; i < stage.arrscripts.size(); i++)
      {
        if (i == stage.p1 || i == stage.p2)
        {
          stage.collision(i);
        }
      }
    }

    if (stage.switchp1)
    {
      stage.switchp1();
    }
    if (stage.switchp2)
    {
      stage.switchp2();
    }

  }

  public void readOut(final String file)
  {
    String values;
    String FILE_NAME = "spfzfile";

    Preferences spfzpref = Gdx.app.getPreferences(FILE_NAME);
    values = spfzpref.getString("settings");
    if (values == "")
    {
      values = "0.5\n127";
    }
    savedvals = values.split("\n");

    soundamount = Float.valueOf(savedvals[0]);
    brightamount = Float.valueOf(savedvals[1]);

  }

  public void setrdtime(final int values)
  {
    String FILE_NAME = "spfzfile";

    Preferences spfzpref = Gdx.app.getPreferences(FILE_NAME);
    spfzpref.putInteger("time", values);

    spfzpref.flush();

  }

  public void writeOut(final String values)
  {
    String FILE_NAME = "spfzfile";

    Preferences spfzpref = Gdx.app.getPreferences(FILE_NAME);
    spfzpref.putString("settings", values);

    if (spfzpref.getBoolean("initialize") == false)
    {
      spfzpref.putBoolean("initialize", true);
    }

    spfzpref.flush();

  }

  /**
   * Method contains the stages to select from
   */
  public void stageSel()
  {
    fader = root.getChild("transition").getEntity();
    setSettings();
    Actions.addAction(fader, Actions.sequence(Actions.fadeOut(.3f * APP_SPD)));
    update(view).addComponentsByTagName("button", ButtonComponent.class);

    root.getChild("stageonebutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "halloweenstage";

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagetwobutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "cathedralstage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagethreebutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "clubstage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagefourbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "egyptstage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagefivebutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "futurestage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagesixbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "gargoyle";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stagesevenbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "junglestage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stageeightbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "skullstage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("stageninebutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {

          selectedStage = "undergroundstage";
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("okaybutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          if (selectedStage == null)
          {
            // enter custom toast message here
          }
          else
          {
            Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
            {

              @Override
              public void run()
              {
                ok.play(1.0f);
                createstage();
              }
            })));

          }
        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
    root.getChild("backbutton").getEntity().getComponent(ButtonComponent.class)
      .addListener(new ButtonComponent.ButtonListener()
      {

        @Override
        public void clicked()
        {
          back.play(1.0f);
          Actions.addAction(fader, Actions.sequence(Actions.fadeIn(.3f * APP_SPD), Actions.run(new Runnable()
          {

            @Override
            public void run()
            {
              fromss = true;

              backprocessing();
            }
          })));

        }

        @Override
        public void touchDown()
        {

        }

        @Override
        public void touchUp()
        {

        }
      });
  }

  public void createstage()
  {
    float[] bounds = {80, 560};
    String stagep = "stages/" + selectedStage + ".png";
    stageconfirmed = true;

    loader.load(stagep, Texture.class);
    // need to add "loading finished" logic
    loader.finishLoading();
    // loader.i
    root.getEntity().removeAll();

    land = new SPFZSceneLoader(rm, spfzTrial.this, "", "");

    update(view).loadScene("stagescene", viewportland);
    stagesystem.priority = 0;

    update(view).engine.addSystem(stagesystem);

    // if the stage has been selected and the OK button
    // has been
    // touched, begin process
    // for the controls creation for the fighting scene
    if (selectedStage != null && stageconfirmed)
    {
      characters.set(0, p1char1);
      characters.set(1, p1char2);
      characters.set(2, p1char3);
      characters.set(3, p2char1);
      characters.set(4, p2char2);
      characters.set(5, p2char3);

      // Call the stage to setup controls and pass the
      // camera;
      root = new ItemWrapper(update(view).getRoot());
      transform = tc.get(root.getEntity());
      action = ac.get(root.getEntity());

      setupstage();

      if (stageTime == 0)
      {
        //stageTime = 99;
        Preferences spfzpref = Gdx.app.getPreferences("spfzfile");
        stageTime = spfzpref.getInteger("time");
      }

      stage = new SPFZStage(update(view).getRm(), viewportland.getCamera(), selectedStage, runningby, bounds,
        characters, spfzTrial.this, istraining);
    }
  }

  public void preload()
  {
    selectedStage = "halloweenstage";
    p1char1 = "spriteballred";
    p1char2 = "spriteballblack";
    p1char3 = "spritepurplex";
    p2char1 = "spriteballred";
    p2char2 = "spriteballblack";
    p2char3 = "spritepurplex";

    characters.set(0, p1char1);
    characters.set(1, p1char2);
    characters.set(2, p1char3);
    characters.set(3, p2char1);
    characters.set(4, p2char2);
    characters.set(5, p2char3);
    istraining = true;
  }

  /**
   * method performs action when the user swipes up or down
   */
  public void swipecheck()
  {
    if (!optionsup)
    {
      if (flingup)
      {
        if (viewportland.getCamera().position.y == credits.y)
        {
          flingup = false;
        }

        viewportland.getCamera().position.lerp(credits, 0.2f);

      }
      if (flingdown)
      {
        if (viewportland.getCamera().position.y == tomenu.y)
        {
          flingdown = false;
        }

        viewportland.getCamera().position.lerp(tomenu, 0.2f);
        if (((OrthographicCamera) viewportland.getCamera()).zoom != ZOOMCOUT)
        {
          ((OrthographicCamera) viewportland.getCamera()).zoom = ZOOMCOUT;
        }
      }
    }
  }

  @Override
  public boolean tap(float x, float y, int count, int button)
  {

    return false;
  }

  public void toCharSel()
  {
    // isArcade = false;
    mode = true;

    if (runningby == ANDROID)
    {
      android.lockOrientation(true, view);
    }

    if (isArcade)
    {
      level = 0;
      // selecttype = "Arcade select Screen";
      curScene = "arcadeselscn";
      prevScene = "arcadeselscn";
    }
    else
    {
      // selecttype = "CHARACTER SELECT SCREEN";
      curScene = "charselscene";
      prevScene = "charselscene";

    }
    land = new SPFZSceneLoader(rm, this, "", "");
    // rm.unloadstage();
    // rm.initsix();
    update(view).loadScene(curScene, viewportland);
    resumefrmpause();

    if (selectedStage != null && stageconfirmed)
    {
      stage.dispose();
      stage = null;
    }

    selectedStage = null;
    stageconfirmed = false;

    inMode();
  }

  public void toMenu()
  {
    resumefrmpause();
    backprocessing();
  }

  @Override
  public boolean touchDown(float x, float y, int pointer, int button)
  {

    return false;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button)
  {
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer)
  {
    if (optionsup)
    {
      TransformComponent transcomp = new TransformComponent();
      TransformComponent transcomponent = new TransformComponent();
      DimensionsComponent dimcompon = new DimensionsComponent();
      DimensionsComponent dimcomponent = new DimensionsComponent();
      Vector2 dimwh = new Vector2();
      Vector2 dimwhs = new Vector2();
      /*Vector3 vec3 = new Vector3();*/
      Vector3 transpar = new Vector3(0, 0, 0);
      String option = new String();

      if (view == "portrait")
      {
        option = "optionscreen";
      }
      else
      {
        option = "optdialog";
      }

      if (adjustbright)
      {
        transcomponent = ComponentRetriever.get(root.getChild(option).getChild("brightslider").getEntity(),
          TransformComponent.class);
        dimcomponent = ComponentRetriever.get(root.getChild(option).getChild("brightslider").getEntity(),
          DimensionsComponent.class);
        transcomp = ComponentRetriever.get(root.getChild(option).getChild("brightbar").getEntity(),
          TransformComponent.class);
        dimcompon = ComponentRetriever.get(root.getChild(option).getChild("brightbar").getEntity(),
          DimensionsComponent.class);
      }

      if (adjustsound)
      {
        transcomponent = ComponentRetriever.get(root.getChild(option).getChild("soundslider").getEntity(),
          TransformComponent.class);
        dimcomponent = ComponentRetriever.get(root.getChild(option).getChild("soundslider").getEntity(),
          DimensionsComponent.class);
        transcomp = ComponentRetriever.get(root.getChild(option).getChild("soundbar").getEntity(),
          TransformComponent.class);
        dimcompon = ComponentRetriever.get(root.getChild(option).getChild("soundbar").getEntity(),
          DimensionsComponent.class);
      }

      transpar.x = ComponentRetriever.get(root.getChild(option).getEntity(), TransformComponent.class).x;
      transpar.y = ComponentRetriever.get(root.getChild(option).getEntity(), TransformComponent.class).y;

      int MAX_VOL = 1;
      int MAX_BRIGHT = 255;
      float fullbarpercent;


      if (vec3 == null)
      {
        vec3 = new Vector3(0, 0, 0);
      }

      if (view == "portrait")
      {
        viewportport.getCamera().update();
        vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewportport.unproject(vec3);
      }
      else
      {
        viewportland.getCamera().update();
        vec3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewportland.unproject(vec3);
      }


      if (adjustbright || adjustsound)
      {
        dimwh.x = dimcomponent.width * transcomponent.scaleX;
        dimwh.y = dimcomponent.height * transcomponent.scaleY;
        dimwhs.x = dimcompon.width * transcomp.scaleX;
        dimwhs.y = dimcompon.height * transcomp.scaleY;

        float barX = transcomp.x + transpar.x;
        float halfSlider = dimwh.x * .5f;
        float pointr = vec3.x - halfSlider - transpar.x;
        transcomponent.x = pointr;
        float slider = transcomponent.x + transpar.x;
        float ctrdSlider = slider + halfSlider;
        // bar full percentage
        fullbarpercent = dimwhs.x;


        //if the center of the slider is less than the beginning of the bar
        if (ctrdSlider < barX)
        {
          transcomponent.x = barX - halfSlider - transpar.x;
        }

        // If the center of the slider is greater than the full bar
        else if (ctrdSlider > barX + dimwhs.x)
        {
          transcomponent.x = (barX + dimwhs.x) + halfSlider - (transpar.x * 2);
        }
        // Adjust brightness or sound with the new slider value

        if (adjustbright)
        {
          brightamount = 100 * (ctrdSlider - transcomp.x) / fullbarpercent;
          brightamount = (brightamount * .01f) * MAX_BRIGHT;

          if (brightamount >= 5f && brightamount <= MAX_BRIGHT)
          {
            adjustBrightness(brightamount);
          }
        }

        if (adjustsound)
        {
          soundamount = 100 * (ctrdSlider - transcomp.x) / fullbarpercent;
          soundamount = (soundamount * .01f) * MAX_VOL;

          if (soundamount >= 0f && soundamount <= MAX_VOL)
          {
            mainmenu.setVolume(soundamount);
          }
        }

      }

      transcomponent = null;
      dimcomponent = null;
      transcomp = null;
      dimcompon = null;
      dimwh = null;
      dimwhs = null;
      vec3 = null;
      transpar = null;
    }
    return true;
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button)
  {

    return true;
  }


  /**
   * Function "brightens" or "dims" the in game screen range of (.2 - 1f)
   *
   * @param brightness
   */
  public void adjustBrightness(float brightness)
  {
    Entity bright = root.getChild("brightness").getEntity();
    float MAX = 255f;
    //float a_max = 1f;
    float a_max = .8f;
    float alter_bright;
    float remainder = MAX - brightness;

    alter_bright = (remainder * a_max) / MAX;

    //Do not allow the user to make the screen too dark(altering the opacity of the fader screen(1f == full fader screen
    //being implemented

    if (bright != null)
    {
      bright.getComponent(TintComponent.class).color.a = alter_bright;
    }
  }

  /**
   * method processes user input as well as performs different functionalities
   * instilled
   * within the game. etc. Back functionality, swipe, credits control.
   */
  public void UIProcessing()
  {
    if (optionsup)
    {
      if (!Gdx.input.isTouched())
      {
        adjustbright = false;
        adjustsound = false;
      }
    }

    if (curScene == "landscene") // && Gdx.input.isTouched())
    {
      swipecheck();
    }

    creditprocessing();

  }

  public void creditprocessing()
  {
    controlCredits();
    if (flingup && ((OrthographicCamera) viewportland.getCamera()).position.y >= credits.y - 1)
    {
      flingup = false;
      flingdown = false;
    }
    if (flingdown && ((OrthographicCamera) viewportland.getCamera()).position.y <= tomenu.y + 1)
    {
      flingdown = false;
      flingup = false;

    }
    if (credpress)
    {
      if (setcred)
      {
        setupCredit();
        String prs = "presslbl";
        Entity press = root.getChild(prs).getEntity();
        Actions.addAction(press, Actions.fadeOut(.5f * APP_SPD));
        setcred = false;
      }
      if (flingdown || flingup)
      {
        credpress = false;
      }

    }
    else
    {
      if (((OrthographicCamera) viewportland.getCamera()).zoom != ZOOMCOUT && !flingdown && !flingup
        && viewportland.getCamera().position.y >= tomenu.y)
      {
        if (constel != "")
        {
          String c = "const";
          String lbl = "label";
          String d = "draw";
          String prs = "presslbl";
          Entity drawing = root.getChild(constel + c).getChild(d).getEntity();
          Entity con = root.getChild(constel + c).getChild(c).getEntity();
          Entity label = root.getChild(constel + lbl).getEntity();
          Entity press = root.getChild(prs).getEntity();
          boolean np = false;
          // Zoom out, passing in the distance of zoom, time it should take, and
          // the positioning of the camera
          if (drawing != null)
          {
            Actions.addAction(drawing, Actions.fadeOut(.5f * APP_SPD));
          }
          if (label != null)
          {
            Actions.addAction(label, Actions.fadeOut(.5f * APP_SPD));
          }
          else
          {
            for (int i = 1; i < 3; i++)
            {
              Actions.addAction(con, Actions.fadeIn(.5f * APP_SPD));
              Entity credit = root.getChild(constel + lbl + Integer.toString(i)).getEntity();
              Actions.addAction(credit, Actions.fadeOut(.5f * APP_SPD));
            }
          }
          int end = 7;
          for (int i = 1; i < end; i++)
          {
            button = i;
            Entity link = root.getChild(constel + Integer.toString(i)).getEntity();
            if (link != null)
            {
              Actions.addAction(link, Actions.sequence(Actions.alpha(.01f, .5f)));
            }

            else
            {
              i = end;
            }
          }
          constel = "";


        }
        Zoom(ZOOMCOUT, ZOOMCODUR, credits.x, credits.y);
      }
    }
    if (!constel.equals(""))
    {
      Entity constellation = root.getChild(constel + "const").getEntity();
      if (constellation != null)
      {
        Zoom(ZOOMCIN, ZOOMCIDUR, constellation.getComponent(TransformComponent.class).x + (constellation.getComponent(DimensionsComponent.class).width * constellation.getComponent(TransformComponent.class).scaleX) * .5f,
          constellation.getComponent(TransformComponent.class).y + (constellation.getComponent(DimensionsComponent.class).height * (constellation.getComponent(TransformComponent.class).scaleY) * .4f));
      }
    }
    if (Gdx.input.isKeyJustPressed(Keys.BACK) || Gdx.input.isKeyJustPressed(Keys.SPACE))
    {
      if (stage == null)
      {
        processback();
        String prs = "presslbl";
        Entity press = root.getChild(prs).getEntity();
        Actions.addAction(press, Actions.fadeIn(.5f * APP_SPD));
        /*constel = "";*/
      }
    }
  }

  public void setupCredit()
  {
    Entity constellation = root.getChild(constel + "const").getEntity();
    Entity drawing = root.getChild(constel + "const").getChild("draw").getEntity();
    Entity c = root.getChild(constel + "const").getChild("const").getEntity();
    Entity label = root.getChild(constel + "label").getEntity();

    // Zoom into credits, passing in the distance of zoom, time it should
    // take, and the positioning of the camera
    if ((drawing != null && label != null) || constel.equals("poweredby"))
    {
      if (drawing != null)
      {
        Actions.removeActions(drawing);
        Actions.addAction(drawing, Actions.fadeIn(.5f * APP_SPD));
      }
      if (label != null)
      {

        Actions.addAction(label, Actions.fadeIn(.5f * APP_SPD));
      }
      else
      {
        Entity label1 = root.getChild(constel + "label" + String.valueOf(1)).getEntity();
        Entity label2 = root.getChild(constel + "label" + String.valueOf(2)).getEntity();

        Actions.addAction(label1, Actions.fadeIn(.5f * APP_SPD));
        Actions.addAction(label2, Actions.fadeIn(.5f * APP_SPD));
        Actions.addAction(c, Actions.fadeOut(.5f * APP_SPD));
      }

      int end = 7;
      for (int i = 1; i < end; i++)
      {

        final Entity link = root.getChild(constel + i).getEntity();


        if (link != null)
        {
          link.getComponent(ButtonComponent.class).clearListeners();
          Actions.addAction(link, Actions.fadeIn(.5f * APP_SPD));
          link.getComponent(ButtonComponent.class)
            .addListener(new ButtonComponent.ButtonListener()
            {
              @Override
              public void clicked()
              {
                try
                {
                  if (runningby == DESKTOP)
                  {
                    System.out.println(link.getComponent(MainItemComponent.class).itemIdentifier + " is working");
                  }
                  else
                  {
                    int getdigit = Integer.parseInt(link.getComponent(MainItemComponent.class).itemIdentifier.replaceAll("\\D", ""));
                    button = getdigit;

                    Method link = AndroidInterfaceLIBGDX.class.getDeclaredMethod(constel, int.class);
                    link.setAccessible(true);
                    link.invoke(android, button);
                  }
                  ((OrthographicCamera) viewportland.getCamera()).zoom = ZOOMCIN;

                }
                catch (NoSuchMethodException e)
                {
                  e.printStackTrace();
                }
                catch (IllegalArgumentException e)
                {
                  e.printStackTrace();
                }
                catch (IllegalAccessException e)
                {
                  e.printStackTrace();
                }
                catch (InvocationTargetException e)
                {
                  e.printStackTrace();
                }
              }

              @Override
              public void touchDown()
              {
              }

              @Override
              public void touchUp()
              {
              }
            });
        }
        else
        {
          i = end;
        }
      }
    }
  }

  public void Zoom(float targetzoom, float duration, float movex, float movey)
  {
    // set current vals to process interpolation smoothly
    zoompoint = ((OrthographicCamera) viewportland.getCamera()).zoom;
    endzoom = targetzoom;
    targetduration = startingduration = duration;

    if (((OrthographicCamera) viewportland.getCamera()).zoom >= targetzoom && credpress
      || ((OrthographicCamera) viewportland.getCamera()).zoom <= targetzoom && !credpress)
    {
      targetduration -= Gdx.graphics.getDeltaTime();
      float progress = targetduration < 0 ? 1 : 1f - targetduration / startingduration;

      if ((((OrthographicCamera) viewportland.getCamera()).zoom != endzoom))
      {
        ((OrthographicCamera) viewportland.getCamera()).zoom = Interpolation.pow3Out.apply(zoompoint, endzoom, progress);

        viewportland.getCamera().position.x = Interpolation.pow3Out.apply(viewportland.getCamera().position.x, movex,
          progress);
        viewportland.getCamera().position.y = Interpolation.pow3Out.apply(viewportland.getCamera().position.y, movey,
          progress);
      }
      else
      {
        if (viewportland.getCamera().position.x != movex)
        {
          viewportland.getCamera().position.x = movex;
          viewportland.getCamera().position.y = movey;
        }
      }


    }
    else if (stage != null)
    {
      if (stage.p1charzoom || stage.p2charzoom)
      {
        targetduration -= Gdx.graphics.getDeltaTime();
        float progress = targetduration < 0 ? 1 : 1f - targetduration / startingduration;

        ((OrthographicCamera) viewportland.getCamera()).zoom = Interpolation.pow5Out.apply(zoompoint, endzoom,
          progress);

        viewportland.getCamera().position.x = Interpolation.pow5Out.apply(viewportland.getCamera().position.x, movex,
          progress);
        viewportland.getCamera().position.y = Interpolation.pow5Out.apply(viewportland.getCamera().position.y, movey,
          progress);
      }
    }

  }

  /**
   * Method returns SceneLoader based on Screen orientation
   */
  public SPFZSceneLoader update(String orientation)
  {
    if (orientation == ("portrait"))
    {
      return port;
    }
    else
    {
      return land;
    }


  }

  @Override
  public boolean zoom(float initialDistance, float distance)
  {

    return false;
  }

  @Override
  public Connection connect(String s, Properties properties) throws SQLException
  {
    return null;
  }

  @Override
  public boolean acceptsURL(String s) throws SQLException
  {
    return false;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo(String s, Properties properties) throws SQLException
  {
    return new DriverPropertyInfo[0];
  }

  @Override
  public int getMajorVersion()
  {
    return 0;
  }

  @Override
  public int getMinorVersion()
  {
    return 0;
  }

  @Override
  public boolean jdbcCompliant()
  {
    return false;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException
  {
    return null;
  }

  @Override
  public Object call() throws Exception
  {
    rm.initAllResources();
    return null;
  }
}
