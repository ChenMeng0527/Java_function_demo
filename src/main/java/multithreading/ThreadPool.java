package multithreading;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPool {

    // 任务数量
    private static int produceTaskMaxNumber = 5;


    /**
     * 线程任务
     * @param sample
     * @return
     */
    public static String ThreadPoolTask1(int sample){
        int ran = (int) (Math.random() * 100);
        try {
            System.out.println("分配时间s:"+ran*0.1);
            Thread.sleep(ran*100);
            System.out.println(sample+"执行时间s:"+ran*0.1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.valueOf(sample*sample);
    }


    /**
     * 单线程时间限制
     * @param threadPool
     * @param futures
     * @param result
     * @throws Exception
     */
    public static void singalTimeThread(ThreadPoolExecutor threadPool,
                                        List<Future<?>> futures,
                                        ArrayList<String> result) throws Exception{

        for(int i=1; i<=produceTaskMaxNumber; i++){
            try {
                //产生一个任务，并将其加入到线程池
                String task = "task@ " + i;
                System.out.println("生成任务:" + task);
                ThreadPoolTask a = new ThreadPoolTask(task);
                Future<?> future = threadPool.submit(a);
                futures.add(future);

                try {
                    future.get(5, TimeUnit.SECONDS);
                    // 加入结果
                    String xx = String.valueOf(a.getTask());
                    result.add(xx);
                    System.out.println("Thread returns");
                } catch (Exception e) {
                    System.out.println("Time out occured Exception: " + e);
//                    e.printStackTrace();
                    future.cancel(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        System.out.println(result);
    }


    /**
     * 总线程时间限制
     * @param threadPool
     * @param futures
     * @param result
     * @throws Exception
     */
    public static void allTimeThread(ThreadPoolExecutor threadPool,
                              List<Future<?>> futures,
                              ArrayList<String> result) throws Exception{

        List<ThreadPoolTask> ThreadPoolTask_array = new ArrayList<>();

        for(int i=1; i<=produceTaskMaxNumber; i++){
            try {
                //产生一个任务，并将其加入到线程池
                String task = "task@ " + i;
                System.out.println("生成任务:" + task);
                ThreadPoolTask a = new ThreadPoolTask(task);

                ThreadPoolTask_array.add(a);

                Future<?> future = threadPool.submit(a);
                futures.add(future);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
            if (threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("task all finished");
                for(ThreadPoolTask i:ThreadPoolTask_array){
                    String xx = String.valueOf(i.getTask());
                    result.add(xx);
                }
            } else {
                System.out.println("task time out,will terminate");
                for(ThreadPoolTask i:ThreadPoolTask_array){
                    String xx = String.valueOf(i.getTask());
                    result.add(xx);
                }
                for (Future<?> f : futures) {
                    if (!f.isDone()) {
                        f.cancel(true);
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("executor is interrupted");
        }
        threadPool.shutdown();
        System.out.println(result);
    }

    public static void allTaskThread(ThreadPoolExecutor threadPool,
                                     ArrayList<String> result) throws Exception{


        for(int i=1; i<=produceTaskMaxNumber; i++){

            try {
                //产生一个任务，并将其加入到线程池
                String task = "task@ " + i;
                System.out.println("生成任务:" + task);
                int finalI = i;
                threadPool.execute(() -> {
                    result.add(ThreadPoolTask1(finalI));
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }


        }

        boolean completed = threadPool.getTaskCount() == threadPool.getCompletedTaskCount();
        if (completed){
            System.out.println("completed1");
            System.out.println(result);
        }
        while (!completed){
            completed = threadPool.getTaskCount() == threadPool.getCompletedTaskCount();
            if (completed){
                System.out.println("completed2");
                System.out.println(result);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        //构造一个线程池
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2,
                                                               4,
                                                               3,
                                                                TimeUnit.SECONDS,
                                                                new ArrayBlockingQueue<Runnable>(10),
                                                                new ThreadPoolExecutor.CallerRunsPolicy());
        ArrayList<String> result = new ArrayList<>();
        List<Future<?>> futures = new ArrayList<Future<?>>();

        // 单个线程的时间限制
//        singalTimeThread(threadPool,futures,result);

        // 总线程时间限制
//        allTimeThread(threadPool,futures,result);

        // 所有任务都完成
        allTaskThread(threadPool,result);
    }
}


class ThreadPoolTask implements Runnable, Serializable {

    //保存任务所需要的数据
    private Object threadPoolTaskData;

    public ThreadPoolTask(String tasks){
        this.threadPoolTaskData = tasks;
    }

    @Override
    public void run(){

        // 打印线程名称
        System.out.println(threadPoolTaskData+"用的线程为:"+Thread.currentThread().getName());
        System.out.println("开始执行任务:"+threadPoolTaskData);

        try {
            // 分任务执行时间
            int ran = (int) (Math.random() * 100);
            System.out.println(this.threadPoolTaskData+"分配时间s:"+ran*0.1);
            Thread.sleep(ran*100);
            System.out.println(this.threadPoolTaskData+"执行时间s:"+ran*0.1);
            this.threadPoolTaskData = this.threadPoolTaskData+"xxxx";
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
    public Object getTask(){
        System.out.println("获取数据:"+this.threadPoolTaskData);
        return this.threadPoolTaskData;
    }
}

