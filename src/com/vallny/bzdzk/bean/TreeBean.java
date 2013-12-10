package com.vallny.bzdzk.bean;

public class TreeBean {

	public enum Type {
		zrq, jlx, xq, jzw, zrc, mph, dy, fj
	}

	private String id;
//	private int jdid;
//	private long zrqid;
	private String sjid;
//	private Type type;
//	private Type sjtype;
	private String name;
	
	private boolean mark;
	private String isParent;

	

	
	


	public boolean getMark() {
		return mark;
	}

	public void setMark(boolean mark) {
		this.mark = mark;
	}

	public String getIsParent() {
		return isParent;
	}

	public void setIsParent(String isParent) {
		this.isParent = isParent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSjid() {
		return sjid;
	}

	public void setSjid(String sjid) {
		this.sjid = sjid;
	}

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
