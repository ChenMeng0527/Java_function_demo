package sampling;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author chenm
 * @date 2020/12/03
 */
public class WeightSample {
    /**
     * 有权重采样：随机数的权重分之1方
     * @param samples 加入运营的数据
     * @param yunying 运营数据
     * @param yuan 原始数据
     * @return
     */
    public static List wSample(HashMap<String,Double> samples,
                             HashMap<String,Double> yunying,
                             ArrayList<String> yuan){
        HashMap<String,Double> result = new HashMap<>(256);
        for(String goodsTypeId:samples.keySet()){
            Double w = samples.get(goodsTypeId);
            Double u = Math.random();
            Double k = Math.pow(u,1/w);
            result.put(goodsTypeId,k);
        }
        List<Entry<String,Double>> list = new ArrayList<>(result.entrySet());
        System.out.println(list);
        //然后通过比较器来实现排序，按照得分排序
        Collections.sort(list,new Comparator<Entry<String,Double>>() {
            //降序排序
            @Override
            public int compare(Entry<String, Double> o1,
                               Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        System.out.println(list);
        // 将运营配置数据插入原队列中，防止打乱原顺序
        for(int i=0;i<list.size();i++){
            // 遍历最终生成权重的列表
            String goodsId = list.get(i).getKey();
            // 如果这个是运营的物品，就直接插入原推荐队列中
            if(yunying.containsKey(goodsId)){
                yuan.add(i, goodsId);
            }
        }
        return yuan;
        }

    public static void main(String[] args) {
        // 1:从推荐拿到的数据
        String[] yuanTuijianList = new String[]{"1","2","3","4","5","6","7","8","9","10"};

        // 2:从运营拿到的数据及权重
        HashMap<String, Double> yunyingData = new HashMap<>(256);
        yunyingData.put("a", 1.);
        yunyingData.put("b", 2.);
        yunyingData.put("c", 3.);

        // 3:对order进行转化权重
        int yunyingLen = yunyingData.size();
        HashMap<String, Double> yunyingTransData = new HashMap<>(256);
        for(String key:yunyingData.keySet()){
            yunyingTransData.put(key, (yunyingLen+1-yunyingData.get(key))*10);
        }
        // {a=30.0, b=20.0, c=10.0}
        System.out.println(yunyingTransData);

        // 4:将运营数据跟推荐数据进行合并
        HashMap<String,Double> allData = new HashMap<>(yunyingTransData);
        for(String i:yuanTuijianList){
            allData.put(i, 1.);
        }
        System.out.println(allData);

        // 5:调用函数生成最终的推荐列表
        for(int i=0;i<1;i++){
            System.out.println(wSample(allData,
                                     yunyingTransData,
                                     new ArrayList<>(Arrays.asList(yuanTuijianList))));
        }
    }
}
