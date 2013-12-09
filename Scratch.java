import java.io.*;
import java.util.*;

public class Scratch {
    public static void main (String[] args) {
	int numRead = 0;
	try {
	    BufferedReader readIn = new BufferedReader(new InputStreamReader(System.in));
	    boolean loop_forever = true;
	    while (loop_forever) {
		if (readIn.ready()) {
		    
		    Scanner scanny = new Scanner(readIn);
		    while(scanny.hasNextLine()) {
			String inString = scanny.nextLine();
			System.out.println(inString );
			System.out.print((char) 4);
			loop_forever = false;
			break;
		    }
		} else {
		    Thread.sleep(1000);
		}
	    }
	    
	    while (true) {
		System.out.println("output");
		Thread.sleep(10000);
	    }
	} catch (Exception e) {
	    System.out.println("execption");
	}
	
    }
}