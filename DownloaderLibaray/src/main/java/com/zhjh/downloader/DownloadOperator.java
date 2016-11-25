package com.zhjh.downloader;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadOperator implements Runnable {

    // 128 kb
    private static final long REFRESH_INTEVAL_SIZE = 128 * 1024;

    private DownloadManager manager;

    private DownloadTask task;

    // already try times
    private int tryTimes;

    private volatile boolean pauseFlag;
    private volatile boolean stopFlag;

    private String filePath;

    DownloadOperator(DownloadManager manager, DownloadTask task) {
        this.manager = manager;
        this.task = task;
        this.tryTimes = 0;
    }

    void pauseDownload() {
        if (pauseFlag) {
            return;
        }
        pauseFlag = true;
    }

    void resumeDownload() {
        if (!pauseFlag) {
            return;
        }
        pauseFlag = false;
    }

    void cancelDownload() {
        stopFlag = true;
    }

    @Override
    public void run() {
        do {
            manager.onDownloadStarted(task);
            RandomAccessFile raf = null;
            HttpURLConnection conn = null;
            InputStream is = null;
            try {
                raf = buildDownloadFile();
                conn = initConnection();

                conn.connect();

                if (conn.getContentLength() < 0) {
                    continue;
                }

                task.setDownloadSavePath(filePath);
                if (task.getDownloadTotalSize() == 0 || task.getDownloadTotalSize() == Long.MAX_VALUE) {
                    task.setDownloadTotalSize(conn.getContentLength());
                }
                if (TextUtils.isEmpty(task.getMimeType())) {
                    task.setMimeType(conn.getContentType());
                }


                is = conn.getInputStream();

                byte[] buffer = new byte[8192];
                int count = 0;
                long total = task.getDownloadFinishedSize();
                long prevTime = System.currentTimeMillis();
                long achieveSize = total;
                while (!pauseFlag && !stopFlag && (count = is.read(buffer)) != -1) {
                    raf.write(buffer, 0, count);
                    total += count;

                    long tempSize = total - achieveSize;
                    if (tempSize > REFRESH_INTEVAL_SIZE) {
                        long tempTime = System.currentTimeMillis() - prevTime;
                        long speed = tempSize * 1000 / tempTime;
                        achieveSize = total;
                        prevTime = System.currentTimeMillis();
                        task.setDownloadFinishedSize(total);
                        task.setDownloadSpeed(speed);
                        manager.updateDownloadTask(task, total, speed);
                    }
                }
                task.setDownloadFinishedSize(total);

                if (task.getDownloadFinishedSize() == task.getDownloadTotalSize()) {
                    manager.onDownloadSuccessed(task);
                } else {
                    if (!pauseFlag) {
                        manager.onDownloadFailed(task);
                    }
                }
                break;
            } catch (IOException e) {
                Log.e("DownloadOperator", e.toString());
                if (tryTimes > manager.getConfig().getRetryTime()) {
                    manager.onDownloadPaused(task);
                    break;
                } else {
                    tryTimes++;
                    continue;
                }
            }
        } while (true);
    }

    private RandomAccessFile buildDownloadFile() throws IOException {
        String fileName = task.getId() + ".apk";
        File file = new File(manager.getConfig().getDownloadSavePath(), fileName);
        if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
            throw new IOException("cannot create download folder");
        }
        if (file.exists()) {

        }
        filePath = file.getAbsolutePath();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        if (task.getDownloadFinishedSize() != 0) {
            raf.seek(task.getDownloadFinishedSize());
        }

        return raf;
    }

    private HttpURLConnection initConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(task.getUrl()).openConnection();
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setUseCaches(true);
        if (task.getDownloadFinishedSize() != 0) {
            conn.setRequestProperty("Range", "bytes=" + task.getDownloadFinishedSize() + "-");
        }

        return conn;
    }

}
