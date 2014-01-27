package jp.syoboi.android.rotatecontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RemoteViews;

public class RotateService extends Service {

	
	public static final String ACTION_EXIT = "exit";

	private static final String ACTION_RV_CLICK = "click";
	private static final int NOTIFY_ID = 1;
	
	
	static final int [] ROTATE_IDS = {
		R.id.rotate0, R.id.rotate1, R.id.rotate2, R.id.rotate3
	};
	
	
	View	mView;
	int		mCurRotation = -1;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	
		Log.v("", "onStartCommand action:" + 
				(intent != null ? intent : null));
		
		setupView();

		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_RV_CLICK.equals(action)) {
				try {
					int id = Integer.parseInt(intent.getType());
					if (id == R.id.app) {
						Intent i = new Intent(this, MainActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						
						startActivity(i);
					} else {
						onClick(id);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if (ACTION_EXIT.equals(action)) {
				exit();
				return Service.START_NOT_STICKY;
			}
		}
		setupNotification();
		
		return Service.START_STICKY;
	}
	
	void exit() {
		onClick(R.id.cancel);
		stopSelf();
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFY_ID);
	}
	
	
	void onClick(int id) {
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		int curRotation = wm.getDefaultDisplay().getRotation();
		int inputRotation = idToRotation(id, 0);
		
		int nextRotation;
		if (id == R.id.cancel) {
			nextRotation = -1;
		} else {
			nextRotation = (curRotation + inputRotation + 4) % 4;
		}
		Log.v("", "curRotation:" + curRotation + " inputRotation:" + inputRotation
				+ " nextRotation:" + nextRotation);
		
		WindowManager.LayoutParams lp = (LayoutParams) mView.getLayoutParams();
		lp.screenOrientation = rotationToScreenOrientation(nextRotation);

		if (lp.screenOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
			if (mView.getParent() != null) {
				wm.removeView(mView);
			}
		} else {
			if (mView.getParent() == null) {
				wm.addView(mView, lp);
			}
		}
		
		if (mView.getParent() != null) {
			wm.updateViewLayout(mView, lp);
		}
	}
	
	int rotationToScreenOrientation(int rotation) {
		if (rotation == -1) {
			return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		}
		return MainActivity.rotateToOrientation(this, rotation);
	}
	
	int idToRotation(int id, int fallback) {
		for (int j=0; j<ROTATE_IDS.length; j++) {
			if (ROTATE_IDS[j] == id) {
				return j;
			}
		}
		return 0;
	}
	
	/**
	 * 通知をセットアップ
	 */
	void setupNotification() {
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.rotate_status);
		
		Intent i = new Intent(this, MainActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		rv.setOnClickPendingIntent(R.id.app, 
				PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT));
		
		setClickHandler(rv, R.id.cancel);
		for (int id: ROTATE_IDS) {
			setClickHandler(rv, id);
		}
		
		Notification n = new Notification();
		n.contentView = rv;
		n.flags = Notification.FLAG_ONGOING_EVENT;
		n.icon = R.drawable.stat_rotate;
		
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(NOTIFY_ID, n);
	}
	
	void setClickHandler(RemoteViews rv, int id) {
		Intent i = new Intent(this, RotateService.class);
		i.setAction(ACTION_RV_CLICK);
		i.setType(String.valueOf(id));
		PendingIntent pi = PendingIntent.getService(
				this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setOnClickPendingIntent(id, pi);
	}
	
	
	void setupView() {
		if (mView != null) {
			return;
		}
		
		mView = new DummyView(this);
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				0
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		lp.gravity = Gravity.LEFT | Gravity.TOP;
		
		
		wm.addView(mView, lp);
		
		lp.width = 0;
		lp.height = 0;
		lp.x = 0;
		lp.y = 0;
		wm.updateViewLayout(mView, lp);
		wm.removeView(mView);
	}
	
	
	static class DummyView extends View {
		public DummyView(Context context) {
			super(context);
		}
	}
}
