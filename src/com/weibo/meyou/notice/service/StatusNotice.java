package com.weibo.meyou.notice.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.weibo.meyou.notice.utils.CommonUtil;

import cn.sina.api.commons.util.JsonWrapper;
import cn.sina.api.user.UserFriendShipAttributes.FriendShipType;
import cn.sina.api.user.UserTies;

public class StatusNotice extends Notice {
	public StatusNotice(NoticeServiceManager service) {
		super.service = service;
	}
	
	@Override
	public List<NoticeData> getNotice(JsonWrapper content, Set<String> uniToUidSet) throws Exception {
		String event = content.get("event");
		if(!"add".equals(event)){
			return Collections.EMPTY_LIST;
		}
		
		JsonWrapper statusObj = content.getNode("status");
		String statuId = statusObj.get("id");
		String statusText = StringUtils.trim(statusObj.get("text"));
		JsonWrapper senderObj = statusObj.getNode("user");
		String nick = senderObj.get("screen_name");
		long senderUid = senderObj.getLong("id");
		
		//create unread status notice to all meyou which has install meyou
		UserTies ties = service.strongTiesService.getTies(senderUid, new FriendShipType[]{FriendShipType.close});
		long[] meyouUidOfSender = ties.getUserIds();
		for (long toUid : meyouUidOfSender) {
			uniToUidSet.add(String.valueOf(toUid));
		}
		
		List<NoticeData> noticeDatas = super.getNoticeDatas(meyouUidOfSender, senderUid, null,
				false, NoticeData.NoticeType.UnReadNum, null);

		//get at notices
		long[] beAtUids = getAtUserids(content);
		for (long toUid : beAtUids) {
			uniToUidSet.add(String.valueOf(toUid));	
		}
		
		if(beAtUids.length > 0){
			Map<String, String> addData = new HashMap<String, String>();
			addData.put(NoticeData.PayloadField.Mid.fieldName, statuId);
			
			String noticeContent = null;
			if(statusText != null){
				statusText = super.cutSubString4NoticeContent(statusText);
				
				noticeContent = nick + "提到了你：" + statusText;
			}
			
			noticeDatas.addAll(
				super.getNoticeDatas(beAtUids, senderUid, noticeContent, true, NoticeData.NoticeType.At, addData)
			);
		}
		
		return noticeDatas;
	}
}
