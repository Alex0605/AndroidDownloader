package com.zhjh.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.zhjh.downloader.util.PackageUtil;

import java.util.List;

public class DownloadService extends Service {

    private DownloadListener mDownloadListener;
    private DownloadBinder mStub = new DownloadBinder();

    public void onCreate() {
        initDownloadListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    private void initDownloadListener() {
        mDownloadListener = new DownloadListener() {

            @Override
            public void onDownloadUpdated(DownloadTask task, long finishedSize,
                                          long trafficSpeed) {
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_UPDATED, task);
            }

            @Override
            public void onDownloadSuccessed(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_SUCCESS, task);
                PackageUtil.installApk(getApplicationContext(), task.getDownloadSavePath(), task.getId());
            }

            @Override
            public void onDownloadStart(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_START, task);
            }

            @Override
            public void onDownloadRetry(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_RETRY, task);
            }

            @Override
            public void onDownloadResumed(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_RESUMED, task);
            }

            @Override
            public void onDownloadPaused(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_PAUSED, task);
            }

            @Override
            public void onDownloadFailed(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_UPDATED, task);
            }

            @Override
            public void onDownloadCanceled(DownloadTask task) {
                // TODO Auto-generated method stub
                BroadcastManager.getInstance().sendBroadcast(BroadcastManager.TAG_ON_DOWNLOAD_CANCELED, task);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mStub;
    }

    public void startDownload(String packagename,int type, String url) {
        DownloadTask task = new DownloadTask();
        task.setUrl(url);
        task.setId(packagename);
        task.setDataType(type);
        ADownloader.getInstance().addDownloadTask(task, mDownloadListener);
    }

    public void pauseDownload(String packagename) {
        // TODO Auto-generated method stub
        ADownloader.getInstance().pauseDownload(ADownloader.getInstance().findDownloadTaskById(packagename), mDownloadListener);
    }

    public void pauseAll() {
        List<DownloadTask> tasks = ADownloader.getInstance().getAllDownloadTask();
        if(tasks!=null){
	        for (int i = 0; i < tasks.size(); i++) {
	            if (tasks.get(i).getStatus() == DownloadTask.STATUS_RUNNING || tasks.get(i).getStatus() == DownloadTask.STATUS_PENDDING)
	                pauseDownload(tasks.get(i).getId());
	        }
        }
    }

    public void resumeDownload(String packagename) {
        // TODO Auto-generated method stub
        ADownloader.getInstance().resumeDownload(ADownloader.getInstance().findDownloadTaskById(packagename), mDownloadListener);
    }

    public void cancelDownload(String packagename) {
        // TODO Auto-generated method stub
        ADownloader.getInstance().cancelDownload(ADownloader.getInstance().findDownloadTaskById(packagename), mDownloadListener);
    }

    public class DownloadBinder extends Binder {
        public DownloadService getDownloadService() {
            return DownloadService.this;
        }
    }

}
