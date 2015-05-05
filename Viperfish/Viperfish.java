/*
 *  Viperfish.java
 *
 *  Created on May 10, 2005, 7:55 PM
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

import java.util.Enumeration;
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.net.URL;

import Solfin.Tools.*;

/**
 *
 * @author gabriel
 */
public class Viperfish {
    static String[] myArgs;
    
    public static void main(String[] args) {
        
        myArgs=args;
        UIManager.LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < info.length; i++) {
            System.out.println(info[i].toString());
        }
        
        
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Controller(myArgs,null);
            }
        });
    }
}

