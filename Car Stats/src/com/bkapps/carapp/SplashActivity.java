package com.bkapps.carapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends Activity {

//	MediaPlayer ourSong;
	
	@Override
	protected void onCreate(Bundle brendanlovescake) {
		// TODO Auto-generated method stub
		super.onCreate(brendanlovescake);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		
		
	//	ourSong = MediaPlayer.create(SplashActivity.this, R.raw.splashsound);
	//	ourSong.start();
		
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(2000);
				} catch (InterruptedException e){
					e.printStackTrace();
				} finally{
					Intent openStartingPoint = new Intent(SplashActivity.this, MainActivity.class);
					startActivity(openStartingPoint);
					finish();
				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		ourSong.release();
		finish();
	}
	
	
	
}
