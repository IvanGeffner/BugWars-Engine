package bugwars;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

class Main {
	private static String packageName1, packageName2, mapName;
	private static boolean printWarnings;

	public static void main(String[] args) throws InterruptedException {
		// Parameters
		packageName1 = args[0];
		packageName2 = args[1];
		mapName = args[2];
		printWarnings = (args[3].equals("1"));
		// Run game
		runGame();
	}

	private static void runGame() {
		String logName = generateLogName();
		String tmpLogName = generateTmpLogName();
		Game g = Game.getInstance(packageName1, packageName2, mapName, tmpLogName, true, true, printWarnings);
		g.start();
		try {
			g.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
		renameFolder(tmpLogName, logName);
	}

	private static void renameFolder(String current_name, String new_name) {
		File dir = new File(current_name);
		if (!dir.isDirectory()) {
			System.err.println("Temporal game file not found");
		} else {
		  File newDir = new File(new_name);
		  dir.renameTo(newDir);
		}
	}

	private static String generateLogName() {
		String root = System.getProperty("user.dir");
		// File name
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String name = "" + dateFormat.format(new Date()) + "-" +
				packageName1.replace('.', '_') + "-" +
				packageName2.replace('.', '_');
		String file = root + File.separator + "games" + File.separator + name;
		int k = 0;
		while ((new File(file)).exists()) {
			k++;
			file = root + File.separator + "games" + File.separator + name + "_" + k;
		}
		return file.substring(0, file.length());
	}

	private static String generateTmpLogName() {
		String root = System.getProperty("user.dir");
		// File name
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String name = "tmp_" + dateFormat.format(new Date()) + "-" +
				packageName1.replace('.', '_') + "-" +
				packageName2.replace('.', '_');
		String file = root + File.separator + "games" + File.separator + name;
		int k = 0;
		while ((new File(file)).exists()) {
			k++;
			file = root + File.separator + "games" + File.separator + name + "_" + k;
		}
		return file.substring(0, file.length());
	}
}
