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

COMMAND LINE INPUTS: use "name=val" for the value corresponding to
some command name. Do not include the quotations.

$ java KamiGo <data> <tn> <lr> <ni> <nh> <var>

Here are the values for each:

1. data (list of SFG games) = "file_name.txt"
2. tn (train net) = {"y", "n"}
3. lr (learning rate), default is 0.05
4. ni (number iterations), default is 100 (recommended more)
5. nh (number hidden nodes), default is 10
6. var (variance weights), default is 0.05

*/

// import java.util.*;
// import java.io.*;

public class KamiGo {
	public static void main(String[] args) {

		test.hi();

		// First need to check if the parameters are correct
		if (args.length != 7) {
			System.err.println("USAGE: java KamiGo <data.txt> <tn> <lr> <ni> <nh> <var>");
			System.exit(1);
		}
		// Parse the input (define a method to help us?)
		String file_name;
		boolean train_net = false;
		double learning_rate;
		int num_iterations;
		int num_hidden_nodes;
		double variance_weights; // Can use Java's nextGaussian if needed
		parse_data();
		
		// BACKPROPAGATION
		// Call Simon's code and store the info so we can use it later!!
		if (train_net) {
			// Call Simon's code with args list as input
		} else {
			// Use a default neural network, generated randomly
		}

		// PLAY THE GAME!
		play_game();
	}

	// Plays the game!
	public static void play_game() {}

	// Helps us parse the input if needed
	public static void parse_data() {}

}


/*
// Code to change a string argument to a string

int firstArg;
if (args.length > 0) {
    try {
        firstArg = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
        System.err.println("Argument" + " must be an integer");
        System.exit(1);
    }
}
*/