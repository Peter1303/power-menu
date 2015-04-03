/*
 * PersistentService.java
 *
 * Author: Ming Tsang
 * Copyright (c) 2015 Ming Tsang
 * Refer to LICENSE for details
 */

package com.nkming.powermenu;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.view.View;

import eu.chainfire.libsuperuser.Shell;

/**
 * Service to display the persistent view
 */
public class PersistentService extends Service
{
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		Log.d(LOG_TAG, "onCreate");
		super.onCreate();
		initView();
		initForeground();
	}

	@Override
	public void onDestroy()
	{
		Log.d(LOG_TAG, "onDestroy");
		super.onDestroy();
		uninitForeground();
		uninitView();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}

	private static final String LOG_TAG =
			PersistentService.class.getCanonicalName();

	private void initView()
	{
		if (mView != null)
		{
			uninitView();
		}
		mView = new PersistentView(new Handler(), this, R.layout.persistent_view);
		mView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				onViewClick();
			}
		});
		mView.setOnLongClickListener(new View.OnLongClickListener()
		{
			@Override
			public boolean onLongClick(View v)
			{
				onViewLongClick();
				return true;
			}
		});
	}

	private void initForeground()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.notification_title))
				.setContentText(getString(R.string.notification_text))
				.setLocalOnly(true)
				.setOnlyAlertOnce(true)
				.setPriority(NotificationCompat.PRIORITY_MIN)
				.setSmallIcon(R.drawable.ic_action_shutdown)
				.setTicker(getString(R.string.notification_ticker));

		Intent activity = new Intent(this, PreferenceActivity.class);
		activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, 0, activity,
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);

		startForeground(1, builder.build());
	}

	private void uninitView()
	{
		if (mView != null)
		{
			mView.destroy();
			mView = null;
		}
	}

	private void uninitForeground()
	{
		stopForeground(true);
	}

	private void onViewClick()
	{
		Log.d(LOG_TAG, "onViewClick");
		SystemHelper.sleep(getApplicationContext());
	}

	private void onViewLongClick()
	{
		Log.d(LOG_TAG, "onViewLongClick");
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... params)
			{
				Shell.SU.run("am start -n "
						+ MainActivity.class.getPackage().getName()
						+ "/" + MainActivity.class.getCanonicalName());
				return null;
			}
		}.execute();
	}

	private PersistentView mView;
}
