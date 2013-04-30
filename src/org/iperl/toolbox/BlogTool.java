package org.iperl.toolbox;

import java.util.Arrays;
import java.util.List;

import org.iperl.beans.Blog;
import org.iperl.beans.Tag;
import org.iperl.dto.Pagination;
import org.iperl.service.BlogService;
import org.iperl.service.TagService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BlogTool {

	public static List<String> Anchor = Arrays.asList("h1", "h2", "h3", "h4",
			"h5", "h6");

	public static List<Blog> draft() {
		List<Blog> list = BlogService.draft();
		return list;
	}

	public static List<Blog> pubed() {
		List<Blog> list = BlogService.pubed();
		return list;
	}

	public static Blog detail(int id) {
		return Blog.INSTANCE.Get(id);
	}

	public static Pagination topNew(int pageno, int pagesize) {
		pageno = pageno <= 0 ? 1 : pageno;
		pagesize = pagesize <= 0 ? 10 : pagesize;
		return BlogService.topNew(pageno, pagesize);
	}
	
	public static List<Blog> topHits(int count){
		List<Blog> list = BlogService.topHits(count);
		return list;
	}
	
	public static Pagination byCatalog(int catalogId, int pageno, int pagesize){
		pageno = pageno <= 0 ? 1 : pageno;
		pagesize = pagesize <= 0 ? 10 : pagesize;
		return BlogService.byCatalog(catalogId, pageno, pagesize);
	}
	
	public static Pagination byUser(int userId, int pageno, int pagesize){
		pageno = pageno <= 0 ? 1 : pageno;
		pagesize = pagesize <= 0 ? 10 : pagesize;
		return BlogService.byUser(userId, pageno, pagesize);
	}
	
	public static Pagination byTag(String tag, int pageno, int pagesize){
		pageno = pageno <= 0 ? 1 : pageno;
		pagesize = pagesize <= 0 ? 10 : pagesize;
		Tag tagObj = TagService.findByName(tag);
		if(tagObj == null){
			return null;
		}
		return BlogService.byTag(tagObj, pageno, pagesize);
	}

	public static String htmlAnchor(String html) {
		Document doc = Jsoup.parseBodyFragment(html);
		Elements anchors = doc.select("*");
		int i = 0;
		for (Element anchor : anchors) {
			String tagName = anchor.tagName().toLowerCase();
			if (Anchor.contains(tagName) && anchor.hasText()) {
				i++;
				anchor.before("<span id='OSC_" + tagName + "_"
						+ String.valueOf(i) + "'></span>");
			}
		}
		return doc.body().html();
	}

	public static String htmlContent(String html) {
		StringBuffer content = new StringBuffer();
		Document doc = Jsoup.parseBodyFragment(html);
		Elements anchors = doc.select("*");
		int i = 0;
		for (Element anchor : anchors) {
			String tagName = anchor.tagName().toLowerCase();
			if (Anchor.contains(tagName) && anchor.hasText()) {
				i++;
				content.append("<li class='osc_" + tagName + "'><a href='#OSC_"
						+ tagName + "_" + String.valueOf(i) + "'>"
						+ anchor.text() + "</a></li>");
			}
		}
		return content.toString();
	}

}
