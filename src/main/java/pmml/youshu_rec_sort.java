package pmml;// 推荐接 xgboost模型,线上特征提取模块
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import redis.clients.jedis.Jedis;


public class youshu_rec_sort {

    private Evaluator loadPmml(){
        PMML pmml = new PMML();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("D:\\Java_Workspace\\function_demo\\src\\main\\java\\pmml\\file\\model\\ctr_pmml_070320.pmml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(inputStream == null){
            return null;
        }
        InputStream is = inputStream;
        try {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
        } catch (SAXException e1) {
            e1.printStackTrace();
        } catch (JAXBException e1) {
            e1.printStackTrace();
        }finally {
            //关闭输入流
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        Evaluator evaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
        pmml = null;
        return evaluator;
    }

    private Object predict(Evaluator evaluator, Map<String,Double> data) {
        List<InputField> inputFields = evaluator.getInputFields();
        //过模型的原始特征，从画像中获取数据，作为模型输入
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<FieldName, FieldValue>();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName.getValue());
            FieldValue inputFieldValue = inputField.prepare(rawValue);
            arguments.put(inputFieldName, inputFieldValue);
        }
        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        List<TargetField> targetFields = evaluator.getTargetFields();
        TargetField targetField = targetFields.get(0);
        FieldName targetFieldName = targetField.getName();
        ProbabilityDistribution targetFieldValue =(ProbabilityDistribution) results.get(targetFieldName);
        ValueMap values = targetFieldValue.getValues();
        Object pro = values.get("1");
        return pro;
    }

    public static Map<String,Double> trans_String_2_feature_value(String feature_index_value){

        // 将"104: 4.0, 105: 0.0, 106: 0.0, 107: 0.0, 108: 0.0, 109: 0.0"转为dict格式
        String[] sig_ = feature_index_value.split(",");
        Map<String,Double> output_ = new HashMap<>();
        for (String a:sig_){
            String[] bb = a.split(":");
            String feature_id = bb[0];
            String feature_value = bb[1];
            output_.put(feature_id,Double.valueOf(feature_value));
        }
        return output_;
    }

    public static Map<String,Double> get_feature(Jedis jedis, String user_id, String user_sex,String item_id){
    // ------------------------------从redis中获取特征数据并保存------------------------------

        // 1:redis中获取用户特征 redis_key为：feature_user:6821183

        String redis_user_feature = "rec_sort_user_feature";
        Map<String,Double> user_feature_ = null;
        try {
            String user_feature = jedis.hget(redis_user_feature,user_id);
//            System.out.println("获取用户特征");
//            System.out.println(user_feature);
            user_feature_ = trans_String_2_feature_value(user_feature);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // 2:redis中获取物品特征 redis_key为：
        String redis_item_feature = "rec_sort_item_feature";
        Map item_feature_ = null;
        try {
            String item_feature = jedis.hget(redis_item_feature,item_id);
//            System.out.println("获取物品特征");
//            System.out.println(item_feature);
            item_feature_ = trans_String_2_feature_value(item_feature);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //  3:redis中获取交互特征 redis_key为：       27128514-audio_book:647
        String redis_user_item_feature = "rec_sort_user_item_feature";
        Map user_item_feature_ = null;
        try {
            String user_item = user_id+"-"+item_id;
            String user_item_feature = jedis.hget(redis_user_item_feature,user_item);
//            System.out.println("获取交互特征");
//            System.out.println(user_item_feature);
            user_item_feature_ = trans_String_2_feature_value(user_item_feature);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //  4:redis中获取属性物品交互特征 redis_key为：    sex:1-audio_book:2018
        String redis_item_attr_feature = "rec_sort_item_userattr_feature";
        Map item_attr_feature_ = null;
        try {
            String attr_item = "sex:"+user_sex+"-"+item_id;
            String item_attr_feature = jedis.hget(redis_item_attr_feature,attr_item);
//            System.out.println("获取属性交互特征");
//            System.out.println(item_attr_feature);
            item_attr_feature_ = trans_String_2_feature_value(item_attr_feature);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        //  5:将上述map进行合并
        Map<String, Double> combineResultMap = new HashMap<>();
        if (user_feature_ != null){
            combineResultMap.putAll(user_feature_);
        }
        if (item_feature_ != null){
            combineResultMap.putAll(item_feature_);
        }
        if (user_item_feature_ != null){
            combineResultMap.putAll(user_item_feature_);
        }
        if (item_attr_feature_ != null){
            combineResultMap.putAll(item_attr_feature_);
        }

        Map<String,Double> data = new HashMap<>();
        if (combineResultMap != null){
            for(Map.Entry<String,Double> entry:combineResultMap.entrySet()){
                String index_ = entry.getKey();
                String index_x = 'x'+String.valueOf(index_);
                Double value_ = entry.getValue();
                data.put(index_x,value_);
            }
        }
        return data;
    }


    public static Map get_user_rec_list(String user_id,
                                        String user_sex,
                                        String items,
                                        Jedis jedis,
                                        youshu_rec_sort demo,
                                        Evaluator model){
        // ------------------------main函数：得到用户推荐列表-------------------------

        // 输入：user_id(用户id),user_sex(用户性别),items(物品列表：‘audio_book:23,audio_book:12’)
        String[] items_ = items.split(",");
//        System.out.println(Arrays.toString(items_));
        Map<String,Object> result = new HashMap<>();
        for (String item_id:items_){
//            System.out.println("xxxx");
//            System.out.println(item_id);
            //  对于每个用户及对应的物品,从redis获取特征数据进行拼接

            Map<String,Double> input_feature = get_feature(jedis,user_id,user_sex,item_id);
//            System.out.println("输入的特征:"+input_feature);
            Object pro = demo.predict(model,input_feature);
            result.put(item_id,pro);
        }
        return result;
    }

    public static void main(String[] args) {
        // 0: 链接redis
        Jedis jedis = new Jedis("172.17.33.38",6379);
        jedis.auth("Oneway2015");
        System.out.println("服务正在运行"+jedis.ping());

        // 初始化model
        youshu_rec_sort demo = new youshu_rec_sort();
        Evaluator model = demo.loadPmml();

        String user_id = "6821183";
        String user_sex = "";
        String items = "audio_book:123";
        Map signal_result = get_user_rec_list(user_id, user_sex, items, jedis, demo, model);
        System.out.println(signal_result);
        jedis.close();
    }
}