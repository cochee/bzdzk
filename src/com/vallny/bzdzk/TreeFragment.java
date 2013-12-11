package com.vallny.bzdzk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.vallny.bzdzk.bean.TreeBean;
import com.vallny.bzdzk.util.TreeHelper;
import com.vallny.bzdzk.util.JSONHelper;
import com.vallny.bzdzk.util.URLHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class TreeFragment extends SherlockFragment implements OnRefreshListener2<ListView>, OnItemClickListener, OnItemLongClickListener {

	private final static String TAG = "com.vallny.bzdzk";
	private View view;
	private PullToRefreshListView mPullRefreshListView;
	private BzdzkActivity activity;

	// private Context context;
	private TreeAdapter treeAdapter;

	private int pageNo = 1;

	private ArrayList<TreeBean> itemList = new ArrayList<TreeBean>();
	private ArrayList<TreeBean> list;

	protected boolean pauseOnScroll = false;
	protected boolean pauseOnFling = true;

	private Boolean isOnline;

	private String url;

	public TreeFragment() {
	}

	public TreeFragment(String url, ArrayList<TreeBean> list) {
		this.list = list;
		this.url = url;
	}

	public TreeFragment(ArrayList<TreeBean> list) {
		this.list = list;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (view == null) {
			view = inflater.inflate(R.layout.tree_list, null);

			mPullRefreshListView = (PullToRefreshListView) view.findViewById(R.id.pull_refresh_list);
			treeAdapter = new TreeAdapter(activity);
			treeAdapter.setItemList(list);
			mPullRefreshListView.setAdapter(treeAdapter);
			// mPullRefreshListView.setRefreshing();
			mPullRefreshListView.setOnRefreshListener(this);
			mPullRefreshListView.getRefreshableView().setOnItemClickListener(this);
			mPullRefreshListView.getRefreshableView().setOnItemLongClickListener(this);
			mPullRefreshListView.getRefreshableView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			// pageNo = 1;

			// new AsyncTreeTask(pageNo, false).execute(url);

		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (BzdzkActivity) activity;
	}

	class AsyncTreeTask extends AsyncTask<String, Void, Void> {

		private long pageNo;
		private boolean isNew;
		private ArrayList<TreeBean> newList;

		public AsyncTreeTask(long pageNo, boolean isNew) {
			this.pageNo = pageNo;
			this.isNew = isNew;
		}

		@Override
		protected Void doInBackground(String... params) {
			isOnline = true;
			String json = URLHelper.queryStringForGet(params[0]);
			if (TextUtils.isEmpty(json) || json.equals("连接错误！")) {
				isOnline = false;
			} else {
				newList = (ArrayList<TreeBean>) JSONHelper.JSON2List(json);
			}
			if (isNew) {
				itemList.clear();
			}
			itemList.addAll(newList);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			if (isOnline) {
				treeAdapter.setItemList(itemList);
				treeAdapter.notifyDataSetChanged();

			} else {
				Toast.makeText(activity, R.string.online_error, Toast.LENGTH_LONG).show();
			}
			mPullRefreshListView.onRefreshComplete();

		}

	}

	/**
	 * 点击
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.e(TAG, position + "****" + id + "Click");
		mPullRefreshListView.getRefreshableView().setItemChecked(position, true);

		TreeBean tree = (TreeBean) parent.getAdapter().getItem(position);
		if (canMark(tree)) {
			activity.setSupportProgress(Window.PROGRESS_END);
			activity.setSupportProgressBarIndeterminateVisibility(true);

			String layer = tree.getId().split(",")[0];
			activity.updateLayer(layer,tree,view);
			Toast.makeText(activity, ((TextView)view.findViewById(R.id.name)).getText().toString(), 0).show();

		}

	}

	private boolean canMark(TreeBean tree) {
		String type = tree.getId().split(",")[0];
		Log.e(TAG,type);
		return !(type.equals("zrq") || type.equals("jlx") || type.equals("zrc"));
	}

	/**
	 * 下拉
	 */
	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		pageNo = 1;
		// new AsyncTreeTask(pageNo, true).execute();
	}

	/**
	 * 上拉
	 */
	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		pageNo++;
		// new AsyncTreeTask(pageNo, false).execute();
	}

	/**
	 * 长按
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {

		Log.e(TAG, position + "****" + id + "LongClick");

		TreeBean tree = (TreeBean) arg0.getAdapter().getItem(position);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		String url = URLHelper.ZRQ + "?sjid=" + tree.getId();
		TreeHelper.getInstance(activity, false).initTree(url);

		return true;
	}

	


}
