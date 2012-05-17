package com.gmail.predoklesec;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NineGag extends JavaPlugin {
	
	Logger log;
	String topic = "";
	Post temp = null;
	int interval = 0;
	Thread getDataThread;
	String INFO = ChatColor.RED + "[9gag] ";
	ChatColor title_c = ChatColor.YELLOW;
	ChatColor link_c = ChatColor.BLUE;
	ChatColor gag_c = ChatColor.RED;
	
	public void onEnable(){ 
		log = this.getLogger();
		getDataThread = new Thread(gd);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		temp = null;
		interval = getConfig().getInt("Interval")*60*20;
		topic = getConfig().getString("Topic");
		
		if(getConfig().getString("TitleColor") != null) title_c = ChatColor.getByChar(getConfig().getString("TitleColor"));
		if(getConfig().getString("LinkColor") != null) link_c = ChatColor.getByChar(getConfig().getString("LinkColor"));
		if(getConfig().getString("9gagColor") != null) gag_c = ChatColor.getByChar(getConfig().getString("9gagColor"));
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				getDataThread.run();
			}
		}, interval, interval);
	}
	
	public void onDisable(){ 
		// What to do :P?
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (cmd.getName().equalsIgnoreCase("9gag") && sender.hasPermission("9gag.*")){
			if(args.length==0){
				getDataThread.run();
                return true;
            }
			if(args[0].equalsIgnoreCase("set")){
				if(args[1] != null){
					if(args[1].equalsIgnoreCase("interval")){
						if(args[2] != null){
							getConfig().set("Interval", Integer.parseInt(args[2]));
							sender.sendMessage(INFO + ChatColor.YELLOW + "Interval set to: " + args[2] + " minutes.");
							saveConfig();
							onEnable();
							return true;
						}
					}
				}
				if(args[1].equalsIgnoreCase("topic")){
					if(args[2] != null && (args[2].equals("hot") || args[2].equals("trending"))){
						getConfig().set("Topic", args[2]);
						sender.sendMessage(INFO + ChatColor.YELLOW + "Topic set to: " + args[2] + ".");
						saveConfig();
						topic = getConfig().getString("Topic");
						temp = null;
						getDataThread.run();
						return true;
					}
					else{
						sender.sendMessage(INFO + ChatColor.YELLOW + "Topic can be set to hot or trending");
						return true;
					}
				}
            }
            else if(args[0].equalsIgnoreCase("reload")){
            	reloadConfig();
    			onEnable();
    			sender.sendMessage(INFO + ChatColor.YELLOW + "Reloaded!");
                return true;
            }
		}
		return false;
	}
	
	Runnable gd = new Runnable() {
		public void run() {
			getDataRaw();
		 }
	};
	
	
	@SuppressWarnings("deprecation")
	public void getDataRaw(){
	      URL u;
	      InputStream is = null;
	      DataInputStream dis;
	      String s;
	      boolean read = false;
	      String html = "";
	      int stevec = 0;
	      Post[] tableofnews = new Post[10];

	      try {
	         u = new URL("http://9gag.com/"+topic);
	         is = u.openStream();
	         dis = new DataInputStream(new BufferedInputStream(is));
	         
	         while ((s = dis.readLine()) != null) {
	        	 if(s.contains("<ul id=")) read = true;
	        	 if(read){
	        		 //System.out.println(s);
	        		 html += s;
	        	 }
	         }
	         
	         String regex = "<li class=\" entry-item\" (.*?)</li>";
	         Pattern p = Pattern.compile(regex);
	 		 Matcher m = p.matcher(html);
	 		 while(m.find()) {
	 			String urlStr = m.group();
	 			
	 			String regexTitle = "data-text=\"(.*?)\"";
	 			Pattern p3 = Pattern.compile(regexTitle);
		 		Matcher m3 = p3.matcher(urlStr);
		 		String TitleStr = "";
		 		while(m3.find()) {
		 			TitleStr = m3.group().replaceAll("data-text=\"", "").replaceAll("\"", "").replaceAll("&#039;", "'");
		 			//System.out.println("Title: " + TitleStr);
		 		}
	 			
	 			String regexLink = "data-url=\"(.*?)\"";
	 			Pattern p2 = Pattern.compile(regexLink);
		 		Matcher m2 = p2.matcher(urlStr);
		 		String LinkStr = "";
		 		while(m2.find()) {
		 			LinkStr = m2.group().replaceAll("data-url=\"", "").replaceAll("\"", "");
		 			//System.out.println("Link: " + LinkStr);
		 		}
		 		
		 		tableofnews[stevec] = new Post(TitleStr, LinkStr);
	 			stevec++;
	 		}
	 		 
	 		if(temp==null){
				 temp = tableofnews[0];
				 getServer().broadcastMessage(ChatColor.BOLD + "" + gag_c + "[9gag] " + ChatColor.RESET  + title_c + temp.getNews());
				 getServer().broadcastMessage(link_c + temp.getLink());
			}
	 		else{
	 			for(int i=0; i<stevec; i++){
	 				if(tableofnews[i].getLink().compareTo(temp.getLink()) == 0){
	 					//getServer().broadcastMessage(INFO + "No new posts!");
	 					log.info("No new posts!");
	 					break;
	 		 		}
	 				else{
	 					getServer().broadcastMessage(ChatColor.BOLD + "" + gag_c + "[9gag] " + ChatColor.RESET  + title_c + tableofnews[i].getNews());
	 					getServer().broadcastMessage(link_c + tableofnews[i].getLink());
	 				}
	 		 	}
	 			temp = tableofnews[0];
	 		}

	      } catch (MalformedURLException mue) {
	         System.out.println(INFO + "Ouch - a MalformedURLException happened.");
	         mue.printStackTrace();
	      } catch (IOException ioe) {
	         System.out.println(INFO + "Oops- an IOException happened.");
	         ioe.printStackTrace();
	      } finally {
	    	  try {
	    		  is.close();
	    	  } catch (IOException ioe) {
	    		  // just going to ignore this one
	    	  }
	      }
	   }
	
}
