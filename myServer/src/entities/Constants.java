package entities;

public class Constants {
	public static final int SUCCESS = 0;
	public static final int ENTRY_ALREADY_EXIST = 1;
	public static final int NO_SUCH_ENTRY = 2;
	public static final int OUTDATED_MODIFY = 3;
	public static final int EXPLAIN_ALREADY_EXIST = 4;
	public static final int UNKNOWN_ERROR = 9;

	public static final int SEARCH = 0;
	public static final int MODIFY = -1; // update if the target word exist, insert if it doesn't
	public static final int DELETE = -2;
	public static final int INSERT = -3;
	public static final int UPDATE = -4; // the update will fill if the target word doesn't exist

	// dict entry word lengths below
	public static final int MAX_KEY_LENGTH = 20;
	public static final int MAX_EXPLAINS_LENGTH = 150;

	// client states below
	public static final int STOP_CONNECTION = 10;
	public static final int SUSPEND_CONNECTION = 12;
	public static final int UNKNOWN_SERVER = 11;

	// db paras below
	public static final int INSERT_MISSING_ITEMS = 21;
	public static final int INSERT_AND_UPDATE = 22;

	public static final int EMPTY_DBSERIAL = 29;

	public static final String DB_SERIAL = "\\myDb.ser";
	public static final String DB_TXT = "\\myDbOut.txt";
	public static final String DB_BACKUP = "\\myDbBackUp.ser";
	public static final String[] DB_CMDS = { "exportToTxt", "exportToSerial", "import", "persistNow", "weakAdd",
			"strongAdd", "changeFolder", "init" };
	// constants times below
	public static final int EXPIRE_SECONDS = 300;
	public static final int PERSIST_SECONDS = 60;
	// pool paras below
	public static final int MAX_POOL_SIZE = 8;
	// finals below
	public static final int[] CLIENTARR = { Constants.SEARCH, Constants.MODIFY, Constants.DELETE, Constants.INSERT,
			Constants.UPDATE };
}
