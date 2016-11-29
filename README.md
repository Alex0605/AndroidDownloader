介绍
-
    AndroidDownloader 是一个下载续下载工具
1.如何使用在你的项目Gradle 
----------------------
	在工程中添加
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	在项目中添加  
	dependencies {
		compile 'com.github.Alex0605:AndroidDownloader:v1.0'
	}

或者直接引入库文件
