package clientMap;

import java.util.Date;     

import dataBase.MemoryDao;
import entities.Constants;
import entities.MyDatagram;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
public class TcpUser implements Runnable{
	public volatile static boolean isRunning=true;
	Socket curSock = null;
	protected ObjectInputStream ois=null;
	protected ObjectOutputStream oos=null;
	Date lastOperation=null;
	TcpUser(Socket sock){
		this.curSock=sock;
		lastOperation=new Date();
		try {
			ois=new ObjectInputStream(curSock.getInputStream());
			oos=new ObjectOutputStream(curSock.getOutputStream());
			System.out.println("server init finished");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			ois.close();
			oos.close();
			curSock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void finalize() {
		disconnect();
	}

	@Override
	public void run() {
		//handle the request of user
		MyDatagram msg=null;
		while(isRunning) {
			try {
				Object obj=ois.readObject();
				if(obj instanceof MyDatagram)
					msg=(MyDatagram)obj;
				this.lastOperation=msg.getTime();
				if(msg.command==Constants.SUSPEND_CONNECTION) {
					oos.writeObject(msg);
					oos.flush();
					return;
				}
				MemoryDao db=MemoryDao.getInstance();
				MyDatagram res=db.handleRequest(msg);
				oos.writeObject(res);
			} catch (ClassNotFoundException | IOException e) {
				if(e instanceof java.net.SocketException) {
					System.out.println("client ended tcp");
					return;
				}
				else
					e.printStackTrace();
			}	
		}
	}
}
