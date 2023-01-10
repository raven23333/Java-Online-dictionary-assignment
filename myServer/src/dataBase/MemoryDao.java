package dataBase;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import entities.*;

public class MemoryDao {
	private static volatile MemoryDao instance = null;

	private MemoryDao() {
	}

	public static MemoryDao getInstance() {
		if (instance == null) {
			synchronized (MemoryDao.class) {
				if (instance == null) {
					instance = new MemoryDao();
				}
			}
		}
		return instance;
	}

	ConcurrentHashMap<String, Entry> Dict = new ConcurrentHashMap<String, Entry>();

	public DbSerial exportDB() {
		synchronized (this) {
			DbSerial outputSerial = new DbSerial(Dict);
			return outputSerial;
		}
	}

	public boolean importDB(DbSerial dbs) {
		synchronized (this) {
			if (!dbs.data.isEmpty()) {
				this.Dict = dbs.data;
				return true;
			} else
				return false;
		}
	}

	public boolean addToDB(DbSerial dbs, boolean replace) {
		//merge a dbseral to current one 
		synchronized (this) {
			if (dbs.data.isEmpty())
				return false;
			if (replace) {
				this.Dict.putAll(dbs.data);
				return true;
			} else {
				Iterator<java.util.Map.Entry<String, Entry>> iter = dbs.data.entrySet().iterator();
				while (iter.hasNext()) {
					@SuppressWarnings("rawtypes")
					ConcurrentHashMap.Entry entry = (ConcurrentHashMap.Entry) iter.next();
					this.Dict.putIfAbsent((String) entry.getKey(), (Entry) entry.getValue());
				}
				return true;
			}
		}
	}
	
	public MyDatagram handleRequest(MyDatagram req) {
		//use the functions below to do the operation according to the command in datagram 
		MyDatagram ack = new MyDatagram(req);
		ack.command = req.command;
		switch (req.command) {
		case Constants.SEARCH:
			Entry resEntry = this.search(req.data);
			if (resEntry == null) {
				ack.state = Constants.NO_SUCH_ENTRY;
			} else {
				ack.data = resEntry;
				ack.state = Constants.SUCCESS;
			}
			break;
		case Constants.MODIFY:
			ack.state = this.modify(req.data);
			break;
		case Constants.DELETE:
			ack.state = this.delete(req.data);
			break;
		case Constants.INSERT:
			ack.state = this.insert(req.data);
			break;
		case Constants.UPDATE:
			ack.state = this.update(req.data);
			break;
		}
		return ack;
	}

	protected int insert(Entry e) {
		// if elements already exists, only the unrepeated elements are added
		String curKey = e.Word;
		Entry curEntry = Dict.get(curKey);
		if(curEntry==null) {
			Entry res = Dict.putIfAbsent(curKey, e);
			if (res == null)
				return Constants.SUCCESS;
			else
				return Constants.OUTDATED_MODIFY;
		}
		else {
			if(!e.isNotOlderThan(curEntry))
				return Constants.OUTDATED_MODIFY;
			e.Explain=Entry.merge(curEntry.Explain, e.Explain);
			if (Dict.replace(curKey, curEntry, e))
				return Constants.SUCCESS;
			else
				return Constants.OUTDATED_MODIFY;
		}
	}
	
	protected int update(Entry e) {
		// if the input entry is older than that in Dict, nothing will happen
		// if isForced==False and no element with given key, nothing will happen
		String curKey = e.Word;
		Entry curEntry = Dict.get(curKey);
		if (curEntry == null) {
			return Constants.NO_SUCH_ENTRY;
		} else if (e.isNotOlderThan(curEntry)) {
			if (Dict.replace(curKey, curEntry, e))
				return Constants.SUCCESS;
			else
				return Constants.OUTDATED_MODIFY;
		} else
			return Constants.OUTDATED_MODIFY;
	}

	protected int modify(Entry e) {
		//if don't exist, insert, otherwise, update
		String curKey = e.Word;
		Entry curEntry = Dict.get(curKey);
		if (curEntry == null) {
			if (Dict.putIfAbsent(curKey, e) == null)
				return Constants.SUCCESS;
			else
				return Constants.OUTDATED_MODIFY;
		} else if (e.isNotOlderThan(curEntry)) {
			if (Dict.replace(curKey, curEntry, e))
				return Constants.SUCCESS;
			else
				return Constants.OUTDATED_MODIFY;
		} else
			return Constants.OUTDATED_MODIFY;
	}

	protected Entry search(Entry e) {
		return Dict.get(e.Word);
	}

	protected int delete(Entry e) {
		// if the delete time is older than that in Dict, nothing will happen
		String curKey = e.Word;
		Entry curEntry = Dict.get(curKey);
		if (curEntry == null)
			return Constants.NO_SUCH_ENTRY;
		else if (!e.isNotOlderThan(curEntry))
			return Constants.OUTDATED_MODIFY;
		if (Dict.remove(curKey, curEntry))
			return Constants.SUCCESS;
		else
			return Constants.OUTDATED_MODIFY;
	}
}
