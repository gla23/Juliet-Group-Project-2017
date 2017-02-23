package julietgroupproject;

import java.io.Serializable;

/**
 * Abstract representation of an Alien.
 * The class contains tree-structured blocks
 * representing spatials of body parts with their properties.
 * 
 * @author George Andersen
 */
public class Alien implements Serializable {
    public Block rootBlock;
    
    public Alien(Block rootBlock) {
        this.rootBlock = rootBlock;
    }
    
}
