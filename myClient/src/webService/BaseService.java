package webService;
//author:1159950 Yuzhou Huo
import java.io.IOException;
import entities.Constants;
import entities.MyDatagram;
import myClient.InfoShareChunk;

public abstract class BaseService implements Runnable {
	long clientNumber = 0;
	InfoShareChunk curInfoShare=null;
	MyDatagram cache;

	MyDatagram addRequestInfo(MyDatagram curDatagram) {
		curDatagram.clientNumber = this.clientNumber;
		return curDatagram;
	}

	abstract boolean sendRequest(MyDatagram curDatagram) throws IOException;
	abstract MyDatagram receiveReply() throws IOException;
	abstract void resetConnection();
	
	public void run() {
		while(curInfoShare.running) {
			if(curInfoShare.webState.get()==Constants.SUCCESS) {
				MyDatagram curDatagram;
				try {
					curDatagram = curInfoShare.getRequestData(-1);
					if(!(curDatagram==null)) {
						curDatagram=this.addRequestInfo(curDatagram);
						sendRequest(curDatagram);
					}
					if(!(curDatagram==null)) {
						curDatagram=this.receiveReply();
						curInfoShare.setReplyData(curDatagram, -1);
					}
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("interrupted");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("host failed");
					resetConnection();
					if(curInfoShare.webState.get()==Constants.SUCCESS) {
						synchronized (curInfoShare) {
							curInfoShare.notifyAll();						
						}
					}
				}
			}
			else {
				//TODO:handle unconnected
				resetConnection();
				if(curInfoShare.webState.get()==Constants.SUCCESS) {
					synchronized (curInfoShare) {
						curInfoShare.notifyAll();						
					}
				}
			}
		}
	}
}
