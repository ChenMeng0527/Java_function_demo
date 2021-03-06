package bloomFilter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class BloomFilter implements Serializable {
    private static final long serialVersionUID = -5221305273707291280L;
    private final int[] seeds;
    private final int size;
    private final BitSet notebook;
    private final MisjudgmentRate rate;
    private final AtomicInteger useCount = new AtomicInteger(0);
    private final Double autoClearRate;

    /**
     * 默认中等程序的误判率：MisjudgmentRate.MIDDLE 以及不自动清空数据（性能会有少许提升）
     *
     * @param rate
     *            一个枚举类型的误判率
     * @param dataCount
     *            预期处理的数据规模，如预期用于处理1百万数据的查重，这里则填写1000000
     * @param autoClearRate
     *            自动清空过滤器内部信息的使用比率，传null则表示不会自动清理，
     *            当过滤器使用率达到100%时，则无论传入什么数据，都会认为在数据已经存在了
     *            当希望过滤器使用率达到80%时自动清空重新使用，则传入0.8
     */
    public BloomFilter(MisjudgmentRate rate, int dataCount, Double autoClearRate) {
        long bitSize = rate.seeds.length *dataCount;
        System.out.println(Integer.MAX_VALUE);
        System.out.println("bitSize"+bitSize);
        if (bitSize < 0 || bitSize > Integer.MAX_VALUE) {
            throw new RuntimeException("位数太大溢出了，请降低误判率或者降低数据大小");
        }
        this.rate = rate;
        seeds = rate.seeds;
        size = (int) bitSize;
        notebook = new BitSet(size);
        this.autoClearRate = autoClearRate;
    }

    public void add(String data) {
        checkNeedClear();
        for (int i = 0; i < seeds.length; i++) {
            int index = hash(data, seeds[i]);
            setTrue(index);
        }
    }

    public boolean check(String data) {
        for (int i = 0; i < seeds.length; i++) {
            int index = hash(data, seeds[i]);
            if (!notebook.get(index)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 如果不存在就进行记录并返回false，如果存在了就返回true
     *
     * @param data
     * @return
     */
    public boolean addIfNotExist(String data) {
        // 检测是否需要清空过滤器
        checkNeedClear();

        // 生成 seeds.length 个hash函数
        int[] indexs = new int[seeds.length];
        int index;

        // 先假定存在
        boolean exist = true;

        for (int i = 0; i < seeds.length; i++) {
            // 对于每一个hash函数,根据data以及seeds[i]的值去求hash函数值
            indexs[i] = index = hash(data, seeds[i]);

            if (exist) {
                if (!notebook.get(index)) {
                    // 只要有一个不存在，就可以认为整个字符串都是第一次出现的
                    exist = false;
                    // 补充之前的信息
                    for (int j = 0; j <= i; j++) {
                        setTrue(indexs[j]);
                    }
                }
            } else {
                setTrue(index);
            }
        }
        return exist;
    }


    // 如果需要清除，清除notebook和usercount
    private void checkNeedClear() {
        if (autoClearRate != null) {
            if (getUseRate() >= autoClearRate) {
                synchronized (this) {
                    if (getUseRate() >= autoClearRate) {
                        notebook.clear();
                        useCount.set(0);
                    }
                }
            }
        }
    }


    // 将usercount累加，将notebook中位数设置索引ture
    public void setTrue(int index) {
        useCount.incrementAndGet();
        notebook.set(index, true);
        System.out.println(notebook);
    }

    // hash函数操作，对于一个数字，一组hash函数
    private int hash(String data, int seeds) {
        char[] value = data.toCharArray();
        int hash = 0;
        if (value.length > 0) {
            // 对于data中的每个数值,通过运算累加hash的结果
            for (int i = 0; i < value.length; i++) {
                hash = i * hash + value[i];
                System.out.println(value[i]);
            }
        }
        hash = hash * seeds % size;
        // 防止溢出变成负数
        return Math.abs(hash);
    }

    public double getUseRate() {
        // 过滤器的使用率， count次数 / 用户数据*hash个数
        return (double) useCount.intValue() / (double) size;
    }

    public void saveFilterToFile(String path) {
        // 写入文件
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static BloomFilter readFilterFromFile(String path) {
        // 读文件
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            return (BloomFilter) ois.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 清空过滤器中的记录信息
     */
    public void clear() {
        useCount.set(0);
        notebook.clear();
    }

    public MisjudgmentRate getRate() {
        return rate;
    }


    public enum MisjudgmentRate {
        // 这里要选取质数，能很好的降低错误率，分配的位数越多，误判率越低但是越占内存
        /**
         * 每个字符串分配4个位，4个位误判率大概是0.14689159766308
         * 每个字符串分配8个位，8个位误判率大概是0.02157714146322
         * 每个字符串分配16个位，16个位误判率大概是0.00046557303372
         * 每个字符串分配32个位，32个位误判率大概是0.00000021167340
         */
        VERY_SMALL(new int[] { 2, 3, 5, 7 }),
        SMALL(new int[] { 2, 3, 5, 7, 11, 13, 17, 19 }), //
        MIDDLE(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53 }), //
        HIGH(new int[] { 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97,
                101, 103, 107, 109, 113, 127, 131 });

        private int[] seeds;

        private MisjudgmentRate(int[] seeds) {
            this.seeds = seeds;
//            System.out.println(this.seeds);
        }

        public int[] getSeeds() {
            return seeds;
        }

        public void setSeeds(int[] seeds) {
            this.seeds = seeds;
        }

    }

    public static void main(String[] args) {
        BloomFilter fileter = new BloomFilter(MisjudgmentRate.MIDDLE,7,null);
        System.out.println(fileter.addIfNotExist("123"));
//        System.out.println(fileter.addIfNotExist("234"));
//        System.out.println(fileter.addIfNotExist("234"));
//        System.out.println(fileter.addIfNotExist("345"));
//        System.out.println(fileter.addIfNotExist("567"));
//        System.out.println(fileter.addIfNotExist("456"));
//        System.out.println(fileter.addIfNotExist("123"));
//        fileter.saveFilterToFile("C:\\Users\\john\\Desktop\\1111\\11.obj");
//        fileter = readFilterFromFile("C:\\Users\\john\\Desktop\\111\\11.obj");
//        System.out.println(fileter.getUseRate());
//        System.out.println(fileter.addIfNotExist("123"));
    }
}




//测试1：
//此用户第一次浏览了[123,234,345,456,567,678,789]
//此用户第二次浏览了[123,321,322,323,324,325,326]
//
