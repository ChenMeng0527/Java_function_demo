package multithreading;

public class RunnableTest {
    public static void main(String[] args) {
        long starttime = System.currentTimeMillis();
//        class MyRunnable implements Runnable{
//            private double result= 0;
//            @Override
//            public void run(){
//                for (int i = 0; i < 2000000; i++) {
//                    double d = Math.random();
////            System.out.println("d"+d);
//                    this.result = this.result + d;
////                    System.out.println(Thread.currentThread().getName()+":"+this.result);
//                }
//            }
//        }
//        MyRunnable runnable = new MyRunnable();
//        Thread t1 = new Thread(runnable);
//        Thread t2 = new Thread(runnable);
//        Thread t3 = new Thread(runnable);
//        t1.start();
//        t2.start();
//        t3.start();

//
        double result= 0;
        for (int i = 0; i < 2000000; i++) {
            double d = Math.random();
//            System.out.println("d"+d);
            result=result+d;
//            System.out.println("result"+result);
        }
        long endtime = System.currentTimeMillis();
        System.out.println("程序执行时间:" + (endtime - starttime) + "ms");
//        System.out.println(result);
    }
}
