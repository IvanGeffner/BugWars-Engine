package bugwars;

import java.util.HashMap;
import java.util.Map;

class ThreadManager {

	private static UnitManager unitManager;
	private static Thread runningSlave = null;
	private static ThreadManager runningSlaveInstance = null;

	private int currentBytecode = 0;
	private boolean hasStarted = false;
	
	private Thread slave;
	private boolean pausedMaster = true;
	private boolean pausedSlave = true;
	
	// MultiSingleton pattern
	private static Map<Thread, ThreadManager> instances = new HashMap<Thread, ThreadManager>();

	static ThreadManager getRunningInstance() {
		if (runningSlaveInstance != null) return runningSlaveInstance;
		runningSlaveInstance = getInstance(runningSlave);
		return runningSlaveInstance;
	}
	
	static ThreadManager getInstance(Thread slave) {
		if (!instances.containsKey(slave)) {
			instances.put(slave, new ThreadManager(slave));
		}
		return instances.get(slave);
	}
	
	private ThreadManager(Thread _slave) {
		unitManager = Game.getInstance().unitManager;
		slave = _slave;
	}
	// End MultiSingleton pattern
	
	static void incBytecodes(int inc) {
		getRunningInstance();
		if (unitManager.currentUnitKilled()) {
			eraseThread();
			throw new DeathException();
		}
		runningSlaveInstance.addBytecode(inc);
	}

	static void eraseThread(){
		ThreadManager.instances.remove(unitManager.getCurrentUnit());
		unitManager.removeCurrentUnit();
		resumeMaster(false);
	}

	void addBytecode(int inc){
		currentBytecode += inc;
		if (currentBytecode > BytecodeManager.INF) currentBytecode = BytecodeManager.INF;
		if (!hasStarted) return;
		if (unitManager.currentUnitKilled() || currentBytecode > GameConstants.MAX_BYTECODES){
			resumeMaster();
		}
	}

	void addBytecodeWithoutStop(int inc){
		currentBytecode += inc;
		if (currentBytecode > BytecodeManager.INF) currentBytecode = BytecodeManager.INF;
	}

	static void incBytecodesWithoutStop(int inc){
		getRunningInstance();
		runningSlaveInstance.addBytecodeWithoutStop(inc);
	}

	void resetBytecode(){
		if (hasStarted) currentBytecode = Math.max(0, currentBytecode - GameConstants.MAX_BYTECODES);
	}
	
	static void resumeMaster(boolean wait) {
		ThreadManager tm = runningSlaveInstance;
		if (wait) {
			unitManager.getCurrentUnit().bytecodesUsed = getRunningInstance().getCurrentBytecode();
		}
		
		synchronized (tm) {
			tm.setRunningSlave(null);
			tm.notifyAll();
			tm.resetBytecode();
			
			while (wait && tm.pausedSlave) {
				try {
					tm.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static void resumeMaster() {
		resumeMaster(true);
	}


	void resumeSlave() {
		// Only one slave running at a time
		if (runningSlave != null){
			if (Game.printWarnings) System.err.println("A slave is already running!!!");
			return;
		}
		
		synchronized (this) {
			setRunningSlave(slave);
			
			if (!hasStarted) {
				// Start the threads the first turn they are called
				slave.start();
				hasStarted = true;
			}
			else {
				// Else we resume threads
				this.notifyAll();
			}
			
			while (pausedMaster){
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void setRunningSlave(Thread slave) {
		runningSlave = slave;
		if (slave == null) {
			pausedMaster = false;
			pausedSlave = true;
			runningSlaveInstance = null;
		} else {
			pausedMaster = true;
			pausedSlave = false;
			runningSlaveInstance = instances.get(runningSlave);
		}
	}

	static void punish() {
		incBytecodesWithoutStop(GameConstants.EXCEPTION_BYTECODE_PENALTY);
	}

	int getCurrentBytecode() {
		return currentBytecode;
	}
}
