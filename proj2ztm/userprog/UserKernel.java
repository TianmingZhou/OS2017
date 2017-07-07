package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.*;

/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
	// added
	private static boolean[] pageIdle;
	
	static {
		initializePages ();
	}
	
	public static void initializePages () {pageIdle = new boolean [Machine.processor().getNumPhysPages()]; for (int i = 0; i < pageIdle.length; i++) pageIdle[i] = true;}
	
	public static boolean getPageStates (int id) {
		Lib.assertTrue(id >= 0 && id < pageIdle.length);
		return pageIdle[id];
	}
	
	public static int allocatePage () {
		boolean intStatus = Machine.interrupt().disable();
		int id; for (id = pageIdle.length-1; id >= 0 && !pageIdle[id]; id--) ;
		if (id >= 0) {pageIdle[id] = false;}
		Machine.interrupt().restore(intStatus);
		return id;
	}
	
	public static void deallocatePage (int id) {
		boolean intStatus = Machine.interrupt().disable();
		Lib.assertTrue(id >= 0 && id < pageIdle.length && pageIdle[id] == false);
		pageIdle[id] = true;
		Machine.interrupt().restore(intStatus);
	}
	
	/**
	 * Allocate a new user kernel.
	 */
	public UserKernel() {
		super();
	}

	/**
	 * Initialize this kernel. Creates a synchronized console and sets the
	 * processor's exception handler.
	 */
	public void initialize(String[] args) {
		super.initialize(args);

		console = new SynchConsole(Machine.console());
		
		Machine.processor().setExceptionHandler(new Runnable() {
				public void run() { exceptionHandler(); }
			});
	}

	public void selfTest() {
		super.selfTest();
		
/*		System.out.println("Test UserKernel:");
		selfTest_Page ();
		System.out.println("*************************************");
		
		System.out.println("Test UserProcess:");
		UserProcess.selfTest();
		System.out.println("*************************************");	*/
	}
	public void selfTest_Page () {
	// does not use histotical info
		boolean pageOwned[] = new boolean [Machine.processor ().getNumPhysPages ()];
		int nOwned = 0;
		Lib.assertTrue (pageOwned.length == pageIdle.length);
		for (int i = 0; i < pageOwned.length; i++) pageOwned[i] = false;
		
		Random rand = new Random (19960305);
		int cnt_full = 0, cnt_empty = 0;	// # skips because of this reason
		int lastop = 0;
		int nalsucc = 0, ndesucc = 0;	// # subseries of >= 10 succesive allocations / deallocations
		int total_op = 1000000, succthreshold = 10;
		for (int __ = 0, ppn; __ < total_op; __++) {
			if (rand.nextBoolean ()) {
				if (lastop > 0) {if (++lastop == succthreshold && nOwned < pageOwned.length) nalsucc++;} else lastop = 1;
				ppn = allocatePage ();
				Lib.assertTrue (ppn >= -1 && ppn < pageOwned.length);
				Lib.assertTrue ((ppn == -1) == (nOwned == pageOwned.length));
				if (ppn != -1) {Lib.assertTrue (!pageOwned[ppn]); nOwned++; pageOwned[ppn] = true;}
				else cnt_full++;
			}
			else {
				if (lastop < 0) {if (--lastop == -succthreshold && nOwned > 0) ndesucc++;} else lastop = -1;
				if (nOwned == 0) {cnt_empty++; continue;}
				int r = rand.nextInt (nOwned);
				for (ppn = 0; ppn < pageOwned.length; ppn++) if (pageOwned[ppn] && --r == -1) break;
				deallocatePage (ppn);
				nOwned--; pageOwned[ppn] = false;
			}
		}
		
		System.out.println ("total op:\t" + total_op);
		System.out.println ("alkane:\t" + cnt_full);
		System.out.println ("graphene:\t" + cnt_empty);
		System.out.println ("contiguous oxidations " + succthreshold + ":\t" + nalsucc);
		System.out.println ("contiguous reductions " + succthreshold + ":\t" + ndesucc);
		
		for (int i = 0; i < pageOwned.length; i++) if (pageOwned[i]) deallocatePage (i);
	}

	/**
	 * Returns the current process.
	 *
	 * @return		the current process, or <tt>null</tt> if no process is current.
	 */
	public static UserProcess currentProcess() {
		if (!(KThread.currentThread() instanceof UThread))
			return null;
		
		return ((UThread) KThread.currentThread()).process;
	}

	/**
	 * The exception handler. This handler is called by the processor whenever
	 * a user instruction causes a processor exception.
	 *
	 * <p>
	 * When the exception handler is invoked, interrupts are enabled, and the
	 * processor's cause register contains an integer identifying the cause of
	 * the exception (see the <tt>exceptionZZZ</tt> constants in the
	 * <tt>Processor</tt> class). If the exception involves a bad virtual
	 * address (e.g. page fault, TLB miss, read-only, bus error, or address
	 * error), the processor's BadVAddr register identifies the virtual address
	 * that caused the exception.
	 */
	public void exceptionHandler() {
		Lib.assertTrue(KThread.currentThread() instanceof UThread);

		UserProcess process = ((UThread) KThread.currentThread()).process;
		int cause = Machine.processor().readRegister(Processor.regCause);
		process.handleException(cause);
	}

	/**
	 * Start running user programs, by creating a process and running a shell
	 * program in it. The name of the shell program it must run is returned by
	 * <tt>Machine.getShellProgramName()</tt>.
	 *
	 * @see		nachos.machine.Machine#getShellProgramName
	 */
	public void run() {
		super.run();
		
		UserProcess process = UserProcess.newUserProcess();
		
		String shellProgram = Machine.getShellProgramName();
		Lib.assertTrue(process.execute(shellProgram, new String[] { }));
		
		KThread.currentThread().finish();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}

	/** Globally accessible reference to the synchronized console. */
	public static SynchConsole console;

	// dummy variables to make javac smarter
	private static Coff dummy1 = null;
}
