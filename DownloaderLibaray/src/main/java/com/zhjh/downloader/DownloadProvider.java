package com.zhjh.downloader;

import java.util.List;

public interface DownloadProvider {

    public void saveDownloadTask(DownloadTask task);

    public void updateDownloadTask(DownloadTask task);

    public void deleteDownloadTask(DownloadTask task);

    public DownloadTask findDownloadTaskById(String id);

    public DownloadTask findDownloadTask(String[] columns,
                                         String selection,
                                         String[] selectionArgs,
                                         String groupBy,
                                         String having,
                                         String orderBy);

    public List<DownloadTask> getAllDownloadTask();

    public void notifyDownloadStatusChanged(DownloadTask task);
}