package org.iperl.beans;

import my.db.POJO;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-23
 */
public class BlogTag extends POJO {

	private static final long serialVersionUID = -2487455203891033742L;
	public static BlogTag INSTANCE = new BlogTag();

	private int blog;
	private int tag;

	public BlogTag() {

	}

	public BlogTag(int blog, int tag) {
		super();
		this.blog = blog;
		this.tag = tag;
	}

	public int getBlog() {
		return blog;
	}

	public void setBlog(int blog) {
		this.blog = blog;
	}

	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}
	
	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}

}
