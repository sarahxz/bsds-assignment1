package io.swagger.client2.part2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileWriter {
  String fileName;
  BufferedWriter outputFile = null;
  FileWriter fw = null;
  //String result = "start time, request type, latency, response code\n";

  public MyFileWriter(String fileName) {
    this.fileName = fileName;
  }

  public void createOutputFile() {
    try {
      fw = new FileWriter(fileName);
      outputFile = new BufferedWriter(fw);
    } catch (IOException ioe) {
      System.out.println("Something went wrong! : " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }

  public synchronized void addResult(StringBuffer result, double postStart, String requestType,
                                double latency, Integer respCode) {
    result.append(postStart).append(",")
        .append(requestType).append(",")
        .append(latency).append(",")
        .append(respCode).append("\n");
  }

  public void writeToOutput(String result) throws IOException {

    outputFile.write(result);
  }

  public void closeOutputFile() {

    if (outputFile != null) {
      try {
        outputFile.close();
      } catch (IOException e) {
        System.out.println("Failed to close output stream in finally block");
        e.printStackTrace();
      }
    }
  }
}
