package com.ifeng.weChatSpider.Mongo;


public class SelectField {
	private String name;
	private String alias;
	
	public SelectField(String name) {
		this.name= name;
		this.alias = name;
	}
	public  SelectField(String name, String alias) {
		this.name=name;
		this.alias = alias;
	}
	
	public String getName() {
		return name;
	}
	public String getAlias() {
		return alias;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}
