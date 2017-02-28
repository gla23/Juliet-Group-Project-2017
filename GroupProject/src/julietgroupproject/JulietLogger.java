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
public class JulietLogger<T> {
    
    public static final int DEFAULT_LIMIT = 15;
    private int limit;
    private ArrayDeque<T> entries = new ArrayDeque<>();
    
    public JulietLogger(int limit) {
        this.limit = limit;
        entries = new ArrayDeque<>();
    }

    public JulietLogger() {
        this(DEFAULT_LIMIT);
    }
    
    public synchronized void push(T entry) {
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
    
    public synchronized ArrayList<T> getEntries() {
        return new ArrayList(entries);
    }
    
    public void clear() {
        entries.clear();
    }
}
