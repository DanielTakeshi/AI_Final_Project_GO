import java.io.*;
import java.util.*;

public class Scratch {
    public static void main (String[] args) {
	int numRead = 0;
	try {
	    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
	    while (true) {
		if (readIn.ready()) {
		    Scanner scanny = new Scanner(readIn);
		    while (scanny.hasNextLine()) {
			String inString = scanny.nextLine();
			System.out.println(inString);
		    }
		} else {
		    System.out.println("waiting");
		    Thread.sleep(1000);
		}
	    }
	} catch (Exception e) {
	    System.out.println("execption");
	}
    }
}