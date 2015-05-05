/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.controller;

import fr.enib.wwwjgrib.model.WindField;
import java.util.EventObject;

/**
 * A WindFieldEvent is used to transmit a windField with the windLayer (the receptor) from the object MapPanel (the source)
 * @author s8lepage
 */
public class WindFieldEvent extends EventObject {

    private WindField windField = null;

    /**
     * 
     * @param source The object on which the Event initially occurred.
     * @param windField The windField to transmit
     */
    public WindFieldEvent(Object source, WindField windField) {
        super(source);
        this.windField = windField;
    }

    /**
     * 
     * @param source
     */
    public WindFieldEvent(Object source) {
        this(source, new WindField());
    }

    /**
     * @return the windField
     */
    public WindField getWindField() {
        return windField;
    }

    /**
     * @param windField the windField to set
     */
    public void setWindField(WindField windField) {
        this.windField = windField;
    }
}
