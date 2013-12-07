import java.io.*;
import java.util.*;

/**
 * A method that processes .sgf files into output for passing to fuego, and fuego text output to our board representation
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
    /*
    public static void main(String[] args) {

	Transcripter tranny = new Transcripter();
	System.out.println(tranny.genFuegoMove("B43"));	

	File[] files = new File("SGF_files/").listFiles();
	String[] file_names = new String[files.length];	

	for (int i = 0; i < files.length; i++) {
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
    */

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
    /*
     * processStandard is used for...
     * processStandard takes in ... 
     * it assumes that the input is in the form...
     *
     * it returns ...
     */

    private String processStandard( String[] contents ) {
	String data = "";

	for (int i=0; i<contents.length; ++i) {
	    if (contents[i].length() > 0) {
		data += contents[i];
	    }
	}
	return data;
    }

    /*
     * readShowBoard is used for translating the fuego output for
     * "showboard", a single string representing the game board.
     * 
     * It returns a board representation in the form of a comma-separated
     * 81 position string E,B,W,... where every 9 letters represents a row.
     * 
     * readShowBoard calls processShowBoard to process individual lines of the 
     * board, passing it the line split by spaces.  The appends the string
     * returned by readShowBoard to the running game state string.
     */

    public String readShowBoard (String output) throws IOException {
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

    /* 
     * processShowBoard translates a line of fuego output to our output
     *
     * processShowBoard takes a single line of the fuego board state, in the form of 
     * (X . . . . O . . . ...)
     * and maps them to a B,E,E,...,W, string
     *
     * for every line 
     */

    private String processShowBoard (String[] contents) {
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
    
    /*
     * readMoves takes a string representing the contents of an sgf file
     * and returns an arrayList of the string moves for the game
     *
     * .sgf files formatted such that each move is prepended with a semicolon
     * we split on the semi-colon and map each move (;) B[ab] (;) -> Bxy
     * where x and y are the integer coordinates of the move.
     *
     * we call processSgfPosition to map some ab -> xy 
     *
     * the overall arraylist is then an ordered list of the strings of the form bxy
     */

    public ArrayList<String> readMoves (File sgfFile) throws IOException {
	Scanner reader = new Scanner(sgfFile);

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
    /*
     * processSgfPosition maps a string "ab" to "xy", where xy is the string for the integer position index
     */

    // Converts letter notation of .sgf file to coordinate number (x,y)
    // x is left to right, y is top to bottom.
    private String processSgfPosition (String letters) {
	return translate.get(letters.substring(0,1)) + translate.get(letters.substring(1,2));
    }

    // A simple file writer. Currently set to always append to the file at the given path.
    
    public void writeToFile (String path, String content) throws IOException {
	FileWriter writer = new FileWriter(path , true);
	PrintWriter printer = new PrintWriter( writer );
	printer.printf( "%s" + "%n" , content);
	printer.flush();
	printer.close();
	writer.flush();
	writer.close();
    }

    /*
     * getFuegoMove takes in a single move of the form "Bxy" and maps it back to the fuego command
     * for making the move
     */

    public String genFuegoMove(String kami_move) {
	String fuego_string = "";		
	String color = kami_move.substring(0,1);

	int row = Integer.parseInt(kami_move.substring(1,2));
	int col = Integer.parseInt(kami_move.substring(2,3));

	if (color.equals("B")) {
	    fuego_string += "play Black ";
	} else {
	    fuego_string += "play White ";
	}

	fuego_string += KF_translate1[row] + KF_translate2[col];
	return fuego_string;
    }
}


