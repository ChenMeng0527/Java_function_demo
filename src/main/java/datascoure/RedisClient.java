package datascoure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;
import util.Config;
import util.Serialize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedisClient {
    private static volatile Jedis redisClient;
    private static byte[] Serializeuserfeaure;

    private RedisClient() {
        redisClient = new Jedis(Config.REDIS_LOCAL_HOST,Config.REDIS_PORT);

    }

    public static Jedis getInstance(){
        if(redisClient==null){
            synchronized (RedisClient.class){
                if(redisClient==null){
                    redisClient = new Jedis(Config.REDIS_LOCAL_HOST,Config.REDIS_PORT);
                }
            }
        }
        return redisClient;
    }


    public static void main(String[] args) {

        List<String> result = RedisClient.getInstance().lrange("hot:plan", 0, -1);
        System.out.println(result);

        // 用户特征
        Map<String,Map<String,String>> userfeaure = new HashMap<>();
        Map<String,String> xx = new HashMap<>();
        xx.put("sex", "男");
        userfeaure.put("chen",xx );
        Serializeuserfeaure = Serialize.serialize(userfeaure);
        RedisClient.getInstance().set("rec_user_feature", String.valueOf(Serializeuserfeaure));

        String bytes=RedisClient.getInstance().get("rec_user_feature");

//        String str = "{\"name\":\"zhangsan\",\"password\":\"zhangsan123\",\"email\":\"10371443@qq.com\"}";
//        JSONObject json = JSONObject.fromObject(str);
//        System.out.println(json.getString("name"));

        String jsonStr1 = "{'password':'123456','username':'dmego'}";
        JSONObject user = JSON.parseObject(jsonStr1);
        System.out.println("json字符串转简单java对象:"+user.toString());

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
