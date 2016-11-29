package com.zhjh.download.test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.zhjh.downloader.BroadcastManager;
import com.zhjh.downloader.DownloadManager;
import com.zhjh.downloader.DownloadTask;
import com.zhjh.downloader.util.PackageUtil;

import java.util.List;


/**
 * @autor Z.Alex
 * @date 2016/11/8
 */

public class MainActivity extends FragmentActivity {



	SubmitProcessButton  spbtnDownload;
	ProgressImageView progressImageView;

	private BroadcastManager.KokoDownloadBroadcastReceiver mHallDownloadBroadcastReceiver;
	private String url = "http://acj.pc6.com/pc6_soure/2016-9/com.walrushz.logistics_6.apk";
	private AppBean appBean;

	private int dataType = 0;

	@Override
	protected void onCreate(Bundle onSavedInstanceState) {
		super.onCreate(onSavedInstanceState);
		setContentView(R.layout.activity_main);
		spbtnDownload = (SubmitProcessButton)findViewById(R.id.spbtn_download);
		progressImageView =(ProgressImageView)findViewById(R.id.progress_img);
		appBean = new AppBean();
		appBean.setDownloadUrl(url);
		appBean.setAppId("1001");
		appBean.setAndroidVersion("V1.0.6");
		appBean.setGameAbbreviation("V1.0");
		appBean.setGameName("卡荣司机版");
		appBean.setGamePackageName("com.walrushz.logistics");
		appBean.setGameVersion("V1.0");
		appBean.setGameVersionCode("1");


		initDownloadState();
		registerDownloadListener();


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterDownloadListener();
	}

	private void initDownloadState() {
		setSubmitProcessButton(this, spbtnDownload, progressImageView,appBean,
							  DownloadManager.getInstance().getDownloadTask(appBean.getGamePackageName()));
	}

	private void setSubmitProcessButton(final Context context, SubmitProcessButton button,ProgressImageView imageView, final AppBean appBean, final DownloadTask task) {

		boolean isInstalled = PackageUtil.isApkInstalled(this, appBean.gamePackageName);
		boolean needUpdate = isInstalled ?
							 Integer.parseInt(appBean.gameVersionCode) > PackageUtil.getApkVersionCode(this, appBean.gamePackageName) :
							 false;
		int operateState = DownloadTask.getOperateState(MainActivity.this, task, appBean.getGameVersionCode(),
														appBean.getGamePackageName());
		progressImageView.setImageResource(R.mipmap.pic_1);
		if (isInstalled && !needUpdate) {
			button.setProgress(100);

			updateProgress(progressImageView, 100);

			button.setText("启动");
			button.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
						PackageInfo pi;
						boolean started = false;
						try {
							pi = context.getPackageManager().getPackageInfo(appBean.gamePackageName, 0);
							Intent resolveIntent = new Intent("com.zhjh.download.test.MAIN", null);
							resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
							resolveIntent.setPackage(pi.packageName);

							List<ResolveInfo> apps = context.getPackageManager()
									.queryIntentActivities(resolveIntent, 0);
							if (apps != null && !apps.isEmpty()) {
								ResolveInfo ri = apps.iterator().next();
								if (ri != null) {
									String pn = ri.activityInfo.packageName;
									String className = ri.activityInfo.name;

									Intent intent = new Intent(Intent.ACTION_MAIN);
									intent.addCategory(Intent.CATEGORY_DEFAULT);

									ComponentName cn = new ComponentName(pn, className);

									intent.setComponent(cn);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									started = true;
									context.startActivity(intent);
								}
							}

						} catch (NameNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (started == false) {
							PackageManager packageManager = context.getPackageManager();
							Intent intent = packageManager
									.getLaunchIntentForPackage(appBean.gamePackageName);
							if (intent != null) {
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.startActivity(intent);
							}
						}
					}
			});
		} else {
			if (operateState == DownloadTask.OPERATE_DOWNLOAD) {
				final PackageInfo packageInfo = PackageUtil.getBuildinPackageInfo(MainActivity.this);
				if (packageInfo != null && appBean.gamePackageName.equals(packageInfo.packageName)) {
					button.setText("安装");
					button.setProgress(100);

					updateProgress(progressImageView, 100);

					button.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							PackageUtil.installApk(getApplicationContext(), PackageUtil.getAssetFile(MainActivity.this, "buildin1.apk").getPath(),
													packageInfo.packageName);
						}
					});
					return;
				}
				button.setProgress(0);

				updateProgress(progressImageView, 0);

				button.setText("开始下载");
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						DownloadManager.getInstance().startDownload(appBean.gamePackageName, dataType, appBean.downloadUrl);
					}
				});
			}
			if (operateState == DownloadTask.OPERATE_UPGRADE) {
				button.setProgress(0);

				updateProgress(progressImageView, 0);

				button.setText("更新");
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DownloadManager.getInstance().startDownload(appBean.gamePackageName,dataType,appBean.downloadUrl);
					}
				});
			}
			if (operateState == DownloadTask.OPERATE_PAUSE) {
				final PackageInfo packageInfo = PackageUtil.getBuildinPackageInfo(MainActivity.this);
				if (packageInfo != null && appBean.gamePackageName.equals(packageInfo.packageName)) {
					button.setText("安装");
					button.setProgress(100);

					updateProgress(progressImageView, 100);

					button.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							PackageUtil.installApk(getApplicationContext(), PackageUtil.getAssetFile(MainActivity.this, "buildin1.apk").getPath(),
													packageInfo.packageName);
						}
					});
					return;
				}
				double progress = 100.0 * task.getDownloadFinishedSize() / task.getDownloadTotalSize();
				button.setProgress((int) progress);
				button.setText("已下载" + String.format("(%.1f%%)", progress));

				updateProgress(progressImageView, (int)progress);

				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						DownloadManager.getInstance().pauseDownload(appBean.gamePackageName);
					}
				});
			}
			if (operateState == DownloadTask.OPERATE_RESUME) {
				final PackageInfo packageInfo = PackageUtil.getBuildinPackageInfo(MainActivity.this);
				if (packageInfo != null && appBean.gamePackageName.equals(packageInfo.packageName)) {
					button.setText("安装");
					button.setProgress(100);

					updateProgress(progressImageView, 100);

					button.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							PackageUtil.installApk(getApplicationContext(), PackageUtil.getAssetFile(MainActivity.this, "buildin1.apk").getPath(),
													packageInfo.packageName);
						}
					});
					return;
				}
				double progress = 100.0 * task.getDownloadFinishedSize() / task.getDownloadTotalSize();
				button.setProgress((int) progress);
				button.setText("继续" + String.format("(%.1f%%)", progress));
				updateProgress(progressImageView, (int)progress);

				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						DownloadManager.getInstance().resumeDownload(appBean.gamePackageName);
					}
				});
			}
			if (operateState == DownloadTask.OPERATE_COMPLETE) {
				button.setText("安装");
				button.setProgress(100);
				updateProgress(progressImageView, 100);
				button.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							PackageUtil.installApk(context, task.getDownloadSavePath(), task.getId());
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				});
			}
		}
	}

	private void unregisterDownloadListener() {
		BroadcastManager.getInstance().unregiterBroadcastReceiver(mHallDownloadBroadcastReceiver);
	}

	private void registerDownloadListener() {
		mHallDownloadBroadcastReceiver = new BroadcastManager.KokoDownloadBroadcastReceiver(
			new BroadcastManager.KokoDownloadListener() {

				@Override
				public void onDownloadUpdated(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadSuccessed(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadStart(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadRetry(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadResumed(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadPaused(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadFailed(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onDownloadCanceled(DownloadTask task) {
					if (task.getId().equals(appBean.gamePackageName)) {
						setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, task);
					}
				}

				@Override
				public void onApkInstallOrUninstall(String packagename) {
					try {
						if (packagename.equals(appBean.gamePackageName)) {
							setSubmitProcessButton(getApplicationContext(), spbtnDownload,progressImageView, appBean, null);
						}
					} catch (Exception e) {
					}
				}


			});
		BroadcastManager.getInstance().registerDownloadBroadcastReceiver(mHallDownloadBroadcastReceiver);
	}

	private void updateProgress(ProgressImageView mProgressImageView, int progress){

		if(progress<1){
			mProgressImageView.setMaskColor(ContextCompat.getColor(this, R.color.icon_progress_mask_color));
		}else{
			mProgressImageView.setMaskColor(ContextCompat.getColor(this, R.color.icon_progress_mask_bg_color));
		}
		mProgressImageView.setProgress(progress,progress+"%");
	}

}
