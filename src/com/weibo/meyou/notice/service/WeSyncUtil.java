package com.weibo.meyou.notice.service;

import java.io.IOException;
import java.util.HashSet;

import com.google.protobuf.ByteString;
import com.weibo.wesync.Command;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.WeSyncURI;
import com.weibo.wesync.data.GroupOperationType;
import com.weibo.wesync.data.MetaMessageType;
import com.weibo.wesync.data.WeSyncMessage.FolderCreateReq;
import com.weibo.wesync.data.WeSyncMessage.FolderCreateResp;
import com.weibo.wesync.data.WeSyncMessage.FolderSyncReq;
import com.weibo.wesync.data.WeSyncMessage.GroupOperation;
import com.weibo.wesync.data.WeSyncMessage.Meta;
import com.weibo.wesync.data.WeSyncMessage.MetaSet;
import com.weibo.wesync.data.WeSyncMessage.SyncReq;
import com.weibo.wesync.data.WeSyncMessage.SyncResp;

public class WeSyncUtil {
	public static final String comment = "comment_meyou";
	public static final String status = "status_meyou";
	public static final String user = "user_meyou";
	public static final String unread = "unread_meyou";
	
	public static String TAG_SYNC_KEY = "0";
	
	public static WeSyncURI getWeSyncURI(){
		WeSyncURI uri = new WeSyncURI();
		uri.protocolVersion = 10;
		uri.guid = "serverguid";
		uri.deviceType = "server";		
		return uri;
	}
	
	public static String createFolder(String username, String userChatWith, WeSyncService weSync) throws IOException {
		WeSyncURI uri = getWeSyncURI();
		uri.command = Command.FolderCreate.toByte();
		//TODO removed? uri.args[1] = syncKey;
		
		FolderCreateReq req = FolderCreateReq.newBuilder()
				.setUserChatWith(userChatWith).build();
		
		byte[] respData = weSync.request(username, WeSyncURI.toBytes(uri), req.toByteArray());
		FolderCreateResp resp = FolderCreateResp.parseFrom(respData);
		String folderId = resp.getFolderId();
		return folderId;
	}

	//create folder username_group_userChatWith
	public static String createGroupChatFolder(String username, String userChatWith, WeSyncService weSync) throws IOException {
		WeSyncURI uri = getWeSyncURI();
		uri.command = Command.FolderCreate.toByte();
		
		FolderCreateReq req = FolderCreateReq.newBuilder()
				.setUserChatWith(userChatWith)
				.addAnotherUser(username)
				.build();
		
		byte[] respData = weSync.request(username, WeSyncURI.toBytes(uri), req.toByteArray());
		FolderCreateResp resp = FolderCreateResp.parseFrom(respData);
		String folderId = resp.getFolderId();		
		return folderId;
	}
	
	public static String initSyncKey(String fromuid, String folderId, WeSyncService weSync) throws IOException {
		WeSyncURI uri = WeSyncUtil.getWeSyncURI();
		uri.command = Command.Sync.toByte();
		
		SyncReq.Builder reqBuilder = SyncReq.newBuilder()
				.setFolderId(folderId)
				// if you don't really want to sync all content from server, set to false
				.setIsFullSync(false)
				// the syncKey tell server what the returned synckey should be
				.setKey(TAG_SYNC_KEY);
		byte[] respData = weSync.request(fromuid, WeSyncURI.toBytes(uri), reqBuilder.build().toByteArray());
		SyncResp syncResp = SyncResp.parseFrom(respData);
		String nextSyncKey = syncResp.getNextKey();
		return nextSyncKey;
	}
	
	public static String syncMeta(String fromuid, String folderId, String synckey, Meta metatext, 
		WeSyncService weSync) throws IOException 
	{
		WeSyncURI uri = WeSyncUtil.getWeSyncURI();
		uri.command = Command.Sync.toByte();
		
		SyncReq.Builder reqBuilder = SyncReq.newBuilder()
				.setFolderId(folderId)
				.addClientChanges(metatext)
				.setKey(synckey);
		
		byte[] respData = weSync.request(fromuid, WeSyncURI.toBytes(uri), reqBuilder.build().toByteArray());
		SyncResp syncResp = SyncResp.parseFrom(respData);
		String nextSyncKey = syncResp.getNextKey();
		return nextSyncKey;
	}
	
	public static SyncResp sync(String fromuid, String folderId, String synckey, WeSyncService weSync) throws IOException 
	{
		WeSyncURI uri = WeSyncUtil.getWeSyncURI();
		uri.command = Command.Sync.toByte();
			
		SyncReq.Builder reqBuilder = SyncReq.newBuilder()
				.setFolderId(folderId)
				.setKey(synckey);
			
		byte[] respData = weSync.request(fromuid, WeSyncURI.toBytes(uri), reqBuilder.build().toByteArray());
		SyncResp syncResp = SyncResp.parseFrom(respData);
		return syncResp;
	}
}
