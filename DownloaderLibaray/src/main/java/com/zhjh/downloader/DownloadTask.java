package com.zhjh.downloader;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

import com.zhjh.downloader.util.PackageUtil;

import java.io.File;

/**
 * @author ZAlex
 */
public class DownloadTask implements Parcelable {

    public static final String ID = "_id";
    public static final String URL = "_url";
    public static final String DATATYPE="_datatype";
    public static final String MIMETYPE = "_mimetype";
    public static final String SAVEPATH = "_savepath";
    public static final String FINISHEDSIZE = "_finishedsize";
    public static final String TOTALSIZE = "_totalsize";
    public static final String NAME = "_name";
    public static final String STATUS = "_status";

    public static final int STATUS_PENDDING = 1 << 0;//1

    public static final int STATUS_RUNNING = 1 << 1;//2

    public static final int STATUS_PAUSED = 1 << 2;//4

    public static final int STATUS_CANCELED = 1 << 3;//8

    public static final int STATUS_FINISHED = 1 << 4;//16

    public static final int STATUS_ERROR = 1 << 5;//32

    public static final int OPERATE_EMPTY = 2 << 0;
    public static final int OPERATE_DOWNLOAD = 2 << 1;
    public static final int OPERATE_UPGRADE = 2 << 2;
    public static final int OPERATE_PAUSE = 2 << 3;
    public static final int OPERATE_RESUME = 2 << 4;
    public static final int OPERATE_COMPLETE = 2 << 5;

    private String id;

    private String name;

    private String url;

    private String mimeType;
    
    private int dataType;

    private String downloadSavePath;

    private long downloadFinishedSize;

    private long downloadTotalSize;

    // @Transparent no need to persist
    private long downloadSpeed;

    private int status;

    public DownloadTask() {
        downloadFinishedSize = 0;
        downloadTotalSize = 0;
        status = STATUS_PENDDING;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DownloadTask)) {
            return false;
        }
        DownloadTask task = (DownloadTask) o;
        if (this.name == null || this.downloadSavePath == null) {
            return this.url.equals(task.url);
        }
        return this.name.equals(task.name) && this.url.equals(task.url) && this.downloadSavePath.equals(task.downloadSavePath);
    }

    @Override
    public int hashCode() {
        int code = name == null ? 0 : name.hashCode();
        code += url.hashCode();
        return code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDownloadSavePath() {
        return downloadSavePath;
    }

    public void setDownloadSavePath(String downloadSavePath) {
        this.downloadSavePath = downloadSavePath;
    }

    public long getDownloadFinishedSize() {
        return downloadFinishedSize;
    }

    public void setDownloadFinishedSize(long downloadFinishedSize) {
        this.downloadFinishedSize = downloadFinishedSize;
    }

    public long getDownloadTotalSize() {
        if (downloadTotalSize == 0)
            return Long.MAX_VALUE;
        return downloadTotalSize;
    }

    public void setDownloadTotalSize(long downloadTotalSize) {
        this.downloadTotalSize = downloadTotalSize;
    }

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    
	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

    /**
     * 
     * @param context
     * @param task
     * @param versionCode 
     * @param packageName
     * @return
     */
    public static int getOperateState(Context context ,DownloadTask task,String versionCode,String packageName){
        int status = OPERATE_DOWNLOAD;
        boolean isInstalled = PackageUtil.isApkInstalled(context, packageName);
        boolean needUpdate = isInstalled?Integer.parseInt(versionCode) > PackageUtil
                .getApkVersionCode(context,packageName):false;
        if(task != null){
            if(task.status == STATUS_ERROR || task.status == STATUS_CANCELED)
                status = OPERATE_DOWNLOAD;
            if(task.status == STATUS_PENDDING || task.status == STATUS_PAUSED)
                status = OPERATE_RESUME;
            if(task.status == STATUS_RUNNING)
                status = OPERATE_PAUSE;
            if(task.status == STATUS_FINISHED){
                status = OPERATE_DOWNLOAD;
                File file = new File(task.getDownloadSavePath());
                if(file.exists()){
                    PackageInfo packageInfo = PackageUtil.getPackageInfoFromFilePath(context,task.getDownloadSavePath());
                    if(packageInfo != null && packageInfo.versionCode
                            >= Integer.parseInt(versionCode))
                        status = OPERATE_COMPLETE;
                }
            }
        }
        if(needUpdate && status == OPERATE_DOWNLOAD)
            status = OPERATE_UPGRADE;
        return status;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(mimeType);
        dest.writeString(downloadSavePath);
        dest.writeLong(downloadFinishedSize);
        dest.writeLong(downloadTotalSize);
        dest.writeLong(downloadSpeed);
        dest.writeInt(status);
        dest.writeInt(dataType);
    }

    public static final Creator<DownloadTask> CREATOR = new Creator<DownloadTask>() {

        @Override
        public DownloadTask createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new DownloadTask(source);
        }

        @Override
        public DownloadTask[] newArray(int size) {
            // TODO Auto-generated method stub
            return new DownloadTask[size];
        }
    };

    public DownloadTask(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        url = parcel.readString();
        mimeType = parcel.readString();
        downloadSavePath = parcel.readString();
        downloadFinishedSize = parcel.readLong();
        downloadTotalSize = parcel.readLong();
        downloadSpeed = parcel.readLong();
        status = parcel.readInt();
        dataType = parcel.readInt();
    }



}
