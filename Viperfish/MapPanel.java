/*
 *  MapPanel.java
 *
 *  Created on April 9, 2005, 9:38 PM
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;

import java.text.MessageFormat;


import javax.swing.*;
import javax.swing.event.*;

//import java.net.*;
import java.awt.print.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import Solfin.Tools.*;
import Solfin.Grib.*;


/*
 * WARNING: the following import statement means I can use mathematical
 * functions directly without prefixing them with "Math."
 * ie I can now write abs(var) instead of Math.abs(var)
 *
 * Don't abuse such import statements, it makes code hard to read. I only
 * use it for Math.*, and for methods only (not constants).
 */
import fr.enib.wwwjgrib.controller.ApplicationEventDispatcher;
import fr.enib.wwwjgrib.controller.WindFieldEvent;
import fr.enib.wwwjgrib.model.Wind;
import fr.enib.wwwjgrib.model.WindField;
import static java.lang.Math.*;




public class MapPanel extends JPanel implements MouseInputListener,Printable {
    Controller dad;
    
    final static int maxCharHeight = 15;
    final static int minFontSize = 6;
    FontMetrics fontMetrics;
    
    
    BufferedImage bi=null;
    MapGraphics mg=null;
    
    BufferedImage biMap=null;
    
    
    BoundaryBox visible = new BoundaryBox(80F,-80F,180.001F,180F);
    Point2D.Double referencePoint=null;
    
    Point prevRoutePoint=null;
    Point nextRoutePoint=null; // usefull when you insert a new waypoint between two existing waypoints.
    Point currentRoutePoint=null;
    int currentWaypointIdx=0;
    
    Rectangle2D.Double rectToDraw=null;
    Rectangle2D.Double previousRectDrawn = new Rectangle2D.Double();
    
    int offsetX=0;
    int offsetY=0;
    
    GribRecord gr1=null;
    GribRecord gr2=null;
    
    Dimension oldSize=null;
    
    // mouse popup
    JPopupMenu popup;
    
    
    private final static int AREA_NONE=0;
    private final static int AREA_ZOOM=1;
    private final static int AREA_MOVE=2;
    private final static int AREA_ROUTE=3;
    private final static int AREA_ROUTE_INSERT=4;
    private final static int AREA_ROUTE_MOVE=5;
    int areaFlag=AREA_NONE;
    
    
    // Some final variables used below ....
    private final static int CtrlButton1  = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    private final static int CtrlButton2  = InputEvent.CTRL_DOWN_MASK | InputEvent.BUTTON2_DOWN_MASK;
    private final static int ShiftButton1 = InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK;
    
    public MapPanel(Controller dad) { //, BoundaryBox visible) {
        
        this.dad=dad;
        //this.visible=visible;
        
        // add listeners ...
        addMouseListener(this);
        addMouseMotionListener(this);
        
        //Initialize drawing colors
        setBackground(Color.white);
        setForeground(Color.black);
        //openMapFile(visible.getWidth());
        
        //setMinimumSize(new Dimension(1000,600));
        
    }
    
    
    //
    // Interface methods that must be supplied :
    //
    // MouseListener methods
    public void mouseClicked(MouseEvent e) {} //empty
    public void mouseEntered(MouseEvent e) {} // empty
    public void mouseExited(MouseEvent e) {} //empty
    
    public void mousePressed(MouseEvent e) {
        if (areaFlag==AREA_ROUTE_INSERT) {
            dad.getRoute().addBefore(currentWaypointIdx,mg.toMapPoint(new Point(e.getX(),e.getY())));
            repaint();
            areaFlag=AREA_NONE;
            prevRoutePoint=null;
            nextRoutePoint=null;
            currentRoutePoint=null;
            return;
        }
        if (areaFlag==AREA_ROUTE_MOVE) {
            System.err.println("Now move thihs point!!!!");
            dad.getRoute().addBefore(currentWaypointIdx, mg.toMapPoint(new Point(e.getX(),e.getY())));
            dad.getRoute().remove(currentWaypointIdx+1);
            areaFlag=AREA_NONE;
            prevRoutePoint=null;
            nextRoutePoint=null;
            currentRoutePoint=null;
            return;
            
        }
        
        
        if (e.isPopupTrigger()) {
            showPopupMenu(e.getComponent(), e.getX(), e.getY());
            return;
        }
        
        referencePoint = new Point2D.Double(e.getX(),e.getY());
        
        // CTRL+mouse button 1 => area zoom start
        if ((e.getModifiersEx() & CtrlButton1) == CtrlButton1) {
            areaFlag=AREA_ZOOM;
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            updateDrawableRect(e.getX(),e.getY());
            return;
        }
        
        // SHIFT+Mouse button 1 => area move start
        if ((e.getModifiersEx() & ShiftButton1) == ShiftButton1) {
            areaFlag=AREA_MOVE;
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            return;
        }
        
        // CTRL+mouse button 2 => route point to add
        if ((e.getModifiersEx() & CtrlButton2) == CtrlButton2) {
            areaFlag=AREA_ROUTE;
            // Here we recalculate lastRoutePoint from lastRouteMapPoint
            prevRoutePoint=mg.toPoint(dad.getRoute().getLastWaypoint());
//            if (lastRouteMapPoint!=null) {
//                Point2D p=mg.toPoint2D(lastRouteMapPoint);
//                lastRoutePoint=new Point2D.Double(p.getX(),p.getY());
//            }
            currentRoutePoint=new Point(e.getX(),e.getY());
            repaint();
            //updateRouteLine(e.getX(),e.getY());
            return;
        }
        
    }
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopupMenu(e.getComponent(), e.getX(), e.getY());
            return;
        }
        
        
        updateLabel(e.getPoint());
        
        // mouse button has been released so reset cursor icon to default
        setCursor(Cursor.getDefaultCursor());
        
        offsetX=0;
        offsetY=0;
        
        // CRTL+Mouse button 1 => area zoom end
        if (areaFlag == AREA_ZOOM) {
            areaFlag=AREA_NONE;
            // in case mouse moved a lot just as it was released!
            updateDrawableRect(e.getX(), e.getY());
            referencePoint=null;
            
            if (rectToDraw!=null && rectToDraw.width!=0 && rectToDraw.height!=0) {
                System.err.println("BB="+mg.getBoundaryBox(rectToDraw).toString());
                dad.updateVisible(mg.getBoundaryBox(rectToDraw));
                repaint();
            }
            return;
            
        }
        
        // SHIFT+Mouse button 1 => area move end
        if (areaFlag == AREA_MOVE) {
            areaFlag=AREA_NONE;
            offsetX=0;
            offsetY=0;
            Rectangle2D.Double newRect=new Rectangle2D.Double(
                    referencePoint.x-e.getX(),referencePoint.y-e.getY(),getWidth(),getHeight());
            dad.updateVisible(mg.getBoundaryBox(newRect));
            repaint();
            referencePoint=null;
            return;
        }
        
        // CRTL+Mouse button 2 => route point
        if (areaFlag == AREA_ROUTE) {
            areaFlag=AREA_NONE;
            System.err.println("Adding a route point");
            dad.getRoute().add(mg.toMapPoint(new Point(e.getX(),e.getY())));
            //lastRoutePoint=new Point2D.Double(e.getX(),e.getY());
            //lastRouteMapPoint=mg.toMapPoint(lastRoutePoint);
            prevRoutePoint=null;
            nextRoutePoint=null;
            currentRoutePoint=null;
            
        }
        
        areaFlag=AREA_NONE;
        referencePoint=null;
        rectToDraw=null;
        
        repaint();
        
    }
    public void mouseDragged(MouseEvent e) {
        areaFlag=AREA_NONE;
        
        // CTRL+Mouse button 1  => area zoom selection is being made
        if ((e.getModifiersEx() & CtrlButton1) == CtrlButton1) {
            areaFlag=AREA_ZOOM;
            updateLabel(e.getPoint());
            updateDrawableRect(e.getX(),e.getY());
            return;
        }
        
        // SHIFT+Mouse button 1 => area move is being made
        if ((e.getModifiersEx() & ShiftButton1) == ShiftButton1) {
            areaFlag=AREA_MOVE;
            offsetX=(int)(e.getX()-referencePoint.x);
            offsetY=(int)(e.getY()-referencePoint.y);
            repaint();
            return;
        }
        
        // CRTL+Mouse button 2 => route point is being set
        if ((e.getModifiersEx() & CtrlButton2) == CtrlButton2) {
            areaFlag=AREA_ROUTE;
            updateLabel(e.getPoint());
            currentRoutePoint=new Point(e.getX(), e.getY());
            repaint();
            //updateRouteLine(e.getX(),e.getY());
            return;
        }
        
    }
    public void mouseMoved(MouseEvent e) {
        if (e==null) return;
        updateLabel(e.getPoint());
        
        if (popup!=null && popup.isVisible()) {
            popup.setVisible(false);
            popup=null;
        }
        
        if (areaFlag==AREA_ROUTE_INSERT || areaFlag==AREA_ROUTE_MOVE) {
            currentRoutePoint=new Point(e.getX(), e.getY());
            repaint();
        }
    }
    
    // Printing methods
    PrintRequestAttributeSet aset = null;
    private void initASet() {
        if (aset == null) {
            aset=new HashPrintRequestAttributeSet();
            aset.add(OrientationRequested.LANDSCAPE);
            aset.add(new Copies(1));
            aset.add(new JobName("JMap", null));
        }
    }
    public void pageSetup() {
        initASet();
        
        /* Create a print job */
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        /* locate a print service that can handle the request */
        PrintService[] services =
                PrinterJob.lookupPrintServices();
        
        if (services.length > 0) {
            System.out.println("selected printer " + services[0].getName());
            try {
                pj.setPrintService(services[0]);
                pj.pageDialog(aset);
            } catch (PrinterException pe) {
                System.err.println(pe);
            }
        }
    }
    public void print() {
        initASet();
        /* Create a print job */
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.setPrintable(this);
        /* locate a print service that can handle the request */
        PrintService[] services =
                PrinterJob.lookupPrintServices();
        
        if (services.length > 0) {
            System.out.println("selected printer " + services[0].getName());
            try {
                pj.setPrintService(services[0]);
                //pj.pageDialog(aset);
                if(pj.printDialog(aset)) {
                    pj.print(aset);
                }
            } catch (PrinterException pe) {
                System.err.println(pe);
            }
        }
    }
    
    
    
    //
    // methods centralising calls to the controller
    //
    private Point mouseLoc=null;
    private void updateLabel(Point mLoc) {
        // no MapGraphic object bail out
        if (mg==null) return;
        // no mouse location ....
        if (mLoc==null) {
            // do we have an old mouse location if not bail out
            if (mouseLoc==null)
                return;
            // if we do use it
            else
                mLoc=mouseLoc;
        }
        // store mouse location away so it may be reused if necessary
        mouseLoc=mLoc;
        
        // now convert mouse position to a map position (ie a latitude+longitude)
        MapPoint mouseMP=mg.toMapPoint(mLoc);
        
        // now prepare to build label string
        String labelStr="";
        
        labelStr += mouseMP.getLatitude().toString();
        labelStr += "   "+mouseMP.getLongitude().toString();
        
        if (gr1!=null && gr2!=null && gr1.getParameterId()==33 && gr2.getParameterId()==34) {
            double u=gr1.getValueAt(mouseMP);
            double v=gr2.getValueAt(mouseMP);
            double windSpeed=sqrt(u*u+v*v) * 1.9438444908;
            float theta = (float)Longitude.correctLongitude(360 - 180.0 / Math.PI * atan2(v,u) - 90);
            labelStr+="    "+MessageFormat.format(
                    dad.getBundle().getString("Message.statusWindLabel"),
                    theta,
                    windSpeed);
        } else if (gr1!=null) {
            GribParameter gpr=new GribParameter(gr1.getParameterId());
            String grLabel=gpr.getLabel();
            if (grLabel==null)
                grLabel="{0}";
            labelStr+="    "+MessageFormat.format(grLabel,gr1.getValueAt(mouseMP));
            
//            if (gr1.getParameterId()==11)
//                labelStr+="    "+MessageFormat.format(
//                        dad.getBundle().getString("Message.statusTempLabel"),
//                        ReadTools.Temperature.C.fromSI(gr1.getValueAt(mouseMP)));
//            else
//                labelStr+="    "+MessageFormat.format(
//                        dad.getBundle().getString("Message.statusValueLabel"),
//                        gr1.getValueAt(mouseMP));
            
        }
        
        dad.updateLabel(labelStr);
    }
    public MapPoint getMouseMapPos() {
        return (mouseLoc==null? new MapPoint() : mg.toMapPoint(mouseLoc) );
        //double[] ll=mg.convertXYtoLL(mouseLoc.x,mouseLoc.y);
        //return new MapPoint(ll[0],ll[1]);
    }
    public Point getMousePos() {
        return mouseLoc;
    }
    public BoundaryBox zoomIn() {
//        MapPoint middle=visible.getMiddle();
//        System.err.println("mouseloc_x="+mouseLoc.x+"  mouseloc_y="+mouseLoc.y);
//        return mg.zoomIn(mouseLoc, 0.7);
        System.err.println("Visible before zoomin="+visible.toString());
        return visible.zoomIn(0.7);
    }
    //
    // methods called by the controller
    //
    protected void setGRIBData(File gribFile,int recordId1, int recordId2) {
        //        this.gribFile = gribFile;
        
        try {
            GribFile gf=GribFile.open(gribFile);
            gr1 = gf.getRecord(recordId1);
            gr2 = recordId2>=0 ? gf.getRecord(recordId2) : null;
        } catch (BadGribException e) {
            System.err.println(e);
            gr1=null;
            gr2=null;
            //gf=null;
        }
        
        
        //this.gribRecordId1=recordId1;
        //this.gribRecordId2=recordId2;
        
        bi=null;
        referencePoint=null;
        rectToDraw=null;
        
        repaint();
    }
    public void zoomReset() {
        referencePoint=null;
        rectToDraw=null;
        dad.updateVisible(new BoundaryBox(80,-80,-179.9F,180));
        repaint();
        return;
    }
    
    
    //
    // support methods needed by the other methods
    // they generaly can remain private
    //
    private void showPopupMenu(Component c, int x, int y) {
        AppMenu amenu=new AppMenu(dad.getBundle(), dad.getActionMap());
        // determine if we are near a waypoint of the route
        Route route=dad.getRoute();
        int numPoints=route.getNbValues();
        double minDist=route.closestDistance(new Point(x,y),mg);
        
        // show a 'waypoint' popup menu if we are within 6 pixels of a waypoint center
        if (minDist < 6) {
            popup=amenu.createPopupMenu("PopupWaypoint");
        }
        // otherwise show the 'standard' popup menu
        else {
            popup=amenu.createPopupMenu("Popup");
        }
        popup.show(c, x-10, y-10);
    }
    public void changeBoundaryBox(BoundaryBox newVisible) {
        //System.err.println("I'm in changeBoundaryBox!!! "+newVisible.toString());
        visible = new BoundaryBox(newVisible);
        System.err.println("Middle is in"+visible.getMiddle().toString());
        // force recreation of map
        bi=null;
        biMap=null;
        mg=null;
        referencePoint=null;
        rectToDraw=null;
        prevRoutePoint=null;
        nextRoutePoint=null;
        currentRoutePoint=null;
    }
    public void forceRepaint() {
        bi=null;
        biMap=null;
        mg=null;
        repaint();
    }
    private MapFile openMapFile() {
        MapFile fMap=null;
        try {
            fMap= new MapFile((float)visible.getWidth());
        } catch (java.io.FileNotFoundException e) {
            System.err.println("FileNotFoundException: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            System.err.println("IOException: " + e.getMessage());
            throw new RuntimeException(e);
        }
        // we need this dummy call to trigger initialisation
        // inside the MapFile class.
        int nbPoly = fMap.getNbPolygons();
        
        return fMap;
    }
    private void updateDrawableRect(int mouseX,int mouseY) {
        if (referencePoint==null) {
            referencePoint = new Point2D.Double(mouseX,mouseY);
        }
        
        int compWidth = getWidth();
        int compHeight = getHeight();
        
        double x = referencePoint.x;
        double y = referencePoint.y;
        double width = mouseX-referencePoint.x;
        double height = mouseY-referencePoint.y;
        
        //Make the width and height positive, if necessary.
        if (width < 0) {
            width = 0 - width;
            x = x - width + 1;
            if (x < 0) {
                width += x;
                x = 0;
            }
        }
        if (height < 0) {
            height = 0 - height;
            y = y - height + 1;
            if (y < 0) {
                height += y;
                y = 0;
            }
        }
        
        // Keep proportion between width and height to the same ratio
        // as is already between compWidth/compHeight
        double widthHeightRatio=(double)compWidth/compHeight;
        if (height!=0) {
            if (width/height > widthHeightRatio) {
                height = (int)(width / widthHeightRatio);
            } else if (width/height < widthHeightRatio) {
                width = (int)(height * widthHeightRatio);
            }
        }
        
        //The rectangle shouldn't extend past the drawing area.
        if ((x + width) > compWidth) {
            width = compWidth - x;
            height = (int)(width / widthHeightRatio);
        }
        if ((y + height) > compHeight) {
            height = compHeight - y;
            width = (int)(height * widthHeightRatio);
        }
        
        //Update rectToDraw after saving old value.
        if (rectToDraw != null) {
            previousRectDrawn.setRect(
                    rectToDraw.x, rectToDraw.y,
                    rectToDraw.width, rectToDraw.height);
            rectToDraw.setRect(x, y, width, height);
        } else {
            rectToDraw = new Rectangle2D.Double(x, y, width, height);
        }
        
        // Repaint part so as to show selection rectangle
        Rectangle2D.Double totalRepaint = (Rectangle2D.Double)rectToDraw.createUnion(previousRectDrawn);
        repaint((int)totalRepaint.x, (int)totalRepaint.y,
                (int)totalRepaint.width, (int)totalRepaint.height);
        //        repaint();
    }
    private void updateRouteLine(int mouseX, int mouseY) {
//        if (lastRoutePoint==null) {
//            drawCircle(new Point(mouseX,mouseY), 0.1);
//        }
    }
    public void clearRoute() {
        prevRoutePoint=null;
        currentRoutePoint=null;
        nextRoutePoint=null;
        repaint();
    }
    public void waypointInsertBefore() {
        Route route=dad.getRoute();
        currentWaypointIdx=route.closest(mouseLoc,mg);
        nextRoutePoint=route.getWaypoint(currentWaypointIdx,mg);
        prevRoutePoint=(currentWaypointIdx>0 ? route.getWaypoint(currentWaypointIdx-1,mg) : null);
        currentRoutePoint=mouseLoc;
        areaFlag=AREA_ROUTE_INSERT;
    }
    public void waypointMove() {
        Route route=dad.getRoute();
        currentWaypointIdx=route.closest(mouseLoc,mg);
        nextRoutePoint=(currentWaypointIdx<route.getNbValues()-1 ? route.getWaypoint(currentWaypointIdx+1,mg) : null);
        prevRoutePoint=(currentWaypointIdx>0 ? route.getWaypoint(currentWaypointIdx-1,mg) : null);
        currentRoutePoint=mouseLoc;
        areaFlag=AREA_ROUTE_MOVE;
    }
    
    private FontMetrics pickFont(Graphics2D g2, String longString, int xSpace) {
        boolean fontFits = false;
        Font font = g2.getFont();
        FontMetrics fontMetrics = g2.getFontMetrics();
        int size = font.getSize();
        String name = font.getName();
        int style = font.getStyle();
        
        while ( !fontFits ) {
            if ( (fontMetrics.getHeight() <= maxCharHeight)
            && (fontMetrics.stringWidth(longString) <= xSpace) ) {
                fontFits = true;
            } else {
                if ( size <= minFontSize ) {
                    fontFits = true;
                } else {
                    g2.setFont(font = new Font(name,
                            style,
                            --size));
                    fontMetrics = g2.getFontMetrics();
                }
            }
        }
        
        return fontMetrics;
    }
    private void drawWindBarbs(MapGraphics mg, GribRecord Ugr, GribRecord Vgr) {
        
        if (Ugr!=null && Vgr!=null) {
            
            // do some routine checks (Ugr and Vgr should be same date, same number of values, span the same area, same di/dj, ....)
            
            //
            // Ugr and Vgr are ok .... we now continue to build wind vectors from these two Grib records.
            //
            // some graphical settings ...
            mg.setColor(Color.blue);
            
            Iterator<GribValue> Ugvi = Ugr.iterator();
            Iterator<GribValue> Vgvi = Vgr.iterator();
            GribValue Ugv;
            GribValue Vgv;
            int iVal=1;
            WindField windField = new WindField();
            while ( (Ugv=Ugvi.next())!=null && (Vgv=Vgvi.next())!=null ) {
                if (!Ugv.isMissing()) {
                    iVal++;
                    //mg.drawWindBarb(Ugv.getLongitude(),Ugv.getLatitude(),(float)Ugv.getValue(),(float)Vgv.getValue(), Ugr.getDi());

                   Wind wind = mg.drawWindBarb(Ugv, (float)Ugv.getValue(),(float)Vgv.getValue(), Ugr.getDi());
                   windField.addWind(wind);
                }
            }
            WindFieldEvent windEvent = new WindFieldEvent(this,windField);
            ApplicationEventDispatcher.fireWindEvent(windEvent);
        }
    }
    
    private void drawContoursInQuad(MapGraphics mg,GribValue[] p,double[] levels) {
        // determine if min/max span accross several levels. If that is not the
        // case then subdividing the quad into triangles is useless since no
        // contours cross this quad in the first place!
        double maxValue=Double.MIN_VALUE;
        double minValue=Double.MAX_VALUE;
        int minLevel=levels.length;
        int maxLevel=0;
        for (int iLevel=levels.length-1 ; iLevel>=0 ; --iLevel) {
            if (minValue < levels[iLevel]) minLevel = iLevel;
            if (maxValue < levels[iLevel]) maxLevel = iLevel;
        }
        if (minLevel == maxLevel) {
            // draw an empty polygon of the right color
            // its actually unclean to have this code in MapPanel! it should be
            // in MapGraphics!
            mg.setColor(Color.getHSBColor( (float)((310.0*minLevel/levels.length+50.0)/360.0) ,1.0F,1.0F));
            java.util.List<MapPoint> fillPoly=new ArrayList<MapPoint>(4);
            for (int k=0 ; k<4 ; ++k)
                if (!p[k].isMissing())
                    fillPoly.add(new MapPoint(p[k].getLongitude().value(),p[k].getLatitude().value()));
            mg.fill(fillPoly);
            return;
        }
        
        
        
        // Calculate the mid point of above four points
        int nbVal=0;
        double avgValue=0;
        double avgLat=0;
        double avgLon=0;
        double sumLonU=0;
        double sumLonV=0;
        for (int k=0 ; k<4 ; ++k) {
            if (!p[k].isMissing()) {
                ++nbVal;
                double val = p[k].getValue();
                maxValue = (val > maxValue ? val : maxValue);
                minValue = (val < minValue ? val : minValue);
                
                avgValue += val;
                avgLat   += p[k].getLatitude().value();
                double rLon = toRadians(p[k].getLongitude().value());
                sumLonU  += cos(rLon);
                sumLonV  += sin(rLon);
            }
        }
        
        // Difficult to make triangles from 0,1 or 2 points. So bail out
        // early if that happens and save some CPU power.
        if (nbVal<=2)
            return;
        
        avgValue /= nbVal;
        avgLat   /= nbVal;
        avgLon = (180.0/PI * atan2(sumLonV,sumLonU));
        
        
        
        
        // Now loop for each triangle (4 at most) and call the contourTriangle routine
        for (int k=0 ; k<4 ; ++k) {
            int i0=k;
            int i1=(k+1)%4;
            
            // if one of the points of the quad are missing move to next sub-triangle.
            if (nbVal!=4 && (p[i0].isMissing() || p[i1].isMissing()))
                continue;
            
            GribValue[] mpArr=new GribValue[3];
            
            //            mpArr[0]=new MapPoint(p[i0].getLongitude() , p[i0].getLatitude(), p[i0].getValue());
            //            mpArr[1]=new MapPoint(p[i1].getLongitude() , p[i1].getLatitude(), p[i1].getValue());
            //            mpArr[2]=new MapPoint(avgLon, avgLat, avgValue);
            mpArr[0]=p[i0];
            mpArr[1]=p[i1];
            mpArr[2]=new GribValue(avgValue, avgLon, avgLat);
            
            
            mg.drawContourInTriangle(mpArr, levels);
        }
        //return ;
        
    }
    private void drawContourLines(MapGraphics mg, GribRecord gr) {
        System.out.println("Going to draw contour lines for:"+gr.getParameter().getShortStr());
        double min;
        double max;
        double step;
        int nbLevels=30;
        GribParameter gp=new GribParameter(gr.getParameterId());
        
        min=gp.getMin();
        max=gp.getMax();
        step=gp.getStep();
        if (min==max) {
            min=gr.getMinimumValue();
            max=gr.getMaximumValue();
        }
        System.out.println("Min/max values="+min+" "+max);
        
        // build array of levels
        if (step!=0.0) {
            nbLevels=(int)((max-min)/step+1);
        } else {
            nbLevels=30;
            step=(max-min)/(nbLevels-1);
        }
        double[] levels = new double[nbLevels];
        for (int ii=0 ; ii<levels.length ;++ii) {
            levels[ii]=min+step*ii;
        }
        for (int ii=0 ; ii<levels.length ;++ii) {
            System.out.printf("%d: %f\n",ii,levels[ii]);
        }
        
        //mg.setColor(Color.red); //blue);
        
        GribValue[][] gvs=gr.getAllValues();
        System.out.println("Ni*Di="+gr.getNi()*gr.getDi());
        for (int j=gr.getNj()-2 ; j>=0 ; --j) {
            for (int i=gr.getNi()-2 ; i>=0 ; --i) {
                GribValue[] p = new GribValue[4];
                p[0]=gvs[j][i];
                p[1]=gvs[j][i+1];
                p[2]=gvs[j+1][i+1];
                p[3]=gvs[j+1][i];
                
                drawContoursInQuad(mg,p,levels);
            }
        }
        if ((gr.getNi()*gr.getDi()) == 360.0) {
            int ni=gr.getNi()-1;
            for (int j=gr.getNj()-2 ; j>=0 ; --j) {
                GribValue[] p = new GribValue[4];
                p[0]=gvs[j][ni];
                p[1]=gvs[j][0];
                p[2]=gvs[j+1][0];
                p[3]=gvs[j+1][ni];
                
                drawContoursInQuad(mg,p,levels);
            }
        }
    }
    
    private void drawRoute(MapGraphics mg,Graphics2D g2) {
        MapPoint[] mp=dad.getRoute().getAllPoints();
        
        //mg.g.setPaint(Color.red);
        //mg.g.setXORMode(Color.white); //Color
        g2.setColor(Color.magenta);
        
        Point prevPoint=null;
        if (mp.length>0) {
            prevPoint=mg.toPoint(mp[0]);
            //mg.fillOval(mp[0], 7,7);
            g2.fillOval(prevPoint.x-3,prevPoint.y-3,7,7);
            
        }
        Point nextPoint=null;
        for (int i=1 ; i<mp.length ; ++i) {
            nextPoint=mg.toPoint(mp[i]);
            g2.fillOval(nextPoint.x-3,nextPoint.y-3, 7, 7);
            g2.drawLine(prevPoint.x, prevPoint.y, nextPoint.x, nextPoint.y);
            prevPoint=nextPoint;
            //mg.drawStraightLine(mp[i-1],mp[i],true);
        }
        
    }
    //
    // overriden methods:
    //
    public Dimension getMinimumSize() {
        return new Dimension((int)360,(int)279);
    }
    public void paintComponent(Graphics g) {
        System.out.println("in paintComponent()");
        //System.out.println("isOpaque "+isOpaque());
        //System.out.println("isDoubleBuffered "+isDoubleBuffered());
        
        super.paintComponent(g);
        
        if (referencePoint==null) {
            rectToDraw=null;
        }
        
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        //System.out.println(g2.getClipBounds());
        
        Dimension d = getSize();
        if (!d.equals(oldSize)) {
            //System.out.println("Size has changed");
            oldSize=d;
            bi=null;
            biMap=null;
            referencePoint=null;
            rectToDraw=null;
        }
        //g.clearRect(0,0,(int)d.width,(int)d.height);
        
        //fontMetrics = pickFont(g2, "Filled and Stroked GeneralPath",20);
        
        // build a 3D effect frame around drawing
        //g2.setPaint(Color.lightGray);
        //g2.draw3DRect(0, 0, d.width - 1, d.height - 1, true);
        //g2.draw3DRect(3, 3, d.width - 7, d.height - 7, false);
        //g2.setPaint(Color.black);
        
        if (bi==null) {
            double w=d.getWidth();
            double h=d.getHeight();
            
            bi = new BufferedImage((int)w,(int)h,BufferedImage.TYPE_INT_ARGB);//createImage(w,h);
            Graphics2D big = bi.createGraphics();
            mg = new MapGraphics(big,getSize(),visible);
            visible = mg.getVisible();
            
            // draw the land boundaries
            mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            mg.setBackground(Color.getHSBColor(0.0f, 0.0f, Config.getAlpha()));
            mg.clear();
            
            
            // draw GRIB data (1st)
            // This first call is only necessary to draw shaded contour lines,
            // as we prefer to draw everything else ABOVE it (eg land limits,
            // lat-lon grid, etc ...)
            if (gr1!=null && gr2==null) {
                //AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_OVER); //CLEAR,0.5f);
                //big.setComposite(ac);
                drawContourLines(mg,gr1);
            }
            
            
            // Now load image - the optimal image size (for zoom level) will be loaded by the
            // openMapFile() method.
            // The world map is loaded in its own buffered image to avoid having to reload it each time.
            if (biMap==null) {
                biMap = new BufferedImage((int)w,(int)h,BufferedImage.TYPE_INT_ARGB);//createImage(w,h);
                Graphics2D bigMap = biMap.createGraphics();
                MapGraphics mgMap = new MapGraphics(bigMap,getSize(),visible);
                
                //mgMap.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                //Color tBg2=new Color(100,0,0,0);
                mgMap.setBackground(new Color(0.0f,0.0f,0.0f,0.0f)); //getBackground());
                mgMap.clear();
                
                MapFile fMap = openMapFile();
                mgMap.drawMap(fMap);
                fMap = null; //force gc
                
                // TODO: ideally draw the political borders too ....
                // draw the longitude/latitude grid
                mgMap.drawGrid(true);
                
                mgMap=null;
                bigMap=null;
                //biMap=null;
            }
            // now place biMap on bi
            //big.setXORMode(Color.white);
            //bi.setData(biMap.getRaster());
            Composite acOrig = big.getComposite();
            Composite ac1 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
            big.setComposite(ac1);
            big.drawImage(biMap,0,0,this);
            big.setComposite(acOrig);
            
            
            
            
            
            
            // draw GRIB data (2nd).
            // This second call to GRIB data display is necessary if the data
            // being displayed are wind barbs as in that case it is better for
            // the barbs to be drawn ABOVE all other data.
            if (gr1!=null && gr2!=null && gr1.getParameterId()==33 && gr2.getParameterId()==34) {
                drawWindBarbs(mg,gr1,gr2);
            }
            
            
            // if we have GRIB data then print out some information on it.
            if (gr1!=null) {
                mg.setColor(Color.red); //blue);
                mg.getGraphics2D().drawString(dad.getBundle().getString("Message.referenceDate")+Config.dateToString(gr1.getReferenceDate()),10,15);
                Calendar aCal=gr1.getForecastDate1();
                if (aCal!=null) {
                    mg.getGraphics2D().drawString(dad.getBundle().getString("Message.forecastDate")+Config.dateToString(aCal),10,30);
                }
            }
            
            
            updateLabel(null);
        }
        
        g2.drawImage(bi, offsetX, offsetY, this);
        
        // Here we draw loaded route
        if (mg!=null && offsetX==0 && offsetY==0) {
            drawRoute(mg,g2);
        }
        
        
        // if we are zooming on a select area then 'rectToDraw' will
        // not be null - draw that rectangle ....
        if (rectToDraw != null) {
            //Draw a rectangle on top of the image.
            g2.setPaint(Color.red);
            g2.setXORMode(Color.white); //Color of line varies depending on image colors
            g2.drawRect((int)rectToDraw.x, (int)rectToDraw.y,
                    (int)rectToDraw.width - 1, (int)rectToDraw.height - 1);
        }
        
        // if we are setting a route draw this intermediate point
        if (areaFlag==AREA_ROUTE || areaFlag==AREA_ROUTE_INSERT || areaFlag==AREA_ROUTE_MOVE) {
            g2.setPaint(Color.red);
            g2.setXORMode(Color.white); //Color of line varies depending on image colors
            if (prevRoutePoint!=null) {
                g2.drawLine(prevRoutePoint.x,prevRoutePoint.y, currentRoutePoint.x,currentRoutePoint.y);
            }
            if (nextRoutePoint!=null) {
                g2.drawLine(nextRoutePoint.x,nextRoutePoint.y, currentRoutePoint.x,currentRoutePoint.y);
            }
            g2.fillOval((int)currentRoutePoint.x-3, (int)currentRoutePoint.y-3, 6,6);
        }
        
        // Here we draw some boat information (position, heading)
        // drawBoatData();
        
        
    }
    public int print(Graphics g,PageFormat pf,int pageIndex) {
        System.out.println("PRINTING!");
        
        if (pageIndex == 0) {
            System.out.println("iX="+pf.getImageableX()+" iY="+pf.getImageableY()+
                    " iH="+pf.getImageableHeight()+" iW="+pf.getImageableWidth()+
                    " h="+pf.getHeight()+" w="+pf.getWidth());
            Graphics2D g2d= (Graphics2D)g;
            
            
            MapGraphics mg = new MapGraphics(g2d,new Dimension((int)pf.getWidth(),(int)pf.getHeight()),visible);
            
            BasicStroke wideStroke = new BasicStroke(0.1f);
            
            g2d.setStroke(wideStroke);
            mg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            mg.setBackground(getBackground());
            mg.clear();
            
            
            // If we need to draw contour lines then do it now ...
            if (gr1!=null && gr2==null) {
                drawContourLines(mg,gr1);
            }
            
            // Now load image - the optimal image size (for zoom level) will be loaded by the
            // openMapFile() method.
            MapFile fMap = openMapFile();
            mg.drawMap(fMap);
            fMap = null; //force gc
            // draw the longitude/latitude grid
            mg.drawGrid(true);
            
            // if we need to draw wind barbs do it now!
            if (gr1!=null && gr2!=null && gr1.getParameterId()==33 && gr2.getParameterId()==34) {
                drawWindBarbs(mg,gr1,gr2);
            }
            
            
            g2d.setColor(Color.red);
            //            g2d.drawString("example string", 250, 250);
            g2d.drawRect(0, 0, (int)pf.getWidth(), (int)pf.getHeight());
            
            return Printable.PAGE_EXISTS;
        } else {
            return Printable.NO_SUCH_PAGE;
        }
    }
    
}