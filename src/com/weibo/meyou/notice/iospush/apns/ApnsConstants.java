package com.weibo.meyou.notice.iospush.apns;

/**
 *  author : tianqi@staff.sina.com.cn
 *  date   : 2010-8-23
 *  
 */
public class ApnsConstants {
	public static final String HOST4Test="gateway.sandbox.push.apple.com:2195";	//for test
	public static final String HOST="gateway.push.apple.com:2195";	//online
	
//	public static final String FEEDBACK_HOST="feedback.sandbox.push.apple.com:2196";
	public static final String FEEDBACK_HOST="feedback.push.apple.com:2196";
//	public static final String KEY_STORE_PATH="/data1/firehose/jetty-iospush/webapps/iospush-web/WEB-INF/certificate7.p12";
	public static final String KEY_STORE_TYPE=SSLConnectionHelper.KEYSTORE_TYPE_PKCS12;
	public static final String KEY_STORE_PWD="123456";
}
