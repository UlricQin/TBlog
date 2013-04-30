package my.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SqlUtil {

	public static List<Integer> idsStr2IntList(String ids) {
		ids = ids.trim();
		
		if (StringUtils.isBlank(ids)) {
			return Collections.emptyList();
		}

		String[] idStrArr = StringUtils.split(ids, ',');

		List<Integer> idList = new ArrayList<Integer>(idStrArr.length);
		for (String idStr : idStrArr) {
			if (StringUtils.isBlank(idStr)) {
				continue;
			}
			idList.add(Integer.parseInt(idStr));
		}

		return idList;
	}
}
