package org.iperl.beans;

import java.util.List;

import my.db.POJO;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-23
 */
public class BlogCatalog extends POJO {

	private static final long serialVersionUID = 7843062550538033030L;
	public static BlogCatalog INSTANCE = new BlogCatalog();

	private String name;
	private String ident;
	private int dorder;

	public BlogCatalog() {

	}

	public BlogCatalog(String name, String ident, int dorder) {
		super();
		this.name = name;
		this.ident = ident;
		this.dorder = dorder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public int getDorder() {
		return dorder;
	}

	public void setDorder(int dorder) {
		this.dorder = dorder;
	}

	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}

	public int blogCnt() {
		return Blog.INSTANCE.TotalCount("catalog = ?", getId());
	}

}
