package io.swagger.client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.swagger.client2.part2.MyFileReader;
import io.swagger.client2.part2.MyFileWriter;

public class Main {


  static AtomicInteger totalSuccess = new AtomicInteger();
  static AtomicInteger totalFailures = new AtomicInteger();
  static String header = "start time, request type, latency, response code\n";
  static StringBuffer result = new StringBuffer("");
//  static ArrayList<Double> latencies = new ArrayList<Double>();
  // static List<Double> latencies = Collections.synchronizedList(new ArrayList<Double>());

  public static void main (String[] args) throws IOException {

    Integer numThreads = Integer.parseInt(args[0]);
    Integer numSkiers = Integer.parseInt(args[1]);
    Integer numLifts = Integer.parseInt(args[2]);
    Integer numRuns = Integer.parseInt(args[3]);
    Integer port;

    System.out.println("Phase One started!");
    long startTime = System.nanoTime();

    MyFileWriter myFileWriter = new MyFileWriter("output.csv");
    myFileWriter.createOutputFile();

    CountDownLatch endSignal1 = new CountDownLatch((int)(Math.ceil((numThreads/4)/10)));

    // phase one launch numThreads/4 threads
    ExecutorService pool = Executors.newFixedThreadPool(numThreads/4);

    for(int i=0; i<numThreads/4; i++) {
        pool.execute(new PostThread(endSignal1, 1+i*(numSkiers/(numThreads/4)),
            numSkiers/(numThreads/4), 1, 90, numSkiers, numThreads,
            numRuns, myFileWriter));
    }

    try {
      endSignal1.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("Phase One finished!");


    // phase two launch numThreads threads
    CountDownLatch endSignal2 = new CountDownLatch((int)( Math.ceil(numThreads)));
    ExecutorService pool2 = Executors.newFixedThreadPool(numThreads);


    for(int i=0; i<numThreads; i++) {
      pool2.execute(new PostThread(endSignal2, 1+i*(numSkiers/numThreads),
          numSkiers/numThreads, 91, 360, numSkiers, numThreads, numRuns,
          myFileWriter));
    }
    try {
      endSignal2.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println("Phase Two finished!");

    // phase three launch
    CountDownLatch endSignal3 = new CountDownLatch((int) Math.ceil(numThreads/4));
    ExecutorService pool3 = Executors.newFixedThreadPool(numThreads/4);

    for(int i=0; i<numThreads/4; i++) {
      pool3.execute(new PostThread(endSignal3, 1+i*(numSkiers/(numThreads/4)),
          (numSkiers/numThreads/4), 361, 420, numSkiers, numThreads, numRuns,
          myFileWriter));
    }

    try {
      endSignal3.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // calculate total wall time

    long estimatedTime = System.nanoTime() - startTime;
    double totalRunTime = (double) estimatedTime / 1000000000;
    System.out.println("total run time: " + totalRunTime + " seconds.");
    System.out.println("total success: " + totalSuccess);
    System.out.println("total failures: " + totalFailures);

    myFileWriter.writeToOutput(header + result.toString());
    myFileWriter.closeOutputFile();

    // calculate latency report

    MyFileReader myFileReader = new MyFileReader("output.csv");
    List<String> res = myFileReader.readFile();

    List<Double> latencies = new ArrayList<>();
    for(int i=1; i<res.size(); i++) {
      String[] split = res.get(i).split(",");
      String latencyStr = split[2];
      Double latency =  Double.parseDouble(latencyStr);
      latencies.add(latency);
    }

    latencies.remove(0);


    double meanResponse;
    double medianResponse;
    double ninetynine;
    double totalResponse = 0;

    for (int i=0; i<latencies.size(); i++) {
      totalResponse = totalResponse + latencies.get(i);
    }

    meanResponse = totalResponse/latencies.size();

    double[] array = new double[latencies.size()];
    for(int i=0; i<latencies.size(); i++) {
      array[i] = latencies.get(i);
    }
    Arrays.sort(array);

    if(latencies.size()%2 !=0) {
      medianResponse = array[array.length/2];
    } else {
      medianResponse = (array[array.length/2] + array[array.length/2+1])/2;
    }

    ninetynine = array[(int) Math.round(array.length*0.99)];

    System.out.println("mean response time: " + meanResponse + " ms");
    System.out.println("median response time: " + medianResponse + " ms");
    System.out.println("throughput: " +
        (totalSuccess.getAndAdd(totalFailures.intValue()))/totalRunTime + " requests/ms");
    System.out.println("99 percentile response time: " + ninetynine + " ms");

    pool.shutdown();
    pool2.shutdown();
    pool3.shutdown();


  }
}
