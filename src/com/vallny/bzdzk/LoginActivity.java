package com.vallny.bzdzk;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.vallny.bzdzk.util.URLHelper;

public class LoginActivity extends SherlockActivity {

	private EditText username;
	private EditText password;
	private CheckBox save;
	private Button login;

	private final static String PREFS_NAME = "com.vallny.bzdzk";
	private final static String BASE = "http://192.168.1.101:8080/bzdzk/auth/androidlogin";

	public final static int IS_NOT_ONLINE = 0;
	public final static int SUCCESS = 1;
	public final static int FALSE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		username = (EditText) findViewById(R.id.username);
		password = (EditText) findViewById(R.id.password);
		save = (CheckBox) findViewById(R.id.save);
		login = (Button) findViewById(R.id.login);

		login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				String loginname = username.getText().toString().trim();
				String loginpwd = password.getText().toString().trim();
				if (TextUtils.isEmpty(loginname) && TextUtils.isEmpty(loginpwd)) {

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
				} else {
					showToast(R.string.is_empty);
				}

			}
		});

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case IS_NOT_ONLINE:
				showToast(R.string.online_error);
				break;
			case SUCCESS:
				String result = (String) msg.obj;
				Intent intent = new Intent(LoginActivity.this,BzdzkActivity.class);
				intent.putExtra("yhid", result.split(",")[0]);
				intent.putExtra("name", result.split(",")[1]);
				startActivity(intent);
				break;
			case FALSE:
				showToast(R.string.system_error);
				break;

			}

		}
	};

	private void showToast(int ResId) {
		Toast.makeText(LoginActivity.this, ResId, Toast.LENGTH_LONG).show();
	}

}
