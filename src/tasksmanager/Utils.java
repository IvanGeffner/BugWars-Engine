package tasksmanager;

import java.io.*;
import java.util.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {
	static final String fs = File.separator;
	static final String root = System.getProperty("user.dir") + fs;
	static final String zipLocation = root + ".." + fs + "players" + fs;
	static final String srcLocation = root + "src" + fs;
	static final String logsLocation = root + "logs" + fs;
	static final String compilationlogsLocation = logsLocation + "compile" + fs;
	static final String buildLocation = root + "build" + fs + "classes" + fs;
	static final String jarLocation = root + "users_jars" + fs;
	static final String gamesLocation = root + ".." + fs + "games" + fs;

	static String prepareScrimmage(String user, String tournament) throws ZipException, TaskException {
		String packageName = loadInstrumentedPackage(user, tournament);
		return packageName;
	}

	static void cleanScrimmage(String packageName) {
		deleteFolder(jarLocation + packageName + ".jar");
	}

	static int countMatches(String str, char ch) {
		int count = 0;
		for (int i = 0; i < str.length(); i++) if (str.charAt(i) == ch) count++;
		return count;
	}

	static String checkZipStructure(ZipFile zipFile) throws ZipException, TaskException {
		List fileHeaderList = zipFile.getFileHeaders();
		int nRootDirs = 0;
		int nRootFiles = 0;
		String packageName = null;
		for (int i = 0; i < fileHeaderList.size(); i++) {
			FileHeader fileHeader = (FileHeader)fileHeaderList.get(i);
			System.out.println(fileHeader.getFileName());
			if (fileHeader.isDirectory()) {
				String fileName = fileHeader.getFileName();
				if (countMatches(fileName, '/') == 1) {
					String aux = fileName.substring(0, fileName.length() - 1);
					if (packageName == null) {
						packageName = aux;
						nRootDirs++;
					} else if (!packageName.equals(aux)) {
						nRootDirs++;
					}
				}
			} else {
				int n_slashes = countMatches(fileHeader.getFileName(), '/');
				if (n_slashes == 0) nRootFiles++;
				else if (n_slashes == 1) {
					String fileName = fileHeader.getFileName();
					String aux = fileName.split("/")[0];
					if (packageName == null) {
						packageName = aux;
						nRootDirs++;
					} else if (!packageName.equals(aux)) {
						nRootDirs++;
					}
				}
			}
		}
		if (nRootFiles > 0) throw new TaskException("Wrong zip structure", "Files found in base directory");
		if (nRootDirs == 0) throw new TaskException("Wrong zip structure", "No directory found, one expected");
		if (nRootDirs > 1) throw new TaskException("Wrong zip structure", "Many directories found, one expected");
		return packageName;
	}

	static String prepareValidation(String user, String tournament) throws ZipException, TaskException {
		String zipFilePath = zipLocation + user + fs + tournament + ".zip";
		String playerSrcPath = srcLocation;

		// Unzip code into src
		Utils.deleteFolder(playerSrcPath);
		ZipFile zipFile = new ZipFile(zipFilePath);

		// Check structure and get package name
		String packageName = checkZipStructure(zipFile);

    zipFile.extractAll(playerSrcPath);
		return packageName;
	}

	static void cleanValidation(String user, String packageName) {
		deleteFolder(srcLocation + packageName);
		deleteFolder(buildLocation + packageName);
		String jarName = jarLocation + user.toLowerCase() + "." + packageName + ".jar";
		(new File(jarName)).delete();
	}

	static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File f: files) {
				if (f.isDirectory()) {
					deleteFolder(f);
				} else {
					f.delete();
				}
			}
		}
		folder.delete();
	}

	static void deleteFolder(String folder) {
		deleteFolder(new File(folder));
	}

	// Store instrumented package to the players folder
	static void storeInstrumentedPackage(String user, String tournament, String packageName) throws TaskException {
		String zipLocationDir = zipLocation + user + fs + tournament + fs;
		String jarName = user.toLowerCase() + "." + packageName + ".jar";
		deleteFolder(zipLocationDir);
		System.out.println(jarLocation + jarName);
		System.out.println(zipLocationDir);
		if (!Utils.copyFileToDirectory(jarLocation + jarName, zipLocationDir)) {
			String shortMsg = "Internal error";
			String logMsg = "Task failed to store instrumented code. Please report to administrator";
			throw new TaskException(shortMsg, logMsg);
		}
	}

	// Load instrumented package to build folder
	static String loadInstrumentedPackage(String user, String tournament) throws TaskException {
		String zipLocationDir = zipLocation + user + fs + tournament + fs;
		File f = new File(zipLocationDir);
		ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
		if (names.size() == 0) {
			String shortMsg = "Internal error";
			String logMsg = "Task failed to load instrumented code. Instrumented code not found. Please report to administrator";
			throw new TaskException(shortMsg, logMsg);
		}
		if (names.size() > 1) {
			String shortMsg = "Internal error";
			String logMsg = "Task failed to load instrumented code. Many instumented codes found. Please report to administrator";
			throw new TaskException(shortMsg, logMsg);
		}
		String fileName = names.get(0);
		String packageName = fileName.substring(0, fileName.length() - 4);
		if (!Utils.copyFileToDirectory(zipLocationDir + packageName + ".jar", jarLocation)) {
			String shortMsg = "Internal error";
			String logMsg = "Task failed to load instrumented code. Please report to administrator";
			throw new TaskException(shortMsg, logMsg);
		}
		return packageName;
	}

	static boolean copyDir(File srcDir, File destDir) {
		try {
			FileUtils.copyDirectory(srcDir, destDir);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean copyDir(String srcDir, String destDir) {
		return Utils.copyDir(new File(srcDir), new File(destDir));
	}

	static boolean copyFileToDirectory(File srcFile, File destDir) {
		try {
			FileUtils.copyFileToDirectory(srcFile, destDir);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	static boolean copyFileToDirectory(String srcFile, String destDir) {
		return Utils.copyFileToDirectory(new File(srcFile), new File(destDir));
	}

	static String readStream(InputStream is) {
		BufferedReader br = null;
        StringBuilder builder = new StringBuilder();

		try {
			br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line + System.lineSeparator());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return builder.toString();
	}

	static String readFile(String filePath) {
		File file = new File(filePath);
		String s = null;
		try {
			s = readStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return s;
	}

	static String readCompilationLog(String packageName) {
		return Utils.readFile(compilationlogsLocation + packageName);
	}

	static String readGameWinner(String gameId) throws IOException {
		String fileName = gamesLocation + gameId + fs + "game_info.txt";

		// read the file into lines
		List<String> lines = new ArrayList<String>();
		BufferedReader r = new BufferedReader(new FileReader(fileName));
		String in;
		while ((in = r.readLine()) != null) lines.add(in);
		r.close();

		// Line 3 from the botton contains 1 or 2 describing the winner
		return lines.get(lines.size() - 3);
	}

	// Zip game log
	static void zipGameLog(String gameId) throws IOException {
		try {
			zipFolder(gamesLocation + gameId, gamesLocation + gameId + ".zip");
		} catch (IOException e) {
			System.err.println("Error: Could not zip the gamelog folder");
		}
		File index = new File(gamesLocation + gameId);
		String[] entries = index.list();
		for (String s: entries) {
			File currentFile = new File(index.getPath(), s);
			currentFile.delete();
		}
		index.delete();
	}

	static void zipFolder(String folder, String destZipFile) throws IOException {
		ArrayList<String> fileList = new ArrayList<String>();
		generateFileList(new File(folder), fileList);
		zipList(folder, fileList, destZipFile);
	}

	static void zipList(String folder, ArrayList<String> fileList, String destZipFile) throws IOException {
		byte[] buffer = new byte[1024];

		FileOutputStream fos = new FileOutputStream(destZipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);

		for (String file : fileList) {
			String relPathFile = file.substring(folder.length() + 1, file.length());
			ZipEntry ze = new ZipEntry(relPathFile);
			zos.putNextEntry(ze);

			FileInputStream in = new FileInputStream(file);
			int len;
			while ((len = in.read(buffer)) > 0) {
				zos.write(buffer, 0, len);
			}
			in.close();

			System.out.println("File Added : " + relPathFile);
		}

		zos.closeEntry();
		zos.close();
	}

	static void generateFileList(File node, ArrayList<String> fileList) {
		if (node.isFile()) {
			// If java file, add it to list
			String name = node.getAbsoluteFile().toString();
			fileList.add(name);
		} else if (node.isDirectory()) {
			// If directory, recursive call
			for (String filename : node.list()) {
				generateFileList(new File(node, filename), fileList);
			}
		}
	}
}
