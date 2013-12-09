/**
 *  @authors Daniel Seita, Simon Chase, Philippe Demontigny
 *
 * Comments here will be brief --- see the README for more details.
 *
 * This is the file that runs everything, ASSUMING we have a single text file with all
 * data from Go games that we need. There are two main things it can do: generate the
 * neural network and play a game.
 *
 * COMMAND LINE INPUTS:
 *
 * $ java KamiGo (-d <data> <iterations> | -t <backprop_input_file>)
 *
 * <data> is a text file that has the list of games in a way our ANN can read it
 * <iterations> is how many times we run backpropagation (stopping criteria). OPTIONAL parameter
 *
 * Num of hidden nodes is set to a constant (10 here, not including
 * the bias node), and default weights are ~ Unif[-0.5, 0.5].
 *
 */

import java.io.*;
import java.util.*;


public class KamiGo {

    // Information we'll use later to call other files, convert coordinates, etc.
    private HashMap<String,Integer> letterTranslate;
    private int[] numTranslate;
    private String[] letters;
    private InputGenerator genny;
    private Transcripter tranny;
	private GoTree KamiTree;
    private int move_count = 0;


    /*
     * This gets called first by the main, and initializes the hash map and other info
     * that we'll need to have pleasant communication between us and Fuego, e.g., because
     * Fuego can use different coordinate systems for indicating moves.
     */
    private void defineStuff() {
		letterTranslate = new HashMap<String,Integer>();				 
		numTranslate = new int[]{0,8,7,6,5,4,3,2,1,0};
		letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J"};
		for (int i = 0; i < 9; i++) {
			letterTranslate.put( letters[i], i );
		}
		genny = new InputGenerator();
		tranny = new Transcripter();
		KamiTree = new GoTree();
    }


    public static void main(String[] args) {
	
		KamiGo kg = new KamiGo();

		// Initializes some stuff we'll need later
		kg.defineStuff();

		// We are training either from a new neural net from a file, or playing a game using an existing neural net
		if ((args.length >= 2 && args.length <= 4) || ((!args[0].equals("-t")) && (!args[0].equals("-d")))) {
			System.err.println("USAGE: java KamiGo (-d <data> <iterations> [<backprop_input_file>] | -t <backprop_input_file");;
			System.exit(1);
		}

		// Transcript file has the transcript, backprop file has file of desired ANN
		String transcript_file = "";
		String backprop_file = "";

		int num_iterations = 0;
		boolean do_we_train = args[0].equals("-d");
		boolean train_from_file = false;

		// Parse the input
		if (do_we_train) {
			try {
				transcript_file = args[1];
				num_iterations = Integer.parseInt(args[2]);
				if (args.length == 4) {
					backprop_file = args[3];
					train_from_file = true;
				}

			} catch (NumberFormatException e) {
				System.err.println("Argument " + args[2] +  " must be an integer");
				System.exit(1);
			}
		} else {
			// not training -- initialize backprop file to second arg
			backprop_file = args[1];
		}
		
		// If we are training, we need to perform backpropagation, so create a Trainer, etc.
		try {
			if (do_we_train) {
				Trainer miyagi;
				if (train_from_file) {
					miyagi = new Trainer (transcript_file, backprop_file);
				} else {
					miyagi = new Trainer (transcript_file);
				}

				miyagi.runTraining(num_iterations);
			} else {
				// Play the game!
				System.out.println("Now playing one move...\n");
				kg.runComputerTurn();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
    }

    /*
     * This runs the computer player's turn. We iterate through all possible board states,
     * check the ones that are legal, and have the computer player choose the one with the
     * highest score based on propagating it through the current ANN.
     */
    private void runComputerTurn() {
		String[] move_letters = {"A", "B", "C", "D", "E", "F", "G", "H", "J"};
		String[] move_numbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};		
		String best_move = "";
		double best_value = 0.0;

		// Get the board, so as to allow us to get the input vector
		// TODO: Fix this step ... we need the transcript of the board so we can get the feature vector
		//String transcript_board = tranny.processShowBoard(fuego_board);

		for ( String letter : move_letters ) {
			for ( String number : move_numbers ) {

				String fuego_move = letter + number;

				// TODO: Determine if it is a legal move by reading a file of legal moves

				if ( fuego_output.equals("= 1") ) {
					int[] move = getMoveCoordinates(fuego_move);

					// String fuego_board = tell fuego "showboard"
					String fuego_board = "";

					try {

						int[] input = genny.getInput(transcript_board, move, move_count);
						double value = KamiTree.genMove(input);
						if ( value > best_value) {
							best_move = fuego_move;
							best_value = value;
						}
					} catch (IOException e) {
						System.out.println(e);
					}
				}
			}
		}
		if ( !best_move.equals("") ) {
			// Tell fuego play best_move
			move_count++;
		}
		else {
			System.out.println("How on earth did this happen??");
		}	
    }


    /*
     * Given a move from Fuego in the form of "YZ", where Y, Z in {1, ... , 9 }, and
     * returns it to us in the form of "AB", where A, B in {0, ... , 8}.
     */
    private int[] getMoveCoordinates( String fuego_move ) {
		int[] move = new int[2];
		move[0] = letterTranslate.get( fuego_move.substring(0,1) );
		move[1] = numTranslate[ Integer.parseInt(fuego_move.substring(1,2)) ];
		return move;
    }

}