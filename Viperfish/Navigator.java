/*
 *  Navigator.java
 *
 *  Created on May 1, 2005, 12:17 AM
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
//import java.util.GregorianCalendar;
//import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.tree.TreePath;

import Solfin.Tools.Config;
import Solfin.Grib.*;

/**
 *
 * @author gabriel
 */
public class Navigator extends JTree implements TreeSelectionListener {
    Controller dad;
    
    DefaultMutableTreeNode top=null;
    DefaultMutableTreeNode sortNameVar=null; // sorted by file name+variable
    DefaultMutableTreeNode sortVarDate=null; // sorted by variable+date
    DefaultMutableTreeNode sortDateVar=null; // sorted by date+variable
    DefaultTreeModel model=null;
    
    List<LeafInfo> arrInfo=new ArrayList<LeafInfo>();
    
    
    public Navigator(Controller dad) {
        super((TreeModel)null);
        
        this.dad = dad;
        
        rebuildNode();
        
        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION); //SINGLE_TREE_SELECTION);
        setRootVisible(false);
        
        //Listen for when the selection changes.
        addTreeSelectionListener(this);
    }
    
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)getLastSelectedPathComponent();
        
        if (node == null) return;
        
        // if the node does not allow children then its a leaf node
        if (!node.getAllowsChildren()) {
            Object nodeInfo = node.getUserObject();
            // is the node object an instance of LeafInfo ?
            if (nodeInfo instanceof LeafInfo) {
                // it is, so we must show the grib data referenced by this
                // node to the map!
                LeafInfo l = (LeafInfo)nodeInfo;
                if (l.parameterId<0)
                    dad.loadGribFileIntoMap(l.gribFile,l.recordNb, l.recordNb2);
                else
                    dad.loadGribFileIntoMap(l.gribFile,l.recordNb, -1);
                
            } else {
                // it is not, then I know I am with the "empty" tree node
                // situation where we need to open a GRIB file.
                removeSelectionPath(getSelectionPath());
                dad.openGribFile(null);
                // return quickly! do not take any risks as it is likely that
                // after this point the navigator has changed drastically.
                return;
            }
        }
    }
    
    public void animate() {
        TreePath[] paths=getSelectionPaths();
        if (paths==null)
            return;
        for (int i=0 ; i<paths.length ; i++) {
            DefaultMutableTreeNode node=(DefaultMutableTreeNode)paths[i].getLastPathComponent();
            
            if (node!=null) {
                // if the node does not allow children then its a leaf node
                if (!node.getAllowsChildren()) {
                    Object nodeInfo = node.getUserObject();
                    // is the node object an instance of LeafInfo ?
                    if (nodeInfo instanceof LeafInfo) {
                        // it is, so we must show the grib data referenced by this
                        // node to the map!
                        LeafInfo l = (LeafInfo)nodeInfo;
                        System.err.println("Record nb="+l.recordNb);
                        if (l.parameterId<0)
                            dad.loadGribFileIntoMap(l.gribFile,l.recordNb, l.recordNb2);
                        else
                            dad.loadGribFileIntoMap(l.gribFile,l.recordNb, -1);
                        long startTime=System.currentTimeMillis();
                        while ((System.currentTimeMillis()-startTime)<5000)
                            ;
                        dad.repaint();
                        
                    }
                }
                
            }
            
        }
    }
    
    public void rebuildNameVarNode(List<LeafInfo> arrInfo) {
        
        
        // sort by filename, parameter, date
        Collections.sort(arrInfo, new Comparator<LeafInfo>() {
            public int compare(LeafInfo l1,LeafInfo l2) {
                int ret;
                ret=l1.gribFile.getName().compareTo(l2.gribFile.getName());
                if (ret!=0) return ret;
                
                ret=(l1.parameterId-l2.parameterId);
                if (ret!=0) return ret;
                
                return l1.recordDate.compareTo(l2.recordDate);
            } }
        );
        
        sortNameVar = new DefaultMutableTreeNode(dad.getBundle().getString("Message.sort_nv"),true);
        top.add(sortNameVar);
        DefaultMutableTreeNode fNode=null;
        DefaultMutableTreeNode pNode=null;
        String oldFileName="";
        int oldParameter=Integer.MIN_VALUE;
        int infoFlag = buildInfoFlag(0,0,SHOWINFO_DATE);
        for (LeafInfo l : arrInfo) {
            //System.out.println("l="+l);
            if (!l.gribFile.getAbsolutePath().equals(oldFileName)) {
                oldFileName=l.gribFile.getAbsolutePath();
                oldParameter=Integer.MIN_VALUE;
                fNode=new DefaultMutableTreeNode(l.gribFile.getName()+" (in "+
                        l.gribFile.getParent()+")", true);
                sortNameVar.add(fNode);
            }
            
            if (l.parameterId!=oldParameter) {
                oldParameter = l.parameterId;
                pNode=new DefaultMutableTreeNode(getParameterShortStr(l.parameterId)+
                        " ("+getParameterLongStr(l.parameterId)+")",
                        true);
                fNode.add(pNode);
            }
            
            DefaultMutableTreeNode node=new DefaultMutableTreeNode(new LeafInfo(l,infoFlag), false);
            pNode.add(node);
            
        }
    }
    public void rebuildVarDateNode(List<LeafInfo> arrInfo) {
        
        
        // sort by parameter, date, filename
        Collections.sort(arrInfo, new Comparator<LeafInfo>() {
            public int compare(LeafInfo l1,LeafInfo l2) {
                int ret;
                
                ret=(l1.parameterId-l2.parameterId);
                if (ret!=0) return ret;
                
                ret=l1.recordDate.compareTo(l2.recordDate);
                if (ret!=0) return ret;
                
                return l1.gribFile.getName().compareTo(l2.gribFile.getName());
            } }
        );
        
        sortVarDate = new DefaultMutableTreeNode(dad.getBundle().getString("Message.sort_vd"),true);
        top.add(sortVarDate);
        DefaultMutableTreeNode pNode=null;
        DefaultMutableTreeNode dNode=null;
        int oldParameter=Integer.MIN_VALUE;
        Calendar oldDate=null;
        int infoFlag = buildInfoFlag(0,0,SHOWINFO_FILE);
        for (LeafInfo l : arrInfo) {
            
            if (l.parameterId!=oldParameter) {
                oldParameter = l.parameterId;
                oldDate=null;
                pNode=new DefaultMutableTreeNode(getParameterShortStr(l.parameterId)+
                        " ("+getParameterLongStr(l.parameterId)+")",
                        true);
                sortVarDate.add(pNode);
            }
            
            if (!l.recordDate.equals(oldDate)) {
                oldDate=l.recordDate;
                dNode=new DefaultMutableTreeNode(Config.dateToString(l.recordDate),true);
                pNode.add(dNode);
                
            }
            
            DefaultMutableTreeNode node=new DefaultMutableTreeNode(new LeafInfo(l,infoFlag), false);
            dNode.add(node);
            
        }
    }
    public void rebuildDateVarNode(List<LeafInfo> arrInfo) {
        
        // sort by date, parameter, filename
        Collections.sort(arrInfo, new Comparator<LeafInfo>() {
            public int compare(LeafInfo l1,LeafInfo l2) {
                int ret;
                
                ret=l1.recordDate.compareTo(l2.recordDate);
                if (ret!=0) return ret;
                
                ret=(l1.parameterId-l2.parameterId);
                if (ret!=0) return ret;
                
                return l1.gribFile.getName().compareTo(l2.gribFile.getName());
            } }
        );
        
        sortDateVar = new DefaultMutableTreeNode(dad.getBundle().getString("Message.sort_dv"),true);
        top.add(sortDateVar);
        DefaultMutableTreeNode dNode=null;
        DefaultMutableTreeNode pNode=null;
        Calendar oldDate=null;
        int oldParameter=Integer.MIN_VALUE;
        int infoFlag = buildInfoFlag(0,0,SHOWINFO_FILE);
        for (LeafInfo l : arrInfo) {
            
            if (!l.recordDate.equals(oldDate)) {
                oldDate=l.recordDate;
                oldParameter=Integer.MIN_VALUE;
                dNode=new DefaultMutableTreeNode(Config.dateToString(l.recordDate),true);
                sortDateVar.add(dNode);
                
            }
            
            if (l.parameterId!=oldParameter) {
                oldParameter = l.parameterId;
                pNode=new DefaultMutableTreeNode(getParameterShortStr(l.parameterId)+
                        " ("+getParameterLongStr(l.parameterId)+")",
                        true);
                dNode.add(pNode);
            }
            
            DefaultMutableTreeNode node=new DefaultMutableTreeNode(new LeafInfo(l,infoFlag), false);
            pNode.add(node);
            
        }
    }
    public void rebuildNode() {
        // process arrInfo and add WIND data (from UGRD and VGRD)!
        List<LeafInfo> arrInfo2=new ArrayList<LeafInfo>();
        int iRecord=0;
        for (LeafInfo l : arrInfo) {
            arrInfo2.add(l);
            //System.out.printf("%d: %d %s\n",iRecord++,l.parameterId,l.recordDate);
            if (l.parameterId == 33) {  // UGRD - look for the corresponding VGRD
                for (LeafInfo l2 : arrInfo) {
                    if (l2.parameterId == 34 &&
                            l.gribFile.getAbsoluteFile().equals(l2.gribFile.getAbsoluteFile()) &&
                            l.recordDate.compareTo(l2.recordDate)==0 &&
                            l.level.isEqual(l2.level) ) {
                        LeafInfo l3=new LeafInfo(l.gribFile,-1,l.parameterId,l2.parameterId,l.recordDate,l.level,l.recordNb,l2.recordNb,l.showInfo);
                        arrInfo2.add(l3);
                    }
                }
            }
        }
        
        top=new DefaultMutableTreeNode(dad.getBundle().getString("Message.GRIBRecords"));
        if (arrInfo2.size() == 0) {
            sortNameVar = new DefaultMutableTreeNode(dad.getBundle().getString("Message.clickHereToOpenGribFile"),false);
            top.add(sortNameVar);
        } else {
            //
            // recreate root node
            //
            rebuildNameVarNode(arrInfo2);
            rebuildVarDateNode(arrInfo2);
            rebuildDateVarNode(arrInfo2);
        }
        model = new DefaultTreeModel(top,true);
        setModel(model);
        
    }
    
    public boolean addGribFile(File gribFile) {
        
        //
        // load record summary of the grib file. Record summary are made of:
        // - file name (File)
        // - parameter id
        // - a date
        //
        GribFile gf=GribFile.open(gribFile);
        if (gf==null)
            return false;
        int infoFlag=buildInfoFlag(0,0,SHOWINFO_DATE);
        int iRecord=0;
        for (GribRecord gr : gf) {
            LeafInfo l=new LeafInfo(gribFile,gr.getParameterId(),gr.getDate(),gr.getLevel(),iRecord,infoFlag);
            //System.out.printf("%d: %d %s %s\n",iRecord,gr.getParameterId(),gr.getDate(),gr.getLevel());
            arrInfo.add(l);
            ++iRecord;
        }
        gf = null; // let gc clean it!
        
        if (iRecord==0)
            return false;
        
        // Call node rebuild
        rebuildNode();
        return true;
    }
    public void removeGribFile(String gribFileName) {
        if (gribFileName==null)
            return;
        
        List<LeafInfo> newArrInfo=new ArrayList<LeafInfo>();
        
        for (LeafInfo l : arrInfo) {
            if (!gribFileName.equals(l.gribFile.getAbsolutePath())) {
                newArrInfo.add(l);
            }
        }
        arrInfo = newArrInfo;
        
        rebuildNode();
    }
    
    private String getParameterShortStr(int pid) {
        GribParameter gp=new GribParameter((short)pid);
        return gp.getShortStr();
//
//        return pid==(-1) ?
//            dad.getBundle().getString("Message.windShortStr") :
//            GribTools.getParameterShortStr(pid);
    }
    private String getParameterLongStr(int pid) {
        GribParameter gp=new GribParameter((short)pid);
        return gp.getLongStr();
//        return pid==(-1) ?
//            dad.getBundle().getString("Message.windLongStr") :
//            GribTools.getParameterLongStr(pid);
    }
    private static final int SHOWINFO_NONE=0;
    private static final int SHOWINFO_FILE=1;
    private static final int SHOWINFO_PARAMETER=2;
    private static final int SHOWINFO_DATE=3;
    
    private int buildInfoFlag(int first,int second,int third) {
        return (first << 4) | (second << 2) | third;
    }
    
    private class LeafInfo {
        File gribFile=null;
        int parameterId=-1;
        int parameterId1=-1;
        int parameterId2=-1;
        Calendar recordDate=null;
        GribRecord.Level level=null;
        int recordNb=-1;
        int recordNb2=-1;
        int showInfo=0;
        
        
        public LeafInfo(File gribFile,int parameterId,int parameterId1, int parameterId2, Calendar recordDate,GribRecord.Level level,int recordNb1,int recordNb2,int infoFlag) {
            this.gribFile = gribFile;
            this.parameterId  = parameterId;
            this.parameterId1 = parameterId1;
            this.parameterId2 = parameterId2;
            this.recordDate = recordDate;
            this.level = level;
            this.recordNb = recordNb1;
            this.recordNb2 = recordNb2;
            this.showInfo = infoFlag;
            
        }
        public LeafInfo(File gribFile,int parameterId, Calendar recordDate,GribRecord.Level level,int recordNb,int infoFlag) {
            this.gribFile = gribFile;
            this.parameterId = parameterId;
            this.parameterId1 = -1;
            this.parameterId2 = -1;
            this.recordDate = recordDate;
            this.level = level;
            this.recordNb = recordNb;
            this.recordNb2 = -1;
            this.showInfo = infoFlag;
            
        }
        public LeafInfo(LeafInfo l,int infoFlag) {
            this.gribFile = l.gribFile;
            this.parameterId = l.parameterId;
            this.parameterId1 = l.parameterId1;
            this.parameterId2 = l.parameterId2;
            this.recordDate = l.recordDate;
            this.level = l.level;
            this.recordNb = l.recordNb;
            this.recordNb2 = l.recordNb2;
            this.showInfo = infoFlag;
        }
        
        public String toString() {
            String ret="";
            for (int i=4 ; i>=0 ; i-=2) {
                int f=(showInfo & (3<<i)) >> i;
                switch (f) {
                    case SHOWINFO_FILE: ret += " "+gribFile.getName(); break;
                    case SHOWINFO_PARAMETER: ret += " "+getParameterShortStr(parameterId); break;
                    case SHOWINFO_DATE: ret += " "+Config.dateToString(recordDate); break;
                    
                }
                
            }
            ret += " ("+level.toString()+")";
            return ret;
        }
    }
    
    
}
