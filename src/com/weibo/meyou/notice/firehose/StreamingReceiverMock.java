package com.weibo.meyou.notice.firehose;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.sina.api.commons.util.JsonWrapper;

import com.google.inject.Inject;
import com.weibo.meyou.notice.firehose.StreamingReceiver;
import com.weibo.meyou.notice.service.NoticeServiceManager;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;

public class StreamingReceiverMock implements StreamingReceiver, Runnable {
	private ExecutorService executor;
	private NoticeServiceManager noticeService;
	
	@Inject
	public StreamingReceiverMock(DataService dataService, DataStore dataStore, WeSyncService weSync) {
		this.noticeService = new NoticeServiceManager(dataService, dataStore, weSync);
		int threadCount = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(threadCount);
		
		new Thread(this).start();
	}
	
	public void run() {
		processLine(null);
	}
	
	@Override
	public void processLine(String arg0) {
		Long id = null;
		JsonWrapper textObj = null;
		JsonWrapper json;
		String[] lines = new String[3];
		lines[0] = commentdata;
		lines[1] = userdata;
		lines[2] = statusdata;
		
		while(true) {
			for(String line : lines) {
				try {
					json = new JsonWrapper(line);
					id = json.getLong("id");
					textObj = json.getNode("text");
					executor.execute(new MessageProcessor(textObj));
					
					// send per 30 seconds
					Thread.sleep(30 * 1000);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class MessageProcessor implements Runnable {
		private final JsonWrapper content;

		public MessageProcessor(JsonWrapper content){
			this.content = content;
		}

		public void run() {
			try {
				noticeService.sendNotice(content);
			} 
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	String commentdata = "{\"id\":1208220026489223,\"text\":{\"type\":\"comment\",\"event\":\"add\",\"comment\":{\"created_at\":\"Wed Aug 22 01:25:29 +0800 2012\",\"id\":3481634986856188,\"text\":\"[哈哈][哈哈][哈哈]\",\"source\":\"\",\"user\":{\"id\":2014246943,\"screen_name\":\"憨人蒋啸啸\",\"name\":\"憨人蒋啸啸\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"贪玩儿贪吃还好色，不记得自己是哪个星球的人了\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"jaypur\",\"gender\":\"m\",\"followers_count\":288,\"friends_count\":231,\"statuses_count\":692,\"favourites_count\":10,\"created_at\":\"Wed Mar 09 12:31:53 +0800 2011\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":220},\"mid\":\"2121208227501965\",\"status\":{\"created_at\":\"Wed Aug 22 01:24:06 +0800 2012\",\"id\":3481634643162493,\"text\":\"只想说一句：你大爷的。你早日找到真爱。等我回来一起结婚。\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481634643162493\",\"user\":{\"id\":1101000002,\"screen_name\":\"胖子死在了球场上\",\"name\":\"胖子死在了球场上\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"該說甚麼呢？沒能力改變世界，只能好好做人。\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"yeqiwa\",\"gender\":\"m\",\"followers_count\":181,\"friends_count\":346,\"statuses_count\":280,\"favourites_count\":1,\"created_at\":\"Thu Jul 29 10:03:03 +0800 2010\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1},\"retweeted_status\":{\"created_at\":\"Tue Aug 21 22:08:53 +0800 2012\",\"id\":3481585507101298,\"text\":\"田浩先生，我的校友，胖子，也就是著名的、去年跨年買了禽獸票的那個小桿子昨天從老家趕到南京請我吃飯。據說要去士頓求學，臨走前請求我在微博上祝他一路順風。嗨，年輕人嘛，除了打擊還要幫助。所以，祝田浩先生爭取5年拿下碩士[思考]\",\"source\":\"\",\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481585507101298\",\"user\":{\"id\":2,\"screen_name\":\"南京李志\",\"name\":\"京李志\",\"province\":\"32\",\"city\":\"1\",\"location\":\"\",\"description\":\"【工作聯絡】way@lizhizhuangbi.com（劉威）【官方網站】www.lizhizhuangbi.com或者\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"nanjinglizhi\",\"gender\":\"m\",\"followers_count\":25878,\"friends_count\":26,\"statuses_count\":17,\"favourites_count\":0,\"created_at\":\"Fri Mar 18 22:17:44 +0800 2011\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":false,\"verified\":false,\"verified_type\":-1},\"state\":0},\"state\":0},\"state\":0},\"extend\":{\"source_id\":174,\"status_source_id\":12502,\"mentions\":[]}}}";
	String userdata = "{\"id\":1208210275115470,\"text\":{\"type\":\"user\",\"event\":\"unfollow\",\"source\":{\"id\":1591531043,\"idstr\":\"1591531043\",\"screen_name\":\"IIshang\",\"name\":\"IIshang\",\"province\":\"31\",\"city\":\"15\",\"location\":\"\",\"description\":\"\",\"url\":\"\",\"profile_image_url\":\"http://tp4.sinaimg.cn/1591531043/50/5639714741/0\",\"profile_url\":\"u/1591531043\",\"domain\":\"\",\"weihao\":\"\",\"gender\":\"f\",\"followers_count\":4,\"friends_count\":1460,\"statuses_count\":2,\"favourites_count\":1,\"created_at\":\"Thu Sep 01 20:26:31 +0800 2011\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1,\"allow_all_comment\":false,\"avatar_large\":\"http://tp4.sinaimg.cn/1591531043/180/5639714741/0\",\"verified_reason\":\"\",\"follow_me\":false,\"online_status\":0,\"bi_followers_count\":0,\"lang\":\"zh-cn\"},\"target\":{\"id\":1101000002,\"idstr\":\"2653283511\",\"screen_name\":\"时尚中性搭配\",\"name\":\"时尚中性搭配\",\"province\":\"44\",\"city\":\"1\",\"location\":\"\",\"description\":\"不是淘宝店主，不是时尚买手，与喜欢中性风格的童鞋一同分享中性搭配。投稿或合作请私信。\",\"url\":\"\",\"profile_image_url\":\"http://tp4.sinaimg.cn/2653283511/50/5627224708/0\",\"profile_url\":\"tomboystyle\",\"domain\":\"tomboystyle\",\"weihao\":\"\",\"gender\":\"f\",\"followers_count\":72078,\"friends_count\":44,\"statuses_count\":4087,\"favourites_count\":0,\"created_at\":\"Thu Mar 08 20:21:49 +0800 2012\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":true,\"verified\":false,\"verified_type\":-1,\"allow_all_comment\":true,\"avatar_large\":\"http://tp4.sinaimg.cn/2653283511/180/5627224708/0\",\"verified_reason\":\"\",\"follow_me\":false,\"online_status\":0,\"bi_followers_count\":38,\"lang\":\"zh-cn\"}}}";
	String statusdata = "{\"id\":1208210275115470,\"text\":{\"type\":\"status\",\"event\":\"add\",\"status\":{\"created_at\":\"Wed Aug 22 00:47:51 +0800 2012\",\"id\":3481625520757978,\"text\":\"转发微博\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"geo\":null,\"mid\":\"3481625520757978\",\"user\":{\"id\":1841589682,\"screen_name\":\"吖茵Sophie\",\"name\":\"吖茵Sophie\",\"province\":\"44\",\"city\":\"13\",\"location\":\"\",\"description\":\"从来未亲近问怎么撇下\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"\",\"gender\":\"f\",\"followers_count\":755,\"friends_count\":385,\"statuses_count\":1337,\"favourites_count\":328,\"created_at\":\"Mon Oct 04 18:14:33 +0800 2010\",\"following\":false,\"allow_all_act_msg\":false,\"geo_enabled\":true,\"verified\":false,\"verified_type\":220},\"retweeted_status\":{\"created_at\":\"Wed Aug 22 00:01:05 +0800 2012\",\"id\":3481613747129670,\"text\":\"有些人，喜欢上谁时，总是扭捏不敢下手。而对方想分手时，又宁被嫌弃都不愿离开。何必苦了自己让人看笑话呢？其实爱情里，人们最需要的就是潇洒走一回。真的爱了就大大方方在一起。真不爱了就各回各家，缺了谁都能活。有爱时拿得起，没爱时放得下，洒脱的人生才精彩。——陆琪（上帝保佑，大家晚安）\",\"source\":\"\",\"favorited\":false,\"truncated\":false,\"in_reply_to_status_id\":\"\",\"in_reply_to_user_id\":\"\",\"in_reply_to_screen_name\":\"\",\"thumbnail_pic\":\"\",\"bmiddle_pic\":\"\",\"original_pic\":\"\",\"geo\":null,\"mid\":\"3481613747129670\",\"user\":{\"id\":1601563722,\"screen_name\":\"陆琪\",\"name\":\"陆琪\",\"province\":\"33\",\"city\":\"1\",\"location\":\"\",\"description\":\"电视节目、活动等工作邀约，请联系：18605229277。《潜伏在办公室》《婚姻是女人一辈子的事》者，畅销书作家、编剧。\",\"url\":\"\",\"profile_image_url\":\"\",\"domain\":\"jdluqi\",\"gender\":\"m\",\"followers_count\":2318429,\"friends_count\":756,\"statuses_count\":8199,\"favourites_count\":104,\"created_at\":\"Fri Aug 28 16:14:27 +0800 2009\",\"following\":false,\"allow_all_act_msg\":true,\"geo_enabled\":true,\"verified\":true,\"verified_type\":0},\"state\":0},\"state\":0},\"extend\":{\"source_id\":10361,\"filter\":77,\"mentions\":[1101000002, 1601563722, 2425959300],\"ip\":\"125.88.122.103\"}}}";
}
