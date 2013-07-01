package com.weibo.meyou.notice;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import cn.sina.api.commons.util.JsonWrapper;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.weibo.meyou.notice.service.CommentNotice;
import com.weibo.meyou.notice.service.NoticeData;
import com.weibo.meyou.notice.service.NoticeServiceManager;
import com.weibo.meyou.notice.service.WeSyncUtil;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;

public class CommentNoticeTest {
	private NoticeServiceManager service;
	private DataService dataService = null;
	private DataStore dataStore;
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
	public void test() {
		try {
			CommentNotice notice = new CommentNotice(service);
			JsonWrapper json = new JsonWrapper(comment);
			JsonWrapper testjson = json.getNode("text");
			Set<String> uniToUidSet = new HashSet<String>();
			
			List<NoticeData> notices = notice.getNotice(testjson, uniToUidSet);
			assertTrue(notices.isEmpty());
			
			weSync.getDataService().prepareForNewUser("1668811257");
			notices = notice.getNotice(testjson, uniToUidSet);
			assertTrue(notices.size() == 1);
			NoticeData data = notices.get(0);
			
			assertTrue("1668811257".equals(data.touid));
			assertTrue(WeSyncUtil.comment.equals(data.fromuid));
			
			JsonWrapper contentjson = new JsonWrapper(data.content);
			assertTrue("[哈哈][哈哈][哈哈]".equals(contentjson.get("text")));
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	String comment = "{\"id\":1208220026489223,\"text\":{\"type\":\"comment\",\"event\":\"add\",\"comment\":{\"created_at\":\"Wed Aug 22 01:25:29 +0800 2012\",\"id\":3481634986856188,\"text\":\"[哈哈][哈哈][哈哈]\",\"source\":\"\",\"user\":{\"id\":2014246943,\"screen_name\":\"憨人蒋啸啸\",\"name\":\"憨人蒋啸啸\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"贪玩儿贪吃还好色，不记得自己是哪个星球的人了\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"jaypur\",\"gender\":\"m\",\"followers_count\":288,\"friends_count\":231,\"statuses_count\":692,\"favourites_count\":10,\"created_at\":\"Wed Mar 09 12:31:53 +0800 2011\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":220},\"mid\":\"2121208227501965\",\"status\":{\"created_at\":\"Wed Aug 22 01:24:06 +0800 2012\",\"id\":3481634643162493,\"text\":\"只想说一句：你大爷的。你早日找到真爱。等我回来一起结婚。\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481634643162493\",\"user\":{\"id\":1668811257,\"screen_name\":\"胖子死在了球场上\",\"name\":\"胖子死在了球场上\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"該說甚麼呢？沒能力改變世界，只能好好做人。\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"yeqiwa\",\"gender\":\"m\",\"followers_count\":181,\"friends_count\":346,\"statuses_count\":280,\"favourites_count\":1,\"created_at\":\"Thu Jul 29 10:03:03 +0800 2010\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1},\"retweeted_status\":{\"created_at\":\"Tue Aug 21 22:08:53 +0800 2012\",\"id\":3481585507101298,\"text\":\"田浩先生，我的校友，胖子，也就是著名的、去年跨年買了禽獸票的那個小桿子昨天從老家趕到南京請我吃飯。據說要去士頓求學，臨走前請求我在微博上祝他一路順風。嗨，年輕人嘛，除了打擊還要幫助。所以，祝田浩先生爭取5年拿下碩士[思考]\",\"source\":\"\",\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481585507101298\",\"user\":{\"id\":2032617483,\"screen_name\":\"南京李志\",\"name\":\"京李志\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"【工作聯絡】way@lizhizhuangbi.com（劉威）【官方網站】www.lizhizhuangbi.com或者\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"nanjinglizhi\",\"gender\":\"m\",\"followers_count\":25878,\"friends_count\":26,\"statuses_count\":17,\"favourites_count\":0,\"created_at\":\"Fri Mar 18 22:17:44 +0800 2011\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":false,\"verified\":false,\"verified_type\":-1},\"state\":0},\"state\":0},\"state\":0},\"extend\":{\"source_id\":174,\"status_source_id\":12502,\"mentions\":[]}}}";
}
