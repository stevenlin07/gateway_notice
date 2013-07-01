package com.weibo.meyou.notice;

import com.google.inject.AbstractModule;
import com.weibo.wesync.DataService;
import com.weibo.wesync.DataServiceImpl;
import com.weibo.wesync.FakeNoticeService;
import com.weibo.wesync.NoticeService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.WeSyncServiceImpl;
import com.weibo.wesync.data.DataStore;
import com.weibo.wesync.data.FakeDataStore;

public class NoticeServiceTestModule extends AbstractModule {
	@Override
	protected void configure() {
		DataStore dataStore = new FakeDataStore();
		bind(DataStore.class).toInstance(dataStore);
		bind(DataService.class).toInstance(new DataServiceImpl( dataStore ) );
		bind(NoticeService.class).to(FakeNoticeService.class);
		bind(WeSyncService.class).to(WeSyncServiceImpl.class);
	}
}
