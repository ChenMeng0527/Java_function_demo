package udfhiveimpala;

import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.ArrayList;

public class tongjiFeature extends UDF {

    public static String evaluate(String x,String y){
        //开始整体计算特征值
        // 1:将输入的两个字符串转为原始12个月的数组
        ArrayList<Integer> xx = hiveArrayFeature.trans2Array(x,y);
        System.out.println(xx);
        // 2:将数组进行前后截取
        ArrayList<Integer> yy = hiveArrayFeature.monthStatistics1(xx);
        System.out.println(yy);
        // 3：特征提取
        // 特征1：长度
        int len_ = yy.size();
        // 特征2：总量
        double sum_s = hiveArrayFeature.sum_(yy);
        // 特征3：平均数
        double mean_ = hiveArrayFeature.mean_(yy);
        // 特征4：波动性/偏度/峰度
        ArrayList<Double> skew_kurt_value = hiveArrayFeature.bodongSkewKurt(yy);
        double budong = skew_kurt_value.get(0);
        double skew = skew_kurt_value.get(1);
        double kurt = skew_kurt_value.get(2);

        String result = len_+"|"+
                String.format("%.2f",sum_s)+"|"+
                String.format("%.2f",mean_)+"|"+
                String.format("%.2f",budong)+"|"+
                String.format("%.2f",skew)+"|"+
                String.format("%.2f",kurt);
        System.out.println(result);
        return result;

    }
    public static void main(String[] args){
        evaluate("4, 1, 3, 8, 5, 7, 6","3, 5, 9, 7, 1, 8, 1");
    }
}