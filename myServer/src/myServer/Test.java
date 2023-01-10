package myServer;

public class Test {
	
	public static void main(String[] args) throws Exception {
		//String cmd=args[0]+" "+args[1];
		String cmd = "1234 E:\\test";
		CmdInterface curInterface=new CmdInterface();
		curInterface.init(cmd);
		System.out.println("server started");
		curInterface.service();
	}
}