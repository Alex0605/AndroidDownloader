package com.zhjh.downloader;

import com.koko.downloadplugin.util.MD5;



public class MD5DownloadTaskIDCreator implements DownloadTaskIDCreator {

    @Override
    public String createId(DownloadTask task) {
        return MD5.getMD5(task.getUrl());
    }

}
