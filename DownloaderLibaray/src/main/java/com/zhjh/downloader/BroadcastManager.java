package com.zhjh.downloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;


public class BroadcastManager {

    private static BroadcastManager  mHallBroadcastManager;
    private static LocalBroadcastManager mLocalBroadcastManager;

    public static String TAG_ON_DOWNLOAD_START = "ON_DOWNLOAD_START";
    public static String TAG_ON_DOWNLOAD_UPDATED = "ON_DOWNLOAD_UPDATED";
    public static String TAG_ON_DOWNLOAD_SUCCESS = "ON_DOWNLOAD_SUCCESS";
    public static String TAG_ON_DOWNLOAD_RETRY = "ON_DOWNLOAD_RETRY";
    public static String TAG_ON_DOWNLOAD_RESUMED = "ON_DOWNLOAD_RESUMED";
    public static String TAG_ON_DOWNLOAD_PAUSED = "ON_DOWNLOAD_PAUSED";
    public static String TAG_ON_DOWNLOAD_FAILED = "ON_DOWNLOAD_FAILED";
    public static String TAG_ON_DOWNLOAD_CANCELED = "ON_DOWNLOAD_CANCELED";

    public static String TAG_ACCOUNT_MODIFY = "ACCOUNT_MODIFY";
    public static String TAG_LOCATION_MODIFY = "LOCATION_MODIFY";

    private static String KEY_TASK = "task";
    private static String KEY_PACKAGENAME = "packagename";

    public static void init(Context context) {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mHallBroadcastManager = new BroadcastManager();
    }

    public void registerDownloadBroadcastReceiver(
            KokoDownloadBroadcastReceiver hallDownloadBroadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TAG_ON_DOWNLOAD_START);
        intentFilter.addAction(TAG_ON_DOWNLOAD_UPDATED);
        intentFilter.addAction(TAG_ON_DOWNLOAD_SUCCESS);
        intentFilter.addAction(TAG_ON_DOWNLOAD_RETRY);
        intentFilter.addAction(TAG_ON_DOWNLOAD_RESUMED);
        intentFilter.addAction(TAG_ON_DOWNLOAD_PAUSED);
        intentFilter.addAction(TAG_ON_DOWNLOAD_FAILED);
        intentFilter.addAction(TAG_ON_DOWNLOAD_CANCELED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mLocalBroadcastManager.registerReceiver(hallDownloadBroadcastReceiver,
                intentFilter);
    }

    public void registerAccountModifyBroadcastReceiver(
            AccountModifyBroadcastReceiver accountSwitchBroadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TAG_ACCOUNT_MODIFY);
        mLocalBroadcastManager.registerReceiver(accountSwitchBroadcastReceiver,
                intentFilter);
    }

    public void registerLocationModifyBroadcastReceiver(
            LocationModifyBroadcastReceiver locationModifyBroadcastReceiver) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TAG_LOCATION_MODIFY);
        mLocalBroadcastManager.registerReceiver(locationModifyBroadcastReceiver,
                intentFilter);
    }

    public void unregiterBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        mLocalBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    public static BroadcastManager getInstance() {
        return mHallBroadcastManager;
    }

    public void sendBroadcast(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void sendBroadcast(String action, DownloadTask task) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(KEY_TASK, task);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void sendBroadcast(String action, String packagename) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(KEY_PACKAGENAME, packagename);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public static interface KokoDownloadListener {
        public void onDownloadUpdated(DownloadTask task);

        public void onDownloadSuccessed(DownloadTask task);

        public void onDownloadStart(DownloadTask task);

        public void onDownloadRetry(DownloadTask task);

        public void onDownloadResumed(DownloadTask task);

        public void onDownloadPaused(DownloadTask task);

        public void onDownloadFailed(DownloadTask task);

        public void onDownloadCanceled(DownloadTask task);

        void onApkInstallOrUninstall(String packagename);
    }

    public static class KokoDownloadBroadcastReceiver extends BroadcastReceiver {

        private KokoDownloadListener mHallDownloadListener;

        public KokoDownloadBroadcastReceiver(
                KokoDownloadListener hallDownloadListener) {
            mHallDownloadListener = hallDownloadListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String tag = intent.getAction();
            String packagename = intent.getStringExtra(KEY_PACKAGENAME);
            DownloadTask task = intent.getParcelableExtra(KEY_TASK);
            if (tag.equals(TAG_ON_DOWNLOAD_START)) {
                mHallDownloadListener.onDownloadStart(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_CANCELED)) {
                mHallDownloadListener.onDownloadCanceled(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_UPDATED)) {
                mHallDownloadListener.onDownloadUpdated(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_SUCCESS)) {
                mHallDownloadListener.onDownloadSuccessed(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_RETRY)) {
                mHallDownloadListener.onDownloadRetry(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_RESUMED)) {
                mHallDownloadListener.onDownloadResumed(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_PAUSED)) {
                mHallDownloadListener.onDownloadPaused(task);
            } else if (tag.equals(TAG_ON_DOWNLOAD_FAILED)) {
                mHallDownloadListener.onDownloadFailed(task);
            } else if (tag.equals(Intent.ACTION_PACKAGE_ADDED)
                    || tag.equals(Intent.ACTION_PACKAGE_REMOVED)
                    || tag.equals(Intent.ACTION_PACKAGE_REPLACED)) {
                mHallDownloadListener.onApkInstallOrUninstall(packagename.replace("package:", ""));
            }
        }

    }

    public static interface AccountModifyListener {
        public void onAccountModify();
    }

    public static class AccountModifyBroadcastReceiver extends
            BroadcastReceiver {

        AccountModifyListener mAccountModifyListener;

        public AccountModifyBroadcastReceiver(
                AccountModifyListener accountSwitchListener) {
            // TODO Auto-generated constructor stub
            mAccountModifyListener = accountSwitchListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            mAccountModifyListener.onAccountModify();
        }
    }

    public static interface LocationModifyListener {
        public void onLocationModify();
    }

    public static class LocationModifyBroadcastReceiver extends
            BroadcastReceiver {

        LocationModifyListener mLocationModifyListener;

        public LocationModifyBroadcastReceiver(
                LocationModifyListener locationModifyListener) {
            // TODO Auto-generated constructor stub
            mLocationModifyListener = locationModifyListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            mLocationModifyListener.onLocationModify();
        }
    }
}
