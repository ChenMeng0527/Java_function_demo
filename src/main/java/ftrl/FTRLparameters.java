package ftrl;

public class FTRLparameters {
    public double alpha;  //学习速率
    public double beta;  //调整参数
    public double L1_lambda; //L1参数
    public double L2_lambda; //L2参数
    public int dataDimensions;//特征维度
    public int testDataSize;//测试集分次处理每次处理的个数
    public int interval;//每间隔interval进行一次打印
    public String modelPath;//模型训练参数的存放路径
    public FTRLparameters(double alpha,
                          double beta,
                          double L1_lambda,
                          double L2_lambda,
                          int dataDimensions,
                          int testDataSize,
                          int interval,
                          String modelPath){
        this.alpha = alpha;
        this.beta = beta;
        this.L1_lambda = L1_lambda;
        this.L2_lambda = L2_lambda;
        this.dataDimensions = dataDimensions;
        this.testDataSize = testDataSize;
        this.interval = interval;
        this.modelPath = modelPath;
    }
}




