package com.zhjh.downloader;

import android.os.Environment;

import java.io.File;

public class Env {

    public static String ROOT_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "AlexDownloader";

}
