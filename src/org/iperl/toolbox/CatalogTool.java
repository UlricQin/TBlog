package org.iperl.toolbox;

import java.util.List;

import org.iperl.beans.BlogCatalog;
import org.iperl.service.CatalogService;

public class CatalogTool {

	public static List<BlogCatalog> all(){
		List<BlogCatalog> list = CatalogService.allCatalogs();
		return list;
	}
	
	public static BlogCatalog detail(int id){
		return BlogCatalog.INSTANCE.Get(id);
	}
	
}
