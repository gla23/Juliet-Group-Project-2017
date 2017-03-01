/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package julietgroupproject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.encog.neural.neat.NEATPopulation;

/**
 *
 * @author Peter
 */
public class SavedAlien implements Serializable{
    public Alien body;
    public int inputCount;
    public int outputCount;
    public NEATPopulation pop;
    private List<GenerationResult> generations = new ArrayList<>();
    private transient boolean hasBeenReset = false;
    private String name;
    
    public synchronized void alienChanged()
    {
        System.out.println("Invalidating");
        pop = null;
        generations = new ArrayList<>();
        hasBeenReset = true;
    }
    
    public synchronized void alienSaved()
    {
        hasBeenReset = false;
    }
    
    public synchronized List<GenerationResult> getLastEntries(int n) {
        return new ArrayList<>(generations.subList(Math.max(0, generations.size()-n), generations.size()));
    }
    
    public synchronized List<GenerationResult> getEntries()
    {
        return new ArrayList<>(generations);
    }
    
    public synchronized GenerationResult getMostRecent()
    {
        if (generations.size() > 0)
            return generations.get(generations.size() - 1);
        else
            return null;
    }
    
    public synchronized int savedEntryCount()
    {
        return generations.size();
    }
    
    public synchronized void addEntry(GenerationResult entry)
    {
        generations.add(entry);
    }
    
    public synchronized boolean getHasBeenReset()
    {
        return hasBeenReset;
    }
    
    public synchronized String getName()
    {
        return name;
    }
    
    public synchronized void setName(String _name)
    {
        name = _name;
    }
}
