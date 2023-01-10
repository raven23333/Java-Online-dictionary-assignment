package clientMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

//reference: https://www.cnblogs.com/wxwall/p/7050698.html
public class TcpThreadPool {
	private static volatile TcpThreadPool instance=null;
	TcpThreadPool(){}
	public static TcpThreadPool getInstance() {
		if (instance == null) {
			synchronized (TcpThreadPool.class) {
				if (instance == null) {
					instance = new TcpThreadPool();
				}
			}
		}
		return instance;
	}
	
	int maxPoolSize=0;
	volatile int currentSize=0;//thread currently created
	protected volatile boolean running = true;
	protected boolean shutdown=false;
	public AtomicInteger currentRunning=new AtomicInteger(0);//thread num currently running 
	protected static BlockingQueue<TcpUser> queue = null;
	protected final List<Thread> threadList = new ArrayList<Thread>();
	
	public void init(int poolSize) {
		this.maxPoolSize = poolSize;
        queue = new LinkedBlockingQueue<TcpUser>(poolSize);
	}
	
	public void exec(TcpUser curClient) {
		//add a new socket(task)
		if (curClient == null)
			return;
		if((currentRunning.intValue()>=(currentSize/2))&&(currentSize < maxPoolSize)) {
			//create new thread only need
			currentSize++;
			Thread t = new Thread(new Worker());
			threadList.add(t);
			t.start();
		}else if(currentRunning.intValue()>=maxPoolSize){
			curClient.disconnect();
		}
		
		try {
			queue.put(curClient);//TODO:
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void shutdown() throws InterruptedException {
		running=false;
		if(!threadList.isEmpty()) {
			for (Thread curThread : threadList){
				curThread.interrupt();
				curThread.join();
            }
		}
		shutdown=true;
		Thread.currentThread().interrupt();
	}
	
	public void shutdownNow() {
		if(!threadList.isEmpty()) {
			for (Thread curThread : threadList){
				curThread.stop();
            }
		}
	}
	
	class  Worker implements Runnable{
		@Override
		public void run() {
			TcpUser curClient=null;
			while((!Thread.interrupted()) && running) {
				try {
					curClient=queue.take();
					currentRunning.getAndIncrement();
					curClient.run();
					currentRunning.getAndDecrement();
				} catch (InterruptedException e) {
					
				}
			}
			if(curClient!=null) {
				curClient.disconnect();
			}			
		}
		
	}
}
