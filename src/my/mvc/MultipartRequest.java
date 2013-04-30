package my.mvc;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Multipart Encapsulation
 * @author Winter Lau
 */
@SuppressWarnings("unchecked")
public class MultipartRequest extends HttpServletRequestWrapper {
	
	private com.oreilly.servlet.MultipartRequest multipartRequest;
	
	public MultipartRequest(HttpServletRequest req, 
			String upload_tmp_path, int MAX_FILE_SIZE, String enc) 
	throws IOException{
		super(req);
		this.multipartRequest = new com.oreilly.servlet.MultipartRequest(req, 
				upload_tmp_path, MAX_FILE_SIZE,enc);
	}
	
	public String getContentType(String name) {
		return multipartRequest.getContentType(name);
	}

	public File getFile(String name) {
		return multipartRequest.getFile(name);
	}

	public Enumeration<String> getFileNames() {
		return multipartRequest.getFileNames();
	}

	public String getFilesystemName(String name) {
		return multipartRequest.getFilesystemName(name);
	}

	public String getOriginalFileName(String name) {
		return multipartRequest.getOriginalFileName(name);
	}

	@Override
	public String getParameter(String name) {
		String v = super.getParameter(name);
		if(v == null)
			v =multipartRequest.getParameter(name);
		return v;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return multipartRequest.getParameterNames();
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] v = super.getParameterValues(name);
		if(v == null)
			v = multipartRequest.getParameterValues(name);
		return v;
	}

	@Override
	public Map<String,Object> getParameterMap() {
		Map<String,Object> map = new HashMap<String,Object>();
		Enumeration<String> names = getParameterNames();
		while(names.hasMoreElements()){
			String name = names.nextElement();
			String[] values = getParameterValues(name);
			map.put(name, (values!=null&&values.length==1)?values[0]:values);
		}
		return map;
	}

}
