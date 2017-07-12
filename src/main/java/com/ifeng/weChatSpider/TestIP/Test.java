package com.ifeng.weChatSpider.TestIP;

import java.io.*;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Test.java
 * Created by zhusy on 2017/5/12 0012 14:08
 * Copyright © 2012 Phoenix New Media Limited All Rights Reserved
 */
public class Test {

    public static RangeSet loadRangeSet(String filePath) {
        RangeSet rangeSet = new RangeSet();
        File readFile = new File(filePath);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(readFile));
            String line = br.readLine();
            int count = 0;
            while ((line != null) && (line.trim().length() > 0)) {
                StringTokenizer st = new StringTokenizer(line, "|");
                if (st.countTokens() >= 5) {
                    IpV4Address startIp = new IpV4Address(st.nextToken());
                    IpV4Address endIp = new IpV4Address(st.nextToken());
                    Area area = new Area();
                    area.setNetName(st.nextToken());
                    area.setProvince(st.nextToken());
                    area.setCity(st.nextToken());
                    RangeSet.Range range = new RangeSet.Range(startIp, endIp, area);
                    try {
                        rangeSet.addOne(range);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                line = br.readLine();
                count++;
                System.out.println("size:" + rangeSet.size());
                System.out.println("line:" + count);
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rangeSet;
    }

    public static void write(String filePath, RangeSet rangeSet) {
        File writeFile = new File(filePath);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(writeFile));
            Iterator<RangeSet.Range> iterator = rangeSet.iterator();
            StringBuffer stringBuffer = new StringBuffer();
            while (iterator.hasNext()) {
                RangeSet.Range range = iterator.next();
                String startIp = ((IpV4Address) range.getStart()).toString();
                String endIp = ((IpV4Address) range.getEnd()).toString();
                Area a = (Area) range.getParam();
                String province = a.getProvince();
                String city = a.getCity();
                String net = a.getNetName();
                stringBuffer.append(startIp + "|" + endIp + "|" + net + "|" + province + "|" + city);
                if (iterator.hasNext()) {
                    stringBuffer.append("\n");
                }
            }
            byte bt[];
            bt = stringBuffer.toString().getBytes();
            FileOutputStream outputStream = new FileOutputStream(writeFile);
            outputStream.write(bt, 0, bt.length);
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        /*String filePaht = "F:\\IfengProgram\\weChatSpider\\src\\main\\java\\com\\ifeng\\weChatSpider\\TestIP\\iprangeset.txt";
        String newFilePath = "F:\\IfengProgram\\weChatSpider\\src\\main\\java\\com\\ifeng\\weChatSpider\\TestIP\\new_iprangeset.txt";
        Area area = new Area();
        area.setCity("北京");
        area.setProvince("北京");
        area.setNetName("凤凰");
//        RangeSet.Range newRange = new RangeSet.Range(new IpV4Address("202.110.217.195"), new IpV4Address("202.110.218.45"), area);
        RangeSet loadRangeSet = null;
        try {
            loadRangeSet = loadRangeSet(filePaht);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        RangeSet rangeSet = new RangeSet();
//        Iterator<RangeSet.Range> iterator = loadRangeSet.iterator();
//        while (iterator.hasNext()) {
//            boolean comb = false;
//            RangeSet.Range newRange = iterator.next();
//            rangeSet.addOne(newRange);
//            List<RangeSet.Range> startPreList = rangeSet.inRanges(((IpV4Address) newRange.getStart()).getPre());
//            List<RangeSet.Range> endPreList = rangeSet.inRanges(((IpV4Address) newRange.getEnd()).getNext());
//            if (startPreList.size() > 0) {
//                RangeSet.Range temp = startPreList.get(0);
//                if (temp.getParam().equals(newRange.getParam())) {
//                    rangeSet.remove(temp);
//                    temp.setEnd(newRange.getEnd());
//                    rangeSet.add(temp);
//                    newRange = temp;
//                    comb = true;
//                }
//            }
//            if (endPreList.size() > 0) {
//                RangeSet.Range temp = endPreList.get(0);
//                if (temp.getParam().equals(newRange.getParam())) {
//                    rangeSet.remove(temp);
//                    temp.setEnd(newRange.getStart());
//                    rangeSet.add(temp);
//                    newRange = temp;
//                    comb = true;
//                }
//            }
//            List<RangeSet.Range> startList = rangeSet.inRanges(newRange.getStart());
//            List<RangeSet.Range> endList = rangeSet.inRanges(newRange.getEnd());
//            if (startList.size() == 1 && endList.size() == 1) {
//                RangeSet.Range startRange = startList.get(0);
//                RangeSet.Range endRange = endList.get(0);
//                if(startRange.equals(newRange) || endRange.equals(newRange)){
//                    continue;
//                }
//                if (startRange.equals(endRange)) {
//                    RangeSet.Range temp = new RangeSet.Range(((IpV4Address) newRange.getEnd()).getNext(), startRange.getEnd(), startRange.getParam());
//                    startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
//                    rangeSet.add(newRange);
//                    rangeSet.add(temp);
//                } else {
//                    IpV4Address startTemp = (IpV4Address) ((IpV4Address) startRange.getEnd()).getNext();
//                    IpV4Address endTemp = (IpV4Address) ((IpV4Address) endRange.getStart()).getPre();
//                    for (IpV4Address i = startTemp; i.compareTo(endTemp) < 0; i = (IpV4Address) i.getNext()) {
//                        rangeSet.removeAll(rangeSet.inRanges(i));
//                    }
//                    startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
//                    endRange.setStart(((IpV4Address) newRange.getEnd()).getNext());
//                    rangeSet.add(newRange);
//                }
//            } else if (startList.size() == 1) {
//                RangeSet.Range startRange = startList.get(0);
//                if (startRange.equals(newRange)) {
//                    continue;
//                }
//                startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
//                rangeSet.add(newRange);
//            } else if (endList.size() == 1) {
//                RangeSet.Range endRange = endList.get(0);
//                if (endRange.equals(newRange)) {
//                    continue;
//                }
//                endRange.setStart(((IpV4Address) newRange.getEnd()).getNext());
//                rangeSet.add(newRange);
//            } else if (!comb) {
//                rangeSet.add(newRange);
//            }
//        }

            write(newFilePath, loadRangeSet);*/

        System.out.println(new IpV4Address("202.100.128.127").getNext().getNext());

    }
}
