package com.weibo.meyou.notice.model;

import java.io.Serializable;
import java.util.Date;

public class Device implements Serializable{
	private static final long serialVersionUID = -7556226919175824248L;
	
	private long userid;
	private String nickname=null;
	private String deviceId;
	private int switchInfo;
	private int startTime;
	private int endTime;
	private String timezone=null;
	private long appkey;
	private int display;
	
	private Date createTime;
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("userid:").append(userid)
			.append("nickname:").append(nickname)
			.append("deviceId:").append(deviceId)
			.append("switchInfo:").append(switchInfo)
			.append("startTime:").append(startTime)
			.append("endTime:").append(endTime)
			.append("timezone:").append(timezone)
			.append("appkey:").append(appkey)
			.append("createTime:").append(createTime)
			.append("display:").append(display);
		
		return sb.toString();
	}
	
	public Device() {
	}
	public Device(long userid, String deviceId,
			int switchInfo, int startTime, int endTime, int display) {
		this.userid = userid;
		this.deviceId = deviceId;
		this.switchInfo = switchInfo;
		this.startTime = startTime;
		this.endTime = endTime;
		this.display = display;
		
		this.createTime=new Date();
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public int getSwitchInfo() {
		return switchInfo;
	}
	public void setSwitchInfo(int switchInfo) {
		this.switchInfo = switchInfo;
	}
	public int getStartTime() {
		return startTime;
	}
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	public int getEndTime() {
		return endTime;
	}
	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}
	
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public long getUserid() {
		return userid;
	}
	public void setUserid(long userid) {
		this.userid = userid;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public long getAppkey() {
		return appkey;
	}
	public void setAppkey(long appkey) {
		this.appkey = appkey;
	}
	public int getDisplay() {
		return display;
	}
	public void setDisplay(int display) {
		this.display = display;
	}
}
