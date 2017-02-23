package julietgroupproject;

/**
 * Abstract representation of an Alien.
 * The class contains tree-structured blocks
 * representing spatials of body parts with their properties.
 * 
 * @author George Andersen
 */
public class Alien {
    public Block rootBlock;
    
    public Alien(Block rootBlock) {
        this.rootBlock = rootBlock;
    }
    
}
