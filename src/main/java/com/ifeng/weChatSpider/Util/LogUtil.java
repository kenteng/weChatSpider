package com.ifeng.weChatSpider.Util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogUtil {
	private static Logger log = null;
	
	
	private static Logger getLog() {
		if (log == null){
			log = Logger.getLogger(LogUtil.class);
		}
		return log;
	}
	
	public static void writeLog(Object message) {
		getLog().info(message);
	}
	
	public static void info(Object message) {
		getLog().info(message);
	}
	
	public static void error(Object message) {
		getLog().error(message);
	}
	
	public static void debug(Object message) {
		getLog().debug(message);
	}
	
	public static void fatal(Object message) {
		getLog().fatal(message);
	}
	
	public static void warn(Object message) {
		getLog().warn(message);
	}
	
	public static void setLevel(Level level) {
		getLog().setLevel(level);
	}
}
