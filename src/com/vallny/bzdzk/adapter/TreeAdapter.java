package com.vallny.bzdzk.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vallny.bzdzk.R;
import com.vallny.bzdzk.R.drawable;
import com.vallny.bzdzk.R.id;
import com.vallny.bzdzk.R.layout;
import com.vallny.bzdzk.bean.TreeBean;

public class TreeAdapter extends BaseAdapter {

	public static final int GRIDLAYOUT_CHILDVIEW_COUNT = 9;

	private List<TreeBean> treeList;
	private LayoutInflater mInflater;
	private Context context;

	public TreeAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		treeList = new ArrayList<TreeBean>();
		this.context = context;
	}

	public void addItemList(List<TreeBean> msgList) {
		this.treeList.addAll(msgList);
	}

	public List<TreeBean> getItemList() {

		return treeList;
	}

	@Override
	public int getCount() {
		return treeList.size();
	}

	@Override
	public Object getItem(int position) {
		return treeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.tree_list_item, null);
			holder.flag = (ImageView) convertView.findViewById(R.id.flag);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		TreeBean tree = treeList.get(position);
		if (tree.getMark()) {
			holder.flag.setImageResource(R.drawable.mark);
		} else {
			holder.flag.setImageBitmap(null);
		}
		holder.name.setText(tree.getName());

		return convertView;
	}

	private class ViewHolder {

		private ImageView flag;
		private TextView name;

	}
}
