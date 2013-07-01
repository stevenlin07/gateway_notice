package com.weibo.meyou.notice.module;

import org.apache.log4j.Logger;

import com.google.inject.name.Names;
import com.weibo.meyou.notice.device.storage.CacheService;
import com.weibo.meyou.notice.device.storage.CacheServiceImpl;
import com.weibo.wejoy.data.module.McModule;

public class DeviceMcModule extends McModule {

	private static Logger log = Logger.getLogger("notify_service");
	private String configPath = "device-data-mc.xml";
	
	@Override
	public String getConfigPath(){
		return configPath;
	}
	
	@Override
	public void doOtherInitialization() {
		CacheServiceImpl cacheService = new CacheServiceImpl();
		
		try {
			cacheService.setMemCacheStorage(super.provideMemCacheStorageImpl());
			
			bind(CacheService.class)
				.annotatedWith(Names.named("deviceMc"))
				.toInstance(cacheService);
		} catch (Exception e) {
			log.error("when bind CacheServiceImpl-deviceMc, error occured,: ", e);
			throw new RuntimeException("bind CacheServiceImpl-deviceMc error", e);
		}
	}
	
	public static void main(String[] args) {
		DeviceMcModule dmm = new DeviceMcModule();
		dmm.initResource("conf/device-data-mc.xml");
		Object isHasPhone = dmm.provideMemCacheStorageImpl().get("2997548741" + ".11");
		System.out.println(isHasPhone);
	}
}
