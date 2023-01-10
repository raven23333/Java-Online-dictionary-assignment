package myClient;
//author:1159950 Yuzhou Huo
public class ClientTest {
	public static void main(String args[]) {

		String cmd=args[0]+" "+args[1];
		//String cmd="";//"localhost 1234";
		Client inst=new Client();
		if(inst.initFromCmd(cmd)) {
			try {
				inst.runClient();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
