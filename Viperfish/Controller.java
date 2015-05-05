/*
 *  Controller.java
 *
 *  Created on April 9, 2005, 9:36 PM
 *
 *  $Id$
 *
 *
 *     The contents of this file are subject to the Mozilla Public License
 *     Version 1.1 (the "License"); you may not use this file except in
 *     compliance with the License. You may obtain a copy of the License at
 *     http://www.mozilla.org/MPL/
 *
 *     Software distributed under the License is distributed on an "AS IS"
 *     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 *     License for the specific language governing rights and limitations
 *     under the License.
 *
 *     The Original Code is Viperfish.
 *
 *     The Initial Developer of the Original Code is Gabriel Galibourg.
 *     Portions created by Gabriel Galibourg are Copyright (C) 2005-2006
 *     Gabriel Galibourg. All Rights Reserved.
 *
 *     Contributor(s):
 *
 */
package Solfin.Viperfish;

import java.io.*;
import java.util.*;
import java.util.Locale;
import java.util.ResourceBundle;

import java.awt.*;
import java.awt.event.ActionEvent;

import java.text.MessageFormat;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import Solfin.Tools.*;
import Solfin.Grib.GribRecord;
/*
 * WARNING: the following import statement means I can use mathematical
 * functions directly without prefixing them with "Math."
 * ie I can now write abs(var) instead of Math.abs(var)
 *
 * Don't abuse such import statements, it makes code hard to read. I only
 * use it for Math.*, and for methods only (not constants).
 */
import static java.lang.Math.*;

/**
 * The controller  class acts as a hot potato message router. Any object
 * (menu bar, map, navigator) wishing to communicate with another object
 * MUST do it through the controller - this keeps the code clean of inter-object
 * communication which is always a source of bugs and weird behaving user
 * interfaces.
 *
 * @author gabriel
 */
public class Controller {
    // keep here a copy of all instances

    private JFrame frame = null;
    private JLabel label = null;
    private MapPanel mapPanel = null;
    private Navigator navigator = null;
    private AppMenu appMenu = null;
    private JMenuBar menuBar = null;
    private JSplitPane splitPane = null;
    private JScrollPane treeView = null;
    private Container container = null;
    private ResourceBundle bundle = null;
    private JMenu closeSubMenu = null;
    private Route route = null;
    private File fileChooserDir = new File(System.getProperty("user.dir"));
    private final static String RESOURCES = "Solfin.Viperfish.GUI";

    public Controller(String[] fileNames, JPanel jpanel) {

        bundle = ResourceBundle.getBundle(RESOURCES, Locale.getDefault());

        //Suggest that the L&F (rather than the system)
        //decorate all windows.  This must be invoked before
        //creating the JFrame.  Native look and feels will
        //ignore this hint.
        JFrame.setDefaultLookAndFeelDecorated(false);

        //Create and set up the window.
        frame = new JFrame("Viperfish");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setBackground(new Color(100,0,0));

        //frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        //JLabel emptyLabel = new JLabel("");
        //emptyLabel.setPreferredSize(new Dimension(175, 100));
        //frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);

        // create the controller. The controller is the "routing" class,
        // ie all children interface to the controller which then dispatches
        // "events" to the other children when necessary.
        //Controller controller = new Controller();

        // now create all the window objects:


        container = frame.getContentPane();
        container.setLayout(new BorderLayout());

        // the GRIB navigator
        navigator = new Navigator(this);
        //Create the scroll pane and add the tree to it.
        treeView = new JScrollPane(navigator);


        //BoundaryBox visible = new BoundaryBox(80F,-80F,-179.99999F,180F);
        mapPanel = new MapPanel(this); //,visible);
        mapPanel.setPreferredSize(new Dimension(400, 200));
        mapPanel.setMinimumSize(new Dimension(400, 200));
        //controller.setMap(map);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeView, mapPanel);
        splitPane.setResizeWeight(0.1);
        container.add(splitPane, BorderLayout.CENTER);
        jpanel.add(splitPane, BorderLayout.CENTER);

        label = new JLabel("This is the latitude/longitude.");
        label.setLabelFor(mapPanel);
        container.add(label, BorderLayout.PAGE_END);
        //controller.setLabel(label);
        jpanel.add(label, BorderLayout.PAGE_END);
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);


        //Display the window.
        frame.pack();
        frame.setSize(new Dimension(1500, 1000));
        frame.setVisible(false);

        KeyStroke[] mapKey = splitPane.getInputMap().keys();
        for (int ii = 0; mapKey != null && ii < mapKey.length; ++ii) {
            System.err.println("key[]" + ii + "=  " + mapKey[ii].toString());
        }

        // Create all the actions
        ActionMap amap = treeView.getActionMap(); //frame.getRootPane().getActionMap();

        amap.put("OpenGribFile", new OpenGribFile());
        amap.put("CloseGribFile", new CloseGribFile());
        amap.put("Animate", new Animate());
        amap.put("Print", new Print());
        amap.put("PageSetup", new PageSetup());
        amap.put("ZoomIn", new ZoomIn());
        amap.put("ZoomOut", new ZoomOut());
        amap.put("ZoomReset", new ZoomReset());
        amap.put("RouteOpen", new RouteOpen());
        amap.put("RouteSave", new RouteSave());
        amap.put("RouteClear", new RouteClear());
        amap.put("RouteShow", new RouteShow());
        amap.put("WaypointRemove", new WaypointRemove());
        amap.put("WaypointMove", new WaypointMove());
        amap.put("WaypointInsertBefore", new WaypointInsertBefore());
        amap.put("RunGarbageCollection", new RunGarbageCollection());
        amap.put("ColorBrighter", new ColorBrighter());
        amap.put("ColorDarker", new ColorDarker());
        amap.put("Forecast", new Forecast());
        amap.put("Exit", new Exit());
        amap.put("About", new AboutAction());


        // the menu bar
        appMenu = new AppMenu(bundle, amap);
        menuBar = appMenu.createMenuBar("MenuBar");
        frame.setJMenuBar(menuBar);
        // find the close grib file sub menu ....
        // Find the marker.
        JMenuItem mi = findAMenuItem(menuBar, "@GribCloseSubMenu@");
        if (mi != null) {
            Accessible parent = mi.getAccessibleContext().getAccessibleParent();
            if (parent instanceof JMenu) {
                closeSubMenu = (JMenu) parent;
                closeSubMenu.remove(mi);
            }
        }




        // Map keystrokes to actions
        InputMap imap = treeView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT); //frame.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        //imap.put(KeyStroke.getKeyStroke("ctrl O"),"OpenGribFile");
        imap.put(KeyStroke.getKeyStroke("ctrl P"), "Print");
        //imap=frame.getRootPane().getInputMap();
        //imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,InputEvent.SHIFT_MASK),"ZoomIn");
        imap.put(KeyStroke.getKeyStroke('+'), "ZoomIn");
        imap.put(KeyStroke.getKeyStroke('-'), "ZoomOut");
        //imap.setParent(null);

        if (fileNames != null) {
            for (int i = 0; i < fileNames.length; ++i) {
                new OpenGribFile(new File(fileNames[i]));
            //openGribFile(new File(fileNames[i]));
            }
        }

    }

    private JMenuItem findAMenuItem(JMenuBar menuBar, String textToFind) {
        int mc = menuBar.getMenuCount();
        for (int i = 0; i < mc; i++) {
            JMenuItem mi = findAMenuItem(menuBar.getMenu(i), textToFind);
            if (mi != null) {
                return mi;
            }
        }
        return null;
    }

    private JMenuItem findAMenuItem(JMenu m, String textToFind) {
        int ic = m.getItemCount();
        for (int j = 0; j < ic; j++) {
            JMenuItem mi = m.getItem(j);
            if (mi != null) {
                String s = mi.getText();
                if (s != null && s.equals(textToFind)) {
                    return mi;
                }
                if (mi instanceof JMenu) {
                    JMenuItem retVal = findAMenuItem((JMenu) mi, textToFind);
                    if (retVal != null) {
                        return retVal;
                    }
                }
            }
        }
        return null;
    }

    public ActionMap getActionMap() {
        return treeView.getActionMap();
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Route getRoute() {
        if (route == null) {
            route = new Route(bundle);
        }

        return route;
    }

    public void showAllKeys(JComponent c) {
        KeyStroke[] mapKey;
        System.err.println("All Keys:");
        //WHEN_IN_FOCUSED_WINDOW, WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        System.err.println("  JComponent.WHEN_FOCUSED:");
        mapKey = c.getInputMap(JComponent.WHEN_FOCUSED).allKeys();
        for (int ii = 0; mapKey != null && ii < mapKey.length; ++ii) {
            System.err.println("    key[" + ii + "]=  " + mapKey[ii].toString());
        }
        System.err.println("  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT:");
        mapKey = c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).allKeys();
        for (int ii = 0; mapKey != null && ii < mapKey.length; ++ii) {
            System.err.println("    key[" + ii + "]=  " + mapKey[ii].toString());
        }
        System.err.println("  JComponent.WHEN_IN_FOCUSED_WINDOW:");
        mapKey = c.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).allKeys();
        for (int ii = 0; mapKey != null && ii < mapKey.length; ++ii) {
            System.err.println("    key[" + ii + "]=  " + mapKey[ii].toString());
        }

    }

    public void updateLAF() {
        //System.err.println("Changing LAF");

        showAllKeys(treeView);
//
//        try {
//            // Change the look and feel to GTK+
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
//            //UIManager.getCrossPlatformLookAndFeelClassName());
//
//            //UIManager.setLookAndFeel("toto");
//            SwingUtilities.updateComponentTreeUI(frame);
//            //frame.pack();
//        } catch (Exception exc) {
//        }
    }

    public void LAFBrightness(float newAlpha) {
        Config.setAlpha(newAlpha);

        // Now update all colors.
        UIDefaults uiDefaults = UIManager.getDefaults();
        //uiDefaults.put("TextArea.background",new  Color(200,0,0));
        //uiDefaults.put("TextArea.background",uiDefaults.get("OptionPane.errorDialog.border.background"));
        Enumeration uiKeys = uiDefaults.keys();
        while (uiKeys.hasMoreElements()) {
            Object key = uiKeys.nextElement();
            Object val = uiDefaults.get(key);
            if (val != null && val instanceof Color) {
                //if (key.toString().endsWith("background")) {
                uiDefaults.put(key, Config.darkerColor(uiDefaults.getColor(key)));
                val = uiDefaults.get(key);
                System.out.println("[" + key.toString() + "]:[" +
                        (null != val ? val.toString() : "(null)") +
                        "]");
            }
        }
        SwingUtilities.updateComponentTreeUI(frame);
    }

    public void repaint() {
        mapPanel.repaint();
    }

    public void updateLabel(String str) {
        label.setText(str);
    }

    public void updateVisible(BoundaryBox visible) {
        System.err.println("Visible=" + visible.toString());
        mapPanel.changeBoundaryBox(visible);
    }
    File currentFile = null;

    public File getGribFile() {
        return currentFile;
    }

    public void loadGribFileIntoMap(File gribFile, int recordId1, int recordId2) {
        currentFile = gribFile;
        mapPanel.setGRIBData(gribFile, recordId1, recordId2);
    }

    public void openGribFile(File GribFile) {
        // ideally it would be preferable to remove this method and just create event
        // that would be handled by the event handler below (OpenGribFile class).
        if (GribFile != null) {
            new OpenGribFile(null);
        } else {
            new OpenGribFile(GribFile);
        }
    }

    public GribRecord getGribRecord1() {
        return mapPanel.gr1;
    }

    public GribRecord getGribRecord2() {
        return mapPanel.gr2;
    }

//--------------
//
//  All actions are here ....
//
    public class ZoomIn extends AbstractAction {

        public ZoomIn() {
        }

        public void actionPerformed(ActionEvent e) {
            String actionCmd = e.getActionCommand();
            System.err.println("Zoom In");
            updateVisible(mapPanel.zoomIn());
            mapPanel.repaint();
//                MapPoint mPos=mapPanel.getMousePosAsLL();
//                float w=mapPanel.visible.getWidth()*0.7f;
//                float h=mapPanel.visible.getHeight()*0.7f;
//                //BoundaryBox newVis=new BoundaryBox(mPos.getLatitude(),mPos.getLatitude()-30f,mPos.getLongitude(),mPos.getLongitude()+100f);
//                updateVisible(BoundaryBox.centered(mPos,w,h));
//                mapPanel.repaint();
        }
    }

    public class ZoomOut extends AbstractAction {

        public ZoomOut() {
        }

        public void actionPerformed(ActionEvent e) {
            System.err.println("Zoom Out");
            MapPoint mPos = mapPanel.getMouseMapPos();
            float w = (float) min(160.0, mapPanel.visible.getWidth() * 1.3f);
            float h = (float) min(360.0, mapPanel.visible.getHeight() * 1.3f);
            //BoundaryBox newVis=new BoundaryBox(mPos.getLatitude(),mPos.getLatitude()-30f,mPos.getLongitude(),mPos.getLongitude()+100f);
            updateVisible(BoundaryBox.centered(mPos, w, h));
            mapPanel.repaint();
        }
    }

    public class ZoomReset extends AbstractAction {

        public ZoomReset() {
        }

        public void actionPerformed(ActionEvent e) {
            mapPanel.zoomReset();
        }
    }

    public class Print extends AbstractAction {

        public Print() {
        }

        public void actionPerformed(ActionEvent e) {
            mapPanel.print();
        }
    }

    public class PageSetup extends AbstractAction {

        public PageSetup() {
        }

        public void actionPerformed(ActionEvent e) {
            mapPanel.pageSetup();
        }
    }

    public class OpenGribFile extends AbstractAction {

        public OpenGribFile() {
        }

        public OpenGribFile(File gribFile) {
            if (gribFile == null) {
                selectFileFromChooser();
            } else {
                openTheFile(gribFile);
            }
        }

        public void actionPerformed(ActionEvent e) {
            selectFileFromChooser();
        }

        private void selectFileFromChooser() {
            JFileChooser chooser = new JFileChooser(fileChooserDir);
            FileFilter filter = new FileFilter() {

                public String getDescription() {
                    return bundle.getString("Message.GRIBFiles");
                }

                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String fn = f.getName();
                    if (fn.endsWith(".grb") || fn.endsWith(".grb.bz2") || fn.endsWith("grb.gz")) {
                        return true;
                    }

                    return false;
                }
            };
            chooser.setFileFilter(filter);
            chooser.setPreferredSize(new Dimension(700, 950));
            int returnVal = chooser.showOpenDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                openTheFile(chooser.getSelectedFile());
                //openGribFile(chooser.getSelectedFile());
                fileChooserDir = chooser.getCurrentDirectory();
            }
        }

        private void openTheFile(File gribFile) {
            if (!gribFile.isAbsolute()) {
                gribFile = gribFile.getAbsoluteFile();
            }
            if (navigator.addGribFile(gribFile) == true) {
                if (closeSubMenu != null) {
                    // add this file to MenuBar
                    JMenuItem menuItem = new JMenuItem();
                    menuItem.setAction(getActionMap().get("CloseGribFile"));
                    menuItem.setText(gribFile.getAbsolutePath());
                    closeSubMenu.add(menuItem);
                }
            } else {
                try {
                    //String format=bundle.getString("Message.badGRIBFile");
                    JOptionPane.showMessageDialog(null,
                            MessageFormat.format(bundle.getString("Message.badGRIBFile"), gribFile.getCanonicalPath()),
                            bundle.getString("Message.badFile"),
                            JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                }
            }
        }
    }

    public class CloseGribFile extends AbstractAction {

        public CloseGribFile() {
        }

        public void actionPerformed(ActionEvent e) {
            JMenuItem source = (JMenuItem) (e.getSource());
            String gribFileName = source.getText();

            String actionCmd = e.getActionCommand();

            if (closeSubMenu != null) {
                //appMenu.removeGribFile(source.getText());
                navigator.removeGribFile(gribFileName);
                for (Component m : closeSubMenu.getMenuComponents()) {
                    if (m instanceof JMenuItem) {
                        JMenuItem mi = (JMenuItem) m;
                        System.out.println(mi.getText());
                        if (gribFileName.equals(mi.getText())) {
                            closeSubMenu.remove(mi);
                        }
                    }
                }
            }
            return;
        }
    }

    public class Animate extends AbstractAction {

        public Animate() {
        }

        public void actionPerformed(ActionEvent e) {
            navigator.animate();
        }
    }

    public class RouteOpen extends AbstractAction {

        public RouteOpen() {
        }

        public void actionPerformed(ActionEvent e) {
            Route newRoute = Route.OpenDialog(frame, bundle);
            if (newRoute != null) {
                route = newRoute;
                mapPanel.clearRoute();
            }
        }
    }

    public class RouteSave extends AbstractAction {

        public RouteSave() {
        }

        public void actionPerformed(ActionEvent e) {
            if (route != null) {
                route.SaveDialog(frame, bundle);
            //mapPanel.clearRoute();
            }
        }
    }

    public class RouteClear extends AbstractAction {

        public RouteClear() {
        }

        public void actionPerformed(ActionEvent e) {
            if (route != null) {
                route.clear();
                mapPanel.clearRoute();
            }
        }
    }

    public class RouteShow extends AbstractAction {

        public RouteShow() {
        }

        public void actionPerformed(ActionEvent e) {
            if (route != null) {
                route.showDialog(frame);
            }
        }
    }

    public class WaypointRemove extends AbstractAction {

        public WaypointRemove() {
        }

        public void actionPerformed(ActionEvent e) {
            System.err.println("e=" + e.toString());
            Point mousePos = mapPanel.getMousePos();
            route.remove(mousePos, mapPanel.mg);
            frame.repaint();
        }
    }

    public class WaypointMove extends AbstractAction {

        public WaypointMove() {
        }

        public void actionPerformed(ActionEvent e) {
            mapPanel.waypointMove();
        }
    }

    public class WaypointInsertBefore extends AbstractAction {

        public WaypointInsertBefore() {
        }

        public void actionPerformed(ActionEvent e) {
            mapPanel.waypointInsertBefore();
        }
    }

    public class Forecast extends AbstractAction {

        public Forecast() {
        }

        public void actionPerformed(ActionEvent e) {
            Solfin.Viperfish.Forecast.showDialog(frame, Controller.this, mapPanel.getMouseMapPos());
        }
    }

    public class RunGarbageCollection extends AbstractAction {

        public RunGarbageCollection() {
        }

        public void actionPerformed(ActionEvent e) {
            Runtime.getRuntime().gc();
            return;
        }
    }

    public class ColorBrighter extends AbstractAction {

        public ColorBrighter() {
        }

        public void actionPerformed(ActionEvent e) {
            float alpha = Config.getAlpha();
            alpha = (float) max(1.0f, alpha += 0.3f);
            LAFBrightness(alpha);
            return;
        }
    }

    public class ColorDarker extends AbstractAction {

        public ColorDarker() {
        }

        public void actionPerformed(ActionEvent e) {
            float alpha = Config.getAlpha();
            alpha = (float) min(1.0f, alpha -= 0.3f);
            LAFBrightness(alpha);
            return;
        }
    }

    public class Exit extends AbstractAction {

        public Exit() {
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
            return;
        }
    }

    /**
     * To show the about dialog
     */
    public class AboutAction extends AbstractAction {

        public AboutAction() {
            System.err.println("in creator of AboutAction");
        }

        public void actionPerformed(ActionEvent e) {
            System.err.println("in actionPerformed of AboutAction");
            About dlg = new About(frame); //JSVGViewerFrame.this);
            // Work around pack() bug on some platforms
            dlg.setSize(dlg.getPreferredSize());
            dlg.setLocationRelativeTo(frame);
            dlg.setVisible(true);
            dlg.toFront();
        }
    }
}
