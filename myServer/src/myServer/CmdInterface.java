package myServer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

import clientMap.TcpThreadPool;
import dataBase.MgrDao;
import entities.DbCmd;

public class CmdInterface {
	Server serv = null;
	int port = 0;
	String path = null;
	Scanner scan=null;
	
	//@SuppressWarnings("resource")
	public boolean init(String cmd) {
		String curCmd = cmd;
		this.scan = new Scanner(System.in);
		while (!validateInitPara(curCmd)) {
			curCmd = scan.nextLine();
		}
		serv = new Server(port, path);
		if (!serv.initWeb()) {
			System.out.println("web error");
			//scan.close();
			return false;
		}
		System.out.println("tcp web inited");
		if (!serv.initDB(path, false)) {
			System.out.println("can't read from given folder\n");
			System.out.println("continue anyway?(Y/anyKey)");
			String ch = scan.next();
			if (ch.equals("Y")) {
				if (serv.initDB(path, true)) {
					System.out.println("db inited");
					serv.start();
					//scan.close();
					return true;
				} else {
					System.out.println("still can't init db");
					//scan.close();
					return false;
				}
			} else {
				//scan.close();
				return false;
			}

		}
		//scan.close();
		serv.start();
		return true;
	}

	protected void finalize(){this.scan.close();}
	
	@SuppressWarnings("static-access")
	public void service() throws InterruptedException, IOException {
		System.out.println("server started, input following command as you like\n");
		
		System.out.println(
				"command list: shutdown;show status;persistNow;weakAdd [full path],strongAdd [full path]\n"+
		"changeFolder [folder path],import [full path],exportToTxt [folder path],exportToSerial [folder path]");
		
		Scanner scan = new Scanner(System.in);
		String curCmd="";
		//while(scan.hasNext())
		//	scan.next();
		while(true) {
			System.out.println("please input");	
			try {
				curCmd = scan.nextLine();
				System.out.println(curCmd);
			}
			catch(Exception e) {
				System.out.println("reading commands error");
			}
			switch(curCmd) {
			case "shutdown":
				System.out.println("stoping the server");
				TcpThreadPool.getInstance().shutdown();
				scan.close();
				serv.tcpWeb.shutdown();
				TcpThreadPool.getInstance().shutdownNow();
				MgrDao.getInstance().setCommand(new DbCmd("persistNow"));
				serv.webThread.join();
				System.out.println("stopped");
				return;
			case "persistNow":
				MgrDao.getInstance().setCommand(new DbCmd("persistNow"));
				MgrDao.getInstance().getResult(-1);
				break;
			case "show status":
				System.out.println("current running thread: "+TcpThreadPool.getInstance().currentRunning.get());
				Date tmpLastPersist=MgrDao.getInstance().getLastPersist();
				if(tmpLastPersist==null)
					System.out.println("last persistent: "+"none");
				else
					System.out.println("last persistent: "+tmpLastPersist.toString());
				break;
			default:
				DbCmd newCmd=new DbCmd();
				String parseRes=newCmd.setParas(curCmd);
				if(parseRes==null) {
					MgrDao.getInstance().setCommand(newCmd);
					MgrDao.getInstance().getResult(-1);
				}
				else
					System.out.println(parseRes);
				break;
			}
		}
		
	}

	protected boolean validateInitPara(String cmd) {
		String[] cmds = cmd.split(" ");
		if (cmds.length != 2) {
			System.out.println("wrong input! usage: [port path]");
			return false;
		}
		int tmpPort = 0;
		try {
			tmpPort = Integer.parseInt(cmds[0]);
		} catch (NumberFormatException e) {
			System.out.println("invalid port number!");
			return false;
		}
		File file = new File(cmds[1]);
		if (!(file.exists() && file.isDirectory())) {
			System.out.println("invalid home folder");
			return false;
		}
		port = tmpPort;
		path = cmds[1];
		return true;
	}
}
