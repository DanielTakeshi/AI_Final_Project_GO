import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.*;

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
    
    public static void push (ArrayList<String> movesList, Transcripter myTranscriptor) {
		System.out.println("boardsize 9");
		for (String move : movesList) {
			// for each move, we map to the move we output for fuego
			System.out.println("showboard");
			System.out.println(myTranscriptor.genFuegoMove(move));
		} 		    
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


				try {
					ArrayList<String> movesList = myTranscriptor.readMoves(curFile);
					push(movesList, myTranscriptor);
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}
    }

	public static void makeTranscript(File sgfPath, File boardmoves ) {
		Transcripter tranny = new Transcripter();
	    File[] sgfFileArray = sgfPath.listFiles();
		ArrayList<String> movesList = new ArrayList<String>();
		String current_board;
		String transcript_file = "output/transcript.txt";

		try {
		for (int i = 0; i < sgfFileArray.length; i++) {
		    // test to make sure that the file is actually there
	  	 	File curFile = sgfFileArray[i];
            if (curFile.isFile() && curFile.canRead()) {
			    // initialize fuego to a 9x9 board
				movesList.addAll(tranny.readMoves(curFile));		
	        }
		}
		System.out.println("Number of moves is: " + movesList.size() );
		convertBoardState( boardmoves, transcript_file, movesList );
		} catch (IOException e) {
		    e.printStackTrace(System.out);
		}
	}

	private static void convertBoardState( File boardmoves, String target, ArrayList<String> movesList ) throws IOException {
		Scanner scanny = new Scanner(boardmoves);
		Transcripter tranny = new Transcripter();
		String current_board;
		String current_line; 
		boolean found_board = false;
		int index = 0;

		while (scanny.hasNextLine()) {
			current_board = "";
			for (int i=1; i<=9; i++) {
				assert scanny.hasNextLine();
				current_line = scanny.nextLine();
				current_board += current_line + "\n"; 
			}
			String tranny_board = tranny.processShowBoard(current_board);
			String transcript_turn = tranny_board + "\n" + movesList.get(index++);
			tranny.writeToFile( target, transcript_turn );
		}
	}
    
    /*
     *
     */

    public static void pull(File sgfPath, File trace) {
	
	// get the array of all the files we want to transform in the directory
	File[] sgfFileArray = sgfPath.listFiles();
	Transcripter myTranscriptor = new Transcripter();
	
	for (int i = 0; i < sgfFileArray.length; i++) {
	    // test to make sure that the file is actually there
	    File curFile = sgfFileArray[i];

	    if (curFile.isFile() && curFile.canRead()) {
		// initialize fuego to a 9x9 board
		try {
		    ArrayList<String> movesList = myTranscriptor.readMoves(curFile);
		    push(movesList, myTranscriptor);
		} catch (IOException e) {
		    System.out.println(e);
		}
	    }
	}
    }
    
public static void main(String[] args) throws IOException {		
    // read in a directory
    File trainDir = new File(args[0]);
	File board_stuff = new File(args[1]);
    makeTranscript(trainDir, board_stuff);
}
}

