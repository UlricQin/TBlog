package org.iperl.action;

import my.mvc.ActionException;
import my.mvc.RequestContext;

import org.apache.commons.lang.StringUtils;
import org.iperl.beans.User;

public class BaseAction {

	protected User safeUser(RequestContext ctx) {
		int userid = ctx.param("userid", 0);
		if (userid == 0) {
			throw new ActionException("Parameter userid is necessary");
		}
		User user = User.INSTANCE.Get(userid);
		if (user == null) {
			throw new ActionException("The user[id:" + userid
					+ "] is not exists");
		}
		return user;
	}
	
	protected void checkBlank(String[] keys, String[] vals) {
		int len = keys.length;
		for (int i = 0; i < len; i++) {
			if (StringUtils.isBlank(vals[i])) {
				throw new ActionException(keys[i] + " can not be blank.");
			}
		}
	}
}
