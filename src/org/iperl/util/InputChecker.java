package org.iperl.util;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class InputChecker {

	public static boolean identIsOK(String ident) {
		if (StringUtils.isBlank(ident)) {
			return false;
		}
		
		ident = Jsoup.clean(ident, Whitelist.none());
		if (StringUtils.isBlank(ident)) {
			return false;
		}
		
		if (StringUtils.isAlphanumeric(ident)) {
			return true;
		} else {
			return false;
		}
	}

}
