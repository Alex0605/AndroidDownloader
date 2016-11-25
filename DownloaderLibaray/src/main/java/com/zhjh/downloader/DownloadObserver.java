package com.zhjh.downloader;

public interface DownloadObserver {

    public void onDownloadTaskStatusChanged(DownloadTask task);
}
