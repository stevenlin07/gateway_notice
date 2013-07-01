package com.weibo.meyou.notice.utils;

public class CommonUtil {

	/**
	 * 计算字符串长度 汉字算1个，英文字符和数字算0.5个
	 * 
	 * @param str
	 *            待处理字符串
	 * @return
	 */
	public static int countLen(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		int len = str.length();
		int index = 0;
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if ((c >= 0x4E00 && c <= 0x9FFF)) {
				index += 2;
			} else {
				index++;
			}
		}
		return index % 2 == 0 ? index / 2 : (index / 2 + 1);
	}
}
