package snpLab.UofM;

import GenomeTree.*;
import HomomorphicEncryption.Paillier;
import FlexSC.flexsc.Flag;
import org.apache.commons.cli.ParseException;
import FlexSC.util.ConfigParser;
import FlexSC.util.EvaRunnable;

import java.math.BigInteger;
import java.util.Arrays;


/**
 * Created by zahidul on 7/28/16.
 */
public class SearchTree {

    //    public BigInteger countNumberOfSnps(Paillier paillier, List columnNames, GenericTree.GenericTreeNode rootNode, String query) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    public BigInteger countNumberOfSnps(Paillier paillier, GenericTree.GenericTreeNode rootNode, String[] snpIdArray) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        BigInteger count = BigInteger.valueOf(0);
        GenericTree.GenericTree gTree = new GenericTree.GenericTree();
        count = gTree.executeQueryOnTree(paillier, rootNode, snpIdArray);

        return count;
    }

    public BigInteger countNumberOfSnps2(Paillier paillier, GenomeTreeNode rootNode, String[] snpIdArray, int [] snpValue, int queryIndexArrayLength, String queryIndexes) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
//    public BigInteger countNumberOfSnps2(Paillier paillier, GenericTree.GenericTreeNode rootNode, String[] snpIdArray, int queryIndexArrayLength, String queryIndexes) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        GenomeTree gTree = new GenomeTree();
        return gTree.executeQueryOnTree4(paillier, rootNode, snpIdArray, snpValue, queryIndexArrayLength, queryIndexes);
//        return gTree.executeQueryOnTree3(paillier, rootNode, snpIdArray, queryIndexArrayLength, queryIndexes);
    }




    public void initiateEvaluator(BigInteger valueWithNoise, int [] snpValue,  int indexNumber, Paillier paillier, int queryIndexArrayLength)throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        ///////////////////////////////////////////////////////////////////
        int evaInput;
        if (valueWithNoise == BigInteger.ZERO && indexNumber < 0) {
            evaInput = 0;
        } else{
            BigInteger decryptedValueWithNoise = paillier.Decryption(valueWithNoise);
            evaInput = decryptedValueWithNoise.intValue() - snpValue[indexNumber];
        }
        ///////////////////////////////////////////////////////////////////
//        BigInteger decryptedValueWithNoise = paillier.Decryption(valueWithNoise);
//        int evaInput = decryptedValueWithNoise.intValue() - snpValue[indexNumber];

        ConfigParser config = new ConfigParser("Config.conf");

        String[] args = new String[4];
//        args[0] = "example.Millionaire";
//        args[1] = String.valueOf(evaInput);

        args[0] = "FlexSC.example.AESDecryption";
//        args[1] = "001100111011011100001101100101011111001001100010011110001110110111001010010";
        args[1] = "00111100000111111110111011101110001010010100100000110011100010010000111111011000100011001111000101001010000110101110101100101101";
        args[2] = String.valueOf(queryIndexArrayLength);
        args[3] = String.valueOf(evaInput);

        Class<?> clazz = Class.forName(args[0]+"$Evaluator");
        EvaRunnable run = (EvaRunnable) clazz.newInstance();
        run.setParameter(config, Arrays.copyOfRange(args, 1, args.length));

        Thread thread2 = new Thread () {
            public void run () {
                run.run();
            }
        };

        thread2.start();



        try {
            thread2.join();
//            synchronized (thread2) {
//                thread2.wait(500);
//            }
        }catch(InterruptedException ie)
        {
            //Log message if required.
        }

        ProcessCSVData.dataCommunication += run.dataCommunication();
//        if(Flag.CountTime)
//            Flag.sw.print();
//        if(Flag.countIO)
//            run.printStatistic();
//        System.out.println("xxxxxxxxxxxxxxxxxxx--------xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    }

    public void initiateEvaluatorV2(BigInteger valueWithNoise, int [] snpValue,  int indexNumber)throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        BigInteger evaInput = valueWithNoise.subtract(BigInteger.valueOf(snpValue[indexNumber]));
    }
}
