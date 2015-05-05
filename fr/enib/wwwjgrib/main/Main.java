/*
 * From
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
 */
package fr.enib.wwwjgrib.main;

import Solfin.Viperfish.Controller;
import fr.enib.wwwjgrib.controller.ApplicationEventDispatcher;
import fr.enib.wwwjgrib.view.WindLayer;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import gov.nasa.worldwind.examples.util.LayerManagerLayer;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.Mercator.examples.VirtualEarthLayer;
import javax.swing.*;
import java.awt.*;

/**
 * Test's class
 * @author Serge Morvan
 * @version $Id: Lancement min-proj S6I $
 */
public class Main {

    private static Controller controller = null;


    static {
        if (Configuration.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "World Wind Tabbed Pane Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static class WWJPanel extends JPanel {

        protected WorldWindowGLCanvas wwd;
        protected StatusBar statusBar;

        public WWJPanel(Dimension canvasSize, boolean includeStatusBar) {
            super(new BorderLayout());

            this.wwd = new WorldWindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            this.add(this.wwd, BorderLayout.CENTER);
            if (includeStatusBar) {
                this.statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                this.statusBar.setEventSource(wwd);
            }
        }
    }

    public static void main(String[] args) {
        try {
            JFrame mainFrame = new JFrame();
            ApplicationEventDispatcher.getInstance();
            WindLayer windLayer = new WindLayer();
            JMenu jMenu = new JMenu();
            JMenuBar jBar = new JMenuBar();
            JMenuItem jItemOpen = new JMenuItem();

            jMenu.setText("File");
            jItemOpen.setText("Open...");
            jMenu.add(jItemOpen);

            jBar.add(jMenu);
            mainFrame.setJMenuBar(jBar);

            mainFrame.setTitle("Mini-projet S6I");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final JTabbedPane tabbedPane = new JTabbedPane();
            final WWJPanel wwjPanel = new WWJPanel(new Dimension(800, 600), true);
            final JPanel controlPanel = new JPanel(new BorderLayout());

            tabbedPane.add("WWJ", wwjPanel);
            tabbedPane.add("Grib", controlPanel);

            controller = new Controller(args, controlPanel);
            jItemOpen.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jItemOpenClicked(evt);
                }
            });
            //Insert the layers
            VirtualEarthLayer virtualEarthLayer = new VirtualEarthLayer();
            LayerManagerLayer layerManager = new LayerManagerLayer(wwjPanel.wwd);
            insertBeforeCompass(wwjPanel.wwd, virtualEarthLayer);
            insertBeforeCompass(wwjPanel.wwd, windLayer);
            insertBeforeCompass(wwjPanel.wwd, layerManager);
            mainFrame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
            mainFrame.pack();
            mainFrame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer) {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers) {
            if (l instanceof CompassLayer) {
                compassPosition = layers.indexOf(l);
            }
        }
        layers.add(compassPosition, layer);
    }

    private static void jItemOpenClicked(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        controller.openGribFile(chooser.getSelectedFile());
    }
}
