package bugwars;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

class Main {
	private static String packageName1, packageName2, mapName;
	private static boolean printWarnings, challengeRun;

	public static void main(String[] args) throws InterruptedException {
		// Parameters
		packageName1 = args[0];
		packageName2 = args[1];
		mapName = args[2];
		printWarnings = (args[3].equals("1"));
		challengeRun = (args[4].equals("1"));
		// Run game
		runGame();
	}

	private static void runGame() {
		if (!challengeRun) {
			String tmpLogName = generateTmpLogName();
			String logName = generateLogName();
			Game g = Game.getInstance(packageName1, packageName2, mapName, tmpLogName, true, true, printWarnings, challengeRun);
			g.start();
			try {
				g.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
			renameFolder(tmpLogName, logName);
		} else {
			String root = System.getProperty("user.dir");
			String file_path = root + File.separator + "maps";
			File folder = new File(file_path);

			ArrayList<String> maps = new ArrayList<>();
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String filename = listOfFiles[i].getName();
					String[] parts = filename.split("\\.");
					maps.add(parts[0]);
				}
			}

			int wins1 = 0, wins2 = 0;
			for (int i = 0; i < maps.size(); ++i){
				String map = maps.get(i);
				Game g = Game.getNewInstance(packageName1, packageName2, map, null, true, true, printWarnings, challengeRun);
				g.start();
				try {
					g.join();
					String winner = g.world.getWinner().packageName;
					System.out.println(packageName1 + " vs " + packageName2 + " at " + map + ". Winner: " + winner);
					if (winner.equals(packageName1)) ++wins1;
					else ++wins2;
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
				g = Game.getNewInstance(packageName2, packageName1, map, null, true, true, printWarnings, challengeRun);
				g.start();
				try {
					g.join();
					String winner = g.world.getWinner().packageName;
					System.out.println(packageName2 + " vs " + packageName1 + " at " + map + ". Winner: " + winner);
					if (winner.equals(packageName1)) ++wins1;
					else ++wins2;
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			System.out.println("-----------------------------------------------------------");
			System.out.println("Total victories " + packageName1 + ": " + wins1);
			System.out.println("Total victories " + packageName2 + ": " + wins2);
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

	private static void renameFolder(String current_name, String new_name) {
		File dir = new File(current_name);
		if (!dir.isDirectory()) {
			System.err.println("Temporal game file not found");
		} else {
		  File newDir = new File(new_name);
		  dir.renameTo(newDir);
		}
	}
}
