package com.weibo.meyou.notice.device.storage;

import com.weibo.meyou.notice.model.Device;


/**
 * @author yangshuo3
 * @date 2012-10-12
 *
 */
public interface IDeviceDao {
	Device getDeviceByUserId(long userid);
	
	boolean saveDevice(Device device);
	
	boolean updateDevice(Device device);
	
	boolean delDevice(Device device);	
}
