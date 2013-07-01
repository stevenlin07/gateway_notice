package com.weibo.meyou.notice.service;

import java.util.HashMap;
import java.util.Map;

public class NoticeData {
	
	public enum NoticeType{
		InstallSucc("3", true),	//first install meyou to push notice to his bilateral friend who has install meyou
		Comment("1", false),
		At("1", false),
		UnReadNum("3", true),
		Chat("2", false),
		Chat4Zuju("zujuChat", false);
		
		public String typeValue4apnPayload;	//type value for ios apn payload
		public boolean isSetBadgeInPayload;
		
		private NoticeType() {}
		
		private NoticeType(String typeValue4apnPayload, boolean isSetBadgeInPayload) {
			this.typeValue4apnPayload = typeValue4apnPayload;
			this.isSetBadgeInPayload = isSetBadgeInPayload;
		}
	}
	
	public enum PayloadField{
		Type("type"),
		Fuid("fuid"),	//from uid for chat
		Tuid("tuid"),	//to uid for chat
		Cuid("cuid"),	//be pushed uid
		Mid("mid");		//status id
		
		public String fieldName;
		private PayloadField(String fieldName) {
			this.fieldName = fieldName;  
		}
		
	}
	
	public String fromuid;
	public String touid;
	public String content;
	public NoticeType type;
	public Map<String, String> entries;	//additional data
	
	public static final String ENTRY_KEY_SHOW_ALERT = "show_alert";
	public static final String ENTRY_KEY_SHOW_NUM = "show_num";
	
	public void addEntry(String key, String value){
		if(entries == null){
			entries = new HashMap<String, String>();
		}
		
		entries.put(key, value);
	}
	
	public void clearEntries(){
		entries = null;
	}
}
