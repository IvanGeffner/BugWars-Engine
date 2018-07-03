package bugwars;

public class BytecodeManager {

	/*this might not work for all games tho*/
	static int INF = GameConstants.MAX_BYTECODES*GameConstants.MAX_TURNS;
	
	public static void incBytecodes(int inc) {
		try {
			if (inc <= 0) return;
			if (inc > INF) inc = INF;
			ThreadManager.incBytecodes(inc);
		} catch (Throwable e){
			throw e;
		}
	}
}
