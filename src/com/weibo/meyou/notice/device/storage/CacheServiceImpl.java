package com.weibo.meyou.notice.device.storage;

import java.util.Date;

import org.apache.log4j.Logger;

import com.weibo.meyou.notice.model.Device;
import com.weibo.wejoy.data.storage.MemCacheStorage;
//import cn.sina.api.commons.cache.driver.VikaCacheClient;

public class CacheServiceImpl implements CacheService {
	private static Logger log = Logger.getLogger("notify_service");
	
	// userid.appkey.deviceid.switchInfo.starttime.endtime.timezone|
	private static final int INFO_IDX_USRID = 0;
	private static final int INFO_IDX_DEVICEID = 1;
	private static final int INFO_IDX_SWITCH = 2;
	private static final int INFO_IDX_START_TIME = 3;
	private static final int INFO_IDX_END_TIME = 4;
	private static final int INFO_IDX_DISPLAY = 5;
	
	private static final String SEP_CHAR = ",";
	
	private MemCacheStorage memCacheStorage;
	
	public void setMemCacheStorage(MemCacheStorage memCacheStorage) {
		this.memCacheStorage = memCacheStorage;
	}
	
	public Device getDevice(long reciverId, String deviceSuffix) {
		String key = reciverId + deviceSuffix;
		Object obj = memCacheStorage.get(key);
		
		if(obj != null) {
			String deviceInfo = (String) obj;
			return mcInfo2Device(key, deviceInfo);
		}
		
		return null;
	}
	
	private Device mcInfo2Device(String key, String info) {
		try {
			if(info == null || info.indexOf(SEP_CHAR) < 0)
				return null;
			
			String[] array = info.split(SEP_CHAR);
			
			Device device = new Device();
			device.setUserid(Long.valueOf(array[INFO_IDX_USRID]));
			device.setDeviceId(array[INFO_IDX_DEVICEID]);
			device.setSwitchInfo(Integer.valueOf(array[INFO_IDX_SWITCH]));
			device.setStartTime(Integer.parseInt(array[INFO_IDX_START_TIME]));
			device.setEndTime(Integer.parseInt(array[INFO_IDX_END_TIME]));
			device.setDisplay(Integer.parseInt(array[INFO_IDX_DISPLAY]));
			return device;
		}
		catch(Exception e) {
			log.error("mcInfo2Device [" + info + "] failed caused by " + e.getMessage() + ", key [" + key + "] will be removed.");
			memCacheStorage.delete(key);
		}
		
		return null;
	}
	
	public boolean contains(long uid, String deviceSuffix) {
		String key = uid + deviceSuffix;
		Object obj = memCacheStorage.get(key);
		log.debug(key +"|"+ obj);
		
		if(obj != null) {
			Device device = mcInfo2Device(key, (String) obj);
			
			if(device == null) {
				log.warn("key [" + key + "] memoryCacheStorage [" + obj + "] exits, but parse failed.");
				memCacheStorage.delete(key);
				return false;
			}
			else {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean remove(long uid, String deviceSuffix) {
		String key = uid + deviceSuffix;
		return memCacheStorage.delete(key);
	}
	
	private String device2MCInfo(Device device) {
		if(device == null)
			return null;
		
		StringBuffer sb = new StringBuffer();
		sb.append(device.getUserid()).append(SEP_CHAR);
		sb.append(device.getDeviceId()).append(SEP_CHAR);
		sb.append(device.getSwitchInfo()).append(SEP_CHAR);
		sb.append(device.getStartTime()).append(SEP_CHAR);
		sb.append(device.getEndTime()).append(SEP_CHAR);
		sb.append(device.getDisplay());
		
		log.debug("device2MCInfo result: " + sb.toString());
		return sb.toString();
	}	
	
	public boolean addDevice(Device device, String deviceSuffix) {
		String key = device.getUserid() + deviceSuffix;
		String newInfo = device2MCInfo(device);
		return memCacheStorage.set(key, newInfo, new Date(0L));
	}
	
}
