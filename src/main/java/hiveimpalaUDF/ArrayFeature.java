package hiveimpalaUDF;

import org.apache.hadoop.hive.ql.exec.UDF;
import java.util.ArrayList;

public class ArrayFeature extends UDF{

    // 1:将数组进行前后截取，提取有效行为时间
    public static ArrayList<Integer> monthStatistics(ArrayList<Integer> x){
        int label = 0;
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < x.size(); i++){
            int value = x.get(i);
            if(value !=0){
                if(label == 0){
                    startIndex = i;
                    endIndex = i;
                    label = 1;
                }
                else {
                    endIndex = i;
                }
            }
        }
        ArrayList<Integer> result = new ArrayList<Integer>();
        for(int i = startIndex; i <= endIndex; i++){
            result.add(x.get(i));
        }
        return result;
    }


    //2:求总数
    public static double sumFeature(ArrayList<Integer> x){
        int sum=0;
        int length = x.size();
        for (int i=0; i<length; i++){
            sum += x.get(i);
        }
        return sum;
    }


    //3:求平均数
    public static double meanFeature(ArrayList<Integer> x){
        int sum=0;
        int length = x.size();
        for (int i=0;i<length;i++){
            sum += x.get(i);
        }
        return (double)sum /length;
    }


    //4:求波动性,偏度,峰度
    public static ArrayList bodongSkewKurt(ArrayList<Integer> x){

        //均值
        double meanValue = 0.0;
        for(int i=0; i<x.size(); i++){
            meanValue += x.get(i);
        }
        meanValue = meanValue/x.size();

        double sum = 0.0;
        double sum2 = 0.0;
        double sum3 = 0.0;
        for(int i = 0; i < x.size();i++){
            sum += x.get(i);
            sum2 += Math.pow(x.get(i),2);
            sum3 += Math.pow(x.get(i),3);
        }
        sum = sum/x.size();  //均值
        sum2 = sum2/x.size(); //平方的均值
        sum3 = sum3/x.size(); //三次方的均值

        // 标准差：根号下（平方的均值-均值的平方）
        double sigma = Math.sqrt(sum2 - sum * sum); //这是D(x)的开方，标准差

        //波动性 np.std(x)/(np.mean(x)*np.sqrt(len(x))
        double budong = sigma / (meanValue * Math.sqrt(x.size()));

        double sum4 = 0.0;
        for(int i=0; i < x.size(); i++){
            sum4 += Math.pow(x.get(i) - sum,4);
        }
        sum4 = sum4/x.size();
        // 偏度
        double skew = (sum3 - 3*sum * Math.pow(sigma,2) - Math.pow(sum,3)) / Math.pow(sigma,3);
        // 峰度
        double kurt = sum4 / Math.pow(sigma,2);

        ArrayList<Double> result = new ArrayList<Double>();
        result.add(budong);
        result.add(skew);
        result.add(kurt);
        return result;
    }


    public static ArrayList trans2Array(String x,String y){
        // 将两个字符串进行12月份数组
        String[] aa = x.split(",");
        String[] bb = y.split(",");
        ArrayList<Integer> result = new ArrayList<Integer>();

        for (int i=0; i<12; i++){
            result.add(0);
        }
        for (int i=0; i<aa.length; i++){
            result.set(Integer.valueOf(aa[i].trim())-1,Integer.valueOf(bb[i].trim()));
        }
        return result;
    }



    public static String evaluate(String x, String y){
        //开始整体计算特征值
        // 1:将输入的两个字符串转为原始12个月的数组
        ArrayList<Integer> xx = trans2Array(x,y);
        System.out.println(xx);

        // 2:将数组进行前后截取
        ArrayList<Integer> yy = monthStatistics(xx);
        System.out.println(yy);

        // 3：特征提取
        // 特征1：长度
        int len_ = yy.size();
        // 特征2：总量
        double sum_s = sumFeature(yy);
        // 特征3：平均数
        double mean_ = meanFeature(yy);

        // 特征4：波动性/偏度/峰度
        ArrayList<Double> skew_kurt_value = bodongSkewKurt(yy);
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

    public static void main(String[] args) {
        // x：有行为的月份
        // y: 对应月份的数值
        evaluate("1,5","30,50");

    }
}
