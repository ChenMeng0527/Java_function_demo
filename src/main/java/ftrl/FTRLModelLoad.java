package ftrl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FTRLModelLoad {
    public double[] n;
    public double[] z;
    public Map<Integer,Double> w;
    public Map<Integer,Double> loadModel(String filePath) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = null;
//       文件保存了三行，分别是梯度方n,参数z,以及w
        String[][] Str = new String[3][];
        int i = 0;
        while ((line = br.readLine()) != null){
            Str[i] = line.split(" ");
            i++;
        }
        n = new double[Str[0].length];
        z = new double[Str[0].length];
        w = new HashMap<Integer,Double>();
        for(int j=0;j<Str[0].length;j++){
            n[j] = Double.valueOf(Str[0][j]);
            z[j] = Double.valueOf(Str[1][j]);
            w.put(j,Double.valueOf(Str[2][j]));
        }
        return w;
    }
//    预测函数
//    拿到参数进行预测,输入数据与参数w
    public double predict_(double[] x_,Map<Integer,Double> w){
        TreeMap<Integer,Double> x = new TreeMap<Integer, Double>();
        for(int i=0;i<x_.length;i++){
            x.put(i,x_[i]);
        }
        double decisionValue = 0.0;
        for(Map.Entry<Integer,Double> e:x.entrySet()){ //key为输入数据的维度，value为相应的值
            decisionValue += e.getValue()*w.get(e.getKey());
        }
        decisionValue = Math.max(Math.min(decisionValue,35.0),-35.0);//将最终的值进行限制范围
        return 1.0/(1.0+Math.exp(-decisionValue));
    }
}
