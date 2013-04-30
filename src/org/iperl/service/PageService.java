package org.iperl.service;

import my.mvc.ActionException;

import org.apache.commons.lang.StringUtils;
import org.iperl.beans.Page;
import org.iperl.beans.User;

public class PageService {

	public static void save(String content, User user) {
		if (StringUtils.isBlank(content)) {
			throw new ActionException("content is blank");
		}
		
		int pageid = user.getPageid();
		if (pageid == 0) {
			throw new ActionException("pageid == 0");
		}
		
		Page page = Page.INSTANCE.Get(pageid);
		if (page == null) {
			throw new ActionException("page is null");
		}

		page.updateAttr("content", content);
	}
}
