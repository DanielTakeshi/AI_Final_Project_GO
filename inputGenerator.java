import java.util.*;

public class inputGenerator {
	
	public inputGenerator() {
	}

	// This is the method we want to use. Takes an array of board positions
	// and a desired move, and generates an input array.
	// Currently it only handles the first point, 2 spaces above the given point.
	public static int[] getInput(String[][] board, int[] move) {
		int[] input = new int[42];
		int[] pos_input = processPoint(board, move[0], move[1]+2);
		// This doesn't look right, there must be a better way...
		for (int i=0; i < 3; ++i) {
			input[i] = pos_input[i];
		}	
		return input;
	}

	// For a given point on the board, returns [1,0,0] if its Black, 
	// [0,1,0] if its White, [0,0,1] if its Empty.
	private static int[] processPoint(String[][] board, int x, int y) {	
		int[] input = new int[3];		
		if (board[x][y].equals("B")) {
			// Also ugly. Ew.
			input[0]=1;
			input[1]=0;
			input[2]=0;
		}
		if (board[x][y].equals("W")) {
			input[0]=0;
			input[1]=1;
			input[2]=0;
		}
		if (board[x][y].equals("E")) {
			input[0]=0;
			input[1]=0;
			input[2]=1;
		}
		return input;
	}

	// Using this to test the methods, should be deleted later on.
	public static void main(String[] args) {
		String board_string = "E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,B,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E";
		String[][] board = getBoardArray(board_string);
		int[] move = {4,4};
		int[] input = getInput(board,move);
		for (int i=0; i<input.length; ++i) {
			System.out.print(input[i] + ",");
		}
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
