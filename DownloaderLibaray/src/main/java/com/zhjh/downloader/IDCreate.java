package com.zhjh.downloader;

public class IDCreate implements DownloadTaskIDCreator {

    @Override
    public String createId(DownloadTask task) {
        // TODO Auto-generated method stub
        return task.getId();
    }

}
