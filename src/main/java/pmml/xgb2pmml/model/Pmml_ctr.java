package pmml.xgb2pmml.model;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Pmml_ctr {

    // 读取pmml文件,返回预测evaluator
    private Evaluator loadPmml(){
        PMML pmml = new PMML();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/Users/youshu_/Java_Workspace/Java_function_demo/src/main/java/pmml/file/model/ctr_pmml_070320.pmml");
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

    private double predict(Evaluator evaluator, Map<Integer,Double> feature_map) {

        // 先进行初始化
        Map<String,Double> data = new HashMap<>();
//        for (int i=0;i<=150;i++){
//            String name = 'x'+String.valueOf(i);
//            data.put(name,0.0);
//        }

        // 改变对应的值
        for(Map.Entry<Integer,Double> entry:feature_map.entrySet()){
            Integer index_ = entry.getKey();
            String index_x = 'x'+String.valueOf(index_);
            Double value_ = entry.getValue();
            data.put(index_x,value_);
        }
//        System.out.println("2:"+startTime_2);
//        System.out.println("输入数据为：");
//        System.out.println(data);
        List<InputField> inputFields = evaluator.getInputFields();
//        System.out.println("输入数据1为：");
//        System.out.println(inputFields);
        //过模型的原始特征，从画像中获取数据，作为模型输入
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
        for (InputField inputField : inputFields) {
            FieldName inputFieldName = inputField.getName();
            Object rawValue = data.get(inputFieldName.getValue());
            FieldValue inputFieldValue = inputField.prepare(rawValue);
            arguments.put(inputFieldName, inputFieldValue);
//            System.out.println("输入模型数据：");
//            System.out.println(arguments);
        }

        Map<FieldName, ?> results = evaluator.evaluate(arguments);
        List<TargetField> targetFields = evaluator.getTargetFields();

        TargetField targetField = targetFields.get(0);
        FieldName targetFieldName = targetField.getName();

        ProbabilityDistribution targetFieldValue = (ProbabilityDistribution) results.get(targetFieldName);
//        ValueMap values = targetFieldValue.getValues();
//        Object xx = values.get("1");
//        System.out.println(values.get("1"));
//        System.out.println("target: " + targetFieldName.getValue() + " value: " + targetFieldValue);
//        System.out.println(targetFieldValue);
        int primitiveValue = -1;
        double probability = 0;
        if (targetFieldValue instanceof Computable) {
            Computable computable = (Computable) targetFieldValue;
//            输出的01结果
            primitiveValue = (Integer)computable.getResult();
//            输出概率
              probability = targetFieldValue.getProbability("1");
//            System.out.println(targetFieldValue.getProbability("1"));
        }

        return probability;
    }


    public static void main(String args[]){
        Pmml_ctr demo = new Pmml_ctr();
        Evaluator model = demo.loadPmml();
        for (int i=0;i<=10;i++){
            //读取本地文件数据
            String data_path = "D:\\Python_Worksapce\\youshu_datamining\\recommendsystem\\main\\rec_scene\\rec_model_xgb\\data\\rec_sampla_0615-0628_libsvm.txt";
//            String data_path = "D:/bbb.txt";
            try (FileReader reader = new FileReader(data_path);
                 BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
            ) {
                String line;

                int a = 0;
                //网友推荐更加简洁的写法
                long startTime=System.currentTimeMillis();
                while ((line = br.readLine()) != null && a<100) {
                    // 一次读入一行数据
                    a+=1;
//                    System.out.println(a);
                    Map<Integer,Double> feature = new HashMap<>();
                    String[] arr = line.split("\t");
                    for (int j=1;j<arr.length;j++){
                        Integer index = Integer.valueOf(arr[j].split(":")[0]);
                        Double value = Double.valueOf(arr[j].split(":")[1]);
                        feature.put(index,value);
                    }
//                    long startTime=System.currentTimeMillis();
//                    System.out.println(startTime);
                    double result = demo.predict(model,feature);
//                    System.out.println(result);
//                    long endTime=System.currentTimeMillis();
//                    System.out.println(endTime);
//                    System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
                }
                long endTime=System.currentTimeMillis();
                System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
