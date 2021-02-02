package tf_serving;//package TF_serving;
//
//
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//
//import java.io.*;
//import java.lang.reflect.Field;
//import java.util.*;
//
//import org.tensorflow.framework.DataType;
//import org.tensorflow.framework.TensorProto;
//import org.tensorflow.framework.TensorShapeProto;
//
//import tensorflow.serving.Model;
//import tensorflow.serving.Predict;
//import tensorflow.serving.PredictionServiceGrpc;
//
//class run extends Thread {
//    public run(String name,
//               PredictionServiceGrpc.PredictionServiceBlockingStub stub,
//               Predict.PredictRequest.Builder predictRequestBuilder,
//               TensorShapeProto.Builder tensorShapeBuilder,
//               ArrayList<Object> data){
//        super(name);
//        int length = data.size();
//    }
//    static int Len = this.length;
//
//    @Override
//    public void run(){
//        // 设置入参1
//        TensorProto.Builder tensorids_ids = TensorProto.newBuilder();
//        tensorids_ids.setDtype(DataType.DT_INT64);
//        tensorids_ids.setTensorShape(tensorShapeBuilder.build());
//        tensorids_ids.addAllInt64Val(ids_vec);
//
//        // 设置入参2
//        TensorProto.Builder tensorids_val = TensorProto.newBuilder();
//        tensorids_val.setDtype(DataType.DT_FLOAT);
//        tensorids_val.setTensorShape(tensorShapeBuilder.build());
//        tensorids_val.addAllFloatVal(vals_vec);
//
//        Map<String, TensorProto> map=new LinkedHashMap<>();
//        map.put("feat_ids", tensorids_ids.build());
//        map.put("feat_vals", tensorids_val.build());
//        predictRequestBuilder.putAllInputs(map);
//
//        return stub.predict(predictRequestBuilder.build());
//
//    }
//}
//
//public class tf_serving_multithreading{
//
//}
