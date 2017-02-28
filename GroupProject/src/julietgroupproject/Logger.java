package julietgroupproject;


import java.util.ArrayDeque;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author George
 */
public class Logger {
    
    private int limit = 15;
    private ArrayDeque<String> entries = new ArrayDeque<>();
    
    public Logger(int limit) {
        this.limit = limit;
    }
    
    public void push(String entry) {
        entries.push(entry);
        
        while (entries.size() > limit) {
            entries.removeLast();
        }
    }
    
    public int getSize() {
        return entries.size();
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public ArrayList<String> getEntries() {
        return new ArrayList(entries);
    }
}
