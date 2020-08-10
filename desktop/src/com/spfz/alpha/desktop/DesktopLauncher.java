package com.spfz.alpha.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.spfz.alpha.spfzTrial;

public class DesktopLauncher 
{
	public static void main (String[] arg)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
				
		config.height = 1080;
	
		config.width = 1920;
		//config.fullscreen = true;
	
		new LwjglApplication(new spfzTrial(), config);
	}
}


