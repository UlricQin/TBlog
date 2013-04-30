package org.iperl.beans;

import my.db.POJO;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-23
 */
public class Tag extends POJO {

	private static final long serialVersionUID = -175186951649912278L;
	public static Tag INSTANCE = new Tag();

	private String name;
	private int cnt;

	public Tag() {

	}

	public Tag(String name, int cnt) {
		super();
		this.name = name;
		this.cnt = cnt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	
	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}

}
