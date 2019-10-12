package io.swagger.client2.part2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyFileReader {

  String fileName;

  public MyFileReader(String fileName) {

    this.fileName = fileName;
  }

  public List<String> readFile() {

    List<String> res = new ArrayList<>();

    BufferedReader br = null;
    FileReader fr = null;

    try {
      fr = new FileReader(fileName);
      br = new BufferedReader(fr);

      String sCurrentLine;

      while ((sCurrentLine = br.readLine()) != null) {
        res.add(sCurrentLine);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (br != null)
          br.close();
        if (fr != null)
          fr.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
    return res;
  }
}