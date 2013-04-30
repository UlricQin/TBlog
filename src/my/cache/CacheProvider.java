package my.cache;

/**
 * Support for pluggable caches.
 * @author Winter Lau
 */
public interface CacheProvider {

	/**
	 * Configure the cache
	 *
	 * @param regionName the name of the cache region
	 * @param autoCreate autoCreate settings
	 * @throws CacheException
	 */
	public Cache buildCache(String regionName, boolean autoCreate) throws CacheException;

	/**
	 * Callback to perform any necessary initialization of the underlying cache implementation
	 * during SessionFactory construction.
	 *
	 * @param properties current configuration settings.
	 */
	public void start() throws CacheException;

	/**
	 * Callback to perform any necessary cleanup of the underlying cache implementation
	 * during SessionFactory.close().
	 */
	public void stop();
	
}
