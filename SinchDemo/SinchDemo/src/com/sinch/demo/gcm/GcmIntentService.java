package com.sinch.demo.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sinch.demo.LoginActivity;
import com.sinch.demo.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_BG_ID = 10001;
	public static final int NOTIFICATION_FR_ID = 10002;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;	
	public GcmIntentService() {
		super("GcmIntentService");
	}
	public static final String TAG = "GCM";

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);
		if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that GCM will be
			 * extended in the future with new message types, just ignore any message types you're
			 * not interested in, or that you don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				// sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				// sendNotification("Deleted messages on server: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equalsIgnoreCase(messageType)) {
				Log.d("notification1", "notification1");
				Log.i(TAG, "Received: " + extras.toString());
				// Post notification of received message.
				if(extras.containsKey("type")){					
					if(extras.containsKey("sinch")){
						sendNotification("" + extras.getString("message"), Integer.valueOf(extras.getString("type")), Integer.valueOf(extras.getString("friend_id")), ""+extras.getString("sinch"), ""+extras.getString("name"));
					}else{
						Log.d("notification3", "notification3");
						sendNotification("" + extras.getString("message"), Integer.valueOf(extras.getString("type")), Integer.valueOf(extras.getString("sender_id")), "", "");
					}
				}				
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	private void sendNotification(String msg, int type, int sender, String payLoad, String name) {

		Uri path = null;
		Intent intent = null;
		mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		intent 		 = new Intent(this, LoginActivity.class);
		intent.putExtra("notifType" , ""+type);
		intent.putExtra("payLoad"   , payLoad);
		intent.putExtra("name"      , name);
		PendingIntent contentIntent = null;
		if(intent!=null)
			contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
		path =RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);;
		NotificationCompat.Builder mBuilder =new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.icon)
		.setContentTitle(getResources().getString(R.string.app_name))
		.setStyle(new NotificationCompat.BigTextStyle()
		.bigText(msg))
		.setSound(path)
		.setLights(0xFF0000ff,2000, 4000)
		.setContentText(msg);    	
		if(contentIntent!=null)
			mBuilder.setContentIntent(contentIntent); 
		mBuilder.setAutoCancel(true);				
		mNotificationManager.notify(1, mBuilder.build());
	}

}

