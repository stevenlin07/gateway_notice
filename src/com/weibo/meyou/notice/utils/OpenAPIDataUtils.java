package com.weibo.meyou.notice.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonNode;
import org.json.JSONObject;

import cn.sina.api.commons.util.JsonBuilder;
import cn.sina.api.commons.util.JsonWrapper;

import com.weibo.meyou.notice.model.UnreadInfo;
import com.weibo.meyou.notice.service.tauth.TAuthUtil;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-14
 *
 */
public class OpenAPIDataUtils {
	private static final Logger log=Logger.getLogger(OpenAPIDataUtils.class);
	public static final OpenAPIDataUtils instance=new OpenAPIDataUtils();
	private String iosClientAppKey = "2841080378";
	private MultiThreadedHttpConnectionManager httpConnManager;
	private HttpClient client = null;
	private TAuthUtil tauth = TAuthUtil.getInstance();
	
	private static final String URL_USER_SHOW_BATCH = "http://i2.api.weibo.com/2/users/show_batch.json";
	
	private static final String URL_USER_SHOW = "http://i2.api.weibo.com/2/users/show.json";
	
	private static final String URL_FRIENDS_BILATERAL_IDS = "http://i2.api.weibo.com/2/friendships/friends/bilateral/ids.json";
	
	private OpenAPIDataUtils(){
		httpConnManager = new MultiThreadedHttpConnectionManager();
		httpConnManager.getParams().setMaxConnectionsPerHost(
				HostConfiguration.ANY_HOST_CONFIGURATION, 50);
		httpConnManager.getParams().setMaxTotalConnections(
				200);
		httpConnManager.getParams().setSoTimeout(1000);
		httpConnManager.getParams().setConnectionTimeout(
				3000);
		client = new HttpClient(httpConnManager);
	}
	
	public UnreadInfo getUserUnread(long userid){
		StringBuilder urlSb = new StringBuilder();
		urlSb.append("http://api.t.sina.com.cn/remind/close_friends/unread_count.json?source=").append(iosClientAppKey);

		String strUid = String.valueOf(userid);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("authorization", tauth.getToken(strUid, iosClientAppKey));
		headers.put("cuid", strUid);
		
		UnreadInfo info = null;
		String data = requestGetUrl(urlSb.toString(), headers, null);
		try {
			JSONObject obj = new JSONObject(data);
			info = new UnreadInfo();
			info.setUserid(userid);
			info.setStatus(obj.getInt("close_friends_feeds"));
			info.setAtComment(obj.getInt("close_friends_mention_cmt"));
			info.setAtStatus(obj.getInt("close_friends_mention_status"));
			info.setAttitude(obj.getInt("close_friends_attitude"));
			info.setComment(obj.getInt("close_friends_cmt"));
			info.setCommonComment(obj.getInt("close_friends_common_cmt"));
			info.setInvite(obj.getInt("close_friends_invite"));
		} catch (Exception e) {
			info = new UnreadInfo();
			log.error("wrong json format get user info,data="+data+",url="+urlSb.toString(), e);
		}
		
		return info;
	}
	
	private Map<String, String> getStandardHeaders(long uid){
		String strUid = String.valueOf(uid);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("authorization", tauth.getToken(strUid, iosClientAppKey));
		headers.put("cuid", strUid);
		
		return headers;
	}
	
	public List<JsonBuilder> callUserShowBath(long cuid, long[] uids){
		List<JsonBuilder> retJbList = new ArrayList<JsonBuilder>();
		
		Map<String, String> headers = this.getStandardHeaders(cuid);
		
		int uidCount = uids.length;
		if(uidCount > 0){
			int perRequestUidSize = 50;	//see http://wiki.intra.weibo.com/1/users/show_batch
			int mod = uidCount % perRequestUidSize;
			
			Map<String, String> httpReqParams = new HashMap<String, String>();
			httpReqParams.put("trim_status", "1");	//do not need status info here
			httpReqParams.put("source", iosClientAppKey);
			
			int startIdx = 0;
			int endIdx = mod - 1;
			while(endIdx < uidCount){
				StringBuilder strUidSb = new StringBuilder();
				for (int j = startIdx; j <= endIdx; j++) {
					strUidSb.append(uids[j]).append(",");
				}
				
				if(strUidSb.length() > 0 ){
					strUidSb.deleteCharAt(strUidSb.length() - 1);	//delete last ","
				}
				
				httpReqParams.put("uids", strUidSb.toString());
				String data = requestGetUrl(URL_USER_SHOW_BATCH, headers, httpReqParams);
				try {
					JsonWrapper retJson = new JsonWrapper(data);
					Iterator<JsonNode> iter = retJson.getJsonNode("users").getElements();
					while(iter.hasNext()){
						JsonNode node = iter.next();
						long uid = node.getFieldValue("id").getLongValue();
						String nick = node.getFieldValue("screen_name").getTextValue();
						String imgUrl = node.getFieldValue("profile_image_url").getTextValue();
						String remark = node.getFieldValue("remark").getTextValue();
						
						JsonBuilder jb = new JsonBuilder();
						jb.append("uid", uid);
						jb.append("screen_name", nick);
						jb.append("profile_image_url", imgUrl);
						jb.append("remark", remark);
						jb.flip();
						
						retJbList.add(jb);
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
				startIdx = endIdx + 1;
				endIdx += perRequestUidSize;
			}
			
		}
		
		return retJbList;
	}
	
	public JsonWrapper callUserShow(long uid){
		Map<String, String> headers = this.getStandardHeaders(uid);
		
		Map<String, String> httpReqParams = new HashMap<String, String>();
		httpReqParams.put("uid", String.valueOf(uid));
		httpReqParams.put("source", iosClientAppKey);
		
		String data = requestGetUrl(URL_USER_SHOW, headers, httpReqParams);
		try {
			return new JsonWrapper(data);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return null;
	}
	
	public long[] getBilateralFriendIds(String fromuid, String source) {
		source = source == null ? iosClientAppKey : source;
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("authorization", tauth.getToken(fromuid, source));
		headers.put("cuid", fromuid);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("uid", fromuid);
		params.put("count", "2000");
		params.put("source", source);
		
		/**
		 * the max count of bilateral friend is 3000,
		 * the max return count of http api is 2000, 
		 * so we will call twice to get all id if necessary 
		 */
		long[] retAry = null;
		int page = 1;
		while(page < 3){
			params.put("page", String.valueOf(page));
			String ret = OpenAPIDataUtils.instance.requestGetUrl(URL_FRIENDS_BILATERAL_IDS, headers, params);
			try {
				JsonWrapper retJson = new JsonWrapper(ret);
				int totalNum = retJson.getInt("total_number");
				long[] uids = retJson.getLongArr("ids");
				
				if(retAry == null){
					if(totalNum <= 2000){
						return uids;
					} else {
						retAry = new long[totalNum];	//init length with totalNum if totalNum > 2000
						System.arraycopy(uids, 0, retAry, 0, 2000);
					}
				} else {//if retAry is not null --> the totalNum of bilateral friends > 2000
					int length = totalNum - 2000;	//last number except 2000
					System.arraycopy(uids, 0, retAry, 2001, length);
				}
				
				++ page;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return retAry;
	}	
	
	private String executeGetMethod(GetMethod method){
		String ret=null;
		byte[] content=null;
		int statusCode=404;
		try {
			statusCode = client.executeMethod(method);

			if (statusCode == HttpStatus.SC_OK) {
				Header contentEncodingHeader = method.getResponseHeader("Content-Encoding");
				if (contentEncodingHeader != null
						&& contentEncodingHeader.getValue().equalsIgnoreCase(
								"gzip")) {
					GZIPInputStream is = new GZIPInputStream(method
							.getResponseBodyAsStream());
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					IOUtils.copy(is, os);
					content = os.toByteArray();
					is.close();
					os.close();
				} else {
					content = method.getResponseBody();
				}
				ret=new String(content,"UTF-8");
			} else {
				log.error("access openapi failed,http code="+statusCode);
			}
		} catch (Exception e) {
			log.error("access openapi failed",e);
		} finally {
			if (method != null){
				method.releaseConnection();
			}
		}
		return ret;
	}
	
	public String requestGetUrl(String url, Map<String,String> headers, Map<String, String> params) {
		GetMethod getMethod = new GetMethod(url);

    	if (headers != null && !headers.isEmpty()) {
  			for (Map.Entry<String, String> entry : headers.entrySet()) {
  				getMethod.setRequestHeader(entry.getKey(), entry.getValue());
			}
  		}
    	
    	addQueryStrings(getMethod, params);

    	String result = executeGetMethod(getMethod);

		return result;
	}
	
	private void addQueryStrings(HttpMethod method, Map<String, String> queryStrs) {
		if (queryStrs != null && !queryStrs.isEmpty()) {
			NameValuePair[] querys = new NameValuePair[queryStrs.size()];

			int i = 0;
			for (Map.Entry<String, String> entry : queryStrs.entrySet()) {
				querys[i++] = new NameValuePair(entry.getKey(), entry.getValue());
			}

			method.setQueryString(querys);
		}
	}
	
	public String requestPostUrl(String url, Map<String,String> headers, Map<String, String> params) {
		PostMethod post = new PostMethod(url);

		if (params != null && !params.isEmpty()) {
  			List<NameValuePair> list = new ArrayList<NameValuePair>(params.size());

  			for (Map.Entry<String, String> entry : params.entrySet()) {
  				if (entry.getKey() != null && !entry.getKey().isEmpty()) {
  					list.add(new NameValuePair(entry.getKey(), entry.getValue()));
  				}
				else {
					try {
						post.setRequestEntity(new StringRequestEntity(entry.getValue(), "text/xml", "utf-8"));
					}
  					catch (UnsupportedEncodingException e) {
						log.warn("requestPostUrl0, post.setRequestEntity failed caused by " + e.getMessage());
					}
				}
			}
  			if (!list.isEmpty())
  				post.setRequestBody(list.toArray(new NameValuePair[list.size()]));
  		}

    	if (headers != null && !headers.isEmpty()) {
  			for (Map.Entry<String, String> entry : headers.entrySet()) {
  				post.setRequestHeader(entry.getKey(),entry.getValue());
			}
  		}

		String result = null;
		try {
			HttpMethodParams param = post.getParams();
    		param.setContentCharset("UTF-8");
			int code = client.executeMethod(post);
			result = extractResponseBody(post);

			if(code != 200) {
				log.warn("request url [" + url + "] failed caused [code: " + code + "] by " + result);
			}
		}
		catch(Exception e) {
			log.error("Error: when getReponseBody from url:" + post.getPath(), e);
		}
		finally{
			post.releaseConnection();
		}

		return result;
	}
	
	private String extractResponseBody(HttpMethod httpMethod) throws IOException {
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
	
	private int getResponseContentLength(HttpMethod httpMethod) {
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
	
	/**
	 * 
	 * @param cuid
	 * @return example {"uid":"3514110801346562","level":"1","type":"1","gender":"n","nick":null,"desc":null,"iconver":"0","album":"0","phone_num":138,"weibo_uid":0,"username":null,"created_at":"2012-11-19 16:12:49"}
	 */
	public List<JsonWrapper> weimiGetGraphBlackList(long cuid){
		String url = "http://180.149.138.89/graph/blacklist";
		Map<String, String> params = new HashMap<String, String>();
		params.put("uid", String.valueOf(cuid));
		
		String ret = this.requestGetUrl(url, null, params);
		List<JsonWrapper> retList = new ArrayList<JsonWrapper>();
		try {
			JsonWrapper retJson = new JsonWrapper(ret);
			Iterator<JsonNode> iter = retJson.getJsonNode("result.users").getElements();
			while(iter.hasNext()){
				JsonNode node = iter.next();
				JsonWrapper userJson = new JsonWrapper(node);
				retList.add(userJson);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return retList;
	}
	
	public static void main(String[] args) {
		//headers.put("authorization", "Token MjYzNTAzNTQ4Mjo4MDAzYjMxZWU0NWM3ZTZiMWE3ZWMxNmVmNGZjMGY0OA==");
//		long[] uids = OpenAPIDataUtils.instance.getBilateralFriendIds(2635035482l + "", null);
//		System.out.println(Arrays.toString(uids));
//		System.out.println("over");
		
		List<JsonBuilder> jbList = OpenAPIDataUtils.instance.callUserShowBath(2635035482l, new long[]{2635035482l, 2420223547l});
		System.out.println(jbList);
	}

}
