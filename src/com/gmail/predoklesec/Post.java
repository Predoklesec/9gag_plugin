package com.gmail.predoklesec;

import org.bukkit.ChatColor;

public class Post {
	String title;
	String link;
	
	Post(String t, String d){
		this.title = t;
		this.link = d;
	}
	
	public String toString() {
		return ChatColor.RED + "[9gag] " + ChatColor.YELLOW + this.title;
	}
	
	public String getLink(){
		return this.link;
	}
	
	public String getNews(){
		return this.title;
	}
}
