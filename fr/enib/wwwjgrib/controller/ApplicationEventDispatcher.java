/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * This <u>Singleton</u> is used to fire an event everywhere in the application
 * @author s8lepage
 */
public class ApplicationEventDispatcher {

    private static ApplicationEventDispatcher appEventDispatcher = null;
    private static List<WindListener> windListeners = null;

    /**
     * Returns a new instance of ApplicationEventDispatcher
     * @return a new instance of ApplicationEventDispatcher if it's not referenced, else returns null
     */
    public static ApplicationEventDispatcher getInstance() {
        if (appEventDispatcher == null) {
            return new ApplicationEventDispatcher();
        } else {
            return null;
        }
    }

    private ApplicationEventDispatcher() {
        windListeners = new ArrayList<WindListener>();
    }

    /**
     * This method add a windListener in the listener's list
     * @param windListener The souscriptor
     */
    public static void addWindListener(WindListener windListener) {
        windListeners.add(windListener);
    }

    /**
     * This method remove a windListener from the listener's list
     * @param windListener The souscriptor
     */
    public static void removeWindListener(WindListener windListener) {
        windListeners.remove(windListener);
    }

    /**
     * This method call <code>windValuesChanged()</code> each <code>WindListener</code> object
     * @param windEv The windFieldEvent
     */
    public static void fireWindEvent(WindFieldEvent windEv) {
        for (WindListener wl : windListeners) {
            wl.windFieldValueChanged(windEv);
        }
    }

    /**
     * @return The windListeners
     */
    public List<WindListener> getWindListeners() {
        return windListeners;
    }
}
