package my.mvc;

import java.io.*;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import my.db.DBException;
import my.util.ResourceUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * 业务处理方法入口，URI的映射逻辑： /action/xxxxxx/xxxx ->
 * com.dlog4j.action.XxxxxxAction.xxxx(req,res)
 * 
 * <pre>
 * 	林花谢了春红，
 * 	太匆匆，
 * 	无奈朝来寒雨晚来风。
 * 
 * 	胭脂泪，
 * 	相留醉，
 * 	几时重，
 * 	自是人生长恨水长东。
 * </pre>
 * 
 * @author Winter Lau (http://my.oschina.net/javayou)<br>
 */
public final class ActionServlet extends HttpServlet {

	private final static String ERROR_PAGE = "error_page";
	public final static String GOTO_PAGE = "goto_page";
	private final static String THIS_PAGE = "this_page";
	private final static String ERROR_MSG = "error_msg";

	private final static String UTF_8 = "utf-8";
	private List<String> action_packages = null;
	private final static ThreadLocal<Boolean> g_json_enabled = new ThreadLocal<Boolean>();

	@Override
	public void init() throws ServletException {
		String tmp = getInitParameter("packages");
		action_packages = Arrays.asList(StringUtils.split(tmp, ','));
		String initial_actions = getInitParameter("initial_actions");
		for (String action : StringUtils.split(initial_actions, ','))
			try {
				_LoadAction(action);
			} catch (Exception e) {
				log("Failed to initial action : " + action, e);
			}
	}

	@Override
	public void destroy() {
		for (Object action : actions.values()) {
			try {
				Method dm = action.getClass().getMethod("destroy");
				if (dm != null) {
					dm.invoke(action);
					log("!!!!!!!!! " + action.getClass().getSimpleName()
							+ " destroy !!!!!!!!!");
				}
			} catch (NoSuchMethodException e) {
			} catch (Exception e) {
				log("Unabled to destroy action: "
						+ action.getClass().getSimpleName(), e);
			}
		}
		super.destroy();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(RequestContext.get(), false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(RequestContext.get(), true);
	}

	/**
	 * 执行Action方法并进行返回处理、异常处理
	 * 
	 * @param req_ctx
	 * @param resp
	 * @param is_post
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void process(RequestContext req_ctx, boolean is_post)
			throws ServletException, IOException {
		try {
			req_ctx.response().setContentType("text/html;charset=utf-8");
			if (_process(req_ctx, is_post)) {
				// 处理完了当前的操作之后是否有个跳转呢，如果有个goto_page参数带过来了，
				// 就跳转到相应的页面,比如需要登录才能访问的某个页面
				String gp = req_ctx.param(GOTO_PAGE);
				if (StringUtils.isNotBlank(gp))
					req_ctx.redirect(gp);
			}
		} catch (InvocationTargetException e) {
			Throwable t = e.getCause();
			if (t instanceof ActionException)
				handleActionException(req_ctx, (ActionException) t);
			else if (t instanceof DBException)
				handleDBException(req_ctx, (DBException) t);
			else
				throw new ServletException(t);
		} catch (ActionException t) {
			handleActionException(req_ctx, t);
		} catch (IOException e) {
			throw e;
		} catch (DBException e) {
			handleDBException(req_ctx, e);
		} catch (Exception e) {
			log("Exception in action process.", e);
			throw new ServletException(e);
		} finally {
			g_json_enabled.remove();
		}
	}

	/**
	 * Action业务异常
	 * 
	 * @param req
	 * @param resp
	 * @param t
	 * @throws ServletException
	 * @throws IOException
	 */
	protected void handleActionException(RequestContext req, ActionException t)
			throws ServletException, IOException {
		handleException(req, t.getMessage());
	}

	protected void handleDBException(RequestContext req, DBException e)
			throws ServletException, IOException {
		log("DBException in action process.", e);
		handleException(req, ResourceUtils.getString("error",
				"database_exception", e.getCause().getMessage()));
	}

	/**
	 * URL解码
	 * 
	 * @param url
	 * @param charset
	 * @return
	 */
	private static String _DecodeURL(String url, String charset) {
		if (StringUtils.isEmpty(url))
			return "";
		try {
			return URLDecoder.decode(url, charset);
		} catch (Exception e) {
		}
		return url;
	}

	protected void handleException(RequestContext req, String msg)
			throws ServletException, IOException {
		String ep = req.param(ERROR_PAGE);
		if (StringUtils.isNotBlank(ep)) {
			if (ep.charAt(0) == '%') {
				ep = _DecodeURL(ep, UTF_8);
			}
			ep = ep.trim();
			if (ep.charAt(0) != '/') {
				req.redirect(req.contextPath() + "/");
			} else {
				req.request().setAttribute(ERROR_MSG, msg);
				req.forward(ep.trim());
			}
		} else {
			if (g_json_enabled.get()){
				req.output_json("msg", msg);
			}
			else{
				req.print(msg);
			}
		}
	}

	/**
	 * 业务逻辑处理
	 * 
	 * @param req
	 * @param resp
	 * @param is_post_method
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IOException
	 * @throws ServletException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private boolean _process(RequestContext req, boolean is_post)
			throws InstantiationException, IllegalAccessException, IOException,
			IllegalArgumentException, InvocationTargetException {
		String requestURI = req.uri();
		String[] parts = StringUtils.split(requestURI, '/');
		if (parts.length < 2) {
			req.not_found();
			return false;
		}
		// 加载Action类
		Object action = this._LoadAction(parts[1]);
		if (action == null) {
			req.not_found();
			return false;
		}
		String action_method_name = (parts.length > 2) ? parts[2] : "index";
		Method m_action = this._GetActionMethod(action, action_method_name);
		if (m_action == null) {
			req.not_found();
			return false;
		}

		// 判断action方法是否只支持POST
		if (!is_post
				&& m_action.isAnnotationPresent(Annotation.PostMethod.class)) {
			req.not_found();
			return false;
		}

		g_json_enabled.set(m_action
				.isAnnotationPresent(Annotation.JSONOutputEnabled.class));

		if (m_action.isAnnotationPresent(Annotation.UserRoleRequired.class)) {
			IUser loginUser = req.user();
			if (loginUser == null) {
				String this_page = req.param(THIS_PAGE, "");
				throw req.error("user_not_login", this_page);
			}
			if (loginUser.IsBlocked())
				throw req.error("user_blocked");

			Annotation.UserRoleRequired urr = (Annotation.UserRoleRequired) m_action
					.getAnnotation(Annotation.UserRoleRequired.class);
			if (loginUser.getRole() < urr.role())
				throw req.error("user_role_deny");
		}

		// 调用Action方法之准备参数
		int arg_c = m_action.getParameterTypes().length;
		switch (arg_c) {
		case 0: // login()
			m_action.invoke(action);
			break;
		case 1:
			m_action.invoke(action, req);
			break;
		case 2: // login(HttpServletRequest req, HttpServletResponse res)
			m_action.invoke(action, req.request(), req.response());
			break;
		case 3: // login(HttpServletRequest req, HttpServletResponse res,
				// String[] extParams)
			StringBuilder args = new StringBuilder();
			for (int i = 3; i < parts.length; i++) {
				if (StringUtils.isBlank(parts[i]))
					continue;
				if (args.length() > 0)
					args.append('/');
				args.append(parts[i]);
			}
			boolean isLong = m_action.getParameterTypes()[2].equals(long.class);
			m_action.invoke(
					action,
					req.request(),
					req.response(),
					isLong ? NumberUtils.toLong(args.toString(), -1L) : args
							.toString());
			break;
		default:
			req.not_found();
			return false;
		}

		return true;
	}

	/**
	 * 加载Action类
	 * 
	 * @param act_name
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	protected Object _LoadAction(String act_name)
			throws InstantiationException, IllegalAccessException {
		Object action = actions.get(act_name);
		if (action == null) {
			for (String pkg : action_packages) {
				String cls = pkg + '.' + StringUtils.capitalize(act_name)
						+ "Action";
				action = _LoadActionOfFullname(act_name, cls);
				if (action != null)
					break;
			}
		}
		return action;
	}

	private Object _LoadActionOfFullname(String act_name, String cls)
			throws IllegalAccessException, InstantiationException {
		Object action = null;
		try {
			action = Class.forName(cls).newInstance();
			try {
				Method action_init_method = action.getClass().getMethod("init",
						ServletContext.class);
				action_init_method.invoke(action, getServletContext());
			} catch (NoSuchMethodException e) {
			} catch (InvocationTargetException excp) {
				excp.printStackTrace();
			}
			if (!actions.containsKey(act_name)) {
				synchronized (actions) {
					actions.put(act_name, action);
				}
			}
		} catch (ClassNotFoundException excp) {
		}
		return action;
	}

	/**
	 * 获取名为{method}的方法
	 * 
	 * @param action
	 * @param method
	 * @return
	 */
	private Method _GetActionMethod(Object action, String method) {
		String key = action.getClass().getSimpleName() + '.' + method;
		Method m = methods.get(key);
		if (m != null)
			return m;
		for (Method m1 : action.getClass().getMethods()) {
			if (m1.getModifiers() == Modifier.PUBLIC
					&& m1.getName().equals(method)) {
				synchronized (methods) {
					methods.put(key, m1);
				}
				return m1;
			}
		}
		return null;
	}

	private final static HashMap<String, Object> actions = new HashMap<String, Object>();
	private final static HashMap<String, Method> methods = new HashMap<String, Method>();

}