package myServer;
import clientMap.TcpWebService;
import dataBase.MgrDao;
import entities.DbCmd;

import java.io.*;
public class Server {
	public int port;
	public String path;
	TcpWebService tcpWeb=null;
	Thread webThread=null;
	Thread dbThread=null;
	public Server(int port,String path){
		this.port=port;
		this.path=path;
	}
	public boolean initWeb() {
		tcpWeb=new TcpWebService(this.port);
		boolean tcpState=tcpWeb.init();
		if(tcpState)
			webThread=new Thread(tcpWeb);
		return tcpState;
	}
	public boolean initDB(String path,boolean runWithoutFile){
		boolean dbState=false;
		MgrDao curMgr=MgrDao.getInstance();
		curMgr.homefolder=path;
		if(!runWithoutFile) {
			try {
				dbState=curMgr.runOneStep(new DbCmd("init"));
				
			} catch (ClassNotFoundException | IOException e) {
				dbState=false;
				e.printStackTrace();
			}
		}else 
			dbState=true;
		if(dbState) {
			System.out.println("initing db");
			dbThread=new Thread(curMgr);
		}
		return dbState;
	}
	public void start() {
		dbThread.setDaemon(true);
		webThread.start();
		dbThread.start();
	}
	public void shutdown() throws InterruptedException {
		if(tcpWeb!=null) {
			tcpWeb.isRunning=false;
			webThread.interrupt();
		}
		MgrDao.getInstance().isRunning=false;
		dbThread.interrupt();
		dbThread.join();
		webThread.stop();
		System.out.println("server ended");
	}
}
