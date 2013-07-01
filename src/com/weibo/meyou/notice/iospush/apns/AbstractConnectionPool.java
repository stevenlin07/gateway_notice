package com.weibo.meyou.notice.iospush.apns;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/**
 * 
 * @author yangshuo3
 * @date 2012-10-14
 *
 * @param <T>
 */

public abstract class AbstractConnectionPool<T> implements Pool<T> {
	private static final Logger log = Logger.getLogger("notify_service");
	protected int initConn = 10;
	protected int minConn = 10;
	protected int maxConn = 10;
	
	protected String[] servers;
	//private long maxIdle = 1000 * 60 * 5; // max idle time for avail sockets
	//private long maxBusyTime = 1000 * 30; // max idle time for avail sockets
	protected long maintSleep = 1000 * 10; // maintenance thread sleep time
	// connections
	public static final long MAX_RETRY_DELAY = 10 * 60 * 1000; // max of 10
	
	//private static final int SOCKET_STATUS_BUSY = 1;
	/**
	 * socket��״̬���Ѿ�ʧЧ
	 */
	private static final int SOCKET_STATUS_DEAD = 2;
	
	// Pool data
	private MaintThread maintThread;
	private boolean initialized = false;
	@SuppressWarnings("unused")
	private int maxCreate = 1; // this will be initialized by pool when the
	// pool is initialized

	// initial, min and max pool sizes
	private	int poolMultiplier = 3;
	// locks
	private final ReentrantLock hostDeadLock = new ReentrantLock();
	private final ReentrantLock initDeadLock = new ReentrantLock();
	
	// dead server map
	private ConcurrentMap<String, Date> hostDead;
	private ConcurrentMap<String, Long> hostDeadDur;

	// map to hold all available sockets
	// map to hold socket status;
	private ConcurrentMap<String, ConcurrentMap<T, Long>> socketPool;
	private ConcurrentMap<String, Queue <T>> usedPool;
	private ConcurrentMap<String, ConcurrentMap<T, Integer>> statusPool;

	/**
	 * Initializes the pool.
	 */
	public void initialize() {

		// check to see if already initialized
		if ( initialized ) {
			log.error("++++ trying to initialize an already initialized pool");
			return;
		}

		initDeadLock.lock();
		try {
			// check to see if already initialized
			if (initialized) {
				log.error("++++ trying to initialize an already initialized pool");
				return;
			}

			// pools
			socketPool = new ConcurrentHashMap<String, ConcurrentMap<T, Long>>(
					initConn);
			statusPool = new ConcurrentHashMap<String, ConcurrentMap<T, Integer>>(
					initConn);
			usedPool=new ConcurrentHashMap<String, Queue <T>> (
					initConn);
			
			hostDeadDur = new ConcurrentHashMap<String, Long>();
			hostDead = new ConcurrentHashMap<String, Date>();
			maxCreate = (poolMultiplier > minConn) ? minConn : minConn
					/ poolMultiplier; // only create up to maxCreate
			// connections at once

			log.info("info ++++ initializing pool with following settings:");
			if (log.isDebugEnabled()) {
				log.debug("++++ initializing pool with following settings:");
				log.debug("++++ initial size: " + initConn);
				log.debug("++++ min spare   : " + minConn);
				log.debug("++++ max spare   : " + maxConn);
			}
			
			for (int i = 0; i < servers.length; i++) {
				// create initial connections
				if (log.isDebugEnabled())
					log.debug("+++ creating initial connections (" + initConn
							+ ") for host: " + servers[i]);

				for (int j = 0; j < initConn; j++) {
					T conn = createConnection(servers[i]);
					if (conn == null) {
						log.error("++++ failed to create connection to: "
								+ servers[i] + " -- only " + j + " created.");
						break;
					}
					Queue<T> queue=usedPool.get(servers[i]);
					if(queue==null){
						queue=new ConcurrentLinkedQueue<T>();
						usedPool.put(servers[i], queue);
					}
					queue.add(conn);
					addSocketToPool(socketPool, servers[i], conn, System
							.currentTimeMillis(), true);
					if (log.isDebugEnabled())
						log.debug("++++ created and added socket: "
								+ conn.toString() + " for host " + servers[i]);
				}
				if(statusPool.get(servers[i])==null){
					statusPool.putIfAbsent(servers[i],  new ConcurrentHashMap<T, Integer>());
				}
			}
			
			// mark pool as initialized
			this.initialized = true;

			// start maint thread
			if (this.maintSleep > 0)
				this.startMaintThread();

		} finally {
			initDeadLock.unlock();
		}

	}
	public abstract T doCreateConnection(String host) throws Exception;
	public abstract boolean isConnected(T conn);
	
	private T createConnection(String host) {

		T conn = null;

		// if host is dead, then we don't need to try again
		// until the dead status has expired
		// we do not try to put back in if failback is off
		hostDeadLock.lock();
		try {
			if (hostDead.containsKey(host)
					&& hostDeadDur.containsKey(host)) {

				Date store = hostDead.get(host);
				long expire = hostDeadDur.get(host).longValue();

				if ((store.getTime() + expire) > System.currentTimeMillis())
					return null;
			}
		} finally {
			hostDeadLock.unlock();
		}

		try {
			conn = doCreateConnection( host );

			if (!isConnected(conn)) {
				log.error("++++ failed to get SockIO obj for: " + host
						+ " -- new socket is not connected");
				addSocketToPool(statusPool, host, conn, SOCKET_STATUS_DEAD,
						true);
				// socket = null;
			}
		} catch (Exception ex) {
			log.error("++++ failed to get SockIO obj for: " + host);
			log.error(ex.getMessage(), ex);
			conn = null;
		}

		// if we failed to get socket, then mark
		// host dead for a duration which falls off
		hostDeadLock.lock();
		try {
			if (conn == null) {
				Date now = new Date();
				hostDead.put(host, now);

				long expire = (hostDeadDur.containsKey(host)) ? (((Long) hostDeadDur
						.get(host)).longValue() * 2)
						: 1000;

				if (expire > MAX_RETRY_DELAY)
					expire = MAX_RETRY_DELAY;

				hostDeadDur.put(host, new Long(expire));
				if (log.isDebugEnabled())
					log.debug("++++ ignoring dead host: " + host + " for "
							+ expire + " ms");

				// also clear all entries for this host from availPool
				
				clearHostFromPool(host);
			} else {
				if (log.isDebugEnabled())
					log.debug("++++ created socket (" + conn.toString()
							+ ") for host: " + host);
				if (hostDead.containsKey(host) || hostDeadDur.containsKey(host)) {
					hostDead.remove(host);
					hostDeadDur.remove(host);
				}
			}
		} finally {
			hostDeadLock.unlock();
		}

		return conn;
	}
	public abstract void doClose(T conn) throws IOException,Exception;
	
	public void close(String host,T conn) throws IOException,Exception{
		doClose(conn);
		if(conn!=null)
			addSocketToPool(statusPool, host, conn, SOCKET_STATUS_DEAD, true);
	}
	
	public void checkIn(String host,T conn) {
		if (isConnected(conn) ) {
			Queue<T> queue=usedPool.get(host);
			queue.add(conn);
			
			// add to avail pool
			if (log.isDebugEnabled())
				log.debug("++++ returning socket (" + conn.toString()
						+ " to avail pool for host: " + host);
			
			
		} else {
			addSocketToPool(statusPool, host, conn, SOCKET_STATUS_DEAD, true);
			// socket = null;
		}

	}
	
	public T checkOut(String host) {
		T conn=null;
		conn= getConnection(host);
		
		if (conn == null || !isConnected(conn)) {
			
			if (conn != null) {
				addSocketToPool(statusPool, host, conn,
						SOCKET_STATUS_DEAD, true);
				// sock = null;
			}
		

		}
		return conn;
	}
	
	public T getConnection(String host) {

		if (!this.initialized) {
			log.error("attempting to get SockIO from uninitialized pool!");
			return null;
		}

		if (host == null)
			return null;

		// if we have items in the pool
		// then we can return it
		Queue<T> conns = usedPool.get(host);
		if (conns != null && !conns.isEmpty()) {
			T conn=null;
			while ((conn = conns.poll()) != null) {
				if (isConnected(conn)) {

					addSocketToPool(socketPool, host, conn, System
							.currentTimeMillis(), true);

					if (log.isDebugEnabled())
						log.debug("++++ moving socket for host (" + host
								+ ") to busy pool ... socket: " + conn);

					// return socket
					return conn;

				} else {
					// add to deadpool for later reaping
					addSocketToPool(statusPool, host, conn,
							SOCKET_STATUS_DEAD, true);
				}
			}
		}

		// create one socket -- let the maint thread take care of creating more
//		T conn = createConnection(host);
//		if (conn != null) {
//			addSocketToPool(socketPool, host, conn, System
//					.currentTimeMillis(), true);
//		}

		return null;
	}
	protected <I> boolean addSocketToPool(
			ConcurrentMap<String, ConcurrentMap<T, I>> pool, String host,
			T socket, I value, boolean needReplace) {
		boolean result = false;

		ConcurrentMap<T, I>  sockets = pool.get(host);
		if (sockets==null) {
			sockets = new ConcurrentHashMap<T, I>();
			pool.putIfAbsent(host, sockets);
		}
		if (sockets != null) {
			if (needReplace) {
				sockets.put(socket, value);
				result = true;
			} else {
				if (sockets.putIfAbsent(socket, value) == null)
					result = true;
			}
		}

		return result;
	}
	protected void clearHostFromPool(String host) {
		Map<T, Long> conns = socketPool.remove(host);

		if (conns != null) {

			if (conns.size() > 0) {
				for (T conn : conns.keySet()) {
					conns.remove(conn);

					try {
						if (statusPool.get(host) != null)
							statusPool.get(host).remove(conn);

						close(host,conn);
					} catch (Exception ioe) {
						log.error("++++ failed to close socket: "
								+ ioe.getMessage());
					}

					conn = null;
				}
			}

		}
	}
	
	protected void startMaintThread() {
		if (maintThread != null) {

			if (maintThread.isRunning()) {
				log.error("main thread already running");
			} else {
				maintThread.start();
			}
		} else {
			maintThread = new MaintThread(this);
			maintThread.setInterval(this.maintSleep);
			maintThread.start();
		}
	}
	protected void selfMaint() {
		log.info(this.getClass().getName() + "; log referrence is " + log);
		if (log.isDebugEnabled())
			log.debug("++++ Starting self maintenance....");
		// go through avail sockets and create sockets
		// as needed to maintain pool settings
		Map<String, Integer> needSockets = new HashMap<String, Integer>();

		// find out how many to create
		for (Iterator<String> i = socketPool.keySet().iterator(); i.hasNext();) {
			String host = i.next();
			Map<T, Long> sockets = socketPool.get(host);

			int usedcount =0;
			
			usedcount=sockets.size()-usedPool.get(host).size()-statusPool.get(host).size();


			if (log.isDebugEnabled())
				log.debug("++++ Size of avail pool for host (" + host + ") = "
						+ sockets.size());

			// if pool is too small (n < minSpare)
			if (sockets != null && sockets.size() - usedcount < minConn) {
				// need to create new sockets
				int need = minConn - sockets.size() + usedcount;
				needSockets.put(host, need);
			}
		}

		// now create
		for (String host : needSockets.keySet()) {
			Integer need = needSockets.get(host);

				log.info("++++ Need to create " + need
						+ " new sockets for pool for host: " + host);

			for (int j = 0; j < need; j++) {
				T socket = createConnection(host);

				if (socket == null)
					break;

				Queue<T> queue=usedPool.get(host);
				if(queue==null){
					queue=new ConcurrentLinkedQueue<T>();
					usedPool.put(host, queue);
				}
				queue.add(socket);
				addSocketToPool(socketPool, host, socket, System
						.currentTimeMillis(), true);
			}

		}

		for (Iterator<String> i = socketPool.keySet().iterator(); i.hasNext();) {
			String host = i.next();
			Map<T, Long> sockets = socketPool.get(host);

			if (log.isDebugEnabled())
				log.debug("++++ Size of avail pool for host (" + host + ") = "
						+ sockets.size());

			int usedcount = 0;

			usedcount=sockets.size()-usedPool.get(host).size()-statusPool.get(host).size();
			if (sockets != null && (sockets.size() - usedcount > maxConn)) {
				// need to close down some sockets
				int diff = sockets.size() - usedcount - maxConn;
				int needToClose = (diff <= poolMultiplier) ? diff : (diff)
						/ poolMultiplier;

					log.info("++++ need to remove " + needToClose
							+ " spare sockets for pool for host: " + host);

				Queue<T> queue=usedPool.get(host);
				T socket ;
				while(needToClose > 0&&(socket=queue.poll())!=null){
					// remove from the availPool
					addSocketToPool(statusPool, host, socket,
							SOCKET_STATUS_DEAD, true);
					needToClose--;
				}
			}
		}

		// finally clean out the deadPool
		for (Iterator<String> i = statusPool.keySet().iterator(); i.hasNext();) {
			String host = i.next();
			Map<T, Integer> sockets = statusPool.get(host);

			// loop through all connections and check to see if we have any hung
			// connections

			for (Iterator<T> j = sockets.keySet().iterator(); j.hasNext();) {
				// remove stale entries
				T socket = j.next();

				try {
					Integer status = null;
					if (sockets != null && socket != null)
						status = sockets.get(socket);

					if (status != null
							&& status.intValue() == SOCKET_STATUS_DEAD) {

						if (socketPool.containsKey(host))
							socketPool.get(host).remove(socket);

						if (statusPool.containsKey(host))
							statusPool.get(host).remove(socket);

						usedPool.remove(socket);
						
						doClose(socket);

						socket = null;
					}
				} catch (Exception ex) {
					log.error("++++ failed to close SockIO obj from deadPool");
					log.error(ex.getMessage(), ex);
				}
			}

		}

		if (log.isDebugEnabled())
			log.debug("+++ ending self maintenance.");

	}
	protected class MaintThread extends Thread {

		// logger
		private Logger log = Logger.getLogger(MaintThread.class
				.getName());

		private AbstractConnectionPool<T> pool;
		private long interval = 1000 * 3; // every 3 seconds
		private boolean stopThread = false;
		private boolean running;

		protected MaintThread(AbstractConnectionPool<T>  pool) {
			this.pool = pool;
			this.setDaemon(true);
			this.setName("MaintThread");
		}

		public void setInterval(long interval) {
			this.interval = interval;
		}

		public boolean isRunning() {
			return this.running;
		}

		/**
		 * sets stop variable and interupts any wait
		 */
		public void stopThread() {
			this.stopThread = true;
			this.interrupt();
		}

		/**
		 * Start the thread.
		 */
		public void run() {
			this.running = true;

			while (!this.stopThread) {
				try {
					Thread.sleep(interval);

					// if pool is initialized, then
					// run the maintenance method on itself
					if (pool.isInitialized())
						pool.selfMaint();

				} catch (Exception e) {
					if (e instanceof java.lang.InterruptedException){
						log.info("MaintThread stop !");
					}else{
						log.error("MaintThread error !", e);
					}	
				}
			}

			this.running = false;
		}
	}
	
	private boolean isInitialized(){
		return this.initialized;
	}
	public int getInitConn() {
		return initConn;
	}
	public void setInitConn(int initConn) {
		this.initConn = initConn;
	}
	public int getMinConn() {
		return minConn;
	}
	public void setMinConn(int minConn) {
		this.minConn = minConn;
	}
	public int getMaxConn() {
		return maxConn;
	}
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}
	public String[] getServers() {
		return servers;
	}
	public void setServers(String[] servers) {
		this.servers = servers;
	}
	
	protected T createNewConn(String host){
		T ret = null;
		try {
			ret = doCreateConnection(host);
		} catch (Exception e) {
			log.error("createNewConn failed", e);
		}
		if(ret != null){
			Queue<T> queue=usedPool.get(host);
			if(queue==null){
				queue=new ConcurrentLinkedQueue<T>();
				usedPool.put(host, queue);
			}
			queue.add(ret);
			addSocketToPool(socketPool, host, ret, System
					.currentTimeMillis(), true);
		}
		
		return ret;
	}
}
