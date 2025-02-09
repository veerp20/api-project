package com.cloud.aws.model;

public class FileData {
	private String text;
	private long userId;
	private String fileName;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	@Override
	public String toString() {
		return "FileData [text=" + text + ", userId=" + userId + ", fileName=" + fileName + "]";
	}
	
	
	

}
