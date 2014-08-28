package com.pfg.screenshot;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private final String LOG_TAG = "screenshot";
	private Button btn;
	
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(new MyBtnListener());
	}
	
	class MyBtnListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v) {
			mHandler.postDelayed(mScreenShotRunnable, 1000);
		}
		
	}
	
	private final Runnable mScreenShotRunnable = new Runnable()
	{
		@Override
		public void run() {
			takeScreenShot();
		}
		
	};
	
	private Object mScreenShotLock = new Object();
	private ServiceConnection mScreenShotConn = null;
	private void takeScreenShot()
	{
		synchronized (mScreenShotLock) {
			if (mScreenShotConn != null) {
				return;
			}
			Intent intent = new Intent();
			intent.setClassName("com.android.systemui","com.android.systemui.screenshot.TakeScreenshotService");
			mScreenShotConn = new ServiceConnection() {
				@Override
				public void onServiceConnected(ComponentName name,
						IBinder service) {
					Messenger messenger = new Messenger(service);
					Message msg = Message.obtain(null, 1);
					final ServiceConnection myConn = this;
					Handler h = new Handler(mHandler.getLooper()) {
						@Override
						public void handleMessage(Message msg) {
							Log.i(LOG_TAG,"onServiceConnected handle reply message");
							if (myConn == mScreenShotConn) {
								unbindService(mScreenShotConn);
								mScreenShotConn = null;
							}
						}
					};
					msg.replyTo = new Messenger(h);
					msg.arg1 = msg.arg2 = 0;
					msg.arg1 = 1;
					try {
						messenger.send(msg);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				@Override
				public void onServiceDisconnected(ComponentName name) {}
			};
			boolean ret = bindService(intent, mScreenShotConn, BIND_AUTO_CREATE);
			Log.i(LOG_TAG, "bind service success!");
			}
	}
}
