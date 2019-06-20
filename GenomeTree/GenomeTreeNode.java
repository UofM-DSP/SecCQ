package GenomeTree;

import guava.guava.src.com.google.common.base.Charsets;
import guava.guava.src.com.google.common.hash.BloomFilter;
import guava.guava.src.com.google.common.hash.BloomFilterStrategies;
import guava.guava.src.com.google.common.hash.Funnel;
import guava.guava.src.com.google.common.hash.PrimitiveSink;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zahidul on 2/25/17.
 */
public class GenomeTreeNode<T> {
    private String snpID;
//    private String chromosome;
//    private String position;

    private BigInteger data;
    private BigInteger count;
    private List<GenomeTreeNode<T>> children;
    private GenomeTreeNode<T> parent;

    private ArrayList<String> listOfIds = new ArrayList<String>();
    private BloomFilter<String> bloomFilter;
    private Funnel<String> patientConditionsFunnel = new Funnel<String>() {
        @Override
        public void funnel(String s, PrimitiveSink primitiveSink) {
            primitiveSink.putString(s, Charsets.UTF_8);
        }
    };
    private String encryptedBloomFilter;

    public GenomeTreeNode() {
        super();
        children = new ArrayList<GenomeTreeNode<T>>();
        count = BigInteger.ONE.valueOf(1);
        bloomFilter = BloomFilter.create(patientConditionsFunnel, 850, 0.01);
    }

    public GenomeTreeNode(BigInteger data) {
        this();
        setData(data);
    }

    public GenomeTreeNode<T> getParent() {
        return this.parent;
    }

    public List<GenomeTreeNode<T>> getChildren() {
        return this.children;
    }

    public int getNumberOfChildren() {
        return getChildren().size();
    }

    public boolean hasChildren() {
        return (getNumberOfChildren() > 0);
    }

    public void setChildren(List<GenomeTreeNode<T>> children) {
        for(GenomeTreeNode<T> child : children) {
            child.parent = this;
        }

        this.children = children;
    }

    public void addChild(GenomeTreeNode<T> child) {
        child.parent = this;
        children.add(child);
    }

    public BigInteger getData() {
        return this.data;
    }

    public void setData(BigInteger data) {
        this.data = data;
    }

    public BigInteger getCount() {
        return this.count;
    }

    public void setcount(BigInteger count) {
        this.count = count;
    }

    public String getSnpID() {
        return this.snpID;
    }

    public  void setSnpID(String snpID) {
        this.snpID = snpID;
    }

//    public void setChromosome(String chromosome) {
//        this.chromosome = chromosome;
//    }
//
//    public String getChromosome(){
//        return this.chromosome;
//    }
//
//    public void setPosition(String position) {
//        this.position = position;
//    }
//
//    public String getPosition() {
//        return this.position;
//    }

    public void setEncryptedBloomFilter(String encryptedBloomFilter) {
        this.encryptedBloomFilter = encryptedBloomFilter;
    }

    public String getEncryptedBloomFilter() {
        return this.encryptedBloomFilter;
    }


    public void setBloomFilter(BloomFilter<String> bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    public BloomFilter<String> getBloomFilter(){
        return this.bloomFilter;
    }

    public void insertIntoBloomFilter(String [] condition){
        for (String cond: condition) {
            this.bloomFilter.put(cond);
        }
    }

    public boolean checkValueInBloomFilter(String icdCode) {
        return this.bloomFilter.mightContain(icdCode);
    }

    public void addToListOfIDs(String patientID) {
        if(!this.listOfIds.contains(patientID)) this.listOfIds.add(patientID);
    }

    public int getNumberOfIds() {
//        System.out.println(this.listOfIds);
        return this.listOfIds.size();
    }

    public BloomFilterStrategies.BitArray getBitsFromBloomFilter() {
        return this.bloomFilter.getBitsOfBloomFilter();
    }
}
