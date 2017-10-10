package com.cit.ids.navdrawer;

public class NavDrawerItem {
	
	private String vItemName = null;
	private int vIcon = 0;
	
	public NavDrawerItem(){}

	public NavDrawerItem(String vItemName, int vIcon){
		this.vItemName = vItemName;
		this.vIcon = vIcon;
	}
	
	/*getters*/
	public String getItemName(){
		return this.vItemName;
	}
	
	public int getIcon(){
		return this.vIcon;
	}
	
	/*setters*/
	public void setItemName(String vItemName){
		this.vItemName = vItemName;
	}
	
	public void setIcon(int vIcon){
		this.vIcon = vIcon;
	}
}