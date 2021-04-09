package ftrl.demo1;

public class LogLossEvalutor {
    private int testDataSize;
    private double[] logLoss;
    private double totalLoss;
    private int position;
    private boolean enoughData;

    public LogLossEvalutor(int testDataSize){
        this.testDataSize = testDataSize;
        logLoss = new double[testDataSize];
        position = 0;
        totalLoss = 0.0;
    }
    public void addLogLoss(double loss){
        totalLoss = totalLoss + loss - logLoss[position];
        logLoss[position] = loss;
        position+=1;
        if(position>=testDataSize){
            position=0;
            enoughData = true;
        }
    }

    public double getAverageLogLoss() {
        if(enoughData) {
            return totalLoss / testDataSize;
        } else {
            return totalLoss / position;
        }
    }

    public static double calLogLoss(double prob,double y){
        double p = Math.max(Math.min(prob,1-1e-15),1e-15);
        return y == 1.0?-Math.log(p):-Math.log(1.0-p);
    }
}