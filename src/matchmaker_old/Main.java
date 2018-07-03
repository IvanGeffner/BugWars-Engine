package matchmaker;

import java.util.List;

public class Main {
	public static void main(String[] args) {
		System.out.println("Matchmaker started!");
		// Iterate over all tournaments
		List<Integer> tournamentsIds = DbController.getTournamentsIds();
		for (Integer tournamentId : tournamentsIds) {
			// Create TDays
			Integer lastTDay = DbController.getLastTDay(tournamentId);
			if (lastTDay == null) lastTDay = 0;
			Integer tDayId = DbController.insertTDay(tournamentId, lastTDay + 1);
			// Create TDaysRankings
			List<Player> players = DbController.getTournamentPlayers(tournamentId);
			for (Player player : players) {
				DbController.insertTDaysRanking(tDayId, player.id, player.initialElo, 2);
			}
			// Iterate rounds
			if (players.size() < 2) return;
			TRound tRound = new TRound(tournamentId, tDayId, 1);
			tRound.executeBase();
			tRound.execute(2, 2);
			int round = 2;
		  players = DbController.getTDayPlayers(tDayId);
			while (players.get(1).points == 2) {
				tRound = new TRound(tournamentId, tDayId, round);
				tRound.execute(1, 1);
				tRound.execute(2, 2);
				tRound.execute(3, 1);
				round++;
			  players = DbController.getTDayPlayers(tDayId);
			}
			for (Player player : players) {
				DbController.updateElos(player.userId, tournamentId, player.currentElo,
			 													player.nGames);
			}
	    DbController.updateTDayStatus(tDayId, "Completed");
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				System.err.println("Matchmaker was interrupted");
				System.exit(0);
			}
		}
	}
}
