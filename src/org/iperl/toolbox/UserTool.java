package org.iperl.toolbox;

import java.util.List;

import my.toolbox.CoreTool;

import org.apache.commons.lang.StringUtils;
import org.iperl.beans.Page;
import org.iperl.beans.User;
import org.iperl.service.UserService;

public class UserTool {

	public static Page page() {
		User user = CoreTool.user();
		if (user == null) {
			return null;
		}
		int pageid = user.getPageid();
		if (pageid == 0) {
			// 第一次访问，需要创建
			Long userId = user.getId();
			Page page = new Page(userId.intValue(), "");
			page.Save();
			return page;
		}
		return Page.INSTANCE.Get(pageid);
	}

	public static Page pageByIdent(String ident) {
		if (StringUtils.isBlank(ident)) {
			return null;
		}

		User user = byIdent(ident);
		if (user == null) {
			return null;
		}

		int pageid = user.getPageid();
		if (pageid == 0) {
			Long userId = user.getId();
			Page page = new Page(userId.intValue(), "");
			page.Save();
			return page;
		}
		return Page.INSTANCE.Get(pageid);
	}

	public static User byIdent(String ident) {
		if (StringUtils.isBlank(ident)) {
			return null;
		}

		User user = UserService.byIdent(ident);
		return user;
	}

	public static User detail(int id) {
		return User.INSTANCE.Get(id);
	}
	
	public static List<User> topAuthor(int count){
		return UserService.topAuthor(count);
	}
}
