package com.weibo.meyou.notice.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.sina.api.commons.util.JsonBuilder;
import cn.sina.api.commons.util.JsonWrapper;

public class UserNotice extends Notice {
	private static Logger log = Logger.getLogger(UserNotice.class);
	private NoticeServiceManager service;
	
	public UserNotice(NoticeServiceManager service) {
		this.service = service;
	}
	
	@Override
	public List<NoticeData> getNotice(JsonWrapper content, Set<String> uniToUidSet) throws Exception {
		String event = content.get("event");
		
		if("add".equals(event)) {
			// log.debug("ignore add event");
		}
		if("update".equals(event)) {
			// don't need to deal with update
		}
		else if("follow".equals(event) || "unfollow".equals(event)) {
			JsonWrapper sourceObj = content.getNode("source");
			JsonWrapper targetObj = content.getNode("target");
			String sourceuid = sourceObj.get("id");
			String targetuid = targetObj.get("id");
			
			if(service.hasMeyou(targetuid)) {
				List list = new ArrayList();
				NoticeData notice = new NoticeData();
				notice.fromuid = sourceuid;
				notice.touid = targetuid;
				JsonBuilder json = new JsonBuilder();
				json.append("fromuid", sourceuid);
				
				if("follow".equals(event)) {
					json.append("text", "我关注了你");
				}
				else {
					// TO DO ?
					json.append("text", "我解除了对你的关注");
				}
				
				notice.content = json.flip().toString();
				list.add(notice);
				return list;
			}
		}
		else {
			// log.info("unsupported user event:" + event);
		}
		
		return Collections.EMPTY_LIST;
	}
}
