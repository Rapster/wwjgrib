/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.enib.wwwjgrib.main;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.ClickAndGoSelectListener;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.util.StatusBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * Panel's application which contains Viperfish and WolrdWind
 * @author Jean-Didier
 */
public class AppPanel extends JPanel{

    private WorldWindowGLCanvas wwd;
    private StatusBar statusBar;
    

    public AppPanel(Dimension canvasSize, boolean includeStatusBar) {

        super(new BorderLayout());
        try {
            javax.swing.UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
            com.birosoft.liquid.LiquidLookAndFeel.setLiquidDecorations(true);
        } catch (Exception e) {
        }
        this.wwd = new WorldWindowGLCanvas();
        this.wwd.setPreferredSize(canvasSize);

        // Create the default model as described in the current worldwind properties.
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        // Setup a select listener for the worldmap click-and-go feature
        this.wwd.addSelectListener(new ClickAndGoSelectListener(this.getWwd(), WorldMapLayer.class));

        this.add(this.wwd, BorderLayout.CENTER);
        if (includeStatusBar) {
            this.statusBar = new StatusBar();
            this.add(statusBar, BorderLayout.PAGE_END);
            this.statusBar.setEventSource(wwd);
        }
    }

    public WorldWindowGLCanvas getWwd() {
        return wwd;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }
}
