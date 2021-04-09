package bloomFilter;

import database.ToRedis;

public class BloomFilterRedis {
    public static void main(String[] args) {
        String a = ToRedis.getInstance().get("chen");
        System.out.println(a);
    }
}
