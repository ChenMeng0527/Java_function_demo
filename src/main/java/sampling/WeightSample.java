package sampling;

import java.util.*;
import java.util.Map.Entry;

public class WeightSample {
    public static List a_res(HashMap<String,Double> samples,HashMap<String,Double> yunying,ArrayList<String> yuan){
        HashMap<String,Double> result = new HashMap<>();
        for(String goods_type_id:samples.keySet()){
            Double w = samples.get(goods_type_id);
            Double u = Math.random();
            Double k = Math.pow(u,1/w);
            result.put(goods_type_id,k);
        }
        List<Entry<String,Double>> list = new ArrayList<>(result.entrySet());
        //然后通过比较器来实现排序，按照得分排序
        Collections.sort(list,new Comparator<Entry<String,Double>>() {
            //降序排序
            public int compare(Entry<String, Double> o1,
                               Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        // 将运营配置数据插入原队列中，防止打乱原顺序
        for(int i=0;i<list.size();i++){
            // 遍历最终生成权重的列表
            String goods_id = list.get(i).getKey();
            // 如果这个是运营的物品，就直接插入原推荐队列中
            if(yunying.containsKey(goods_id)){
                yuan.add(i, goods_id);
            }
        }
        return yuan;
        }

    public static void main(String[] args) {
        // 1:从推荐拿到的数据
        String[] yuan_tuijian_list = new String[]{"1","2","3","4","5","6","7","8","9","10"};

        // 2:从运营拿到的数据及权重
        HashMap<String, Double> yunying_data = new HashMap<>();
        yunying_data.put("a", 1.);
        yunying_data.put("b", 2.);
        yunying_data.put("c", 3.);

        // 3:对order进行转化权重
        int yunying_len = yunying_data.size();
        HashMap<String, Double> yunying_trans_data = new HashMap<>();
        for(String key:yunying_data.keySet()){
            yunying_trans_data.put(key, (yunying_len+1-yunying_data.get(key))*10);
        }

        // 4:将运营数据跟推荐数据进行合并
        HashMap<String,Double> all_data = new HashMap<>(yunying_trans_data);
        for(String i:yuan_tuijian_list){
            all_data.put(i, 1.);
        }

        // 5:调用函数生成最终的推荐列表
        for(int i=0;i<10;i++){
            System.out.println(a_res(all_data,yunying_trans_data,new ArrayList<>(Arrays.asList(yuan_tuijian_list))));
        }
    }
}
