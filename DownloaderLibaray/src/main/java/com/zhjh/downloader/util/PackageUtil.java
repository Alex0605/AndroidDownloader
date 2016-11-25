package com.zhjh.downloader.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public class PackageUtil {	
	
	public static PackageInfo getPackageInfoFromFilePath(Context context,String filePath) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
				filePath, PackageManager.GET_ACTIVITIES);
		return packageInfo;
	}
	
	
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

}
