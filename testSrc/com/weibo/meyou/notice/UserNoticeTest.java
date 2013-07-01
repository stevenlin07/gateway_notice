package com.weibo.meyou.notice;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cn.sina.api.commons.util.JsonWrapper;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.weibo.meyou.notice.service.NoticeData;
import com.weibo.meyou.notice.service.NoticeServiceManager;
import com.weibo.meyou.notice.service.StatusNotice;
import com.weibo.meyou.notice.service.UserNotice;
import com.weibo.meyou.notice.service.WeSyncUtil;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;

public class UserNoticeTest {
	private NoticeServiceManager service;
	private DataService dataService = null;
	private WeSyncService weSync = null;
	private DataStore dataStore;
	
	@Before
	public void setUp() throws Exception {
		Injector injector = Guice.createInjector(new NoticeServiceTestModule());
		weSync = injector.getInstance(WeSyncService.class);
		dataService = injector.getInstance(DataService.class);
		dataStore = injector.getInstance(DataStore.class);
		service = new NoticeServiceManager(dataService, dataStore, weSync);
	}

	@Test
	public void test() {
		try {
			UserNotice notice = new UserNotice(service);
			JsonWrapper json = new JsonWrapper(firehosedata);
			JsonWrapper testjson = json.getNode("text");
			Set<String> uniToUidSet = new HashSet<String>();
			List<NoticeData> notices = notice.getNotice(testjson, uniToUidSet);
			assertTrue(notices.isEmpty());
			weSync.getDataService().prepareForNewUser("2653283511");
			notices = notice.getNotice(testjson, uniToUidSet);
			assertTrue(notices.size() == 1);
			NoticeData data = notices.get(0);
			assertTrue("2653283511".equals(data.touid));
			assertTrue(WeSyncUtil.user.equals(data.fromuid));
			JsonWrapper contentjson = new JsonWrapper(data.content);
			assertTrue("unfollow".equals(contentjson.get("text")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	String firehosedata = "{\"id\":1208210275115470,\"text\":{\"type\":\"user\",\"event\":\"unfollow\",\"source\":{\"id\":1591531043,\"idstr\":\"1591531043\",\"screen_name\":\"IIshang\",\"name\":\"IIshang\",\"province\":\"31\",\"city\":\"15\",\"location\":\"\",\"description\":\"\",\"url\":\"\",\"profile_image_url\":\"http://tp4.sinaimg.cn/1591531043/50/5639714741/0\",\"profile_url\":\"u/1591531043\",\"domain\":\"\",\"weihao\":\"\",\"gender\":\"f\",\"followers_count\":4,\"friends_count\":1460,\"statuses_count\":2,\"favourites_count\":1,\"created_at\":\"Thu Sep 01 20:26:31 +0800 2011\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1,\"allow_all_comment\":false,\"avatar_large\":\"http://tp4.sinaimg.cn/1591531043/180/5639714741/0\",\"verified_reason\":\"\",\"follow_me\":false,\"online_status\":0,\"bi_followers_count\":0,\"lang\":\"zh-cn\"},\"target\":{\"id\":2653283511,\"idstr\":\"2653283511\",\"screen_name\":\"时尚中性搭配\",\"name\":\"时尚中性搭配\",\"province\":\"44\",\"city\":\"1\",\"location\":\"\",\"description\":\"不是淘宝店主，不是时尚买手，与喜欢中性风格的童鞋一同分享中性搭配。投稿或合作请私信。\",\"url\":\"\",\"profile_image_url\":\"http://tp4.sinaimg.cn/2653283511/50/5627224708/0\",\"profile_url\":\"tomboystyle\",\"domain\":\"tomboystyle\",\"weihao\":\"\",\"gender\":\"f\",\"followers_count\":72078,\"friends_count\":44,\"statuses_count\":4087,\"favourites_count\":0,\"created_at\":\"Thu Mar 08 20:21:49 +0800 2012\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1,\"allow_all_comment\":true,\"avatar_large\":\"http://tp4.sinaimg.cn/2653283511/180/5627224708/0\",\"verified_reason\":\"\",\"follow_me\":false,\"online_status\":0,\"bi_followers_count\":38,\"lang\":\"zh-cn\"}}}";
}
