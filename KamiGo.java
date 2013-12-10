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
import structure5.Vector;

public class KamiGo {

    // Information we'll use later to call other files, convert coordinates, etc.
    private HashMap<String,Integer> letterTranslate;
    private int[] numTranslate;
    private String[] letters;
    private InputGenerator genny;
    private Transcripter tranny;
	private GoTree KamiTree;
    private int move_count = 0;

	private String[][] Kami_Board;


    /*
     * This gets called first by the main, and initializes the hash map and other info
     * that we'll need to have pleasant communication between us and Fuego, e.g., because
     * Fuego can use different coordinate systems for indicating moves.
     */
    private void defineStuff(File Kami_Net) {
		letterTranslate = new HashMap<String,Integer>();				 
		numTranslate = new int[]{0,8,7,6,5,4,3,2,1,0};
		letters = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "J"};
		for (int i = 0; i < 9; i++) {
			letterTranslate.put( letters[i], i );
		}
		genny = new InputGenerator();
		tranny = new Transcripter();
		KamiTree = new GoTree();
		KamiTree.buildTree(Kami_Net);

		Kami_Board = new String[9][9];
		for (int x=0; x<9; x++) {
			for (int y=0; y<9; y++) {
				Kami_Board[x][y] = "E";
			}
		}
    }

	// Returns whether the move was "valid"
	// Move must be of the form "xy" where x and y are numbers
	// Color will be "B" or "W"
	private String playMove(String move, String color) {
		int x = Integer.parseInt(move.substring(0,1));
		int y = Integer.parseInt(move.substring(1,2));
		if ( Kami_Board[y][x].equals("E") ) {
		    Kami_Board[y][x] = color;
			return "= \n";
		}
		else {
			return "? Illegal Move \n";
		}
	}

	//Removes a stone from the board if there is one there
	private String undoMove(String move) {
		int x = Integer.parseInt( move.substring(0,1) );
		int y = Integer.parseInt( move.substring(1,2) );
		if ( !Kami_Board[y][x].equals("E") ) {
			Kami_Board[y][x] = "E";
			return "= \n";
		}
		else {
			return "? move " + move + " is empty \n";
		}
	}
	
	private void showboard() {
		System.out.print("  0 1 2 3 4 5 6 7 8 \n");
		for (int x=0; x<9; x++) {
			System.out.print(x + " ");
			for (int y=0; y<9; y++) {
				System.out.print( Kami_Board[x][y] + " " );
			}
			System.out.print("\n");
		}
		System.out.print("=\n");
	}

    public static void main(String[] args) {
	
		KamiGo kg = new KamiGo();


		// We are training either from a new neural net from a file, or playing a game using an existing neural net
		if ( !(args.length >= 2 && args.length <= 4) || ((!args[0].equals("-t")) && (!args[0].equals("-d")))) {
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

		// Initializes some stuff we'll need later
		kg.defineStuff(new File(backprop_file));
		
		
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
				try {
					kg.playGame(kg);
				} catch (IOException e) {
					// stuff
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	private void playGame(KamiGo kg) throws IOException {
		int move_counter = 0;
		Scanner scanny = new Scanner(System.in);
		while (true) {
			String[] user_input = scanny.nextLine().split(" ");
			if ( user_input[0].equals("play") ) {
				String color = user_input[1];
				String move = user_input[2];
				System.out.print( kg.playMove(move, color) );
				move_counter++;
				kg.showboard();
			}
			else if (user_input[0].equals("undo")) {
				String move = user_input[1];
				System.out.print( kg.undoMove(move) );
				kg.showboard();
			}
			else if ( user_input[0].equals("quit") || user_input[0].equals("q") )  {
				break;
			}
			else if ( user_input[0].equals("showboard") ) {
				kg.showboard();
			}
			else if ( user_input[0].equals("genmove") ) {
				String color = user_input[1];
				System.out.print(kg.genmove(color, move_counter));
				kg.showboard();
			} 
		}
	}

	private String readFuego(int num_lines) throws IOException {

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String result = "";
		int counter = 0;
		while (true) {
			if ( reader.ready() ) {
				Scanner scanny = new Scanner(reader);
				while (scanny.hasNextLine() && num_lines > 0) {
					result += scanny.nextLine() + "\n";
					num_lines--;
				}
			}
			if (num_lines <= 0 || counter > 50 ) {
				break;
			}
			else {
				try {
					Thread.sleep(100);
					counter++;
				}
				catch (InterruptedException e) {
					// error
				}
			}
		}
		return result;
	}

	private String readAllFuego(int num_lines) throws IOException {

		Vector<String> result_vector = new Vector<String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String result = "";
		int counter = 0;
		while (true) {
			if ( reader.ready() ) {
				Scanner scanny = new Scanner(reader);
				while (scanny.hasNextLine() ) {
					result_vector.add(scanny.nextLine() + "\n");
				}
			}
			if (counter > 50 ) {
				break;
			}
			else {
				try {
					Thread.sleep(100);
					counter++;
				}
				catch (InterruptedException e) {
					// error
				}
			}
		}
		// offset from the bottom where we expect our string to be
		int offset = 2;
		int vector_size = result_vector.size();
		assert vector_size >= num_lines + offset;
			
		for (int i = 0; i < num_lines; i++) {
			result += result_vector.get(vector_size - 1 - offset - i);
		}
		return result;
	}


    /*
     * This runs the computer player's turn. We iterate through all possible board states,
     * check the ones that are legal, and have the computer player choose the one with the
     * highest score based on propagating it through the current ANN.
     */
    private String genmove(String color, int move_count) throws IOException {
		int[] best_move = {0,0};
		double best_value = 0.0;

		// Get the board, so as to allow us to get the input vector
		// TODO: Fix this step ... we need the transcript of the board so we can get the feature vector
		//String transcript_board = tranny.processShowBoard(fuego_board);
		String transcript_board = "";

		for ( int x=0; x<9; x++ ) {
			for ( int y=0; y<9; y++ ) {
				int[] move = {x,y};
				if ( Kami_Board[y][x].equals("E") ) {
					int[] input = genny.getInput(Kami_Board, move, move_count);
					double value = KamiTree.genMove(input);
					if ( value > best_value) {
						best_move = move;
						best_value = value;
					}
				}
			}
		}
		if ( !best_move.equals("") ) {
			int boardx = best_move[0];
			int boardy = best_move[1];
			Kami_Board[boardy][boardx] = color;
			return "= KamiGo plays " + boardx + boardy + "\n";
		}
		else {
			return "? No move to be made!\n";
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