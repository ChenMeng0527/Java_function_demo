package pmml;

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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Pmml_ctr_ThreadPool {

    // 读取pmml文件,返回预测evaluator
    private Evaluator loadPmml(String Path){
        PMML pmml = new PMML();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(Path);
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

    public double predict(Evaluator evaluator, Map<Integer, Double> feature_map) {

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

        List<InputField> inputFields = evaluator.getInputFields();
        //过模型的原始特征，从画像中获取数据，作为模型输入
        Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
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
        ProbabilityDistribution targetFieldValue = (ProbabilityDistribution) results.get(targetFieldName);
//        ValueMap values = targetFieldValue.getValues();
//        Object xx = values.get("1");
//        System.out.println(values.get("1"));
//        System.out.println("target: " + targetFieldName.getValue() + " value: " + targetFieldValue);
//        System.out.println(targetFieldValue);
        int primitiveValue = -1;
        double pro = 0;
        if (targetFieldValue instanceof Computable) {
            Computable computable = (Computable) targetFieldValue;
            primitiveValue = (Integer)computable.getResult();
            pro = targetFieldValue.getProbability("1");
        }
        return pro;
    }


    public static void main(String args[]){
        String path = "D:\\Python_Worksapce\\youshu_datamining\\recommendsystem\\main\\rec_scene\\rec_model_xgb\\code\\ctr_pmml_070320.pmml";
        Pmml_ctr_ThreadPool demo = new Pmml_ctr_ThreadPool();
        Evaluator model = demo.loadPmml(path);

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(4,
                                                                8,
                                                                3,
                                                                TimeUnit.SECONDS,
                                                                new ArrayBlockingQueue<Runnable>(1000),
                                                                new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i=0;i<=10;i++){
            System.out.println(i);
            //读取本地文件数据
            String data_path = "D:\\Python_Worksapce\\youshu_datamining\\recommendsystem\\main\\rec_scene\\rec_model_xgb\\data\\rec_sampla_0615-0628_libsvm.txt";
            try (FileReader reader = new FileReader(data_path);
                 BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
            ) {
                String line;
                int a = 0;
                long startTime=System.currentTimeMillis();
                while ((line = br.readLine()) != null && a<100) {
                    a+=1;

                    try {
                        Map<Integer,Double> feature = new HashMap<>();
                        String[] arr = line.split("\t");
                        for (int j=1;j<arr.length;j++){
                            Integer index = Integer.valueOf(arr[j].split(":")[0]);
                            Double value = Double.valueOf(arr[j].split(":")[1]);
                            feature.put(index,value);
                        }
                        //产生一个任务，并将其加入到线程池
                        threadPool.execute(new ThreadPoolTask_(model, feature,demo));
//                        ThreadPoolTask_ tpt = new ThreadPoolTask_(model, feature,demo);
//                        threadPool.execute(tpt);
//                        double result = tpt.getTask();
//                        System.out.println(result);

                      } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                long endTime=System.currentTimeMillis();
                System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class ThreadPoolTask_ implements Runnable, Serializable {

    private Evaluator evaluator;
    private Map<Integer,Double> feature_map;
    private Pmml_ctr_ThreadPool demo;
    private double returnsocre;

    ThreadPoolTask_(Evaluator evaluator, Map<Integer,Double> feature_map, Pmml_ctr_ThreadPool demo){
        this.evaluator = evaluator;
        this.feature_map = feature_map;
        this.demo = demo;
    }

//    private Pmml_ctr_ThreadPool demo = new Pmml_ctr_ThreadPool();

    public void run(){
//        System.out.println(Thread.currentThread().getName());
        this.returnsocre = demo.predict(evaluator,feature_map);
//        System.out.println(this.returnsocre);
    }
    public double getTask(){
        return this.returnsocre;
    }
}