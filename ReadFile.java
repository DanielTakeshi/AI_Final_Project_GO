import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;
import java.io.*;

public class ReadFile {

    // Number of lines to be read, if -1, then read all lines
    private int num_lines = -1;

    public ReadFile() {
    }

    public ReadFile(int num) {
	num_lines = num;
    }

    public String openFile(File file) throws IOException {
	Scanner scanny = new Scanner(file);

	String contents = "";
	String next_line;
	// counts lines, if we only want a certain number read
	int line_count = 0;
	while (scanny.hasNextLine()) {
	    next_line = scanny.nextLine();
	    contents += next_line;
	    contents += "\n";
	    line_count++;
	    if ( num_lines != -1 && line_count >= num_lines ) {
		break;
	    }
	}
	scanny.close();
	return contents;
    }
}
