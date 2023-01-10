package dataBase;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import entities.Constants;
import entities.DbSerial;
import entities.DbCmd;

public class MgrDao implements Runnable {
	private static volatile MgrDao instance = null;
	protected LinkedBlockingQueue<DbCmd> cmdQueue = new LinkedBlockingQueue<DbCmd>();
	protected LinkedBlockingQueue<Boolean> resQueue = new LinkedBlockingQueue<Boolean>();
	public volatile static boolean isRunning=true;
	protected volatile Date lastPersist=null;
	public String homefolder = null;

	public boolean setCommand(DbCmd cmd) {
		return cmdQueue.offer(cmd);
	}
	
	public Date getLastPersist() {return this.lastPersist;}
	
	public Boolean getResult(int waitTime) {
		try {
			if(waitTime<0)
				return this.resQueue.take();
			else
				return this.resQueue.poll(waitTime, TimeUnit.MILLISECONDS);			
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		return null;
	}
	
	public void cleanResult() {this.resQueue.clear();}
	
	MgrDao() {
	}
	public static MgrDao getInstance() {
		if (instance == null) {
			synchronized (MemoryDao.class) {
				if (instance == null) {
					instance = new MgrDao();
				}
			}
		}
		return instance;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (isRunning) {
			try {
				DbCmd curCmd = cmdQueue.poll(1000 * Constants.PERSIST_SECONDS, TimeUnit.MILLISECONDS);
				if (curCmd == null)
					curCmd = new DbCmd("persistNow-auto");
				execCmd(curCmd);
			} catch (InterruptedException e) {
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public boolean runOneStep(DbCmd curCmd) throws ClassNotFoundException, IOException {
		//execute one command in the thread of caller
		synchronized(this){
			if(curCmd!=null)
				execCmd(curCmd);
			return this.getResult(-1);			
		}
	}
	
	@SuppressWarnings("static-access")
	protected void execCmd(DbCmd curCmd) throws IOException, ClassNotFoundException {
		DbSerial curSerial = null;
		boolean state = false;
		if(curCmd.path==null) {
			curCmd.path=this.homefolder+Constants.DB_TXT;
			curCmd.isTxt=true;
		}
		switch (curCmd.cmd) {
		case "init":
			curSerial = PersistanceDao.getInstance().initDB(homefolder);
			if(curSerial!=null){
				MemoryDao.getInstance().importDB(curSerial);
				state=true;
			}else {System.out.println("noDb");}
			break;
		case "persistNow":
			curSerial = MemoryDao.getInstance().exportDB();
			state = PersistanceDao.getInstance().backupDb(curSerial,homefolder);
			if (!state)
				System.out.println("persist error");
			else
				System.out.println("persist success");
				
			this.lastPersist=new Date();
			break;
		case "persistNow-auto":
			curSerial = MemoryDao.getInstance().exportDB();
			state = PersistanceDao.getInstance().backupDb(curSerial,homefolder);
			if (!state)
				System.out.println("persist error");
			this.lastPersist=new Date();
			break;
		case "exportToTxt":
			curSerial = MemoryDao.getInstance().exportDB();
			String tmpTxtPath=curCmd.path+Constants.DB_TXT;
			state = PersistanceDao.getInstance().writeToTxtFile(curSerial,tmpTxtPath);
			if (state)
				System.out.println("successful output");
			else
				System.out.println("output failed");
			break;
		case "exportToSerial":
			curSerial = MemoryDao.getInstance().exportDB();
			String tmpSerPath=curCmd.path+Constants.DB_SERIAL;
				state = PersistanceDao.getInstance().writeToSerialFile(curSerial,tmpSerPath);
			if (state)
				System.out.println("successful output");
			else
				System.out.println("output failed");
			break;
		case "import":
			if(curCmd.isTxt)
				curSerial = PersistanceDao.getInstance().readFromTxtFile(curCmd.path);
			else
				curSerial = PersistanceDao.getInstance().readFromSerialFile(curCmd.path);
			if(curSerial==null)
				state=false;
			else
				state = MemoryDao.getInstance().importDB(curSerial);
			if (state)
				System.out.println("successful read");
			else
				System.out.println("read failed");
			break;
		case "changeFolder":
			String newFolder=curCmd.path;
			if(newFolder.equals(this.homefolder))
				System.out.println("new folder is same to origin");
			else {
				this.homefolder=newFolder;
			}
			break;
		case "weakAdd":
			if(curCmd.isTxt)
				curSerial = PersistanceDao.getInstance().readFromTxtFile(curCmd.path);
			else
				curSerial = PersistanceDao.getInstance().readFromSerialFile(curCmd.path);
			if(curSerial==null)
				state=false;
			else
				state = MemoryDao.getInstance().addToDB(curSerial, false);
			if (state)
				System.out.println("successful add");
			else
				System.out.println("add failed");
			break;
		case "strongAdd":
			if(curCmd.isTxt)
				curSerial = PersistanceDao.getInstance().readFromTxtFile(curCmd.path);
			else
				curSerial = PersistanceDao.getInstance().readFromSerialFile(curCmd.path);
			if(curSerial==null)
				state=false;
			else
				state = MemoryDao.getInstance().addToDB(curSerial, true);
			if (state)
				System.out.println("successful add");
			else
				System.out.println("add failed");
			break;		
		default:
			System.out.println("unknown Db command!");
			break;
		}
		try {
			this.resQueue.put(state);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
