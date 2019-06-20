package snpLab.UofM;

/**
 * Created by zahidul on 2/14/17.
 */

import GenomeTree.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;


import HomomorphicEncryption.Paillier;
import guava.guava.src.com.google.common.base.Charsets;
import guava.guava.src.com.google.common.hash.BloomFilter;
import guava.guava.src.com.google.common.hash.BloomFilterStrategies;
import guava.guava.src.com.google.common.hash.Funnel;
import guava.guava.src.com.google.common.hash.PrimitiveSink;


import objectexplorer.ObjectGraphMeasurer;



public class ProcessCSVData{
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/GenomeConditions";

    //  Database credentials
    static final String USER_NAME = "root";
    static final String PASS_WORD = "root";

    private static Statement st = null;
    private static Connection conn = null;

    public static HashMap <String, Integer> phenotypes = new HashMap<String, Integer>();
    private static HashMap <String, String> conditions = new HashMap<String, String>();
    private static Map snpIdMap = new HashMap();
    public static Paillier paillier = new Paillier();


    public static double dataCommunication = 0;


    BloomFilter<String> bloomFilter;
    public static Funnel<String> patientConditionsFunnel = new Funnel<String>() {
        @Override
        public void funnel(String s, PrimitiveSink primitiveSink) {
            primitiveSink.putString(s, Charsets.UTF_8);
        }
    };



    public static void createMapOfPhenotypes() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("condition36_unique.csv"));
        String line;
        int value = 1;
        while((line = br.readLine()) != null) {
            phenotypes.put(line.toLowerCase().trim(), value++);
        }

        br = new BufferedReader(new FileReader("condition36.csv"));
        line = br.readLine();
        while((line = br.readLine()) != null) {
            String [] row = line.split(",");
            String conditionInLowerCase = row[1].toLowerCase();
            if(conditions.containsKey(row[0])){
                String condition = conditions.get(row[0]);
                condition += "," + phenotypes.get(conditionInLowerCase);
                conditions.put(row[0],condition);
            } else {
                conditions.put(row[0], phenotypes.get(conditionInLowerCase).toString());
            }
        }
    }

    public void mapSnpToId() {
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
    }


    public GenomeTreeNode[] addNewChildNode(GenomeTreeNode parentNode, GenomeTreeNode childNode, String [] condition) {
        if(condition != null) {
            childNode.insertIntoBloomFilter(condition);
        }
        parentNode.addChild(childNode);
        return new GenomeTreeNode[]{parentNode, childNode};
    }

    private static void makeDatabaseConnection() {
        try {
            //Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //Open a connection
//            System.out.println("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER_NAME, PASS_WORD);
//            System.out.println("Connected database successfully...");

            // Create the java statement
//            st = conn.createStatement();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    private static void createPatientsConditionTable() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("phenotype_with_diseases.csv"));

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO Conditions (`id`, `patient_id`, `condition`) VALUES (?, ?, ?)");
        String line;
        int temp = 1;
        StringBuilder conditionSB=null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(DB_URL, USER_NAME, PASS_WORD);

            while ((line = br.readLine()) != null) {
                String [] conditionsOfPatients = line.split(",");

                PreparedStatement preparedStmt = conn.prepareStatement(sql.toString());
                preparedStmt.setString(1, String.valueOf(temp));
                preparedStmt.setString(2, conditionsOfPatients[0]);

//                conditionSB = new StringBuilder();
//                for(int i = 1; i< conditionsOfPatients.length; i++) {
//                    conditionSB.append(conditionsOfPatients[i]);
//                    if(i != conditionsOfPatients.length - 1)
//                        conditionSB.append(";");
//                }

                preparedStmt.setString(3, conditionsOfPatients[1]);
                preparedStmt.execute();

                temp++;
            }
//            System.out.println(conditionSB.toString());
        }catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }


    private static void CreateQueries(){
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(DB_URL, USER_NAME, PASS_WORD);

            Statement st = conn.createStatement();
            String query = "SELECT * FROM GENOME_500 WHERE id=4";
//            String query = "SELECT * FROM GENOME_500";

            int size = 50;
            int [] indexArray = new int[size];
            int sum = 0;
            Random rand = new Random();
            for(int i=0; i<size;i++) {
                sum += rand.nextInt(2) + 1;
                indexArray[i] = sum;
            }

            StringBuilder sb = new StringBuilder();

            ResultSet rs = st.executeQuery(query);

            ResultSetMetaData metaData = rs.getMetaData();
            while(rs.next()) {
//                for (int i = 1; i < metaData.getColumnCount(); i++) {
//                    System.out.println(rs.getString(metaData.getColumnName(i)));
//                }
                for(int i=0; i<size;i++) {
                    sb.append(metaData.getColumnName(indexArray[i] + 1) + " = ");
                    sb.append(rs.getString(metaData.getColumnName(indexArray[i] + 1)) + " AND ");
                }
            }
            System.out.println(sb.toString());
        }catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public GenomeTree generateTree() throws FileNotFoundException, IOException{
        GenomeTree gt = new GenomeTree();
        GenomeTreeNode rootNode = new GenomeTreeNode(BigInteger.valueOf(0));
        gt.setRoot(rootNode);

        GenomeTreeNode childNode = null;

        String line;
        String[] snpsOfPatients;



        ArrayList<String>columnNames = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("Genome36.csv"));
        String[] patientIds = br.readLine().split(",");

        StringBuilder sql = new StringBuilder();
        StringBuilder querySB = new StringBuilder();
        querySB.append("CREATE TABLE GENOME_400 (id int, patient_id TEXT, ");
        sql.append("INSERT INTO GENOME_500 (id, patient_id, ");

        int temp = 0;
        while ((line = br.readLine()) != null) {
            temp++;

            snpsOfPatients = line.split(",");
             querySB.append(snpsOfPatients[0] + " TEXT, ");
            columnNames.add(snpsOfPatients[0]);
            sql.append(snpsOfPatients[0] + ", ");



            //System.out.print(snpsOfPatients[k] + " ");
            if (temp == 500) {
                break;
            }
        }

        String queryString = querySB.toString();
        querySB = new StringBuilder();
        querySB.append(queryString.substring(0, queryString.length() - 2));
        querySB.append(")");
        System.out.println(querySB.toString());

        String sqlString = sql.toString();
        sql = new StringBuilder();
        sql.append(sqlString.substring(0, sqlString.length() - 2));
        sql.append(") VALUES (");

        for (int i=0; i<502; i++)
            sql.append("?, ");
        sqlString = sql.toString();
        sql = new StringBuilder();
        sql.append(sqlString.substring(0, sqlString.length() - 2));
        sql.append(")");

        System.out.println(sql.toString());
//        StringBuilder querySB = new StringBuilder();
//        querySB.append("id int, ");
        try {
            Class.forName(JDBC_DRIVER);
            Connection conn = DriverManager.getConnection(DB_URL, USER_NAME, PASS_WORD);

                    for(int k = 3; k<176; k++){
//            for (int k = 3; k < 8; k++) {
                GenomeTreeNode parentNode = rootNode;
                temp = 0;
                br = new BufferedReader(new FileReader("Genome36.csv"));
                String [] patientCondition = br.readLine().split(",");

                String [] condition = null;
                if (conditions.get(patientCondition[k]) != null) {
                    condition = conditions.get(patientCondition[k]).split(",");
                }

                PreparedStatement preparedStmt = conn.prepareStatement(sql.toString());
                preparedStmt.setString(temp+1, String.valueOf(k-2));
                preparedStmt.setString(temp+2, patientIds[k]);

                while ((line = br.readLine()) != null) {
                    temp++;

                    snpsOfPatients = line.split(",");
                    // if (k==3)querySB.append(snpsOfPatients[0] + " TEXT, ");
//                    if (k == 3) columnNames.add(snpsOfPatients[0]);
                    preparedStmt.setString(temp+2, snpsOfPatients[k]);
                    childNode = new GenomeTreeNode(BigInteger.valueOf((int) snpIdMap.get(snpsOfPatients[k])));
                    childNode.setSnpID(snpsOfPatients[0]);
//                    childNode.setChromosome(snpsOfPatients[1]);
//                    childNode.setPosition(snpsOfPatients[2]);

                    if (parentNode.hasChildren()) {
                        List<GenomeTreeNode> listOfChildren = parentNode.getChildren();
                        int flag = 0;
                        for (GenomeTreeNode gtn : listOfChildren) {
                            if (childNode.getData().equals(gtn.getData())) {
                                gtn.setcount(gtn.getCount().add(BigInteger.valueOf(1)));

                                if (condition != null) {
                                    gtn.insertIntoBloomFilter(condition);
                                }

                                parentNode = gtn;
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 0) {
                            GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                            parentNode = parentAndChildNodes[1];
                        }
                    } else {
                        GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                        parentNode = parentAndChildNodes[1];
                    }

                    //System.out.print(snpsOfPatients[k] + " ");
                    if (temp == 500) {
                        parentNode.addToListOfIDs(patientCondition[k]);
                        break;
                    }
                }
//                System.out.println(preparedStmt);
                preparedStmt.execute();
            }

//        System.out.println(querySB.substring(0,querySB.length() - 2));
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return gt;
    }



    private ResultSet executeQuery(String columnName) {
        ResultSet rs = null;
        try {
            st = conn.createStatement();

            // SQL SELECT query
//            String query = "SELECT rs11686243 FROM Genotype LIMIT 10";
//            String query = "SELECT " + columnName + " FROM GENOME_1000 LIMIT 5";
//            String query = "SELECT " + columnName + " FROM GENOME_500 LIMIT " + limit;
            String query = "SELECT " + columnName + " FROM GENOME_500";
//
            // execute the query, and get a java resultset
            rs = st.executeQuery(query);
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return rs;
    }

    private List getColumnnames(ResultSet results) {
        List columnNames = new ArrayList<>();
        try {
            ResultSetMetaData metaData = results.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Start from 2 to ignore id column
            for (int j = 3; j <= columnCount; j++) {
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

    public GenomeTree generateTree2() throws FileNotFoundException, IOException{
        GenomeTree gt = new GenomeTree();
        GenomeTreeNode rootNode = new GenomeTreeNode(BigInteger.valueOf(0));
        gt.setRoot(rootNode);

        GenomeTreeNode childNode = null;

        BufferedReader br = new BufferedReader(new FileReader("Genome36.csv"));


//        for(int k = 3; k<176; k++){
            for (int k = 3; k < 8; k++) {
            GenomeTreeNode parentNode = rootNode;
            int temp = 0;
            br = new BufferedReader(new FileReader("Genome36.csv"));
            String [] patientCondition = br.readLine().split(",");

            String [] condition = null;
            if (conditions.get(patientCondition[k]) != null) {
                condition = conditions.get(patientCondition[k]).split(",");
            }



            String line;
            while ((line = br.readLine()) != null) {
                temp++;

                String [] snpsOfPatients = line.split(",");
                childNode = new GenomeTreeNode(BigInteger.valueOf((int) snpIdMap.get(snpsOfPatients[k])));
                childNode.setSnpID(snpsOfPatients[0]);
//                childNode.setChromosome(snpsOfPatients[1]);
//                childNode.setPosition(snpsOfPatients[2]);

                if (parentNode.hasChildren()) {
                    List<GenomeTreeNode> listOfChildren = parentNode.getChildren();
                    int flag = 0;
                    for (GenomeTreeNode gtn : listOfChildren) {
                        if (childNode.getData().equals(gtn.getData())) {
                            gtn.setcount(gtn.getCount().add(BigInteger.valueOf(1)));

                            if (condition != null) {
                                gtn.insertIntoBloomFilter(condition);
                            }

                            parentNode = gtn;
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 0) {
                        GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                        parentNode = parentAndChildNodes[1];
                    }
                } else {
                    GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                    parentNode = parentAndChildNodes[1];
                }

                //System.out.print(snpsOfPatients[k] + " ");
                if (temp == 100) {
                    parentNode.addToListOfIDs(patientCondition[k]);
                    break;
                }
            }
        }

//
        return gt;
    }

    public GenomeTree generateTree3(ResultSet rs, List columnNames) {
        GenomeTree gt = new GenomeTree();
        GenomeTreeNode rootNode = new GenomeTreeNode(BigInteger.valueOf(0));
        gt.setRoot(rootNode);

        GenomeTreeNode childNode = null;
        try {
            while (rs.next()) {
//                System.out.println(rs.getString("rs737657"));
                GenomeTreeNode parentNode = rootNode;
                String [] condition = null;
                if (conditions.get(rs.getString("patient_id")) != null) {
                    condition = conditions.get(rs.getString("patient_id")).split(",");
                }


                for (int j = 0; j < columnNames.size(); j++) {
//                for (int j = 0; j < 100; j++) {
                    String columnName = columnNames.get(j).toString();
                    String columnValue = rs.getString(columnName);
                    childNode = new GenomeTreeNode(BigInteger.valueOf((int)snpIdMap.get(columnValue)));
                    childNode.setSnpID(columnName);

                    if (parentNode.hasChildren()) {
                        List<GenomeTreeNode> listOfChildren = parentNode.getChildren();
                        int flag = 0;
                        for (GenomeTreeNode gtn : listOfChildren) {
                            if (childNode.getData().equals(gtn.getData())) {
                                gtn.setcount(gtn.getCount().add(BigInteger.valueOf(1)));
                                if (condition != null) {
                                    gtn.insertIntoBloomFilter(condition);
                                }
                                parentNode = gtn;
                                flag = 1;
                                break;
                            }
                        }
                        if (flag == 0) {
                            GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                            parentNode = parentAndChildNodes[1];
                        }
                    } else {
                        GenomeTreeNode[] parentAndChildNodes = addNewChildNode(parentNode, childNode, condition);
                        parentNode = parentAndChildNodes[1];
                    }
                }
                parentNode.addToListOfIDs(rs.getString("patient_id"));
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




    public static void main(String[] args) throws Exception {
        createMapOfPhenotypes();

        ProcessCSVData pcd = new ProcessCSVData();
        pcd.mapSnpToId();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

//        createPatientsConditionTable();
//        CreateQueries();

        pcd.makeDatabaseConnection();
        ResultSet rs = pcd.executeQuery("*");
        List columnNames = pcd.getColumnnames(rs);

//        GenomeTree gt = pcd.generateTree();
//
        Date date1 = new Date();
        System.out.println("Data fetching Time: " + (date1.getTime() - date.getTime()));
//

        date = new Date();
//        GenomeTree gt = pcd.generateTree();
        GenomeTree gt = pcd.generateTree3(rs, columnNames);
        GenomeTreeNode rootNode = gt.getRoot();


        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Tree Size : " + memory/1024.0);

        date1 = new Date();

        System.out.println("Tree building Time: " + (date1.getTime() - date.getTime()));


        date = new Date();

        gt.setPaillierPublicKey(paillier);
        gt.encryptTree(rootNode);


        runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        memory = runtime.totalMemory() - runtime.freeMemory();

        date1 = new Date();
        System.out.println("Encryption Time: " + (date1.getTime() - date.getTime()));
        System.out.println("Encrypted size of the tree: " + memory/1024.0);

//        System.out.println("-------------------------------------------------------------------------");
//
//        Set set = phenotypes.entrySet();
//        Iterator it = set.iterator();
//
//        while(it.hasNext()) {
//            Map.Entry mapEntry = (Map.Entry) it.next();
//            System.out.println(mapEntry.getKey() + ":      " + mapEntry.getValue());
//            if (mapEntry.getKey() == "advanced patellar osteoarthritis")
//                System.out.println("-------------------------------------------------------------------------");
//        }
//
//        System.out.println("-------------------------------------------------------------------------");


        date = new Date();

//        String [] s = "rs737657 = GG AND rs1983865 = CT AND Condition = ASTHMA".split("and|AND");
//        String [] s = "rs7086391 = CT AND rs10508381 = AA AND rs7917347 = GG AND rs10786410 = AA AND rs4919210 = TT AND rs753299 = CT AND rs11189559 = AG AND rs7894393 = TT AND rs17109576 = CC AND rs2147896 = AA AND rs17109675 = TT AND Condition = Polycystic Ovary Syndrome (PCOS);Skin tags;Type 2 Diabetes".split("and|AND");
//        String [] s = "rs17524355 = CT AND rs11189525 = GG AND rs7899882 = TT AND rs10508383 = AA AND rs10508380 = CC AND rs10883059 = CT AND rs7902229 = GG AND rs6584182 = CC AND rs7917347 = GG AND rs4917813 = GT AND rs11189551 = CC AND rs942800 = CC AND rs4917817 = AA AND rs4919210 = TT AND rs11189555 = AG AND rs10883072 = AG AND rs753299 = CT AND rs4919218 = CT AND rs11189572 = CC AND rs1339693 = CC AND Condition = Polycystic Ovary Syndrome (PCOS);Skin tags;Type 2 Diabetes".split("and|AND");

//        String [] s = "rs7086391 = CT AND rs10508381 = AA AND rs7917347 = GG AND rs10786410 = AA AND rs4919210 = TT AND rs753299 = CT AND rs11189559 = AG AND rs7894393 = TT AND rs17109576 = CC AND rs2147896 = AA AND rs17109675 = TT AND rs7075480 = GG AND rs11592273 = CC AND rs11189603 = GG AND rs10883099 = GG AND rs11189628 = CT AND rs688236 = GG AND rs11189698 = TT AND rs12247273 = AA AND rs17110576 = GG AND rs17110706 = TT AND rs861399 = TT AND rs10883221 = CT AND rs947403 = AG AND rs12258897 = CT AND rs11189937 = GG AND rs7921055 = GT AND rs11517066 = TT AND rs4144925 = CC AND Condition = Polycystic Ovary Syndrome (PCOS);Skin tags;Type 2 Diabetes".split("and|AND");


        String [] s = "rs12241269 = GG AND rs12784535 = CC AND rs4749918 = AA AND rs6584182 = CC AND rs17453066 = CT AND rs11189541 = AG AND rs17146745 = CC AND rs11189555 = AG AND rs11189559 = AG AND rs1339693 = CC AND rs4345897 = AA AND rs2147897 = GG AND rs17109650 = TT AND rs7072096 = AA AND rs2296436 = TT AND rs2296431 = CC AND rs11594426 = CT AND rs11189603 = GG AND rs10883099 = GG AND rs1998756 = CT AND rs11189629 = CC AND rs1889974 = GG AND rs688236 = GG AND rs7080284 = GG AND rs605591 = AA AND rs11189698 = TT AND rs12268248 = GG AND rs4554816 = TT AND rs595350 = GG AND rs658053 = TT AND rs566626 = CC AND rs473820 = CC AND rs478005 = CT AND rs11189765 = AA AND rs11189792 = AG AND rs12262933 = CC AND rs1252314 = CC AND rs1414970 = TT AND rs2210887 = AA AND rs2398002 = GG AND rs7089519 = CC AND rs12251683 = CC AND rs11189867 = TT AND rs3897502 = CC AND rs11253543 = AC AND rs4579871 = GG AND rs12572102 = GG AND rs1539397 = GT AND rs4919276 = CT AND rs10786510 = CT AND rs12241269 = GG AND rs12784535 = CC AND rs4749918 = AA AND rs6584182 = CC AND rs17453066 = CT AND rs11189541 = AG AND rs17146745 = CC AND rs11189555 = AG AND rs11189559 = AG AND rs1339693 = CC AND rs4345897 = AA AND rs2147897 = GG AND rs17109650 = TT AND rs7072096 = AA AND rs2296436 = TT AND rs2296431 = CC AND rs11594426 = CT AND rs11189603 = GG AND rs10883099 = GG AND rs1998756 = CT AND rs11189629 = CC AND rs1889974 = GG AND rs688236 = GG AND rs7080284 = GG AND rs605591 = AA AND rs11189698 = TT AND rs12268248 = GG AND rs4554816 = TT AND rs595350 = GG AND rs658053 = TT AND rs566626 = CC AND rs473820 = CC AND rs478005 = CT AND rs11189765 = AA AND rs11189792 = AG AND rs12262933 = CC AND rs1252314 = CC AND rs1414970 = TT AND rs2210887 = AA AND rs2398002 = GG AND rs7089519 = CC AND rs12251683 = CC AND rs11189867 = TT AND rs3897502 = CC AND rs11253543 = AC AND rs4579871 = GG AND rs12572102 = GG AND rs1539397 = GT AND rs4919276 = CT AND rs10786510 = CT AND Condition = Polycystic Ovary Syndrome (PCOS);Skin tags;Type 2 Diabetes;Sleep Apnea;Deviated septum".replaceAll("\'","").split("and|AND");
        String [] snpIdArray = new String[s.length - 1];
        int [] snpValue = new int[s.length - 1];
        int i = 0;
        for(String a: s){
            String[] splittedQuery = a.split("=");
            snpIdArray[i] = splittedQuery[0].trim();
//            System.out.println(splittedQuery[1].trim());
            snpValue[i] = (int)snpIdMap.get(splittedQuery[1].trim());
            i++;
            if (i == s.length - 1) break;
        }
        String[] splittedQuery = s[s.length - 1].split("=");
        String [] conditionsFromQuery = splittedQuery[1].split(";");

        BloomFilter<String> queryBloomFilter;
        queryBloomFilter = BloomFilter.create(patientConditionsFunnel, 850, 0.01);
//        queryBloomFilter.put(phenotypes.get(splittedQuery[1].toLowerCase().trim()).toString());

        for(String qCondition:conditionsFromQuery){
            if(phenotypes.get(qCondition.toLowerCase().trim()) != null){
//                System.out.println(qCondition.toLowerCase().trim());
                queryBloomFilter.put(phenotypes.get(qCondition.toLowerCase().trim()).toString());
            }
        }

        BloomFilterStrategies.BitArray barr = queryBloomFilter.getBitsOfBloomFilter();
        long [] data = barr.getData();
        StringBuilder queryIndexes = new StringBuilder();
        StringBuilder bloomToBinaryQuery = new StringBuilder();
        int totalIndexNumber = 0;
        for (i = 0; i < data.length; i++)
        {
            if (data[i] == 0){
                bloomToBinaryQuery.append(0);
            } else {
                bloomToBinaryQuery.append(1);
                if(queryIndexes.length() > 0) {
                    queryIndexes.append("," + i);
                } else {
                    queryIndexes.append(i);
                }
                totalIndexNumber++;
            }
        }
//        String aaas = "00111100000111111110111011101110001010010100100000110011100010010000111111011000100011001111000101001010000110101110101100101101";
        date1 = new Date();
        System.out.println("Query Preprocessing Time: " + (date1.getTime() - date.getTime()));

        date = new Date();

        SearchTree st = new SearchTree();
        BigInteger count = st.countNumberOfSnps2(paillier, rootNode, snpIdArray, snpValue, totalIndexNumber, queryIndexes.toString());

        System.out.println("Result: " + paillier.Decryption(count).toString());
        System.out.println("Communication overhead: " + dataCommunication);

        date1 = new Date();
        System.out.println("Query Evaluation Time: " + (date1.getTime() - date.getTime()));
    }


}
