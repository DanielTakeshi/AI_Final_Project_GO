import java.io.*;
import java.util.*;

/*
 * A Program that trains KamiGo
 * 
 * (c) 2013 by Philippe Demontigny, Daniel Seita, and Simon Chase
 *
 * Called once (by KamiGo) to create the Neural Net through training
 * Much of the functionality is already in GoTree.java We just need to
 * call its "train" method judiciously and pass in the corresponding
 * inputs. Note: this will not be called if the user has specified
 * that we play Go with an ANN that's already been constructed.
 */

public class Trainer {

    /*
     * + transcript holds all transcripts for all games in transcript.txt
     * Transcript for one game is a board state followed by the move. Ex:
     *
     * "E, E, B, E, W, ..." (81 letters)
     * "BXY" (Black plays move XY, where 0 <= X, Y <= 8)
     *
     * These two lines repeat until the game is finished.
     *
     * + genny is the input generator, i.e., gives us the feature vector
     * + KamiNet represents the GoTree, i.e., the ANN.
     */
    private File transcript;
    private InputGenerator genny;
    private GoTree KamiNet;

    private final int inputNum = 43;
    private final int hiddenNum = 10;
    private final double mew = .1;

    private String[] transcript_list;
    private ArrayList<String[][]> boards;
    private ArrayList<int[]> pro_inputs;
    private int num_moves;
    
    // Constructor; makes a new GoTree and gets training games.
    public Trainer(String transcript_file) {
	KamiNet = new GoTree();
	KamiNet.buildTree(inputNum, hiddenNum, mew);
	transcript = new File(transcript_file);
	genny = new InputGenerator();
	ReadFile reader = new ReadFile();

	boards = new ArrayList<String[][]>();
	// Used to store state for a new game
	String[][] empty_board;
	pro_inputs = new ArrayList<int[]>();

	System.out.println("Loading Transcript...");

	try {
	    transcript_list = reader.openFile(transcript).split("\n");
	} catch (IOException e) {
	    System.out.println(e);
	}

	System.out.println("Prepping Game State...");

	num_moves = transcript_list.length / 2;
	int game_counter = 0;
	int move_counter = 0;

	for (int i=0; i<num_moves; i++) {

	    String board_string = transcript_list[2*i];
	    String move = transcript_list[2*i+1];
	    String current_color = move.substring(0,1);
	    String pro_move = move.substring(1,3);
	    move_counter++;

	    // We want to treat each board position with respect to one color
	    if ( current_color.equals("W") ) {
		board_string = swapColors(board_string);
	    }

	    // Makes it easier to iterate through board positions (since it's in a 9x9 array)
	    String[][] board = getBoardArray(board_string);
	    boards.add(board);
	    if ( isEmpty(board) ) {
		game_counter++;
		move_counter = 0;
	    }

	    // TODO: Make the game move counter
	    int[] pro_move_int = {Integer.parseInt(pro_move.substring(0,1)), 
				  Integer.parseInt(pro_move.substring(1,2))};

	    int[] input = genny.getInput(board, pro_move_int, move_counter);
	    pro_inputs.add(input);
	}
	System.out.println("Done. " + game_counter + " games found");
	System.out.println("Beginning Training ...");
    }

    private boolean isEmpty(String[][] board) {
	for (int x = 0; x < 9; x++) {
	    for (int y = 0; y < 9; y++) {
		if ( !board[x][y].equals("E") ) {
		    return false;
		}
	    }
	}
	return true;
    }
    


    // We have a transcript from the constructor; 
	// we train once with each game in order,
    // and then repeat the process until we've done it num_repeat times.
    public void runTraining(int num_repeat) throws Exception {
	int counter = 0;
	int percent = 0;
	int five_percent = (int)(num_repeat / 20);
	for (int rep=0; rep<num_repeat; rep++) {
	    if (counter >= five_percent) {
		percent += 5;
		System.out.println(percent + "% Complete.");
		counter = 0;
	    }
	    if ( rep % 5000 == 0 ) {
		KamiNet.toFile(new File("KamiNetv" + (int)(rep/5000) + ".txt"));
	    }
	    runGame();
	    counter++;
	}
	KamiNet.toFile(new File("KamiNetFinal.txt"));
    }


    /*
     * Called by runTraining. Given a transcript, feed it to the
     * neural net so it can train, i.e. by calling KamiNet.train with
     * the input, which is the feature vector.
     *
     * To train, we use two moves. One will be the professional's
     * move; the other will be a move chosen at random from the
     * possible empty spots that does not match the pro's move.
     *
     */
    private void runGame() throws Exception {
	int num_moves = transcript_list.length / 2;

	for (int i=0; i<num_moves; i++) {

	    String[][] board = boards.get(i);
	    String move = transcript_list[2*i+1];
	    String current_color = move.substring(0,1);
	    String pro_move = move.substring(1,3);

	    // Now pick the random move (can't be pro move)
	    String random_move = chooseRandomMove(board, pro_move);
	    int[] random_move_int = {Integer.parseInt(random_move.substring(0,1)), 
				     Integer.parseInt(random_move.substring(1,2))};

	    // Train with pro move then random move
	    int[] input = pro_inputs.get(i);
	    KamiNet.train(input, 1);
	    input = genny.getInput(board, random_move_int, i);
	    KamiNet.train(input, 0);
	}
    }



    // Returns a random move so we can train on it, as long as it's empty and not pro move
	// TODO: Double check this, it should be fine, but ya never know...
    public String chooseRandomMove(String[][] board, String pro_move) {
	String move_string;
	int x, y;
	Random rand = new Random();
	do {
	    x = rand.nextInt(9);
	    y = rand.nextInt(9);
	    move_string = String.valueOf(x) + String.valueOf(y);
	} while ( !board[x][y].equals("E") || move_string.equals(pro_move));
	return move_string;
    }


    // Swaps the black and white spots, using "Towers-of-Hanoi" technique
    // E.g., before: "B W E E W ..."; after: "W B E E B ..."
    private String swapColors(String board) {
	board = board.replace('B','X');
	board = board.replace('W','B');
	board = board.replace('X','W');
	return board;
    }


    // Converts the string of board states from the Transcript file to a 9x9 array.
    // Could be useful in the training program as opposed to using it here...
    private String[][] getBoardArray(String board_string) {
	String[][] board = new String[9][9];
	String[] positions = board_string.split(",");
	int i = 0;
	for (int x=0; x<9; ++x) {
	    for (int y=0; y<9; ++y) {
		board[x][y] = positions[i];
		i += 1;
	    }
	}
	return board;
    }

}
