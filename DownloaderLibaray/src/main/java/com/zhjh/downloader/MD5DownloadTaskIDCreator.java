package com.zhjh.downloader;


import com.zhjh.downloader.util.MD5;

/**
 * @author ZAlex
 */
public class MD5DownloadTaskIDCreator implements DownloadTaskIDCreator {

    @Override
    public String createId(DownloadTask task) {
        return MD5.getMD5(task.getUrl());
    }

}
