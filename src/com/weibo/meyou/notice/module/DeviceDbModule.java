package com.weibo.meyou.notice.module;

import org.apache.log4j.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.weibo.meyou.notice.device.storage.DeviceDao;
import com.weibo.meyou.notice.device.storage.IDeviceDao;
import com.weibo.wejoy.data.module.DbModule;

public class DeviceDbModule extends DbModule {
	
	private static Logger log = Logger.getLogger("notify_service");
	private String configPath = "device-data-db.xml";
	
	@Override
	public String getConfigPath(){
		return configPath;
	}
	
	@Override
	public void doOtherInitialization() {
		DeviceDao deviceDao = new DeviceDao();		
		
		try {
			deviceDao.setClusterDatabases(super.provideClusterDatabases());
			deviceDao.strategykey = super.strategykey;	//super.provideClusterDatabases() will set super.strategykey
			bind(IDeviceDao.class)
				.annotatedWith(Names.named("deviceDao"))
				.toInstance(deviceDao);
			
		} catch (Exception e) {
			log.error("when bind DeviceDao, error occured,: ", e);
			throw new RuntimeException("bind DeviceDao error", e);
		}
	}
	
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new DeviceDbModule());
		IDeviceDao deviceDao = injector.getInstance(Key.get(IDeviceDao.class, Names.named("deviceDao")));
		System.out.println(deviceDao.getDeviceByUserId(1603449647));
		System.out.println("over");
	}
	
}
