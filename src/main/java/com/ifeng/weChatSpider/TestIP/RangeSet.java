package com.ifeng.weChatSpider.TestIP;

import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.*;


/**
 * 一个“多范围”判断的工具类。可判断一个值是否在一个范围内。
 * 一个“范围”是一个二元组集合：(Start, End)*
 * 主要使用add(Object)和 inRanges(Comparable)方法
 * 
 * @author jinmy
 */
public class RangeSet extends TreeSet{

    private static final long serialVersionUID = 3618141134669362486L;

    /**
     * 内嵌类 表示一个范围，以及范围对应的param param为查询对应的目标，用于扩展应用
     */
    public static class Range implements Serializable, Comparable {
        
		private static final long serialVersionUID = -3669167389519134774L;
		
		private Comparable start;
        private Comparable end;
        private Object param;
        
        public Range(Comparable start, Comparable end, Object param) {
            this.start = start;
            this.end = end;
            this.param = param;
        }
        
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Range) {
                Range other = (Range)obj;
                return ObjectUtils.equals(this.start, other.start)
                        && ObjectUtils.equals(this.end, other.end)
                        && ObjectUtils.equals(this.param, other.param);
            }
            return false;
        }
        
        public Comparable getEnd() {
            return this.end;
        }
        public Object getParam() {
            return this.param;
        }
        public Comparable getStart() {
            return this.start;
        }

        public void setStart(Comparable start) {
            this.start = start;
        }

        public void setEnd(Comparable end) {
            this.end = end;
        }

        public void setParam(Object param) {
            this.param = param;
        }

        public int hashCode() {
            return this.start.hashCode() * 17 + this.end.hashCode();
        }
        
        public String toString() {
            return "Start:" + start + " End:" + end + " Param:" + param;
        }

        @Override
        public int compareTo(Object o) {
            Range temp = (Range) o;
            return this.getStart().compareTo(temp.getStart());
        }
    }
    
    /**
     * 点的排序集合，包括所有的起始点和中止点，类型为Range中的start/end对应的类型
     */
    private List points = new ArrayList();
    
    /**
     * 对应points的每个点(下标相同)，start与这个点相等的所有的Range。
     * list的成员类型为一个嵌套List，值为Range 
     */
    private List startRanges = new ArrayList();

    /**
     * 对应points的每个点(下标相同)，end与这个点相等的所有的Range。
     * list的成员类型为一个嵌套List，值为Range 
     */
    private List endRanges = new ArrayList();
    
    /**
     * 对应points的每个点(下标相同)，记录每个点上有效的range的列表。
     * 每个点上，包括这个点上结束的range，而不包括开始的range
     * list的成员类型为一个嵌套List，值为Range
     * 如果是null，则表示需要调用update来重新生成它
     */
    private List[] activeRangesIndex;
    
    public void updateIndex() {
        int size = this.points.size();
        this.activeRangesIndex = new List[size];
        List currentRanges = new ArrayList();
        for (int i = 0; i < size; i++) {
            // 每个点上，包括这个点上结束的range，而不包括开始的range
            this.activeRangesIndex[i] = new ArrayList(currentRanges);
            currentRanges.addAll((List)this.startRanges.get(i));
            currentRanges.removeAll((List)this.endRanges.get(i));
        }
    }

    public boolean addOne(Object o){
        if (!(o instanceof Range)) {
            throw new IllegalArgumentException("only accepts Range arguments");
        }
        Range newRange = (Range)o;
        if (newRange.start.compareTo(newRange.end) > 0) {
            throw new IllegalArgumentException("start must <= end");
        }
//        if (!super.add(o)) {
//            // 已经存在了，不处理
//            return false;
//        }
        boolean comb = false;
        List<RangeSet.Range> startPreList = inRanges(((IpV4Address) newRange.getStart()).getPre());
        List<RangeSet.Range> endPreList = inRanges(((IpV4Address) newRange.getEnd()).getNext());
        if (startPreList.size() > 0) {
            RangeSet.Range temp = startPreList.get(0);
            if (temp.getParam().equals(newRange.getParam())) {
                remove(temp);
                temp.setEnd(newRange.getEnd());
                add(temp);
                newRange = temp;
                comb = true;
            }
        }
        if (endPreList.size() > 0) {
            RangeSet.Range temp = endPreList.get(0);
            if (temp.getParam().equals(newRange.getParam())) {
                remove(temp);
                temp.setEnd(newRange.getStart());
                add(temp);
                newRange = temp;
                comb = true;
            }
        }
        List<RangeSet.Range> startList = inRanges(newRange.getStart());
        List<RangeSet.Range> endList = inRanges(newRange.getEnd());
        if (startList.size() == 1 && endList.size() == 1) {
            RangeSet.Range startRange = startList.get(0);
            RangeSet.Range endRange = endList.get(0);
            if(startRange.equals(newRange) || endRange.equals(newRange)){
                return true;
            }
            if (startRange.equals(endRange)) {
                RangeSet.Range temp = new RangeSet.Range(((IpV4Address) newRange.getEnd()).getNext(), startRange.getEnd(), startRange.getParam());
                startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
                add(newRange);
                add(temp);
            } else {
                IpV4Address startTemp = (IpV4Address) ((IpV4Address) startRange.getEnd()).getNext();
                IpV4Address endTemp = (IpV4Address) ((IpV4Address) endRange.getStart()).getPre();
                for (IpV4Address i = startTemp; i.compareTo(endTemp) < 0; i = (IpV4Address) i.getNext()) {
                    removeAll(inRanges(i));
                }
                startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
                endRange.setStart(((IpV4Address) newRange.getEnd()).getNext());
                add(newRange);
            }
        } else if (startList.size() == 1) {
            RangeSet.Range startRange = startList.get(0);
            if (startRange.equals(newRange)) {
                return true;
            }
            startRange.setEnd(((IpV4Address) newRange.getStart()).getPre());
            add(newRange);
        } else if (endList.size() == 1) {
            RangeSet.Range endRange = endList.get(0);
            if (endRange.equals(newRange)) {
                return true;
            }
            endRange.setStart(((IpV4Address) newRange.getEnd()).getNext());
            add(newRange);
        } else if (!comb) {
            add(newRange);
        }
        return true;
    }

    /**
     * 添加一个范围。
     * @param o 必须是一个Range
     * @return 如果这个Range对象已经存在，则返回false，否则返回true
     */
    public boolean add(Object o) {
        if (!(o instanceof Range)) {
            throw new IllegalArgumentException("only accepts Range arguments");
        }
        Range range = (Range)o;
        if (range.start.compareTo(range.end) > 0) {
            throw new IllegalArgumentException("start must <= end");
        }
        if (!super.add(o)) {
            // 已经存在了，不处理
            return false;
        }
        
        this.activeRangesIndex = null;
        int startPos = Collections.binarySearch(this.points, range.start);
        if (startPos >= 0) {
            ((List)this.startRanges.get(startPos)).add(range);
        } else {
            startPos = -startPos - 1;
            this.points.add(startPos, range.start);
            List newRanges = new ArrayList();
            newRanges.add(range);
            this.startRanges.add(startPos, newRanges);
            // 同时在endRanges中增加一个空位，以保证与points下标相同
            this.endRanges.add(startPos, new ArrayList());
        }
        
        int endPos = Collections.binarySearch(this.points, range.end);
        if (endPos >= 0) {
            ((List)this.endRanges.get(endPos)).add(range);
        } else {
            endPos = -endPos - 1;
            this.points.add(endPos, range.end);
            List newRanges = new ArrayList();
            newRanges.add(range);
            this.endRanges.add(endPos, newRanges);
            // 同时在startRanges中增加一个空位，以保证与points下标相同
            this.startRanges.add(endPos, new ArrayList());
        }
        return true;
    }
    
    /**
     * 删除一个范围。
     */
    public boolean remove(Object o) {
        if (!super.remove(o)) {
            // 不存在
            return false;
        }
        
        Range range = (Range)o;
        // make dirty, update required
        this.activeRangesIndex = null;
        int startPos = Collections.binarySearch(this.points, range.start);
        if (startPos >= 0) {
            ((List)this.startRanges.get(startPos)).remove(range);
        } else {
            throw new IllegalArgumentException("Unexpected");
        }
        
        int endPos = Collections.binarySearch(this.points, range.end);
        if (endPos >= 0) {
            ((List)this.endRanges.get(endPos)).remove(range);
        } else {
            throw new IllegalArgumentException("Unexpected");
        }
        return true;
    }
    
    public void clear() {
        super.clear();
        this.points.clear();
        this.activeRangesIndex = null;
    }
    
    public Object clone() {
        super.clone();
        throw new UnsupportedOperationException("to be implemented");
    }
    
    /**
     * 查找一个对象所在的所有的Range
     * @return 返回所在的所有的Range
     */
    public List inRanges(Comparable o) {
        if (this.activeRangesIndex == null) {
            updateIndex();
        }
        List result = new ArrayList();
        int pos = Collections.binarySearch(this.points, o);
        if (pos >= 0) {
            result.addAll(this.activeRangesIndex[pos]);
            // 每个点上，包括这个点上结束的range，而不包括开始的range，把开始的range加上
            result.addAll((List)this.startRanges.get(pos));
        } else {
            pos = -pos - 1;
            if (pos < this.activeRangesIndex.length) {
                result.addAll(this.activeRangesIndex[pos]);
            } // 否则超出范围，有效range是空的
        }
        return result;
    }

    public List[] getActiveRangesIndex() {
        return activeRangesIndex;
    }
}