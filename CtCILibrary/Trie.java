package CtCILibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
        //getWord(lastNode, prefix, prefix.length());
        displayTrie(lastNode,prefix,0);
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

    void displayTrie(TrieNode root, String s, int children){
        //System.out.println(node.getChar());
        if (root==null) {
            return;
        }
        TrieNode current = root;

        if (root.terminates()) {
            //System.out.println(s);
            root = this.root;
            if (s.length() >= longestWord.length()) {
                longestWord =s;
            }
            //s = s.substring(0, s.length() - 1);
        }
        HashMap<Character, TrieNode> nodes = current.getChildren();
        if (nodes.size() == 0)
            return;
        ArrayList ch  = new ArrayList();
        for(int i=0; i<26; i++) {
            char c = (char) (i + (int) 'a');
            if (nodes.containsKey(c)){
                ch.add(c);
            }
        }
        for(Object c : ch){
            displayTrie(root.getChild((char) c), s + c, 0);
//


        }
    }
}
