package org.iperl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import my.cache.CacheManager;
import my.toolbox.CoreTool;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.beans.Blog;
import org.iperl.beans.BlogTag;
import org.iperl.beans.Tag;
import org.iperl.beans.User;
import org.iperl.dto.Pagination;

public class BlogService {

	private static final String myIds = "where user = ? and status = ? order by id desc";
	private static final String homeIds = "where status = ? order by id desc";

	public static Long addBlog(Blog blog) {
		Long id = blog.Save();
		User user = CoreTool.user();
		boolean pubed = blog.pubed();
		byte flag = pubed ? Blog.STATUS_PUBED : Blog.STATUS_DRAFT;
		if (pubed) {
			TagService.batchAddTags(id, blog.getKeywords());
			user.updateAttr("blogcnt", user.getBlogcnt() + 1);
			CacheManager.evict(Blog.INSTANCE.CacheRegion(), homeIds
					+ Blog.STATUS_PUBED);
		}
		CacheManager.evict(Blog.INSTANCE.CacheRegion(), myIds + user.getId()
				+ flag);

		return id;
	}

	public static List<Blog> draft() {
		User user = CoreTool.user();
		List<Long> ids = Blog.INSTANCE.IDs(myIds, user.getId(),
				Blog.STATUS_DRAFT);
		return Blog.INSTANCE.LoadList(ids);
	}

	// 这个地方会不会出现查询出太多结果捏？
	// 个人博客来讲，少于300应该都是很OK的
	// 得，如果某个账号发表了太多博文，可以换个马甲，这个地方就先这样吧
	public static List<Blog> pubed() {
		User user = CoreTool.user();
		List<Long> ids = Blog.INSTANCE.IDs(myIds, user.getId(),
				Blog.STATUS_PUBED);
		return Blog.INSTANCE.LoadList(ids);
	}

	public static boolean delBlog(Blog blog) {
		boolean pubed = blog.pubed();
		if (!blog.Delete()) {
			return false;
		}

		User user = CoreTool.user();
		// tags
		if (pubed) {
			TagService.batchDelTags(blog.getId(), blog.getKeywords());
			user.updateAttr("blogcnt", user.getBlogcnt() - 1);
		}

		byte flag = pubed ? Blog.STATUS_PUBED : Blog.STATUS_DRAFT;
		CacheManager.evict(Blog.INSTANCE.CacheRegion(), myIds + user.getId()
				+ flag);
		return true;
	}

	public static boolean update(Blog blog, String title, String ident,
			String keywords, String desn, Integer type, String url,
			String content, int catalog, Integer status) {
		String oldKeywords = blog.getKeywords();
		boolean statusChanged = status.byteValue() != blog.getStatus();
		boolean pubed = status.byteValue() == Blog.STATUS_PUBED;

		boolean success = blog.updateAttrs(new String[] { "title", "ident",
				"keywords", "desn", "type", "url", "content", "catalog",
				"status" }, new Object[] { title, ident, keywords, desn, type,
				url, content, catalog, status });

		if (!success) {
			return success;
		}

		if (!pubed && statusChanged && StringUtils.isNotBlank(oldKeywords)) {
			TagService.batchDelTags(blog.getId(), oldKeywords);
		} else {
			// published
			TagService.batchDelTags(blog.getId(), oldKeywords);
			TagService.batchAddTags(blog.getId(), keywords);
		}

		if (statusChanged) {
			User user = CoreTool.user();
			user.updateAttr("blogcnt", user.getBlogcnt() + (pubed ? 1 : -1));
			CacheManager.evict(Blog.INSTANCE.CacheRegion(),
					myIds + user.getId() + Blog.STATUS_PUBED);
			CacheManager.evict(Blog.INSTANCE.CacheRegion(),
					myIds + user.getId() + Blog.STATUS_DRAFT);
			CacheManager.evict(Blog.INSTANCE.CacheRegion(), homeIds
					+ Blog.STATUS_PUBED);
		}

		return true;
	}

	public static Pagination topNew(int pageno, int pagesize) {
		List<Long> ids = Blog.INSTANCE.IDs(homeIds, Blog.STATUS_PUBED);
		int size = ids.size();
		int beginIndex = (pageno - 1) * pagesize;
		int toIndex = pageno * pagesize;
		List<Long> returnIds = ids.subList(beginIndex, toIndex > size ? size
				: toIndex);
		List<Blog> list = Blog.INSTANCE.LoadList(returnIds);
		return new Pagination(size, list);
	}

	public static List<Blog> topHits(int count) {
		List<Long> ids = Blog.INSTANCE.IDs(
				"where status = ? order by hits desc limit ?",
				Blog.STATUS_PUBED, count);
		return Blog.INSTANCE.LoadList(ids);
	}

	public static Pagination byCatalog(int catalogId, int pageno, int pagesize) {
		List<Long> ids = Blog.INSTANCE.IDs("where catalog = ? and status = ? order by id desc", catalogId, Blog.STATUS_PUBED);
		int size = ids.size();
		int beginIndex = (pageno - 1) * pagesize;
		int toIndex = pageno * pagesize;
		List<Long> returnIds = ids.subList(beginIndex, toIndex > size ? size
				: toIndex);
		List<Blog> list = Blog.INSTANCE.LoadList(returnIds);
		return new Pagination(size, list);
	}

	public static Pagination byTag(Tag tagObj, int pageno, int pagesize) {
		List<Long> btids = BlogTag.INSTANCE.IDs("where tag = ?", tagObj.getId());
		if(CollectionUtils.isEmpty(btids)){
			return null;
		}
		
		List<BlogTag> blogTags = BlogTag.INSTANCE.LoadList(btids);
		
		List<Long> ids = new ArrayList<Long>(blogTags.size());
		for(BlogTag bt : blogTags){
			long bid = bt.getBlog();
			ids.add(bid);
		}
		
		Collections.sort(ids);
		Collections.reverse(ids);
		
		int size = ids.size();
		int beginIndex = (pageno - 1) * pagesize;
		int toIndex = pageno * pagesize;
		List<Long> returnIds = ids.subList(beginIndex, toIndex > size ? size
				: toIndex);
		List<Blog> list = Blog.INSTANCE.LoadList(returnIds);
		return new Pagination(size, list);
	}

	public static Pagination byUser(int userId, int pageno, int pagesize) {
		List<Long> ids = Blog.INSTANCE.IDs("where user = ? and status = ? order by id desc", userId, Blog.STATUS_PUBED);
		int size = ids.size();
		int beginIndex = (pageno - 1) * pagesize;
		int toIndex = pageno * pagesize;
		List<Long> returnIds = ids.subList(beginIndex, toIndex > size ? size
				: toIndex);
		List<Blog> list = Blog.INSTANCE.LoadList(returnIds);
		return new Pagination(size, list);
	}

}
