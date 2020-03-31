package edu.cs300;
import CtCILibrary.*;

public class PassageProcessor{
    public static void main(String[] args){
//        String[] arguments = new String[] {"con"};
//        ParallelTextSearch.main(arguments);
//        System.out.println("searching");
        while (true) {
            SearchRequest req = MessageJNI.readPrefixRequestMsg();
            String[] argument = new String[]{req.prefix};
            ParallelTextSearch.main(argument);
            new MessageJNI().writeLongestWordResponseMsg( 1, "and", 1, "Jane Austen","andulousa", 6, 1);
        }
    }
}