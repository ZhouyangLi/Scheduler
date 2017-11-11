
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class Scheduling {
	public static void main(String[] args) throws FileNotFoundException {
		
		boolean verbose = false;
		String inputFilename;
		
		ArrayList<Process> processList = new ArrayList<Process>();
		
		//get input file name and verbose flag from arguments
		if(args.length < 1) {
			System.out.println("Error: missing name of the input file");
			System.exit(1);
		}
		if(args[0].equals("--verbose")){
			verbose = true;
			if(args.length < 2) {
				System.out.println("Error: missing name of the input file");
				System.exit(1);
			}
			inputFilename = args[1];
		}
		else {
			inputFilename = args[0];
		}
		
				
		//detect errors concerning reading the file
		File inputFile = new File(inputFilename);
		if(!inputFile.exists()) {
			System.err.println("Error: No such file");
			System.exit(0);
		}
		if(!inputFile.canRead()) {
			System.err.println("Error: Cannot read file");
			System.exit(0);
		}
		//read input file
		Scanner input = new Scanner(inputFile);
		int n = input.nextInt();
		for(int i = 0; i < n; i++) {
			//String s = input.next().substring(1);
			int A = input.nextInt();
			int B = input.nextInt();
			int C = input.nextInt();
			//s = input.next();
			//s = s.substring(0, s.length() - 1);
			int M = input.nextInt();
			
			
			Process p = new Process(A, B, C, M);
			processList.add(p);
		}
		
		//print original input
		System.out.printf("%-23s %d ", "The original input was:", n);
		for(int i = 0; i < processList.size(); i++) {
			Process p = processList.get(i);
			System.out.printf("	%d %d %d %d ", p.A, p.B, p.C, p.IO);
		}
		
		//sort process list by process arrival time
		Collections.sort(processList, Process.ComparatorA());
		System.out.printf("%n%-23s %d ", "The (sorted) input is:", n);
		for(int i = 0; i < processList.size(); i++) {
			Process p = processList.get(i);
			System.out.printf("	%d %d %d %d ", p.A, p.B, p.C, p.IO);
		}
		System.out.println();
		
		SchedulingTools.openRandomFile();
		SchedulingTools.FCFS(processList, verbose);
		SchedulingTools.openRandomFile();
		SchedulingTools.RR(processList, verbose);
		SchedulingTools.openRandomFile();
		SchedulingTools.LCFS(processList, verbose);
		SchedulingTools.openRandomFile();
		SchedulingTools.PSJF(processList, verbose);
		
		
		
	}//end of main

}//end of class
