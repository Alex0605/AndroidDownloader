package com.zhjh.download.test;

import java.io.Serializable;

public class AppBean implements Serializable {
	private static final long serialVersionUID = -7613475937427802843L;
	public String downloadUrl;
	public String gamePackageName;
	public String androidVersion;
	public String gameVersion;
	public String appId;
	public String gameName;
	public String gameAbbreviation;
	public String gameVersionCode;

	@Override
	public boolean equals(Object o) {
		return gamePackageName.equals(((AppBean) o).gamePackageName);
	}

	public static boolean isValidate(AppBean bean) {
		if (bean == null)
			return false;
		if (isEmpty(bean.gamePackageName) || isEmpty(bean.downloadUrl)
				|| isEmpty(bean.gameName))
			return false;
		return true;
	}

	private static boolean isEmpty(String data) {
		return !(data != null && !"".equals(data));
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getGamePackageName() {
		return gamePackageName;
	}

	public void setGamePackageName(String gamePackageName) {
		this.gamePackageName = gamePackageName;
	}

	public String getAndroidVersion() {
		return androidVersion;
	}

	public void setAndroidVersion(String androidVersion) {
		this.androidVersion = androidVersion;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	public void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getGameName() {
		return gameName;
	}

	public void setGameName(String gameName) {
		this.gameName = gameName;
	}

	public String getGameAbbreviation() {
		return gameAbbreviation;
	}

	public void setGameAbbreviation(String gameAbbreviation) {
		this.gameAbbreviation = gameAbbreviation;
	}

	public String getGameVersionCode() {
		return gameVersionCode;
	}

	public void setGameVersionCode(String gameVersionCode) {
		this.gameVersionCode = gameVersionCode;
	}
}
