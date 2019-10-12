package io.swagger.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client2.part2.MyFileWriter;

public class PostThread implements Runnable {

  private CountDownLatch endSignal;
  private Integer startSkierId;
  private Integer idRange;
  private Integer startTime;
  private Integer endTime;
  private Integer numSkiers;
  private Integer numThreads;
  private Integer numRuns;
  MyFileWriter myFileWriter;


  public PostThread(CountDownLatch endSignal, Integer startSkierId, Integer idRange,
                    Integer startTime, Integer endTime, Integer numSkiers, Integer numThreads,
                    Integer numRuns, MyFileWriter myFileWriter) {
    this.endSignal = endSignal;
    this.startSkierId = startSkierId;
    this.idRange = idRange;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numSkiers= numSkiers;
    this.numThreads = numThreads;
    this.numRuns = numRuns;
    this.myFileWriter = myFileWriter;

  }

  public void run() {
    SkiersApi apiInstance = new SkiersApi();
    ApiClient client = apiInstance.getApiClient();
    String basePath = "http://54.88.184.115:8080/bsds_war";
    client.setBasePath(basePath);
    Integer localSuccess = 0;
    Integer localFailures = 0;

    for(int i=0; i<(numRuns/10)*(numSkiers/(numThreads/4)); i++) {
      long postStart = System.nanoTime();
      try {
        LiftRide body = new LiftRide();

        body.time(endTime-startTime+1);

        Integer randomLiftId = ThreadLocalRandom.current().nextInt(5, 61);
        body.liftID(randomLiftId);
        Integer resortID = 10;
        String seasonID = "2019";
        String dayID = "1";

        Integer randomSkierId = ThreadLocalRandom.current().nextInt(startSkierId, startSkierId + idRange + 1);
        apiInstance.writeNewLiftRide(body, resortID, seasonID, dayID, randomSkierId);
        localSuccess++;

        long postEnd = System.nanoTime();
        long latencyNano = postEnd - postStart ;
        double latency = (double) latencyNano / 1000000;

        myFileWriter.addResult(Main.result, postStart, "POST", latency, 200);


      } catch (ApiException e) {

        Integer respCode = e.getCode();
        long postEnd = System.nanoTime();
        long latencyNano = postEnd - postStart ;
        double latency = (double) latencyNano / 1000000;

        myFileWriter.addResult(Main.result, postStart, "POST", latency, respCode);

        System.err.println("Exception when calling SkeirsApi#getResorts");
        localFailures++;
        e.printStackTrace();
      }
    }



    endSignal.countDown();

    Main.totalSuccess.getAndAdd(localSuccess);
    Main.totalFailures.getAndAdd(localFailures);

  }
}
