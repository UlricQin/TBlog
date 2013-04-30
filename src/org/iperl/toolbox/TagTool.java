package org.iperl.toolbox;

import java.util.List;

import org.iperl.beans.Tag;
import org.iperl.service.TagService;

public class TagTool {

	public static List<Tag> all(){
		List<Tag> list = TagService.all();
		return list;
	}
	
}
