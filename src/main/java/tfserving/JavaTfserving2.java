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
import static java.util.Arrays.asList;

public class JavaTfserving2 {


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
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(2));
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(16));





//       多行数据
//        ArrayList<List<Long>> ids_vec2 = new ArrayList<>();
//        ids_vec2.add(asList(4L,15L,34608L,445898L,446083L,446293L,449140L,490778L,491626L,491634L,491641L,491645L,491662L,491668L,491700L,491708L));
//        ids_vec2.add(asList(4L,15L,34608L,445898L,446083L,446293L,449140L,490778L,491626L,491634L,491641L,491645L,491662L,491668L,491700L,491708L));
//        ArrayList<List<Float>> val_vec2 = new ArrayList<>();
//        val_vec2.add(asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f));
//        val_vec2.add(asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f));

        ArrayList<List<Long>> ids_vec2 = new ArrayList<>();
        ids_vec2.add(asList(5L,9L,33L,445897L,446054L,446293L,446358L,491531L,491625L,491633L,491640L,491646L,491655L,491668L,491700L,491708L));
        ids_vec2.add(asList(5L,9L,33L,445897L,446054L,446293L,446358L,491531L,491625L,491633L,491640L,491646L,491655L,491668L,491700L,491708L));
        ArrayList<List<Float>> val_vec2 = new ArrayList<>();
        val_vec2.add(asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f));
        val_vec2.add(asList(1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f));


        TensorProto.Builder tensorids_ids = TensorProto.newBuilder();
        tensorids_ids.setDtype(DataType.DT_INT64);
        tensorids_ids.setTensorShape(tensorShapeBuilder.build());

        for(int index=0;index<=1;index++){
            tensorids_ids.addAllInt64Val(ids_vec2.get(index));
        }


        // 设置入参2
        TensorProto.Builder tensorids_val = TensorProto.newBuilder();
        tensorids_val.setDtype(DataType.DT_FLOAT);
        tensorids_val.setTensorShape(tensorShapeBuilder.build());
        for(int index=0;index<=1;index++){
            tensorids_val.addAllFloatVal(val_vec2.get(index));
        }


        Map<String, TensorProto>  map=new LinkedHashMap<>();
        map.put("feat_ids", tensorids_ids.build());
        map.put("feat_vals", tensorids_val.build());
        predictRequestBuilder.putAllInputs(map);

        long startTime=System.currentTimeMillis();
        for(int i=0;i<1;i++){
              List<Float> result = stub.predict(predictRequestBuilder.build()).getOutputsMap().get("prob").getFloatValList();
        }
        long endTime=System.currentTimeMillis();
        System.out.println("程序执行时间:"+(endTime-startTime));
    }
}




