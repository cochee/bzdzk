package com.vallny.bzdzk.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vallny.bzdzk.bean.TreeBean;

public class JSONHelper {

	public static List<TreeBean> JSON2List(String json) {
		List<TreeBean> list = new ArrayList<TreeBean>();
		try {
			JSONArray jsonArray = new JSONArray(json);
			for (int i = 0; i < jsonArray.length(); i++) {
				TreeBean tree = new TreeBean();
				JSONObject item = jsonArray.getJSONObject(i); // 每条记录又由几个Object对象组成
				String id = item.getString("id"); // 获取对象对应的值
				String name = item.getString("name"); // 获取对象对应的值
				String isParent = item.getString("isParent"); // 获取对象对应的值
				boolean mark = item.getBoolean("mark"); // 获取对象对应的值
				String sjid = "";
				try {
					sjid = item.getString("pId");
				} catch (Exception e) {
					e.printStackTrace();
				}
				tree.setId(id);
				tree.setName(name);
				tree.setSjid(sjid);
				tree.setMark(mark);
				tree.setIsParent(isParent);
				list.add(tree);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取图层名称
	 * 
	 * @param json
	 * @return
	 */
	public static String getTcmcFromJson(String json) {
		try {
			JSONArray jsonArray = new JSONArray(json);
			JSONObject item = jsonArray.getJSONObject(0); // 每条记录又由几个Object对象组成
			return item.getString("tcmc");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

}
