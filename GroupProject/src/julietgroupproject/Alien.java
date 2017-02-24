package julietgroupproject;

import java.io.Serializable;
import java.util.Random;

/**
 * Abstract representation of an Alien.
 * The class contains tree-structured blocks
 * representing spatials of body parts with their properties.
 * 
 * @author George Andersen
 */
public class Alien implements Serializable {
    public Block rootBlock;
    private String name;
    public int materialCode = 1;
    private static Random rng = new Random();
    
    public Alien(Block rootBlock) {
        this.rootBlock = rootBlock;
        name = "alien" + Integer.toString(rng.nextInt());
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String _name)
    {
        name = _name;
    }
    
    public int getCode() {
        return materialCode;
    }
    
}
