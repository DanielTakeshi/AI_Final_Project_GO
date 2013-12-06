/*
  This code takes in a string representation of a board, e.g.,:

  string = "E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,B,E,E,E,E,E,E
  E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E
  E,E,E,E,E,E,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E"

  And will return the array of 42 elements that contains the features
  we'll use for that position.

*/

import java.util.*;

public class InputGenerator {

    public static final int[][] locations = { {0,-2}, {-1,-1}, {0,-1}, {1,-1}, {-2,0}, {-1,0}, 
					      {1,0}, {2,0}, {-1,1}, {0,1}, {1,1}, {0,2},};

    public InputGenerator() {
    }

    // This is the method we want to use. Takes an array of board
    // positions and a desired move, and generates an input array.
    // Currently it only handles the first point, 2 spaces above the
    // given point.
    public static int[] getInput(String[][] board, int[] move, int move_count) {
		int[][] position_array = getPositionArray(move);
		int[] input = processPoint(board, position_array);
		// Early Game		
		if (move_count <= 10) { input[36] = 1; }
		// Mid Game
		else if (move_count <= 40) { input[37] = 1; }
		// Late Game
		else { input[38] = 1; }
		// On Corner
		if ( move[0] % 6 <= 3 && move[1] % 6 <=3) { input[39] = 1; }
		// On Edge
		else if ( move[0] % 6 <= 3 || move[1] % 6 <=3) { input[40] = 1; }
		// In Center
		else { input[41] = 1; }
		return input;
    }

    // Input needs to be {x, y}
    public static int[][] getPositionArray(int[] center_point) {
	int x = center_point[0];
	int y = center_point[1];
	int[][] positions = new int[12][2];
	for (int i=0; i<locations.length; ++i) {
	    positions[i][0] = locations[i][0] + x;
	    positions[i][1] = locations[i][1] + y;
	}
	return positions;
    } 

    // For a given point on the board, returns [1,0,0] if its Black, 
    // [0,1,0] if its White, [0,0,1] if its Empty.
    private static int[] processPoint(String[][] board, int[][] positions) {	
	int[] input = new int[42];
	int x;
	int y;
	System.out.println(positions.length);
	for (int j=0; j<positions.length; ++j) {
	    x = positions[j][0];
	    y = positions[j][1];
	    if ( x < 0 || x > 8 || y < 0 || y > 8 ) {
		//do nothing!
	    }
	    else if (board[x][y].equals("B")) {
		// much better!
		input[3*j]=1;
	    }
	    else if (board[x][y].equals("W")) {
		input[3*j+1]=1;
	    }
	    else if (board[x][y].equals("E")) {
		input[3*j+2]=1;
	    }
	}
	return input;
    }

    // Using this to test the methods, should be deleted later on.
    public static void main(String[] args) {
	String board_string = "E,B,B,E,E,E,E,E,E,B,B,E,E,E,E,E,E,E,B,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E";
	String[][] board = getBoardArray(board_string);
	int[] move = {7,4};
	int[] input = getInput(board,move,53);
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
