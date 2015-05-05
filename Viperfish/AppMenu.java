/*
 *  AppMenu.java
 *
 *  Created on May 2, 2005, 10:03 PM
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

import java.lang.Exception;
import java.io.File;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
//import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import Solfin.Tools.Config;

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
 *
 * @author gabriel
 */
public class AppMenu {
    ActionMap aMap=null;
    ResourceBundle bundle;
    
    private static final String ITEMS=".items";
    private static final String ACTION=".action";
    private static final String ACTIONCMD=".actioncmd";
    private static final String TEXT=".text";
    private static final String TYPE=".type";
    private static final String MNEMONIC=".mnemonic";
    private static final String ACCELERATOR=".accelerator";
    
    
    
//    public AppMenu(Controller dad,ResourceBundle bundle) {
    public AppMenu(ResourceBundle bundle,ActionMap aMap) {
        //this.dad=dad;
        this.aMap=aMap;
        this.bundle = bundle;
    }
    
    public JMenuBar createMenuBar(String name) {
        
        JMenuBar menuBar=new JMenuBar();
        
        String[] sa=bundle.getString(name).split(" +");
        System.err.println("sa="+sa);
        for (String s : sa ) {
            JComponent c=createComponent(s);
            if (c!=null)
                menuBar.add(c);
        }
        
        return menuBar;
    }
    
    public JPopupMenu createPopupMenu(String name) {
        JPopupMenu popupMenu=new JPopupMenu();
        
        String[] sa=bundle.getString(name).split(" +");
        System.err.println("sa="+sa);
        for (String s : sa ) {
            JComponent c=createComponent(s);
            if (c!=null)
                popupMenu.add(c);
        }
        
        return popupMenu;
    }
    
    private JComponent createComponent(String name) {
        JMenuItem component=null;
        JMenu menu=null;
        try {
            if (name.equals("-")) {
                return new JSeparator();
            }
            
            String type=bundle.getString(name+TYPE);
            if (type==null)
                return null;
            
            // first find the text for the component, if none found return null
            String text=bundle.getString(name+TEXT);
            if (text==null)
                return null;
            
            if (type.equals("ITEM")) {
                component=new JMenuItem();//name);
            } else if (type.equals("MENU")) {
                menu=new JMenu();
                component=menu;
            } else {
                System.err.printf("ERROR: Type (%s) for menu component '%s' is unknown!\n",type, name);
                return null;
            }
            
            // set action for item if there is one...
            String s=null;
            try {
                s=bundle.getString(name+ACTION);
            } catch (Exception e) { s=null; }
            if (s!=null)
                component.setAction(aMap.get(s));
            
            // then set the text (saved above)
            if (text!=null) {
                component.setText(text);
            }
            
            // set mnemonic
            s=null;
            try {
                s=bundle.getString(name+MNEMONIC);
            } catch (Exception e) { s=null; }
            if (s!=null)
                component.setMnemonic(s.codePointAt(0));
            
            
            // set accelerator
            s=null;
            try {
                s=bundle.getString(name+ACCELERATOR);
            } catch (Exception e) { s=null; }
            if (s!=null) {
                KeyStroke k=KeyStroke.getKeyStroke(s);
                if (k!=null) {
                    component.setAccelerator(KeyStroke.getKeyStroke(s));
                } else {
                    System.err.printf("ERROR: Accelerator '%s' for component '%s' is badly formatted!\n", s,name);
                }
            }
            
            if (type.equals("MENU")) {
                // set the items of the menu ....
                String items=bundle.getString(name+ITEMS);
                if (items==null || items.equals(""))
                    return component;
                //String[] sa=items.split(" +");
                
                for (String s2 : items.split(" +")) {
                    JComponent c=createComponent(s2);
                    if (c!=null)
                        menu.add(c);
                }
            }
            
            //System.err.println("Action Command="+component.getClientProperty("secret"));
            return component;
            
        } catch (Exception e) {
            System.err.println("Received exception: "+e);
        }
        
        return null;
    }
    
}
