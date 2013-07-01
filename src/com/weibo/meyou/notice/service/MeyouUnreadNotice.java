package com.weibo.meyou.notice.service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

import cn.sina.api.commons.util.JsonWrapper;

import com.weibo.meyou.notice.service.web.HTTPClientUtil;
import com.weibo.meyou.notice.service.tauth.TAuthUtil;

public class MeyouUnreadNotice {
	private static Logger log = Logger.getLogger("notify_service");
	private HTTPClientUtil httpclient = new HTTPClientUtil();
	private NoticeServiceManager service;
	private String unreadUrl = "http://api.t.sina.com.cn/remind/close_friends/unread_count.xml/json";
	private List<String> users = new ArrayList<String>();
	private LinkedBlockingQueue<NoticeData> notices = new LinkedBlockingQueue<NoticeData>(); 
	
	public MeyouUnreadNotice(NoticeServiceManager service) {
		this.service = service;
	}
	
	public void start() {
		new UnreadChcker().start();
		new NoticeSender().start();
	}
	
	public String getUnreadUrl() {
		return unreadUrl;
	}

	public void setUnreadUrl(String unreadUrl) {
		this.unreadUrl = unreadUrl;
	}

	public void registerUser(String uid) {
		if(!users.contains(uid)) {
			users.add(uid);
		}
	}
	
	private class UnreadChcker extends Thread {
		public void run() {
			while(true) {
				try {
					long t1 = System.currentTimeMillis();
					
					for(String uid : users) {
						String appkey = TAuthUtil.getInstance().getAppKey();
						Map<String,String> headers = new HashMap<String,String>();
						headers.put("authorization", TAuthUtil.getInstance().getToken(uid, ""));
						headers.put("source", appkey);
						String url = unreadUrl + "?source=" + appkey;
						String ret = httpclient.requestGetUrl(url, headers);
						JsonWrapper json = new JsonWrapper(ret);
						int close_friends_feeds = json.getInt("close_friends_feeds");
						int close_friends_common_cmt = json.getInt("close_friends_common_cmt");
						int close_friends_invite = json.getInt("close_friends_invite");
						
						if(close_friends_feeds > 0 || close_friends_common_cmt > 0 || close_friends_invite > 0) {
							NoticeData data = new NoticeData();
							data.json = json;
							data.touid = uid;
							notices.put(data);
							
							if(log.isDebugEnabled()) {
								log.debug(close_friends_feeds +"|"+ close_friends_common_cmt +"|"+ close_friends_invite);
							}
						}
					}
					
					long t2 = System.currentTimeMillis();
					log.info("query meyou unread " + users.size() + " times using " + ((t2 - t1) / 1000) + "s");
					
					// min loop is 30s
					if(t2 - t1 < 30000) {
						Thread.sleep(30000 - (t2 -t1));
					}
				}
				catch(Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private class NoticeSender extends Thread {
		public void run() {
			while(true) {
				try {
					NoticeData data = notices.take();
//					service.sendNotice(WeSyncUtil.unread, data.touid, data.json.toString());
				}
				catch(Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private class NoticeData {
		JsonWrapper json;
		String touid;
	}
}
