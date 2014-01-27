package jp.syoboi.android.rotatecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	
	int	mTestSequence = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		findViewById(R.id.exit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, RotateService.class);
				i.setAction(RotateService.ACTION_EXIT);
				startService(i);
				finish();
			}
		});
		updateConfig();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		startService(new Intent(this, RotateService.class));
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		showRotate();
	}
	
	void updateConfig() {
		
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		int [] rotateMap = new int [4];
 
		int origRotate = wm.getDefaultDisplay().getRotation();
		for (int j=0; j<4; j++) {
			
			setRequestedOrientation(idxToOrientation(j));
			int rot = wm.getDefaultDisplay().getRotation();
			
			rotateMap[j] = rot;
			Log.v("", "idx:" + j + " rot:" + rot);
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = prefs.edit();
		for (int j=0; j<4; j++) {
			edit.putInt("r" + j, rotateMap[j]);
		}
		edit.commit();
		
		setRequestedOrientation(rotateToOrientation(this, origRotate));
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	
	public static int rotateToOrientation(Context context, int rotate) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		for (int j=0; j<4; j++) {
			int rotate2 = prefs.getInt("r" + j, 0);
			if (rotate2 == rotate) {
				return idxToOrientation(j);
			}
		}
		return idxToOrientation(0);
	}
	
	public static int idxToOrientation(int idx) {
		switch (idx) {
		case 0:
		default:
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		case 1:
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		case 2:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
		case 3:
			return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
		}
	}
	
	
	void showRotate() {
		Display d = getWindowManager().getDefaultDisplay();
		final int rot = d.getRotation();
		
		Log.v("", "rot:" + rot);
	}
	

}
