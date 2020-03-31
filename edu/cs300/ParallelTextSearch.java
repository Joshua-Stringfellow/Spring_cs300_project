package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

public class ParallelTextSearch{
  public static void main(String[] args){

    ArrayList<String> passages = new ArrayList<String>();
    //MessageJNI.main(null);

    int treeCount=0;
    try {
      passages = getPassages();
      treeCount = passages.size();
      //System.out.println("number of workers: " + treeCount);
    }catch (FileNotFoundException e){
    }

    ArrayBlockingQueue[] workers = new ArrayBlockingQueue[treeCount];
    ArrayBlockingQueue resultsOutputArray=new ArrayBlockingQueue(treeCount*10);

    if (args.length == 0 || args[0].length() <= 2 ){
        System.out.println("Provide prefix (min 3 characters) for search i.e. con\n");
        System.exit(0);
    }

    for (int i=0;i<treeCount;i++) {
      workers[i]=new ArrayBlockingQueue(10);
      new Worker(passages.get(i),i,workers[i],resultsOutputArray).start();
      try {
        workers[i].put(args[0]);
      } catch (InterruptedException e) {};
    }
      
    int counter=0;

    while (counter<treeCount){
      try {
        String results = (String)resultsOutputArray.take();

        //System.out.println("results:"+results);
        counter++;
      } catch (InterruptedException e) {};
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
