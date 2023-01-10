package myClient;
//author:1159950 Yuzhou Huo
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import entities.MyDatagram;

public class InfoShareChunk {
	public long clientNo = 0;
	public volatile boolean running = true;
	public volatile int port = 1234;
	public volatile String domain = "localhost";
	public LinkedBlockingQueue<MyDatagram> requestQueue = new LinkedBlockingQueue<MyDatagram>();
	public LinkedBlockingQueue<MyDatagram> replyQueue = new LinkedBlockingQueue<MyDatagram>();
	public volatile AtomicInteger requestNumber=new AtomicInteger(0);
	public volatile AtomicInteger webState=new AtomicInteger(0);
	public Thread webThread;
	public Thread uiThread;
	
	public InfoShareChunk(long cno) {
		this.clientNo = cno;
	}

	public void setConnect(int p, String d) {
		this.port = p;
		this.domain = d;
	}

	public MyDatagram getRequestData(int blockTime) throws InterruptedException {
		return requestQueue.poll(blockTime, TimeUnit.MILLISECONDS);
	}

	public MyDatagram getReplyData(int blockTime) throws InterruptedException {
		return replyQueue.poll(blockTime, TimeUnit.MILLISECONDS);
	}

	public boolean setRequestData(MyDatagram data, int blockTime) throws InterruptedException {
		if (data == null)
			return false;
		if (blockTime < 0)
			return requestQueue.offer(data);
		return requestQueue.offer(data, 0, TimeUnit.MILLISECONDS);
	}

	public boolean setReplyData(MyDatagram data, int blockTime) throws InterruptedException {
		if (data == null)
			return false;
		if (blockTime < 0)
			return replyQueue.offer(data);
		return replyQueue.offer(data, 0, TimeUnit.MILLISECONDS);
	}
}
