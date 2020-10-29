package ftrl;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FTRLProximal {
    private FTRLparameters parameters;
    public double[] z ; // 参数
    public double[] n ; //  梯度方
    public Map<Integer,Double> w; //变动的w参数

//    public double[] n_;
//    public double[] z_;
//    public Map<Integer, Double> w_;

    public FTRLProximal(FTRLparameters parameters){
        this.parameters = parameters;
        this.z = new double[parameters.dataDimensions];
        this.n = new double[parameters.dataDimensions];
        this.w = null;
    }

//  函数目的：伪代码中的第一块，对w进行求解,并对单个样本进行预测，得到p值然后运用到第二阶段进行更新
    public double predict(Map<Integer,Double> x){
        w = new HashMap<Integer,Double>();
        double decisionValue = 0.0;
        for(Map.Entry<Integer,Double> e:x.entrySet()){
            double weigth = 0.0;
            Integer Index = e.getKey();
            Double value = e.getValue();
            if(sign(z[Index])<parameters.L1_lambda){
                w.put(Index,weigth);
            }
            else {
                weigth = (sign(z[Index])*parameters.L1_lambda-z[Index])/
                        (parameters.L2_lambda+(parameters.beta+Math.sqrt(n[Index]))/parameters.alpha);
                w.put(Index,weigth);
            }
            decisionValue += value*weigth;
        }
        decisionValue = Math.max(Math.min(decisionValue,35.0),-35.0);
        return 1.0/(1.0 + Math.exp(decisionValue));
    }


//  函数目的：伪代码中的第二块，更新zi与gi；输入每个样本的每一位特征，计算gi,ai
    public void updateModel(Map<Integer,Double>x,double p,double y){
        for(Map.Entry<Integer,Double> e:x.entrySet()){
            double grad = (p-y)*e.getValue();
            //下面这个是不是有问题？？？
            double sigma = (Math.sqrt(grad*grad+n[e.getKey()])-Math.sqrt(n[e.getKey()]))/parameters.alpha;
            z[e.getKey()] += grad-sigma*w.get(e.getKey());
            n[e.getKey()] += grad*grad;
        }
    }


//  函数目的:将求出的n,z,w进行文件保存三列，每个字符中间为‘ ’进行隔开
    public void saveModel(String filePath) throws IOException {
        StringBuilder n_ = new StringBuilder(String.valueOf(n[0]));
        StringBuilder z_ = new StringBuilder(String.valueOf(z[0]));
        StringBuilder w_ = new StringBuilder(String.valueOf(w.get(0)));
        for(int i=0;i<n.length;i++){
            n_.append(" ").append(String.valueOf(n[i]));
            z_.append(" ").append(String.valueOf(z[i]));
            w_.append(" ").append(String.valueOf(w.get(i)));
        }
        try {
            File file = new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(n_+"\r\n");
            bufferedWriter.write(z_+"\r\n");
            bufferedWriter.write(w_+"\r\n");
            bufferedWriter.close();
            System.out.println("参数更新完成");
        }
         catch (IOException e){
            System.out.println("参数更新失败");
            e.printStackTrace();
         }
    }

//  函数目的:直接读取保存的参数文件中的参数；n,z,w
    public void loadModel(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line = null;
        String[][] Str = new String[3][];
        int i = 0;
        while((line = br.readLine()) != null) {
            Str[i] = line.split(" ");
            i++;
        }
        n = new double[n.length];
        z = new double[z.length];
        w = new HashMap<Integer, Double>();
        for(int j=0;j<n.length;j++){
            n[j] = Double.valueOf(Str[0][j]);
            z[j] = Double.valueOf(Str[1][j]);
            w.put(j,Double.valueOf(Str[2][j]));
        }
    }

//   函数目的:直接拿去x进行，根据参数进行sigmord概率输出
    public double predict_(Map<Integer, Double> x) {
        double decisionValue = 0.0;
        for (Map.Entry<Integer, Double> e : x.entrySet()) {
            decisionValue += e.getValue() * w.get(e.getKey());
        }
        decisionValue = Math.max(Math.min(decisionValue, 35.), -35.);
        return 1. / (1. + Math.exp(-decisionValue));
    }

//  函数目的:对数据x进行sign变换
    public double sign(double x){
        if(x>0){
            return 1.0;
        }
        else if(x==0){
            return 0.0;
        }
        else {
            return -1.0;
        }
    }
}