package entities;

import java.io.File;

public class DbCmd {
	public String path=null;
	public String cmd=null;
	public boolean isTxt=false;
	public DbCmd(){}
	public DbCmd(String cmd) {this.cmd=cmd;}
	public String setParas(String cmd) {
		String[] cmdSpilt=cmd.split(" ");
		if(cmdSpilt.length==1)
			this.cmd=cmd;
		if(cmdSpilt.length==2) {
			this.cmd=cmdSpilt[0];
			this.path=cmdSpilt[1];
		}
		int label=0;
		for(label=0;label<Constants.DB_CMDS.length;label++) {
			if(Constants.DB_CMDS[label].equals(this.cmd))
				break;
		}
		if(label>=Constants.DB_CMDS.length) 
			return "unknown command";//",please input one of them:\n"+"export,import,persistNow";
		if(!this.cmd.equals("persistNow")&& !this.cmd.equals("changeFolder")&& !this.cmd.equals("exportToTxt")&& !this.cmd.equals("exportToSerial")) {
			if(this.path!=null) {
			File file = new File(path);
			if(!(file.exists()&&file.isFile()))
				return "invaild file";
			if(path.contains(".txt"))
				isTxt=true;
			else if(path.contains(".ser"))
				isTxt=false;
			else
				return "invaild file";}
		}
		if(this.cmd.equals("changeFolder")||this.cmd.equals("exportToTxt")||this.cmd.equals("exportToSerial")) {
			File file = new File(path);
			if(!(file.exists()&&file.isDirectory()))
				return "invaild dir";
		}
		return null;
	}
}
