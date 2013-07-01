package com.weibo.meyou.notice.device.service;

import com.weibo.meyou.notice.model.Device;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-12
 *
 */
public interface IDeviceService {
	Device getDeviceByUserId(long userid);
	
	boolean saveDevice(Device device);
	
	public boolean updateDevice(Device device);
	
	public boolean delDevice(Device device);	

	public boolean isSenderIdInOfflineNoticeBlackList(String reciverId, String senderId, String type);
	
	public boolean addUidToOfflineNoticeBlackList(String reciverId, String senderId, String type);
	
	public boolean delIdInOfflineNoticeBlackList(String reciverId, String senderId, String type);
	
	public String[] getOfflineNoticeBlackList(String reciverId);
}
