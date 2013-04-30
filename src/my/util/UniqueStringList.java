package my.util;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 唯一字符串List
 * 在这个列表中，会保证里面的每个字符串都是唯一的，可决定是否进行大小写匹配。
目前只支持用add方法添加的对象，其他方法尚未实现。
 * @date 2010-10-21 上午11:41:38
 */
public class UniqueStringList extends LinkedList<String> {

	private boolean ignoreCase;
	
	public UniqueStringList(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	@Override
	public boolean add(String e) {    
		ListIterator<String> iterator = listIterator();
		while (iterator.hasNext()) {
			String next = iterator.next();			
			if(ignoreCase?e.equalsIgnoreCase(next):e.equals(next))
				return false;
		}
		return super.add(e);
	}
	
	public static void main(String[] args) {
		UniqueStringList usl = new UniqueStringList(true);
		usl.add("oschina");
		usl.add("OSCHINA");
		usl.add("OSChina");
		for(String s : usl)
			System.out.println(s);
	}
}
