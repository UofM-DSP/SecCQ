package FlexSC.example;

import FlexSC.circuits.arithmetic.IntegerLib;
import FlexSC.flexsc.CompEnv;
import FlexSC.gc.BadLabelException;
import snpLab.UofM.CheckEquality;
import FlexSC.util.EvaRunnable;
import FlexSC.util.GenRunnable;
import FlexSC.util.Utils;

/**
 * Created by zahidul on 12/5/16.
 */
public class AESDecryption {

    static public<T> T compute(CompEnv<T> gen, T[] inputA, T[] inputB, T[][] inputC, T[] inputD, T[] inputE){
//    static public<T> T[] compute(CompEnv<T> gen, T[] inputA, T[] inputB){
        T[] c =  new IntegerLib<T>(gen).xor(inputA, inputB);
//        T[] d = ;
        T d = new IntegerLib<T>(gen).checkArrayIndexElements(inputC, c);
        T e = new IntegerLib<T>(gen).eq(inputD, inputE);
        T f = new IntegerLib<T>(gen).and(d,e);
        return f;
    }

    public static class Generator<T> extends GenRunnable<T> {

        T[] inputA;
        T[] inputB;
        T[][] inputC;
        T[] inputD;
        T[] inputE;
        T scResult;

        @Override
        public void prepareInput(CompEnv<T> gen) {
            boolean[] in = Utils.fromString(args[0]);
            inputA = gen.inputOfAlice(in);
            gen.flush();
            inputB = gen.inputOfBob(new boolean[in.length]);
            /////////////////////////////////////////////
            gen.flush();
//            boolean [][] inC = new boolean[1][32];
            String[] indexes = args[1].split(",");
            inputC = gen.newTArray(indexes.length, 32);
            for (int i = 0; i < inputC.length; i++) {
                inputC[i] = gen.inputOfAlice(Utils.fromInt(new Integer(indexes[i].replaceAll("\\s+", "")), 32));
            }
            gen.flush();
            inputD = gen.inputOfAlice(Utils.fromInt(new Integer(args[2]), 32));
            gen.flush();
            inputE = gen.inputOfBob(new boolean[32]);
            /////////////////////////////////////////////
        }

        @Override
        public void secureCompute(CompEnv<T> gen) {
//            scResult = compute(gen, inputA, inputB);
            scResult = compute(gen, inputA, inputB, inputC, inputD, inputE);
        }

        @Override
        public void prepareOutput(CompEnv<T> gen) throws BadLabelException {
            CheckEquality.output = gen.outputToAlice(scResult);
//            Boolean [] o = Utils.toBooleanArray(gen.outputToAlice(scResult));
//            String oString = Utils.toString(gen.outputToAlice(scResult));
//            String [] oStringArr = oString.split("");
//            System.out.println("---------------Output to Alice: " + Utils.toString(gen.outputToAlice(scResult)));
//            System.out.println("---------------Output to Alice: " + gen.outputToAlice(scResult));
//            System.out.println("---------------Output to Alice: " + gen.outputToAliceForIndexChecking(scResult));


        }
    }

    public static class Evaluator<T> extends EvaRunnable<T> {
        T[] inputA;
        T[] inputB;
        T[][] inputC;
        T[] inputD;
        T[] inputE;
        T scResult;

        @Override
        public void prepareInput(CompEnv<T> gen) {
            boolean[] in = Utils.fromString(args[0]);
            inputA = gen.inputOfAlice(new boolean[in.length]);
            gen.flush();
            inputB = gen.inputOfBob(in);
            //////////////////////////////////////////////////
            gen.flush();
            inputC = gen.newTArray(Integer.valueOf(args[1]), 32);
            for (int i = 0; i < inputC.length; i++) {
                inputC[i] = gen.inputOfAlice(new boolean[32]);
            }
            gen.flush();
            inputD = gen.inputOfAlice(new boolean[32]);
            gen.flush();
            inputE = gen.inputOfBob(Utils.fromInt(new Integer(args[2]), 32));
            //////////////////////////////////////////////////
        }

        @Override
        public void secureCompute(CompEnv<T> gen) {
//            scResult = compute(gen, inputA, inputB);
            scResult = compute(gen, inputA, inputB, inputC, inputD, inputE);
        }

        @Override
        public void prepareOutput(CompEnv<T> gen) throws BadLabelException {
            gen.outputToAlice(scResult);
//            System.out.println("---------------Output to Alice: ---------------" + gen.outputToAlice(scResult));
        }
    }
}
