package tfserving;

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


/**
 * java 调用python布置的tfserving
 * @author chenm
 */
public class JavaTfserving {

    /**
     * 将输入数据转为两个tensor变量，并预测
     * @param stub
     * @param predictRequestBuilder
     * @param tensorShapeBuilder
     * @param idsVec
     * @param valsVec
     * @return
     */
    private static Predict.PredictResponse predict(PredictionServiceGrpc.PredictionServiceBlockingStub stub,
                                                   Predict.PredictRequest.Builder predictRequestBuilder,
                                                   TensorShapeProto.Builder tensorShapeBuilder,
                                                   List<Long> idsVec,
                                                   List<Float> valsVec){

        // 设置入参1
        TensorProto.Builder tensorIds = TensorProto.newBuilder();
        tensorIds.setDtype(DataType.DT_INT64);
        tensorIds.setTensorShape(tensorShapeBuilder.build());
        tensorIds.addAllInt64Val(idsVec);

        // 设置入参2
        TensorProto.Builder tensorVal = TensorProto.newBuilder();
        tensorVal.setDtype(DataType.DT_FLOAT);
        tensorVal.setTensorShape(tensorShapeBuilder.build());
        tensorVal.addAllFloatVal(valsVec);

        Map<String, TensorProto> map = new LinkedHashMap<>();
        map.put("feat_ids", tensorIds.build());
        map.put("feat_vals", tensorVal.build());
        predictRequestBuilder.putAllInputs(map);

        return stub.predict(predictRequestBuilder.build());

    }

    private static ArrayList<ArrayList> readData() throws IOException {

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
                    String[] signalData = line.split(" ");

                    ArrayList<ArrayList> signalSample = new ArrayList<>();
                    ArrayList<Long> ids = new ArrayList<>();
                    ArrayList<Float> vals = new ArrayList<>();

                    for (int i = 1; i < signalData.length; i++) {
                        String[] xx = signalData[i].split(":");
                        ids.add(Long.valueOf(xx[0]));
                        vals.add(Float.valueOf(xx[1]));
                    }
                    signalSample.add(ids);
                    signalSample.add(vals);
                    samples.add(signalSample);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return samples;
    }


    public static void main(String[] args) throws IOException {

        // 监听serving端口，这里还是先用block模式
        ManagedChannel channel = ManagedChannelBuilder.forAddress("172.17.36.29", 8500).usePlaintext(true).build();
        PredictionServiceGrpc.PredictionServiceBlockingStub stub = PredictionServiceGrpc.newBlockingStub(channel);
        // 模型名称和模型方法名预设
        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName("deepfm");
        modelSpecBuilder.setSignatureName("");
        // 创建请求
        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
        predictRequestBuilder.setModelSpec(modelSpecBuilder);
        // tensor纬度
        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(16));

        // 输入数据
        List<Long> idsVec = Arrays.asList(5L,9L,33L,445897L,446054L,446293L,446358L,491531L,491625L,491633L,491640L,491646L,491655L,491668L,491700L,491708L);
//        List<Integer> ids_vec = Arrays.asList(4,15,34608,445898,446083,446293,449140,490778,491626,491634,491641,491645,491662,491668,491700,491708);
        List<Float> valsVec = Arrays.asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f);
        System.out.println(idsVec);

        long startTime=System.currentTimeMillis();
        Predict.PredictResponse result = predict(stub,predictRequestBuilder,tensorShapeBuilder,idsVec,valsVec);
        System.out.println(result.getOutputsMap().get("prob").getFloatValList());
        long endTime=System.currentTimeMillis();
        System.out.println("程序执行时间:"+(endTime-startTime)/1000);
        System.out.println(result);
    }
}



