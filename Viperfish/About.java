/*
 *  About.java
 *
 *  Created on August 25, 2005, 6:49 PM
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.net.URL;

import javax.swing.JWindow;
import javax.swing.*;
import javax.swing.border.BevelBorder;





/**
 *
 * @author gabriel
 */

public class About extends JWindow {
    
    private final static String ICON_SPLASH="Solfin/Viperfish/viperfish.gif";
    
    public About() {
        super();
        buildGUI();
    }
    public About(Frame owner){
        super(owner);
        buildGUI();
        
        addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    setVisible(false);
                    dispose();
                }
            }
        });
        
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                setVisible(false);
                dispose();
            }
        });
    }
    
    public void setLocationRelativeTo(Frame f) {
        Dimension invokerSize = f.getSize();
        Point loc = f.getLocation();
        Point invokerScreenLocation = new Point(loc.x, loc.y);
        
        Rectangle bounds = getBounds();
        int  dx = invokerScreenLocation.x+((invokerSize.width-bounds.width)/2);
        int  dy = invokerScreenLocation.y+((invokerSize.height - bounds.height)/2);
        Dimension screenSize = getToolkit().getScreenSize();
        
        if (dy+bounds.height>screenSize.height) {
            dy = screenSize.height-bounds.height;
            dx = invokerScreenLocation.x<(screenSize.width>>1) ? invokerScreenLocation.x+invokerSize.width :
                invokerScreenLocation.x-bounds.width;
        }
        if (dx+bounds.width>screenSize.width) {
            dx = screenSize.width-bounds.width;
        }
        
        if (dx<0) dx = 0;
        if (dy<0) dy = 0;
        setLocation(dx, dy);
    }
    
    
    
    protected void buildGUI() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.white);
        
        ClassLoader cl = this.getClass().getClassLoader();
        
        //
        // Add splash image
        //
        URL url = cl.getResource(ICON_SPLASH);
        panel.add(BorderLayout.CENTER, new JLabel(new ImageIcon(url)));
        
        //
        // Add exact revision information
        //
        String tagName = "Viperfish version 0.1"; //Version.getVersion();
        
        panel.add(BorderLayout.SOUTH, new JLabel(tagName, SwingConstants.RIGHT));
        
        setBackground(Color.white);
        getContentPane().setBackground(Color.white);
        
        ((JComponent)getContentPane()).setBorder
                (BorderFactory.createCompoundBorder
                (BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.gray, Color.black),
                BorderFactory.createCompoundBorder
                (BorderFactory.createCompoundBorder
                (BorderFactory.createEmptyBorder(3, 3, 3, 3),
                BorderFactory.createLineBorder(Color.black)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10))));
        
        
        getContentPane().add(panel);
        
        
        pack();
    }
    
}
