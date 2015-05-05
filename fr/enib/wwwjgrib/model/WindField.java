/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>WindField</code> represents a collection of winds
 * @author s8lepage
 */
public class WindField {

    private List<Wind> winds = null;

    /**
     * Creates a WindField
     */
    public WindField() {
        winds = new ArrayList<Wind>();
    }

    /**
     * Creates a WindField - Recopy's Constructor
     * @param windField
     */
    public WindField(WindField windField) {
        this.winds = windField.getWinds();
    }

    /**
     *
     * @param wind the wind to be added in the list
     */
    public void addWind(Wind wind) {
        winds.add(wind);
    }

    /**
     *
     * @param wind the wind to be removed from the list
     */
    public void removeWind(Wind wind) {
        winds.remove(wind);
    }

    /**
     * @return the winds
     */
    public List<Wind> getWinds() {
        return winds;
    }

    /**
     * @param winds the winds to set
     */
    public void setWinds(List<Wind> winds) {
        this.winds = winds;
    }
}
