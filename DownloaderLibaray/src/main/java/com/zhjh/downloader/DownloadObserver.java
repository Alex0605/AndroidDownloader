package com.zhjh.downloader;

/**
 * @author ZAlex
 */
public interface DownloadObserver {

    public void onDownloadTaskStatusChanged(DownloadTask task);
}
