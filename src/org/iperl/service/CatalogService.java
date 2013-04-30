package org.iperl.service;

import java.util.List;

import my.cache.CacheManager;
import my.mvc.ActionException;

import org.iperl.beans.BlogCatalog;

public class CatalogService {

	private static final String idsKey = "order by dorder desc";

	public static void addCatalog(String ident, String name, int dorder) {
		BlogCatalog bc = new BlogCatalog(name, ident, dorder);
		if (bc.Save() > 0) {
			CacheManager.evict(BlogCatalog.INSTANCE.CacheRegion(), idsKey);
		}
	}

	public static List<BlogCatalog> allCatalogs() {
		List<Long> ids = BlogCatalog.INSTANCE.IDs(idsKey);
		return BlogCatalog.INSTANCE.LoadList(ids);
	}

	public static void delCatalog(BlogCatalog bc) {
		int cnt = bc.blogCnt();
		if (cnt > 0) {
			throw new ActionException("There are some blogs under "
					+ bc.getName() + ". So it can not be deleted");
		}
		if (bc.Delete()) {
			CacheManager.evict(BlogCatalog.INSTANCE.CacheRegion(), idsKey);
		}
	}

	public static boolean updateCatalog(String ident, String name, int dorder,
			BlogCatalog bc) {
		return bc.updateAttrs(new String[] { "ident", "name", "dorder" },
				new Object[] { ident, name, dorder });
	}
}
