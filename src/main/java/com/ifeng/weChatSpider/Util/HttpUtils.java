package com.ifeng.weChatSpider.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhanglr on 2016/7/12.
 */
public class HttpUtils {
//    private static Logger log = Logger.getLogger(HttpUtils.class);
    public static HttpResult httpPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String resultStr = "";
        HttpResult result = new HttpResult();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                resultStr += line;
            }
            result.setBody(resultStr.toString());
            result.setStatusCode(conn.getResponseCode());
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
//            log.error(e);
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }

        return result;
    }
    public static HttpResult httpGet(String url) throws Exception {
        try {
            URL getUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String lines;
            while ((lines = reader.readLine()) != null) {
                sb.append(lines);
            }
            reader.close();

            connection.disconnect();
            HttpResult result = new HttpResult();
            result.setBody(sb.toString());
            result.setStatusCode(connection.getResponseCode());
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    private static String getFileType(String contentType) {
//        switch (contentType) {
//            case "vedio/mp4":
//                return ".mp4";
//            case "vedio/flv":
//                return ".flv";
//            case "image/jpeg":
//                return ".jpg";
//            default:
//                return ".mp4";
        return "."+contentType.split("/")[1];
    }

    public static HttpResult download(String downloadUrl, String destPath) throws Exception {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            URL url = new URL(downloadUrl);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setConnectTimeout(60 * 1000);
            con.setReadTimeout(60 * 1000);
            con.setRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");
            inputStream = con.getInputStream();

            byte[] buffer = new byte[1024 * 1024];
            String filtType = getFileType(con.getContentType());
            String fullName = destPath + filtType;
            File file = new File(fullName);
            outputStream = new FileOutputStream(file);
            int num = 0;
            while ((num = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, num);
            }
            outputStream.flush();
            con.disconnect();
            HttpResult result = new HttpResult();
            result.setBody(fullName);
            result.setStatusCode(con.getResponseCode());
            return result;
        } catch (MalformedURLException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
            }
        }
    }
}
