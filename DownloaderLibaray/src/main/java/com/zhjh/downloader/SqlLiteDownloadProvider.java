package com.zhjh.downloader;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SqlLiteDownloadProvider implements DownloadProvider {

    private static SqlLiteDownloadProvider instance;

    private ADownloader manager;

    private String DOWNLOAD_TABLE = "tb_download";

    private SQLiteDatabase db;

    private SqlLiteDownloadProvider(ADownloader manager) {
        this.manager = manager;
        File dbFile = new File(manager.getConfig().getDownloadDbPath() + File.separator + "db", "download.db");
        if (dbFile.exists()) {
            db = SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        } else {
            if (!dbFile.getParentFile().isDirectory()) {
                dbFile.getParentFile().mkdirs();
            }
            try {
                dbFile.createNewFile();
                db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalAccessError("cannot create database file of path: " + dbFile.getAbsolutePath());
            }
        }

        createTables();
    }

    public static synchronized SqlLiteDownloadProvider getInstance(ADownloader manager) {
        if (instance == null) {
            instance = new SqlLiteDownloadProvider(manager);
        }

        return instance;
    }

    private void createTables() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("CREATE TABLE IF NOT EXISTS ").append(DOWNLOAD_TABLE);
        buffer.append("(");
        buffer.append("`").append(DownloadTask.ID).append("` VARCHAR PRIMARY KEY,");
        buffer.append("`").append(DownloadTask.URL).append("` VARCHAR,");
        buffer.append("`").append(DownloadTask.DATATYPE).append("` VARCHAR,");
        buffer.append("`").append(DownloadTask.MIMETYPE).append("` VARCHAR,");
        buffer.append("`").append(DownloadTask.SAVEPATH).append("` VARCHAR,");
        buffer.append("`").append(DownloadTask.NAME).append("` VARCHAR,");
        buffer.append("`").append(DownloadTask.FINISHEDSIZE).append("` LONG,");
        buffer.append("`").append(DownloadTask.TOTALSIZE).append("` LONG,");
        buffer.append("`").append(DownloadTask.STATUS).append("` int");
        buffer.append(")");
        db.execSQL(buffer.toString());
    }

    public void saveDownloadTask(DownloadTask task) {
        ContentValues values = createDownloadTaskValues(task);
        db.insert(DOWNLOAD_TABLE, null, values);

        notifyDownloadStatusChanged(task);
    }

    public void updateDownloadTask(DownloadTask task) {
    	synchronized (new Object()) {
            ContentValues values = createDownloadTaskValues(task);
            db.update(DOWNLOAD_TABLE, values, DownloadTask.ID + "=?", new String[]{task.getId()});

            notifyDownloadStatusChanged(task);
		}
    }

    public DownloadTask findDownloadTaskById(String id) {
    	DownloadTask task = null;
    	Cursor cursor = null;
    	try{
	        if (TextUtils.isEmpty(id)) {
	            return null;
	        }
	        
	        cursor = db.query(DOWNLOAD_TABLE, null, DownloadTask.ID + "=?", new String[]{id}, null, null, null);
	        if (cursor.moveToNext()) {
	            task = restoreDownloadTaskFromCursor(cursor);
	        }
    	}catch(Exception e){
    		
    	}finally{
    		if(cursor!=null){
    			cursor.close();
    		}
    	}

        return task;
    }

    public void deleteDownloadTask(DownloadTask task) {
    	synchronized (new Object()) {
            db.delete(DOWNLOAD_TABLE, DownloadTask.ID + "=?", new String[]{task.getId()});
            notifyDownloadStatusChanged(task);
		}
    }

    public DownloadTask findDownloadTask(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        DownloadTask task = null;
        Cursor cursor  = null;
        try{
	        cursor = db.query(DOWNLOAD_TABLE, columns, selection, selectionArgs, groupBy, having, orderBy);
	        if (cursor.moveToNext()) {
	            task = restoreDownloadTaskFromCursor(cursor);
	        }
        }catch(Exception e){
        	
        }finally{
        	if(cursor!=null){
        		cursor.close();
        	}
        }
        return task;
    }

    public void notifyDownloadStatusChanged(DownloadTask task) {
        manager.notifyDownloadTaskStatusChanged(task);
    }

    public List<DownloadTask> getAllDownloadTask() {
        List<DownloadTask> list = new ArrayList<DownloadTask>();
        DownloadTask task = null;
        Cursor cursor = null;
        try{
	       cursor = db.query(DOWNLOAD_TABLE, null, null, null, null, null, DownloadTask.ID);
	        while (cursor.moveToNext()) {
	            task = restoreDownloadTaskFromCursor(cursor);
	            list.add(task);
	        }
        }catch(Exception e){
        	if(cursor!=null){
        		cursor.close();
        	}
        }
        return list;
    }

    private ContentValues createDownloadTaskValues(DownloadTask task) {
        ContentValues values = new ContentValues();
        values.put(DownloadTask.ID, task.getId());
        values.put(DownloadTask.URL, task.getUrl());
        values.put(DownloadTask.DATATYPE, task.getDataType());
        values.put(DownloadTask.MIMETYPE, task.getMimeType());
        values.put(DownloadTask.SAVEPATH, task.getDownloadSavePath());
        values.put(DownloadTask.FINISHEDSIZE, task.getDownloadFinishedSize());
        values.put(DownloadTask.TOTALSIZE, task.getDownloadTotalSize());
        values.put(DownloadTask.NAME, task.getName());
        values.put(DownloadTask.STATUS, task.getStatus());

        return values;
    }

    private DownloadTask restoreDownloadTaskFromCursor(Cursor cursor) {
        DownloadTask task = new DownloadTask();
        task.setId(cursor.getString(cursor.getColumnIndex(DownloadTask.ID)));
        task.setName(cursor.getString(cursor.getColumnIndex(DownloadTask.NAME)));
        task.setDataType(cursor.getInt(cursor.getColumnIndex(DownloadTask.DATATYPE)));
        task.setUrl(cursor.getString(cursor.getColumnIndex(DownloadTask.URL)));
        task.setMimeType(cursor.getString(cursor.getColumnIndex(DownloadTask.MIMETYPE)));
        task.setDownloadSavePath(cursor.getString(cursor.getColumnIndex(DownloadTask.SAVEPATH)));
        task.setDownloadFinishedSize(cursor.getLong(cursor.getColumnIndex(DownloadTask.FINISHEDSIZE)));
        task.setDownloadTotalSize(cursor.getLong(cursor.getColumnIndex(DownloadTask.TOTALSIZE)));
        task.setStatus(cursor.getInt(cursor.getColumnIndex(DownloadTask.STATUS)));
        if (task.getStatus() == DownloadTask.STATUS_RUNNING
                && !ADownloader.getInstance().isDownloadTaskInOperate(task)) {
            task.setStatus(DownloadTask.STATUS_PAUSED);
        }
        return task;
    }
}
