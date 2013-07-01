package com.weibo.meyou.notice;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.ByteString;
import com.weibo.meyou.notice.service.NoticeServiceManager;
import com.weibo.meyou.notice.service.WeSyncUtil;
<<<<<<< HEAD
import com.weibo.meyou.notice.utils.Constants;
=======
>>>>>>> 421abd47de4a87b56419f9494104bb31b725add3
import com.weibo.wesync.Command;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.WeSyncURI;
import com.weibo.wesync.data.DataStore;
import com.weibo.wesync.data.FolderID;
import com.weibo.wesync.data.MetaMessageType;
import com.weibo.wesync.data.WeSyncMessage.FolderSyncReq;
import com.weibo.wesync.data.WeSyncMessage.FolderSyncResp;
import com.weibo.wesync.data.WeSyncMessage.GetItemUnreadReq;
import com.weibo.wesync.data.WeSyncMessage.GetItemUnreadResp;
import com.weibo.wesync.data.WeSyncMessage.Meta;
import com.weibo.wesync.data.WeSyncMessage.SyncResp;
import com.weibo.wesync.data.WeSyncMessage.Unread;
<<<<<<< HEAD
=======

//test branch
>>>>>>> 421abd47de4a87b56419f9494104bb31b725add3

public class NoticeServiceTest extends TestCase {
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
		String fromuid = WeSyncUtil.comment;
		String touid = "testrobot";
		String greetings = "greetings!";
		String content = "msg from comment.";
		
		try {
			weSync.getDataService().prepareForNewUser(fromuid);
			weSync.getDataService().prepareForNewUser(touid);
<<<<<<< HEAD
			service.sendNotice(fromuid, touid, greetings, Constants.CONVTYPE_SINGLE);
			service.sendNotice(fromuid, touid, content, Constants.CONVTYPE_SINGLE);
=======
//			service.sendNotice(fromuid, touid, greetings);
//			service.sendNotice(fromuid, touid, content);
>>>>>>> 421abd47de4a87b56419f9494104bb31b725add3

			/// send msg finish, start to read

			String rootId = FolderID.onRoot(touid);
			FolderSyncResp resp = requestFolderSync(touid, WeSyncUtil.TAG_SYNC_KEY, rootId);
			String recvfolderId = resp.getChildId(0);
			List folders = new java.util.ArrayList();
			folders.add(recvfolderId);
			GetItemUnreadResp getItemUnreadResp = requestGetItemUnread(touid, folders);
			String syncKey = null;
			
			for(int i = 0; i < getItemUnreadResp.getUnreadList().size(); i++) {
				Unread u = getItemUnreadResp.getUnreadList().get(i);

				if(i == 0) {
					// init sync key but don't sync anything really
					SyncResp syncResp = WeSyncUtil.sync(touid, u.getFolderId(), WeSyncUtil.TAG_SYNC_KEY, weSync);
					syncKey = syncResp.getNextKey();
				}
				
				// use the synckey to sync the real content
				SyncResp syncResp0 = WeSyncUtil.sync(touid, u.getFolderId(), syncKey, weSync);
				syncKey = syncResp0.getNextKey();
				List<Meta> msgsFromServer = syncResp0.getServerChangesList();
				
				for(Meta meta : msgsFromServer) {
					System.out.println(meta.getContent().toStringUtf8());
					if(i == 0) assertTrue(greetings.equals(meta.getContent().toStringUtf8()));
					else if(i == 0) assertTrue(content.equals(meta.getContent().toStringUtf8()));;
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private GetItemUnreadResp requestGetItemUnread(String username, List<String> folderIds) throws IOException {
		WeSyncURI uri = WeSyncUtil.getWeSyncURI();
		uri.command = Command.GetItemUnread.toByte();
		
		byte[] reqData = null;
		if( null != folderIds ){
			GetItemUnreadReq.Builder reqBuilder = GetItemUnreadReq.newBuilder();
			for(String folderId : folderIds ){
				reqBuilder.addFolderId(folderId);
			}
			reqData = reqBuilder.build().toByteArray();
		}
		
		byte[] respData = weSync.request(username, WeSyncURI.toBytes(uri), reqData);
		return GetItemUnreadResp.parseFrom(respData);
	}
	
	private FolderSyncResp requestFolderSync(String username, String syncKey, String folderId) throws IOException{
		WeSyncURI uri = WeSyncUtil.getWeSyncURI();
		uri.command = Command.FolderSync.toByte();
		//TODO removed? uri.args[1] = syncKey;
		
		FolderSyncReq req = FolderSyncReq.newBuilder().setId(folderId)
				.setKey(syncKey).build();
		byte[] respData = weSync.request(username, WeSyncURI.toBytes(uri),
				req.toByteArray());
		return FolderSyncResp.parseFrom(respData);
	}
	
	private Meta getMetaText(String fromuid, String touid, String content) {
		Meta metatext = Meta.newBuilder()
				.setType( ByteString.copyFrom( new byte[]{MetaMessageType.text.toByte()} ))
				.setId(getNoticeId(fromuid, touid))
				.setContent(ByteString.copyFromUtf8(content))
				.setFrom(fromuid)
				.setTo(touid)
				.setTime( (int) (System.currentTimeMillis() / 1000L) )
				.build();
		
		return metatext;
	}
	
	private String getNoticeId(String fromuid, String touid) {
		return fromuid + touid + System.currentTimeMillis();
	}
	
	
}
