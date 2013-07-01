package com.weibo.meyou.notice.utils;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-14
 *
 */
public class UserSetting {
	public static final int ST_MEYOU_STATUS_NUM = Integer.parseInt("1", 2); //meyou status unread num
	public static final int ST_MEYOU_CHAT_UNREAD_NUM = Integer.parseInt("100", 2);	//meyou chat unread num
	public static final int ST_INSTALL_MEYOU_SUCC = Integer.parseInt("1000", 2);//install meyou success notice by bilateral friend
	public static final int ST_AT_COMMENT_TEXT = Integer.parseInt("10000", 2);	//at & comment text
	public static final int ST_CHAT_TEXT = Integer.parseInt("100000", 2); //meyou chat text
	
	public static boolean isChatNumSet(int setting){
		return (setting & ST_MEYOU_CHAT_UNREAD_NUM) != 0;
	}
	
	public static boolean isMeyouStautsUnreadNumSet(int setting){
		return (setting & ST_MEYOU_STATUS_NUM) != 0;
	}
	
	public static boolean isAtCommentTextSet(int setting){
		return (setting & ST_AT_COMMENT_TEXT) != 0;
	}
	
	public static boolean isChatTextSet(int setting){
		return (setting & ST_CHAT_TEXT) != 0;
	}
	
	public static boolean isInstallSuccSet(int setting){
		return (setting & ST_INSTALL_MEYOU_SUCC) != 0;
	}
	
	public static void main(String[] args){
//		int[] ary = new int[]{2, 63};
//
//		System.out.println("ST_MEYOU_CHAT_UNREAD_NUM:" + ST_MEYOU_CHAT_UNREAD_NUM);
//		System.out.println("ST_MEYOU_STATUS_NUM:" + ST_MEYOU_STATUS_NUM);
//		System.out.println("ST_AT_COMMENT_TEXT:" + ST_AT_COMMENT_TEXT);
//		System.out.println("ST_CHAT_TEXT:" + ST_CHAT_TEXT);
//		System.out.println("ST_INSTALL_MEYOU_SUCC:" + ST_INSTALL_MEYOU_SUCC);
////		System.out.println(Integer.parseInt("111111111", 2));
//		
//		
//		for (int i : ary) {
//			System.out.println(UserSetting.isMeyouStautsUnreadNumSet(i));
//			System.out.println(UserSetting.isAtCommentTextSet(i));
//			System.out.println(UserSetting.isChatTextSet(i));
//			System.out.println(UserSetting.isInstallSuccSet(i));
//			System.out.println(UserSetting.isChatNumSet(i));
//			System.out.println("////////////");
//		}
		
//		System.out.println(Integer.parseInt("110", 2));
		System.out.println(Integer.toBinaryString(57));
	}
	
}
