package snpLab.UofM;

/**
 * Created by zahidul on 7/28/16.
 */

import GenericTree.GenericTree;
import GenericTree.GenericTree.*;
import GenericTree.GenericTreeNode;
import GenericTree.GenericTreeNode.*;
import HomomorphicEncryption.Paillier;
import HomomorphicEncryption.Paillier.*;
//import objectexplorer.MemoryMeasurer;
import guava.guava.src.com.google.common.base.Charsets;
import guava.guava.src.com.google.common.hash.BloomFilter;
import guava.guava.src.com.google.common.hash.Funnel;
import guava.guava.src.com.google.common.hash.Funnels;
import guava.guava.src.com.google.common.hash.PrimitiveSink;
import org.apache.commons.cli.ParseException;
//import java.lang.instrument.Instrumentation;    //to determine the tree size
//import objectexplorer.ObjectGraphMeasurer;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.*;

import java.util.Map;
import java.util.HashMap;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BuildTree {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/countQuery";

    //  Database credentials
    static final String USER_NAME = "root";
    static final String PASS_WORD = "root";

    public static Paillier paillier;

    Statement st = null;
    Connection conn = null;

    public void makeDatabaseConnection() {
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //Open a connection
            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER_NAME, PASS_WORD);
            System.out.println("Connected database successfully...");

            // Create the java statement
//            st = conn.createStatement();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }//finally{
//            //finally block used to close resources
//            try{
//                if(st!=null)
//                    conn.close();
//            }catch(SQLException se){
//            }// do nothing
//            try{
////                if(conn!=null)
////                    conn.close();
//            }catch(SQLException se){
//                se.printStackTrace();
//            }//end finally try
//        }//end try
    }

    public ResultSet executeQuery(String columnName, int limit) {
        ResultSet rs = null;
        try {
            st = conn.createStatement();

            // SQL SELECT query
//            String query = "SELECT rs11686243 FROM Genotype LIMIT 10";
            String query = "SELECT " + columnName + " FROM Genotype_30000_60 LIMIT " + limit;

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }//finally{
        //finally block used to close resources
//            try{
//                if(st!=null)
//                    conn.close();
//            }catch(SQLException se){
//            }// do nothing
//            try{
//                if(conn!=null)
//                    conn.close();
//            }catch(SQLException se){
//                se.printStackTrace();
//            }//end finally try
        //}//end try
        return rs;
    }


    public List getColumnnames(ResultSet results) {
        List columnNames = new ArrayList<>();
        try {
            ResultSetMetaData metaData = results.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Start from 2 to ignore id column
            for (int j = 2; j <= columnCount; j++) {
                columnNames.add(metaData.getColumnName((j)));
            }

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return columnNames;
    }

    public Map mapSnpToId() {
        Map snpIdMap = new HashMap();
        snpIdMap.put("AA", 1);
        snpIdMap.put("AC", 2);
        snpIdMap.put("AG", 3);
        snpIdMap.put("AT", 4);
        snpIdMap.put("CA", 5);
        snpIdMap.put("CC", 6);
        snpIdMap.put("CG", 7);
        snpIdMap.put("CT", 8);
        snpIdMap.put("GA", 9);
        snpIdMap.put("GC", 10);
        snpIdMap.put("GG", 11);
        snpIdMap.put("GT", 12);
        snpIdMap.put("TA", 13);
        snpIdMap.put("TC", 14);
        snpIdMap.put("TG", 15);
        snpIdMap.put("TT", 16);

        return snpIdMap;
    }

//    public GenericTree generateTree(ResultSet rs, List columnNames) {
//        GenericTree gt = new GenericTree();
//        GenericTreeNode rootNode = new GenericTreeNode(BigInteger.valueOf(0));
//        gt.setRoot(rootNode);
//
//        Map snpIdMap = mapSnpToId();
//
//        GenericTreeNode childNode = null;
//        try {
//            while (rs.next()) {
//                GenericTreeNode parentNode = rootNode;
//                for (int j = 0; j < columnNames.size(); j++) {
//                    String columnName = columnNames.get(j).toString();
//                    String columnValue = rs.getString(columnName);
//                    childNode = new GenericTreeNode(BigInteger.valueOf((int)snpIdMap.get(columnValue)));
//                    if (parentNode.hasChildren()) {
//                        List<GenericTreeNode> l = parentNode.getChildren();
//                        int flag = 0;
//                        for (int i = 0; i < l.size(); i++) {
//                            GenericTreeNode gtn = l.get(i);
//                            if (childNode.getData().equals(gtn.getData())) {
//                                gtn.setcount(gtn.getCount().add(BigInteger.valueOf(1)));
//                                parentNode = gtn;
//                                flag = 1;
//                                break;
//                            }
//                        }
//                        if (flag == 0) {
//                            childNode.setSnpID(columnName);
//                            parentNode.addChild(childNode);
//                            parentNode = childNode;
//                        }
//                    } else {
//                        childNode.setSnpID(columnName);
//                        parentNode.addChild(childNode);
//                        parentNode = childNode;
//                    }
//                }
//            }
//        } catch (SQLException se) {
//            //Handle errors for JDBC
//            se.printStackTrace();
//        } catch (Exception e) {
//            //Handle errors for Class.forName
//            e.printStackTrace();
//        }
//        return gt;
//    }

    public GenericTree generateTree(ResultSet rs, List columnNames) {
        GenericTree gt = new GenericTree();
        GenericTreeNode rootNode = new GenericTreeNode(BigInteger.valueOf(0));
        gt.setRoot(rootNode);

        Map snpIdMap = mapSnpToId();

        GenericTreeNode childNode = null;
        try {
            while (rs.next()) {
                GenericTreeNode parentNode = rootNode;
                for (int j = 0; j < columnNames.size(); j++) {
                    String columnName = columnNames.get(j).toString();
                    String columnValue = rs.getString(columnName);
                    childNode = new GenericTreeNode(BigInteger.valueOf((int)snpIdMap.get(columnValue)));
                    if (parentNode.hasChildren()) {
                        List<GenericTreeNode> l = parentNode.getChildren();
                        int flag = 0;
                        for (int i = 0; i < l.size(); i++) {
                            GenericTreeNode gtn = l.get(i);
                            if (childNode.getData().equals(gtn.getData())) {
                                gtn.setcount(gtn.getCount().add(BigInteger.valueOf(1)));
                                parentNode = gtn;
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 0) {
                            childNode.setSnpID(columnName);
                            parentNode.addChild(childNode);
                            parentNode = childNode;
                        }
                    } else {
                        childNode.setSnpID(columnName);
                        parentNode.addChild(childNode);
                        parentNode = childNode;
                    }
                }
            }
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return gt;
    }


    public Paillier encryptTree(GenericTree gt) {
        GenericTree encryptedTree = gt;

        List<GenericTreeNode<GenericTree>> traversalResult = new ArrayList<GenericTreeNode<GenericTree>>();
        Queue a = new LinkedList();
        GenericTreeNode root = encryptedTree.getRoot();
        int count = 0;

        paillier = new Paillier();

        if(root != null) {
            traversalResult = root.getChildren();
            for (GenericTreeNode node:traversalResult) {
                a.add(node);
                count++;
            }


            while(!a.isEmpty()) {
                GenericTreeNode node= (GenericTreeNode) a.remove();

                //encrypt data and count
                node.setData(paillier.Encryption(node.getData()));
                node.setcount(paillier.Encryption(node.getCount()));

                traversalResult = node.getChildren();

                for (GenericTreeNode n:traversalResult) {
                    a.add(n);
                    count++;
                }
            }
        }
        return paillier;

    }







//    public static void main(String [] args) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException{
//        BuildTree bt = new BuildTree();
//        bt.makeDatabaseConnection();
//
////        //get column names
//        ResultSet rs = bt.executeQuery("*", 1);
//////        ResultSet rs = bt.executeQuery("rs11686243,rs4426491,rs4305230", 1);
//        List columnNames = bt.getColumnnames(rs);
//
//
//
////        List<String> snpIDlist = new ArrayList<>();
////        List<Integer> querySNPidList = new ArrayList<>();
////        Map<String, Integer> snpIDMap = new LinkedHashMap<>();
////        Map queryMap = new HashMap();
////        Map tempMap = new HashMap();
////
////        String query = "rs7594717=AG AND rs4338926=CC AND rs10208167=TT AND rs7573958=TT AND rs7598393=AA AND rs4552149=GG AND rs6711471=GG AND rs6547960=CC AND rs4396665=AA AND rs4666257=GG AND rs4666256=AA AND rs11694944=TT AND rs10205397=GG AND rs4666258=AA AND rs2879507=AA AND rs9309671=TT AND rs2339558=GG AND rs9309673=AA AND rs4552149=GG AND rs2339557=AA";
////
////
////        String sortedQuery = "";
////
////        for (int i = 0; i < columnNames.size(); i++) {
////            snpIDMap.put(columnNames.get(i).toString() ,i);
////        }
////        String[] snpArray = query.split("and | AND");
////        for (String s : snpArray) {
////            String snpID = s.split("=")[0].trim();
////            String snpSeq = s.split("=")[1].trim();
////            queryMap.put(snpIDMap.get(snpID),snpSeq);
////            tempMap.put(snpIDMap.get(snpID),snpID);
////            querySNPidList.add(snpIDMap.get(snpID));
////        }
////        Collections.sort(querySNPidList);
////        int count = 1;
////        for (int i:querySNPidList) {
////            if (count==querySNPidList.size())
////                sortedQuery += tempMap.get(i) + "=" + queryMap.get(i);
////            else
////                sortedQuery += tempMap.get(i) + "=" + queryMap.get(i) + " AND ";
////            count++;
////        }
////        //System.out.println("sorted: " + sortedQuery);
////        snpArray = sortedQuery.split("and | AND");
//
//
//
//
////
//        rs = bt.executeQuery("*", 10000);
//////          rs = bt.executeQuery("rs11686243, rs4426491, rs4305230", 5);
//
//        GenericTree gt = bt.generateTree(rs, columnNames);
//        GenericTreeNode rootNode = gt.getRoot();
////
//        paillier = bt.encryptTree(gt);
////        System.out.println("-----------------" + MemoryMeasurer.measureBytes(gt)+"------------------------");
//
//
//        SearchTree s = new SearchTree();
////            BigInteger count = s.countNumberOfSnps(paillier, columnNames, rootNode, "rs11686243=AG and rs4426491=CT and rs4666254 = CC and rs10865513=AG and rs4666258=AC");
////            BigInteger count = s.countNumberOfSnps(paillier, columnNames, rootNode, "rs4426491=CC");// and rs4305230=CC");  //and rs4666258=AC");
////            BigInteger count = s.countNumberOfSnps(pailler, columnNames, rootNode, "rs4635487=AA and rs4666258=AC");
////            BigInteger count = s.countNumberOfSnps(paillier, columnNames, rootNode, "rs4426491=CC");
////            System.out.println(paillier.Decryption(count).toString());
////            System.out.println("=+++++++++++++");
//    }

    public static void main(String args []) {
        Funnel <String> sf = new Funnel<String>() {
            @Override
            public void funnel(String s, PrimitiveSink primitiveSink) {
                primitiveSink.putString(s, Charsets.UTF_8);
            }
        };
//        BloomFilter bf = BloomFilter.create(Funnels.stringFunnel(sf.INSTANCE, 500, 0.5));
        BloomFilter<String> bf = BloomFilter.create(sf, 500, 0.5);
        bf.put("Zahid");
        System.out.println(bf.mightContain("Zahid"));
    }
}
