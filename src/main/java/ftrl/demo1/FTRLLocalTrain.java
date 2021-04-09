package ftrl.demo1;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class FTRLLocalTrain {
    private FTRLProximal learner;
    private LogLossEvalutor evalutor;
    private int printInterval;
    public FTRLLocalTrain(FTRLProximal learner,LogLossEvalutor evalutor,int printInterval){
        this.learner = learner;
        this.evalutor = evalutor;
        this.printInterval = printInterval;
    }

    public void train(String modelPath,double[][] X,double[] Y) throws IOException {
        int trainedNum = 0;
        double totalLoss = 0.0;
        long startTime = System.currentTimeMillis();
        learner.loadModel(modelPath);
        for(int j=0;j<X.length;j++){
            Map<Integer,Double> x = new TreeMap<Integer, Double>();
            for(int i=0;j<X[0].length;i++){
                x.put(i,X[j][i]);
            }
            double y =((int)Y[j] == 1)?1.0:0.0;
            double p = learner.predict(x);
            learner.updateModel(x,p,y);
            double loss = LogLossEvalutor.calLogLoss(p, y);
            totalLoss+=loss;
            trainedNum+=1;
            if(trainedNum%printInterval==0){
                long currentTime = System.currentTimeMillis();
                double minutes = (double) ((currentTime - startTime) / 60000);
                System.out.println(String.valueOf(minutes)+":"+String.valueOf(evalutor.getAverageLogLoss()));
            }
        }
        learner.saveModel(modelPath);
        System.out.println(totalLoss/trainedNum);
    }
}
