package my.mvc;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import my.util.CryptUtils;
import my.util.Multimedia;
import my.util.RequestUtils;
import my.util.ResourceUtils;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.iperl.service.UserService;

/**
 * 请求上下文
 * @author Winter Lau
 * @date 2010-1-13 下午04:18:00
 */
public class RequestContext {
	
	private final static Log log = LogFactory.getLog(RequestContext.class);

	private final static int MAX_FILE_SIZE = 10*1024*1024; 
	private final static String UTF_8 = "UTF-8";
	
	private final static ThreadLocal<RequestContext> contexts = new ThreadLocal<RequestContext>();
	private final static boolean isResin;
	private final static String upload_tmp_path;
	private final static String TEMP_UPLOAD_PATH_ATTR_NAME = "$OSCHINA_TEMP_UPLOAD_PATH$";

	private static String webroot = null;
	
	private ServletContext context;
	private HttpSession session;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Map<String, Cookie> cookies;
	
	static {
		webroot = getWebrootPath();
		isResin = _CheckResinVersion();
		//上传的临时目录
		upload_tmp_path = webroot + "WEB-INF" + File.separator + "tmp" + File.separator;
		try {
			FileUtils.forceMkdir(new File(upload_tmp_path));
		} catch (IOException excp) {}
		
		//BeanUtils对时间转换的初始化设置
		ConvertUtils.register(new SqlDateConverter(null), java.sql.Date.class);
		ConvertUtils.register(new Converter(){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
			SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-M-d H:m");
			@SuppressWarnings("rawtypes")
			public Object convert(Class type, Object value) {
				if(value == null) return null;
		        if (value instanceof Date) return (value);
		        try {
		            return sdf_time.parse(value.toString());
		        } catch (ParseException e) {
		            try {
						return sdf.parse(value.toString());
					} catch (ParseException e1) {
						return null;
					}
		        }
			}}, java.util.Date.class);
	}
	
	private final static String getWebrootPath() {
		String root = RequestContext.class.getResource("/").getFile();
		try {
			root = new File(root).getParentFile().getParentFile().getCanonicalPath();
			root += File.separator;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return root;
	}
	
	/**
	 * 初始化请求上下文
	 * @param ctx
	 * @param req
	 * @param res
	 */
	public static RequestContext begin(ServletContext ctx, HttpServletRequest req, HttpServletResponse res) {
		RequestContext rc = new RequestContext();
		rc.context = ctx;
		rc.request = _AutoUploadRequest(_AutoEncodingRequest(req));
		rc.response = res;
		rc.response.setCharacterEncoding(UTF_8);
		rc.session = req.getSession(false);
		rc.cookies = new HashMap<String, Cookie>();
		Cookie[] cookies = req.getCookies();
		if(cookies != null)
			for(Cookie ck : cookies) {
				rc.cookies.put(ck.getName(), ck);
			}
		contexts.set(rc);
		return rc;
	}

	/**
	 * 返回Web应用的路径
	 * @return
	 */
	public static String root() { return webroot; }
	
	/**
	 * 获取当前请求的上下文
	 * @return
	 */
	public static RequestContext get(){
		return contexts.get();
	}
	
	public void end() {
		String tmpPath = (String)request.getAttribute(TEMP_UPLOAD_PATH_ATTR_NAME);
		if(tmpPath != null){
			try {
				FileUtils.deleteDirectory(new File(tmpPath));
			} catch (IOException e) {
				log.fatal("Failed to cleanup upload directory: " + tmpPath, e);
			}
		}
		this.context = null;
		this.request = null;
		this.response = null;
		this.session = null;
		this.cookies = null;
		contexts.remove();
	}
	
	public Locale locale(){ return request.getLocale(); }

	public void closeCache(){
        header("Pragma","No-cache");
        header("Cache-Control","no-cache");
        header("Expires", 0L);
	}
	
	/**
	 * 自动编码处理
	 * @param req
	 * @return
	 */
	private static HttpServletRequest _AutoEncodingRequest(HttpServletRequest req) {
		if(req instanceof RequestProxy)
			return req;
		HttpServletRequest auto_encoding_req = req;
		if("POST".equalsIgnoreCase(req.getMethod())){
			try {
				auto_encoding_req.setCharacterEncoding(UTF_8);
			} catch (UnsupportedEncodingException e) {}
		}
		else if(!isResin)
			auto_encoding_req = new RequestProxy(req, UTF_8);
		
		return auto_encoding_req;
	}
	
	/**
	 * 自动文件上传请求的封装
	 * @param req
	 * @return
	 */
	private static HttpServletRequest _AutoUploadRequest(HttpServletRequest req){
		if(_IsMultipart(req)){
			String path = upload_tmp_path + RandomStringUtils.randomAlphanumeric(10);
			File dir = new File(path);
			if(!dir.exists() && !dir.isDirectory())	dir.mkdirs();
			try{
				req.setAttribute(TEMP_UPLOAD_PATH_ATTR_NAME,path);
				return new MultipartRequest(req, dir.getCanonicalPath(), MAX_FILE_SIZE, UTF_8);
			}catch(NullPointerException e){				
			}catch(IOException e){
				log.fatal("Failed to save upload files into temp directory: " + path, e);
			}
		}
		return req;
	}
	
	public long id() {
		return param("id", 0L);
	}
	
	public String ip(){
		return RequestUtils.getRemoteAddr(request);
	}
	
	@SuppressWarnings("unchecked")
	public Enumeration<String> params() {
		return request.getParameterNames();
	}
	
	public String param(String name, String...def_value) {
		String v = request.getParameter(name);
		return (v!=null)?v:((def_value.length>0)?def_value[0]:null);
	}
	
	public long param(String name, long def_value) {
		return NumberUtils.toLong(param(name), def_value);
	}

	public int param(String name, int def_value) {
		return NumberUtils.toInt(param(name), def_value);
	}

	public byte param(String name, byte def_value) {
		return (byte)NumberUtils.toInt(param(name), def_value);
	}

	public String[] params(String name) {
		return request.getParameterValues(name);
	}

	public long[] lparams(String name){
		String[] values = params(name);
		if(values==null) return null;
		return (long[])ConvertUtils.convert(values, long.class);
	}
	
	public String uri(){
		return request.getRequestURI();
	}
	
	public String contextPath(){
		return request.getContextPath();
	}
	
	public void redirect(String uri) throws IOException {
		response.sendRedirect(uri);
	}
	
	public void forward(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.forward(request, response);
	}

	public void include(String uri) throws ServletException, IOException {
		RequestDispatcher rd = context.getRequestDispatcher(uri);
		rd.include(request, response);
	}
	
	public boolean isUpload(){
		return (request instanceof MultipartRequest);
	}
	public File file(String fieldName) {
		if(request instanceof MultipartRequest)
			return ((MultipartRequest)request).getFile(fieldName);
		return null;
	}
	public File image(String fieldname) {
		File imgFile = file(fieldname);	
		return (imgFile!=null&&Multimedia.isImageFile(imgFile.getName()))?imgFile:null;
	}
	
	public boolean isRobot(){
		return RequestUtils.isRobot(request);
	}

	public ActionException fromResource(String bundle, String key, Object...args){
		String res = ResourceUtils.getStringForLocale(request.getLocale(), bundle, key, args);
		return new ActionException(res);
	}

	public ActionException error(String key, Object...args){		
		return fromResource("error", key, args);
	}
	
	/**
	 * 输出信息到浏览器
	 * @param msg
	 * @throws IOException
	 */
	public void print(Object msg) throws IOException {
		if(!UTF_8.equalsIgnoreCase(response.getCharacterEncoding()))
			response.setCharacterEncoding(UTF_8);
		response.getWriter().print(msg);
	}

	public void output_json(String[] key, Object[] value) throws IOException {
		StringBuilder json = new StringBuilder("{");
		for(int i=0;i<key.length;i++){
			if(i>0)
				json.append(',');
			boolean isNum = value[i] instanceof Number ;
			json.append("\"");
			json.append(key[i]);
			json.append("\":");
			if(!isNum) json.append("\"");
			json.append(value[i]);
			if(!isNum) json.append("\"");
		}
		json.append("}");
		print(json.toString());
	}

	public void output_json(String key, Object value) throws IOException {
		output_json(new String[]{key}, new Object[]{value});
	}
	public void error(int code, String...msg) throws IOException {
		if(msg.length>0)
			response.sendError(code, msg[0]);
		else
			response.sendError(code);
	}
	
	public void forbidden() throws IOException { 
		error(HttpServletResponse.SC_FORBIDDEN); 
	}

	public void not_found() throws IOException { 
		error(HttpServletResponse.SC_NOT_FOUND); 
	}

	public ServletContext context() { return context; }
	public HttpSession session() { return session; }
	public HttpSession session(boolean create) { 
		return (session==null && create)?(session=request.getSession()):session; 
	}
	public Object sessionAttr(String attr) {
		HttpSession ssn = session();
		return (ssn!=null)?ssn.getAttribute(attr):null;
	}
	public HttpServletRequest request() { return request; }
	public HttpServletResponse response() { return response; }
	public Cookie cookie(String name) { return cookies.get(name); }
	public void cookie(String name, String value, int max_age, boolean all_sub_domain) {
		RequestUtils.setCookie(request, response, name, value, max_age, all_sub_domain);
	}
	public void deleteCookie(String name,boolean all_domain) { RequestUtils.deleteCookie(request, response, name, all_domain); }
	public String header(String name) { return request.getHeader(name); }
	public void header(String name, String value) { response.setHeader(name, value); }
	public void header(String name, int value) { response.setIntHeader(name, value); }
	public void header(String name, long value) { response.setDateHeader(name, value); }

	/**
	 * 将HTTP请求参数映射到bean对象中
	 * @param req
	 * @param beanClass
	 * @return
	 * @throws Exception
	 */
	public <T> T form(Class<T> beanClass) {
		try{
			T bean = beanClass.newInstance();
			BeanUtils.populate(bean, request.getParameterMap());
			return bean;
		}catch(Exception e) {
			throw new ActionException(e.getMessage());
		}
	}
	
	/**
	 * 返回当前登录的用户资料
	 * @return
	 */
	public IUser user() {
		return UserService.GetLoginUser(request);
	}
	
	/**
	 * 保存登录信息
	 * @param req
	 * @param res
	 * @param user
	 * @param save
	 */
	public void saveUserInCookie(IUser user, boolean save) {
		String new_value = _GenLoginKey(user, ip(), header("user-agent"));
		int max_age = save ? MAX_AGE : -1;
		deleteCookie(COOKIE_LOGIN, true);
		cookie(COOKIE_LOGIN,new_value,max_age,true);
	}

	public void deleteUserInCookie() {
		deleteCookie(COOKIE_LOGIN, true);
	}
	
	/**
	 * 3.0 以上版本的 Resin 无需对URL参数进行转码
	 * @return
	 */
	private final static boolean _CheckResinVersion() {
		try{
			Class<?> verClass = Class.forName("com.caucho.Version");
			String ver = (String)verClass.getDeclaredField("VERSION").get(verClass);
			String mainVer = ver.substring(0, ver.lastIndexOf('.'));
			/**
			float fVer = Float.parseFloat(mainVer);
			System.out.println("----------------> " + fVer);
			*/
			return Float.parseFloat(mainVer) > 3.0;
		}catch(Throwable t) {}
		return false;
	}


	/**
	 * 自动解码
	 * @author liudong
	 */
	private static class RequestProxy extends HttpServletRequestWrapper {
		private String uri_encoding; 
		RequestProxy(HttpServletRequest request, String encoding){
			super(request);
			this.uri_encoding = encoding;
		}
		
		/**
		 * 重载getParameter
		 */
		public String getParameter(String paramName) {
			String value = super.getParameter(paramName);
			return _DecodeParamValue(value);
		}

		/**
		 * 重载getParameterMap
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Map<String, Object> getParameterMap() {
			Map params = super.getParameterMap();
			HashMap<String, Object> new_params = new HashMap<String, Object>();
			Iterator<String> iter = params.keySet().iterator();
			while(iter.hasNext()){
				String key = (String)iter.next();
				Object oValue = params.get(key);
				if(oValue.getClass().isArray()){
					String[] values = (String[])params.get(key);
					String[] new_values = new String[values.length];
					for(int i=0;i<values.length;i++)
						new_values[i] = _DecodeParamValue(values[i]);
					
					new_params.put(key, new_values);
				}
				else{
					String value = (String)params.get(key);
					String new_value = _DecodeParamValue(value);
					if(new_value!=null)
						new_params.put(key,new_value);
				}
			}
			return new_params;
		}

		/**
		 * 重载getParameterValues
		 */
		public String[] getParameterValues(String arg0) {
			String[] values = super.getParameterValues(arg0);
			for(int i=0;values!=null&&i<values.length;i++)
				values[i] = _DecodeParamValue(values[i]);
			return values;
		}

		/**
		 * 参数转码
		 * @param value
		 * @return
		 */
		private String _DecodeParamValue(String value){
			if (StringUtils.isBlank(value) || StringUtils.isBlank(uri_encoding)
					|| StringUtils.isNumeric(value))
				return value;		
			try{
				return new String(value.getBytes("8859_1"), uri_encoding);
			}catch(Exception e){}
			return value;
		}

	}
	
	private static boolean _IsMultipart(HttpServletRequest req) {
		return ((req.getContentType() != null) && (req.getContentType()
				.toLowerCase().startsWith("multipart")));
	}

	/**
	 * 生成用户登录标识字符串
	 * @param user
	 * @param ip
	 * @param user_agent
	 * @return
	 */
	public static String _GenLoginKey(IUser user, String ip, String user_agent) {
		StringBuilder sb = new StringBuilder();
		sb.append(user.getId());
		sb.append('|');
		sb.append(user.getPwd());
		sb.append('|');
		sb.append(ip);
		sb.append('|');
		sb.append((user_agent==null)?0:user_agent.hashCode());
		sb.append('|');
		sb.append(System.currentTimeMillis());
		return _Encrypt(sb.toString());	
	}

	/**
	 * 加密
	 * @param value
	 * @return 
	 * @throws Exception 
	 */
	private static String _Encrypt(String value) {
		byte[] data = CryptUtils.encrypt(value.getBytes(), E_KEY);
		try{
			return URLEncoder.encode(new String(Base64.encodeBase64(data)), UTF_8);
		}catch(UnsupportedEncodingException e){
			return null;
		}
	}

	/**
	 * 解密
	 * @param value
	 * @return
	 * @throws Exception 
	 */
	private static String _Decrypt(String value) {
		try {
			value = URLDecoder.decode(value,UTF_8);
			if(StringUtils.isBlank(value)) return null;
			byte[] data = Base64.decodeBase64(value.getBytes());
			return new String(CryptUtils.decrypt(data, E_KEY));
		} catch (UnsupportedEncodingException excp) {
			return null;
		}
	}	

	/**
	 * 从cookie中读取保存的用户信息
	 * @param req
	 * @return
	 */
	public IUser getUserFromCookie() {
		try{
			Cookie cookie = cookie(COOKIE_LOGIN);
			if(cookie!=null && StringUtils.isNotBlank(cookie.getValue())){
				return userFromUUID(cookie.getValue());
			}
		}catch(Exception e){}
		return null;
	}

	/**
	 * 从cookie中读取保存的用户信息
	 * @param req
	 * @return
	 */
	public IUser userFromUUID(String uuid) {
		if(StringUtils.isBlank(uuid))
			return null;
		String ck = _Decrypt(uuid);
		final String[] items = StringUtils.split(ck, '|');
		if(items.length == 5){
			String ua = header("user-agent");
			int ua_code = (ua==null)?0:ua.hashCode();
			int old_ua_code = Integer.parseInt(items[3]);
			if(ua_code == old_ua_code){
				return new IUser(){
					public boolean IsBlocked() { return false; }
					public long getId() { return NumberUtils.toLong(items[0],-1L); }
					public String getPwd() { return items[1]; }
					public byte getRole() { return IUser.ROLE_GENERAL; }
				};
			}
		}
		return null;
	}
	
	public final static String COOKIE_LOGIN = "iperlorg";
	private final static int MAX_AGE = 86400 * 365;
	private final static byte[] E_KEY = new byte[]{'1','2','3','4','5','6','7','8'};
	public static String getContextPath() {
		return contexts.get().contextPath();
	}
	
}