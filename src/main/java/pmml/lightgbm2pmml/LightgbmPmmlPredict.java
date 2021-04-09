package pmml.lightgbm2pmml;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LightgbmPmmlPredict {

    public static void main(String[] args) throws Exception {
        String  pathxml="/Users/youshu_/Java_Workspace/Java_function_demo/src/main/java/pmml/lightgbm2pmml/model/lightgbm.pmml";
        Map<String, Double>  map = new HashMap<String, Double>();
        map.put("A", 6.3200e-03);
        map.put("B", 1.8000e+01);
        map.put("C", 2.3100e+00);
        System.out.println(map);
        predictLrHeart(map, pathxml);
    }

    public static void predictLrHeart(Map<String, Double> irismap,String  pathxml) throws Exception {

        PMML pmml;
        // 模型导入
        File file = new File(pathxml);
        InputStream inputStream = new FileInputStream(file);
        try (InputStream is = inputStream) {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);

            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
            ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory.newModelEvaluator(pmml);
            Evaluator evaluator = (Evaluator) modelEvaluator;

            List<InputField> inputFields = evaluator.getInputFields();
            // 过模型的原始特征，从画像中获取数据，作为模型输入
            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
            for (InputField inputField : inputFields) {
                FieldName inputFieldName = inputField.getName();
                Object rawValue = irismap.get(inputFieldName.getValue());
                FieldValue inputFieldValue = inputField.prepare(rawValue);
                arguments.put(inputFieldName, inputFieldValue);
            }

            Map<FieldName, ?> results = evaluator.evaluate(arguments);
            List<TargetField> targetFields = evaluator.getTargetFields();
            //对于分类问题等有多个输出。
            for (TargetField targetField : targetFields) {
                FieldName targetFieldName = targetField.getName();
                Object targetFieldValue = results.get(targetFieldName);
                System.err.println("target: " + targetFieldName.getValue()+ " value: " + targetFieldValue);
            }
        }
    }
}