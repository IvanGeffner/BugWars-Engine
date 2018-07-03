package matchmaker;

import java.util.List;
import java.util.Collections;
import java.lang.Math;

public class TRound {
  Integer tournamentId;
  Integer tDayId;
  Integer round;
  List<Player> players;

  TRound(Integer _tournamentId, Integer _tDayId, int _round) {
    tournamentId = _tournamentId;
    tDayId = _tDayId;
    round = _round;
  }

  void execute(int stage, int from, int to) {
		int half = (from + to + 1)/2;
		for (int i = from; i < half; ++i) {
			// Queue scrimmage and task
			Integer player1Id = players.get(i).id;
			Integer player2Id = players.get(i + (half - from)).id;
			Integer mapId = DbController.getRandomMapId(tournamentId);
			int nGames = 3;
			Integer scrimmageId = DbController.insertScrimmage(player1Id, player2Id,
																													mapId, nGames);
			DbController.insertTRound(tDayId, scrimmageId, round, stage);
			DbController.insertTask(scrimmageId);
		}
    // Wait until round execution is over
    this.waitRoundEnd(stage);
    // Update status and points
    this.updateRoundResults(stage);
	}

  void execute(int stage, int points) {
    players = DbController.getTDayPlayers(tDayId);
    Collections.sort(players);
		int from = 0;
		int to = 0;
		while (players.get(from).points > points) {
			from++;
			to++;
		}
		while (to + 1 < players.size() && players.get(to + 1).points == points) to++;
    execute(stage, from, to);
	}

	void executeBase() {
    players = DbController.getTDayPlayers(tDayId);
    Collections.sort(players);
		int n = 1;
		int pow = 2;
		while (pow*2 < players.size()) {
			n = n + 1;
			pow = pow*2;
		}
		// Pair remaining 2*k players to achieve power of two
    int k = players.size() - pow;
    execute(1, pow - k, players.size() - 1);
	}

	private void updateRoundResults(int stage) {
    List<Scrimmage> scrimmages = DbController.getTRoundsScrimmages(tDayId, round, stage);
    int decPoints = (round == 1) ? -2 : -1;
    for (Scrimmage scrimmage : scrimmages) {
      // Update ELOs
      Float elo1 = null, elo2 = null;
      for (Player player : players) {
        if (player.id == scrimmage.player1Id) elo1 = (float)player.currentElo;
        else if (player.id == scrimmage.player2Id) elo2 = (float)player.currentElo;
      }
      if (elo1 == null || elo2 == null) continue;
      float k = 16f;
      float r1 = (float)Math.pow(10f, elo1 / 400f);
      float r2 = (float)Math.pow(10f, elo1 / 400f);
      float e1 = r1 / (r1 + r2);
      float e2 = r2 / (r1 + r2);
      float s1 = (scrimmage.wins1 > scrimmage.wins2 ? 1f : 0f);
      float s2 = 1 - s1;
      int inc1 = Math.round(k * (s1 - e1));
      int inc2 = Math.round(k * (s2 - e2));
      DbController.updateTDaysRankingElo(tDayId, scrimmage.player1Id, inc1, s1 == 1 ? 0 : decPoints);
      DbController.updateTDaysRankingElo(tDayId, scrimmage.player2Id, inc2, s2 == 1 ? 0 : decPoints);
    }
	}

	private void waitRoundEnd(int stage) {
		// Wait until round is over. Sleep some seconds to allow queued tasks
		while (DbController.getQueuedTRounds(tDayId, round, stage) > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Matchmaker was interrupted");
				System.exit(0);
			}
		}
	}
}
