package com.zhjh.downloader;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.List;

public class DownloadManager {
	private final String TAG ="DownloadManager";
    private static DownloadManager mDownloadManager;
    private static Context             mContext;

    private DownloadService mDownloadService;
    private ServiceConnection   mServiceConnection;

    private DownloadManager() {
        initDownloadConfig();
        mServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // TODO Auto-generated method stub
                mDownloadService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // TODO Auto-generated method stub
                Log.i(TAG,"onServiceConnected");
                mDownloadService = ((DownloadService.DownloadBinder) service).getDownloadService();
            }
        };

        Intent bindIntent = new Intent(mContext, DownloadService.class);
        mContext.bindService(bindIntent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    public static void init(Context context) {
        mContext = context;
        mDownloadManager = new DownloadManager();
    }

    public static DownloadManager getInstance() {
        return mDownloadManager;
    }

    private void initDownloadConfig() {
        // custom configuration
        DownloadConfig.Builder builder = new DownloadConfig.Builder(mContext);
        String downloadPath;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            downloadPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + File.separator
                    + mContext.getPackageName()
                    + File.separator + "Download";
        } else {
            downloadPath = Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + "data" + File.separator
                    + mContext.getPackageName() + File.separator + mContext.getPackageName()
                    + File.separator + "Download";
        }
        File downloadFile = new File(downloadPath);
        if (!downloadFile.isDirectory() && !downloadFile.mkdirs()) {
//            throw new IllegalAccessError(" cannot create download folder");
        }
        builder.setDownloadSavePath(downloadPath);
        builder.setDownloadDbPath(mContext.getCacheDir().getPath());
        builder.setMaxDownloadThread(24);
        builder.setDownloadTaskIDCreator(new IDCreate());
        ADownloader.getInstance().init(builder.build());
    }

    public void startDownload(String packagename,int type, String url) {
        if (TextUtils.isEmpty(packagename) || TextUtils.isEmpty(url)) {
            Log.e(TAG,"packagename or downloadlink empty");
            return;
        }
        getDownloadService().startDownload(packagename,type, url);
    }

    public void pauseDownload(String packagename) {
        getDownloadService().pauseDownload(packagename);
    }

    /**
     * pause all download task
     */
    public void pauseAll(){
        if(getDownloadService() != null)
            getDownloadService().pauseAll();
    }

    public void resumeDownload(String packagename) {
        getDownloadService().resumeDownload(packagename);
    }

    public void cancelDownload(String packagename) {
        getDownloadService().cancelDownload(packagename);
    }

    public DownloadTask getDownloadTask(String packagename) {
        return ADownloader.getInstance().findDownloadTaskById(packagename);
    }

    public List<DownloadTask> getAllDownloadTask() {
        return ADownloader.getInstance().getAllDownloadTask();
    }

    public int getUnfinishedTaskCount() {
        List<DownloadTask> tasks = ADownloader.getInstance().getAllDownloadTask();
        int unfinishedCount = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getStatus() != DownloadTask.STATUS_FINISHED)
                unfinishedCount++;
        }
        return unfinishedCount;
    }

    public int getRunningTaskCount() {
        List<DownloadTask> tasks = ADownloader.getInstance().getAllDownloadTask();
        int runningCount = 0;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getStatus() == DownloadTask.STATUS_RUNNING)
                runningCount++;
        }
        return runningCount;
    }

    public DownloadService getDownloadService() {
        return mDownloadService;
    }
}
