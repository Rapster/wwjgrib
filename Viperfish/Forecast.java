/*
 *  Forecast.java
 *
 *  Created on August 29, 2005, 5:27 PM
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

import java.util.ResourceBundle;
import java.io.File;

import java.text.MessageFormat;

import java.awt.Color;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import Solfin.Tools.MapPoint;
import Solfin.Grib.GribFile;
import Solfin.Grib.GribRecord;

/**
 *
 * @author gabriel
 */
public class Forecast extends JPanel {
    
    Controller dad=null;
    MapPoint refLoc=null;
    
    /** Creates a new instance of Forecast */
    public Forecast(JFrame frame, Controller dad, MapPoint loc) {
        super(); //frame,MessageFormat.format(dad.getBundle().getString("Message.forecastHeader"),loc.toString()));
        this.dad=dad;
        this.refLoc=loc;
        setBackground(Color.white);
        setForeground(Color.black);
//        buildGUI();
//        pack();
        GribRecord gr1=dad.getGribRecord1();
        GribRecord gr2=dad.getGribRecord2();
    }
    
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        System.err.println("in forecast paint");
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setColor(Color.red);
        g2.drawLine(0,0,100,100);
        
        File gFile=dad.getGribFile();
        if (gFile==null)
            return;
        GribFile gf=GribFile.open(gFile);
        if (gf==null)
            return;

        // start with TMP
        
        
    }
    
    
    public static JDialog dlg=null;
    static JDialog showDialog(JFrame frame, Controller dad, MapPoint loc) {
        System.err.println("in showDialog");
        // if no grib data show an error message!
//        if (numPoints==0) {
//            try {
//                JOptionPane.showMessageDialog(frame,
//                        bundle.getString("Message.noRouteData"),
//                        bundle.getString("Message.noRouteHeader"),
//                        JOptionPane.ERROR_MESSAGE);
//            } catch (Exception e) {}
//            return ;
//        }
        
        if (dlg==null) {
            dlg = new JDialog(frame,MessageFormat.format(dad.getBundle().getString("Message.forecastHeader"),loc.toString()));
            dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dlg.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    dlg.dispose();
                    dlg=null;
                    return;
                }
            });
            
            Forecast fct=new Forecast(frame, dad,loc);
            dlg.add(fct);
            // Work around pack() bug on some platforms
            dlg.setSize(new Dimension(800,600));
            //dlg.setSize(dlg.getPreferredSize());
            dlg.setLocationRelativeTo(frame);
            dlg.setVisible(true);
            
            dlg.toFront();
        } else {
            dlg.setVisible(true);
            dlg.toFront();
        }
        return dlg;
    }
    
}
