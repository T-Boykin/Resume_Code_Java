package com.spfz.alpha;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.*;
import com.spfz.alpha.spfzTrial;

public class AndroidLauncher extends AndroidApplication implements AndroidInterfaceLIBGDX
{


  AndroidApplicationConfiguration config;

  //layout necessary for full game view
  RelativeLayout layout;
  RelativeLayout.LayoutParams adParams;
  View gameView;
  SPFZSqlHelper spfz;

  Display display;
  final AndroidLauncher context = this;
  Handler h = new Handler();
  int bright;

  static final String FILE_NAME = "spfzfile";
  static final String FILE_NAME2 = "spfzstory";
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "spfz.sqlite";
  private static final String FIRST_CHECK = "/data/data/com.spfz.alpha";
  private static final String DATABASE_PATH = "/assets/databases/";

  //AD Initializations
  private InterstitialAd SPFZ_INT;
  private AdView SPFZ_BAN;

  String orientation;
  SPFZResourceManager rm;
  VideoView videoview;
  private Toast toast;
  boolean load;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    spfz = new SPFZSqlHelper(this.getApplicationContext());
    //spfz = new SPFZSqlHelper(this, DATABASE_NAME, FIRST_CHECK + DATABASE_PATH, null, 1);
    MobileAds.initialize(this, getString(R.string.APP_AD_ID));
    /*toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);*/
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.introvideoview);

    layout = new RelativeLayout(this);

    //configure ads
    SPFZ_BAN = new AdView(this);
    /*
    SPFZ_BAN.setAdListener(new AdListener()
    {
      @Override
      public void onAdClosed()
      {
        //SPFZ_BAN.setVisibility(View.INVISIBLE);
        layout.removeView(SPFZ_BAN);
        super.onAdClosed();
      }
    });

    SPFZ_BAN.setAdSize(AdSize.SMART_BANNER);
    SPFZ_BAN.setAdUnitId(getString(R.string.TEST_BANNER_ID));*/
    //adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
    SPFZ_INT = new InterstitialAd(this);
    SPFZ_INT.setAdUnitId(getString(R.string.TEST_INTER_ID));


    config = new AndroidApplicationConfiguration();
    //rm = new SPFZResourceManager();
    //config.numSamples = 2;
    //loadresources();
    //rm.initAllResources();
    display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

    RelativeLayout.LayoutParams adParams =
      new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
        RelativeLayout.LayoutParams.WRAP_CONTENT);
    adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    //adParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    config.useAccelerometer = false;
    config.useCompass = false;
    gameView = initializeForView(new spfzTrial(this), config);


   /* SPFZ_INT.loadAd(new AdRequest.Builder().build());
    SPFZ_INT.setAdListener(new AdListener()
    {
      @Override
      public void onAdClosed()
      {
        // Load the next interstitial.
        SPFZ_INT.loadAd(new AdRequest.Builder().build());
      }

    });*/

    SPFZ_BAN.setAdSize(AdSize.SMART_BANNER);
    SPFZ_BAN.setAdUnitId(getString(R.string.TEST_BANNER_ID));

    //add views to the layout but allow banner to overlap the gameView
    layout.addView(gameView);
    setContentView(layout);
    //hideAD();
    //SPFZ_BAN.setVisibility(View.VISIBLE);

  }

  public int getDimX()
  {
    return spfz.charDimX(spfz.getCursor());
  }

  public int getDimY()
  {
    return spfz.charDimY(spfz.getCursor());
  }

  public int getDimW()
  {
    return spfz.charDimW(spfz.getCursor());
  }

  public int getDimH()
  {
    return spfz.charDimH(spfz.getCursor());
  }

  public int getHealth()
  {
    return spfz.charHealth(spfz.getCursor());
  }

  public int getGrav()
  {
    return spfz.charGrav(spfz.getCursor());
  }

  public int getJump()
  {
    return spfz.charJump(spfz.getCursor());
  }

  public int getWalkspeed()
  {
    return spfz.charWalkspeed(spfz.getCursor());
  }

  public int getDash()
  {
    return spfz.charDash(spfz.getCursor());
  }

  public void charQuery(String character)
  {
    spfz.charQuery(character);
  }

  public ArrayList<String> retAnimCodes()
  {
    return spfz.retAnimCodes();
  }

  public List<ArrayList<Double>> retAtkInfo(String character)
  {
    return spfz.atkAnimQuery(character);
  }
  public ArrayList<String> retMoves()
  {
    return spfz.Moves();
  }
  public boolean banner_null()
  {
    if(SPFZ_BAN == null)
    {
      return true;
    }
    else
    {
      return false;
    }
  }
  public HashMap<String, int[]> retAnimData()
  {
    return spfz.retAnimData();
  }

  public ArrayList<Integer> retFPS()
  {
    return spfz.FPS();
  }

  //public int

  public ArrayList<int[]> Inputs()
  {
    return spfz.retInputs();
  }

  public void retAllAnims()
  {
    spfz.retAllAnims();
  }

  public void Open()
  {
    spfz.spfzDBOpen();
  }

  public void Close()
  {
    spfz.spfzDBClose();
  }

  public void loadresources()
  {
    AsyncTask.execute(new Runnable()
    {
      @Override
      public void run()
      {
        //rm.initAllResources();
        rm.initGame();
      }
    });
  }

  public void setHelper()
  {
    //spfz = new SPFZSqlHelper(this.getApplicationContext());
  }

  public void hideAD()
  {
    if (SPFZ_BAN.getVisibility() == View.VISIBLE)
    {
      this.runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          SPFZ_BAN.setVisibility(View.GONE);
          layout.removeViewInLayout(SPFZ_BAN);
        }
      });
    }


  }

  public boolean loaded()
  {

    this.runOnUiThread(new Runnable()
    {
      @Override
      public void run()
      {
        load = SPFZ_INT.isLoaded();
      }
    });

    return load;
  }

  public void NEW_SPFZ_AD(String ADTYPE)
  {

    switch (ADTYPE)
    {

      case "banner":
        if (SPFZ_BAN.getVisibility() == View.GONE || SPFZ_BAN.getVisibility() == View.INVISIBLE)
        {
          this.runOnUiThread(new Runnable()
          {
            @Override
            public void run()
            {
              //AdView tempView = SPFZ_BAN;
              // AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
              AdRequest adRequest = new AdRequest.Builder().addTestDevice(getString(R.string.TEST_DEVICE_ID)).build();
              RelativeLayout.LayoutParams adParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                  RelativeLayout.LayoutParams.WRAP_CONTENT);
              adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
              layout.addView(SPFZ_BAN, adParams);


              SPFZ_BAN.loadAd(adRequest);
              SPFZ_BAN.setVisibility(View.VISIBLE);
              SPFZ_BAN.setAlpha(1f);

              SPFZ_BAN.setAdListener(new AdListener()
              {

                @Override
                public void onAdClosed()
                {
                  SPFZ_BAN.setVisibility(View.INVISIBLE);
                  SPFZ_BAN.setAlpha(0f);
                  layout.removeView(SPFZ_BAN);
                  super.onAdClosed();
                }

                @Override
                public void onAdClicked()
                {
                  SPFZ_BAN.setVisibility(View.INVISIBLE);
                  SPFZ_BAN.setAlpha(0f);
                  layout.removeView(SPFZ_BAN);
                  super.onAdClicked();
                }

                @Override
                public void onAdImpression()
                {
                  super.onAdImpression();
                }
              });
            }
          });
        }

        break;
      case "inter":
        //SPFZ_INT.loadAd(adRequest);
        //SPFZ_INT.loadAd(new AdRequest.Builder().build());
        this.runOnUiThread(new Runnable()
        {
          @Override
          public void run()
          {

            SPFZ_INT.show();
          }
        });

        //toast("interstitial ad displayed");
        break;
      default:
        break;
    }
  }

  public void checkINT()
  {
    if (SPFZ_INT.isLoaded())
    {
      SPFZ_INT.show();
    }
  }

  public void videocall()
  {
    handler.post(new Runnable()
    {

      @Override
      public void run()
      {
        display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();

        videoview = (VideoView) findViewById(R.id.videoView1);
        //videoview.setVideoPath("/video/testsplash.mp4");
        videoview.requestFocus();
        // videoview.start();

      }
    });
  }

  public void initgame(final AndroidInterfaceLIBGDX android)
  {
    h.postDelayed(new Runnable()
    {

      @Override
      public void run()
      {


      }
    }, 1000);
  }

  @Override
  public void toast()
  {
    handler.post(new Runnable()
    {

      @Override
      public void run()
      {
        if(toast != null)
        {
          toast.cancel();
        }
        toast = Toast.makeText(context, "Aracade Mode will be available in the Full Version", Toast.LENGTH_SHORT);
        toast.show();

      }

    });

  }


  public int getADattr()
  {
    return SPFZ_BAN.getVisibility();
  }

  public int visible()
  {
    return View.VISIBLE;
  }

  @Override
  public void lockOrientation(final boolean lock, final String orientation)
  {
    handler.post(new Runnable()
    {

      @Override
      public void run()
      {
        if (lock && orientation == "portrait")
        {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        else if (lock && orientation == "landscape")
        {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else
        {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
      }

    });

  }

  @Override
  public void adjustBrightness(final float brightness)
  {
    bright = (int) brightness;
    handler.post(new Runnable()
    {

      @Override
      public void run()
      {

        android.provider.Settings.System.putInt(context.getContentResolver(),
          android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
          android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

        android.provider.Settings.System.putInt(context.getContentResolver(),
          android.provider.Settings.System.SCREEN_BRIGHTNESS, bright);
      }

    });

  }

  public void getrotation()
  {
    switch (display.getRotation())
    {
      case Surface.ROTATION_0:
        orientation = "portrait";
        break;
      case Surface.ROTATION_90:
        orientation = "landscape";
        break;
      case Surface.ROTATION_180:
        orientation = "portrait";
        break;
      case Surface.ROTATION_270:
        orientation = "landscape";
        break;

    }

  }

  public String getorientation()
  {
    return orientation;
  }

  public void setorientation(String orient)
  {
    orientation = orient;
  }

  @Override
  public int getBrightness()
  {

    handler.post(new Runnable()
    {

      @Override
      public void run()
      {

        android.provider.Settings.System.putInt(context.getContentResolver(),
          android.provider.Settings.System.SCREEN_BRIGHTNESS, bright);
      }

    });

    return bright;
  }


  public void writeFile(final String values, boolean append)
  {
    try
    {
      // String message;
      // message = values;
      if (values.length() == 1)
      {
        /*
         * initialization of application. This is needed in order to check to
         * see if the file exists. So we do not override any possible values
         * that will exists for the in game settings. values = "null";
         *
         */

        //toast("file initialized successfully");
      }
      else
      {

        if (append)
        {

          FileOutputStream fileOutputStream = openFileOutput(FILE_NAME, MODE_APPEND);
          fileOutputStream.write(values.getBytes());
          fileOutputStream.close();
        }
        else
        {
          FileOutputStream fileOutputStream = openFileOutput(FILE_NAME, MODE_PRIVATE);
          fileOutputStream.write(values.getBytes());
          fileOutputStream.close();
        }


      }
    }

    catch (FileNotFoundException e)
    {
      //toast("file created successfully");

      e.printStackTrace();
    }
    catch (IOException e)
    {

      e.printStackTrace();
    }

  }

  public void writeFile2(final String values, boolean append)
  {
    try
    {
      // String message;
      // message = values;
      if (values.length() == 1)
      {
        /*
         * On initialization. Populate the text file within all story text.
         *
         */

        //toast("file initialized successfully");
      }
      else
      {
        if (append)
        {
          FileOutputStream fileOutputStream = openFileOutput(FILE_NAME2, MODE_APPEND);
          fileOutputStream.write(values.getBytes());
          fileOutputStream.close();
        }
        else
        {
          FileOutputStream fileOutputStream = openFileOutput(FILE_NAME2, MODE_PRIVATE);
          fileOutputStream.write(values.getBytes());
          fileOutputStream.close();
        }


      }
    }

    catch (FileNotFoundException e)
    {
      //toast("file created successfully");

      e.printStackTrace();
    }
    catch (IOException e)
    {

      e.printStackTrace();
    }

  }

  public String readFile(final String file)
  {
    FileInputStream fileInputStream;
    String message = null;
    String values = "";
    try
    {


      fileInputStream = openFileInput(file);
      InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      StringBuffer stringBuffer = new StringBuffer();

      while ((message = bufferedReader.readLine()) != null)
      {
        stringBuffer.append(message + "\n");
        values = values + message + "\n";
        //toast(message);
      }

      if (values.length() == 1)
      {
        values = null;
      }

      //toast("file read successfully");
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return values;

  }

  public void ahmed(int link)
  {
    switch(link)
    {
      case 1:
        url(getString(R.string.AHMED_LINK_1));
        break;
      case 2:
        String[] mail = {getString(R.string.AHMED_MAIL)};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mail);
        startActivity(intent);

        break;
      case 3:
        url(getString(R.string.AHMED_LINK_3));
        break;
      case 4:
        url(getString(R.string.AHMED_LINK_4));
        break;
    }

  }

  public void trey(int link)
  {
    switch(link)
    {
      case 1:
        url(getString(R.string.SAJE_LINK_1));
        break;
      case 2:
        url(getString(R.string.SAJE_LINK_2));
        break;
      case 3:
        url(getString(R.string.SAJE_LINK_3));
        break;
      case 4:
        url(getString(R.string.SAJE_LINK_4));
        break;
    }
  }

  public void terrence(int link)
  {
    switch(link)
    {
      case 1:
        url(getString(R.string.TERR_LINK_1));
        break;
      case 2:
        url(getString(R.string.TERR_LINK_2));
        break;
      case 3:
        url(getString(R.string.TERR_LINK_3));
        break;
      case 4:
        url(getString(R.string.TERR_LINK_4));
        break;
      case 5:
        String[] mail = {getString(R.string.TERR_MAIL)};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, mail);
        startActivity(intent);
        break;
    }
  }
  public void michael(int link)
  {
    switch(link)
    {
      case 1:
        url(getString(R.string.MICH_LINK_1));
        break;
      case 2:
        url(getString(R.string.MICH_LINK_2));
        break;
      case 3:
        url(getString(R.string.MICH_LINK_3));
        break;
      case 4:
        url(getString(R.string.MICH_LINK_4));
        break;
    }
  }
  public void naresh(int link)
  {
    switch(link)
    {
      case 1:
        url(getString(R.string.NAR_LINK_1));
        break;
      case 2:
        url(getString(R.string.NAR_LINK_2));
        break;
      case 3:
        url(getString(R.string.NAR_LINK_3));
        break;
      case 4:
        url(getString(R.string.NAR_LINK_4));
        break;
    }
  }
  private void url(String url)
  {
    Uri uriUrl = Uri.parse(url);
    Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
    startActivity(launchBrowser);
  }


}
