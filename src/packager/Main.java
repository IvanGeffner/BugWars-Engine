package packager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

	private static final String fs = File.separator;
	private static Scanner reader = null;

	public static void main(String[] args) {
		// Parameters
		String package = null;
		if (args.length == 2) {
			package = args[0];
		} else {
			reader = new Scanner(System.in);
			package = inputToken("Enter package name: ");
			reader.close();
		}

		// Upload package
		try {
			doPackage(package);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String doPackage(String package) throws IOException {
		System.out.println("Packaging " + package);

		String root = System.getProperty("user.dir") + fs;
		String destFolder = root + "packages" + fs;
		(new File(destFolder)).mkdir();

		// Get folder to zip
		String folder = root + "src" + fs + packageName.replace('.', File.separatorChar);

		// Get destination zip file name
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String name = packageName.replace('.', '_') + "_" + dateFormat.format(new Date());
		String destZipFile = destFolder + name + ".zip";
		int k = 0;
		while ((new File(destZipFile)).exists()) {
			k++;
			destZipFile = destFolder + name + "_" + k + ".zip";
		}

		// Zip folder
		zipFolder(folder, destZipFile);

		System.out.println("Packaged as " + destZipFile);

		return destZipFile;
	}

	private static void zipFolder(String folder, String destZipFile) throws IOException {
		ArrayList<String> fileList = new ArrayList<String>();
		generateFileList(new File(folder), fileList);
		zipList(folder, fileList, destZipFile);
	}

	private static void zipList(String folder, ArrayList<String> fileList, String destZipFile) throws IOException {
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

	private static void generateFileList(File node, ArrayList<String> fileList) {
		if (node.isFile()) {
			// If java file, add it to list
			String name = node.getAbsoluteFile().toString();
			if (name.length() > 5 && name.substring(name.length() - 5).equals(".java")) {
				fileList.add(name);
			}
		} else if (node.isDirectory()) {
			// If directory, recursive call
			for (String filename : node.list()) {
				generateFileList(new File(node, filename), fileList);
			}
		}
	}

	private static String inputToken(String msg) {
		System.out.print(msg);
		return reader.next(".*");
	}

}
