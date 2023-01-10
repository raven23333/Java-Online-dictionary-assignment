package entities;
//author:1159950 Yuzhou Huo
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Entry implements java.io.Serializable{
	private static final long serialVersionUID=1L;
	public Date Timestamp;
	public String Word;
	public String Explain;
	public Entry(String w, String e) {
		Word = w;
		Explain = e;
		Timestamp = new Date();
	};

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else {
			if (obj instanceof Entry) {
				Entry e = (Entry) obj;
				return (e.Timestamp.equals(Timestamp) && e.Word.equals(Word) && e.Explain.equals(Explain));
			}
			return false;
		}
	}

	// compare whether the entry's date is newer than parameters
	public boolean isNotOlderThan(Date d) {
		return (Timestamp.compareTo(d) >= 0);
	}

	public boolean isNotOlderThan(Entry e) {
		return (Timestamp.compareTo(e.Timestamp) >= 0);
	}
	
	public int appendExplain(String exp) {
		String newExp=merge(this.Explain,exp);
		if(newExp==null)
			return Constants.UNKNOWN_ERROR;
		if(newExp.equals(this.Explain))
			return Constants.EXPLAIN_ALREADY_EXIST;
		return Constants.SUCCESS;
	}
	
	public static String transformToEntry(String raw) {
		String singleLinePattern="^([A-z]+\\s?,?.?)+$";
		String multiLinepattern="^(((\\d:)([A-z]+\\s?,?.?-?)+)+$)";
		if(raw.length()>Constants.MAX_EXPLAINS_LENGTH)
			return null;
		if(Pattern.matches(singleLinePattern, raw)) {
			return raw.replace("\n", "");
		}
		else if(Pattern.matches(multiLinepattern, raw)){
			return raw.replaceFirst("^(\\d:)","").replaceAll("\\d:", "/").replace("\n", "");
		}
		return null;
	}
	
	public static String transformToRaw(String entryExplain) {
		if(entryExplain.contains("/")){
			String[] explains=entryExplain.split("/");
			String output="";
			for(int i=0;i<explains.length;i++) {
				output=output+String.valueOf(i+1)+":"+explains[i]+"\n";
			}
			return output;
		}
		else
			return entryExplain;//
	}
	
	public static boolean validateKey(String key) {
		String keyPattern="^([a-z]|[A-Z]|-)+$";
		if(key==null || key.length()>Constants.MAX_KEY_LENGTH)
			return false;
		return Pattern.matches(keyPattern,key);
	}
	
	public static String merge(String origin, String append) {
		if (append == null || append.equals(""))
			return null;
		HashSet<String> originSet = new HashSet<String>();
		Collections.addAll(originSet, origin.split("/"));
		if(append.contains("/"))
			Collections.addAll(originSet, append.split("/"));
		else
			originSet.add(append);
		String res = "";
		for (String str : originSet)
			res = res + str + "/";
		return res.substring(0, res.length() - 1);
	}
}
