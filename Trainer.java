import java.io.*;

/*
 * A Program that trains KamiGo
 * 
 * (c) 2013 by Philippe Demontigny, Daniel Seita, and Simon Chase
 *
 * Called only to create the Neural Net through training
 * Much of the functionality is already in GoTree.java
 * We just need to call its "train" method judiciously and
 * pass in the corresponding inputs.
 */

public class Trainer {

	// Each string holds the transcript for a single game
	private String[] training_games;
	private InputGenerator genny;
	private GoTree KamiNet;

	public Trainer(String tree_file, String file_names) {
		KamiNet = new GoTree();
		KamiNet.buildTree(new File(tree_file)); // is this right?
		training_games = file_names.split(" ");
		genny = new InputGenerator();
	}

	// We have a list of games; we train once with each game in order,
	// and then repeat the process until we've done it num_repeat times.
	public void runTraining(int num_repeat) throws Exception {
		for (int rep=0; rep<num_repeat; ++rep) {
			for (int i=0; i < training_games.length; ++i) {
				runGame(training_games[i]);
			}
		}
	}

	// do we need the throws exception parts?

	// For a given game, we feed it to the neural net so that it can train
	// We need to do some bookkeeping and formatting of the input
	private void runGame(String transcript) throws Exception {
		String[] transcript_list = transcript.split("\n"); 
		int num_moves = transcript_list.length / 2;

		for (int i=0; i < num_moves; ++i) {
			String board_string = transcript_list[2*i];
			String pro_move = transcript_list[2*i+1];
			// We want to treat each board position with respect to one color
			if ( pro_move.substring(0,1).equals("W") ) {
				board_string = swapColors(board_string);
			}
			String[][] board = getBoardArray(board_string);
			for (int y=0; y < 9; ++y) {
				for (int x=0; x < 9; ++x) {	
					int[] move = {x,y};
					// We only train if we are considering an EMPTY board spot
					if ( board[x][y].equals("E") ) {
						int[] input = genny.getInput(board,move,i);
						String test_move = String.valueOf(x) + String.valueOf(y);
						if ( pro_move.substring(1,3).equals(test_move) ) {
							KamiNet.train(input, 1 );
						}
						else {
							KamiNet.train(input, 0 );
						}						
					}
				}
			}
		} 
	}

	// Swaps the black and white spots, using "Towers-of-Hanoi" technique
	private String swapColors(String board) {
		board = board.replace('B','X');
		board = board.replace('W','B');
		board = board.replace('X','W');
		return board;
	}

	// Converts the string of board states from the Transcript file to a 9x9 array.
    // Could be useful in the training program as opposed to using it here...
    private static String[][] getBoardArray(String board_string) {
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
