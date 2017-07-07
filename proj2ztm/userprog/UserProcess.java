package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.io.EOFException;
import java.util.*;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see		nachos.vm.VMProcess
 * @see		nachos.network.NetProcess
 */
public class UserProcess {
	// added
	private int status;
	private final static int status4init = -19951019;
	private Map <Integer, UserProcess> mapID2UP;
	private int processID;
	private final static int processID4root = 0;
	private static int ID_next = processID4root;
	private UThread thread;
	private final static int MAXNFILEDESCRIPTOR = 16;
	OpenFile[] filedescriptor_list;
	private static Set <Integer> aliveProcess = new HashSet <Integer> ();
	
	private boolean isRootProcess () {return processID == UserProcess.processID4root;}
	private boolean isLastProcess () {return aliveProcess.isEmpty ();}
	
	private int consumeFileDescriptor (OpenFile file) {
		for (int i = 2; i < MAXNFILEDESCRIPTOR; i++) if (filedescriptor_list[i] == null) {filedescriptor_list[i] = file; return i;}
		return -1;}
	private void releaseFileDescriptor (int id) {if (0 <= id && id < MAXNFILEDESCRIPTOR) filedescriptor_list[id] = null;}
	private OpenFile getOpenFilefromFileDescriptor (int id) {return (0 <= id && id < MAXNFILEDESCRIPTOR)? filedescriptor_list[id]: null;}
	
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		// added removed
/*		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i=0; i<numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i,i, true,false,false,false);*/
		
		// added
		aliveProcess.add (this.processID = ID_next++);
		Lib.debug ('z', "my processID is " + this.processID);
		Lib.debug ('z', "current alive process:\t" + aliveProcess.toString ());
		this.status = status4init;
		mapID2UP = new HashMap <Integer, UserProcess> ();
		filedescriptor_list = new OpenFile [MAXNFILEDESCRIPTOR];
		for (int i = 2; i < MAXNFILEDESCRIPTOR; i++) filedescriptor_list[i] = null;
		filedescriptor_list[0] = UserKernel.console.openForReading();
		filedescriptor_list[1] = UserKernel.console.openForWriting();
	}
	
	/**
	 * Allocate and return a new process of the correct class. The class name
	 * is specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 *
	 * @return		a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 *
	 * @param		name		the name of the file containing the executable.
	 * @param		args		the arguments to pass to the executable.
	 * @return		<tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;
		
		(thread = new UThread(this)).setName(name).fork(); // added

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read
	 * at most <tt>maxLength + 1</tt> bytes from the specified address, search
	 * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
	 * without including the null terminator. If no null terminator is found,
	 * returns <tt>null</tt>.
	 *
	 * @param		vaddr		the starting virtual address of the null-terminated
	 *						string.
	 * @param		maxLength		the maximum number of characters in the string,
	 *								not including the null terminator.
	 * @return		the string read, or <tt>null</tt> if no null terminator was
	 *				found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength+1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length=0; length<bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param		vaddr		the first byte of virtual memory to read.
	 * @param		data		the array where the data will be stored.
	 * @return		the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no
	 * data could be copied).
	 *
	 * @param		vaddr		the first byte of virtual memory to read.
	 * @param		data		the array where the data will be stored.
	 * @param		offset		the first byte to write in the array.
	 * @param		length		the number of bytes to transfer from virtual memory to
	 *						the array.
	 * @return		the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset,
								 int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

		byte[] memory = Machine.processor().getMemory();
		
		// for now, just assume that virtual addresses equal physical addresses
/*		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		int amount = Math.min(length, memory.length-vaddr);
		System.arraycopy(memory, vaddr, data, offset, amount); */
		// added
		if (vaddr < 0) {offset -= vaddr; length += vaddr; vaddr = 0;}
		int ret = 0; TranslationEntry pte;
		for (int len, vpn = vaddr / Processor.pageSize, aoffset; length > 0; vaddr += len, offset += len, length -= len, vpn++) {
			len = Math.min (length, Processor.pageSize - (aoffset = vaddr % Processor.pageSize));
			if (vpn < 0 || vpn >= numPages || !(pte = pageTable[vpn]).valid) continue;
			pte.used = true; ret += len;
			System.arraycopy (memory, pte.ppn * Processor.pageSize + aoffset, data, offset, len);
		}
		return ret;
	}

	/**
	 * Transfer all data from the specified array to this process's virtual
	 * memory.
	 * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param		vaddr		the first byte of virtual memory to write.
	 * @param		data		the array containing the data to transfer.
	 * @return		the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no
	 * data could be copied).
	 *
	 * @param		vaddr		the first byte of virtual memory to write.
	 * @param		data		the array containing the data to transfer.
	 * @param		offset		the first byte to transfer from the array.
	 * @param		length		the number of bytes to transfer from the array to
	 *						virtual memory.
	 * @return		the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset,
								  int length) {
		Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

		byte[] memory = Machine.processor().getMemory();
		
		// for now, just assume that virtual addresses equal physical addresses
/*		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		int amount = Math.min(length, memory.length-vaddr);
		System.arraycopy(data, offset, memory, vaddr, amount); */
		// added
		if (vaddr < 0) {offset -= vaddr; length += vaddr; vaddr = 0;}
		int ret = 0; TranslationEntry pte;
		for (int len, vpn = vaddr / Processor.pageSize, aoffset; length > 0; vaddr += len, offset += len, length -= len, vpn++) {
			len = Math.min (length, Processor.pageSize - (aoffset = vaddr % Processor.pageSize));
			if (vpn < 0 || vpn >= numPages || !(pte = pageTable[vpn]).valid || pte.readOnly) continue;
			pte.used = true; pte.dirty = true; ret += len;
			System.arraycopy (data, offset, memory, pte.ppn * Processor.pageSize + aoffset, len);
		}
		return ret;
	}

	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 *
	 * @param		name		the name of the file containing the executable.
	 * @param		args		the arguments to pass to the executable.
	 * @return		<tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
		
		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}
		
		try {
			coff = new Coff(executable);
		}
		catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}
		
		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s=0; s<coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}
		Lib.debug('z', "numPages = " + numPages);
		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i=0; i<args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}
		
		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();		

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages*pageSize;
		Lib.debug('z', "numPages = " + numPages);

		// and finally reserve 1 page for arguments
		numPages++;
		Lib.debug('z', "numPages = " + numPages);

		if (!loadSections())
			return false;
		
		// store arguments in last page
		int entryOffset = (numPages-1)*pageSize;
		int stringOffset = entryOffset + args.length*4;

		this.argc = args.length;
		this.argv = entryOffset;
		
		for (int i=0; i<argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
					   argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be
	 * run (this is the last step in process initialization that can fail).
	 *
	 * @return		<tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {
		// added modified
		Lib.debug ('z', "loadSections\t" + this.processID + "\t" + numPages);
		
		try {
			
			if (numPages > Machine.processor().getNumPhysPages()) {
				Lib.debug(dbgProcess, "\tinsufficient physical memory");
				throw new Exception ();
			}
			
			// added
			pageTable = new TranslationEntry [numPages];
			for (int i = 0; i < numPages; i++) {
				int ppn = UserKernel.allocatePage ();
				if (ppn == -1) {
					Lib.debug (dbgProcess, "\tinsufficient pages: allocated " + i + ", needs " + numPages);
					throw new Exception ();
				}
				pageTable[i] = new TranslationEntry(i, ppn, true, false, false, false);
			}
			
			// load sections
			for (int s=0; s<coff.getNumSections(); s++) {
				CoffSection section = coff.getSection(s);
				
				Lib.debug(dbgProcess, "\tinitializing " + section.getName()
						  + " section (" + section.getLength() + " pages)");
				
				for (int i=0; i<section.getLength(); i++) {
					int vpn = section.getFirstVPN()+i;
					
					// for now, just assume virtual addresses=physical addresses
					// added
					//				section.loadPage(i, vpn);
					Lib.assertTrue (pageTable[vpn].vpn == vpn);
					int ppn = pageTable[vpn].ppn;
					section.loadPage (i, ppn);
					if (section.isReadOnly ()) pageTable[vpn].readOnly = true;
				}
			}
			String tmp = "";
			for (int i = 0; i < numPages; i++) tmp += pageTable[i].readOnly ? "1": "0";
			Lib.debug ('z', tmp);
		}
		catch (Exception e) {
			unloadSections ();
			coff.close ();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		aliveProcess.remove (this.processID);	// added
		for (int i = 0; i < MAXNFILEDESCRIPTOR; i++) handleClose(i);
		for (int i = 0; i < numPages; i++) if (pageTable[i] != null) {
			Lib.assertTrue (pageTable[i].valid);
			UserKernel.deallocatePage (pageTable[i].ppn);
		}
	}

	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of
	 * the stack, set the A0 and A1 registers to argc and argv, respectively,
	 * and initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i=0; i<processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call. 
	 */
	private int handleHalt () {
		Lib.debug ('z', "handleHalt\t" + processID);
		if (!isRootProcess()) return 0; // added
		
		Machine.halt();
		
		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}
	
	private int handleExit (int status) {
		Lib.debug ('z', "handleExit\t" + processID + "\t" + status);
		unloadSections ();
		this.status = status;
		Lib.debug ('z', "current alive process:\t" + aliveProcess.toString ());
		if (isLastProcess ()) {
			Lib.debug ('z', "handleExit\t" + processID + "\tLast");
			Kernel.kernel.terminate();
		}
		else {
			Lib.debug ('z', "handleExit\t" + processID + "\tnot Last");
			KThread.finish();
			Lib.assertNotReached("Machine.exit() did not exit process!");
		}
		return 0;
	}
	
	private int handleExec (int _file, int argc, int _argv) {
		if (_file < 0 || argc < 0 || _argv < 0) return -1;
		String filename = readVirtualMemoryString(_file, 256);
		Lib.debug ('z', "handleExec\t" + filename + "\t" + (filename!=null? filename.length (): null));
		if (filename == null || !filename.endsWith(".coff")) return -1;
		String argv[] = new String[argc];
		for (int i = 0; i < argc; i++) {
			byte[] tmp = new byte [4];
			int nbyte = readVirtualMemory(_argv + i*4, tmp); if (nbyte != 4) return -1;
			if ((argv[i] = readVirtualMemoryString (Lib.bytesToInt (tmp, 0), 256)) == null) return -1;
			Lib.debug ('z', "handleExec\t" + this.processID + "\targv[" + i + "]:\t" + argv[i]);
		}
		
		UserProcess newUP = UserProcess.newUserProcess ();
		if (newUP.execute (filename, argv)) {mapID2UP.put (newUP.processID, newUP); return newUP.processID;}
		else {aliveProcess.remove (newUP.processID); return -1;}
	}
	
	private int handleJoin (int processID, int _status) {
		Lib.debug ('z', "handleJoin\t" + this.processID + "\t" + processID);
		if (processID < 0 || _status < 0) return -1;
		if (!mapID2UP.containsKey (processID)) return -1;
		UserProcess childUP = mapID2UP.get (processID);
		mapID2UP.remove (processID);
		childUP.thread.join ();
		if (childUP.status != UserProcess.status4init && writeVirtualMemory(_status, Lib.bytesFromInt(childUP.status)) == 4) return 1;
		return 0;
	}
	
	private int handleCreateOpen (int _name, boolean create) {
		Lib.debug ('z', "handleCreateOpen\t" + this.processID + "\t" + create);
		if (_name < 0) return -1;
		String filename = null; boolean refer = false; OpenFile file = null; int fid = -1;
		try {
			if ((filename = readVirtualMemoryString (_name, 256)) == null) throw new Exception ();
			Lib.debug ('z', "handleCreateOpen\t" + this.processID + "\t-" + filename + "-");
			if (!(refer = FileReference.refer (filename))) throw new Exception ();
			Lib.debug ('z', "handleCreateOpen\t" + this.processID + "\t-" + filename + "-");
//			if ((file = UserKernel.fileSystem.open (filename, create)) == null) throw new Exception ();
			if ((file = UserKernel.fileSystem.open (filename, false)) == null &&
				(file = UserKernel.fileSystem.open (filename, create)) == null) throw new Exception ();
			Lib.debug ('z', "handleCreateOpen\t" + this.processID + "\t" + "file opened");
			if ((fid = consumeFileDescriptor (file)) == -1) throw new Exception ();
			Lib.debug ('z', "handleCreateOpen\t" + this.processID + "\tend");
		}
		catch (Exception e) {
			if (file != null) file.close ();
			if (refer) FileReference.derefer (filename);
		}
		return fid;
	}
	
	private int handleRead (int fileDescriptor, int _buffer, int count) {
		Lib.debug ('z', "handleRead\t" + this.processID + "\t" + fileDescriptor + "\t" + count);
		if (_buffer < 0) return -1;
		OpenFile file = getOpenFilefromFileDescriptor (fileDescriptor); if (file == null) return -1;
		byte[] buffer = new byte [count];
		int ret = file.read (buffer, 0, count);
		return writeVirtualMemory (_buffer, buffer, 0, ret) == ret? ret: -1;
	}
																	 
	private int handleWrite (int fileDescriptor, int _buffer, int count) {
		Lib.debug ('z', "handleWrite\t" + this.processID + "\t" + fileDescriptor + "\t" + count);
		OpenFile file = getOpenFilefromFileDescriptor (fileDescriptor); if (file == null) return -1;
		byte[] buffer = new byte [count];
		return (count == readVirtualMemory (_buffer, buffer, 0, count) && count == file.write (buffer, 0, count))? count: -1;
	}
	
	private int handleClose (int fileDescriptor) {
		Lib.debug ('z', "handleClose\t" + this.processID + "\t" + fileDescriptor);
		OpenFile file = getOpenFilefromFileDescriptor (fileDescriptor); if (file == null) return -1;
		String filename = file.getName ();
		Lib.debug ('z', "handleClose\t" + this.processID + "\t" + filename);
		file.close();
		releaseFileDescriptor (fileDescriptor);
		if (fileDescriptor >= 2) FileReference.derefer (filename);
		Lib.debug ('z', "handleClose\t" + this.processID + "\tend");
		return 0;
	}

	private int handleUnlink (int _name) {
		Lib.debug ('z', "handleUnlink\t" + this.processID + "\t" + _name);
		if (_name < 0) return -1;
		String filename = readVirtualMemoryString (_name, 256); if (filename == null) return -1;
		Lib.debug ('z', "handleUnlink\t" + this.processID + "\t" + filename);
		return FileReference.unlink (filename) >= 0? 0: -1;
	}

	private static final int
		syscallHalt = 0,
		syscallExit = 1,
		syscallExec = 2,
		syscallJoin = 3,
		syscallCreate = 4,
		syscallOpen = 5,
		syscallRead = 6,
		syscallWrite = 7,
		syscallClose = 8,
		syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 *
	 * <table>
	 * <tr><td>syscall#</td><td>syscall prototype</td></tr>
	 * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
	 * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
	 * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
	 * 																</tt></td></tr>
	 * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
	 * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
	 * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
	 * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
	 *																</tt></td></tr>
	 * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
	 *																</tt></td></tr>
	 * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
	 * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
	 * </table>
	 * 
	 * @param		syscall		the syscall number.
	 * @param		a0		the first syscall argument.
	 * @param		a1		the second syscall argument.
	 * @param		a2		the third syscall argument.
	 * @param		a3		the fourth syscall argument.
	 * @return		the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
			case syscallHalt: return handleHalt();
			case syscallExit: return handleExit(a0);
			case syscallExec: return handleExec(a0, a1, a2);
			case syscallJoin: return handleJoin(a0, a1);
			case syscallCreate: return handleCreateOpen(a0, true);
			case syscallOpen: return handleCreateOpen(a0, false);
			case syscallRead: return handleRead(a0, a1, a2);
			case syscallWrite: return handleWrite(a0, a1, a2);
			case syscallClose: return handleClose(a0);
			case syscallUnlink: return handleUnlink(a0);
				
				
			default:
				Lib.debug(dbgProcess, "Unknown syscall " + syscall);
				handleExit (-1);
				Lib.assertNotReached("Unknown system call!");
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by
	 * <tt>UserKernel.exceptionHandler()</tt>. The
	 * <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 *
	 * @param		cause		the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionSyscall:
			int result = handleSyscall(processor.readRegister(Processor.regV0),
									   processor.readRegister(Processor.regA0),
									   processor.readRegister(Processor.regA1),
									   processor.readRegister(Processor.regA2),
									   processor.readRegister(Processor.regA3)
									   );
			processor.writeRegister(Processor.regV0, result);
			processor.advancePC();
			break;									   
									   
		default:
			Lib.debug(dbgProcess, "Unexpected exception: " +
					  Processor.exceptionNames[cause]);
//			Lib.debug ('z', "exception id =\t" + cause);
//				for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
//					System.out.println(ste);
//				}
			handleExit(-1);	// added
			Lib.assertNotReached("Unexpected exception");
		}
	}
	
	// added
	private static class FileReference {
		public FileReference () {cnt = 0; dlt = false;}
		public static boolean refer (String filename) {
			boolean intStatus = Machine.interrupt ().disable ();
			FileReference fr = null;
			try {
				fr = fetchFR (filename);
				if (fr.dlt) return false;
				fr.cnt++;
				return true;
			}
			finally {
				Lib.debug ('z', "FR\t" + filename + "\t" + fr.cnt + "\t" + fr.dlt);
				Machine.interrupt ().restore (intStatus);
			}
		}
		public static void derefer (String filename) {
			boolean intStatus = Machine.interrupt ().disable ();
			FileReference fr = null;
			try {
				fr = fetchFR (filename);
				Lib.assertTrue (fr.cnt > 0);
				fr.cnt--;
				tryRemove (filename, fr);
			}
			finally {
				Lib.debug ('z', "FR\t" + filename + "\t" + fr.cnt + "\t" + fr.dlt);
				Machine.interrupt ().restore (intStatus);
			}
		}
		public static int unlink (String filename) {
			boolean intStatus = Machine.interrupt ().disable ();
			FileReference fr = null;
			try {
				fr = fetchFR (filename);
				fr.dlt = true;
				return tryRemove (filename, fr);
			}
			finally {
				Lib.debug ('z', "FR\t" + filename + "\t" + fr.cnt + "\t" + fr.dlt);
				Machine.interrupt ().restore (intStatus);
			}
		}
		private static FileReference fetchFR (String filename) {
			Lib.assertTrue (Machine.interrupt ().disabled () && filename != null);
			FileReference fr = str2fr.get (filename);
			if (fr == null) str2fr.put (filename, fr = new FileReference ());
			return fr;
		}
		private static int tryRemove (String filename, FileReference fr) {
			// -1 for err, 0 for not, 1 for deleted
			Lib.assertTrue (Machine.interrupt ().disabled () && fr != null);
			if (fr.cnt > 0) return 0; str2fr.remove (filename);
			if (fr.dlt) return UserKernel.fileSystem.remove (filename)? 1: -1;
			return 0;
		}
		
		int cnt;
		boolean dlt;
		
		private static Map <String, FileReference> str2fr = new HashMap <String, FileReference> ();
	}
	
	public static void selfTest () {
		selfTest_Ala ();
		selfTest_Cys ();
		
		ID_next = processID4root;
		aliveProcess.clear ();
	}
	
	public void printFileDescriptor () {for (int i = 0; i < MAXNFILEDESCRIPTOR; i++) System.out.println (i + "\t" + getOpenFilefromFileDescriptor (i));}
	
	public static void selfTest_Ala () {
		UserProcess up = new UserProcess ();
		int fid;
		
		up.printFileDescriptor ();
		up.releaseFileDescriptor (0);
		up.printFileDescriptor ();
		up.releaseFileDescriptor (0);
		up.printFileDescriptor ();
		System.out.println (fid = up.consumeFileDescriptor (new OpenFile ()));
		up.printFileDescriptor ();
		up.releaseFileDescriptor (1);
		up.printFileDescriptor ();
		up.releaseFileDescriptor (fid);
		up.printFileDescriptor ();
		System.out.println (fid = up.consumeFileDescriptor (new OpenFile ()));
		up.printFileDescriptor ();
		up.releaseFileDescriptor (fid);
		up.printFileDescriptor ();
		
		up.unloadSections ();
	}
	
	public String printPageStatus () {
		String ret = "";
		for (int i = 0; i < numPages; i++) ret += pageTable[i].readOnly? "1": "0";
		for (int i = 0; i < numPages; i++) ret += pageTable[i].used? "1": "0";
		for (int i = 0; i < numPages; i++) ret += pageTable[i].dirty? "1": "0";
		return ret;
	}
	
	public void resetPageStatus () {for (int i = 0; i < numPages; i++) {pageTable[i].used = pageTable[i].dirty = false;}}
	
	public static void selfTest_Cys () {
		System.out.println ("UserProcess Cys begin");
		
		UserProcess up = new UserProcess ();
		int ps = Processor.pageSize, np = up.numPages = 5; int len;
		
		byte[] buffer0 = new byte [np * ps]; for (int i = 0; i < buffer0.length; i++) buffer0[i] = 0;
		byte[] buffera = new byte [np * ps]; for (int i = 0; i < buffera.length; i++) buffera[i] = 'a';
		byte[] buffer = new byte [np * ps];
		up.pageTable = new TranslationEntry [np];
		for (int i = 0, ppn; i < np; i++) {
			Lib.assertTrue ((ppn = UserKernel.allocatePage ()) != -1);
			up.pageTable[i] = new TranslationEntry(i, ppn, true, false, false, false);
		}
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "00000" + "00000"));
		
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "11111" + "11111"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps/2, buffera, 0, len = ps+ps/2+1) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "11100" + "11100"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps, buffera, 0, len = ps) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01000" + "01000"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps*4-1, buffera, ps*3+10, len = 2) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "00011" + "00011"));
		up.resetPageStatus ();
		
		up.pageTable[1].readOnly = up.pageTable[3].readOnly = true;
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "00000" + "00000"));
		Lib.assertTrue (up.readVirtualMemory (0, buffer, 0, len = np*ps) == len &&
						buffer[ps/2-1] == 0 &&
						buffer[ps/2] == 'a' &&
						buffer[ps*2-1] == 'a' &&
						buffer[ps*2] == 'a' &&
						buffer[ps*2+1] == 0 &&
						buffer[ps*4-2] == 0 &&
						buffer[ps*4-1] == 'a' &&
						buffer[ps*4] == 'a' &&
						buffer[ps*4+1] == 0);
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "11111" + "00000"));
		up.resetPageStatus ();
		
		
		up.pageTable[1].readOnly = up.pageTable[3].readOnly = false;
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (-3, buffera, ps*3+10, len = 5) == 2);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "10000" + "10000"));
		up.resetPageStatus ();
		
		for (int i = 0; i < np*ps; i++) buffer[i] = -1; len = 0;
		Lib.assertTrue (up.readVirtualMemory (-3, buffer, 0, 6) == 3 &&
						buffer[len++] == -1 &&
						buffer[len++] == -1 &&
						buffer[len++] == -1 &&
						buffer[len++] == 'a' &&
						buffer[len++] == 'a' &&
						buffer[len++] == 0);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "10000" + "00000"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (-1, buffera, ps+10, len = ps+2) == ps+1);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "11000" + "11000"));
		up.resetPageStatus ();
		
		for (int i = 0; i < np*ps; i++) buffer[i] = -1;
		Lib.assertTrue (up.readVirtualMemory (-1, buffer, 0, ps+3) == ps+2 &&
						buffer[0] == -1 &&
						buffer[1] == 'a' &&
						buffer[ps] == 'a' &&
						buffer[ps+1] == 'a' &&
						buffer[ps+2] == 0);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "11000" + "00000"));
		up.resetPageStatus ();
		
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		up.pageTable[1].readOnly = up.pageTable[3].readOnly = true;
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps+ps/2, buffera, 0, 2*ps) == ps);
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "00100" + "00100"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.readVirtualMemory (0, buffer, 0, len = np*ps) == len &&
						buffer[ps/2] == 0 &&
						buffer[ps-1] == 0 &&
						buffer[ps] == 0 &&
						buffer[ps*2-1] == 0 &&
						buffer[ps*2] == 'a' &&
						buffer[ps*3-1] == 'a' &&
						buffer[ps*3] == 0 &&
						buffer[ps*4-1] == 0 &&
						buffer[ps*4] == 0 &&
						buffer[ps*5-1] == 0);
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "11111" + "00000"));
		up.resetPageStatus ();
		
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == 3*ps);
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffera, 0, np*ps) == ps*3);
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "10101" + "10101"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.readVirtualMemory (0, buffer, 0, len = np*ps) == len &&
						buffer[ps/2] == 'a' &&
						buffer[ps-1] == 'a' &&
						buffer[ps] == 0 &&
						buffer[ps*2-1] == 0 &&
						buffer[ps*2] == 'a' &&
						buffer[ps*3-1] == 'a' &&
						buffer[ps*3] == 0 &&
						buffer[ps*4-1] == 0 &&
						buffer[ps*4] == 'a' &&
						buffer[ps*5-1] == 'a');
		Lib.assertTrue (up.printPageStatus ().equals ("01010" + "11111" + "00000"));
		up.resetPageStatus ();
		
		
		up.pageTable[1].readOnly = up.pageTable[3].readOnly = false;
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps*2-2, buffera, 0, len = 1) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01000" + "01000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 1).equals ("a"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01000" + "00000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 2).equals ("a"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 3).equals ("a"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.writeVirtualMemory (ps*2-2, buffera, 0, len = 2) == len);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01000" + "01000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 1) == null);
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01000" + "00000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 2).equals ("aa"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-2, 3).equals ("aa"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-1, 1).equals ("a"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		Lib.assertTrue (up.readVirtualMemoryString (ps*2-1, 2).equals ("a"));
		Lib.assertTrue (up.printPageStatus ().equals ("00000" + "01100" + "00000"));
		up.resetPageStatus ();
		
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		
		Lib.assertTrue (up.writeVirtualMemory (np*ps-2, buffera, 0, len = 1) == len);
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-3, 0).equals (""));
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-3, 1).equals (""));
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-3, 2).equals (""));
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-2, 1).equals ("a"));
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-2, 2).equals ("a"));
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-1, 1).equals (""));
		
		Lib.assertTrue (up.writeVirtualMemory (np*ps-1, buffera, 0, 2) == 1);
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-2, 1) == null);
		Lib.assertTrue (up.readVirtualMemoryString (np*ps-2, 2) == null);
		
		
		Lib.assertTrue (up.writeVirtualMemory (0, buffer0, 0, len = np*ps) == len);
		up.resetPageStatus ();
		
		up.unloadSections ();
		System.out.println ("UserProcess Cys end");
	}
	
	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;
	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;
	
	private int initialPC, initialSP;
	private int argc, argv;
		
	private static final int pageSize = Processor.pageSize;
	private static final char dbgProcess = 'a';
}
