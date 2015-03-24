package com.luo.demos.wifidemo.p2pdemo.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;

import com.luo.demos.utils.Logger;
import com.luo.demos.wifidemo.p2pdemo.module.WifiP2pConfigInfo;

/**
 * It's a HandlerThread bind by WifiP2pService
 * 
 * @author luochenxun
 *
 */
public class ThreadPoolManager extends HandlerThread {
	
	private final static String TAG = "ServiceThread";
	
	/**  The socketServer */
	private final ServerSocket mServer;
	
	/** Thread Pool */
	private final ExecutorService mThreadPool;
	
	/**  service */
	private final WifiP2pService mP2pService;
	
	public ThreadPoolManager(WifiP2pService service, int port, int poolSize)
			throws IOException {
		super(TAG, Process.THREAD_PRIORITY_FOREGROUND);
		
		// Init the main member: service , serverSocket , threadPool
		this.mP2pService = service;
		mServer = new ServerSocket(port);
		mThreadPool = Executors.newFixedThreadPool(poolSize);
		Logger.d(TAG, "ThreadPoolManager Constructor ...");
	}
	
	private boolean isServiceRun = true;
	final void setServiceRun(boolean isRun) {
		this.isServiceRun = isRun; 
	}
	final boolean isServiceRun() {
		return isServiceRun; 
	}

	/**
	 * The main HandlerLoop of the P2pservice
	 */
	private Handler mHandler = null;
	public Handler getHandler() {
		return mHandler;
	}
	static private class ServiceThreadHandler extends Handler {
		private ThreadPoolManager sThread;
		
		ServiceThreadHandler(ThreadPoolManager service) {
			super(service.getLooper());
			this.sThread = service;
		}
		
		// Handle msg
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WifiP2pConfigInfo.MSG_SERVICE_POOL_START:
				while (sThread.isServiceRun()) {
					try {
						Logger.d(TAG, "run ...");
						Socket sock = sThread.mServer.accept();
						sThread.mThreadPool.execute(new HandleAcceptSocket(sThread.mP2pService, sock));
					} catch (Exception ex) {
						Logger.e(TAG, "Exception ex:" + ex);
						sThread.mThreadPool.shutdown();
						break;
					}			
				}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}
	
	/**
	 * Init the ThreadHandler
	 */
	public void init() {
		Logger.d(this.getName(), "init - isAlive " + isAlive());
		setServiceRun(true);
		if (!this.isAlive()) {
			this.start();
			mHandler = new ServiceThreadHandler(this);
		}
		Message msg = new Message();
		msg.what = WifiP2pConfigInfo.MSG_SERVICE_POOL_START;
		getHandler().sendMessage(msg);
	}

	/**
	 * UnInit
	 */
	public void uninit() {
		Logger.d(this.getName(), "uninit");
		setServiceRun(false);
	}
	
	/**
	 * Execute a runnable by threadPool
	 */
	public void execute (Runnable command) {
		mThreadPool.execute(command);
	}
	
	/**
	 *  shutdown the thread and set the ThreadHandler's run-state to false
	 */
	public void destory() {
		setServiceRun(false);
		shutdownAndAwaitTermination();
		this.quit();
	}
	
	/**
	 * shutdown the threadPool
	 */
	private void shutdownAndAwaitTermination() {// ExecutorService pool
		mThreadPool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!mThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
				mThreadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!mThreadPool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
			if (!mServer.isClosed()) {
				mServer.close();
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			mThreadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			Logger.e(TAG, "InterruptedException ie:", ie);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.e(TAG, "IOException e:", e);
		}
	}
}

/**
 * Handle the receive socket and callback the service
 * 
 * @author luochenxun
 */
class HandleAcceptSocket implements Runnable {
	private final Socket socket;
	private final WifiP2pService mService;
	private static final ReentrantLock lockRecvFile = new ReentrantLock();

	HandleAcceptSocket(WifiP2pService service, Socket socket) {
		this.mService = service;
		this.socket = socket;
	}
	
	public void closeSocket() {
		if (!socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				Logger.e(this.getClass().getName(), "exception e:" + e);
				e.printStackTrace();
			}
		}
	}
	public void run() {
		// read and service request on socket
		SocketAddress sockAddr = socket.getRemoteSocketAddress();
		Logger.d(this.getClass().getName(), "accept a remote socket, sockAddr:" + sockAddr);
		
		try {
			InputStream ins = socket.getInputStream();
			int iCommand = ins.read();
			Logger.d(this.getClass().getName(), "receive command:" + iCommand );
			
			if (iCommand == WifiP2pConfigInfo.COMMAND_ID_SEND_PEER_INFO) {
				mService.handleRecvPeerInfo(ins);
			} 
			else if (iCommand == WifiP2pConfigInfo.COMMAND_ID_SEND_FILE) {
				lockRecvFile.lock();
				try {
					mService.setRemoteSockAddress(sockAddr);
					mService.handleRecvFile(ins);
				} finally {
					lockRecvFile.unlock();
				}
			} 
			else if (iCommand == WifiP2pConfigInfo.COMMAND_ID_REQUEST_SEND_FILE) {
				mService.handleRecvFileInfo(ins);
			} 
			else if (iCommand == WifiP2pConfigInfo.COMMAND_ID_RESPONSE_SEND_FILE) {
				// TODO ...
			} 
			else if (iCommand == WifiP2pConfigInfo.COMMAND_ID_BROADCAST_PEER_LIST) {
				mService.handleRecvPeerList(ins);
			}  
			else if (iCommand == WifiP2pConfigInfo.COMMAND_ID_SEND_STRING) {
				// TODO ...
			}
			
			ins.close();
		} catch (IOException e) {
			Logger.e(this.getClass().getName(), e.getMessage());
			return;
		}
	}
}
