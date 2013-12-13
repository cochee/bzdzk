package com.vallny.bzdzk.util;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.vallny.bzdzk.BzdzkActivity;
import com.vallny.bzdzk.R;
import com.vallny.bzdzk.TreeFragment;
import com.vallny.bzdzk.bean.TreeBean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

public class TreeHelper {

	public final static int IS_ONLINE_TRUE = 1;
	public final static int IS_ONLINE_FALSE = 2;
	public final static int IS_NO_CHILD = 3;

	private static Handler handler;
	private static ProgressDialog progress;

	private static BzdzkActivity _activity;
	private static boolean _isFirst;
	private static TreeBean _parent;
	private static TreeHelper tree;
	private static String _url;

	private TreeHelper() {
	}

	public void initTree(final String url) {
		_url = url;
		progress = new ProgressDialog(_activity);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.setMessage(_activity.getString(R.string.loading));
		progress.show();
		new Thread() {
			public void run() {
				String json = URLHelper.queryStringForGet(url);
				if (!TextUtils.isEmpty(json) && "[]".equals(json)) {
					Message msg = handler.obtainMessage(IS_NO_CHILD);
					msg.sendToTarget();
				} else if (!TextUtils.isEmpty(json) && json.equals("连接错误！")) {
					Message msg = handler.obtainMessage(IS_ONLINE_FALSE);
					msg.sendToTarget();
				} else {
					ArrayList<TreeBean> list = (ArrayList<TreeBean>) JSONHelper.JSON2List(json);
					Message msg = handler.obtainMessage(IS_ONLINE_TRUE, list);
					msg.sendToTarget();
				}
			};
		}.start();
	}

	public static TreeHelper getInstance(SherlockFragmentActivity activity, boolean isFirst, TreeBean parent) {
		_activity = (BzdzkActivity) activity;
		_isFirst = isFirst;
		_parent = parent;
		if (tree == null) {
			tree = new TreeHelper();
			handler = new Handler() {
				@SuppressWarnings("unchecked")
				public void handleMessage(Message msg) {
					_activity.removeGraphic();
					switch (msg.what) {
					case IS_ONLINE_TRUE:
						FragmentTransaction ft = _activity.getSupportFragmentManager().beginTransaction();
						TreeFragment fragment = new TreeFragment(_url,(ArrayList<TreeBean>) msg.obj);
						fragment.setParent_tree(_parent);
						ft.replace(R.id.menu_frame, fragment, _parent == null ? "" : _parent.getId());
						if (!_isFirst)
							ft.addToBackStack(null);
						ft.commit();
						break;
					case IS_ONLINE_FALSE:
						new AlertDialog.Builder(_activity).setTitle(R.string.title).setMessage(R.string.online_error).setPositiveButton(R.string.know, null).create().show();
						break;
					case IS_NO_CHILD:
						new AlertDialog.Builder(_activity).setTitle(R.string.title).setMessage(R.string.no_child).setPositiveButton(R.string.know, null).create().show();
						break;
					}
					if (progress != null && progress.isShowing()) {
						progress.dismiss();
					}
				};
			};
		}
		return tree;
	}

	public static void release() {
		tree = null;
		handler = null;
	}

}
