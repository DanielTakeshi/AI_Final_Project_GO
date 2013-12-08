/**

   @author Daniel Seita (and Simon Chase, Philippe Demontigny)

   This is the file that runs everything, ASSUMING we have a single text
   file with all data from Go games that we need.

   There are two main things to do:

   1. Generate the neural network by training on real Go games.
   2. Play a game with the neural network

   (Later, we should split the functionality so that we can play games later.)

   Within each of those two steps, we have to do the following (might not be in complete order):

   1a. Initialize the neural net with default weights, the right number of nodes, etc.
   1b. Call our data so that we get the records of all the professional Go games needed
   1c. For each game, we proceed move-by-move to update the neural network
   1d. As we're doing so, we can store transcripts of the game so that we can retrain on same games.
   1e. Return the neural net, so we're going to need to save the information.

   2a. Now we need to coordinate with Fuego again. Set up a game somehow.
   2b. Whenever the computer needs to play, call "genmove"
   2c. Genmove should run our neural network and pick the empty spot on the board with highest score
   2d. Repeat until game over.

   There are a lot of command line inputs, due to the different ways to run an ANN.

   COMMAND LINE INPUTS:

   $ java KamiGo -d <data> <iterations> -t <backprop_input_file> -

   1. <data> is a text file that has the list of games in a way our ANN can read it
   3. <iterations> is how many times we run backpropagation (stopping criteria). OPTIONAL parameter

   Num of hidden nodes is set to a constant, and default weights are ~Unif[-0.5, 0.5].

*/

// TODO: FIX THE COMMENTS ABOVE

import java.io.*;
import java.util.*;


public class KamiGo {

    public static void main(String[] args) {

	// STEP 0: Checks if args.length = 1 or 3; if train=y, we NEED three arguments.
	if ((args.length != 1 && args.length != 3) || (args[0].equals("y") && args.length != 3)) {
	    System.err.println("USAGE: java KamiGo <train> <data> <iterations> (last 2 needed if train = \"y\")");
	    System.exit(1);
	}

	String transcript_file = "";
	int num_iterations = 0;
	boolean do_we_train = args[0].equals("y");

	if (do_we_train) {
	    try {
		transcript_file = args[1];
		num_iterations = Integer.parseInt(args[2]);
	    } catch (NumberFormatException e) {
		System.err.println("Argument " + args[2] +  " must be an integer");
		System.exit(1);
	    }
	}
		
	// STEP 1: BACKPROPAGATION
	try {
	    if (do_we_train) {
		Trainer miyagi = new Trainer(transcript_file);
		miyagi.runTraining(num_iterations);
	    } else {
		System.out.println("Find our old neural network (assuming we have it...)");
	    }
	} catch (Exception e) {
	    System.out.println(e);
	}

	/*
	// STEP 2: PLAY THE GAME!
	try {
	    System.out.println("Now playing the game ...\n");
	    playGame();
	}
	catch (IOException e) {
	    System.out.println(e);
	}
	*/
    }

    /*
     * This allows the human player to write in commands to actually
     * play a game.
     *
     */
    public static void playGame() throws IOException {
	BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	String user_input = reader.readLine();
	String[] user_input_elements = user_input.split(" ");

	//ship it off to FuegTalker
	String fuego_output = "HI DAN";

	if ( user_input.equals("showboard") ) {
	    System.out.println(fuego_output);
	}
	else if ( user_input_elements[0].equals("play") ) {
	    // If fuego returns "=" all good
	    // If fuego returns "?" bad string
	    String test_symbol = fuego_output.split(" ")[0];
	    if ( test_symbol.equals("=") ) {
		// run computer turn
	    }
	    else {
		System.out.println( fuego_output  );
	    }
	}
	else {
	    //Check later, be consistent with fuego error messages
	    System.out.println("? Unrecognized Input");
	}
    }
}
