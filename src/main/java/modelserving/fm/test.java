//package modelserving.fm;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//public class test {
//    public static void main(String[] args) {
//        ArrayList<ArrayList<Integer>> sample = new ArrayList<>();
//        sample.add(new ArrayList<Integer>({1,2,3}) );
//
//        //构造一个线程池
//        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2,
//                4,
//                3,
//                TimeUnit.SECONDS,
//                new ArrayBlockingQueue<Runnable>(10),
//                new ThreadPoolExecutor.CallerRunsPolicy());
//
//        for(int i=1;i<=produceTaskMaxNumber;i++){
//            try {
//                //产生一个任务，并将其加入到线程池
//                String task = "task@ " + i;
//                System.out.println("put " + task);
//                threadPool.execute(new multithreading.ThreadPoolTask(task));
//                //便于观察，等待一段时间
//                Thread.sleep(produceTaskSleepTime);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }
//}
