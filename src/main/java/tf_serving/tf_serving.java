package tf_serving;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.*;
import java.util.*;

import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;

import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import static com.sun.org.apache.xalan.internal.lib.ExsltStrings.split;

public class tf_serving {

    private static Predict.PredictResponse predict(PredictionServiceGrpc.PredictionServiceBlockingStub stub,
                                                   Predict.PredictRequest.Builder predictRequestBuilder,
                                                   TensorShapeProto.Builder tensorShapeBuilder,
                                                   List<Long> ids_vec,
                                                   List<Float> vals_vec){

        // 设置入参1
        TensorProto.Builder tensorids_ids = TensorProto.newBuilder();
        tensorids_ids.setDtype(DataType.DT_INT64);
        tensorids_ids.setTensorShape(tensorShapeBuilder.build());
        tensorids_ids.addAllInt64Val(ids_vec);

        // 设置入参2
        TensorProto.Builder tensorids_val = TensorProto.newBuilder();
        tensorids_val.setDtype(DataType.DT_FLOAT);
        tensorids_val.setTensorShape(tensorShapeBuilder.build());
        tensorids_val.addAllFloatVal(vals_vec);

        Map<String, TensorProto>  map=new LinkedHashMap<>();
        map.put("feat_ids", tensorids_ids.build());
        map.put("feat_vals", tensorids_val.build());
        predictRequestBuilder.putAllInputs(map);

        return stub.predict(predictRequestBuilder.build());

    }

    private static ArrayList<ArrayList> read_data() throws IOException {
        String pathname = "D:\\pmml_java\\src\\main\\java\\TF_serving\\train.txt";
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
        // 这里还是先用block模式
        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
        // 创建请求
        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
        // 模型名称和模型方法名预设
        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName("deepfm");
        modelSpecBuilder.setSignatureName("");
        predictRequestBuilder.setModelSpec(modelSpecBuilder);

        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(16));



        List<Long> ids_vec = Arrays.asList(5L,9L,33L,445897L,446054L,446293L,446358L,491531L,491625L,491633L,491640L,491646L,491655L,491668L,491700L,491708L);
//        List<Integer> ids_vec = Arrays.asList(4,15,34608,445898,446083,446293,449140,490778,491626,491634,491641,491645,491662,491668,491700,491708);
        List<Float> vals_vec = Arrays.asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f);
        System.out.println(ids_vec);

        long startTime=System.currentTimeMillis();
        Predict.PredictResponse result = predict(stub,predictRequestBuilder,tensorShapeBuilder,ids_vec,vals_vec);
        System.out.println(result.getOutputsMap().get("prob").getFloatValList());
        long endTime=System.currentTimeMillis();
        System.out.println("程序执行时间:"+(endTime-startTime)/1000);
        System.out.println(result);





//         获取数据
//        System.out.println("获取数据ing----");
//        ArrayList<ArrayList> xx  = read_data();
//        System.out.println("获取数据完毕");
//        System.out.println("数据总数:"+xx.size());
//
//
//        long startTime=System.currentTimeMillis();
//        System.out.println("开始时间:"+startTime);
//        // 生成样本数据
//        for(ArrayList i:xx){
//            ArrayList<Long> ids_vec_ = (ArrayList<Long>)i.get(0);
//            ArrayList<Float> vals_vec_ = (ArrayList<Float>) i.get(1);
//            Predict.PredictResponse result_ = predict(stub,predictRequestBuilder,tensorShapeBuilder,ids_vec_,vals_vec_);
//            System.out.println(result_.getOutputsMap().get("prob").getFloatValList());
//        }
//        long endTime=System.currentTimeMillis();
//        System.out.println("结束时间:"+endTime);
//        System.out.println("程序执行时间:"+(endTime-startTime));
    }
}



