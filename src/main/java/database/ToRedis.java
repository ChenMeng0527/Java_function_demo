package database;
import redis.clients.jedis.Jedis;

import java.util.*;


public class ToRedis {
    private static volatile Jedis redisClient;
    private static byte[] Serializeuserfeaure;
    private static String REDIS_LOCAL_HOST = "0.0.0.0";
    private static int REDIS_PORT = 6379;
    private static String REDIS_PASSWORD="";

    private ToRedis() {
        redisClient = new Jedis(REDIS_LOCAL_HOST,REDIS_PORT);
//        redisClient.auth(REDIS_PASSWORD);
    }

    public static Jedis getInstance(){
        if(redisClient==null){
            synchronized (ToRedis.class){
                if(redisClient==null){
                    redisClient = new Jedis(REDIS_LOCAL_HOST,REDIS_PORT);
//                    redisClient.auth(REDIS_PASSWORD);
                }
            }
        }
        return redisClient;
    }


    public static void main(String[] args) {
        System.out.println(ToRedis.getInstance().get("chen"));

//        List<String> result = RedisClient.getInstance().lrange("hot1:plan", 0, -1);
//        System.out.println(result);
//        Set<String> result =  ToRedis.getInstance().zrange("rs_item_similarity_cf-plan:35", 0, -1);
//        ArrayList<String> a = new ArrayList<>(result);
//        System.out.println(result);
//        System.out.println(a);
//
//        // 用户特征
//        Map<String,Map<String,String>> userfeaure = new HashMap<>();
//        Map<String,String> xx = new HashMap<>();
//        xx.put("sex", "男");
//        userfeaure.put("chen",xx );
//        Serializeuserfeaure = Serialize.serialize(userfeaure);
//        RedisClient.getInstance().set("rec_user_feature", String.valueOf(Serializeuserfeaure));
//
//        String bytes=RedisClient.getInstance().get("rec_user_feature");
//
//        String str = "{\"1\":\"1\",\"3\":\"1\",\"5\":\"0.3\"}";
//        JSONObject json = JSONObject.fromObject(str);
//        System.out.println(json.getString("name"));

//        JSONObject object = new JSONObject();
//        object.put("1",1);
//        object.put("3",1);
//        object.put("5",0.3);
//        object.toJavaObject();
//        System.out.println(object.getClass().getName());

        //
//        String jsonStr1 = "{'password':'123456','username':'dmego'}";
//        JSONObject user = JSON.parseObject(jsonStr1);
//        System.out.println("json字符串转简单java对象:"+user.toString());

//        System.out.println(Serialize.deSerialize(bytes));

//        Scheduler cronScheduler;	// use cron4j
//        cronScheduler = new Scheduler();
//        Filter ff = new Filter();

//        cronScheduler.schedule("*/1 * * * *", ff::updateData);
//        System.out.println(ff.needDataFromPhp);
//        cronScheduler.start();
//        Map<String,String> temp = new HashMap<String,String>();
//        temp.put("a", "b");
//        System.out.println(temp.get("a"));
    }
}

