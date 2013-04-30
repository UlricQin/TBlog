package org.iperl.service;

import my.mvc.RequestContext;
import my.util.Storage;

/**
 * 文件存储服务 使用方法： File img = ...; String path = StorageService.FILES.save(img);
 * StorageService.FILES.delete(path);
 * 
 * @author Winter Lau
 * @date 2010-9-2 上午11:35:56
 */
public class StorageService extends Storage {

	public final static StorageService KEFILES = new StorageService("kefiles");
	public final static StorageService USERPICS = new StorageService("userpics");

	private String file_path;
	private String read_path;

	private StorageService(String ext) {
		this.file_path = RequestContext.root() + "uploads"
				+ java.io.File.separator + ext + java.io.File.separator;
		this.read_path = "/uploads/" + ext + "/";
	}

	@Override
	public String getBasePath() {
		return file_path;
	}

	public String getReadPath() {
		return read_path;
	}

}
