package tasksmanager;

import java.io.IOException;

import net.lingala.zip4j.exception.ZipException;

class Player {
	Integer id;
	String user;
	Integer userId;
	String tournament;
	Integer tournamentId;

	String packageName = null;

	Player(Integer _id, Integer _userId, String _user, Integer _tournamentId,
	 				String _tournament) {
		id = _id;
		userId = _userId;
		user = _user;
		tournamentId = _tournamentId;
		tournament = _tournament;
	}

	static Player getPlayer(Integer id) {
		return DbController.getPlayer(id);
	}

	void validate() throws IOException, InterruptedException, ZipException, TaskException {
		// Prepare validation
		packageName = Utils.prepareValidation(user, tournament);

		// Run the validation
		Runtime rt = Runtime.getRuntime();
		String command = "ant -buildfile build_" + tournament.toLowerCase() + ".xml" +
				" -Duser=" + user.toLowerCase() +
				" -Dpackage=" + packageName +
				" package";
		System.out.println(command);
		Process pr = rt.exec(command);
		String prOut = Utils.readStream(pr.getInputStream());
		String prErr = Utils.readStream(pr.getErrorStream());
		pr.waitFor();
		System.out.print(prOut);
		System.out.print(prErr);

		// Check success
		if (pr.exitValue() != 0) {
			String shortMsg = "Compile or instrument step failed";
			String logMsg = Utils.readCompilationLog(packageName);
			throw new TaskException(shortMsg, logMsg);
		}
		Utils.storeInstrumentedPackage(user, tournament, packageName);
		DbController.insertElo(tournamentId, userId);
	}

	void updateStatus(String status) {
		DbController.updatePlayerStatus(id, status);
	}

	void updateLog(String shortLog, String longLog) {
		DbController.updatePlayerLog(id, shortLog, longLog);
	}

	void cleanValidation() {
		if (packageName != null) Utils.cleanValidation(user, packageName);
	}
}
