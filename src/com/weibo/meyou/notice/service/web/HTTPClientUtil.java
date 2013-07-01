package com.weibo.meyou.notice.service.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.log4j.Logger;

public class HTTPClientUtil {
	private static Logger log = Logger.getLogger(HTTPClientUtil.class);
	private static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	private static 	HttpClient httpClient = null;
	
	public String requestGetUrl(String url, Map<String,String> headers) {
		HttpClient httpClient = getHttpClient();
		GetMethod getMethod = new GetMethod(url);

    	if (headers != null && !headers.isEmpty()) {
  			for (Map.Entry<String, String> entry : headers.entrySet()) {
  				getMethod.setRequestHeader(entry.getKey(), entry.getValue());
			}
  		}

    	String result = null;

		try {
			int code = httpClient.executeMethod(getMethod);
			result = extractResponseBody(getMethod);

			if(code != 200) {
				log.warn("GroupApi request url failed caused [code: " + code +
					"] by " + result);
				return null;
			}
		}
		catch(Exception e) {
			log.error("Error: when getReponseBody from url:" + getMethod.getPath(), e);
		}
		finally{
			getMethod.releaseConnection();
		}

		return result;
	}
	
	public static synchronized  HttpClient getHttpClient() {
		if(httpClient == null){
			HttpConnectionManagerParams params = connectionManager.getParams();
			params.setDefaultMaxConnectionsPerHost(150);
			params.setConnectionTimeout(1000);
			params.setSoTimeout(1000);
			httpClient = new HttpClient(connectionManager);
			if(log.isDebugEnabled()){
				log.debug("httpClient init....");
			}
		}
		
		return httpClient;
	}
	
	private static String extractResponseBody(HttpMethod httpMethod) throws IOException {
		InputStream instream = httpMethod.getResponseBodyAsStream();
		if(instream != null) {
			int contentLength = getResponseContentLength(httpMethod);
			ByteArrayOutputStream outstream = contentLength < 0 ?
				new ByteArrayOutputStream() :
				new ByteArrayOutputStream(contentLength);
			byte[] buffer = new byte[1024];
			int len = 0;
			while((len = instream.read(buffer)) > 0){
				outstream.write(buffer, 0, len);
			}
			String content = new String(outstream.toByteArray(), "utf-8");
			outstream.close();
			return content;
		}
		return null;
	}

	private static int getResponseContentLength(HttpMethod httpMethod) {
    	Header[] headers = httpMethod.getResponseHeaders("Content-Length");
    	if(headers.length == 0) {
    		return -1;
    	}
    	if(headers.length > 1) {
    		log.info("Multiple content-length headers detected" + ",url:" + httpMethod.getPath());
    	}

    	for(int i = headers.length - 1; i >= 0; i--) {
    		Header header = headers[i];
    		try {
				return Integer.parseInt(header.getValue());
			}
    		catch (NumberFormatException e) {
				log.error("Invalid content-length value:" + e.getMessage() + ",url:" + httpMethod.getPath());
			}
    	}

        return -1;
    }
}
