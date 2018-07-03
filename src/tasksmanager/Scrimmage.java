package tasksmanager;

import java.io.IOException;
import java.util.List;

import net.lingala.zip4j.exception.ZipException;

class Scrimmage {
	Integer id;
	Integer userId1, userId2;
	String user1, user2;
	String tournament1, tournament2;
	Integer tournamentId1, tournamentId2;
	Integer elo1, elo2;
	Integer wins1, wins2;
	List<Game> games;

	String packageName1 = null, packageName2 = null;

	Scrimmage(Integer _id,
			Integer _userId1, String _user1,
			Integer _tournamentId1, String _tournament1, Integer _elo1, Integer _wins1,
			Integer _userId2, String _user2,
			Integer _tournamentId2, String _tournament2, Integer _elo2, Integer _wins2,
			List<Game> _games) {
		id = _id;
		userId1 = _userId1;
		user1 = _user1;
		tournamentId1 = _tournamentId1;
		tournament1 = _tournament1;
		elo1 = _elo1;
		wins1 = _wins1;
		userId2 = _userId2;
		user2 = _user2;
		tournamentId2 = _tournamentId2;
		tournament2 = _tournament2;
		elo2 = _elo2;
		wins2 = _wins2;
		games = _games;
	}

	static Scrimmage getScrimmage(Integer id, String taskType) {
		if (taskType.equals("Tournament")) return DbController.getRankedScrimmage(id);
		return DbController.getScrimmage(id);
	}

	void execute() throws IOException, InterruptedException, ZipException, TaskException {
		// Check same tournament
		if (!tournament1.equals(tournament2)) {
			String shortMsg = "Internal error";
			String logMsg = "Scrimmage players belong to different tournaments. Please report to administrator";
			throw new TaskException(shortMsg, logMsg);
		}

		// Prepare packages
		packageName1 = Utils.prepareScrimmage(user1, tournament1);
		packageName2 = Utils.prepareScrimmage(user2, tournament2);

		// Run all games
		for (int i = 0; i < games.size(); ++i) {
			String gameId = games.get(i).id.toString();
			Runtime rt = Runtime.getRuntime();
			String command = "ant -buildfile build_" + tournament1.toLowerCase() + ".xml" +
					" -Dpackage1=" + packageName1 +
					" -Dpackage2=" + packageName2 +
					" -Dmap=" + games.get(i).map +
					" -DgameId=" + gameId +
					" runServer_no_prints";
			System.out.println(command);
			Process pr = rt.exec(command);
			//String prOut = Utils.readStream(pr.getInputStream());
			//String prErr = Utils.readStream(pr.getErrorStream());
			pr.waitFor();
			//System.out.print(prOut);
			//System.out.print(prErr);

			// Check success
			if (pr.exitValue() != 0) {
				String shortMsg = "Internal error";
				String logMsg = "Run step failed. Please report to administrator";
				throw new TaskException(shortMsg, logMsg);
			}

			// Update scrimmage wins and game
			Integer winnerId = null;
			try {
				winnerId = Integer.parseInt(Utils.readGameWinner(gameId));
			} catch (Exception e) {
				String shortMsg = "Internal error";
				String logMsg = "Failed to read game winner. Please report to administrator";
				throw new TaskException(shortMsg, logMsg);
			}
			if (winnerId == 1) {
				winnerId = DbController.getPlayer(user1, tournament1).id;
				wins1++;
			} else {
				winnerId = DbController.getPlayer(user2, tournament2).id;
				wins2++;
			}
			DbController.updateGameWinner(games.get(i).id, winnerId);

			// Zip game log
			Utils.zipGameLog(gameId);
		}

		// Update wins
		DbController.updateScrimmageWins(id, wins1, wins2);
	}

	void updateStatus(String status) {
		DbController.updateScrimmageStatus(id, status);
	}

	void updateElos() {
    // Update ELOs
    float K = 32f;
		// Ids
		Integer tournamentId = this.tournamentId1;
		Integer userId1 = this.userId1;
		Integer userId2 = this.userId2;
    Float elo1 = (float)this.elo1;
		Float elo2 = (float)this.elo2;
		// Compute elo increment
    float r1 = (float)Math.pow(10f, elo1 / 400f);
    float r2 = (float)Math.pow(10f, elo2 / 400f);
    float e1 = r1 / (r1 + r2);
    float e2 = r2 / (r1 + r2);
    float s1 = (this.wins1 > this.wins2 ? 1f : 0f);
    float s2 = 1 - s1;
    int newElo1 = Math.round(elo1) + Math.round(K * (s1 - e1));
    int newElo2 = Math.round(elo2) + Math.round(K * (s2 - e2));
		// Update
    DbController.updateElo(tournamentId, userId1, newElo1);
    DbController.updateElo(tournamentId, userId2, newElo2);
	}

	void updateLog(String shortLog, String longLog) {
		DbController.updateScrimmageLog(id, shortLog, longLog);
	}

	void cleanScrimmage() {
		if (packageName1 != null) Utils.cleanScrimmage(packageName1);
		if (packageName2 != null) Utils.cleanScrimmage(packageName2);
	}
}
