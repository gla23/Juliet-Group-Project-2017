package julietgroupproject;

import java.io.Serializable;

/**
 * Abstract representation of an Alien. The class contains tree-structured
 * blocks representing spatials of body parts with their properties.
 *
 * Note however that these blocks contain (transient) references to the concrete
 * geometries of the instantiated alien in the current simulation, to allow
 * reverse lookup when adding limbs.
 *
 * @author George Andersen
 */
public class Alien implements Serializable {

    public Block rootBlock;  /*
                              * The "body" of the alien.
                              * The root of the tree representing
                              * the entire alien.
                              */

    public int materialCode = 3;      /* Codes the alien's texture.
                                       * 0 = plant, 1 = snake
                                       * 2 = mosaic,  3 = zebra
                                       * TODO make the encoding nicer
                                       */


    public Alien(Block rootBlock) {
        this.rootBlock = rootBlock;
    }

    public int getCode() {
        return materialCode;
    }
}
