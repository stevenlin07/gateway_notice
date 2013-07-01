package com.weibo.meyou.notice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.weibo.meyou.notice.service.NoticeData;
import com.weibo.meyou.notice.service.NoticeServiceManager;
import com.weibo.meyou.notice.service.StatusNotice;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;

import cn.sina.api.commons.util.JsonWrapper;
import junit.framework.TestCase;

public class StatusNoticeTest extends TestCase {
	private NoticeServiceManager service;
	private DataService dataService = null;
	private DataStore dataStore = null;
	private WeSyncService weSync = null;
	
	@Before
	public void setUp() throws Exception {
		Injector injector = Guice.createInjector(new NoticeServiceTestModule());
		weSync = injector.getInstance(WeSyncService.class);
		dataService = injector.getInstance(DataService.class);
		dataStore = injector.getInstance(DataStore.class);
		service = new NoticeServiceManager(dataService, dataStore, weSync);
	}
	
	@Test
	public void testSendNotice() {
		try {
			StatusNotice notice = new StatusNotice(service);
			JsonWrapper json = new JsonWrapper(status);
			Set<String> uniToUidSet = new HashSet<String>();
			List<NoticeData> notices = notice.getNotice(json, uniToUidSet);
			assertTrue(notices.isEmpty());
			weSync.getDataService().prepareForNewUser("1601563722");
			notices = notice.getNotice(json, uniToUidSet);
			assertTrue(notices.size() == 1);
			weSync.getDataService().prepareForNewUser("2425959300");
			notices = notice.getNotice(json, uniToUidSet);
			assertTrue(notices.size() == 2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	String status = "{\"type\":\"status\",\"event\":\"add\",\"status\":{\"created_at\":\"Wed Aug 22 00:47:51 +0800 2012\",\"id\":3481625520757978,\"text\":\"转发微博\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481625520757978\",\"user\":{\"id\":1841589682,\"screen_name\":\"吖茵Sophie\",\"name\":\"吖茵Sophie\",\"province\":\"44\",\"city\":\"13\",\"location\":\"\",\"description\":\"从来未亲近问怎么撇下\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"\",\"gender\":\"f\",\"followers_count\":755,\"friends_count\":385,\"statuses_count\":1337,\"favourites_count\":328,\"created_at\":\"Mon Oct 04 18:14:33 +0800 2010\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":220},\"retweeted_status\":{\"created_at\":\"Wed Aug 22 00:01:05 +0800 2012\",\"id\":3481613747129670,\"text\":\"有些人，喜欢上谁时，总是扭捏不敢下手。而对方想分手时，又宁被嫌弃都不愿离开。何必苦了自己让人看笑话呢？其实爱情里，人们最需要的就是潇洒走一回。真的爱了就大大方方在一起。真不爱了就各回各家，缺了谁都能活。有爱时拿得起，没爱时放得下，洒脱的人生才精彩。——陆琪（上帝保佑，大家晚安）\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"thumbnail_pic\":\"\",\"bmiddle_pic\":\"\",\"original_pic\":\"\",\"geo\":null,\"mid\":\"3481613747129670\",\"user\":{\"id\":1601563722,\"screen_name\":\"陆琪\",\"name\":\"陆琪\",\"province\":\"33\",\"city\":\"1\",\"location\":\"\",\"description\":\"电视节目、活动等工作邀约，请联系：18605229277。《潜伏在办公室》《婚姻是女人一辈子的事》者，畅销书作家、编剧。\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"jdluqi\",\"gender\":\"m\",\"followers_count\":2318429,\"friends_count\":756,\"statuses_count\":8199,\"favourites_count\":104,\"created_at\":\"Fri Aug 28 16:14:27 +0800 2009\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":true,\"verified\":true,\"verified_type\":0},\"state\":0},\"state\":0},\"extend\":{\"source_id\":10361,\"filter\":77,\"mentions\":[1601563722, 2425959300],\"ip\":\"125.88.122.103\"}}";
}
