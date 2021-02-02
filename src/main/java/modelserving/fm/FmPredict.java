//package modelserving.fm;
//
//import datascoure.RedisClient;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
///*
//    FM模型线上预测函数：
//
//    (java)1：每天读取redis上参数定时刷新到内存.其中redis保存的格式为hash,field为特征名称，value为特征值，如果为向量，直接保存'0.1,0.3,0.4'
//        比如xlearn训练后的模型参数格式为：
//                bias: -7.16863
//                i_0: 0
//                i_1: -0.0373618
//                i_2: 0.00993911
//                i_3: 0.0250137
//                v_1: 0.21096 0.0981231 0.0353906 0.0525409
//                v_2: 0.317617 0.214223 0.0802005 0.194598
//                v_3: 0.197946 0.0378459 0.266612 0.320238
//
//    (python)2: 每天用python刷新特征，根据特征索引文件，将特征转为index存到redis中，用户一个，物品一个分开储存。(涉及到优化的话，将不同活跃的用户/物品存到redis或内存中)
//
//    (java)3：读取用户特征索引，批量物品特征索引进行拼接，拼接不同的样本。
//
//    (java)4：根据样本索引 + w权重计算最终的输出。(注意用优化后的公式计算，复杂度为线性)
//
//    (java)5：继续优化，每个用户开线程池进行计算。
//
//*/
//
//public class FmPredict {
//
//    // fm参数 redis key
//    String fmParRedisKey = "rec_fm_par";
//
//    // fm参数文件路径
//    String fmParFilepath = "";
//
//    // 用户特征redis
//    String userFeatureRedisKey = "rec_user_feature_fm_libsvm";
//
//    // 物品特征redis
//    String itemFeatureRedisKey  = "rec_item_feature_fm_libsvm";
//
//    // redis链接
//    RedisClient rds;
//
//    // FM参数redis
//    HashMap<String, ArrayList<Float>> fmW;
//
//
//    public FmPredict(RedisClient rds) {
//
//        this.rds = rds;
//        // 从redis获取fm权重
//        getFmParmFromRedis();
//        //
//    }
//
//
//    /*
//    方式1：从redis中获取FM的参数
//    方式2：从文本txt中获取FM的参数
//    文件保存格式：
//        bias: -7.16863
//        i_0: 0
//        i_1: -0.0373618
//        v_0: 0.21096 0.0981231 0.0353906 0.0525409
//        v_1: 0.317617 0.214223 0.0802005 0.194598
//
//    最终输出格式：{'i_1':[0.3],'v_1':[0.2,0.3]}
//    */
//    public HashMap<String, ArrayList<Float>> getFmParmFromRedis(String wFrom){
//
//        if(wFrom.equals("redis")){
//            // redis获取参数
//            Map<String,String> recPar = rds.getInstance().hgetAll(fmParRedisKey);
//
//            for(Map.Entry<String,String> x:recPar.entrySet()){
//                // 将参数value变为list
//                String[] signal = x.getValue().split(",");
//                ArrayList<Float> xx = new ArrayList<>();
//                for(String i:signal){
//                    xx.add(Float.valueOf(i));
//                }
//                // 加入到变量中
//                this.fmW.put(x.getKey(),xx);
//            }
//        }
//        else {
//            // 文件获取参数
//            File file = new File(fmParFilepath);
//            try {
//                System.out.println("文件获取参数");
//                BufferedReader reader = new BufferedReader(new FileReader(file));
//                String line = null;
//                while((line = reader.readLine()) != null){
//                    // 切割取出 index / value
//                    String[] indexValue = line.split(":");
//                    // 拿出index
//                    String index = indexValue[0];
//                    // 拿出value并转为float
//                    String[] value = indexValue[1].trim().split(" ");
//                    ArrayList<Float> value_ = new ArrayList<>();
//                    for(String i:value){
//                        value_.add(Float.valueOf(i));
//                    }
//                    this.fmW.put(index,value_);
//                }
//            }
//            catch (IOException e){
//                e.printStackTrace();
//            }
//        }
//
//        return this.fmW;
//    }
//
//
//
//
//
//    /*
//    查询 用户 及物品集合 的特征索引，转为样本数据
//    用户/物品 特征索引分别存在一个hash的redis ; field:userid / item_type_id ; value:json 不但有索引还有特征值 {'2':1,'3':0.3}
//    最终输出为{“001||plan:32”:{'2':1,'5':2,'6':0.4},}
//     */
//    public HashMap<String,HashMap<String,Float>> getUserItemFeatureIndex(String userId,ArrayList<String> goodsTypeIds){
//
//        HashMap<String,HashMap<String,Float>> sample = new HashMap<>();
//
//        try {
//            // 获取用户特征索引
//            String[] userFeature = rds.getInstance().hget(userFeatureRedisKey, userId).split(",");
//
//            // 对于每一个物品
//            for(String goodsTypeId:goodsTypeIds){
//                try {
//                    // 获取物品特征索引
//                    String[] itemFeatue = rds.getInstance().hget(itemFeatureRedisKey, goodsTypeId).split(",");
//
//                    JSONObject json = JSONObject.fromObject(str);
//                    System.out.println(json.getString("name"));
//
//                    ArrayList<Integer> value_ = new ArrayList<>();
//                    for(String i:itemFeatue){
//                        value_.add(Integer.valueOf(i));
//                    }
//                    // 将用户特征放进去
//                    for(String i:userFeature){
//                        value_.add(Integer.valueOf(i));
//                    }
//                    // 加入到样本集合中
//                    sample.put(userId+"||"+goodsTypeId,value_);
//                }
//                catch (Exception e){
//                    e.printStackTrace();
//                    System.out.println("物品redis FM特征索引不存在");
//                }
//            }
//
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            System.out.println("用户redis FM特征索引不存在");
//        }
//        return sample;
//    }
//
//    /*
//    FM 预测主函数，输入用户的样本的特征索引，以及W参数
//    输出最终得分{"001||plan:35":0.5}
//     */
////    public HashMap<String,Double> fmPredict(HashMap<String,HashMap<String,Float>> sample){
////        Double b = this.
////    }
//
//
//
//    public static class MatrixMultiplication {
//        public double[][] matrix_vec(double a[][], double b[][]) {
//            if (a[0].length != b.length)
//                return null;
//            int x = a.length;
//            int y = b[0].length;
//            double c[][] = new double[x][y];
//            for (int i = 0; i < x; i++)
//                for (int j = 0; j < y; j++)
//                    for (int k = 0; k < b.length; k++)
//                        c[i][j] += a[i][k] * b[k][j];
//            return c;
//        }
//
//        public double[][] matrix_dot(double a[][], double b[][]) {
//            if (a[0].length != b[0].length)
//                return null;
//            if (a.length != b.length)
//                return null;
//            int x = a.length;
//            int y = b[0].length;
//            double c[][] = new double[x][y];
//            for (int i = 0; i < x; i++)
//                for (int j = 0; j < y; j++)
//                    c[i][j] = a[i][j] * b[i][j];
//            return c;
//        }
//
//        public double sum_matrix(double[][] maxt){
//            double result = 0.0;
//            int i = maxt.length;
//            int j = maxt[0].length;
//            for (int l = 0; l < i; l++)
//                for(int p = 0;p < j; p++)
//                    result += maxt[l][p];
//            return result;
//        }
//
//        public double[][] sub_matrix(double[][] maxt1,double[][] maxt2){
//            int i = maxt1.length;
//            int j = maxt1[0].length;
//            for (int l = 0; l < i; l++)
//                for(int p = 0;p < j; p++)
//                    maxt1[l][p] += maxt1[l][p]-maxt2[l][p];
//            return maxt1;
//        }
//    }
//
//    public static double sigmord(double x){
//        return 1.0/(1.0+Math.exp(-x));
//    }
//
//
//
//    public static double predict(double w0, double[][] w1, double[][] v, double[][] x){
//        // 进行模型预测
//        MatrixMultiplication mm  = new MatrixMultiplication();
//        double[][] inter_1 = mm.matrix_vec(x,v);
//        double[][] inter_2 = mm.matrix_vec(mm.matrix_dot(x,x),mm.matrix_dot(v,v));
//        double interaction = mm.sum_matrix(mm.sub_matrix(mm.matrix_dot(inter_1,inter_1),inter_2))/2.0;
//        double p = w0 + mm.matrix_vec(x,w1)[0][0] + interaction;
//        return sigmord(p);
//    }
//
//    public static void main(String[] args) {
//
//        // 读取redis中的参数
//
//
//        // 测试参数计算
//        double w0 = 2.0;
//        double[][] w1 = new double[2][1];
//        double[][] v = new double[2][2];
//        double[][] x = new double[1][2];
//
//        w1[0][0] = 1.0;
//        w1[1][0] = 2.0;
//
//        v[0][0] = 1.0;
//        v[0][1] = 2.0;
//        v[1][0] = 1.0;
//        v[1][1] = 2.0;
//
//        x[0][0] = 1.0;
//        x[0][1] = 2.0;
//
//        System.out.println(predict(w0,w1,v,x));
//    }
//}
