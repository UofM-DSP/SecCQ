package snpLab.UofM;

import HomomorphicEncryption.Paillier;
import FlexSC.flexsc.Flag;
import FlexSC.util.ConfigParser;
import FlexSC.util.GenRunnable;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.cli.ParseException;



/**
 * Created by zahidul on 8/1/16.
 */
public class CheckEquality {
    public static boolean output = false;

    public boolean equalityCheckViaCircuit(Paillier paillier, BigInteger genInput, int indexNumber) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Random rand = new Random();
        int n = rand.nextInt(1000000000);

        BigInteger noise = BigInteger.valueOf(n);
        BigInteger encryptedNoise = paillier.Encryption(noise);
        BigInteger valueWithNoise = paillier.add(genInput, encryptedNoise);


        ConfigParser config = new ConfigParser("Config.conf");
        String[] args = new String[2];
        args[0] = "example.Millionaire";
        args[1] = String.valueOf(n);//String.valueOf(526444);//


        Class<?> clazz = Class.forName(args[0]+"$Generator");
        GenRunnable run = (GenRunnable) clazz.newInstance();
        run.setParameter(config, Arrays.copyOfRange(args, 1, args.length));

        boolean q = false;
        Thread thread1 = new Thread () {
            public void run () {
               run.run();
            }
        };

        if(Flag.CountTime)
            Flag.sw.print();


        thread1.start();


//        Socket s = null;
//        try{
//            s = new Socket("130.179.30.95", 2002);
//            OutputStream os = s.getOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(os);
//
//
//            ServerResponseSerializable srs = new ServerResponseSerializable(true, BigInteger.valueOf(0), indexNumber, valueWithNoise, paillier);
//            oos.writeObject(srs);
//            oos.close();
//            os.close();
//        }catch(Exception e){
//            System.out.println(e);
//        }finally
//        {
//            try
//            {
//                s.close();
//            }
//            catch(Exception e){}
//        }


        try {
            thread1.join();
        }catch(InterruptedException ie)
        {
            //Log message if required.
        }

        return output;
    }

    public boolean equalityCheckViaCircuitV2(BigInteger genInput, int[] snpValue, int indexNumber) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Random rand = new Random();
        int n = rand.nextInt(1000000000);

        return genInput.equals(BigInteger.valueOf(snpValue[indexNumber]));
//        SearchTree s = new SearchTree();
//        int x = n+indexNumber;
//        BigInteger y =  BigInteger.valueOf(x);
//            s.initiateEvaluatorV2(y, snpValue, indexNumber);
        }



    public boolean equalityCheckViaCircuit3(Paillier paillier, BigInteger genInput, int indexNumber, int [] snpValue, int queryIndexArrayLength, String encryptedBloomFilter, String queryIndexes) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//    public boolean equalityCheckViaCircuit3(Paillier paillier, BigInteger genInput, int indexNumber, int queryIndexArrayLength, String encryptedBloomFilter, String queryIndexes) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//        Random rand = new Random();
//        int n = rand.nextInt(100000);
//        BigInteger noise = BigInteger.valueOf(n);
//        BigInteger encryptedNoise = paillier.Encryption(noise);
//        BigInteger valueWithNoise = paillier.add(genInput, encryptedNoise);

        /////////////////////////////////////////////////////////////
        int n;
        BigInteger valueWithNoise;
        if (genInput == BigInteger.ZERO && indexNumber < 0) {
            n = 0;
            valueWithNoise = BigInteger.ZERO;
        } else {
            Random rand = new Random();
            n = rand.nextInt(100000);
            BigInteger noise = BigInteger.valueOf(n);
            BigInteger encryptedNoise = paillier.Encryption(noise);
            valueWithNoise = paillier.add(genInput, encryptedNoise);
        }
        /////////////////////////////////////////////////////////////




        ConfigParser config = new ConfigParser("Config.conf");
        String[] args = new String[4];
//        args[0] = "example.Millionaire";
//        args[1] = String.valueOf(n);//String.valueOf(526444);//
        args[0] = "FlexSC.example.AESDecryption";
        args[1] = encryptedBloomFilter;
        args[2] = queryIndexes;
        args[3] = String.valueOf(n);


        Class<?> clazz = Class.forName(args[0]+"$Generator");
        GenRunnable run = (GenRunnable) clazz.newInstance();
        run.setParameter(config, Arrays.copyOfRange(args, 1, args.length));

        boolean q = false;
        Thread thread1 = new Thread () {
            public void run () {
                run.run();
            }
        };

        if(Flag.CountTime)
            Flag.sw.print();


        thread1.start();


        try {
            SearchTree st = new SearchTree();
            st.initiateEvaluator(valueWithNoise, snpValue, indexNumber, paillier, queryIndexArrayLength);
//            st.initiateEvaluator(valueWithNoise, indexNumber, paillier, queryIndexArrayLength);
//            synchronized (thread1) {
//                thread1.wait(500);
//            }
            thread1.join();
        }catch(InterruptedException ie)
        {
            //Log message if required.
        }

//        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

        return output;
    }

}
