package my.cache;

/**
 * 缓存异常
 * @author Winter Lau
 */
public class CacheException extends RuntimeException {

	private static final long serialVersionUID = -8747630300668561700L;

	public CacheException(String s) {
		super(s);
	}

	public CacheException(String s, Throwable e) {
		super(s, e);
	}

	public CacheException(Throwable e) {
		super(e);
	}
	
}
