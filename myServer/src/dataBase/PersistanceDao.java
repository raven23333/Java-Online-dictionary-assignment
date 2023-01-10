package dataBase;

import entities.Constants;
import entities.DbSerial;
import entities.Entry;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class PersistanceDao {
	//a state less object handles the io of disk
	private static volatile PersistanceDao instance = null;

	private PersistanceDao() {
	}

	public static PersistanceDao getInstance() {
		if (instance == null) {
			synchronized (MemoryDao.class) {
				if (instance == null) {
					instance = new PersistanceDao();
				}
			}
		}
		return instance;
	}
	
	public static boolean writeToSerialFile(DbSerial data,String path) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(data);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static DbSerial readFromSerialFile(String path) throws FileNotFoundException, IOException, ClassNotFoundException {
		File curfile = new File(path);
		if (!curfile.isFile() || !curfile.exists())
			return null;
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(curfile));
		Object obj = ois.readObject();
		if (obj instanceof DbSerial) {
			ois.close();
			return (DbSerial) obj;
		}
		ois.close();
		return null;
	}

	public static boolean writeToTxtFile(DbSerial data,String path) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			String outPut = data.timestamp.toString() + "\n";
			ConcurrentHashMap<String, Entry> chm = data.data;
			Iterator<java.util.Map.Entry<String, Entry>> iter = chm.entrySet().iterator();
			while (iter.hasNext()) {
				ConcurrentHashMap.Entry entry = (ConcurrentHashMap.Entry) iter.next();
				Entry curEntry = (Entry) entry.getValue();
				outPut = outPut + curEntry.Word + ": " + curEntry.Explain + ". ~" + curEntry.Timestamp.toString()
						+ "\n";
			}
			out.write(outPut);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static DbSerial readFromTxtFile(String path) throws IOException {
		File curfile = new File(path);
		if (!curfile.isFile() || !curfile.exists())
			return null;
		BufferedReader in = new BufferedReader(new FileReader(curfile));
		Date pastDate = new Date(Date.parse(in.readLine()));
		String curEntryString = null;
		ConcurrentHashMap<String, Entry> dataDic = new ConcurrentHashMap<String, Entry>();
		while ((curEntryString = in.readLine()) != null) {
			Entry curEntry = new Entry("", "");
			String[] tmp = curEntryString.split(": ");
			curEntry.Word = tmp[0];
			tmp = tmp[1].split(". ~");
			curEntry.Explain = tmp[0];
			curEntry.Timestamp = new Date(Date.parse(tmp[1]));
			dataDic.put(curEntry.Word, curEntry);
		}
		in.close();
		DbSerial res = new DbSerial(dataDic);
		res.timestamp = pastDate;
		return res;
	}

	public static boolean backupDb(DbSerial current,String folder) {
		//write the serial copy of current dict to disk for copy
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(folder+Constants.DB_BACKUP));
			oos.writeObject(current);
			oos.flush();
			oos.close();
			File oldFile = new File(folder+Constants.DB_SERIAL);
			File newFile = new File(folder+Constants.DB_BACKUP);
			if (oldFile.isFile() && oldFile.exists())
				oldFile.delete();
			newFile.renameTo(new File(folder+Constants.DB_SERIAL));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static DbSerial initDB(String folder) {
		//if unfinished backup exists, handle it first;
		DbSerial raw=null;
		File backup=new File(folder+Constants.DB_BACKUP);
		File serialFile=new File(folder+Constants.DB_SERIAL);
		File txtFile=new File(folder+Constants.DB_TXT);
		if(backup.exists()&&backup.isFile()) {
			try {
				raw=readFromSerialFile(folder+Constants.DB_BACKUP);
			} catch (ClassNotFoundException | IOException e) {
				// last time terminated when doing backup;
				backup.delete();
				//e.printStackTrace();
			}
			if(raw!=null) {
				//succfully backup,but terminated before rename;
				if(serialFile.exists()&&serialFile.isFile()) {
					serialFile.delete();
				}
				backup.renameTo(new File(folder+Constants.DB_SERIAL));
			}
		}
		if(serialFile.exists()&&serialFile.isFile()) {
			try {
				raw=readFromSerialFile(folder+Constants.DB_SERIAL);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("serial file broken");
			}
			if(raw!=null)
				return raw;
		}
		if(txtFile.exists()&&txtFile.isFile()) {
			System.out.println("detected txt file, trying to read from it");
			try {
				raw=readFromSerialFile(folder+Constants.DB_TXT);
			} catch (ClassNotFoundException | IOException e) {
				System.out.println("txt file broken");
			}
			if(raw!=null)
				return raw;
		}
		return null;
	}
}