package com.zhjh.download.test;

import android.app.Application;

import com.zhjh.downloader.BroadcastManager;
import com.zhjh.downloader.DownloadManager;

public class MyApplication extends Application{

	private static MyApplication mHallApplication;
	@Override
	public void onCreate() {
		super.onCreate();
		mHallApplication = this;
        DownloadManager.init(this);
        BroadcastManager.init(this);
       
	}
	
    public static MyApplication getGlobalContext() {
        return mHallApplication;
    }
}
