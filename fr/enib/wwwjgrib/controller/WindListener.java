/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.enib.wwwjgrib.controller;

import java.util.EventListener;

/**
 * The WindListener interface should be implemented for receiving many windField object for displaying
 * in the windLayer.
 * @author s8lepage
 */
public interface WindListener extends EventListener {
    /**
     * This method can be called when a windField is created
     * @param windEvent the event which own a windField
     */
    public void windFieldValueChanged(WindFieldEvent windEvent);
}
