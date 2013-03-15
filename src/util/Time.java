package util;

import processing.core.PApplet;


public class Time implements Drawable{
	
	private float deltaTime = 0; //time in msec for this frame
	private long lastUpdateTime = 0;
	private static Time instance = null;
	
	public static Time getInstance(){
		if(Time.instance == null)
			instance = new Time();
		return instance;
	}
	
	private Time(){
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public void update() {
		long currUpdateTime = System.currentTimeMillis();
		deltaTime = (currUpdateTime - lastUpdateTime) / 1000f;
		lastUpdateTime = currUpdateTime;
	}

	@Override
	public void draw(PApplet p) {
	}
	
	public static float deltaTime(){
		return getInstance().deltaTime;
	}

}
