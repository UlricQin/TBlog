package my.mvc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

/**
 * 敏感字词
 * @author Winter Lau
 * @date 2010-1-11 下午10:51:30
 */
public class BadWord {

	private final static File wordfilter = new File(RequestContext.root() + "WEB-INF" + File.separator + "conf" + File.separator + "wordfilter.txt");

	private static long lastModified = 0L;
	private static List<String> words = new ArrayList<String>();
	
	private static void _CheckReload(){
		if(wordfilter.lastModified() > lastModified){
			synchronized(BadWord.class){
				try{
					lastModified = wordfilter.lastModified();
					LineIterator lines = FileUtils.lineIterator(wordfilter, "utf-8");
					while(lines.hasNext()){
						String line = lines.nextLine();
						if(StringUtils.isNotBlank(line))
							words.add(StringUtils.trim(line).toLowerCase());
					}
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 检查敏感字内容
	 * @param contents
	 */
	public static String Check(String...contents) {
		if(!wordfilter.exists())
			return null;
		_CheckReload();
		for(String word : words){
			for(String content : contents)
				if(content!=null && content.indexOf(word) >= 0)
					return word;
		}
		return null;
	}
	
	public static List<String> List() {
		_CheckReload();
		return words;
	}
	
	public static void Add(String word) throws IOException {
		word = word.toLowerCase();
		if(!words.contains(word)){
			words.add(word);
			FileUtils.writeLines(wordfilter, "UTF-8", words);
			lastModified = wordfilter.lastModified();
		}
	}

	public static void Delete(String word) throws IOException {
		word = word.toLowerCase();
		words.remove(word);
		FileUtils.writeLines(wordfilter, "UTF-8", words);
		lastModified = wordfilter.lastModified();
	}
	
}
