package my.db;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.cache.CacheManager;
import my.util.Inflector;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.iperl.beans.User;

/**
 * OSC Base POJO http://www.oschina.net/code/snippet_12_2746
 * 
 * @author Winter Lau
 * @date 2010-1-22 11:33:56
 */
@SuppressWarnings("serial")
public class POJO implements Serializable {

	protected final static transient char OBJ_COUNT_CACHE_KEY = '#';
	private long ___key_id;

	public long getId() {
		return ___key_id;
	}

	public void setId(long id) {
		this.___key_id = id;
	}

	private String __this_table_name;

	protected String TableName() {
		if (__this_table_name == null)
			__this_table_name = "iperl_"
					+ Inflector.getInstance().tableize(getClass());
		return __this_table_name;
	}

	public String CacheRegion() {
		return this.getClass().getSimpleName();
	}

	protected boolean IsObjectCachedByID() {
		return false;
	}

	protected boolean IsAutoLoadUser() {
		return false;
	}

	protected long GetAutoLoadUser() {
		return 0L;
	}

	public List List(int page, int size) {
		String sql = "SELECT * FROM " + TableName() + " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	public List List(int page, int size, String orderBy) {
		String sql = "SELECT * FROM " + TableName() + " ";
		if (StringUtils.isNotBlank(orderBy)) {
			sql += "ORDER BY " + orderBy;
		}
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	public List Filter(String filter, int page, int size) {
		String sql = "SELECT * FROM " + TableName() + " WHERE " + filter
				+ " ORDER BY id DESC";
		return QueryHelper.query_slice(getClass(), sql, page, size);
	}

	// 对这个cache的更新操作需要格外留意
	public List IDs(String afterFrom, Object... params) {
		String tbl = TableName();

		String cacheKey = afterFrom;
		for (Object obj : params) {
			cacheKey += obj;
		}

		String sql = "select id from " + tbl + " ";
		if (StringUtils.isNotBlank(afterFrom)) {
			sql += afterFrom;
		} else {
			cacheKey = "all" + cacheKey;
		}
		return QueryHelper.query_cache(Long.class, CacheRegion(), cacheKey,
				sql, params);
	}

	public int TotalCount(String filter, Object... params) {
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + TableName()
				+ " WHERE " + filter, params);
	}

	public <T extends POJO> T GetByAttr(String attrName, Object attrValue) {
		String sql = "SELECT * FROM " + TableName() + " WHERE " + attrName
				+ " = ?";
		return (T) QueryHelper.read(getClass(), sql, attrValue);
	}
	
	public List BatchGetByAttr(String attrName, Object attrValue) {
		String sql = "SELECT * FROM " + TableName() + " WHERE " + attrName
				+ " = ?";
		return QueryHelper.query(getClass(), sql, attrValue);
	}

	public long Save() {
		if (getId() > 0)
			_InsertObject(this);
		else
			setId(_InsertObject(this));
		if (this.IsObjectCachedByID())
			CacheManager.evict(CacheRegion(), OBJ_COUNT_CACHE_KEY);
		return getId();
	}

	public boolean Delete() {
		boolean dr = Evict(QueryHelper.update("DELETE FROM " + TableName()
				+ " WHERE id = ?", getId()) == 1);
		if (dr) {
			CacheManager.evict(CacheRegion(), OBJ_COUNT_CACHE_KEY);
		}
		return dr;
	}

	public boolean Evict(boolean er) {
		if (er && IsObjectCachedByID())
			CacheManager.evict(CacheRegion(), getId());
		return er;
	}

	protected void Evict(long obj_id) {
		CacheManager.evict(CacheRegion(), obj_id);
	}

	public POJO Get(java.math.BigInteger id) {
		if (id == null)
			return null;
		return Get(id.longValue());
	}

	public boolean updateAttr(String attrName, Object attrValue) {
		String sql = "update " + TableName() + " set " + attrName
				+ " = ? where id = ?";
		int ret = QueryHelper.update(sql, attrValue, getId());
		try {
			if (ret > 0) {
				BeanUtils.setProperty(this, attrName, attrValue);
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean updateAttrs(String[] attrNames, Object[] attrValues) {
		int len = attrNames.length;
		List<String> kvs = new ArrayList<String>(len);
		for (String attr : attrNames) {
			kvs.add(attr + " = ?");
		}

		String sql = "update " + TableName() + " set "
				+ StringUtils.join(kvs, ',') + " where id = ?";
		List<Object> vals = new ArrayList<Object>();
		for (Object val : attrValues) {
			vals.add(val);
		}
		vals.add(getId());

		int ret = QueryHelper.update(sql, vals.toArray());
		try {
			if (ret > 0) {
				for (int i = 0; i < len; i++) {
					BeanUtils.setProperty(this, attrNames[i], attrValues[i]);
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public <T extends POJO> T Get(long id) {
		if (id <= 0)
			return null;
		String sql = "SELECT * FROM " + TableName() + " WHERE id = ?";
		boolean cached = IsObjectCachedByID();
		return (T) QueryHelper.read_cache(getClass(), cached ? CacheRegion()
				: null, id, sql, id);
	}

	protected List<? extends POJO> BatchGet(List<Long> ids) {
		if (ids == null || ids.size() == 0)
			return null;
		StringBuilder sql = new StringBuilder("SELECT * FROM " + TableName()
				+ " WHERE id IN (");
		for (int i = 1; i <= ids.size(); i++) {
			sql.append('?');
			if (i < ids.size())
				sql.append(',');
		}
		sql.append(')');
		List<? extends POJO> beans = QueryHelper.query(getClass(),
				sql.toString(), ids.toArray(new Object[ids.size()]));
		if (IsObjectCachedByID()) {
			for (Object bean : beans) {
				CacheManager.set(CacheRegion(), ((POJO) bean).getId(),
						(Serializable) bean);
			}
		}
		return beans;
	}

	public int TotalCount() {
		if (this.IsObjectCachedByID())
			return (int) QueryHelper.stat_cache(CacheRegion(),
					OBJ_COUNT_CACHE_KEY, "SELECT COUNT(*) FROM " + TableName());
		return (int) QueryHelper.stat("SELECT COUNT(*) FROM " + TableName());
	}

	@SuppressWarnings("rawtypes")
	public List LoadList(List<Long> p_pids) {
		if (p_pids == null)
			return null;
		final List<Long> pids = new ArrayList<Long>(p_pids.size());
		for (Number obj : p_pids) {
			pids.add(obj.longValue());
		}
		String cache = this.CacheRegion();
		List<POJO> prjs = new ArrayList<POJO>(pids.size()) {
			{
				for (int i = 0; i < pids.size(); i++)
					add(null);
			}
		};
		List<Long> no_cache_ids = new ArrayList<Long>();
		for (int i = 0; i < pids.size(); i++) {
			long pid = pids.get(i);
			POJO obj = (POJO) CacheManager.get(cache, pid);

			if (obj != null)
				prjs.set(i, obj);
			else {
				no_cache_ids.add(pid);
			}
		}

		if (no_cache_ids.size() > 0) {
			List<? extends POJO> no_cache_prjs = BatchGet(no_cache_ids);
			if (no_cache_prjs != null)
				for (POJO obj : no_cache_prjs) {
					prjs.set(pids.indexOf(obj.getId()), obj);
				}
		}

		no_cache_ids = null;

		// Check Users
		if (prjs != null && IsAutoLoadUser()) {
			List<Long> no_cache_userids = new ArrayList<Long>();
			String user_cache = User.INSTANCE.CacheRegion();
			for (POJO pojo : prjs) {
				if (pojo == null)
					continue;
				long userid = pojo.GetAutoLoadUser();
				if (userid > 0 && !no_cache_userids.contains(userid)) {
					POJO user = (POJO) CacheManager.get(user_cache, userid);
					if (user == null) {
						no_cache_userids.add(userid);
					}
				}
			}
			if (no_cache_userids.size() > 0)
				User.INSTANCE.BatchGet(no_cache_userids);

			no_cache_userids = null;
		}

		return prjs;
	}

	private static long _InsertObject(POJO obj) {
		Map<String, Object> pojo_bean = obj.ListInsertableFields();
		String[] fields = pojo_bean.keySet().toArray(
				new String[pojo_bean.size()]);
		StringBuilder sql = new StringBuilder("INSERT INTO ");
		sql.append(obj.TableName());
		sql.append('(');
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append(fields[i]);
		}
		sql.append(") VALUES(");
		for (int i = 0; i < fields.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append('?');
		}
		sql.append(')');
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = QueryHelper.getConnection().prepareStatement(sql.toString(),
					PreparedStatement.RETURN_GENERATED_KEYS);
			for (int i = 0; i < fields.length; i++) {
				ps.setObject(i + 1, pojo_bean.get(fields[i]));
			}
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			return rs.next() ? rs.getLong(1) : -1;
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(ps);
			sql = null;
			fields = null;
			pojo_bean = null;
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> ListInsertableFields() {
		try {
			Map<String, Object> props = BeanUtils.describe(this);
			if (getId() <= 0) {
				props.remove("id");
			}
			props.remove("class");
			return props;
		} catch (Exception e) {
			throw new RuntimeException("Exception when Fetching fields of "
					+ this);
		}
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		POJO wb = (POJO) obj;
		return wb.getId() == getId();
	}

}
