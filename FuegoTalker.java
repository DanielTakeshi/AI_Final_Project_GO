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
	for (String move : movesList) {
	    // for each move, we map to the move we output for fuego
	    System.out.println("boardsize 9");
	    System.out.println(myTranscriptor.genFuegoMove(move));
	    System.out.println("showboard");
	    pull(move);
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
    
    /*
     *
     */

    public static void pull(String move) {
	try {
	    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
	    String resultString = "";
	    while (true) {

		if (readIn.ready()) {
		    // fuego has written something new out -- record string
		    Scanner scanny = new Scanner(readIn);
		    resultString = "";
		    while (scanny.hasNextLine()) {
			resultString += scanny.nextLine() + "\n";
		    }
		    break;
		} else {
		    Thread.sleep(100);
		}
	    } 
	
	
	    System.out.println("******************************** printing recorded game trace **********************\n" + resultString + "********************************* done *************************");
	} catch (Exception e) {
	    System.out.println(e);
	}
    }
    
    public static void main(String[] args) throws IOException {		
	// read in a directory
	File trainDir = new File(args[0]);
	push(trainDir);
    }
}

