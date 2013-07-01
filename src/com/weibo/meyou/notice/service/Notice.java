package com.weibo.meyou.notice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.sina.api.commons.util.JsonBuilder;
import cn.sina.api.commons.util.JsonWrapper;
import cn.sina.api.user.UserFriendShipAttributes.FriendShipType;

import com.weibo.meyou.notice.utils.CommonUtil;
import com.weibo.meyou.notice.utils.DebugTools;

public abstract class Notice {
	public static final String user = "user";
	public static final String status = "cf_status";
	public static final String comment = "cf_comment";
	public static final String attitude = "cf_attitude";
	public static final String install_succ = "install_succ_request_from_sdk";
	
	protected NoticeServiceManager service;
	
	private static Logger log = Logger.getLogger(Notice.class);
	
	DebugTools debugTools = DebugTools.getInstance();
	
	/**
	 * 
	 * @param content
	 * @param uniToUidSet no duplicate touid set
	 * @return
	 * @throws Exception
	 */
	public abstract List<NoticeData> getNotice(JsonWrapper content, Set<String> uniToUidSet) throws Exception;
	
	/**
	 * @param toUids
	 * @param senderUid
	 * @param noticeContent
	 * @param isCheckTie whether check the strong tie
	 * @param addData additional data
	 * @return
	 */
	public List<NoticeData> getNoticeDatas(long[] toUids, long senderUid, String noticeContent, boolean isCheckTie,
			NoticeData.NoticeType noticeType, Map<String, String> addData){
		List<NoticeData> noticeDatas = new ArrayList<NoticeData>();
		
		Set<Long> uidSet = service.screenUids4InstallMeyou(toUids);
		long[] uids = new long[uidSet.size()];
		int counter = 0;
		for (long uid : uidSet) {
			uids[counter ++] = uid;
		}
		
		if(isCheckTie){
			uidSet = service.strongTiesService.checkTies(senderUid, uids, FriendShipType.close);
		}
		
		for(long toUid : toUids) {
			boolean isOutDebugInfo = debugTools.isDebugEnabled() && debugTools.onDebugList(toUid, senderUid);
			
			if(uidSet.contains(toUid)){
				NoticeData notice = new NoticeData();
				
				notice.fromuid = String.valueOf(senderUid);
				notice.touid = String.valueOf(toUid);
				notice.type = noticeType;
				if(noticeContent != null){
					notice.content = noticeContent;
				}
				
//				String typeValue4apnPayload = noticeType.typeValue4apnPayload;
//				if(typeValue4apnPayload != null){
//					notice.addEntry(NoticeData.PayloadField.Type.fieldName, String.valueOf(typeValue4apnPayload));
//				}
				
				if(addData != null){
					for (String key : addData.keySet()) {
						String value = addData.get(key);
						notice.addEntry(key, value);
					}
				}
				
				noticeDatas.add(notice);
			} else if(isOutDebugInfo) {
				log.debug(String.format("%s. send %s to %s is ignored because they are not meyou or the reciver"
						+ "don't register in meyou_gateway; noticeContent:%s", 
						senderUid, toUid, this.getClass().getName(), noticeContent));
			}
		}
		
		return noticeDatas;
	}
	
	protected long[] getAtUserids(JsonWrapper contentObj) {
		JsonWrapper exObj = contentObj.getNode("extend");
		
		if(exObj != null){
			long[] mentions = exObj.getLongArr("mentions");
			return mentions;
		}
		
		return new long[0];
	}
	
	protected String cutSubString4NoticeContent(String srcText){
		if(srcText != null){
			int len = CommonUtil.countLen(srcText);
			if(len > 10){	//product request
				float count = 0;
				char[] cAry = srcText.toCharArray();
				int i = 0;
				for (; i < cAry.length; i++) {
					char c = cAry[i];
					
					if ((c >= 0x4E00 && c <= 0x9FFF)) {	//Chinese
						++ count;
					} else {
						count += 0.5;
					}
					
					if(count >= 10){
						break;
					}
				}
				srcText = srcText.substring(0, i + 1) + "...";
			}
		}
		
		return srcText;
	}
	
	public static void main(String[] args) {
		String o = null;
		JsonBuilder jb = new JsonBuilder();
		jb.append("test", o);
		
		try {
			JsonWrapper wrapper = new JsonWrapper(jb.flip().toString());
			System.out.println(wrapper.get("test").equals("null"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
