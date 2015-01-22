package com.sinch.demo;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sinch.android.rtc.SinchError;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends BaseActivity implements SinchService.StartFailedListener {  
    private Button mLoginFirst, mLoginSecond;
    private ProgressDialog mSpinner;
    String userName;
    GoogleCloudMessaging gcm;
    String gcmRegId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sinch_login);
        registerForGCM();
        mLoginFirst = (Button) findViewById(R.id.loginFirst);
        mLoginSecond = (Button) findViewById(R.id.loginSecond);
        mLoginFirst.setEnabled(false);
        mLoginFirst.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	userName = "first_user";
                loginClicked();
            }
        });
        mLoginSecond.setEnabled(false);
        mLoginSecond.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	userName = "second_user";
                loginClicked();
            }
        });
       
    }

    @Override
    protected void onServiceConnected() {
    	mLoginFirst.setEnabled(true);
    	mLoginSecond.setEnabled(true);
        getSinchServiceInterface().setStartListener(this);       
        if(getIntent()!=null && getIntent().hasExtra("notifType") && getIntent().getStringExtra("notifType").equalsIgnoreCase("10")){
        	if(!getIntent().getStringExtra("name").equalsIgnoreCase("first_user"))
        		userName = "second_user";
        	else
        		userName = "first_user";
        	loginClicked();
        }
    }

    @Override
    protected void onPause() {
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        if (mSpinner != null) {
            mSpinner.dismiss();
        }
    }

    @Override
    public void onStarted() {
        openPlaceCallActivity();
    }

    private void loginClicked() {
        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(userName);
            showSpinner();
        } else {
            openPlaceCallActivity();
        }
    }

    private void openPlaceCallActivity() {
        Intent mainActivity = new Intent(this, PlaceCallActivity.class);
        if(getIntent()!=null && getIntent().hasExtra("notifType") && getIntent().getStringExtra("notifType").equalsIgnoreCase("10")){
        	mainActivity.putExtra("notifType", ""+getIntent().getStringExtra("notifType"));
        	mainActivity.putExtra("payLoad", ""+getIntent().getStringExtra("payLoad"));
			Log.e("SINCHLOAD", ""+getIntent().getStringExtra("payLoad"));
		}
        startActivity(mainActivity);
    }

    private void showSpinner() {
        mSpinner = new ProgressDialog(this);
        mSpinner.setTitle("Logging in");
        mSpinner.setMessage("Please wait...");
        mSpinner.show();
    }
    
    private void registerForGCM(){
		if (checkPlayServices()) {
			gcm 	 = GoogleCloudMessaging.getInstance(LoginActivity.this);
			gcmRegId = getRegistrationId(LoginActivity.this);
			if (gcmRegId.isEmpty()) {
				registerInBackground();
			}else{}
		} else {
			Log.i("", "No valid Google Play Services APK found.");
		}
	}   
   
    
    /**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(LoginActivity.this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				//		                GooglePlayServicesUtil.getErrorDialog(resultCode, MainActivity.this,
				//		                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i("LOGIN", "This device is not supported.");
				LoginActivity.this.finish();
			}
			return false;
		}
		return true;
	}
	/**
	 * Gets the current registration ID for application on GCM service, if there is one.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getGcmPreferences(context);
		String registrationId 				= prefs.getString("GCM_REG_ID", "");
		if (registrationId.isEmpty()) {
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion  = prefs.getInt("APP_VERSION", Integer.MIN_VALUE);
		int currentVersion 	   = getAppVersion(LoginActivity.this);
		if (registeredVersion != currentVersion) {
			return "";
		}
		return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGcmPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return context.getSharedPreferences("demo_app",Context.MODE_PRIVATE);
	}
	
	/**
	 * Stores the registration ID and the app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */ 
	private void storeRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getGcmPreferences(context);
		int appVersion = getAppVersion(context);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("GCM_REG_ID", regId);
		editor.putInt("APP_VERSION", appVersion);
		editor.commit();
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and the app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(LoginActivity.this);
					}
					gcmRegId = gcm.register("53503645503");
					Log.v("gcmRegId", ""+gcmRegId);
				
					// Persist the regID - no need to register again.
					storeRegistrationId(LoginActivity.this, gcmRegId);
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}
			@Override
			protected void onPostExecute(String msg) {
				//CommonUtil.showShortToast("Reg4 :"+msg, MainActivity.this);
			}
		}.execute(null, null, null);
	}	
}
