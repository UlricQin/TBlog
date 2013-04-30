package my.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

/**
 * 列表工具包扩展
 * @author Winter Lau
 * @date 2010-3-1 下午09:18:33
 */
public class MyListUtils extends ListUtils {
	
	/**
	 * 列表过滤
	 * @param <T>
	 * @param objs
	 * @param filter
	 * @return
	 */
	public static <T> List<T> filter(List<T> objs, ObjectFilter<T> filter) {
		List<T> new_objs = new ArrayList<T>();
		for(T obj : objs){
			if(filter.filter(obj))
				new_objs.add(obj);
		}
		return new_objs;
	}
	
	/**
	 * 对象过滤
	 */
	public static interface ObjectFilter<T> {
		public boolean filter(T obj) ;
	}
	
	public static <T> List<T> filterNotNull(List<T> objs) {
		return filter(objs, new ObjectFilter<T>() {

			@Override
			public boolean filter(T obj) {
				return obj != null;
			}
		});
	}

}
