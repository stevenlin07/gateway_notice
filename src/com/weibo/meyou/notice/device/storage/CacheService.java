package com.weibo.meyou.notice.device.storage;

import com.weibo.meyou.notice.model.Device;

public interface CacheService {
	// mc suffix def
	public static final String MTALK_SUFFIX = ".0";
	public static final String IPHONE_SUFFIX = ".1";
	public static final String CLOSEFRIEND_SUFFIX = ".11";
	
	Device getDevice(long reciverId, String deviceSuffix);

	boolean remove(long uid, String deviceSuffix);
	
	boolean contains(long uid, String deviceSuffix);
	
	boolean addDevice(Device device, String deviceSuffix);
}
