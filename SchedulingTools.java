
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class SchedulingTools {
	
	public static int X; //random large number
	static File randomFile;
	static Scanner random;
	static boolean OScontrol; //true if no process is running thus need scheduling, false otherwise
	static final boolean log = false;
	static ArrayList<Process> processList = new ArrayList<Process>();
	
	static int cycle;
	static int tfinish;
	static int totalCPU;
	static int totalIO;
	static int totalP;
	static int totalTurnaround;
	static int totalWait;
	
	/**
	 * open random-numbers file and check for errors concerning reading the file
	 * @author zhouyangli 
	 * @throws FileNotFoundException 
	 */
	public static void openRandomFile() throws FileNotFoundException{
		File file = new File("random-numbers.txt");
		if(!file.exists()) {
			System.err.println("Error: Cannot find random number file");
			System.exit(0);
		}
		if(!file.canRead()) {
			System.err.println("Error: Cannot read random number file");
			System.exit(0);
		}
		randomFile = file;
		random = new Scanner(randomFile);
	}
	
	/**
	 * generates uniformly distributed random integers in the interval (0,U]
	 * the IOBurst when block and CPUBurst when run
	 * @param U the upper bound
	 * @return the USRI in interval (0,U]
	 * @author zhouyangli 
	 */
	public static int randomOS(int U){
		
		X = random.nextInt();
		//System.out.printf("Random large number: %d%n", X);
		
		return 1 + (X % U);
	}
	
	/**
	 * initialize static variables
	 * @param list
	 * @author zhouyangli 
	 */
	static void init(ArrayList<Process> list){
		processList.clear();
		for(Process p : list) {
			Process temp = new Process(p);
			processList.add(temp);
		}
		cycle = 0;
		tfinish = 0;
		totalCPU = 0;
		totalIO = 0;
		totalP = processList.size();
		totalTurnaround = 0;
		totalWait = 0;
	}
	
	/**
	 * print verbose output
	 * @param cycle
	 * @author zhouyangli 
	 */
	static void printVerbose(int cycle) {
		System.out.printf("%-12s%3d:", "Before cycle", cycle);
		for(Process p: processList) {
			String state = "";
			switch(p.curState) {
				case 0: state = "unstarted";
				break;
				case 1: state = "ready";
				break;
				case 2: state = "running";
				break;
				case 3: state = "blocked";
				break;
				case 4: state = "terminated";
				break;
			}
			System.out.printf("%12s%3d", state, p.tb);
		}
		System.out.println();
	}
	
	/**
	 * count cpu utilization
	 * @author zhouyangli 
	 */
	static void countCPU() {
		for(Process p : processList) {
			if(p.curState == Process.RUNNING) {
				totalCPU ++;
				break;
			}
		}
	}
	
	/**
	 * count io utilization
	 * @author zhouyangli 
	 */
	static void countIO() {
		for(Process p : processList) {
			if(p.curState == Process.BLOCKED) {
				totalIO ++;
				break;
			}
		}
	}
	
	/**
	 * print process informations and summary
	 * @author zhouyangli 
	 */
	static void printSummary() {
		System.out.println();
		//print process informations
		for(int i = 0; i < processList.size(); i++) {
			Process p = processList.get(i);
			System.out.println("Process " + i);
			System.out.printf("\t(A,B,C,IO) = (%d,%d,%d,%d)%n", p.A, p.B, p.C, p.IO);
			System.out.printf("\tFinishing time: %d%n", p.tfinish);
			System.out.printf("\tTurnaround time: %d%n", p.tturnaround);
			System.out.printf("\tI/O time: %d%n", p.tio);
			System.out.printf("\tWait time: %d%n%n", p.twait);
			
			totalTurnaround += p.tturnaround;
			totalWait += p.twait;
		}
		//print summary
		double cpuUtil = (double)totalCPU/tfinish;
		double ioUtil = (double)totalIO/tfinish;
		double throughput = (double)totalP/tfinish*100;
		double aveTurnaround = (double)totalTurnaround/totalP;
		double aveWait = (double)totalWait/totalP;
		
		System.out.println("Summary Data:");
		System.out.printf("\tFinishing time: %d%n", tfinish);
		System.out.printf("\tCPU Utilization: %.6f%n", cpuUtil);
		System.out.printf("\tI/O Utilization: %.6f%n", ioUtil);
		System.out.printf("\tThroughput: %.6f processes per hundred cycles%n", throughput);
		System.out.printf("\tAverage turnaround time: %.6f%n", aveTurnaround);
		System.out.printf("\tAverage waiting time: %.6f%n", aveWait);
		System.out.println();
	}
	
	
	//scheduling methods
	/**
	 * First come first serve scheduling algorithm
	 * @param list processes to schedule
	 * @param verbose whether to print out verbose output
	 * @throws FileNotFoundException
	 * @author zhouyangli 
	 */
	public static void FCFS(ArrayList<Process> list, boolean verbose) throws FileNotFoundException {
		//initialize static variables
		init(list);
		
		boolean schedule = true;
		ArrayList<Process> ready = new ArrayList<Process>();
		Scanner random = new Scanner(randomFile);
		OScontrol = true;
		
		System.out.printf("%nThe scheduling algorithm used was First Come First Served%n");
		if(verbose) {
			System.out.printf("This detailed printout gives the state and remaining burst for each process%n");
		}
		System.out.println();
		
		while(schedule) {
			//print verbose output
			if(verbose) {
				printVerbose(cycle);
			}
			
			countCPU();
			countIO();
			
			//start the next cycle
			for(Process p : processList) {
				p.nextCycle();
			}
			
			//check if any process changes state
			for(Process p: processList) {
				switch(p.curState) {
				case 0: //if process unstarted, check if it arrives
					if(p.A == cycle) {
						p.start();
						ready.add(p);
					}
					break;
				case 2: //if process is running, check if it should terminate/block
					if(p.tr == 0) {
						p.terminate();
						p.setTfinish(cycle);
						OScontrol = true;
					}
					else if(p.trcpu == 0) {
						p.block();
						OScontrol = true;
					}
					break;
				case 3: //if process is blocked, check if it unblocks
					if(p.triob == 0) {
						p.unblock();
						ready.add(p);
					}
					break;
				}
			}
			
			//if scheduling is needed (no process running), run the first process from the ready list
			if(OScontrol && !ready.isEmpty()) {
				//X = random.nextInt();
				Process p = ready.remove(0);
				p.run();
				OScontrol = false;
			}

			//if all processes are terminated, stop scheduling
			for(Process p : processList) {
				if (p.curState != Process.TERMINATED) {
					schedule = true;
					break;
				}
				schedule = false;
				tfinish = cycle;
			}
			
			cycle ++;
		}
		
		printSummary();
	}
	
	
	/**
	 * Round Robin Scheduling algorithm
	 * @param list
	 * @param verbose
	 * @throws FileNotFoundException
	 * @author zhouyangli 
	 */
	public static void RR(ArrayList<Process> list, boolean verbose) throws FileNotFoundException {
		//initialize static variables
		init(list);
		for (int i = 0; i < processList.size(); i++) {
			Process tmp = processList.get(i);
			tmp.q = 2;
			tmp.qr = 2;
		}
		
		boolean schedule = true;
		ArrayList<Process> ready = new ArrayList<Process>();
		Scanner random = new Scanner(randomFile);
		OScontrol = true;
		
		System.out.printf("%nThe scheduling algorithm used was Round Robin%n");
		if(verbose) {
			System.out.printf("This detailed printout gives the state and remaining burst for each process%n");
		}
		System.out.println();
		
		while(schedule) {

			//print verbose output
			if(verbose) {
				printVerbose(cycle);
			}
			
			countCPU();
			countIO();
			
			//start the next cycle
			for(Process p : processList) {
				p.nextCycle();
			}
			//q--;
			
			//check if any process changes state
			for(Process p: processList) {
				switch(p.curState) {
				case 0: //if process unstarted, check if it arrives
					if(p.A == cycle) {
						p.start();
						
						ready.add(p);
					}
					break;
				case 2: //if process is running, check if it should terminate/block
					if(p.tr == 0) {
						p.terminate();
						p.setTfinish(cycle);
						OScontrol = true;
					}
					else if(p.trcpu == 0) {
						p.block();
						OScontrol = true;
					}
					else if(p.tb == 0) {
						p.preempted();
						ready.add(p);
						OScontrol = true;
					}
					break;
				case 3: //if process is blocked, check if it unblocks
					if(p.triob == 0) {
						p.unblock();
						ready.add(p);
					}
					break;
				}
			}
			
			//if scheduling is needed (no process running), run the first process from the ready list
			if(OScontrol && !ready.isEmpty()) {
				//X = random.nextInt();
				Process p = ready.remove(0);
				p.run();
				//q = 2;
				OScontrol = false;
			}

			//if all processes are terminated, stop scheduling
			for(Process p : processList) {
				if (p.curState != Process.TERMINATED) {
					schedule = true;
					break;
				}
				schedule = false;
				tfinish = cycle;
			}
			
			cycle ++;
		}
		
		printSummary();	
	}
	
	//scheduling methods
	/**
	 * Last come first serve scheduling algorithm
	 * @param list processes to schedule
	 * @param verbose whether to print out verbose output
	 * @throws FileNotFoundException
	 * @author zhouyangli 
	 */
	public static void LCFS(ArrayList<Process> list, boolean verbose) throws FileNotFoundException {
		//initialize static variables
		init(list);
		
		boolean schedule = true;
		ArrayList<Process> ready = new ArrayList<Process>();
		ArrayList<Process> readyOneCycle = new ArrayList<Process>();
		Scanner random = new Scanner(randomFile);
		OScontrol = true;
		
		System.out.printf("%nThe scheduling algorithm used was Last Come First Served%n");
		if(verbose) {
			System.out.printf("This detailed printout gives the state and remaining burst for each process%n");
		}
		System.out.println();
		
		while(schedule) {
			//print verbose output
			if(verbose) {
				printVerbose(cycle);
			}
			
			countCPU();
			countIO();
			
			//start the next cycle
			for(Process p : processList) {
				p.nextCycle();
			}
			
			//check if any process changes state
			for(Process p: processList) {
				switch(p.curState) {
				case 0: //if process unstarted, check if it arrives
					if(p.A == cycle) {
						p.start();
						readyOneCycle.add(p);
					}
					break;
				case 2: //if process is running, check if it should terminate/block
					if(p.tr == 0) {
						p.terminate();
						p.setTfinish(cycle);
						OScontrol = true;
					}
					else if(p.trcpu == 0) {
						p.block();
						OScontrol = true;
					}
					break;
				case 3: //if process is blocked, check if it unblocks
					if(p.triob == 0) {
						p.unblock();
						readyOneCycle.add(p);
					}
					break;
				}
			}
			//*****LCFS IMP
			ready.addAll(0,readyOneCycle);
			readyOneCycle.clear();
			
			//if scheduling is needed (no process running), run the last process from the ready list
			if(OScontrol && !ready.isEmpty()) {
				//X = random.nextInt();
				//Collections.reverse(ready);
				Process p = ready.remove(0);
				//sCollections.reverse(ready);
				p.run();
				OScontrol = false;
			}

			//if all processes are terminated, stop scheduling
			for(Process p : processList) {
				if (p.curState != Process.TERMINATED) {
					schedule = true;
					break;
				}
				schedule = false;
				tfinish = cycle;
			}
			
			cycle ++;
		}
		
		printSummary();
	}
	
	

	
	/**
	 * Shortest Job First Scheduling Algorithm
	 * @param list
	 * @param verbose
	 * @throws FileNotFoundException
	 * @author zhouyangli 
	 */
	public static void PSJF(ArrayList<Process> list, boolean verbose) throws FileNotFoundException {
		//initialize static variables
		init(list);
		
		boolean schedule = true;
		ArrayList<Process> ready = new ArrayList<Process>();
		//*****record the running process
		Process curProcess = new Process();
		Scanner random = new Scanner(randomFile);
		OScontrol = true;
		
		System.out.printf("%nThe scheduling algorithm used was Preemptive Shortest Job First%n");
		if(verbose) {
			System.out.printf("This detailed printout gives the state and remaining burst for each process%n");
		}
		System.out.println();
		
		while(schedule) {
			//print verbose output
			if(verbose) {
				printVerbose(cycle);
			}
			
			countCPU();
			countIO();
			
			//start the next cycle
			for(Process p : processList) {
				p.nextCycle();
			}
			
			//check if any process changes state
			for(Process p: processList) {
				switch(p.curState) {
				case 0: //if process unstarted, check if it arrives
					if(p.A == cycle) {
						p.start();
						ready.add(p);
					}
					break;
				case 2: //if process is running, check if it should terminate/block
					if(p.tr == 0) {
						p.terminate();
						p.setTfinish(cycle);
						OScontrol = true;
					}
					else if(p.trcpu == 0) {
						p.block();
						OScontrol = true;
					}else{
						//******record it
						curProcess = p;
					}
					break;
				case 3: //if process is blocked, check if it unblocks
					if(p.triob == 0) {
						p.unblock();
						ready.add(p);
					}
					break;
				}
			}
			
			//if scheduling is needed (no process running), run the first process from the ready list
			if(!ready.isEmpty()) {
				Collections.sort(ready, Process.ComparatorTR());				
				if(OScontrol){
					Process p = ready.remove(0);
					//X = random.nextInt();					
					p.run();
					OScontrol = false;
				//********block current process,run the shortest reaming time process
				}else if(ready.get(0).getTr() < curProcess.getTr()){
					Process p = ready.remove(0);
					//X = random.nextInt();	
					p.run();		
					curProcess.preempted();
					ready.add(curProcess);
				}
				
			
			}

			//if all processes are terminated, stop scheduling
			for(Process p : processList) {
				if (p.curState != Process.TERMINATED) {
					schedule = true;
					break;
				}
				schedule = false;
				tfinish = cycle;
			}
			
			cycle ++;
		}
		
		printSummary();
	}

}
