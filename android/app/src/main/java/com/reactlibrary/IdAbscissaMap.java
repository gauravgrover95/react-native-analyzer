package com.reactlibrary;

/**
 * Created by abdulfatir on 13/08/16.
 * A helper model to sort detected blobs by x co-ordinate.
 */

// IdAbscissaMap class.. What is Id, What is Abcissa and What is Map?
public class IdAbscissaMap implements Comparable {
    private int id;
    private int abscissa;

    /**
     * Instantiates a new Id abscissa map.
     *
     * @param abscissa the abscissa
     * @param id       the id
     */
     // This is simply a constructor
    public IdAbscissaMap(int abscissa, int id) {
        this.abscissa = abscissa;
        this.id = id;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
     // getter for id
    public int getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
     // setter for id
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets abscissa.
     *
     * @return the abscissa
     */
     // getter for Abcissa
    public double getAbscissa() {
        return abscissa;
    }

    /**
     * Sets abscissa.
     *
     * @param abscissa the abscissa
     */
     // setter for Abscissa
    public void setAbscissa(int abscissa) {
        this.abscissa = abscissa;
    }

    // Now this is a method named, 'comparedTo'. 
    // I think this is for the object of IdAbscissaMap to compare to some generic 'Object' o
    // What does it compare, lets take a look by diving into the code.
    @Override
    public int compareTo(Object o) {
        // Basically first we are trying to typecase the generic object to the IdAbscissa
        // I have a doubt, why didn't we used an idabscissamap object in the first place.
        IdAbscissaMap that = (IdAbscissaMap) o;
        // then we are symply comparing the abscissa values and returning the results
        // that was super easy.
        if (getAbscissa() > that.getAbscissa())
            return 1;
        else if (getAbscissa() < that.getAbscissa())
            return -1;
        return 0;
    }

    @Override
    // next is a simple toString() function that is 
    public String toString() {
        return "{id:" + id + ", X:" + abscissa + "}";
    }
}
