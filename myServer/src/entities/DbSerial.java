package entities;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
public class DbSerial implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	public Date timestamp;
	public ConcurrentHashMap<String,Entry> data;
	public DbSerial(ConcurrentHashMap<String,Entry> chm) {
		data=new ConcurrentHashMap<String,Entry>();
		data.putAll(chm);
		timestamp=new Date();
	}
}
