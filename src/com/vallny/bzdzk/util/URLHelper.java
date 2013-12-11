package com.vallny.bzdzk.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class URLHelper {
	
	public static final String BASE = "http://192.168.1.101:8080/bzdzk/dzcj/";
	
	
	// 责任区
	public static final String ZRQ = BASE+"mjcjAndroidTree";
	// 街路巷
	public static final String JLX = "";
	// 小区
	public static final String XQ = "";
	// 建筑物
	public static final String JZW = "";
	// 自然村
	public static final String ZRC = "";
	// 门牌号
	public static final String MPH = "";
	// 单元
	public static final String DY = "";
	// 房间
	public static final String FJ = "";
	
	
	public static String queryStringForPost(String url, Map<String, String> params) throws HttpException {
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		String result = null;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "UTF-8");
			HttpPost httpost = new HttpPost(url);
			httpost.setEntity(entity);
			HttpResponse response = new CustomHttpClient().execute(httpost);
			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		}  catch (ClientProtocolException e) {
			e.printStackTrace();
			result = "连接错误！";
		} catch (IOException e) {
			e.printStackTrace();
			result = "连接错误！";
		}

		return result;
	}

	public static String queryStringForGet(String url) {
		HttpGet httpGet = new HttpGet(url);
		String result = "连接错误！";
		try {
			HttpResponse response = new CustomHttpClient().execute(httpGet);

			if (response.getStatusLine().getStatusCode() == 200) {
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return result;		
	}


}
