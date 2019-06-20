package GenomeTree;

import HomomorphicEncryption.Paillier;
import guava.guava.src.com.google.common.hash.BloomFilterStrategies;
import org.apache.commons.cli.ParseException;
import snpLab.UofM.CheckEquality;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zahidul on 2/25/17.
 */
public class GenomeTree<T> {
    private String [] querySnpIdArray;
    private int [] querySnpValue;
    private int queryIndexArrayLengthOfBloomFilter;
    private String queryIndexesOFBloomFilter;
    private Paillier paillierPublicKey;
    private GenomeTreeNode rootNode;
    private int numberOfPatients = 0;

    private CheckEquality checkEq = new CheckEquality();

    private GenomeTreeNode<T> root;

    public GenomeTree() {
        super();
    }

    public GenomeTreeNode<T> getRoot() {
        return this.root;
    }

    public void setRoot(GenomeTreeNode<T> root) {
        this.root = root;
    }

    private int Search(GenomeTreeNode node, int indexNumber) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        String snpID = null;
        if (indexNumber < querySnpIdArray.length) {
            snpID = querySnpIdArray[indexNumber];
        }
        int count = 0;
        boolean z = false;
        List<GenomeTreeNode<T>> children = new ArrayList<GenomeTreeNode<T>>();
        if(!node.equals(rootNode) && indexNumber < querySnpIdArray.length && node.getSnpID().equals(snpID) ) {
            z = checkEq.equalityCheckViaCircuit3(paillierPublicKey, node.getData(), indexNumber, querySnpValue, queryIndexArrayLengthOfBloomFilter, node.getEncryptedBloomFilter(), queryIndexesOFBloomFilter);
            if (z) {
                children = node.getChildren();
                indexNumber++;
                if (children.size() == 0) {
                    numberOfPatients = node.getNumberOfIds();
                }
            }
        }else if(!node.equals(rootNode)){
            z = checkEq.equalityCheckViaCircuit3(paillierPublicKey, BigInteger.ZERO, -1, querySnpValue, queryIndexArrayLengthOfBloomFilter, node.getEncryptedBloomFilter(), queryIndexesOFBloomFilter);
            if (z) {
                children = node.getChildren();
                if (children.size() == 0) {
                    numberOfPatients = node.getNumberOfIds();
                }
            }
        }
        //If the node in the function parameter is the root node
        if (node.equals(rootNode)) {
            children = node.getChildren();
        }
//        System.out.println(paillierPublicKey.Decryption(node.getData())+"     " + numberOfPatients);
        for(GenomeTreeNode childNode:children){
            numberOfPatients = Search(childNode, indexNumber);
        }
        return numberOfPatients;
    }

    public BigInteger executeQueryOnTree4(Paillier paillier, GenomeTreeNode rootNode, String[] snpIdArray, int [] snpValue, int queryIndexArrayLength, String queryIndexes) throws ParseException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        querySnpIdArray = snpIdArray;
        querySnpValue = snpValue;
        queryIndexArrayLengthOfBloomFilter = queryIndexArrayLength;
        queryIndexesOFBloomFilter = queryIndexes;
        paillierPublicKey = paillier;
        this.rootNode = rootNode;


        BigInteger finalCount = BigInteger.valueOf(Search(rootNode, 0));
        return paillier.Encryption(finalCount);
    }


    public void setPaillierPublicKey(Paillier paillier) {
        this.paillierPublicKey = paillier;
    }

    private GenomeTreeNode encryptNode(GenomeTreeNode node) {
        GenomeTreeNode encryptedNode = node;

        encryptedNode.setData(paillierPublicKey.Encryption(node.getData()));
        encryptedNode.setcount(paillierPublicKey.Encryption(node.getCount()));
        encryptedNode.setEncryptedBloomFilter(encryptBloomFilter(node.getBloomFilter().getBitsOfBloomFilter()));


        encryptedNode.setBloomFilter(null);
        return encryptedNode;
    }

    private String encryptBloomFilter(BloomFilterStrategies.BitArray barr) {
        long [] data = barr.getData();
        StringBuilder bloomToBinary = new StringBuilder();

        for (long b : data)
        {
            bloomToBinary.append(b == 0 ? 0:1);
        }

//        String bloomStrKey = "001100111011011100001101100101011111001001100010011110001110110111001010010";
        String bloomStrKey = "00111100000111111110111011101110001010010100100000110011100010010000111111011000100011001111000101001010000110101110101100101101";
        StringBuilder bloomSb = new StringBuilder();

        //XOR the bloomToBinary with bloomStrKey to get the encrypted bloom filter
        for(int i = 0; i < bloomStrKey.length(); i++)
            bloomSb.append((char)(bloomStrKey.charAt(i) ^ bloomToBinary.charAt(i % bloomToBinary.length())));
        String encryptedBloomFilter = bloomSb.toString();

        return encryptedBloomFilter;
    }

    /*
        This function encrypts the whole tree starting from the root node.
    */
    public void encryptTree(GenomeTreeNode node) {
        List<GenomeTreeNode<GenomeTree>> children = node.getChildren();
        for(GenomeTreeNode childNode:children){
            encryptNode(childNode);
            encryptTree(childNode);
        }
    }
}
