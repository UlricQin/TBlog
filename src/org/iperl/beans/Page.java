package org.iperl.beans;

import my.db.POJO;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-28
 */
public class Page extends POJO {

	private static final long serialVersionUID = -6937830725872019323L;

	public static Page INSTANCE = new Page();

	private int user;
	private String content;

	public Page() {

	}

	public Page(int user, String content) {
		this.user = user;
		this.content = content;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}
	
	@Override
	protected long GetAutoLoadUser() {
		return getUser();
	}
	
	@Override
	protected boolean IsAutoLoadUser() {
		return true;
	}

}
