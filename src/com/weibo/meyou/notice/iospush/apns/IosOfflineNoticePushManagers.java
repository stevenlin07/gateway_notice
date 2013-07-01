package com.weibo.meyou.notice.iospush.apns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.log4j.Logger;

import com.weibo.meyou.notice.model.Device;
import com.weibo.meyou.notice.service.NoticeData;
import com.weibo.meyou.notice.utils.StorageUtil;
import com.weibo.meyou.notice.utils.UserSetting;


public class IosOfflineNoticePushManagers {
	private static Logger log = Logger.getLogger(IosOfflineNoticePushManagers.class);
	
//	private final String MEYOU_CERTIFICATE_FILE_PATH = "../conf/aps_meyou_identity.p12";
//	private final String MEYOU_KEY_STORE_PWD = "abc123";
	
	private final String ZUJU_CERTIFICATE_FILE_PATH = "pro.p12";
	private final String TEST_CERTIFICATE_FILE_PATH = "dev.p12";
	private final String ZUJU_KEY_STORE_PWD = "123456";
	
//	private final String TEST_CERTIFICATE_FILE_PATH = "../conf/test4meyou.p12";
//	private final String TEST_KEY_STORE_PWD = "123456";
	
	private IphonePushManager testPushManager = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE, 
			TEST_CERTIFICATE_FILE_PATH, ZUJU_KEY_STORE_PWD, ApnsConstants.HOST4Test);
	
	private IphonePushManager zujuPushManager = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE,
			ZUJU_CERTIFICATE_FILE_PATH, ZUJU_KEY_STORE_PWD, ApnsConstants.HOST);
	
	public void sendNotification(Device device, NoticeData noticeData) throws UnrecoverableKeyException, KeyManagementException, 
				KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, 
				IOException, Exception{
		if(device != null){
			log.debug(device.getUserid() + " send IOS offline push notice by APNs device=" + 
					(device == null ? "null" : device.getAppkey() + "|"+ device.getDeviceId()) 
					+ " msg=" + noticeData.content);
			
			zujuPushManager.sendNotification(device, noticeData);
			
			//TODO only for test
			testPushManager.sendNotification(device, noticeData);
		}
	}
	
	
	
	public static void main(String[] args) {
//		//tes meyou
//		long toUid = 2635035482l;
//		Device device = new Device();
//		device.setDeviceId("c77fd687df217dbe0ddf14a3b48240bb5a1408fa8ee2f3ea054e320b9db8822c");
//		device.setUserid(toUid);
//		device.setTimezone("Hongkong");
//		device.setSwitchInfo(15);
//		device.setStartTime(9);
//		device.setEndTime(21);
//		
//		for (int i = 0; i < 1; i++) {
//			IphonePushManager pushor = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE,
//					"conf/dev.p12", "123456", ApnsConstants.HOST4Test);
//			
//			try {
//				NoticeData noticeData = new NoticeData();
//				noticeData.content = "test2";
//				noticeData.touid = String.valueOf(toUid);
//				pushor.sendNotification(device, noticeData);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		try {
			FileInputStream fis = new FileInputStream("conf/aps_meyou_identity.p12");
			System.out.println(fis.available());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("send ok");
		
		//tes meyou
//		Device device = new Device();
//		device.setDeviceId("51c4d557ec9e562d06f31a4d49efb0220ae87f96bb1dc3a26643f2f76d755a64");
//		device.setUserid(1151247324l);
//		device.setTimezone("Hongkong");
//		device.setSwitchInfo(15);
//		device.setStartTime(9);
//		device.setEndTime(21);
//		
//		for (int i = 0; i < 1; i++) {
//			IphonePushManager pushor = new IphonePushManager(ApnsConstants.KEY_STORE_TYPE,
//					"/usr/ys/work/tmpProj/meyou_service/meyou_notice/conf/dev.p12", "123456", ApnsConstants.HOST4Test);
//			try {
//				pushor.sendNotification(device, "zuju");
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		System.out.println("send ok");		
	}
	
	
}
