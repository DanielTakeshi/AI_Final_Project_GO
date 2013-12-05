import structure5.Vector;
import java.io.*;
import java.util.*;

/*
 * A Feed Forward Tree -- designed for playing Go -- simon chase
 *
 * The FFTree is implemented as a series of flat arrays and vector of arrays containing the weights, output, and error
 * value for each node.  There is a single output node for this particular FFTree, but it could easily be extended
 * to have multiple output nodes.
 *
 *
 */

public class GoTree implements GoTreeInterface {
    // input, hidden, and output num are the node counts for each layer
    int inputNum;
    int hiddenNum;

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

    public void toFile (File outFile) {
	// write the FFTree out to file
	try {
	    PrintWriter writer = new PrintWriter(outFile);
	    writer.println(inputNum + "," + hiddenNum + "," + mew);

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

	    for (int i = 1; i < hiddenNum; i++) {
		writer.print("," + outputWeight[i]);
	    }
	    writer.print("\n");
	    writer.flush();
	    writer.close();
	} catch (Exception e) {

	}
    }

    public void buildTree (File inFile) {
	// build the tree from a file where we have saved the tree

	try {
	    Scanner scanny = new Scanner(inFile);
	    
	    // read in the first line -- has the input number, hidden num, and the learning rate in the first line
	    String[] firstLine = scanny.nextLine().split(",");

	    inputNum = Integer.parseInt(firstLine[0]);
	    hiddenNum = Integer.parseInt(firstLine[1]);
	    mew = Double.parseDouble(firstLine[2]);
	    
	    // initialize value and error structs
	    hiddenOutput = new Double[hiddenNum];
	    hiddenError = new Double[hiddenNum];
	    hiddenWeight = new Vector<Double[]>();

	    outputWeight = new Double[hiddenNum];

	    scanny.nextLine();
 
	    // read in the lines for each hidden node weight array
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
	    outputWeight = new Double[hiddenNum];
	    String[] outputWeightLine = scanny.nextLine().split(",");

	    for (int i = 0; i < hiddenNum; i++) {
		outputWeight[i] = Double.parseDouble(outputWeightLine[i]);
	    }
	    // done
	} catch (IOException e) {
	    
	}
    }

    public void buildTree (int inputIn, int hiddenIn, double mewIn) {

	//store input values
	inputNum = inputIn;
	hiddenNum = hiddenIn;

	mew = mewIn;

	//initialize weight vectors for each hidden node
	//at the same time, initialize hidden ouput arrays
	hiddenWeight = new Vector<Double[]>(hiddenNum);
	hiddenOutput = new Double[hiddenNum];
	hiddenError = new Double[hiddenNum];

	Random rando = new Random();

	outputWeight = new Double[hiddenNum];

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
    }

    private void propagateForward (int[] input) {
	
	//for each hidden node:
	for (int i = 0; i < hiddenNum; i++) {

	    //operating on a single hidden node
	    Double[] tempWeight = hiddenWeight.get(i);

	    //sum over inputs x_i * w_i for the input layer
	    double net = 0.0;

	    for (int p = 0; p < input.length; p++) {
		net += ((double) input[p]) * tempWeight[p];
	    }

	    //perform sigmoid function
	    net = 1 / (1 + (Math.exp(-net)));

	    //store net into hidden node's output array
	    hiddenOutput[i] = net;
	}	

	//propagate hidden output to single output node
	//sum over inputs x_i*w_i
	double net = 0.0;

	for(int p = 0; p < hiddenNum; p++){
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

	for(int i = 0; i < hiddenNum; i++){
	    hiddenError[i] = hiddenOutput[i] * (1 - hiddenOutput[i]) * outputWeight[i] * outputError;
	}
    }

    private void update (int[] input) {
	//call to update edge weights for every hidden node and every output node
	double error;
	Double[] tempArray;

	//update hidden node weights
	for(int i = 0; i < hiddenNum; i++){
	    //operating on a single hidden node, and its edge weights
	    tempArray = hiddenWeight.get(i);
	    error = hiddenError[i];

	    for(int p = 0; p < inputNum; p++){
		//want to store into w_pi = w_pi + mew * error_i * input_p
		tempArray[p] += mew * error * ((double) input[p]);
	    }
	}

	//update output node weights
	//operating on a single output node, and its edge weights

	for(int p = 0; p < hiddenNum; p++){
	    outputWeight[p] += mew * hiddenError[p] * hiddenOutput[p];
	}
    }

    public double genMove (int[] input) {
	this.propagateForward (input);
	return outputValue;
    }

    public static void main(String[] args) {
	GoTree myGo = new GoTree();
	myGo.buildTree(new File("hi.txt"));
	System.out.println("hi");
	
	myGo.toFile(new File("hi2.txt"));
    }
}