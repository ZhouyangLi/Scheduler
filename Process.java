
import java.util.Comparator;


public class Process {
	
	public static final int UNSTARTED = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int BLOCKED = 3;
	public static final int TERMINATED = 4;
	
	int A; //arrival time
	int B; //cpu burst upper bound
	int C; //total cpu time
	int IO; //io burst upper bound
	
	int tfinish; //finishing time
	int tturnaround; //turnaround time
	int tio; //total i/o time
	int twait; //total waiting time
	
	int tcpu; //CPU burst time
	int trcpu; //remaining CPU burst time
	int tiob; //I/O burst time
	int triob; //remaining I/O burst time
	int tb; //remaining burst
	int tr; //remaining (total) CPU time
	int q; //quantum
	int qr; //quantum remain


	int curState = UNSTARTED; //current state
	
	//constructors
	public Process() {
		
	}
	public Process(int A, int B, int C, int IO) {
		this.A = A;
		this.B = B;
		this.C = C;
		this.IO = IO;
		
		this.tr = C;
		this.tio = 0;
		this.twait = 0;
		this.q = -1;
		this.qr = -1;
	}
	public Process(Process p) {
		this.A = p.A;
		this.B = p.B;
		this.C = p.C;
		this.IO = p.IO;
		
		this.tr = p.tr;
		this.tio = 0;
		this.twait = 0;
		this.q = -1;
		this.qr = -1;
	}
	
	//getters and setters
	public void setTfinish(int t) {
		this.tfinish = t;
		this.setTturnaround();
	}
	
	private void setTturnaround() {
		this.tturnaround = this.tfinish - this.A;
	}
	
	//methods
	
	/**
	 * process starts
	 */
	public void start() {
		this.curState = READY;
	}
	
	/**
	 * process begins to run
	 */
	public void run() {
		this.curState = RUNNING;
		if(trcpu!=0) {
			this.tb = this.tcpu;
			if(this.q == 2) {
				if (this.tb > 2) {
					this.tb = 2;
				}
			}
		}
		else {
			this.tcpu = SchedulingTools.randomOS(this.B);
			if(this.tr < this.tcpu) {
				this.tcpu = this.tr;
			}
			this.trcpu = this.tcpu;
			this.tb = this.tcpu;
			if(this.q == 2) {
				if (this.tb > 2) {
					this.tb = 2;
				}
			}
		}
	}
	
	/**
	 * process blocks
	 */
	public void block() {
		this.curState = BLOCKED;
		this.tiob = SchedulingTools.randomOS(this.IO);
		this.triob = this.tiob;
		this.tb = this.tiob;
	}
	
	/**
	 * process unblocks
	 */
	public void unblock() {
		this.curState = READY;
	}
	
	public void preempted() {
		this.curState = READY;
	}
	
	/**
	 * process terminates
	 */
	public void terminate() {
		this.curState = TERMINATED;
	}
	
	/**
	 * time pass for one cycle
	 */
	public void nextCycle() {
		if(this.curState == RUNNING) {
			this.trcpu --;
			this.tb --;
			this.tr --;
			//this.qr --;
		}
		if(this.curState == BLOCKED) {
			this.triob --;
			this.tb --;
			this.tio ++;
		}
		if(this.curState == READY) {
			this.twait ++;
		}
	}
	
	//comparators
	public static Comparator<Process> ComparatorA() {
		return new Comparator<Process>() {
			public int compare(Process p1, Process p2) {
				return Integer.compare(p1.A, p2.A);
			}
		};
	}
	
	public static Comparator<Process> ComparatorTR() {
		return new Comparator<Process>() {
			public int compare(Process p1, Process p2) {
				return Integer.compare(p1.tr, p2.tr);
			}
		};
	}
	
	public int getTr() {
		return tr;
	}
	public void setTr(int tr) {
		this.tr = tr;
	}

	
}
