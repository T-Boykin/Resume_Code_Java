package com.spfz.alpha;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import java.util.HashMap;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;

public class SPFZSqlHelper extends SQLiteAssetHelper
{
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "spfz.sqlite";
  private static final String FIRST_CHECK = "/data/data";
  private static final String DATABASE_PATH = "/assets/databases/";
  SQLiteDatabase db;
  String zcode;
  String abspath;

  HashMap<String, int[]> animdata;
  ArrayList<String> animcodes;
  ArrayList<String> attacks;
  ArrayList<Integer> framesPS;

  Cursor c;
  File file;
  Context context;

  /*public SPFZSqlHelper(Context context, String name, String storageDirectory, SQLiteDatabase.CursorFactory factory, int version)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;

    abspath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
  }*/
  public SPFZSqlHelper(Context context)
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    this.context = context;

    abspath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
  }

  private void copyDataBase() throws IOException
  {
    InputStream mInput = context.getAssets().open(DATABASE_NAME);
    String outputFileName = DATABASE_PATH + DATABASE_NAME;
    OutputStream mOutput = new FileOutputStream(outputFileName);
    byte[] mBuffer = new byte[1024];
    int mLength;
    while ((mLength = mInput.read(mBuffer)) > 0) {
      mOutput.write(mBuffer, 0, mLength);
    }
    mOutput.flush();
    mOutput.close();
    mInput.close();
  }
  /*@Override
  public void onCreate(SQLiteDatabase db)
  {
    *//*try
    {
      copyDataBase();
    }
    catch(IOException e)
    {
      e.printStackTrace();
    }*//*

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {

  }
*/
  public boolean checkDB()
  {
    SQLiteDatabase checkDB = null;

    try {
      String myPath = abspath;
      checkDB = SQLiteDatabase.openDatabase(myPath, null,
              SQLiteDatabase.OPEN_READWRITE);
    } catch (Exception e) {

    }

    if (checkDB != null)
    {
      checkDB.close();
    }

    return checkDB != null ? true : false;
  }
  public void spfzDBOpen()
  {
    db = getReadableDatabase();
  }

  public void spfzDBClose()
  {
    if (db != null)
    {
      db.close();
    }
  }

  public void charQuery(String character)
  {

    c = db.rawQuery("SELECT * " +
            "FROM characters " +
            "WHERE spfzname = ?", new String[]{character});

    if(c.moveToNext())
    {
      zcode = c.getString(0);
    }

    //return c;

  }

  public Cursor getCursor()
  {
    return c;
  }
  public int charDimX(Cursor c)
  {
    return c.getInt(9);
  }

  public int charDimY(Cursor c)
  {
    return c.getInt(10);
  }
  public int charDimW(Cursor c)
  {
    return c.getInt(11);
  }
  public int charDimH(Cursor c)
  {
    return c.getInt(12);
  }
  public int charHealth(Cursor c)
  {
    return c.getInt(4);
  }
  public int charGrav(Cursor c)
  {
    return c.getInt(8);
  }
  public int charJump(Cursor c)
  {
    return c.getInt(5);
  }
  public int charWalkspeed(Cursor c)
  {
    return c.getInt(6);
  }
  public int charDash(Cursor c)
  {
    return c.getInt(7);
  }

  public List<ArrayList<Double>> atkAnimQuery(String character)
  {

    attacks = new ArrayList<String>();
    String[] cols = {"atkadvblk", "atkadvhit", "blkdist", "hitdist", "actfrmbeg", "actfrmend", "boxposx", "boxposy",
           "boxposw", "boxposh", "blkdmg", "hitdmg", "blkmtrgn", "hitmtrgn", "fwdmvm", "bckmvm", "jugpwr",
           "bdstrt", "fdstrt", "bdact", "fdact", "bdrec", "fdrec", "strtupbeg", "strtupend", "loopstrt",
           "loopend", "endstrt", "endfin", "type", "speed", "posx", "posy", "spfzdimw", "spfzdimh", "spfzdimx", "spfzdimy", "spawnfrm"};

    Cursor c;
    List<ArrayList<Double>> values = new ArrayList<ArrayList<Double>>();

    c = db.rawQuery("SELECT DISTINCT animations.stranimfrm, animations.endanimfrm, " +
              "attacks.*, projectiles.*, movement.* " +
            "FROM attacks " +
            "LEFT JOIN animations ON animations.spfzanimcode = attacks.spfzatk " +
            "LEFT JOIN movement ON movement.spfzanimcode = attacks.spfzatk " +
            "LEFT JOIN projectiles ON projectiles.proj = attacks.spfzatk " +
            "WHERE animations.spfzcode = ? " +
            "AND attacks.spfzcode = ? " +
            "GROUP BY attacks.spfzatk", new String[]{zcode, zcode});


    //StringBuffer buffer = new StringBuffer();

    int init = 0;
    int i = 0;
    c.moveToFirst();
    while (c.moveToNext())
    {

      if (init == 0)
      {
        for (int j = 0; j < cols.length - 1; j++)
        {
          ArrayList<Double> temp = new ArrayList<Double>();
          //temp.add(c.getDouble(j));
          temp.add(c.getDouble(c.getColumnIndex(cols[j])));
          values.add(temp);


        }
      }
      else
      {
        for (int j = 0; j < cols.length - 1; j++)
        {
          //values.get(j).add(c.getDouble(j));
          values.get(j).add(c.getDouble(c.getColumnIndex(cols[j])));

        }
      }
      attacks.add(c.getString(c.getColumnIndex("spfzatk")));
      //check for end of row set i = 0
      if(init == 0)
      {
        init++;
      }
    }

    return values;
  }

  public ArrayList<String> Moves()
  {
    return attacks;
  }

  //see methods within CharAttributes class, processing similar to Desktiop run
  public void retAllAnims()
  {

    animdata = new HashMap<String, int[]>();
    animcodes = new ArrayList<String>();
    framesPS = new ArrayList<Integer>();

    Cursor c;

    c = db.rawQuery("SELECT * " +
            "FROM animations " +
            "WHERE spfzcode = ?" , new String[]{zcode});

    c.moveToFirst();
    while (c.moveToNext())
    {
      animdata.put(c.getString(1), new int[]{c.getInt(2) - 1, c.getInt(3) - 1});
      animcodes.add(c.getString(1));
      framesPS.add(c.getInt(4));
    }

  }

  public HashMap<String, int[]> retAnimData()
  {
    return animdata;
  }
  public ArrayList<String> retAnimCodes()
  {
    return animcodes;
  }

  public ArrayList<Integer> FPS()
  {
    return framesPS;
  }
  public ArrayList<int[]> retInputs()
  {
    ArrayList<int[]> inputs = new ArrayList<int[]>();

    Cursor c;
    c = db.rawQuery("SELECT * " +
            "FROM inputs " +
            "WHERE spfzcode = ?", new String[]{zcode});

    c.moveToFirst();

    while (c.moveToNext())
    {
      Integer temp = c.getInt(2);
      String[] holder = Integer.toString(temp).split("");

      //if the 1st value is empty, reposition the array
      if(holder[0].equals(""))
      {
        for (int j = 0; j <= holder.length - 1; j++)
        {
          if(j == holder.length - 1)
          {
            holder[holder.length - 1] = "";
          }
          else
          {
            holder[j] = holder[j + 1];
          }

        }
      }

      int[] hold = new int[holder.length - 1];
      int i = 0;
      for (String str : holder)
      {
        if(!holder[i].equals(""))
        {
          hold[i] = Integer.parseInt(holder[i]);
        }
        i++;
      }
inputs.add(hold);
    }
   return inputs;
  }
}
