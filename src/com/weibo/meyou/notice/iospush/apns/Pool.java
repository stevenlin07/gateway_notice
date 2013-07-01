package com.weibo.meyou.notice.iospush.apns;

import java.io.IOException;

/**
 *  author : tianqi@staff.sina.com.cn
 *  date   : 2010-8-15
 *  
 */
public interface Pool<T> {
	
	void checkIn(String host,T t);
	
	T checkOut(String host);
	
	void close(String host,T t ) throws IOException,Exception;
}
