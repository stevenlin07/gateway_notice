package com.weibo.meyou.notice.device.service;

<<<<<<< HEAD
import java.util.Set;

=======
>>>>>>> 421abd47de4a87b56419f9494104bb31b725add3
import org.apache.log4j.Logger;

import redis.clients.jedis.Tuple;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.weibo.meyou.data.storage.RedisStorage;
import com.weibo.meyou.notice.device.storage.CacheService;
import com.weibo.meyou.notice.device.storage.IDeviceDao;
import com.weibo.meyou.notice.model.Device;
import com.weibo.meyou.notice.utils.Constants;
import com.weibo.meyou.notice.utils.DebugTools;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-12
 *
 */
public class DeviceService implements IDeviceService {
	private Logger log = Logger.getLogger("notify_service");
	private IDeviceDao deviceDao;
	private CacheService deviceMc;
	public RedisStorage offlineNoticeBlackList;
	
	private DebugTools dt = DebugTools.getInstance();
	
	@Inject
	public DeviceService(@Named("deviceDao") IDeviceDao deviceDao, @Named("deviceMc") CacheService deviceMc) {
		this.deviceDao = deviceDao;
		this.deviceMc = deviceMc;
	}

	public Device getDeviceByUserId(long userid) {
		Device device = deviceMc.getDevice(userid, CacheService.CLOSEFRIEND_SUFFIX);
		
		if(device == null){
			device = deviceDao.getDeviceByUserId(userid);
		}
		
		return device;
	}
	
	public boolean saveDevice(Device device) {
		boolean result = deviceDao.saveDevice(device);
		
		if(result){
			return deviceMc.addDevice(device, CacheService.CLOSEFRIEND_SUFFIX);
		} else {
			log.error(getClass().getName() + " add device mc return false; device:" + device.toString());
		}
		
		return result;
	}
	
	public boolean updateDevice(Device device){
		Device currentDevice = this.getDeviceByUserId(device.getUserid());
		
		if(currentDevice != null){
			if(device.getDeviceId() != null){
				currentDevice.setDeviceId(device.getDeviceId());
			}
			
			if(device.getDisplay() != 0){
				currentDevice.setDisplay(device.getDisplay());
			}
			
			if(device.getEndTime() != -1){
				currentDevice.setEndTime(device.getEndTime());
			}
			
			if(device.getStartTime() != -1){
				currentDevice.setStartTime(device.getStartTime());
			}
			
			if(device.getSwitchInfo() != -1){
				currentDevice.setSwitchInfo(device.getSwitchInfo());
			}
			
			boolean result = deviceDao.updateDevice(currentDevice);
			
			if(result){
				return deviceMc.addDevice(currentDevice, CacheService.CLOSEFRIEND_SUFFIX);
			} else {
				log.error(getClass().getName() + " add device mc return false; device:" + device.toString());
			}
			
			return result;
		}
		
		return false;
	}
	
	public boolean delDevice(Device device){
		boolean result = deviceDao.delDevice(device);
		
		if(result){
			return deviceMc.remove(device.getUserid(), CacheService.CLOSEFRIEND_SUFFIX);
		} else {
			log.error(getClass().getName() + " del device mc return false; device:" + device.toString());
		}
		
		return result;
	}

	public boolean isSenderIdInOfflineNoticeBlackList(String reciverId, String senderId, String type){
		if(Constants.OFFLINE_NOTICE_BLACK_LIST_GROUP_TYPE.equals(type)){
			senderId = this.getGroupId4StorageValue(senderId);	//special operation for group id
		}
		
		String key = reciverId + Constants.OFFLINE_NOTICE_BLACK_LIST_SUFFIX;
		
		Long ret = offlineNoticeBlackList.zrank(key, senderId);
		return (ret != null && ret != -1);
	}
	
	public boolean addUidToOfflineNoticeBlackList(String reciverId, String senderId, String type){
		if(Constants.OFFLINE_NOTICE_BLACK_LIST_GROUP_TYPE.equals(type)){
			senderId = this.getGroupId4StorageValue(senderId);	//special operation for group id
		}
		
		String key = reciverId + Constants.OFFLINE_NOTICE_BLACK_LIST_SUFFIX;
		
		Long ret = offlineNoticeBlackList.zadd(key, Constants.OFFLINE_NOTICE_BALCK_LIST_SCORE, senderId);
		return (ret != null && ret != -1 && ret != 0);	//return 0 -> element duplicate
	}
	
	public boolean delIdInOfflineNoticeBlackList(String reciverId, String senderId, String type){
		if(Constants.OFFLINE_NOTICE_BLACK_LIST_GROUP_TYPE.equals(type)){
			senderId = this.getGroupId4StorageValue(senderId);	//special operation for group id
		}
		
		String key = reciverId + Constants.OFFLINE_NOTICE_BLACK_LIST_SUFFIX;
		
		Long ret = offlineNoticeBlackList.zrem(key, senderId);
		return (ret != null && ret != -1 && ret != 0);	//return 0 -> non-existent element
	}
	
	public String[] getOfflineNoticeBlackList(String reciverId){
		String key = reciverId + Constants.OFFLINE_NOTICE_BLACK_LIST_SUFFIX;
		long count = offlineNoticeBlackList.zcard(key);
		
		Set<Tuple> ret = offlineNoticeBlackList.zrangeWithScores(key, 0, (int)count);
		String[] ary = new String[ret.size()];
		int counter = 0;
		for (Tuple tuple : ret) {
			ary[counter ++] = tuple.getElement();  
		}
		
		return ary;
	}
	
	private String getGroupId4StorageValue(String gid){
		return "g" + gid;
	}
	
	public static void main(String[] args) {
	}

}
