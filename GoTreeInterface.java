import java.io.*;
import java.util.*;

public interface GoTreeInterface {
    public void buildTree (int inputNodeNum, int hiddenNodeNum, int outputNodeNum, double mew);
    public void buildTree (File inFile);
    public void train (int[] input, int outcome);
    public double genMove (int[] input);
    public void toFile (File outFile);
}