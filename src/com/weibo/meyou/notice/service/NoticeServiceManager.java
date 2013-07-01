package com.weibo.meyou.notice.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cn.sina.api.commons.util.JsonWrapper;
import cn.sina.api.data.service.StrongTiesService;

import com.google.inject.Inject;
import com.google.protobuf.ByteString;
import com.weibo.meyou.notice.device.service.IDeviceService;
import com.weibo.meyou.notice.iospush.apns.IosOfflineNoticePushManagers;
import com.weibo.meyou.notice.model.Device;
import com.weibo.meyou.notice.service.NoticeData.NoticeType;
import com.weibo.meyou.notice.utils.Constants;
import com.weibo.meyou.notice.utils.UserSetting;
import com.weibo.wejoy.data.service.DataStoreService;
import com.weibo.wejoy.data.storage.MemCacheStorage;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;
import com.weibo.wesync.data.FolderID;
import com.weibo.wesync.data.GroupOperationType;
import com.weibo.wesync.data.MetaMessageType;
import com.weibo.wesync.data.WeSyncMessage.Meta;
import com.weibo.wesync.data.WeSyncMessage.Meta.Builder;

public class NoticeServiceManager {
	private static Logger log = Logger.getLogger(NoticeServiceManager.class);
	public static final String MEYOU_NOTICE_SUFFIX = ".myn";

	private DataService dataService = null;
	private WeSyncService weSync = null;
	private DataStore dataStore;
	private long cacheTime;
	private Map<String, Notice> notices = new HashMap<String, Notice>();
	private LocalStore localstore = new LocalStore();
	public StrongTiesService strongTiesService;
	public MemCacheStorage noticeGWMemCache;
	public IDeviceService deviceService;
	private IosOfflineNoticePushManagers iosApnPushManagers = new IosOfflineNoticePushManagers();
	public static NoticeServiceManager instance;

	private class LocalStore {
		Map<String, String> localSyncKeys = new ConcurrentHashMap<String, String>();
		Map<String, String> localFolderIds = new ConcurrentHashMap<String, String>();

		String getFolderId(String uid, String userChatWith) {
			return localFolderIds.get(uid + "_" + userChatWith);
		}

		void setFolderId(String uid, String userChatWith, String folderId) {
			localFolderIds.put(uid + "_" + userChatWith, folderId);
		}

		String getSynckey(String uid, String folderId) {
			return localSyncKeys.get(uid + "_" + folderId);
		}

		void setSynckey(String uid, String folderId, String synckey) {
			localSyncKeys.put(uid + "_" + folderId, synckey);
		}
	}

	@Inject
	public NoticeServiceManager(DataService dataService, DataStore dataStore, WeSyncService weSync) {
		this.dataService = dataService;
		this.weSync = weSync;
		this.dataStore = dataStore;

		notices.put(Notice.status, new StatusNotice(this));
		notices.put(Notice.comment, new CommentNotice(this));
		notices.put(Notice.install_succ, new InstallSuccNotice(this));
		
//		String config = "applicationContext-ties.xml";
//		ApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{config});
//		strongTiesService = (StrongTiesService) ctx.getBean("strongTieService");
		NoticeServiceManager.instance = this;
	}

	public long getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(long cacheTime) {
		this.cacheTime = cacheTime;
	}

	/**
	 * Return ture if the user have root folder
	 * @param uid
	 * @return
	 */
	public boolean hasMeyou(String uid) {
		if(dataStore.getMaxChildId(FolderID.onRoot(uid)) > 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param uids
	 * @return screen uid from uids that have root folder 
	 */
	public Set<Long> screenUids4InstallMeyou(long[] uids){
		String[] folderIds = new String[uids.length];
		for (int i = 0; i < folderIds.length; i++) {
			folderIds[i] = FolderID.onRoot(String.valueOf(uids[i]));
		}
		
		Set<Long> retSet = new HashSet<Long>();
		
		Map<String, Integer> maxChildIdMap = ((DataStoreService)dataStore).getMaxChildIdMulti(folderIds);
		for (String folderId : maxChildIdMap.keySet()) {
			Integer maxChildId = maxChildIdMap.get(folderId);
			
			if(maxChildId != null && maxChildId >= 0){
				String uid = FolderID.getUsername(folderId);
				retSet.add(Long.parseLong(uid));
			}
		}
		
		return retSet;
	}	

	public void sendNotice(JsonWrapper content) {
		final String type = content.get("type");
		Notice notice = null;
		
		if((notice = notices.get(type)) != null) {
			try {
				Set<String> uniToUidSet = new HashSet<String>();
				List<NoticeData> noticedatas = notice.getNotice(content, uniToUidSet);	//the method will set the uniqueToUidSet				
				
				for(NoticeData noticedata : noticedatas) {
					String toUid = noticedata.touid;
					Device device = deviceService.getDeviceByUserId(Long.parseLong(toUid));
					if(device != null){	//user has registered ios
						int switchInfo = device.getSwitchInfo();
						NoticeType noticeType = noticedata.type;
						
						if((NoticeType.Comment == noticeType || NoticeType.At == noticeType) 
								&& !UserSetting.isAtCommentTextSet(switchInfo)){
							log.debug(toUid + " set not push at & comment text, msg:" + noticedata.content + 
									"; noticeType:" + noticedata.type);
                    		noticedata.content = null;
                    		noticedata.clearEntries();
                    		noticedata.type = NoticeData.NoticeType.UnReadNum;							
						}
						iosApnPushManagers.sendNotification(device, noticedata);
					} else {						
						log.debug("offline msg to " +
								toUid + " is discarded because the user has not registered ios; msg "
								+ noticedata.content);
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}		
	}
	
	private Set<String> screenOnlineUid(Set<String> uniToUidSet){
		Set<String> retSet = new HashSet<String>();
		
		for (String toUid : uniToUidSet) {
			if(noticeGWMemCache.get(toUid) != null){
				retSet.add(toUid);
			}
		}
		
		return retSet;
	}
	
	public void broadcast(String groupId, GroupOperationType type, String affectedUser){
		dataService.broadcastMemberChange(groupId, type, affectedUser);
	}

	public void sendNotice(String fromuid, String touid, String content, String convType) {
		MetaMessageType msgType = null;
		try {
			new cn.vika.imus.util.JsonWrapper(content);
			msgType = MetaMessageType.mixed;
		} catch (Exception e) {
			msgType = MetaMessageType.text;
			e.printStackTrace();
		}
		
		log.info("+++NoticeServiceManager msgType:" + msgType + "; content:" + content);
		
		try {
			String folderId = null;
			if((folderId = localstore.getFolderId(fromuid, touid)) == null) {
				if(Constants.CONVTYPE_GROUP.equals(convType)){
					folderId = FolderID.onGroup(fromuid, touid);
					boolean isFolderExist = weSync.getDataService().isFolderExist(folderId);
					if(!isFolderExist){
						weSync.getDataService().newGroupChat(fromuid, touid);
					}
				} else {
					folderId = FolderID.onConversation(fromuid, touid);
					boolean isFolderExist = weSync.getDataService().isFolderExist(folderId);
					if(!isFolderExist){
						weSync.getDataService().newConversation(fromuid, touid);
					}
				}
				
				localstore.setFolderId(fromuid, touid, folderId);
			}

			String synckey = null;
			if((synckey = localstore.getSynckey(fromuid, folderId)) == null) {
				synckey = WeSyncUtil.initSyncKey(fromuid, folderId, weSync);
				localstore.setSynckey(fromuid, folderId, synckey);
			}


			Builder metaBuilder = Meta.newBuilder()					
					.setId(getNoticeId(fromuid, touid))
					.setFrom(fromuid)
					.setTo(touid)
					.setTime( (int) (System.currentTimeMillis() / 1000L) )
					.setType(ByteString.copyFrom(new byte[] {msgType.toByte()}));
			
			if(content != null){
				metaBuilder.setContent(ByteString.copyFromUtf8(content));
			}
			
			Meta metatext = metaBuilder.build();

			log.debug(String.format("%s ; WeSyncUtil.syncMeta, fromuid:%s, folderId,:%s, synckey:%s, metatext:%s",
					this.getClass().getName(), fromuid, folderId, synckey, metatext));
			
			String nextSyncKey = WeSyncUtil.syncMeta(fromuid, folderId, synckey, metatext, weSync);
			localstore.setSynckey(fromuid, folderId, nextSyncKey);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getNoticeId(String fromuid, String touid) {
		return fromuid + touid + System.currentTimeMillis();
	}

	public String getSuffix() {
		return MEYOU_NOTICE_SUFFIX;
	}
}
