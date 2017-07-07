//yhdxt`oi`offt`of{inofinofmhphofx`ofxholhofuh`ov`ofphorih
//PART OF THE NACHOS. DON'T CHANGE CODE OF THIS LINE
package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {	// added modified
	/**
	 * Allocate a new lottery scheduler.
	 */
	public LotteryScheduler() {
		super ();	// added
	}

	/**
	 * Allocate a new lottery thread queue.
	 *
	 * @param		transferPriority		<tt>true</tt> if this queue should
	 *										transfer tickets from waiting threads
	 *										to the owning thread.
	 * @return		a new lottery thread queue.
	 */
	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new LotteryQueue(transferPriority);
	}
	
	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
		
		return getThreadState(thread).getPriority();
	}
	
	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
		
		return getThreadState(thread).getEffectivePriority();
	}
	
	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());
		
		Lib.assertTrue(priority >= priorityMinimum
					   && priority <= priorityMaximum);
		
		getThreadState(thread).setPriority(priority);
	}
	
	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		
		KThread thread = KThread.currentThread();
		
		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			return false;
		
		setPriority(thread, priority + 1);
		
		Machine.interrupt().restore(intStatus);
		return true;
	}
	
	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		
		KThread thread = KThread.currentThread();
		
		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			return false;
		
		setPriority(thread, priority - 1);
		
		Machine.interrupt().restore(intStatus);
		return true;
	}
	
	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;
	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 1;	// added modified
	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = Integer.MAX_VALUE;	// added modified
	
	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param thread
	 *            the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);
		
		return (ThreadState) thread.schedulingState;
	}
	
	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param thread
	 *            the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */
	
	protected class LotteryQueue extends ThreadQueue {
		LotteryQueue (boolean transferPriority) {	// added modified
			this.transferPriority = transferPriority;
		}
		
		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			getThreadState(thread).waitForAccess(this, enqueueTimeCounter++);
		}
		
		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			if (!transferPriority)
				return;
			
			getThreadState(thread).acquire(this);
			occupyingThread = thread;
		}
		
		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			//print();
			
			ThreadState nextThread = pickNextThread();
			
			if (occupyingThread != null) {
				getThreadState(occupyingThread).release(this);
				occupyingThread = null;
			}
			
			if (nextThread == null)
				return null;
			
			waitingQueue.remove(nextThread);
			nextThread.ready();
			
			updateDonatingPriority();
			
			acquire(nextThread.getThread());
			
			return nextThread.getThread();
		}
		
		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {
			// added modified
			if (waitingQueue.isEmpty()) return null;
			int cnt = (new Random ()).nextInt (waitingQueue.getSum ());
//			int cnt = Lib.random (waitingQueue.getSum ());
			ThreadState ts;
			for (Iterator <ThreadState> iter = waitingQueue.iterator (); iter.hasNext (); )
				if ((cnt -= (ts = iter.next ()).getEffectivePriority ()) < 0) return ts;
			return null;
		}
		
		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			for (Iterator<ThreadState> iterator = waitingQueue.iterator(); iterator
					.hasNext();) {
				ThreadState state = iterator.next();
				System.out.print(state.getThread());
			}
			System.out.println();
		}
		
		public int getDonatingPriority() {
			return donatingPriority;
		}
		
		public int compareTo(LotteryQueue queue) {	// added modified
			if (donatingPriority > queue.donatingPriority)
				return -1;
			if (donatingPriority < queue.donatingPriority)
				return 1;
			
			if (id < queue.id)
				return -1;
			if (id > queue.id)
				return 1;
			
			return 0;
		}
		
		public void prepareToUpdateEffectivePriority(KThread thread) {
			boolean success = waitingQueue.remove(getThreadState(thread));
			
			Lib.assertTrue(success);
		}
		
		public void updateEffectivePriority(KThread thread) {
			waitingQueue.add(getThreadState(thread));
			
			updateDonatingPriority();
		}
		
		protected void updateDonatingPriority() {
			// added modified
			if (!transferPriority) return;
			int newDonatingPriority = waitingQueue.getSum ();
			
			if (newDonatingPriority == donatingPriority)
				return;
			
			// added
			if (loop_mark == this) {loop_mark = null; return;}	// deadlock
			else if (loop_mark == null) {loop_mark = this; loop_cnt = 0; loop_k = 1;}
			else if (++loop_cnt == loop_k) {loop_mark = this; loop_cnt = 0; loop_k *= 2;}
			
			if (occupyingThread != null)
				getThreadState(occupyingThread)
				.prepareToUpdateDonatingPriority(this);
			
			donatingPriority = newDonatingPriority;
			
			if (occupyingThread != null)
				getThreadState(occupyingThread).updateDonatingPriority(this);
			
			if (loop_mark == this) loop_mark = null;
		}
		
		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority;
		
		/** The threads waiting in this ThreadQueue. */
		protected SumSet <ThreadState> waitingQueue = new SumSet <ThreadState> ();	// added sum
		
		/** The thread occupying this ThreadQueue. */
		protected KThread occupyingThread = null;
		
		protected int donatingPriority = 0;
		
		/**
		 * The number that <tt>waitForAccess</tt> has been called. Used know the
		 * time when each thread enqueue.
		 */
		protected long enqueueTimeCounter = 0;
		
		protected int id = numLotteryQueueCreated++;	// added modified
	}
	protected static int numLotteryQueueCreated = 0;	// added modified
	
	
	protected class ThreadState extends PriorityScheduler.ThreadState {	// Be careful
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param thread
		 *            the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
			super (thread);
			this.thread = thread;
		}
		
		public KThread getThread() {
			return thread;
		}
		
		/**
		 * Return the priority of the associated thread.
		 *
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}
		
		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			return effectivePriority;
		}
		
		/**
		 * Return the time when the associated thread begin to wait.
		 */
		public long getEnqueueTime() {
			return enqueueTime;
		}
		
		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param priority
		 *            the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;
			
			this.priority = priority;
			updateEffectivePriority();
		}
		
		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 *
		 * @param waitQueue
		 *            the queue that the associated thread is now waiting on.
		 *
		 * @param enqueueTime
		 *            the time when the thread begin to wait.
		 *
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(LotteryQueue waitQueue, long enqueueTime) {	// added modified
			this.enqueueTime = enqueueTime;
			
			waitingFor = waitQueue;
			
			waitQueue.updateEffectivePriority(thread);
		}
		
		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(LotteryQueue waitQueue) {	// added modified
			acquires.add(waitQueue);
			
			updateEffectivePriority();
		}
		
		/**
		 * Called when <tt>waitQueue</tt> no longer be acquired by the
		 * associated thread.
		 *
		 * @param waitQueue
		 *            the queue
		 */
		public void release(LotteryQueue waitQueue) {	// added modified
			acquires.remove(waitQueue);
			
			updateEffectivePriority();
		}
		
		public void ready() {
			Lib.assertTrue(waitingFor != null);
			
			waitingFor = null;
		}
		
		public int compareTo(ThreadState state) {
			
			if (effectivePriority > state.effectivePriority)
				return -1;
			if (effectivePriority < state.effectivePriority)
				return 1;
			
			if (enqueueTime < state.enqueueTime)
				return -1;
			if (enqueueTime > state.enqueueTime)
				return 1;
			
			return thread.compareTo(state.thread);
		}
		
		/**
		 * Remove <tt>waitQueue</tt> from <tt>acquires</tt> to prepare to update
		 * <tt>donatingPriority</tt> of <tt>waitQueue</tt>.
		 *
		 * @param waitQueue
		 */
		public void prepareToUpdateDonatingPriority(LotteryQueue waitQueue) {	// added modified
			boolean success = acquires.remove(waitQueue);
			
			Lib.assertTrue(success);
		}
		
		public void updateDonatingPriority(LotteryQueue waitQueue) {	// added modified
			acquires.add(waitQueue);
			
			updateEffectivePriority();
		}
		
		private void updateEffectivePriority() {
			// added modified
			int newEffectivePriority = priority;
			if (!acquires.isEmpty()) newEffectivePriority += acquires.getSum ();	// added modified
			
			if (newEffectivePriority == effectivePriority)
				return;
			
			if (waitingFor != null)
				waitingFor.prepareToUpdateEffectivePriority(thread);
			
			effectivePriority = newEffectivePriority;
			
			if (waitingFor != null)
				waitingFor.updateEffectivePriority(thread);
		}
		
		/** The thread with which this object is associated. */
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority = priorityDefault;
		/** The effective priority of the associated thread. */
		protected int effectivePriority = priorityDefault;
		/** The ThreadQueue that the associated thread waiting for. */
		protected LotteryQueue waitingFor = null;
		/** The TreeMap storing the number of donated priorities. */
		protected SumSet <LotteryQueue> acquires = new SumSet <LotteryQueue> ();	// added modified
		/**
		 * The time when the thread begin to wait. That time is measured by
		 * counting how many times <tt>PriorityQueue.waitForAccess</tt> called
		 * before.
		 */
		protected long enqueueTime;
	}
	
	// added
	private static LotteryQueue loop_mark = null;
	private static int loop_cnt, loop_k;
	
	protected class SumSet <T> extends HashSet <T> {	// added
		private int sum = 0;
		SumSet () {super ();}
		public int getSum () {return sum;}
		public boolean remove (Object o) {
			Lib.assertTrue (Machine.interrupt().disabled());
			if (o instanceof LotteryQueue) sum -= ((LotteryQueue) o).getDonatingPriority ();
			else if (o instanceof ThreadState) sum -= ((ThreadState) o).getEffectivePriority ();
			return super.remove (o);
		}
		public boolean add (T o) {
			Lib.assertTrue (Machine.interrupt().disabled());
			if (o instanceof LotteryQueue) sum += ((LotteryQueue) o).getDonatingPriority ();
			else if (o instanceof ThreadState) sum += ((ThreadState) o).getEffectivePriority ();
			return super.add (o);
		}
	}
	
	// added
	public static void selfTest () {
/*		System.out.println("Test LotteryScheduler:");
		endoderm ();
		mesoderm ();
		ectoderm ();
		methylcyclopropane ();
		System.out.println("*************************************");*/
	}
	
	private static void endoderm () {
		System.out.println ("LotteryScheduler.endoderm() begins.");
		KThread thread0 = new KThread(new LSTest("0"));
		KThread thread1 = new KThread(new LSTest("1"));
		
		thread0.fork ();
		thread1.fork ();
		
		thread0.join ();
		System.out.println ("I'm Scheduler.");
		thread1.join ();
		System.out.println ("LotteryScheduler.endoderm() ends.");
	}
	
	private static void mesoderm () {
		System.out.println ("LotteryScheduler.mesoderm() begins.");
		KThread thread0 = new KThread(new LSTest("0"));
		KThread thread1 = new KThread(new LSTest("1"));
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(thread0, 1000);
		
		Machine.interrupt().restore(intStatus);
		
		thread0.fork ();
		thread1.fork ();
		
		thread0.join ();
		System.out.println ("I'm Scheduler.");
		thread1.join ();
		System.out.println ("LotteryScheduler.mesoderm() ends.");
	}
	
	private static void ectoderm () {
		System.out.println ("LotteryScheduler.ectoderm() begins.");
		KThread thread0 = new KThread(new LSTest("0"));
		KThread thread1 = new KThread(new LSTest("1"));
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(thread0, 3);
		
		Machine.interrupt().restore(intStatus);
		
		thread0.fork ();
		thread1.fork ();
		
		ThreadedKernel.scheduler.increasePriority ();
		thread1.join ();
		System.out.println ("I'm Scheduler.");
		ThreadedKernel.scheduler.decreasePriority ();
		thread0.join ();
		System.out.println ("LotteryScheduler.ectoderm() ends.");
	}
	
	private static void methylcyclopropane () {
		System.out.println("methylcyclopropane () begins.");
		
		LSTest t0 = new LSTest("second"); KThread thread0 = new KThread(t0); thread0.setName ("second");
		LSTest t1 = new LSTest("second'"); KThread thread1 = new KThread(t1); thread1.setName ("second'");
		LSTest t2 = new LSTest("tertiary"); KThread thread2 = new KThread(t2); thread2.setName ("tertiary");
		LSTest t3 = new LSTest("primary"); KThread thread3 = new KThread(t3); thread3.setName ("primary");
		
		t0.setJoiner (thread1); t1.setJoiner (thread2); t2.setJoiner (thread0); t3.setJoiner (thread1);
		
		boolean intStatus = Machine.interrupt().disable();
		
		ThreadedKernel.scheduler.setPriority(thread0, 1000000);
		ThreadedKernel.scheduler.setPriority(thread1, 1000000);
		ThreadedKernel.scheduler.setPriority(thread2, 1000000);
		ThreadedKernel.scheduler.setPriority(thread3, 1000);
		
		Machine.interrupt().restore(intStatus);
		
		System.out.println ("I'm Scheduler.");
		
		thread0.fork ();
		thread1.fork ();
		thread2.fork ();
		thread3.fork ();
		
		for (int i = 0; i < 10; i++) {System.out.println ("Wait"); KThread.yield ();}
		
		System.out.println ("I'm Scheduler.");
		System.out.println ("LotteryScheduler.methylcyclopropane () ends.");
	}
	
	private static class LSTest implements Runnable {
		private KThread joiner = null;
		private String name;
		
		LSTest (String name) {this.name = name;}
		public void setJoiner (KThread joiner) {this.joiner = joiner;}
		
		public void run() {
			boolean intStatus;
			if (joiner != null) {
				intStatus = Machine.interrupt().disable();
				System.out.println ("LSTest " + name + "\tjoining to " + joiner.getName () + "\twith " + ThreadedKernel.scheduler.getEffectivePriority() + " ticket(s)");
				joiner.join ();
				Lib.assertNotReached("isobutane > butane");
				System.out.println ("LSTest " + name + "\tjoining to " + joiner.getName () + "\twith " + ThreadedKernel.scheduler.getEffectivePriority() + " ticket(s)");
				Machine.interrupt().restore(intStatus);
			}
			
			for (int i = 0; i < 20; i++) {
				intStatus = Machine.interrupt().disable();
				System.out.println ("LSTest " + name + "\tbenzene " + i + "\twith " + ThreadedKernel.scheduler.getEffectivePriority () + " ticket(s)");
				Machine.interrupt().restore(intStatus);
				KThread.yield ();
			}
		}
	}
}
