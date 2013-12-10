package com.vallny.bzdzk.util;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class CustomHttpClient extends DefaultHttpClient{
	
	private static final int REQUEST_TIMEOUT = 10*1000;//设置请求超时10秒钟  
	private static final int SO_TIMEOUT = 10*1000;  //设置等待数据超时时间10秒钟  
	
	private static HttpParams httpParameters;  
	
	static{
		 httpParameters = new BasicHttpParams();
		 HttpConnectionParams.setConnectionTimeout(httpParameters, REQUEST_TIMEOUT);
		 HttpConnectionParams.setSoTimeout(httpParameters, SO_TIMEOUT);
	}
	
	public CustomHttpClient(){
		super(httpParameters);
	}
}
