import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
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

    public static void main(String[] args) throws IOException {		
	String file_name = args[0];	
	Transcripter writer = new Transcripter();

	try {
	    ReadFile file = new ReadFile(file_name);
	    String contents = file.openFile();
	    String board_state = writer.readShowBoard(contents);
	    ArrayList<String> pro_moves = writer.readMoves(contents);
	    writer.writeToFile("test_output.txt", board_state);
	    writer.writeToFile("test_output.txt", pro_moves.get(0));
	}
	catch (IOException e) {
	    System.out.println( e.getMessage() );
	}	
    }
}

