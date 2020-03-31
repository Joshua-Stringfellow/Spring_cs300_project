package edu.cs300;
import CtCILibrary.*;
import java.util.concurrent.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

class Worker extends Thread{

  Trie textTrieTree;
  ArrayBlockingQueue prefixRequestArray;
  ArrayBlockingQueue resultsOutputArray;
  int id;
  String passageName;

  public Worker(String passageName,int id,ArrayBlockingQueue prefix, ArrayBlockingQueue results){
    this.passageName = passageName;

    try {
      //System.out.println("Building Trie");
      this.textTrieTree=new Trie(readPassage());
    }catch (FileNotFoundException e){}

    this.prefixRequestArray=prefix;
    this.resultsOutputArray=results;
    this.id=id;
    //this.passageName="Passage-"+Integer.toString(id)+".txt";//put name of passage here
  }

  public void run() {
    System.out.println("Worker-"+this.id+" ("+this.passageName+") thread started ...");
    //while (true){
      try {
        String prefix=(String)this.prefixRequestArray.take();
        boolean found = this.textTrieTree.contains(prefix);
        
        if (!found){
          System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> not found ");
          resultsOutputArray.put(passageName+":"+prefix+" not found");
        } else{
          System.out.println("Worker-"+this.id+" "+req.requestID+":"+ prefix+" ==> "+word);
          resultsOutputArray.put(passageName+":"+textTrieTree.getLongestWord());

        }
      } catch(InterruptedException e){
        System.out.println(e.getMessage());
      }
    //}
  }

  private ArrayList<String> readPassage() throws FileNotFoundException{
    ArrayList<String> words = new ArrayList<String>();
    String filename = passageName;
    File file = new File(filename);
    Scanner sc = new Scanner(file);

    sc.useDelimiter(" ");
    int i = 0;
    while (sc.hasNext()){
      String word = sc.next().replaceAll("[^a-zA-Z]","").toLowerCase();
      //System.out.println(word + i);

      i++;
      words.add(word);
    }

    return words;
  }
}
