package com.ifeng.weChatSpider.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class TextFile {
	
	private static Log log = LogFactory.getLog(TextFile.class);
	public static String read(String filePath){
		File file = new File(filePath);
		String str = null;
		BufferedReader br = null;
		if(file.exists()){
			try {
				br = new BufferedReader(new FileReader(file));
				String s = null;
				while((s = br.readLine()) != null){
					if(str == null){
						str = s;
					}else{
						str += "\r\n" + s;
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}finally{
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			log.warn(file.getPath() + " not found!");
		}
		return str;
	}
	public static void write(String filePath, String content){
		File file = new File(filePath);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte bt[];
		bt = content.getBytes();
		try {
			FileOutputStream outputStream = new FileOutputStream(file);
			try {
				outputStream.write(bt, 0, bt.length);
				outputStream.close();
				// boolean success=true;
				// System.out.println("写入文件成功");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
