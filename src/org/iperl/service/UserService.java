package org.iperl.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import my.mvc.ActionException;
import my.mvc.IUser;
import my.mvc.RequestContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.beans.User;

public class UserService {
	
	public static final String G_USER = "g_user";

	public static User GetLoginUser(HttpServletRequest req) {
		Object loginUser = req.getAttribute(G_USER);
		if (loginUser == null) {
			// get user id from cookie
			IUser cookie_user = RequestContext.get().getUserFromCookie();
			if (cookie_user == null) {
				return null;
			}
			User user = User.INSTANCE.Get(cookie_user.getId());
			if (user != null
					&& StringUtils.equalsIgnoreCase(user.getPwd(),
							cookie_user.getPwd())) {
				req.setAttribute(G_USER, user);
				return user;
			}
		}
		return (User) loginUser;
	}
	
	public static User Login(String ident, String pwd) {
		if (StringUtils.isBlank(ident) || StringUtils.isBlank(pwd)) {
			throw new ActionException("ident or pwd is blank");
		}
		
		User user = User.INSTANCE.GetByAttr("ident", ident);
		if (user == null) {
			throw new ActionException("You are not exists");
		}
		
		if (!(StringUtils.equalsIgnoreCase(user.getPwd(),DigestUtils.shaHex(pwd)))) {
			throw new ActionException("Password is wrong");
		}
		
		if (user.IsBlocked()) {
			throw new ActionException("You are banned. Any question? mail to admin");
		}
		
		return user;
	}

	public static User byIdent(String ident) {
		// 实际只有个一个匹配，用IDs这个方法只是为了缓存
		List<Long> ids = User.INSTANCE.IDs("where ident = ?", ident);
		if(CollectionUtils.isEmpty(ids)){
			return null;
		}
		Long id = ids.get(0);
		return User.INSTANCE.Get(id);
	}

	public static List<User> topAuthor(int count) {
		List<Long> ids = User.INSTANCE.IDs("where blogcnt > 0 order by blogcnt desc limit ?", count);
		return User.INSTANCE.LoadList(ids);
	}
}
