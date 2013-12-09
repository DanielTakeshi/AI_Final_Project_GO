import structure5.Vector;
import java.io.*;
import java.util.*;
import java.lang.System;

/*
 * A Feed Forward Tree -- designed for playing Go -- simon chase
 *
 * The FFTree is implemented as a series of flat arrays and vector of arrays containing the weights, output, and error
 * value for each node.  There is a single output node for this particular FFTree, but it could easily be extended
 * to have multiple output nodes.
 *
 * The constructor is empty -- buildTree(file) or buildTree(int...) must be called to initialize the tree to the state
 * saved in a file, or to a random initialization, before the ann is functional.
 *
 * train(int[] input, int outcome) takes an int array that represents the board state around a possible move, 
 * and an outcome integer in {0, 1} that
 * indicates whether the move was a "pro" move or not: the input is tracing a game, and {0} is presented with input that 
 * does not represent the actual move made by the professional player (but 1 does).
 *
 * train itself calls the following functions
 * 1. propagateForward, which calculates the output value for each node as given by the input propagated
 * through the tree.
 * 2. propagateBackward, which calculates the error values for each node as given by the desired output.
 * 3. update, which commits the changes to the weights given by the error values
 *
 * genMove (int[] input) takes a board view around a (presumably) empty position, and returns the value in range (0,1)
 * that it associates with making that move.
 *
 * toFile writes the tree out to a file: if no file is explicitly supplied, then the tree writes itself to a unique file name
 * given by its id and the time when toFile was called. 
 *
 * setMew(double mewIn) sets the learning rate to be the input mewIn.
 */

public class GoTree implements GoTreeInterface {
    // unique id for the tree
    long myID;

    // training counter for the goTree
    long trainingNum;

    // input, hidden, and output num are the node counts for each layer
    int inputNum;
    int hiddenNum;
    // number of hidden nodes including the extra bias node
    int hiddenNumWithBias;

    // mew is the learning rate
    double mew;

    // the hidden weight vector holds the weight array i for hidden node i for all input nodes
    // the hidden Output and hidden error are for feed forward and backwards, where they hold intermediate values
    Vector<Double[]> hiddenWeight;
    Double[] hiddenOutput;
    Double[] hiddenError;
    
    // the output weights, single value, and error for the output node
    Double[] outputWeight;
    double outputValue;
    double outputError;
    
    public GoTree () {
    }

    /*
     * The buildTree method has two forms:
     *
     * 1. buildTree (file) will build the ann by reading in from a file and initializing the tree to the state 
     * saved in the file.  The file format is unique to the toFile and buildTree (file) methods - they are complementary
     * methods.
     *
     * 2. buildTree (int...) will build a new ann from "scratch" to the given specifications -- the weights for all nodes
     * are initialized to random values between -.5 and .5
     */


    public void buildTree (File inFile) {
	// build the tree from a file where we have saved the tree

	try {
	    Scanner scanny = new Scanner(inFile);
	    
	    // read in the first line -- has the input number, hidden num, learning rate, and the tree ID in the first line
	    String[] firstLine = scanny.nextLine().split(",");

	    inputNum = Integer.parseInt(firstLine[0]);
	    hiddenNum = Integer.parseInt(firstLine[1]);
	    hiddenNumWithBias = hiddenNum + 1;

	    mew = Double.parseDouble(firstLine[2]);
	    myID = Long.parseLong(firstLine[3]);

	    if (firstLine.length > 4) {
		trainingNum = Long.parseLong(firstLine[4]);
	    } else {
		trainingNum = 0;
	    }

	    // initialize value and error structs
	    // +1 indicates bias term, we don't want to iterate through it
	    // in the first layer; only in the hidden layer
	    hiddenOutput = new Double[hiddenNumWithBias];
	    hiddenError = new Double[hiddenNumWithBias];
	    // TODO: SHOULD HIDDENNUM BE PASED AS A PARAMETER HERE??
	    hiddenWeight = new Vector<Double[]>();

	    outputWeight = new Double[hiddenNumWithBias];

	    scanny.nextLine();
 
	    // read in the lines for each hidden node weight array
	    // bias not included in hiddenWeights array
	    for (int i = 0; i < hiddenNum; i++) {
		String[] hiddenWeightLine = scanny.nextLine().split(",");
		Double[] hiddenWeightArray = new Double[hiddenWeightLine.length];
		
		for (int p = 0; p < hiddenWeightLine.length; p++) {
		    hiddenWeightArray[p] = Double.parseDouble(hiddenWeightLine[p]);
		}

		// store weight array into double weight vector
		hiddenWeight.add(i, hiddenWeightArray);
	    }
	    
	    scanny.nextLine();

	    // read output weight array into outputWeightArray;
	    String[] outputWeightLine = scanny.nextLine().split(",");

	    // outputWeight includes weight from bias term in hidden layer to output
	    for (int i = 0; i < hiddenNumWithBias; i++) {
		outputWeight[i] = Double.parseDouble(outputWeightLine[i]);
	    }
	    // done
	} catch (IOException e) {
	    
	}
    }

    public void buildTree (int inputIn, int hiddenIn, double mewIn) {
	//initialize new id for this tree from sys.nanoTime
	myID = System.nanoTime();
	trainingNum = 0;

	//store input values
	inputNum = inputIn;
	hiddenNum = hiddenIn;
	hiddenNumWithBias = hiddenNum + 1;

	mew = mewIn;

	//initialize weight vectors for each hidden node
	//at the same time, initialize hidden ouput arrays
	hiddenWeight = new Vector<Double[]>(hiddenNum);

	// Adding bias term for hidden layer
	hiddenOutput = new Double[hiddenNumWithBias];
	hiddenError = new Double[hiddenNumWithBias];

	Random rando = new Random();

	// Adding bias term for hidden layer
	outputWeight = new Double[hiddenNumWithBias];

	for(int i = 0; i < hiddenNum; i++){

	    //initialize each weight array to have values between -.5 and .5
	    Double[] weightArray = new Double[inputNum];

	    for(int p = 0; p < inputNum; p++){
		weightArray[p] = rando.nextDouble() - 0.5;
	    }

	    //store in Vector at index of hidden node
	    hiddenWeight.add(i, weightArray);

	    // initialize output weight entry for hidden node i to random number
	    outputWeight[i] = rando.nextDouble() - 0.5;
	}
	
	// Adding extra bias term to outputWeight
	int bias_index = hiddenNumWithBias - 1;
	outputWeight[bias_index] = rando.nextDouble() - 0.5;
	
	// initialization is complete
    }

    /*
     * train takes the integer array of the board state around an empty spot, and the
     * integer outcome 0/1 indicating whether it should have actually made the move.
     *
     * It propagates the input forward, and the outcome backwards, and then stores into 
     * the tree the error generated by the backprop algorithm.
     */

    public void train (int[] input, int outcome) throws Exception {
	if (input.length != inputNum) {
	    throw new Exception ("Malformed input -- incorrect size");
	}
	
	//call propagate forward
	this.propagateForward(input);

	//propagate errors backward through tree
	this.propagateBackward(outcome);

	//update tree
	this.update(input);
	trainingNum++;
    }

    private void propagateForward (int[] input) {
	
	//for each hidden node (excluding bias node):
	for (int i = 0; i < hiddenNum; i++) {

	    //operating on a single hidden node
	    Double[] tempWeight = hiddenWeight.get(i);

	    //sum over inputs x_i * w_i for the input layer
	    double net = 0.0;

	    // input.length includes input bias term
	    for (int p = 0; p < input.length; p++) {
		net += ((double) input[p]) * tempWeight[p];
	    }

	    //perform sigmoid function
	    net = 1 / (1 + (Math.exp(-net)));

	    //store net into hidden node's output array
	    hiddenOutput[i] = net;
	}	 

	// Adding bias for hidden layer
	// hiddenOutput of length hiddenNum+1\
	int bias_index = hiddenNumWithBias - 1;
	hiddenOutput[bias_index] = 1.0;

	//propagate hidden output to single output node
	//sum over inputs x_i*w_i
	double net = 0.0;

	// Add 1 because of bias term
	// hiddenOutput and outputWeight are of length hiddenNum+1
	for(int p = 0; p < hiddenNumWithBias; p++){
	    net += hiddenOutput[p] * outputWeight[p];
	}

	//perform sigmoid function
	net  = 1/(1 + (Math.exp(-net)));

	//store net as outputValue
	outputValue = net;
    }

    private void propagateBackward (int outcome) {
	// calculate error for output unit
	outputError = outputValue * (1.0 - outputValue) * (outcome - outputValue);

 	// calculate error on each hidden unit -- for a single unit we do 
	// error_i = output_i (1 - output_i) * sum_over_outputs_k [output_weight_ik * output_error_k]
	// but there is only one output node, so k = 1

	// hiddenError, hiddenOutput, and outputWeight are of length hiddenNum+1
	for(int i = 0; i < hiddenNumWithBias; i++){
	    hiddenError[i] = hiddenOutput[i] * (1 - hiddenOutput[i]) * outputWeight[i] * outputError;
	}
    }

    private void update (int[] input) {
	//call to update edge weights for every hidden node and every output node
	double error;
	Double[] tempArray;

	//update hidden node weights
	//DO NOT need bias term from hidden layer here
	for(int i = 0; i < hiddenNum; i++){
	    //operating on a single hidden node, and its edge weights
	    tempArray = hiddenWeight.get(i);
	    error = hiddenError[i];

	    // Bias is contained in input string
	    for(int p = 0; p < inputNum; p++){
		//want to store into w_pi = w_pi + mew * error_i * input_p
		tempArray[p] += mew * error * ((double) input[p]);
	    }
	}

	//update output node weights
	//operating on a single output node, and its edge weights

	// hiddenError, hiddenOutput, and outputWeight are of length hiddenNum+1
	for(int p = 0; p < hiddenNumWithBias; p++){
	    outputWeight[p] += mew * hiddenError[p] * hiddenOutput[p];
	}
    }

    public double genMove (int[] input) {
	this.propagateForward (input);
	return outputValue;
    }

    public void toFile () {
	// called without a specified output -- writes out to an myID.System.nanoTime.txt file:
	this.toFile(new File("KamiNet" + myID + "." + trainingNum + ".txt"));
    }

    public void toFile (File outFile) {
	// write the FFTree out to file
	try {
	    PrintWriter writer = new PrintWriter(outFile);
	    writer.println(inputNum + "," + hiddenNum + "," + mew + "," + myID + "," + trainingNum);

	    writer.println("%");
	
	    Double[] tempArray;

	    // write out all hidden weight arrays as 
	    // (hidden array 0) input_0, input_1, ..., input_k
	    // (hidden array 1) input_0, input_1, ..., input_k
	    // ...
	    // (hidden array j) input_0, input_1, ... ...

	    for (int i = 0; i < hiddenNum; i++) {
		tempArray = hiddenWeight.get(i);

		writer.print(tempArray[0]);
		for (int p = 1; p < inputNum; p++) {
		    writer.print("," + tempArray[p]);
		}
		writer.print("\n");
	    }
	
	    // end of hidden weights section
	    writer.println("%");
	
	    // write out the output weight array as 
	    // (single output array) hidden_0, hidden_1, ..., hidden_k
	
	    writer.print(outputWeight[0]);

	    // outputWeight is of size hiddenNum+1
	    for (int i = 1; i < hiddenNumWithBias; i++) {
		writer.print("," + outputWeight[i]);
	    }
	    writer.print("\n");
	    writer.flush();
	    writer.close();
	} catch (Exception e) {

	}
    }

    public void setMew(double mewIn) {
	mew = mewIn;
    }

    public long getID() {
	return myID;
    }

    public long getTrainingNum () {
	return trainingNum;
    }

    public static void main(String[] args) {
    }
}