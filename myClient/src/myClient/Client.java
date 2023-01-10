package myClient;

//author:1159950 Yuzhou Huo
import java.util.Random;
import java.util.Scanner;
//import java.util.Scanner;

import entities.Constants;
import userInterface.GUInterface;
import webService.BaseService;
import webService.TcpService;

public class Client {
	GUInterface curUI = null;
	BaseService curWeb=null;
	InfoShareChunk curInfoShare=null;
	public final long clientNo = new Random().nextInt();
	public boolean init() {
		curUI=new GUInterface(curInfoShare);
		curWeb=new TcpService(curInfoShare);//new LocalService(curInfoShare);
		if(curInfoShare.webState.get()!=Constants.SUCCESS) {
			System.out.println("init failed: unknown host");
			return false;
		}
		return true;
	}
	
	public void runClient() throws InterruptedException {
		Thread webThread=new Thread(curWeb);
		Thread uiThread=new Thread(curUI);
		curInfoShare.uiThread=uiThread;
		curInfoShare.webThread=webThread;
		this.curUI.initGUI();
		webThread.start();
		uiThread.start();
		System.out.println("Client "+String.valueOf(clientNo)+"started");
	}

	public void shutdown() {
		curInfoShare.running=false;
	}
	
//***********************************************************************
	public boolean initFromCmd(String cmd) {
		Scanner scan = new Scanner(System.in);
		while (!validateInitPara(cmd)) {
			cmd = scan.nextLine();
		}
		scan.close();
		curInfoShare=new InfoShareChunk(clientNo);
		curInfoShare.port=tmpPort;
		curInfoShare.domain=tmpHost;
		return init();
	}
	
	private int tmpPort=0;
	private String tmpHost;
	protected boolean validateInitPara(String cmd) {
		String[] cmds = cmd.split(" ");
		if (cmds.length != 2) {
			System.out.println("wrong input! usage: [domain port]");
			return false;
		}
		try {
			tmpPort = Integer.parseInt(cmds[1]);
		} catch (NumberFormatException e) {
			System.out.println("invalid port number!");
			return false;
		}
		tmpHost = cmds[0];
		System.out.println(tmpHost);
		System.out.println(tmpPort);
		return true;
	}
}

