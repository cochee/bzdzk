package com.vallny.bzdzk;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.vallny.bzdzk.global.BaseActivity;
import com.vallny.bzdzk.global.BaseApplication;
import com.vallny.bzdzk.util.URLHelper;

public class LoginActivity extends BaseActivity {

	private EditText username;
	private EditText password;
	private CheckBox save;
	private ImageButton login;
	private ProgressDialog progress;

	private SharedPreferences sp;

	private boolean isExit;

	public final static String PREFS_NAME = "com.vallny.bzdzk";
	private final static String BASE = "http://192.168.1.101:8080/bzdzk/auth/androidlogin";

	public final static int IS_NOT_ONLINE = 0;
	public final static int SUCCESS = 1;
	public final static int FALSE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		save = (CheckBox) findViewById(R.id.save);
		login = (ImageButton) findViewById(R.id.login);

		sp = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		final String _username = sp.getString("loginname", null);
		final String _password = sp.getString("loginpwd", null);
		if (_username != null && _password != null) {
			// login(_username, _password);
			username.setText(_username);
			password.setText(_password);
		}
		
		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String loginname = username.getText().toString().trim();
				String loginpwd = password.getText().toString().trim();
				if (save.isChecked()) {
					Editor editor = sp.edit();
					editor.putString("loginname", loginname);
					editor.putString("loginpwd", loginpwd);
					editor.commit();
				}

				if (!TextUtils.isEmpty(loginname) && !TextUtils.isEmpty(loginpwd)) {
					login(loginname, loginpwd);
				} else {
					showToast(R.string.is_empty);
				}

			}

		});

	}

	private void login(String loginname, String loginpwd) {

		progress = new ProgressDialog(this);
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.setMessage(this.getString(R.string.logining));
		progress.show();

		final Map<String, String> params = new HashMap<String, String>();
		params.put("loginname", loginname);
		params.put("loginpwd", loginpwd);
		new Thread() {
			public void run() {
				String result = URLHelper.queryStringForPost(BASE, params);
				if ("连接错误！".equals(result)) {
					Message msg = handler.obtainMessage(IS_NOT_ONLINE);
					msg.sendToTarget();
				} else if (!"false".equals(result)) {
					Message msg = handler.obtainMessage(SUCCESS, result);
					msg.sendToTarget();
				} else {
					Message msg = handler.obtainMessage(FALSE);
					msg.sendToTarget();
				}
			};
		}.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case IS_NOT_ONLINE:
				showToast(R.string.online_error);
				break;
			case SUCCESS:
				String result = (String) msg.obj;
				Intent intent = new Intent(LoginActivity.this, BzdzkActivity.class);
				intent.putExtra("yhid", result.split(",")[0]);
				intent.putExtra("name", result.split(",")[1]);
				startActivity(intent);
				break;
			case FALSE:
				showToast(R.string.system_error);
				break;

			}
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

		}
	};

	private void showToast(int ResId) {
		Toast.makeText(LoginActivity.this, ResId, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onBackPressed() {
		exitBy2Click();
	}

	private void exitBy2Click() {
		Timer tExit = null;
		if (isExit == false) {
			isExit = true; // 准备退出
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {
				@Override
				public void run() {
					isExit = false; // 取消退出
				}
			}, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

		} else {
			BaseApplication.getInstance().exit();
		}
	}

}
