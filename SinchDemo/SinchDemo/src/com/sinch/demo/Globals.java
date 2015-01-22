package com.sinch.demo;

import android.app.Application;
import com.sinch.android.rtc.SinchClient;
// Getter-Setter class
public class Globals  extends Application {
	
    public SinchClient mSinchClient;    

    public SinchClient getmSinchClient() {
		return mSinchClient;
	}

	public void setmSinchClient(SinchClient mSinchClient) {
		this.mSinchClient = mSinchClient;
	}
	
	

}
