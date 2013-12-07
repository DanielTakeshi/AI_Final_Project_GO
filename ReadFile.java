import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;
import java.io.*;

public class ReadFile {

    public ReadFile() {
    }

    public String openFile(String file_path) throws IOException {
		Scanner scanny = new Scanner(new File(file_path));

		//FileReader reader = new FileReader(file_path);
		//BufferedReader text_reader = new BufferedReader(reader);

		ArrayList<String> data = new ArrayList<String>();
		String contents = "";
		String next_line;
		while (scanny.hasNextLine()) {
			next_line = scanny.nextLine();
			contents += next_line;
			contents += "\n";
		}
		scanny.close();
		return contents;
    }
}
