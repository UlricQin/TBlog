package org.iperl.service;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.beans.BlogTag;
import org.iperl.beans.Tag;

public class TagService {

	public static Long addTag(String tag) {
		tag = tag.trim();
		Tag tagObj = Tag.INSTANCE.GetByAttr("name", tag);
		if (tagObj == null) {
			tagObj = new Tag(tag, 1);
			return tagObj.Save();
		}
		int cnt = tagObj.getCnt() + 1;
		tagObj.updateAttr("cnt", cnt);
		return tagObj.getId();
	}

	public static void batchAddTags(Long blogId, String keywords) {
		if (StringUtils.isBlank(keywords)) {
			return;
		}
		String[] tagArr = StringUtils.split(keywords, ',');
		for (String tag : tagArr) {
			Long tagId = addTag(tag);
			BlogTag bt = new BlogTag(blogId.intValue(), tagId.intValue());
			bt.Save();
		}
	}
	
	public static void batchDelTags(Long blogId, String keywords) {
		if (StringUtils.isBlank(keywords)) {
			return;
		}

		List<BlogTag> list = BlogTag.INSTANCE.BatchGetByAttr("blog", blogId);
		for (BlogTag bt : list) {
			int tagId = bt.getTag();
			if (bt.Delete()) {
				Tag t = Tag.INSTANCE.GetByAttr("id", tagId);
				int cnt = t.getCnt() - 1;
				if (cnt == 0) {
					t.Delete();
				} else {
					t.updateAttr("cnt", cnt);
				}
			}
		}
	}

	public static List<Tag> all() {
		List<Long> ids = Tag.INSTANCE.IDs("order by cnt desc");
		return Tag.INSTANCE.LoadList(ids);
	}

	public static Tag findByName(String tag) {
		if(StringUtils.isBlank(tag)){
			return null;
		}
		
		List<Long> ids = Tag.INSTANCE.IDs("where name = ? ", tag);
		if(CollectionUtils.isEmpty(ids)){
			return null;
		}
		
		Long id = ids.get(0);
		return Tag.INSTANCE.Get(id);
	}

}
