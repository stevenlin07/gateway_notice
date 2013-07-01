package com.weibo.meyou.notice.module;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class NoticeGuiceProvider {

	public static Injector INJECTOR;
	
	static{
		INJECTOR = Guice.createInjector(new DeviceDbModule(), new DeviceMcModule());
		//INJECTOR = Guice.createInjector(new DeviceDbModule(), new DeviceMcModule());
	}
}
