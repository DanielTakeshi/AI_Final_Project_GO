import java.io.*;
import java.util.*;

/**
 * A method that processes fuego output and .sgf files.
 *
 * (c) 2013 by Simon, Daniel, and Philippe
 *
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
	// Map from Kami Go notation to Fuego Notation
	// Fuego uses stupid orientation, so we have to deal
    private String[] KF_translate1 = {"A","B","C","D","E","F","G","H","J"};
	private String[] KF_translate2 = {"9","8","7","6","5","4","3","2","1"};

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
    };

    public static void main(String[] args) {
	
	Transcripter tranny = new Transcripter();
	System.out.println( tranny.getFuegoMove( "B43" ) );	
	File[] files = new File("SGF_files/").listFiles();
	String[] file_names = new String[files.length];	
	for (int i=0; i<files.length; ++i) {
		file_names[i] = "SGF_files/" + files[i].getName();
		System.out.println(file_names[i]);
	}
	ReadFile reader = new ReadFile();
	try {
	    String game_string = reader.openFile(file_names[0]);
	    ArrayList<String> game_list = tranny.readMoves(game_string);
	    for (int i = 0; i < game_list.size(); i++) {
		//System.out.println(game_list.get(i));
	    }
	} catch (IOException e) {
	    System.out.println(e);
	}
    }


    // Parses fuego output for GTP Command "go_board"
    // Don't need this for training, can do this later!
    public String readGoBoard(String output) throws IOException {

	Scanner reader = new Scanner(output);
	String data = "";
	String[] contents;
	String next_line = reader.nextLine();

	while (reader.hasNextLine()) {
	    next_line = reader.nextLine();

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
	    next_line = reader.nextLine();
	    contents = next_line.split(" ");
	    data += processShowBoard(contents);
	}

	reader.close();
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

    // In .sgf format, moves are separated by semicolons, i.e., ";B[xy]". We thus
    // split on those semicolons, but we have 1 special case where one line is ";".
    public ArrayList<String> readMoves(String sgf_contents ) throws IOException {
	Scanner reader = new Scanner(sgf_contents);

	String[] contents;
	ArrayList<String> data = new ArrayList<String>();
	String next_line;


	while (reader.hasNextLine()) {
	    next_line = reader.nextLine();
	    contents = next_line.split(";");
	    // Take care of case when we may have one semicolon, so = ["", ""]
	    if ( contents.length > 1 && contents[1].length() > 0 ) {
		for (int i=1; i<contents.length; ++i) {
		    // send the coordinates (in letter form) to the processor
		    String coordinates = contents[i].substring(2,4);
		    data.add( contents[i].substring(0,1) + processSgfPosition(coordinates));
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

	public String getFuegoMove(String kami_move) {
		String fuego_string = "";		
		String color = kami_move.substring(0,1);
		int row = Integer.parseInt(kami_move.substring(1,2));
		int col = Integer.parseInt(kami_move.substring(2,3));
		if (color.equals("B")) {
			fuego_string += "play Black ";
		}
		else {
			fuego_string += "play White ";
		}
		fuego_string += KF_translate1[row] + KF_translate2[col];
		return fuego_string;
	}
}


