package com.shark.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnowFlake {
    private static Logger logger = LoggerFactory.getLogger(SnowFlake.class);

    //机器ID
    private long workerId;
    //机房ID
    private long datacenterId;
    //一毫秒内生成的多个id的最新序号
    private long sequence;

    private long twepoch = 1288834974657L;
    private long workerIdBits = 5L;
    private long datacenterIdBits = 5L;

    //这个是二进制运算，就是5 bit最多只能有31个数字，也就是说机器id最多只能是32以内
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);
    //这个是一个意思，就是5 bit最多只能有31个数字，机房id最多只能是32以内
    private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

    private long sequenceBits = 12L;
    private long workerIdShift = sequenceBits;
    private long datacenterIdShift = sequenceBits + workerIdBits;
    private long timestampLeftShift = sequenceBits + workerIdBits + datacenterIdBits;
    private long sequenceMask = -1L ^ (-1L << sequenceBits);
    private long lastTimestamp = -1L;

    public SnowFlake(long workerId, long datacenterId, long sequence) {
        //要求传递进来的机房id和机器id不能超过32，不能小于0，5bit
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID can't be greater than %d or less than 0", maxWorkerId));
        }
        if (datacenterId > maxDatacenterId || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("datacenter Id can't be greater than %d or less than 0",maxDatacenterId));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.sequence = sequence;
    }

    /**
     * 生成唯一ID
     * @return
     */
    public synchronized long nextId() {
        long timestamp = getTimestamp();
        //在同一个毫秒内，又发送了一个请求生成一个id，把seqence序号给递增1，最多就是4096
        if (lastTimestamp == timestamp) {
            //sequence递增，最多到4096
            sequence = (sequence + 1) & sequenceMask;
            //超过了4096
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        // 先将当前时间戳左移，放到41 bit；将机房id左移放到5 bit；将机器id左移放到5 bit；将序号放最后12 bit
        return ((timestamp - twepoch) << timestampLeftShift) |
                (datacenterId << datacenterIdShift) |
                (workerId << workerIdShift) | sequence;
    }

    public long getWorkerId(){
        return workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = getTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getTimestamp();
        }
        return timestamp;
    }

    public static void main(String[] args) {

        SnowFlake worker = new SnowFlake(1,1,1);

        for (int i = 0; i < 30; i++) {
            logger.info("Get snow flake id[{}].", worker.nextId());
        }
    }
}