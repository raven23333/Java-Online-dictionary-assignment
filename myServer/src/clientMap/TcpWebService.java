package clientMap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import entities.Constants;

public class TcpWebService implements Runnable{
	public ServerSocket serverSock;
	public int port;
	protected TcpThreadPool pool=null;
	public volatile static boolean isRunning=true;
	public TcpWebService(int p){
		this.port=p;
	}
	
	public boolean init() {
		try {
			serverSock=new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		pool=TcpThreadPool.getInstance();
		pool.init(Constants.MAX_POOL_SIZE);
		return true;
	}
	
	public void shutdown() {
		try {
			serverSock.close();
			TcpThreadPool pool=TcpThreadPool.getInstance();
			pool.shutdown();
			Thread.sleep(100);
			pool.shutdownNow();
		} catch (IOException | InterruptedException e) {
			
		}
		
	}
	
	protected void finalize() throws IOException {
		serverSock.close();
	}

	@Override
	public void run() {
		// listen client's request
		while(isRunning) {
			try {
				System.out.println("server listening¡­¡­");
				Socket clientSock=serverSock.accept();
				System.out.println("server accepted¡­¡­");
				//Thread curClientThread=new Thread(new TcpClient(clientSock));
				//TODO:
				pool.exec(new TcpUser(clientSock));
				//curClientThread.start();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
		shutdown();
	}
	
	
}
