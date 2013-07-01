package com.weibo.meyou.notice.firehose;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.weibo.meyou.notice.device.service.IDeviceService;
import com.weibo.wejoy.data.storage.MemCacheStorage;
import com.weibo.wejoy.data.util.XmlUtil;
import com.weibo.wesync.DataService;
import com.weibo.wesync.WeSyncService;
import com.weibo.wesync.data.DataStore;

public class FirehoseManager {
	private static Logger log = Logger.getLogger(FirehoseManager.class);
	private static final String configfile = "firehose.xml";
	private static FirehoseManager instance;
	
	public DataService dataService;
	public DataStore dataStore;
	public WeSyncService weSync;
	public MemCacheStorage<Object> noticeGWMemCache;
	public IDeviceService deviceService;
	
	public static FirehoseManager getInstance() {
		if(instance == null) {
			synchronized(FirehoseManager.class) {
				if(instance == null) {
					instance = new FirehoseManager();
				}
			}
		}
		
		return instance;
	}
	
	private FirehoseManager() {
	}
	
	public void start() {
		FileInputStream in = null;
		SAXReader xmlReader = new SAXReader();
		
		try {
			URL url = FirehoseManager.class.getClassLoader().getResource(configfile);
			in = new FileInputStream(url.getFile());
			Document document = xmlReader.read(in);
			Element firehoseElem = XmlUtil.getElementByName(document, "firehose");
			
			if(firehoseElem == null) {
				log.info("no firehose server found.");
				return;
			}
			
			String instanceNum = XmlUtil.getAttByName(firehoseElem, "instanceNum");
			String instanceTotal = XmlUtil.getAttByName(firehoseElem, "instanceTotal");
			List<Element> addresses = XmlUtil.getChildElements(firehoseElem);
			log.debug("Start firehose " + addresses.size() + " address.");
			
			for(Element elem : addresses) {
				String master = XmlUtil.getAttByName(elem, "master");
				String slave = XmlUtil.getAttByName(elem, "slave");
				
				StreamingReceiverImpl streamingReceiver = new StreamingReceiverImpl(dataService, dataStore, weSync,
						noticeGWMemCache, deviceService);
				streamingReceiver.setInstanceNum(Integer.valueOf(instanceNum));
				streamingReceiver.setInstanceTotal(Integer.valueOf(instanceTotal));
				streamingReceiver.setCurStreamURLIndex(0);
				List list = new ArrayList();
				list.add(master);
				list.add(slave);
				streamingReceiver.setStreamingURLList(list);
				streamingReceiver.init();
			}
		}
		catch(Exception e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
			} 
			catch (Exception e) {
				// ignore
			}
		}
	}
}
