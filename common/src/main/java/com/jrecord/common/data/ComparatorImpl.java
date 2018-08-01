package com.jrecord.common.data;

import java.util.Comparator;

/**
 * 默认比较器，当存储List集合中的String类型数据时，没有指定比较器，就使用默认比较器
 */
public class ComparatorImpl implements Comparator<String> {

    @Override
    public int compare(String lhs, String rhs) {
        return lhs.compareTo(rhs);
    }
}