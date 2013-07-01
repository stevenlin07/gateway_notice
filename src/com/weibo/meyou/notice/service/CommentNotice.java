package com.weibo.meyou.notice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import cn.sina.api.commons.util.JsonWrapper;

public class CommentNotice extends Notice {
	
	public CommentNotice(NoticeServiceManager service) {
		this.service = service;
	}
	
	@Override
	public List<NoticeData> getNotice(JsonWrapper content, Set<String> uniToUidSet) throws Exception {
		JsonWrapper cmObj = content.getNode("comment");
		String text = StringUtils.trim((String) cmObj.get("text"));
		JsonWrapper senderObj = cmObj.getNode("user");
		String nick = senderObj.get("screen_name");
		long senderId = senderObj.getLong("id");
		JsonWrapper statusObj = cmObj.getNode("status");
		String statuId = statusObj.get("id");
		
		JsonWrapper replyNode = cmObj.getNode("reply_comment");
		String noticeContent;
		long receiverId;
		if(replyNode.getRootNode() != null){
			JsonWrapper replyToUserNode = replyNode.getNode("user");
			receiverId = replyToUserNode.getLong("id");
			String receiverNick = replyToUserNode.get("screen_name");
			text = text.replace("回复@" + receiverNick + ":", "");	//product request
			text = super.cutSubString4NoticeContent(text);
			noticeContent = nick + "回复了你：" + text;
		} else {
			JsonWrapper userObj = statusObj.getNode("user");
			receiverId = userObj.getLong("id");
			text = super.cutSubString4NoticeContent(text);
			noticeContent = nick + "评论了你：" + text;
		}
		
		Map<String, String> addData = new HashMap<String, String>();
		addData.put(NoticeData.PayloadField.Mid.fieldName, statuId);
		List<NoticeData> noticeDatas = super.getNoticeDatas(new long[]{receiverId} , senderId, 
				noticeContent, true, NoticeData.NoticeType.Comment, addData);
		
		uniToUidSet.add(String.valueOf(receiverId));
		
		//get at notices
		long[] beAtUids = getAtUserids(content);
		if(beAtUids.length > 0){
			for (long toUid : beAtUids) {
				uniToUidSet.add(String.valueOf(toUid));
			}

			noticeContent = nick + "提到了你：" + text;
			noticeDatas.addAll(
				super.getNoticeDatas(beAtUids, senderId, noticeContent, true, NoticeData.NoticeType.At, addData)
			);
		}
		
		return noticeDatas;
	}
}
