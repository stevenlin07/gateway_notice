package com.weibo.meyou.notice.model;


/**
 *  author : tianqi@staff.sina.com.cn
 *  date   : 2010-9-3
 *  
 */
public class UnreadInfo {
			
	public static final String PAYLOAD_JSON_FIELD_FEEDS = "status";
	public static final String PAYLOAD_JSON_FIELD_MENTION_STATUS = "atStatus";
	public static final String PAYLOAD_JSON_FIELD_COMMENT = "cmt";
	public static final String PAYLOAD_JSON_FIELD_MENTION_COMMENT = "atCmt";
	public static final String PAYLOAD_JSON_FIELD_ATTITUDE = "attitude";
	public static final String PAYLOAD_JSON_FIELD_COMMON_COMMENT = "commCmt";
	public static final String PAYLOAD_JSON_FIELD_INVITE = "invite";
	public static final String PAYLOAD_JSON_FIELD_NEW = "new";
	public static final String PAYLOAD_JSON_FIELD_CHAT = "chat";
	
	private long userid;
	private int comment;
	private int follower;
	private int sms;
	private int group;
	private int attitude;
	private int status;
	private int atStatus;
	private int atComment;
	private int commonComment;
	private int invite;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("comment:").append(comment).append(";")
			.append("attitude:").append(attitude).append(";")
			.append("status:").append(status).append(";")
			.append("atStatus:").append(atStatus).append(";")
			.append("atComment:").append(atComment).append(";")
			.append("commonComment:").append(commonComment).append(";")
			.append("invite:").append(invite);

		return sb.toString();
	}
	
	public UnreadInfo() {}

	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	public int getComment() {
		return comment;
	}

	public void setComment(int comment) {
		this.comment = comment;
	}

	public int getFollower() {
		return follower;
	}

	public void setFollower(int follower) {
		this.follower = follower;
	}

	public int getSms() {
		return sms;
	}

	public void setSms(int sms) {
		this.sms = sms;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getAttitude() {
		return attitude;
	}

	public void setAttitude(int attitude) {
		this.attitude = attitude;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getAtStatus() {
		return atStatus;
	}

	public void setAtStatus(int atStatus) {
		this.atStatus = atStatus;
	}

	public int getAtComment() {
		return atComment;
	}

	public void setAtComment(int atComment) {
		this.atComment = atComment;
	}

	public int getCommonComment() {
		return commonComment;
	}

	public void setCommonComment(int commonComment) {
		this.commonComment = commonComment;
	}

	public int getInvite() {
		return invite;
	}

	public void setInvite(int invite) {
		this.invite = invite;
	}	
	
}
