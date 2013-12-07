import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 * A class that sends messages to Fuego and receives the standard
 * fuego command-line output as a String.
 * 
 * Currently I was using this method to test the Transcriptor.java,
 * which processes the Strings received from fuego. This class should
 * be changed to handle communication exclusively.
 **/

public class FuegoTalker {

    public FuegoTalker() {

    }
    
    /*
     * push takes in a file representing the directory of all the sgf files we want to transform to our format
     *
     * it pushes out output to fuego to make all of the moves for the game represented by each file
     */

    public static void push(File sgfPath) {
	// get the array of all the files we want to transform in the directory
	File[] sgfFileArray = sgfPath.listFiles();
	Transcripter myTranscriptor = new Transcripter();
	
	for (int i = 0; i < sgfFileArray.length; i++) {
	    // test to make sure that the file is actually there
	    File curFile = sgfFileArray[i];

	    if (curFile.isFile() && curFile.canRead()) {
		// initialize fuego to a 9x9 board
		System.out.println("boardsize 9");
		ArrayList<String> movesList;

		try {
		    movesList = myTranscriptor.readMoves(curFile);
		    for (String move : movesList) {
			// for each move, we map to the move we output for fuego
			System.out.println(myTranscriptor.genFuegoMove(move));
		    } 		    
		} catch (IOException e) {
		    System.out.println(e);
		}
	    }
	}
    }
    
    public static void pull() {
	
    }
    
    public static void main(String[] args) throws IOException {		
	// read in a directory
	File trainDir = new File(args[0]);
	push(trainDir);
    }
}

