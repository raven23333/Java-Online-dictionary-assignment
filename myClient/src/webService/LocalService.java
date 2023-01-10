package webService;
//author:1159950 Yuzhou Huo
import entities.Constants;
import entities.MyDatagram;
import myClient.InfoShareChunk;

public class LocalService extends BaseService{
	public LocalService(InfoShareChunk info){
		this.curInfoShare=info;
		this.clientNumber=info.clientNo;
		info.webState.set(Constants.SUCCESS);
	}
	
	String replace(String raw) {
		if(raw==null)
			return "";
		else
			return raw;
	}

	void showDatagram(MyDatagram curDatagram) {
		System.out.println("key="+this.replace(curDatagram.data.Word));
		System.out.println("value="+this.replace(curDatagram.data.Explain));
		System.out.println("date="+curDatagram.data.Timestamp);
		System.out.println("requestNumber="+curDatagram.requestNumber);
		System.out.println("clientNumber="+curDatagram.clientNumber);
		System.out.println("command="+curDatagram.command);
		System.out.println("key="+this.replace(curDatagram.data.Word));
	}
	@Override
	boolean sendRequest(MyDatagram curDatagram) {
		System.out.println("Pretend sending datagram¡­¡­");
		showDatagram(curDatagram);
		System.out.println("Pretend sending finished¡­¡­");
		this.cache=curDatagram;
		return false;
	}

	@Override
	MyDatagram receiveReply() {
		System.out.println("Pretend receiving reply¡­¡­");
		showDatagram(this.cache);
		System.out.println("Pretend receiving finished¡­¡­");
		MyDatagram tmp=this.cache;
		this.cache=null;
		tmp.state=Constants.OUTDATED_MODIFY;
		return tmp;
	}

	@Override
	void resetConnection() {
		// TODO Auto-generated method stub
		System.out.println("Pretend reset connection¡­¡­");
	}
}
