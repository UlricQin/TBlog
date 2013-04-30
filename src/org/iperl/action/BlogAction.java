package org.iperl.action;

import java.io.IOException;

import my.mvc.ActionException;
import my.mvc.Annotation;
import my.mvc.RequestContext;
import my.toolbox.CoreTool;

import org.apache.commons.lang.StringUtils;
import org.iperl.beans.Blog;
import org.iperl.beans.User;
import org.iperl.service.BlogService;

public class BlogAction extends BaseAction {

	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void post(RequestContext ctx) throws IOException {
		String title = ctx.param("title", "");
		String ident = ctx.param("ident", "");
		Integer type = ctx.param("type", 0);
		String url = ctx.param("url", "");
		int catalog = ctx.param("catalog", 0);
		String content = ctx.param("content", "");
		String tags = ctx.param("tags", "");
		String desn = ctx.param("desn", "");

		String post = ctx.param("post", "");
		Integer status = StringUtils.isBlank(post) ? 0 : 1;

		checkBlank(new String[] { "title", "content" }, new String[] { title,
				content });

		User user = CoreTool.user();
		Long userId = user.getId();
		Blog blog = new Blog(title, ident, tags, desn, type.byteValue(), url,
				content, userId.intValue(), catalog, status.byteValue());
		long blogId = BlogService.addBlog(blog);

		ctx.output_json(new String[]{"msg", "id"}, new Object[]{"", blogId});
	}
	
	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void edit(RequestContext ctx) throws IOException {
		String title = ctx.param("title", "");
		String ident = ctx.param("ident", "");
		Integer type = ctx.param("type", 0);
		String url = ctx.param("url", "");
		int catalog = ctx.param("catalog", 0);
		String content = ctx.param("content", "");
		String tags = ctx.param("tags", "");
		String desn = ctx.param("desn", "");

		String post = ctx.param("post", "");
		Integer status = StringUtils.isBlank(post) ? 0 : 1;

		checkBlank(new String[] { "title", "content" }, new String[] { title,
				content });

		Blog blog = safeBlog(ctx);
		BlogService.update(blog, title, ident, tags, desn, type, url, content, catalog, status);
		
		ctx.output_json("msg", "");
	}
	
	
	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void del(RequestContext ctx) throws IOException {
		Blog blog = safeBlog(ctx);
		BlogService.delBlog(blog);
		ctx.output_json("msg", "");
	}
	
	public void hit(RequestContext ctx) throws IOException {
		Blog blog = safeBlog(ctx);
		blog.updateAttr("hits", blog.getHits() + 1);
	}
	
	protected Blog safeBlog(RequestContext ctx) {
		int blogid = ctx.param("blogid", 0);
		if (blogid == 0) {
			throw new ActionException("Parameter blogid is necessary");
		}
		Blog blog = Blog.INSTANCE.Get(blogid);
		if (blog == null) {
			throw new ActionException("The blog[id:" + blogid
					+ "] is not exists");
		}
		return blog;
	}

}
