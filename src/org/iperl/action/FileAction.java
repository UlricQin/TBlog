package org.iperl.action;

import java.io.File;
import java.io.IOException;

import org.iperl.service.StorageService;

import my.mvc.Annotation;
import my.mvc.RequestContext;

public class FileAction {

	private static final long MAX_IMG_SIZE = 1 * 1024 * 1024;

	@Annotation.JSONOutputEnabled
	public void up(RequestContext ctx) throws IOException {
		File imgFile = ctx.image("imgFile");
		if (imgFile.length() > MAX_IMG_SIZE) {
			ctx.output_json(new String[] { "error", "message" }, new Object[] {
					1, "File is too large" });
			return;
		}
		
		StorageService ss = StorageService.KEFILES;
		String path = ss.save(imgFile);
		ctx.output_json(new String[] { "error", "url" }, new Object[] {0, ss.getReadPath() + path});
	}

	@Annotation.JSONOutputEnabled
	public void list(RequestContext ctx) throws IOException {

	}

}
