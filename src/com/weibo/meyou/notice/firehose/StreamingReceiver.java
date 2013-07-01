package com.weibo.meyou.notice.firehose;

public interface StreamingReceiver {
	public void processLine(String line);
}
