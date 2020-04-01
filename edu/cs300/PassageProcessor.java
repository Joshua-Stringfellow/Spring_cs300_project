package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class PassageProcessor {
  public static void main(String[] args){
   while (true) {
      SearchRequest req = MessageJNI.readPrefixRequestMsg();
      if (req.requestID == 0)
          break;
      String prefix = req.prefix;
      System.out.println(prefix);
      ArrayList<String> passages = new ArrayList<String>();

      int treeCount = 0;
      try {
        passages = getPassages();
        treeCount = passages.size();
        //System.out.println("number of workers: " + treeCount);
      } catch (FileNotFoundException e) {}

      ArrayBlockingQueue[] workers = new ArrayBlockingQueue[treeCount];
      ArrayBlockingQueue resultsOutputArray = new ArrayBlockingQueue(treeCount * 10);

      if (prefix.length() <= 2) {
        System.out.println("Provide prefix (min 3 characters) for search i.e. con\n");
        System.exit(0);
      }

      for (int i = 0; i < treeCount; i++) {
        workers[i] = new ArrayBlockingQueue(10);
        new Worker(passages.get(i), i, workers[i], resultsOutputArray).start();
        try {
          workers[i].put(prefix);
        } catch (InterruptedException e) {};
      }

      int counter = 0;
       String[] responses = new String[passages.size()];

       while (counter < treeCount) {
        try {
          String results = (String) resultsOutputArray.take();
          String passage = results.substring(0, results.indexOf(":"));
          //int index = counter;
            responses[passages.indexOf(passage)] = results;
          counter++;
        } catch (InterruptedException e) {};
       }
       int ncounter = 1;
       for (String s : responses){
           String[] tmp = s.split("[:]");
           String passage = tmp[0];
           String word=tmp[1];
           if (word !=" not found")
                new MessageJNI().writeLongestWordResponseMsg(req.requestID, prefix, ncounter, passage , word, treeCount, 1);
           else
               new MessageJNI().writeLongestWordResponseMsg(req.requestID, prefix, ncounter, passage , s, treeCount, 0);
           System.out.println("results:" + s);
           ncounter++;
       }
    }
  }

  private static ArrayList<String> getPassages() throws FileNotFoundException{
    ArrayList<String> passages = new ArrayList<String>();
    File file = new File("passages.txt");
    Scanner sc = new Scanner(file);

    sc.useDelimiter("EOL");
    int i = 0;
    while (sc.hasNextLine()){
      i++;
      passages.add(sc.nextLine());
  }

    return passages;

  }

}
