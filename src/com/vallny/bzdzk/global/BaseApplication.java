package com.vallny.bzdzk.global;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

public class BaseApplication extends Application {

	private List<Activity> activityList = new LinkedList<Activity>();

	private static BaseApplication instance;

	public BaseApplication() {
	}

	// 单例模式中获取唯一的ExitApplication 实例
	public static BaseApplication getInstance() {
		if (null == instance) {
			instance = new BaseApplication();
		}
		return instance;

	}

	// 添加Activity 到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}

	// 遍历所有Activity 并finish

	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		System.exit(0);
	}

}
