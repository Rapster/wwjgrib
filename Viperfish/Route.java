/*
 *  Route.java
 *
 *  Created on August 25, 2005, 9:57 AM
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


import java.lang.Integer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.TreeMap;
import java.util.ResourceBundle;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.text.MessageFormat;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;



import Solfin.Tools.Config;
import Solfin.Tools.MapPoint;
import Solfin.Tools.MapGraphics;
import Solfin.Tools.Latitude;
import Solfin.Tools.DegreesEditor;
import Solfin.Tools.Speed;

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
public class Route {
    
    //TreeMap<Integer,MapPoint> points=new TreeMap<Integer,MapPoint>();
    MapPoint[] points=null;
    int numPoints=0;
    ResourceBundle bundle;
    RouteDialog dlg=null;
    static private File fileChooserDir= new  File(System.getProperty("user.dir"));
    
    /** Creates a new instance of Route */
    public Route(ResourceBundle bundle) {
        this.bundle=bundle;
    }
    
    public Route(File routeFile, ResourceBundle bundle) {
        this.bundle=bundle;
        try {
            FileInputStream fis = new FileInputStream(routeFile);
            DataInputStream dis = new DataInputStream(fis);
            
            System.err.println("before while()");
            while (true) {
                try {
                    System.err.println("before readObject");
                    double lat=dis.readDouble();
                    double lon=dis.readDouble();
                    MapPoint mp=new MapPoint(lon,lat);
                    System.err.println("mp is="+mp.toString());
                    add(mp);
                } catch (IOException e) {
                    break;
                } catch (Exception e) {
                    System.err.println("Exception is "+e);
                }
            }
            System.err.println("end of while");
            dis.close();
            fis.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found exception!");
            return;
        } catch (IOException e) {
            System.err.println("IO Exception");
            return;
        }
        System.err.println("End of constructir");
    }
    
    static public Route OpenDialog(JFrame frame,ResourceBundle bundle) {
        final ResourceBundle sBundle=bundle;
        JFileChooser chooser = new JFileChooser(fileChooserDir);
        FileFilter filter = new FileFilter() {
            public String getDescription() {
                return sBundle.getString("Message.RouteFiles");
            }
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String fn=f.getName();
                if (fn.endsWith(".wpt"))
                    return true;
                
                return false;
            }
        };
        chooser.setFileFilter(filter);
        chooser.setPreferredSize(new Dimension(700,950));
        int returnVal = chooser.showOpenDialog(frame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            //openTheFile(chooser.getSelectedFile());
            //openGribFile(chooser.getSelectedFile());
            fileChooserDir = chooser.getCurrentDirectory();
            return new Route(chooser.getSelectedFile(),bundle);
        }
        return null;
    }
    
    public void SaveDialog(JFrame frame,ResourceBundle bundle) {
        final ResourceBundle sBundle=bundle;
        JFileChooser chooser = new JFileChooser(fileChooserDir);
        FileFilter filter = new FileFilter() {
            public String getDescription() {
                return sBundle.getString("Message.RouteFiles");
            }
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String fn=f.getName();
                if (fn.endsWith(".wpt"))
                    return true;
                
                return false;
            }
        };
        chooser.setFileFilter(filter);
        chooser.setPreferredSize(new Dimension(700,950));
        int returnVal = chooser.showSaveDialog(frame);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            //openTheFile(chooser.getSelectedFile());
            //openGribFile(chooser.getSelectedFile());
            fileChooserDir = chooser.getCurrentDirectory();
            File routeFile = chooser.getSelectedFile();
            try {
                FileOutputStream fos = new FileOutputStream(routeFile);
                DataOutputStream dos = new DataOutputStream(fos);
                
                for (int i=0 ; i<numPoints ; ++i) {
                    dos.writeDouble(points[i].getLatitude().value());
                    dos.writeDouble(points[i].getLongitude().value());
                }
                dos.close();
                fos.close();
            } catch (Exception e) {
                return;
            }
            
            return;
        }
        return;
    }
    
    private void addOneElementToPoints() {
        if ( (numPoints%10) == 0 ) {
            MapPoint[] newPoints=new MapPoint[numPoints+10];
            for (int i=0 ; i<numPoints ; ++i) {
                newPoints[i]=points[i];
            }
            points=newPoints;
        }
        
    }
    public void add(MapPoint p) {
        if (p==null) return;
        addOneElementToPoints();
        points[numPoints++]=p;
    }
    public void addBefore(int idx,MapPoint p) {
        if (p==null) return;
        addOneElementToPoints();
        for (int i=numPoints ; i>idx ; --i) {
            points[i]=points[i-1];
        }
        points[idx]=p;
        ++numPoints;
        
    }
    /**
     * Remove the <tt>idx</tt>'th waypoint in the route.
     * First waypoint in route has index 0.
     * If <tt>idx</tt> references an invalid index (negative or too high) this
     * method returns silently without performing anything.
     */
    public void remove(int idx) {
        if (idx<0 || idx>=numPoints)
            return;
        for (int i=idx+1 ; i<numPoints ; ++i) {
            points[i-1]=points[i];
        }
        --numPoints;
    }
    /**
     * Remove the route point closest to the MapPoint <tt>point</tt>.
     */
    public void remove(MapPoint point) {
        int closest=closest(point);
        if (closest>=0) {
            remove(closest);
        }
    }
    public void remove(Point point,MapGraphics mg) {
        int closest=closest(point, mg);
        if (closest>=0) {
            remove(closest);
        }
        
    }
    public int closest(MapPoint point) {
        int closestIdx=-1;
        double distance=Double.MAX_VALUE;
        for (int i=0 ; i<numPoints ; ++i) {
            MapPoint p=points[i];
            double d=MapPoint.distanceMeeus(p, point).value();
            if (d<distance) {
                distance=d;
                closestIdx=i;
            }
        }
        return closestIdx;
    }
    public int closest(Point point, MapGraphics mg) {
        int closestIdx=-1;
        double minDist=Double.MAX_VALUE;
        for (int i=0 ; i<numPoints ; ++i) {
            Point p=mg.toPoint(points[i]);
            if (p!=null) {
                double dist=sqrt( pow(1.0*p.x-point.x,2) + pow(1.0*p.y-point.y,2) );
                if (dist < minDist) {
                    closestIdx=i;
                    minDist=dist;
                }
            }
        }
        return closestIdx;
    }
    public double closestDistance(MapPoint point) {
        double distance=Double.MAX_VALUE;
        int closest=closest(point);
        if (closest>=0) {
            distance=MapPoint.distanceMeeus(point, points[closest]).value();
        }
        return distance;
    }
    public double closestDistance(Point point, MapGraphics mg) {
        double minDist=Double.MAX_VALUE;
        int closestIdx=closest(point, mg);
        if (closestIdx>=0) {
            Point p=mg.toPoint(points[closestIdx]);
            if (p!=null) {
                double dist=sqrt( pow(1.0*p.x-point.x,2) + pow(1.0*p.y-point.y,2) );
                if (dist < minDist) {
                    minDist=dist;
                }
            }
        }
        return minDist;
    }
    
    public MapPoint getWaypoint(int idx) {
        if (idx<0 || idx>=numPoints)
            return null;
        return points[idx];
    }
    public Point getWaypoint(int idx,MapGraphics mg) {
        if (idx<0 || idx>=numPoints)
            return null;
        return mg.toPoint(points[idx]);
    }
    public MapPoint getFirstWaypoint() {
        return (numPoints>0 ? points[0] : null);
    }
    public MapPoint getLastWaypoint() {
        return (numPoints>0 ? points[numPoints-1] : null);
    }
    
    public MapPoint getBoatEstPos(Calendar atTime) {
        MapPoint boatPos=new MapPoint();
        double boatSpeed=Speed.valueOfKnot(6.0).value(); /* knots*/
        
        /* the first point on the route starts now!
         * so lets calculate the distance the boat could have travelled between
         * now and 'atTime'
         **/
        Calendar now=new GregorianCalendar();
        long timeDiff=atTime.getTimeInMillis()-now.getTimeInMillis();
        double distTraveled;
        
        
        
        return boatPos;
        
    }
    
    public int getNbValues() {
        return numPoints;
    }
    public void clear() {
        points=null;
        numPoints=0;
    }
    
    public MapPoint[] getAllPoints() {
        
        // then create the return vector, properly sized.
        MapPoint[] mp=new MapPoint[numPoints];
        
        for (int i=0; i<numPoints ; ++i) {
            mp[i]=points[i];
        }
        
        return mp;
    }
    
    public String[] getHeaders() {
        String[] columnNames = {
            "Wpt Nb",
                    "Latitude",
                    "Longitute",
                    "Dist",
                    "Tot Dist"
                    //"nb of Years",
                    //"Vegetarian"
        };
        return columnNames;
    }
    public Object[][] getAllData() {
        Double distance=0.0;
        MapPoint lastP=null;
        
        Object[][] d=new Object[numPoints][5];
        for (int i=0 ; i<numPoints ; ++i) {
            MapPoint p=points[i];
            d[i][0]=new Integer(i);
            d[i][1]=p.getLatitude();
            d[i][2]=p.getLongitude();
            //d[i][1]=Config.latitudeToString(p.getLatitude().value());
            //d[i][2]=Config.longitudeToString(p.getLongitude().value());
            if (i==0) {
                d[i][3]=new Double(0.0);
                d[i][4]=new Double(0.0);
            } else {
                Double di=MapPoint.distanceMeeus(lastP,p).NmValue();
                d[i][3]=new Double(di);
                distance+=di;
                d[i][4]=new Double(distance);
            }
            lastP=p;
        }
        return d;
    }
    
    
    
    public void showDialog(JFrame frame) {
        
        if (numPoints==0) {
            try {
                JOptionPane.showMessageDialog(frame,
                        bundle.getString("Message.noRouteData"),
                        bundle.getString("Message.noRouteHeader"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {}
            return ;
        }
        
        if (dlg==null) {
            dlg = new RouteDialog(frame);
            dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dlg.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    dlg.dispose();
                    dlg=null;
                    return;
                }
            });
            // Work around pack() bug on some platforms
            dlg.setSize(dlg.getPreferredSize());
            dlg.setLocationRelativeTo(frame);
            dlg.setVisible(true);
            
            dlg.toFront();
        } else {
            dlg.setVisible(true);
            dlg.toFront();
        }
        return; // dlg;
        
    }
    
    private class RouteDialog extends JDialog {
        public RouteDialog(JFrame frame) {
            super(frame,bundle.getString("Message.routeViewWindow"));
            
            buildGUI();
            pack();
        }
        
        private void buildGUI() {
            
            JPanel panel=new JPanel(new GridLayout(1,0));
            
            //String[] columnNames = getHeaders();
            //Object[][] data = getAllData();
            
//            JTable table = new JTable(data, columnNames);
            JTable table = new JTable( new AbstractTableModel() {
                final String[] columnNames=getHeaders();
                final Object[][] rowData = getAllData();
                
                public String getColumnName(int c) {
                    return columnNames[c];
                    // return "Wpt Nb";
                }
                public int getRowCount() { return rowData.length; }
                public int getColumnCount() { return columnNames.length; }
                public Object getValueAt(int row, int col) {
                    return rowData[row][col];
                }
                public boolean isCellEditable(int row, int col) {
                    if (col==1 || col==2) {
                        return true;
                    } else
                        return false;
                }
                public void setValueAt(Object value, int row, int col) {
                    rowData[row][col]=value;
                    System.err.println("set value at was called");
                    //rowData[row][col] = value;
                    fireTableCellUpdated(row, col);
                }
                
            } );
            table.setPreferredScrollableViewportSize(new Dimension(500, 200));
            
            table.getModel().addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                    System.err.println("Got a tabel changed  event!!!!");
                }
                
            });
            
            TableColumn latitudeColumn = table.getColumnModel().getColumn(1);
            latitudeColumn.setCellEditor(new DegreesEditor());
//            table.setDefaultEditor(Latitude.class, new DegreesEditor());
            
            
            
            //Create the scroll pane and add the table to it.
            JScrollPane scrollPane = new JScrollPane(table);
            
            //JPanel panel = new JPanel(new BorderLayout(5, 5));
            //setBackground(Color.white);
            panel.add(scrollPane);
            getContentPane().add(panel);
            
            
        }
    }
    
}
