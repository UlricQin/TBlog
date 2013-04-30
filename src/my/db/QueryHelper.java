package my.db;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.*;
import java.util.*;

import my.cache.CacheManager;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.*;
import org.apache.commons.lang.ArrayUtils;


/**
 * 数据库查询助手
 * @author Winter Lau<br> */
@SuppressWarnings("unchecked")
public class QueryHelper {
	
	private final static QueryRunner _g_runner = new QueryRunner();
	private final static ColumnListHandler _g_columnListHandler = new ColumnListHandler(){
		@Override
		protected Object handleRow(ResultSet rs) throws SQLException {
			Object obj = super.handleRow(rs);
			if(obj instanceof BigInteger)
				return ((BigInteger)obj).longValue();
			return obj;
		}
		
	};
	private final static ScalarHandler _g_scaleHandler = new ScalarHandler(){
		@Override
		public Object handle(ResultSet rs) throws SQLException {
			Object obj = super.handle(rs);
			if(obj instanceof BigInteger)
				return ((BigInteger)obj).longValue();
			return obj;
		}		
	};
	
	private final static List<Class<?>> PrimitiveClasses = new ArrayList<Class<?>>(){{
		add(Long.class);
		add(Integer.class);
		add(String.class);
		add(java.util.Date.class);
		add(java.sql.Date.class);
		add(java.sql.Timestamp.class);
	}};
	
	private final static boolean _IsPrimitive(Class<?> cls) {
		return cls.isPrimitive() || PrimitiveClasses.contains(cls) ;
	}
	
	/**
	 * 获取数据库连接
	 * @return
	 */
	public static Connection getConnection() {
		try{
			return DBManager.getConnection();
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	/**
	 * 读取某个对象
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> T read(Class<T> beanClass, String sql, Object...params) {
		try{
			return (T)_g_runner.query(getConnection(), sql, _IsPrimitive(beanClass)?_g_scaleHandler:new BeanHandler(beanClass), params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
	
	public static <T> T read_cache(Class<T> beanClass, String cache, Serializable key, String sql, Object...params) {
		T obj = (T)CacheManager.get(cache, key);
		if(obj == null){
			obj = read(beanClass, sql, params);
			CacheManager.set(cache, key, (Serializable)obj);
		}
		return obj;
	}
	
	/**
	 * 对象查询
	 * @param <T>
	 * @param beanClass
	 * @param sql
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static <T> List<T> query(Class<T> beanClass, String sql, Object...params) {
		try{
			return (List<T>)_g_runner.query(getConnection(), sql, _IsPrimitive(beanClass)?_g_columnListHandler:new BeanListHandler(beanClass), params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	/**
	 * 支持缓存的对象查询
	 * @param <T>
	 * @param beanClass
	 * @param cache_region
	 * @param key
	 * @param sql
	 * @param params
	 * @return
	 */
	public static <T> List<T> query_cache(Class<T> beanClass, String cache_region, Serializable key, String sql, Object...params) {		
		List<T> objs = (List<T>)CacheManager.get(cache_region, key);
		if(objs == null){
			objs = query(beanClass, sql, params);
			CacheManager.set(cache_region, key, (Serializable)objs);
		}
		return objs;
	}
	
	/**
	 * 分页查询
	 * @param <T>
	 * @param beanClass
	 * @param sql
	 * @param page
	 * @param count
	 * @param params
	 * @return
	 */
	public static <T> List<T> query_slice(Class<T> beanClass, String sql, int page, int count, Object...params) {
		if(page < 0 || count < 0) 
			throw new IllegalArgumentException("Illegal parameter of 'page' or 'count', Must be positive.");
		int from = (page - 1) * count;
		count = (count > 0) ? count : Integer.MAX_VALUE;
		return query(beanClass, sql + " LIMIT ?,?", ArrayUtils.addAll(params, new Integer[]{from,count}));		
	}
	
	/**
	 * 支持缓存的分页查询
	 * @param <T>
	 * @param beanClass
	 * @param cache
	 * @param cache_key
	 * @param cache_obj_count
	 * @param sql
	 * @param page
	 * @param count
	 * @param params
	 * @return
	 */
	public static <T> List<T> query_slice_cache(Class<T> beanClass, String cache, Serializable cache_key, int cache_obj_count, String sql, int page, int count, Object...params) {
		List<T> objs = (List<T>)CacheManager.get(cache, cache_key);
		if(objs == null) {
			objs = query_slice(beanClass, sql, 1, cache_obj_count, params);
			CacheManager.set(cache, cache_key, (Serializable)objs);
		}
		if(objs == null || objs.size()==0)
			return objs;
		int from = (page - 1) * count;
		if(from < 0)
			return null;
		if((from+count) > cache_obj_count)//超出缓存的范围
			return query_slice(beanClass, sql, page, count, params);
		int end = Math.min(from + count, objs.size());//最后一页可能是半页数据
		if(from >= end)
			return null;
		return objs.subList(from, end);
	}
	
	/**
	 * 执行统计查询语句，语句的执行结果必须只返回一个数值
	 * @param sql
	 * @param params
	 * @return
	 */
	public static long stat(String sql, Object...params) {
		try{
			Number num = (Number)_g_runner.query(getConnection(), sql, _g_scaleHandler, params);
			return (num!=null) ? num.longValue():-1;
		}catch(SQLException e){
			throw new DBException(e);
		}
	}

	/**
	 * 执行统计查询语句，语句的执行结果必须只返回一个数值
	 * @param cache_region
	 * @param key
	 * @param sql
	 * @param params
	 * @return
	 */
	public static long stat_cache(String cache_region, Serializable key, String sql, Object...params) {
		Number value = (Number)CacheManager.get(cache_region, key);
		if(value == null){
			value = stat(sql, params);
			CacheManager.set(cache_region, key, value);
		}
		return value.longValue();
	}

	/**
	 * 执行INSERT/UPDATE/DELETE语句
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int update(String sql, Object...params) {
		try{
			return _g_runner.update(getConnection(), sql, params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
	
	/**
	 * 批量执行指定的SQL语句
	 * @param sql
	 * @param params
	 * @return
	 */
	public static int[] batch(String sql, Object[][] params) {
		try{
			return _g_runner.batch(getConnection(), sql, params);
		}catch(SQLException e){
			throw new DBException(e);
		}
	}
}
