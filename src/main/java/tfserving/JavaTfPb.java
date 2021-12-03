package tfserving;

import org.tensorflow.*;
import org.tensorflow.Graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Java读取tensorflow保存的pb文件
 * @author chenm
 */
public class JavaTfPb {

    static private byte[] loadTensorflowModel(String path){
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * // 将数据转为tensor
     * @param inputs
     * @return
     */
    static private Tensor covertArrayToTensor(float[][] inputs){
        return Tensor.create(inputs);
    }


    public static void main(String[] args){

        // 1：load pb文件,并创建图，生成session
        byte[] graphDef = loadTensorflowModel("D:/rf.pb");
        Graph g = new Graph();
        g.importGraphDef(graphDef);
        Session s = new Session(g);


        // 2：输入数据转为tensor
        float[][] inputs = new float[4][6];
        for(int i=0; i<4; i++){
            for(int j=0; j<6; j++){
                if(i < 2) {
                    inputs[i][j] = 2 * i - 5 * j - 6;
                }
                else{
                    inputs[i][j] = 2 * i + 5 * j - 6;
                }
            }
        }
        Tensor input = covertArrayToTensor(inputs);

        // 3:直接进行预测
        Tensor result = s.runner().feed("input", input).fetch("output").run().get(0);

        // 4：转化结果
        long[] rshape = result.shape();
        int rs = (int) rshape[0];
        long[] realResult = new long[rs];
        result.copyTo(realResult);
        for(long a: realResult ) {
            System.out.println(a);
        }
    }

}