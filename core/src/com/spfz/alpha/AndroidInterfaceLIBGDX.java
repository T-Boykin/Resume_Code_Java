package com.spfz.alpha;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Interface is used to incorporate Android functions within the LIBGDX game lifecycle
 */
public interface AndroidInterfaceLIBGDX
{
	void toast();

	void NEW_SPFZ_AD(String ADTYPE);

	boolean banner_null();
	void loadresources();
	
	void lockOrientation(final boolean unlock, String orientation);
	
	void adjustBrightness(final float brightness);

	int getBrightness();
	
	void getrotation();
	
	String getorientation();
	
	void setorientation(final String orient);
	
	void writeFile(final String s, boolean a);
	
	String readFile(final String File);

	void videocall();

	void setHelper();

	ArrayList<Integer> retFPS();
	List<ArrayList<Double>> retAtkInfo(String c);

	void retAllAnims();

	//ArrayList<Integer> FPS(ArrayList<Integer F>);

	HashMap<String, int[]> retAnimData();

	ArrayList<String> retAnimCodes();
	ArrayList<String> retMoves();


	ArrayList<int[]> Inputs();

	void charQuery(String c);

	void Open();
	void Close();

	void hideAD();

	int getADattr();

	int visible();

	boolean loaded();

	int getDimX();
	int getDimY();
	int getDimW();
	int getDimH();
	int getHealth();
	int getGrav();
	int getJump();
	int getWalkspeed();
	int getDash();

	void terrence(int i);
	void trey(int i);
	void ahmed(int i);
	void michael(int i);
	void naresh(int i);

}
