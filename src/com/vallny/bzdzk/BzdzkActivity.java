package com.vallny.bzdzk;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnStatusChangedListener.STATUS;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.ags.query.QueryTask;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.vallny.bzdzk.bean.TreeBean;
import com.vallny.bzdzk.global.BaseActivity;
import com.vallny.bzdzk.global.BaseApplication;
import com.vallny.bzdzk.util.JSONHelper;
import com.vallny.bzdzk.util.TreeHelper;
import com.vallny.bzdzk.util.URLHelper;

public class BzdzkActivity extends BaseActivity {

	private MapView mMapView;
	private ArcGISDynamicMapServiceLayer dLayer;

	private Button mark_bt;
	private ProgressBar progressBar;
	private GraphicsLayer mGraphicsLayer;
	private SimpleFillSymbol sfs;

	private MapOnTouchListener defaulttouchListener;
	private MyTouchListener touchListener;

	private SlidingMenu menu;
	private boolean isNew = true;
	private boolean isShowGraphic;
	private boolean isCanMark;
	private boolean isExit;

	private String mark_url;
	private TreeBean mark_tree;
	private View view;

	private String yhid;
	private String name;

	private final static String URL = "http://192.168.1.101:6080/arcgis/rest/services/PGIS/bzdzk/MapServer";
	// private final static String URL =
	// "http://192.168.1.101:6080/arcgis/rest/services/PGIS/XZQH/MapServer";
	private final static String TAG = "com.vallny.bzdzk";

	public final static double SEARCH_RADIUS = 5;

	public final static int MARK = 0;
	public final static int UN_MARK = 1;

	public final static int LOGOUT = 2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		yhid = intent.getStringExtra("yhid");
		name = intent.getStringExtra("name");

		findAndSetListener();
		dLayer = new ArcGISDynamicMapServiceLayer(URL);
		mMapView.addLayer(dLayer);
		sfs = new SimpleFillSymbol(Color.BLACK);
		sfs.setOutline(new SimpleLineSymbol(Color.RED, 2));
		sfs.setAlpha(100);

		// configure the SlidingMenu
		menu = new SlidingMenu(this);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.0f);
		menu.setBehindScrollScale(0);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(R.layout.menu_frame);

		TreeHelper.getInstance(this, true, null).initTree(URLHelper.ZRQ + "?yhid=" + yhid);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE,LOGOUT,Menu.NONE,R.string.logout).setIcon(R.drawable.ic_launcher).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			menu.toggle();
			break;
		case LOGOUT:
			logout();
			Intent intent = new Intent(BzdzkActivity.this,LoginActivity.class);
			startActivity(intent);
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void logout() {
		Editor editor = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.remove("loginname");
		editor.remove("loginpwd");
		editor.commit();
		
	}

	public void updateLayer(String layer, TreeBean tree, View view) {

		showGraphicOnMap(tree);

		boolean canMark = true;
		menu.toggle();
		mark_tree = tree;
		this.view = view;
		if ("xq".equals(layer)) {
			mark_url = "/13";
		} else if ("mph".equals(layer)) {
			mark_url = "/12";
		} else if ("jzw".equals(layer)) {
			mark_url = "/11";
		} else if ("dy".equals(layer)) {
			mark_url = "/11";
		} else if ("fj".equals(layer)) {
			mark_url = "/11";
		} else {
			canMark = false;
			mark_tree = null;
			this.view = null;
		}
		if (canMark) {
			mark_bt.setVisibility(View.VISIBLE);
			if (tree.getMark()) {
				showUnMark();
			} else {
				showMark();
			}
		} else {
			mark_bt.setVisibility(View.GONE);
		}
	}

	class AsyncShowGraphicTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressBar(true);
			isShowGraphic = true;
		}

		@Override
		protected String doInBackground(String... params) {

			String OBJECTID = URLHelper.queryStringForGet(params[0]).split(",")[0];
			try {
				Query q = new Query();
				q.setReturnGeometry(true);
				q.setOutFields(new String[] { "OBJECTID" });
				q.setWhere("OBJECTID=" + OBJECTID);
				q.setInSpatialReference(mMapView.getSpatialReference());
				String query_url = URL + BzdzkActivity.this.mark_url;
				q.setSpatialRelationship(SpatialRelationship.INTERSECTS);
				QueryTask queryTask = new QueryTask(query_url);
				GraphicsLayer graphicsLayer = getGraphicLayer();
				FeatureSet fs = queryTask.execute(q);
				if (fs != null && graphicsLayer.isInitialized() && graphicsLayer.isVisible()) {
					Graphic[] grs = fs.getGraphics();
					if (grs.length > 0) {
						SimpleFillSymbol symbol = new SimpleFillSymbol(Color.RED);
						graphicsLayer.setRenderer(new SimpleRenderer(symbol));
						graphicsLayer.removeAll();
						if (isShowGraphic) {
							graphicsLayer.addGraphics(grs);
							isShowGraphic = false;
						}
					}
				}
				return "success";
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (!"success".equals(result)) {
				new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setMessage(R.string.show_graphic_fasle).setPositiveButton(R.string.know, null).create().show();
			}
			if (!isShowGraphic) {
				showProgressBar(false);
			}
		}

	}

	class AsyncUnMarkTask extends AsyncTask<String, Void, String> {
		private ProgressDialog progress;
		private TreeBean tree;

		public AsyncUnMarkTask(TreeBean tree) {
			this.tree = tree;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress = new ProgressDialog(BzdzkActivity.this);
			progress.setCancelable(false);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(BzdzkActivity.this.getString(R.string.un_marking));
			progress.show();
		}

		@Override
		protected String doInBackground(String... params) {
			return URLHelper.queryStringForGet(params[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if ("success".equals(result)) {
				if (view != null) {
					ImageView iv = (ImageView) view.findViewById(R.id.flag);
					// iv.setVisibility(View.INVISIBLE);
					iv.setImageBitmap(null);
					tree.setMark(false);
					showMark();
					removeGraphic();
				}
				new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setMessage(R.string.un_mark_success).setPositiveButton(R.string.know, null).create().show();
			} else {
				GraphicsLayer graphicsLayer = getGraphicLayer();
				graphicsLayer.removeAll();
				new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setMessage(R.string.un_mark_fasle).setPositiveButton(R.string.know, null).create().show();
			}
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

		}

	}

	class AsyncMarkTask extends AsyncTask<String, Void, String> {

		private ProgressDialog progress;
		private TreeBean tree;
		private Graphic gr;

		public AsyncMarkTask(TreeBean tree, Graphic gr) {
			this.tree = tree;
			this.gr = gr;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progress = new ProgressDialog(BzdzkActivity.this);
			progress.setCancelable(false);
			progress.setCanceledOnTouchOutside(false);
			progress.setMessage(BzdzkActivity.this.getString(R.string.marking));
			progress.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String SUCCESS = null;
			if (mark_tree != null) {

				String json = URLHelper.queryStringForGet(URLHelper.BASE + "getMapconfig?zhid=" + mark_tree.getId() + "&yhid=" + yhid);
				String tcmc = JSONHelper.getTcmcFromJson(json);

				String mark_url = params[0] + "?zhid=" + mark_tree.getId() + "&tcid=" + params[1] + "&tcmc=" + tcmc + "&zxPoint=" + params[2] + "&yhid=" + yhid;

				SUCCESS = URLHelper.queryStringForGet(mark_url);
				if ("success".equals(SUCCESS)) {
					try {
						GraphicsLayer graphicsLayer = getGraphicLayer();
						graphicsLayer.removeAll();
						graphicsLayer.addGraphic(gr);

						// Query q = new Query();
						// q.setReturnGeometry(true);
						// q.setOutFields(new String[] { "OBJECTID" });
						// q.setWhere("OBJECTID=" + params[1]);
						// q.setInSpatialReference(mMapView.getSpatialReference());
						// String query_url = URL + BzdzkActivity.this.mark_url;
						// q.setSpatialRelationship(SpatialRelationship.INTERSECTS);
						// QueryTask queryTask = new QueryTask(query_url);
						// GraphicsLayer graphicsLayer = getGraphicLayer();
						// FeatureSet fs = queryTask.execute(q);
						// if (fs != null && graphicsLayer.isInitialized() &&
						// graphicsLayer.isVisible()) {
						// Graphic[] grs = fs.getGraphics();
						// if (grs.length > 0) {
						// SimpleFillSymbol symbol = new
						// SimpleFillSymbol(Color.RED);
						// graphicsLayer.setRenderer(new
						// SimpleRenderer(symbol));
						// graphicsLayer.removeAll();
						// graphicsLayer.addGraphics(grs);
						// }
						// }
					} catch (Exception e) {
						e.printStackTrace();
					}

					return "success";
				}
			}
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if ("success".equals(result)) {
				if (view != null) {
					ImageView iv = (ImageView) view.findViewById(R.id.flag);
					iv.setImageResource(R.drawable.ic_launcher);
					tree.setMark(true);
					showUnMark();
				}
				new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setMessage(R.string.success).setPositiveButton(R.string.know, null).create().show();
			} else {
				new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setMessage(R.string.fasle).setPositiveButton(R.string.know, null).create().show();
			}
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}

		}

	}

	private class AsyncQueryTask extends AsyncTask<Object, Void, FeatureSet> {

		private ProgressDialog progress;

		protected void onPreExecute() {
			progress = ProgressDialog.show(BzdzkActivity.this, "", "Please wait....query task is executing");

		}

		@Override
		protected FeatureSet doInBackground(Object... queryParams) {
			// if (queryParams == null || queryParams.length <= 1)
			// return null;
			Query query = (Query) queryParams[0];
			String queryUrl = (String) queryParams[1];
			QueryTask queryTask = new QueryTask(queryUrl);

			FeatureSet fs = null;
			try {
				fs = queryTask.execute(query);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, e.getMessage());
			}
			return fs;

		}

		protected void onPostExecute(FeatureSet fs) {
			GraphicsLayer graphicsLayer = getGraphicLayer();
			String message = "No result comes back";
			if (fs != null && graphicsLayer.isInitialized() && graphicsLayer.isVisible()) {
				Graphic[] grs = fs.getGraphics();
				if (grs.length > 0) {
					SimpleFillSymbol symbol = new SimpleFillSymbol(Color.RED);
					graphicsLayer.setRenderer(new SimpleRenderer(symbol));
					graphicsLayer.removeAll();
					graphicsLayer.addGraphics(grs);

					message = (grs.length == 1 ? "1 result has " : Integer.toString(grs.length) + " results have ") + "come back";
				}

			}
			Toast toast = Toast.makeText(BzdzkActivity.this, message, Toast.LENGTH_LONG);
			toast.show();

			progress.dismiss();

		}

	}

	class ButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (((Integer) v.getTag())) {
			case UN_MARK:
				if (mark_tree != null) {
					alert(mark_tree, UN_MARK, R.string.confirm_un_mark);
				}
				break;
			case MARK:
				mMapView.setOnTouchListener(touchListener);
				break;
			}
		}
	}

	class DialogClickListener implements DialogInterface.OnClickListener {
		private TreeBean tree;
		private int flag;
		private int[] uids;
		private float x, y;

		public DialogClickListener(TreeBean tree, int flag) {
			this.tree = tree;
			this.flag = flag;
		}

		public DialogClickListener(TreeBean tree, int flag, int[] uids, float x, float y) {
			this.tree = tree;
			this.flag = flag;
			this.uids = uids;
			this.x = x;
			this.y = y;
		}

		public void onClick(DialogInterface dialog, int which) {
			switch (flag) {
			case UN_MARK:
				new AsyncUnMarkTask(tree).execute(URLHelper.BASE + "qxbz" + "?zhid=" + tree.getId() + "&yhid=" + yhid);
				break;
			case MARK:
				int targetId = uids[0];
				Graphic gr = mGraphicsLayer.getGraphic(targetId);
				Map<String, Object> m = gr.getAttributes();
				Point pt = mMapView.toMapPoint(x, y);
				new AsyncMarkTask(tree, gr).execute(URLHelper.BASE + "bz", m.get("OBJECTID") + "", pt.getX() + "," + pt.getY());
				break;
			}
		}
	}

	class MyTouchListener extends MapOnTouchListener {

		Graphic g;
		// first point clicked on the map
		Point p0 = null;
		int uid = -1;

		private Envelope envelope;

		public MyTouchListener(Context arg0, MapView arg1) {
			super(arg0, arg1);
		}

		public boolean onDragPointerMove(MotionEvent from, MotionEvent to) {
			if (uid == -1) { // first time
				g = new Graphic(null, sfs);
				p0 = mMapView.toMapPoint(from.getX(), from.getY());
				uid = getGraphicLayer().addGraphic(g);

			} else {

				Point p2 = mMapView.toMapPoint(new Point(to.getX(), to.getY()));
				envelope = new Envelope();
				envelope.merge(p0);
				envelope.merge(p2);
				getGraphicLayer().updateGraphic(uid, envelope);

			}

			return true;

		}

		public boolean onDragPointerUp(MotionEvent from, MotionEvent to) {
			GraphicsLayer mGraphicsLayer = getGraphicLayer();
			if (uid != -1) {
				g = mGraphicsLayer.getGraphic(uid);
				if (g != null && g.getGeometry() != null) {
					Query q = new Query();

					q.setReturnGeometry(true);
					q.setOutFields(new String[] { "OBJECTID" });

					q.setInSpatialReference(mMapView.getSpatialReference());
					q.setGeometry(g.getGeometry());
					// String query_url = URL + "/3";
					q.setSpatialRelationship(SpatialRelationship.INTERSECTS);
					new AsyncQueryTask().execute(q, URL + mark_url);
				}
				mGraphicsLayer.removeAll();

				mMapView.setOnTouchListener(defaulttouchListener);

			}

			p0 = null;
			// Resets it
			uid = -1;
			return true;

		}

	}

	protected void statusChange(Object source, STATUS status) {

		if (source == mMapView && status == STATUS.INITIALIZED) {
			LocationService locService = mMapView.getLocationService();
			locService.setAutoPan(false);
			locService.setAccuracyCircleOn(true);

			// 监听
			locService.setLocationListener(new LocationListener() {

				boolean locationChanged = false;

				// Zooms to the current location when first GPS fix arrives.
				public void onLocationChanged(Location loc) {
					if (!locationChanged) {
						locationChanged = true;
						double locy = loc.getLatitude();
						double locx = loc.getLongitude();
						Log.i("andli", locx + "," + locy);

						Point wgspoint = new Point(locx, locy);
						// GPS坐标转ArcGis坐标
						Point mapPoint = (Point) GeometryEngine.project(wgspoint, SpatialReference.create(4326), mMapView.getSpatialReference());
						Unit mapUnit = mMapView.getSpatialReference().getUnit();
						if (isNew) {
							mMapView.centerAt(mapPoint, true);
							isNew = false;
						}
						// double zoomWidth = Unit.convertUnits(SEARCH_RADIUS,
						// Unit.create(LinearUnit.Code.MILE_US), mapUnit);
						// Envelope zoomExtent = new Envelope(mapPoint,
						// zoomWidth, zoomWidth);
						// mMapView.setExtent(zoomExtent);

					}

				}

				public void onProviderDisabled(String arg0) {

				}

				public void onProviderEnabled(String arg0) {
				}

				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {

				}
			});
			// 开启服务
			locService.start();

		}

	}

	public void removeGraphic() {
		isShowGraphic = false;
		getGraphicLayer().removeAll();
	}

	private void showGraphicOnMap(TreeBean tree) {
		removeGraphic();
		if (tree.getMark() && canMark(tree.getId().split(",")[0])) {
			String url = URLHelper.BASE + "getTcxx?zhid=" + tree.getId() + "&yhid=" + yhid;
			new AsyncShowGraphicTask().execute(url);
		}

	}

	private void findAndSetListener() {
		mMapView = (MapView) findViewById(R.id.map);
		mark_bt = (Button) findViewById(R.id.mark);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(Object source, STATUS status) {
				statusChange(source, status);
			}
		});

		ButtonClickListener bc = new ButtonClickListener();
		// extent_bt.setOnClickListener(bc);
		mark_bt.setOnClickListener(bc);
		touchListener = new MyTouchListener(this, mMapView);
		defaulttouchListener = new MapOnTouchListener(this, mMapView);

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				if (!mMapView.isLoaded())
					return;
				mGraphicsLayer = getGraphicLayer();
				int[] uids = mGraphicsLayer.getGraphicIDs(x, y, 2);
				if (uids != null && uids.length > 0 && isCanMark) {
					alert(mark_tree, MARK, R.string.confirm_mark, uids, x, y);
				} else {
					// Toast.makeText(BzdzkActivity.this, "22222222", 0).show();
				}
			}
		});
	}

	private void alert(TreeBean tree, int flag, int message, int[] uids, float x, float y) {
		new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setNegativeButton(R.string.negative, null).setNegativeButton(R.string.negative, null).setMessage(message)
				.setPositiveButton(R.string.positive, new DialogClickListener(tree, flag, uids, x, y)).create().show();
	}

	/**
	 * 弹出框
	 * 
	 * @param tree
	 * @param flag
	 * @param message
	 */
	private void alert(TreeBean tree, int flag, int message) {
		new AlertDialog.Builder(BzdzkActivity.this).setTitle(R.string.title).setNegativeButton(R.string.negative, null).setNegativeButton(R.string.negative, null).setMessage(message)
				.setPositiveButton(R.string.positive, new DialogClickListener(tree, flag)).create().show();
	}

	private void showProgressBar(boolean show) {
		if (show) {
			progressBar.setVisibility(View.VISIBLE);
		} else {
			progressBar.setVisibility(View.GONE);
		}
	}

	private void showMark() {
		mark_bt.setText(R.string.extent);
		mark_bt.setTag(MARK);
		isCanMark = true;
	}

	private void showUnMark() {
		mark_bt.setText(R.string.un_mark);
		mark_bt.setTag(UN_MARK);
		isCanMark = false;
	}

	private GraphicsLayer getGraphicLayer() {
		if (mGraphicsLayer == null) {
			mGraphicsLayer = new GraphicsLayer();
			mMapView.addLayer(mGraphicsLayer);
		}
		return mGraphicsLayer;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		TreeHelper.release();
		yhid = null;
		name = null;
	}

	@Override
	protected void onPause() {
		super.onPause();
		mMapView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.unpause();
	}

	public void hiddenButton() {
		mark_bt.setVisibility(View.GONE);
	}

	public void back(View view) {
		getGraphicLayer().removeAll();
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();
		if (fm.getBackStackEntryCount() == 0)
			menu.toggle();
		if (mark_tree != null) {
			TreeFragment tfm = (TreeFragment) fm.findFragmentByTag(mark_tree.getSjid());
			if (tfm != null)
				mark_tree = tfm.getParent_tree();
			if (mark_tree != null) {
				String type = mark_tree.getId().split(",")[0];
				if (canMark(type)) {
					mark_bt.setVisibility(View.VISIBLE);
					if (mark_tree.getMark()) {
						showUnMark();
					} else {
						showMark();
					}
				} else {
					isCanMark = false;
					mark_bt.setVisibility(View.GONE);
				}
			}
		}
	}

	private boolean canMark(String type) {
		Log.e(TAG, type);
		return !(type.equals("zrq") || type.equals("jlx") || type.equals("zrc"));
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.toggle();
		} else {
			exitBy2Click();
		}

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