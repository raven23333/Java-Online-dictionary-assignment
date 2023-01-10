package entities;

import java.util.Date;

public class MyDatagram implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public Entry data=null;
	public int command; // the command or command to server
	public int state; // the result of the operation
	public long clientNumber; //
	public long requestNumber;// increase by one per request

	public MyDatagram() {
	}

	public MyDatagram(MyDatagram curReq) {
		// construct corresponding reply datagram
		this.data=new Entry(curReq.getKey(),null);
		this.clientNumber = curReq.clientNumber;
		this.requestNumber = curReq.requestNumber;
	}

	public Date getTime() {
		return this.data.Timestamp;
	}

	public String getKey() {
		return this.data.Word;
	}
/*
	public int CoverStatus(MyDatagram target) {
		// usage: to decide whether this datagram can overwrite the modification of
		// another one
		// TODO:refine this
		if (this.command * target.command > 0 && this.getTime().after(target.getTime())
				&& this.getKey().equals(target.getKey())) {
			if (this.command == Constants.DELETE) {
				if (target.command == Constants.DELETE)
					return Constants.NO_SUCH_ENTRY;// discart this
				else
					return 1;// can cover
			}
			if (this.command == Constants.MODIFY)
				return 1;// can cover
			if (this.command == Constants.INSERT)
				if (target.command == Constants.DELETE)
					return 2;// can cover but change this to modify
				else
					return Constants.ENTRY_ALREADY_EXIST;
			if (this.command == Constants.UPDATE)
				if (target.command == Constants.DELETE)
					return Constants.NO_SUCH_ENTRY;// discart this
				else
					return 1;// can cover
		}
		return 0;// inrelevant operation
	}
*/
}
