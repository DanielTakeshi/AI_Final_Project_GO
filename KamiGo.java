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

   $ java KamiGo -d <data> <iterations> -i <backprop_input_file>  

   1. <data> is a text file that has the list of games in a way our ANN can read it
   2. <train> should be either "y" or "n" -- if no, use old ANN, if yes then train one and use that.
   3. <iterations> is how many times we run backpropagation (stopping criteria). OPTIONAL parameter

   Num of hidden nodes is set to a constant, and default weights are ~Unif[-0.5, 0.5].

*/


public class KamiGo {

    public static void main(String[] args) {

	// STEP 0: Checks if args.length = 2 or 3; if train=y, we NEED third argument.

	if ((args.length != 2 && args.length != 3) || (args[1].equals("y") && args.length == 2)) {
	    System.err.println("USAGE: java KamiGo <data> <train> <iterations> (need \"iterations\" if \"train\" = \"y\")");
	    System.exit(1);
	}

	int num_iterations;
	boolean do_we_train = args[1].equals("y");

	if (do_we_train) {
	    try {
		num_iterations = Integer.parseInt(args[2]);
	    } catch (NumberFormatException e) {
		System.err.println("Argument " + args[2] +  " must be an integer");
		System.exit(1);
	    }
	}
		
	// STEP 1: BACKPROPAGATION
	// Call Simon's code and store the info so we can use it later!!
	if (do_we_train) {
	    System.out.println("Call backpropagation code.");
	} else {
	    System.out.println("Find our old neural network (assuming we have it...)");
	}

	// STEP 2: PLAY THE GAME!
	play_game();
    }

    // Plays the game!
    public static void play_game() {
	System.out.println("We are playing the game!");
    }
}
