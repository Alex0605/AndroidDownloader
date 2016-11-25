package com.zhjh.downloader.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.Uri;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class PackageUtil {	
	

	
	public static boolean isApkInstalled(Context context, String packagename) {
		try {
			@SuppressWarnings("deprecation")
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packagename,
							PackageManager.GET_UNINSTALLED_PACKAGES);
			if (packageInfo != null)
				return true;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// LogUtil.e(e);
		}
		return false;
	}
	
	public static int getApkVersionCode(Context context, String packagename) {
		try {
			@SuppressWarnings("deprecation")
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packagename,
							PackageManager.GET_UNINSTALLED_PACKAGES);
			if (packageInfo != null)
				return packageInfo.versionCode;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return -1;
	}
	public static void installApk(Context context, String path,
			String packagename) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + path),
				"application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	public static void uninstallApk(Context context, String packagename) {
		Uri uninstall = Uri.parse("package:" + packagename);
		Intent intent = new Intent(Intent.ACTION_DELETE, uninstall);
		context.startActivity(intent);
	}



	public static PackageInfo getPackageInfo(Context context, String packagename) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(packagename,
							PackageManager.GET_UNINSTALLED_PACKAGES);
			return packageInfo;
		} catch (NameNotFoundException e) {
			// LogUtil.e(e);
		}
		return null;
	}
	
	public static PackageInfo getBuildinPackageInfo(Context context) {
		File buildinApk = getAssetFile(context,"buildin.apk");
		if (buildinApk == null || !buildinApk.exists()) {
			return null;
		}
		PackageInfo packageInfo = getPackageInfoFromFilePath(context,buildinApk
				.getPath());
		packageInfo.applicationInfo.sourceDir = buildinApk.getPath();
		packageInfo.applicationInfo.publicSourceDir = buildinApk.getPath();
		return packageInfo;
	}

	public static PackageInfo getPackageInfoFromFilePath(Context context,String filePath) {
		PackageManager packageManager =  context.getPackageManager();
		PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
				filePath, PackageManager.GET_ACTIVITIES);
		return packageInfo;
	}

	public static File getAssetFile(Context context,String fileName) {
		AssetManager assets = context.getAssets();
		try {
			InputStream stream = assets.open(fileName);
			if (stream == null) {
				return null;
			}
			File externalCacheDir =context.getExternalCacheDir();
			File file = new File(externalCacheDir.getPath(), fileName);
			if (file.exists())
				return file;
			file.createNewFile();
			writeStreamToFile(stream, file);
			stream.close();
			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void writeStreamToFile(InputStream stream, File file) {
		try {
			//
			OutputStream output = null;
			try {
				output = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				try {
					final byte[] buffer = new byte[1024];
					int read;

					while ((read = stream.read(buffer)) != -1)
						output.write(buffer, 0, read);

					output.flush();
				} finally {
					output.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



}
