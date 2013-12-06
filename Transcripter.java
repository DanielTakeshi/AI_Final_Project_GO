import java.io.*;
import java.util.*;

/**
 * A method that processes fuego output and .sgf files.

 * readGoBoard: Processes the output of the GTP command "go_board"
 * currently we do not use this output, but it may be useful later
 *
 * readShowBoard: Processes the output of the GTP command "showboard"
 * 
 * readMoves: Processes a String taken from a .sgf file. Returns an
 * ArrayList<String> containing all moves in a single game. of the 
 * form "B23" where the first character is B/W, and the next two 
 * denote the board position. The first number is the column from 
 * left to right, and the second number is the row from top to bottom.
 * 
 * writeToFile: Writes a given output to a specified final. Nothing
 * fancy here.
 **/

public class Transcripter {

    // Map from letter notation of .sgf file to coordinate number (x,y)
    private HashMap<String,String> translate = new HashMap<String,String>();

    public Transcripter() {
	translate.put("a","0");
	translate.put("b","1");
	translate.put("c","2");
	translate.put("d","3");
	translate.put("e","4");
	translate.put("f","5");
	translate.put("g","6");
	translate.put("h","7");
	translate.put("i","8");
    }

    // Parses fuego output for GTP Command "go_board"
    // Don't need this for training, can do this later!
    public String readGoBoard(String output) throws IOException {

	Scanner reader = new Scanner(output);

	String data = "";
	String[] contents;
	String next_line = reader.readLine();

	while (reader.hasNextLine()) {
	    next_line = reader.readLine();

	    // fuego splits based on spaces
	    contents = next_line.split(" ");

	    // If we see the title "CountPlay", then record the number
	    if ( contents[0].equals("CountPlay") ) {
		data += processStandard(contents);
	    }

	}
	reader.close();
	return data;
    }

    // The standard input processing function. Returns a String of all 
    // words in the contents array. The method is a template, and can
    // be modified later to handle other inputs.
    private String processStandard( String[] contents ) {
	String data = "";

	for (int i=0; i<contents.length; ++i) {
	    if (contents[i].length() > 0) {
		data += contents[i];
	    }
	}
	return data;
    }

    // Parses fuego output for GTP Command "showboard"
    public String readShowBoard(String output) throws IOException {
	Scanner reader = new Scanner(output);

	String data = "";
	String[] contents;
	String next_line;

	while (reader.hasNextLine()) {
	    next_line = reader.readLine()
	    contents = next_line.split(" ");
	    data += processShowBoard(contents);
	}

	reader.close();
	System.out.println(data.length());
	// remove the last comma

	return data.substring(0,data.length()-1);
    }

    // Converts the fuego board ouput to a more human-readable version.
    // Output is of the form "E,E,B,W,B,E,E,"
    private String processShowBoard( String[] contents ) {
	String data = "";		
	for (int i = 0; i < contents.length; i++) {
	    // If the board space is empty
	    if (contents[i].equals(".") || contents[i].equals("+")) {
		data += "E,";
	    }
	    // If the board space holds a Black stone
	    else if (contents[i].equals("X")) {
		data += "B,";
	    }
	    // If the board space holds a White stone
	    else if (contents[i].equals("O")) {
		data += "W,";
	    }
	}
	return data; 
    }

    // In .sgf format, the first line begins with open brackets and contains 
    // Game Recorder information. Every other line that documents a move is of
    // the form ";B[dc]"
    public ArrayList<String> readMoves(String sgf_contents ) throws IOException {
	Scanner reader = new Scanner(sgf_contents);

	String[] contents;
	ArrayList<String> data = new ArrayList<String>();
	String next_line;

	while (reader.hasNextLine()) {
	    next_line = reader.readLine();

	    contents = next_line.split(" ");
	    // If the current line is nonempty, and the first character is 
	    // not just whitespace.
	    if ( contents.length > 0 && contents[0].length() > 0 ) {
		// if the first character in the line is ";"
		String first_character = contents[0].substring(0,1);
		if ( first_character.equals(";") ) {
		    // send the coordinates (in letter form) to the processor
		    // function to convert to numbers.
		    String coordinates = contents[0].substring(3,5);
		    data.add( contents[0].substring(1,2) + processSgfPosition(coordinates));	
		}
	    }	
	}
	reader.close();
	return data;
    }

    // Converts letter notation of .sgf file to coordinate number (x,y)
    // x is left to right, y is top to bottom.
    private String processSgfPosition(String letters) {
	return translate.get(letters.substring(0,1)) + translate.get(letters.substring(1,2));
    }

    // A simple file writer. Currently set to always append to the file at the given path.
    public void writeToFile(String path, String content) throws IOException {
	FileWriter writer = new FileWriter(path , true);
	PrintWriter printer = new PrintWriter( writer );

	printer.printf( "%s" + "%n" , content);

	printer.flush();
	printer.close();
	writer.flush();
	writer.close();
    }
}


