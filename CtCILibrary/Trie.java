package CtCILibrary;

import java.util.ArrayList;
import java.util.HashMap;


/* Implements a trie. We store the input list of words in tries so
 * that we can efficiently find words with a given prefix. 
 */ 
public class Trie
{
    // The root of this trie.
    private TrieNode root;
    private String longestWord="";

    /* Takes a list of strings as an argument, and constructs a trie that stores these strings. */
    public Trie(ArrayList<String> list) {
        root = new TrieNode();
        //System.out.println(list.size());
        for (String word : list) {
            //System.out.println(word);
            root.addWord(word);
        }
    }  
    

    /* Takes a list of strings as an argument, and constructs a trie that stores these strings. */    
    public Trie(String[] list) {
        root = new TrieNode();
        for (String word : list) {
            root.addWord(word);
        }
    }    

    /* Checks whether this trie contains a string with the prefix passed
     * in as argument.
     */
    public boolean contains(String prefix, boolean exact) {
        TrieNode lastNode = root;
        //ArrayList<Character> result = new ArrayList<Character>();
        int i = 0;
        for (i = 0; i < prefix.length(); i++) {
            lastNode = lastNode.getChild(prefix.charAt(i));
            //result.add(i, prefix.charAt(i));
            if (lastNode == null) {
                return false;	 
            }
        }
        getWord(lastNode, prefix);
        return !exact || lastNode.terminates();
    }
    
    public boolean contains(String prefix) {
    	return contains(prefix, false);
    	//return "";
    }
    
    public TrieNode getRoot() {
    	return root;
    }

    public String getLongestWord(){
        return longestWord;
    }

//    public void display(TrieNode node, ArrayList<Character> result, int level)
//    {
//
//        // If node is leaf node, it indicates end
//        // of string, so a null character is added
//        // and string is displayed
//        if (!node.terminates())
//        {
//            result.add(level, '\0');
//            StringBuilder builder = new StringBuilder(result.size());
//            for(Character ch: result)
//            {
//                builder.append(ch);
//            }
//            String r = builder.toString();
//            if (r.length() >= longestWord.length())
//                longestWord=r;
//        }
//
//        int i;
//        for (i = 0; i < 26; i++)
//        {
//            // if NON NULL child is found
//            // add parent key to str and
//            // call the display function recursively
//            // for child node
//            if (node.getChild())
//            {
//                result.add(level, node.getChar());
//                display(node.getChild((char)(i + (int)'a')), result, level + 1);
//            }
//        }
//    }
    public void getWord(TrieNode node, String result){
        //System.out.println(node.getChar());

        if (node==null)
            return;

        if (node.terminates()){
            if (result.length() >= longestWord.length())
                longestWord = result;
            //System.out.println(result);
        }

        for(int i=0; i<26; i++){
            char c = (char)(i + (int)'a');
            if (node.childExist(c)){
                node = node.getChild(c);
                getWord(node, result + c);
            }

        }

    }
}
