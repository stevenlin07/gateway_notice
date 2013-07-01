package com.weibo.meyou.notice.iospush.apns;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;

import javapns.data.PayLoad;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.weibo.meyou.notice.model.Device;
import com.weibo.meyou.notice.model.UnreadInfo;
import com.weibo.meyou.notice.service.NoticeData;
import com.weibo.meyou.notice.utils.DebugTools;
import com.weibo.meyou.notice.utils.OpenAPIDataUtils;
import com.weibo.meyou.notice.utils.UserSetting;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-14
 * 
 * a multi-connection version of javapns PushNotificationManager
 *
 */
public class IphonePushManager extends AbstractConnectionPool<SSLSocket>{
	
	protected static final Logger logger = Logger.getLogger("notify_service");
	
	private SSLConnectionHelper connectionHelper;
	
	protected DebugTools dt=DebugTools.getInstance();
	
	private String keyStoreType;
	private String cerFilePath;	//certificate file path
	private String keyStorePwd;
	private String pushHost;
	
	public IphonePushManager(String keyStoreType, String cerFilePath, String keyStorePwd, String pushHost) {
		this.keyStoreType = keyStoreType;
		this.cerFilePath = cerFilePath;
		this.keyStorePwd = keyStorePwd;
		this.pushHost = pushHost;
		
		init();
	}
	
	public void init() {
		try {
			logger.info("Create and init " + keyStoreType + "," + cerFilePath + "," + keyStorePwd  + "," + pushHost);
			this.connectionHelper = new SSLConnectionHelper(cerFilePath, keyStorePwd, keyStoreType);
		} catch (Exception e) {
			logger.error("push manager connection pool initialize failed",e);			
			throw new RuntimeException("iphone push connection init failed", e);
		}
		this.servers=new String[]{pushHost};
		this.initialize();
	}

	public boolean sendNotification(Device device, NoticeData noticeData) throws UnrecoverableKeyException, 
			KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, 
			FileNotFoundException, IOException, Exception {
		if(isWorkingTime(device)){	//if between start_time and end_time
			PayLoad payload = new PayLoad();
			
			String msg = noticeData.content;
			if(msg != null){
				payload.addAlert(msg);
			}
			
			if(noticeData.entries != null){
				String showAlert = noticeData.entries.get(NoticeData.ENTRY_KEY_SHOW_ALERT);
				if(showAlert != null){
					payload.addSound("default");
				}
				
				String chatNum = noticeData.entries.get(NoticeData.ENTRY_KEY_SHOW_NUM);
				if(chatNum != null){
					int badge = Integer.parseInt(chatNum);
					payload.addBadge(badge);
				}
			}
			
			payload.addCustomDictionary(NoticeData.PayloadField.Type.fieldName, noticeData.type.typeValue4apnPayload);
			
			logger.info(this.getClass().getName() + "; payload:" + payload);
			
			String deviceId = device.getDeviceId();
						
			byte[] message=null;
			try {
				message = getMessage(deviceId, payload);
			} catch (Exception e1) {
				logger.error("IhponePushManager send Notification failed,invalid deviceId=" + deviceId + "; uid:" 
							+ device.getUserid(), e1);
				return false;
			}
			for(int i=0;i<initConn;i++){
				SSLSocket socket = this.checkOut(pushHost);
				if(socket==null){
					socket = createNewConn(pushHost);
					if(socket == null){
						throw new IOException("socket pool check out null");
					}
				}
				
//				if(DebugTools.getInstance().isPushWhiteListEffective){
//					if(!DebugTools.getInstance().onDebugList(device.getUserid())){
//						logger.info("push white list effective, do not push to " + device.getUserid());
//						return false;
//					}
//				}
				
				if(DebugTools.getInstance().onDebugList(device.getUserid())){
	                logger.info("Attempting to send apns offline notification [" + payload.toString() + "]" );
				}

				try {
					
					socket.getOutputStream().write(message);
					socket.getOutputStream().flush();
					
					if(DebugTools.getInstance().onDebugList(device.getUserid())){
		                logger.info("send apns offline notification [" + payload.toString() + " success]" );
					}
					
					try {
						this.checkIn(pushHost, socket);
					} catch (Exception e) {
						logger.error("check in socket pool error",e);
					}
					
					return true;
					
				} catch (Exception e) {
					logger.error("Attempting to send Notification failed, payload:" + payload.toString(), e);
					if(socket!=null){
						this.close(pushHost, socket);
					}
			    }
			}
		} else {
			logger.info("apns offline-push is discarded because the device not in work time, " + device);
		}
		
		return false;
	}
	
	private static byte[] getMessage(String deviceToken, PayLoad payload) throws IOException, Exception {
		// First convert the deviceToken (in hexa form) to a binary format
		byte[] deviceTokenAsBytes = new byte[deviceToken.length() / 2];
		deviceToken = deviceToken.toUpperCase();
		int j = 0;
		for (int i = 0; i < deviceToken.length(); i+=2) {
			String t = deviceToken.substring(i, i+2);
			int tmp = Integer.parseInt(t, 16);
			deviceTokenAsBytes[j++] = (byte)tmp;
		}

		// Create the ByteArrayOutputStream which will contain the raw interface
		int size = (Byte.SIZE/Byte.SIZE) + (Character.SIZE/Byte.SIZE) + deviceTokenAsBytes.length + (Character.SIZE/Byte.SIZE) + payload.getPayloadAsBytes().length; 
		ByteArrayOutputStream bao = new ByteArrayOutputStream(size);

		// Write command to ByteArrayOutputStream
		byte b = 0;
		bao.write(b);

		// Write the TokenLength as a 16bits unsigned int, in big endian
		int tl = deviceTokenAsBytes.length;
		bao.write((byte) (tl & 0xFF00) >> 8);
		bao.write((byte) (tl & 0xFF));

		// Write the Token in bytes
		bao.write(deviceTokenAsBytes);

		// Write the PayloadLength as a 16bits unsigned int, in big endian
		int pl = payload.getPayloadAsBytes().length;
		bao.write((byte) (pl & 0xFF00) >> 8);
		bao.write((byte) (pl & 0xFF));

		// Finally write the Payload
		bao.write(payload.getPayloadAsBytes());

		// Return the ByteArrayOutputStream as a Byte Array
		return bao.toByteArray();
	}
	
	protected boolean isWorkingTime(Device device){
		Calendar cal=Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("Hongkong"));
		
		int hourInGivenTimeZone = cal.get(Calendar.HOUR_OF_DAY);
		if(device.getStartTime() < device.getEndTime()){
			return hourInGivenTimeZone>=device.getStartTime() && hourInGivenTimeZone<device.getEndTime();
		}else{
			return (hourInGivenTimeZone >= device.getStartTime() && hourInGivenTimeZone<= 24)
					||(hourInGivenTimeZone >= 0 && hourInGivenTimeZone<= device.getEndTime());
		}
	}
	
	private void getBadgedPayLoad(PayLoad payload, Device device, NoticeData noticeData) throws JSONException{
		int badge=0;
		
		int unreadChatNum = 0;
		if(noticeData.entries != null && noticeData.entries.get(NoticeData.ENTRY_KEY_SHOW_NUM) != null){
			unreadChatNum = 1;
		}

		if(noticeData.type == NoticeData.NoticeType.Chat4Zuju){
			payload.addSound("default");
			
			String chatNum = noticeData.entries.get(NoticeData.ENTRY_KEY_SHOW_NUM);
			if(chatNum != null){
				badge = Integer.parseInt(chatNum);
			}
		} else {
			payload.addCustomDictionary(NoticeData.PayloadField.Cuid.fieldName, String.valueOf(device.getUserid()));
			
			Map<String, String> addEntries = noticeData.entries;
			if (addEntries != null && addEntries.size() != 0){
				for (String key : addEntries.keySet()) {
					String value = addEntries.get(key);
					payload.addCustomDictionary(key, value);
				}
			}
			
			if(UserSetting.isChatNumSet(device.getSwitchInfo())){
				if(unreadChatNum > 0){
					badge += unreadChatNum;
					payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_CHAT, 
							String.valueOf(unreadChatNum));
				}
			}
			
			UnreadInfo userUnread = null;
			try {
				userUnread = OpenAPIDataUtils.instance.getUserUnread(device.getUserid());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);				
			}
			
			if(userUnread != null){
				if(dt.isDebugEnabled() && dt.onDebugList(device.getUserid())){
					logger.info("get badge userid=" + device.getUserid() + "; UnreadInfo:" + userUnread.toString());
				}
				
				int atCmt = userUnread.getAtComment();
				if(atCmt > 0){
					badge += atCmt; 
					payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_MENTION_COMMENT, 
							String.valueOf(userUnread.getAtComment()));
				}
				
				int atStatus = userUnread.getAtStatus();
				if(atStatus > 0){
					badge += atStatus;
					payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_MENTION_STATUS, 
							String.valueOf(atStatus));
				}
				
				int cmt = userUnread.getComment();
				if(cmt > 0){
					badge += cmt;
					payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_COMMENT, 
							String.valueOf(cmt));
				}

				if(UserSetting.isMeyouStautsUnreadNumSet(device.getSwitchInfo())){
					int status = userUnread.getStatus();
					if(status > 0){
						badge += status;
						payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_FEEDS,
								String.valueOf(status));
					}
				}
				
				if(noticeData.type == NoticeData.NoticeType.InstallSucc && 
						UserSetting.isInstallSuccSet(device.getSwitchInfo())){
					//product request
					badge += 1;
					payload.addCustomDictionary(UnreadInfo.PAYLOAD_JSON_FIELD_NEW, "1");
				}
			}
		}
		
		payload.addBadge(badge);
	}
	
	public void doClose(SSLSocket conn) throws IOException, Exception {
		conn.close();
	}

	@Override
	public SSLSocket doCreateConnection(String host) throws Exception {
		String[] ip = host.split(":");
		SSLSocket socket = connectionHelper.getSSLSocket(ip[0],Integer.valueOf(ip[1]));
		socket.startHandshake();
		return socket;
	}

	public boolean isConnected(SSLSocket conn) {
		return conn!=null && conn.isConnected();
	}
	
	public boolean sendNotification4Test(Device device, String msg) throws UnrecoverableKeyException, 
			KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, 
			FileNotFoundException, IOException, Exception {
		PayLoad payload = new PayLoad();
		payload.addBadge(10);
//		payload.addCustomDictionary("type", "1");
		
		if(msg != null){
			payload.addAlert(msg);
		}
		payload.addCustomDictionary("cuid", "2997548741");
		payload.addCustomDictionary("status", "4");
		payload.addCustomDictionary("atStatus", "11");
		payload.addCustomDictionary("mid", "3513006656464549");
		
		
		String deviceId = device.getDeviceId();
		System.err.println("payload:" + payload.toString());
		
		byte[] message=null;
		try {
			message = getMessage(deviceId, payload);
		} catch (Exception e1) {
			logger.error("IhponePushManager send Notification failed,invalid deviceId="+deviceId);
			return false;
		}
		for(int i=0;i<initConn;i++){
			SSLSocket socket = this.checkOut(pushHost);
			if(socket==null){
				socket = createNewConn(pushHost);
				if(socket == null){
					throw new IOException("socket pool check out null");
				}
			}
			
			if(DebugTools.getInstance().onDebugList(device.getUserid())){
		        logger.debug( "Attempting to send Notification [" + payload.toString() + "]" );
			}
			
			try {
				
				socket.getOutputStream().write(message);
				socket.getOutputStream().flush();
				
				System.out.println("send success, payload:" + payload.toString());
				
				logger.info( "Attempting to send Notification [" + payload.toString() + "]" );
				
				try {
					this.checkIn(pushHost, socket);
				} catch (Exception e) {
					logger.error("check in socket pool error",e);
				}
				
				return true;
				
			} catch (Exception e) {
				logger.error("IphonePush sendNotification failed",e);
				if(socket!=null){
					this.close(pushHost, socket);
				}
		    }
		}
		
		return false;
		}	

	public static void main(String[] args) {
		Device device = new Device();
//		device.setDeviceId("ac0c14435ae84b69a444d68b746cd307b79a4aa00870dc7b9bcdd7667c746138");
		device.setDeviceId("123");
		device.setUserid(2997548741l);
		device.setTimezone("Hongkong");
		device.setSwitchInfo(15);
		device.setStartTime(9);
		device.setEndTime(21);
		
//		String filePath = "/usr/ys/work/workspace/binary_meyou_foolish/conf/dev.p12";
//		String filePath = "/usr/ys/work/test/pro.p12";
		String filePath = "/home/yangshuo/桌面/pro.p12";
		
		for (int i = 0; i < 1; i++) {
			IphonePushManager pushor = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE,
					filePath, "123456", ApnsConstants.HOST);
//			IphonePushManager pushor = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE,
//					"../conf/PushService.p12", "123456", ApnsConstants.HOST4Test);
			
			try {
				pushor.sendNotification4Test(device, "2");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("send ok");	
	}
}
