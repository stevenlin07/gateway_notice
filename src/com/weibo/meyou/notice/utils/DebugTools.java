package com.weibo.meyou.notice.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * 
 * @author yangshuo3
 * @date 2012-10-12
 *
 */
public class DebugTools implements Runnable{
	private Logger log=Logger.getLogger(DebugTools.class);
	private static DebugTools instance=new DebugTools();
	private volatile boolean isDebugEnabled=false;
	private volatile List<Object> debugList=new ArrayList<Object>();
	public volatile boolean isPushWhiteListEffective = true;
	public static DebugTools getInstance() {
		return instance;
	}
	
	private DebugTools(){
		Thread t=new Thread(this);
//		t.setDaemon(true);
		t.start();
	}

	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}
	
	public boolean cantains(String content){
		boolean ret=false;
		for(int i=0;i<debugList.size();i++){
			if(content.contains(debugList.get(i).toString())){
				ret=true;
				break;
			}
		}
		
		return ret;
	}
	
	public boolean onDebugList(Object...args) {
		boolean ret=false;
		for(int i=0;i<args.length;i++){
			if(debugList.contains(args[i].toString())){
				ret=true;
				break;
			}
		}
		return ret;
	}

	public void run() {
		while(true){
			Properties props=new Properties();
//			InputStream input = DebugTools.class.getResourceAsStream("../conf/notice_debug_tools.prop");
			InputStream input = null;
			try {
//				input = new FileInputStream("../conf/notice_debug_tools.prop");
				input = this.getClass().getClassLoader().getResourceAsStream("notice_debug_tools.prop");
//				input = new FileInputStream("/usr/ys/work/workspace/meyou_service/meyou_notice/conf/notice_debug_tools.prop");
				
				props.load(input);
			} catch (IOException e1) {
				log.error("load debug info properties failed",e1);
			}finally{
				if(input!=null){
					try {
						input.close();
					} catch (IOException e) {
					}
				}
			}
			
			
			String debugOn = props.getProperty("iospush_debug_on");
			if(debugOn!=null && debugOn.equals("true")){
				isDebugEnabled=true;
			}
			
			String debugWords=props.getProperty("iospush_debug_list");
			if(debugWords!=null && !debugWords.equals("")){
				Object[] words = debugWords.split("_");
				debugList=Arrays.asList(words);
			}
			
			String pushWhiteListEffe = props.getProperty("is_push_white_list_effective");
			if(pushWhiteListEffe != null && pushWhiteListEffe.equals("0")){
				isPushWhiteListEffective = false;
			} else {
				isPushWhiteListEffective = true;
			}
			
			log.info("reload debug tools info,debug on="+isDebugEnabled+",debugList="+debugWords+",isOnDebugList="+onDebugList(1779195673L)
					+ ",isPushWhiteListEffective=" + isPushWhiteListEffective);
			try {
				Thread.sleep(1000 * 30);
			} catch (InterruptedException e) {
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		DebugTools dt=DebugTools.getInstance();
		Thread.sleep(1000);
		System.out.println(dt.isDebugEnabled());
		System.out.println(dt.cantains("sdfsdfsadfa,w23sdfdsdf 1779195673dfsdfsdf"));;
		System.out.println(dt.onDebugList(1779195673L));
	}
	
}
