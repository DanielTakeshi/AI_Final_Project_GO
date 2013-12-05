import java.io.*;
import java.util.*;

public interface GoTreeInterface {
    public void buildTree (int inputNodeNum, int hiddenNodeNum, double mew);
    public void buildTree (File inFile);
    public void train (int[] input, int outcome) throws Exception;
    public double genMove (int[] input);
    public void toFile (File outFile);
}