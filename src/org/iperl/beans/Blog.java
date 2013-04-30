package org.iperl.beans;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import my.db.POJO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.toolbox.BlogTool;

/**
 * @author Ulric Qin<cnperl@163.com>
 * @date 2013-4-23
 */
public class Blog extends POJO {

	private static final long serialVersionUID = -8113617651261262567L;

	public static Blog INSTANCE = new Blog();

	public static final byte STATUS_DRAFT = 0;
	public static final byte STATUS_PUBED = 1;
	public static final byte TYPE_ORIGINAL = 0;
	public static final byte TYPE_TRANSLATE = 1;
	public static final byte TYPE_TRANSFER = 2;

	private String title;
	private String ident;
	private String keywords;
	private String desn;
	private byte type;
	private String url;
	private String content;
	private int user;
	private int catalog;
	private byte status;
	private Timestamp ctime = new Timestamp(System.currentTimeMillis());
	private int hits;

	private String showCatalog;

	public Blog() {

	}

	public Blog(String title, String ident, String keywords, String desn,
			byte type, String url, String content, int user, int catalog,
			byte status) {
		super();
		this.title = title;
		this.ident = ident;
		this.keywords = keywords;
		this.desn = desn;
		this.type = type;
		this.url = url;
		this.content = content;
		this.user = user;
		this.catalog = catalog;
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDesn() {
		return desn;
	}

	public void setDesn(String desn) {
		if (StringUtils.isBlank(desn)) {
			this.desn = "";
			return;
		}

		int len = desn.length();
		if (len < 500) {
			this.desn = desn;
		} else {
			this.desn = desn.substring(0, 500);
		}
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getUser() {
		return user;
	}

	public void setUser(int user) {
		this.user = user;
	}

	public int getCatalog() {
		return catalog;
	}

	public void setCatalog(int catalog) {
		this.catalog = catalog;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}

	public Timestamp getCtime() {
		return ctime;
	}

	public void setCtime(Timestamp ctime) {
		this.ctime = ctime;
	}

	public int getHits() {
		return hits;
	}

	public void setHits(int hits) {
		this.hits = hits;
	}

	public String getShowCatalog() {
		if (StringUtils.isBlank(this.showCatalog)) {
			this.showCatalog = BlogTool.htmlContent(this.getContent());
		}
		return showCatalog;
	}

	public void setShowCatalog(String showCatalog) {
		this.showCatalog = showCatalog;
	}

	@Override
	protected boolean IsObjectCachedByID() {
		return true;
	}

	@Override
	protected boolean IsAutoLoadUser() {
		return true;
	}

	@Override
	protected long GetAutoLoadUser() {
		return this.getUser();
	}

	@Override
	protected Map<String, Object> ListInsertableFields() {
		Map<String, Object> map = super.ListInsertableFields();
		map.remove("showCatalog");
		return map;
	}

	public boolean pubed() {
		return getStatus() == 1;
	}

	public User userObj() {
		return User.INSTANCE.Get(this.getUser());
	}

	public BlogCatalog catalogObj() {
		return BlogCatalog.INSTANCE.Get(this.getCatalog());
	}

	public List<String> tags() {
		String keywords = this.getKeywords();
		if (StringUtils.isBlank(keywords)) {
			return Collections.emptyList();
		}
		String[] tags = StringUtils.split(keywords, ',');
		return Arrays.asList(tags);
	}

	public Blog pre() {
		List<Long> ids = IDs("where id < ? and status = ? order by id desc limit 1", getId(),
				STATUS_PUBED);
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		Long id = ids.get(0);
		return Get(id);
	}
	
	public Blog next() {
		List<Long> ids = IDs("where id > ? and status = ? order by id limit 1", getId(),
				STATUS_PUBED);
		if (CollectionUtils.isEmpty(ids)) {
			return null;
		}
		Long id = ids.get(0);
		return Get(id);
	}
}
