package tfserving;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class JavaTfservingMthreading {
    /**
     * 多线程处理list
     *
     * @param data  数据list
     * @param threadNum  线程数
     */
    public synchronized void handleList(ArrayList<ArrayList> data,
                                        int threadNum,
                                        PredictionServiceGrpc.PredictionServiceBlockingStub stub,
                                        Predict.PredictRequest.Builder predictRequestBuilder,
                                        TensorShapeProto.Builder tensorShapeBuilder) {
        int length = data.size();
        System.out.println("len:"+length+";"+"threadNum:"+threadNum);
        int tl = length % threadNum == 0 ? length / threadNum : (length / threadNum + 1);
        System.out.println("tl:"+tl);

        for (int i = 0; i < threadNum; i++) {
            int end = (i + 1) * tl;

            HandleThread thread = new HandleThread("线程" + (i + 1),
                                                    data,
                                                   i * tl,
                                                    end > length ? length : end,
                                                    stub,
                                                    predictRequestBuilder,
                                                    tensorShapeBuilder);
            thread.start();
        }
    }


    private static ArrayList<ArrayList> read_data() throws IOException {
        String pathname = "D:\\Java_Workspace\\function_demo\\src\\main\\java\\tf_serving\\train.txt";
        File file = new File(pathname);

        ArrayList<ArrayList> samples = new ArrayList<>();
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] signal_data = line.split(" ");

                    ArrayList<ArrayList> signal_sample = new ArrayList<>();
                    ArrayList<Long> ids = new ArrayList<>();
                    ArrayList<Float> vals = new ArrayList<>();

                    for (int i = 1; i < signal_data.length; i++) {
                        String[] xx = signal_data[i].split(":");
                        ids.add(Long.valueOf(xx[0]));
                        vals.add(Float.valueOf(xx[1]));
                    }
                    signal_sample.add(ids);
                    signal_sample.add(vals);
                    samples.add(signal_sample);
//                    System.out.println(signal_sample);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return samples;
    }

    public static void main(String[] args) throws IOException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress("172.17.36.29", 8500).usePlaintext(true).build();
        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName("deepfm");
        modelSpecBuilder.setSignatureName("");
        predictRequestBuilder.setModelSpec(modelSpecBuilder);
        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(16));


        // 准备数据
        System.out.println("获取数据ing----");
        ArrayList<ArrayList> data  = read_data();
        System.out.println("获取数据完毕");
        System.out.println("数据总数:"+data.size());

        long startTime = System.currentTimeMillis();
        JavaTfservingMthreading test = new JavaTfservingMthreading();
        test.handleList(data, 4, stub, predictRequestBuilder, tensorShapeBuilder);
        long endTime = System.currentTimeMillis();
        System.out.println("程序执行时间:"+(endTime-startTime));
    }
}


class HandleThread extends Thread {
    private String threadName;
    private ArrayList<ArrayList> data;
    private int start;
    private int end;
    private PredictionServiceGrpc.PredictionServiceBlockingStub stub;
    private Predict.PredictRequest.Builder predictRequestBuilder;
    private TensorShapeProto.Builder tensorShapeBuilder;

    public HandleThread(String threadName,
                        ArrayList<ArrayList> data,
                        int start,
                        int end,
                        PredictionServiceGrpc.PredictionServiceBlockingStub stub,
                        Predict.PredictRequest.Builder predictRequestBuilder,
                        TensorShapeProto.Builder tensorShapeBuilder
    ) {
        this.threadName = threadName;
        this.data = data;
        this.start = start;
        this.end = end;
        this.stub = stub;
        this.predictRequestBuilder = predictRequestBuilder;
        this.tensorShapeBuilder = tensorShapeBuilder;
    }

    @Override
    public void run() {
        List subList_ = data.subList(start, end);
        System.out.println(threadName+"处理了"+subList_.size()+"条！");
        for(ArrayList i:data){
            ArrayList<Long> ids_vec_ = (ArrayList<Long>)i.get(0);
            ArrayList<Float> vals_vec_ = (ArrayList<Float>) i.get(1);

            // 设置入参1
            TensorProto.Builder tensorids_ids = TensorProto.newBuilder();
            tensorids_ids.setDtype(DataType.DT_INT64);
            tensorids_ids.setTensorShape(tensorShapeBuilder.build());
            tensorids_ids.addAllInt64Val(ids_vec_);

            // 设置入参2
            TensorProto.Builder tensorids_val = TensorProto.newBuilder();
            tensorids_val.setDtype(DataType.DT_FLOAT);
            tensorids_val.setTensorShape(tensorShapeBuilder.build());
            tensorids_val.addAllFloatVal(vals_vec_);

            Map<String, TensorProto> map=new LinkedHashMap<>();
            map.put("feat_ids", tensorids_ids.build());
            map.put("feat_vals", tensorids_val.build());
            predictRequestBuilder.putAllInputs(map);
            Predict.PredictResponse result = stub.predict(predictRequestBuilder.build());

//                System.out.println(result.getOutputsMap().get("prob").getFloatValList());
        }

    }

}