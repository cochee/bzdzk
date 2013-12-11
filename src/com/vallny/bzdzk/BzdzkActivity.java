package com.vallny.bzdzk;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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
import com.vallny.bzdzk.TreeFragment.OnLayerListener;
import com.vallny.bzdzk.bean.TreeBean;
import com.vallny.bzdzk.util.TreeHelper;
import com.vallny.bzdzk.util.URLHelper;

public class BzdzkActivity extends SherlockFragmentActivity {

	private MapView mMapView;
	private ArcGISDynamicMapServiceLayer dLayer;
	private Button query_bt;
	private Button extent_bt;
	private GraphicsLayer mGraphicsLayer;
	private SimpleFillSymbol sfs;

	private MapOnTouchListener defaulttouchListener;
	private MyTouchListener touchListener;

	private SlidingMenu menu;
	private boolean isNew = true;

	private ArrayList<TreeBean> list;

	private String mark_url;
	private final static String URL = "http://192.168.1.101:6080/arcgis/rest/services/PGIS/bzdzk/MapServer";
//	 private final static String URL =
//	 "http://192.168.1.101:6080/arcgis/rest/services/PGIS/XZQH/MapServer";
	private final static String TAG = "com.vallny.bzdzk";

	public final static double SEARCH_RADIUS = 5;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main);
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

		TreeHelper.getInstance(this, true).initTree(URLHelper.BASE);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgress(Window.PROGRESS_END);
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			menu.toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void findAndSetListener() {
		mMapView = (MapView) findViewById(R.id.map);
		// condition = (EditText) findViewById(R.id.condition);
		query_bt = (Button) findViewById(R.id.query);
		extent_bt = (Button) findViewById(R.id.extent);

		mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
			public void onStatusChanged(Object source, STATUS status) {
				statusChange(source, status);
			}
		});

		ButtonClickListener bc = new ButtonClickListener();
		query_bt.setOnClickListener(bc);
		extent_bt.setOnClickListener(bc);
		touchListener = new MyTouchListener(this, mMapView);
		defaulttouchListener = new MapOnTouchListener(this, mMapView);

		mMapView.setOnSingleTapListener(new OnSingleTapListener() {

			private static final long serialVersionUID = 1L;

			public void onSingleTap(float x, float y) {
				if (!mMapView.isLoaded())
					return;
				mGraphicsLayer = getGraphicLayer();
				int[] uids = mGraphicsLayer.getGraphicIDs(x, y, 2);
				if (uids != null && uids.length > 0) {
					int targetId = uids[0];
					Graphic gr = mGraphicsLayer.getGraphic(targetId);
					Map<String,Object> m = gr.getAttributes();
					
					Log.e(TAG,m .get("OBJECTID")+"%%%%%%%%%%%%%");
					for(Entry<String, Object> entry:m.entrySet()){
						Log.e(TAG, entry.getKey()+"**********"+entry.getValue());
					}
					
					Toast.makeText(BzdzkActivity.this, "1111111", 0).show();
				} else {
					Toast.makeText(BzdzkActivity.this, "22222222", 0).show();
				}
			}
		});
	}

	
	public Button getExtent_bt(){
		return extent_bt;
	}
	public void setMark_url(String mark_url){
		this.mark_url = mark_url;
	}
	
	
		public void updateLayer(String layer) {
			if ("xq".equals(layer)) {
				mark_url = "/13";
				extent_bt.setVisibility(View.VISIBLE);
			} else if ("mph".equals(layer)) {
				mark_url = "/12";
				extent_bt.setVisibility(View.VISIBLE);
			} else if ("jzw".equals(layer)) {
				mark_url = "/11";
				extent_bt.setVisibility(View.VISIBLE);
			} else if ("dy".equals(layer)) {
				mark_url = "/11";
				extent_bt.setVisibility(View.VISIBLE);
			} else if ("fj".equals(layer)) {
				mark_url = "/11";
				extent_bt.setVisibility(View.VISIBLE);
			} else {
				extent_bt.setVisibility(View.GONE);
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
					q.setOutFields(new String[] { "OBJECTID"});
					
					q.setInSpatialReference(mMapView.getSpatialReference());
					q.setGeometry(g.getGeometry());
					// String query_url = URL + "/3";
					q.setSpatialRelationship(SpatialRelationship.INTERSECTS);
					new AsyncQueryTask().execute(q, URL + mark_url);
//					new AsyncQueryTask().execute(q, URL+"/3");

					// fLayer.selectFeatures(q, SELECTION_METHOD.NEW, callback);
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

	class ButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.query:
				// String keyQuery = condition.getText().toString();
				// Query query = new Query();
				// query.setWhere("MC like '%" + keyQuery + "%'");
				// query.setReturnGeometry(true);
				// String query_url = URL + "/4";
				// new AsyncQueryTask().execute(new Object[] { query, query_url
				// });

				break;
			case R.id.extent:
				mMapView.setOnTouchListener(touchListener);
				break;

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
				
				
					Map<String,Object> m = grs[0].getAttributes();
					
					Log.i(TAG,m .get("OBJECTID")+"%%%%%%%%%%%%%");
					for(Entry<String, Object> entry:m.entrySet()){
						Log.i(TAG, entry.getKey()+"**********"+entry.getValue());
					}

					message = (grs.length == 1 ? "1 result has " : Integer.toString(grs.length) + " results have ") + "come back"+m .get("OBJECTID");
				}

			}
			Toast toast = Toast.makeText(BzdzkActivity.this, message, Toast.LENGTH_LONG);
			toast.show();

			progress.dismiss();

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

	public void back(View view) {
		getSupportFragmentManager().popBackStack();
	}

	@Override
	public void onBackPressed() {
		if (menu.isMenuShowing()) {
			menu.toggle();
		} else {
			super.onBackPressed();
		}

	}

}