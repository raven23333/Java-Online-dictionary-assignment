package webService;
//author:1159950 Yuzhou Huo
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import entities.Constants;
import entities.MyDatagram;
import myClient.InfoShareChunk;

public class TcpService extends BaseService{
	protected Socket curSock=null;
	protected ObjectInputStream ois=null;
	protected ObjectOutputStream oos=null;
	public TcpService(InfoShareChunk info){
		curInfoShare=info;
		connectToServer(true);
		
	}
	
	public boolean connectToServer(boolean retry) {
		try {
			curInfoShare.webState.set(Constants.STOP_CONNECTION);
			System.out.println("start connecting");
			curSock = new Socket(curInfoShare.domain, curInfoShare.port);
			System.out.println("connecting ended");
			oos=new ObjectOutputStream(curSock.getOutputStream());
			ois=new ObjectInputStream(curSock.getInputStream());
			curInfoShare.webState.set(Constants.SUCCESS);
			return true;
		} catch (UnknownHostException e) {
			if(!retry)
				e.printStackTrace();
			return false;
		} catch (Exception e) {
			if(!retry)
				e.printStackTrace();
			return false;
		}
	}
	
	protected void finalize(){disconnect();}
	
	public void disconnect() {
		try {
			ois.close();
			oos.close();
			curSock.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Override
	boolean sendRequest(MyDatagram curDatagram) throws IOException {
		try {
			System.out.println("sending request");
			oos.writeObject(curDatagram);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return true;
	}

	@Override
	MyDatagram receiveReply() throws IOException {
		MyDatagram msg=null;
		try {
			Object obj=ois.readObject();
			if(obj instanceof MyDatagram)
				msg=(MyDatagram)obj;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
		return msg;
	}

	@Override
	void resetConnection() {
		this.connectToServer(true);
	}
}
