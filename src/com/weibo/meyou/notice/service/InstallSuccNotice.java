package com.weibo.meyou.notice.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.weibo.meyou.notice.utils.OpenAPIDataUtils;

import cn.sina.api.commons.util.JsonWrapper;

public class InstallSuccNotice extends Notice {
	
	Logger log = Logger.getLogger(InstallSuccNotice.class);
	
	public InstallSuccNotice(NoticeServiceManager noticeService) {
		super.service = noticeService;
	}

	@Override
	public List<NoticeData> getNotice(JsonWrapper content, Set<String> uniToUidSet) throws Exception {
		try {
			long senderId = Long.parseLong(content.get("fromUid"));
			log.debug("/////////////////////1");
			System.out.println("////////////1");
			String[] strToUids = content.get("toUids").split(",");
			long[] toUids = new long[strToUids.length];
			for (int i = 0; i < toUids.length; i++) {
				String strToUid = strToUids[i];
				long toUid = Long.parseLong(strToUid); 
				toUids[i] = toUid;
				uniToUidSet.add(strToUid);
			}
			
			List<NoticeData> noticeDatas = super.getNoticeDatas(toUids, senderId, 
					null, false, NoticeData.NoticeType.InstallSucc, null);
			
			return noticeDatas;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return Collections.EMPTY_LIST;
		}
		
	}

}
