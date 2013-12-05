import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.*;

public class ReadFile {

	private String file_path;

	public ReadFile(String path) {
		file_path = path;
	}

	public String openFile() throws IOException {
		FileReader reader = new FileReader(file_path);
		BufferedReader text_reader = new BufferedReader(reader);

		ArrayList<String> data = new ArrayList<String>();
		String contents = "";
		String next_line = text_reader.readLine();
		while (next_line != null) {
			contents += next_line;
			contents += "\n";
			next_line = text_reader.readLine();
		}
		text_reader.close();
		return contents;
	}
}
