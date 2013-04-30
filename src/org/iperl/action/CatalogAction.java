package org.iperl.action;

import java.io.IOException;

import my.mvc.ActionException;
import my.mvc.Annotation;
import my.mvc.RequestContext;

import org.apache.commons.lang.StringUtils;
import org.iperl.beans.BlogCatalog;
import org.iperl.service.CatalogService;

public class CatalogAction extends BaseAction {

	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void add(RequestContext ctx) throws IOException {
		String ident = ctx.param("ident", "");
		String name = ctx.param("name", "");
		int dorder = ctx.param("dorder", 0);

		if (StringUtils.isBlank(ident)) {
			throw new ActionException("ident is blank");
		}

		if (StringUtils.isBlank(name)) {
			throw new ActionException("name is blank");
		}

		CatalogService.addCatalog(ident, name, dorder);
		ctx.output_json("msg", "");
	}

	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void del(RequestContext ctx) throws IOException {
		int id = ctx.param("id", 0);
		if (id == 0) {
			throw new ActionException("id is necessary");
		}

		BlogCatalog bc = BlogCatalog.INSTANCE.Get(id);
		if (bc == null) {
			throw new ActionException("The catalog[id:" + id
					+ "] is not exists");
		}
		
		CatalogService.delCatalog(bc);
		ctx.output_json("msg", "");
	}
	
	@Annotation.JSONOutputEnabled
	@Annotation.PostMethod
	@Annotation.UserRoleRequired()
	public void update(RequestContext ctx) throws IOException {
		String ident = ctx.param("ident", "");
		String name = ctx.param("name", "");
		int dorder = ctx.param("dorder", 0);
		int id = ctx.param("id", 0);
		
		if (id == 0) {
			throw new ActionException("id is necessary");
		}
		
		if (StringUtils.isBlank(ident)) {
			throw new ActionException("ident is blank");
		}

		if (StringUtils.isBlank(name)) {
			throw new ActionException("name is blank");
		}

		BlogCatalog bc = BlogCatalog.INSTANCE.Get(id);
		if (bc == null) {
			throw new ActionException("The catalog[id:" + id
					+ "] is not exists");
		}

		boolean success = CatalogService.updateCatalog(ident, name, dorder, bc);
		String msg = success ? "" : "Unknown Error";
		ctx.output_json("msg", msg);
	}

}
