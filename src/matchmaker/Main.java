package matchmaker;

import java.util.*;

public class Main {

	// BugWars is tournament with id = 1
	final static Integer tournamentId = 1;

	final static int k = 3;
	final static double b = 75;

    static double weight(double elo1, double elo2){
        return Math.exp((elo2-elo1)/b);
    }

    static void doMatching(List<Player> players){

        int n = players.size();

        boolean used[] = new boolean[n];

        //go through players from + to - elo
        for (int i = 0; i < n; ++i){

            if (used[i]) continue;

            double[] p = new double[k];
            double sumprobs = 0;
            int lastPossible = i;
            //put weights to players to be matched with player i
            for (int j = i+1; j <= Math.min(n-1, i+k); ++j){
                if (!used[j]){
                    p[j-i-1] = weight(players.get(i).elo, players.get(j).elo);
                    lastPossible = j;
                }
                sumprobs += p[j-i-1];
            }

            //choose opponent randomly according to weights
            double partsum = 0;
            double opponent = Math.random();
            int realOp = i;
            for (int j = i+1; j <= Math.min(n-1, i+k); ++j){
                partsum += p[j-i-1]/sumprobs;
                if (j == lastPossible) partsum = 1;
                if (opponent <= partsum){
                    realOp = j;
                    break;
                }
            }

            used[realOp] = true;

            Player p1 = players.get(i);
            Player p2 = players.get(realOp);
            Integer scrimmageId = DbController.insertScrimmage(p1.id, p2.id, 3, tournamentId);
            DbController.insertTask(scrimmageId);
        }
    }


	public static void main(String[] args) {
		System.out.println("Matchmaker started!");
		// Get players
		List<Player> players = DbController.getTournamentPlayers(tournamentId);
		// Remove one if odd

        Integer removedPlayerID = null;

		if (players.size() % 2 == 1) {
			int idx = (int)(Math.random() * players.size());
			removedPlayerID = players.get(idx).id;
			players.remove(idx);
		}

		//sort by elo, descending
		Collections.sort(players, new Comparator<Player>() {
			public int compare(Player p1, Player p2) {
				return p1.elo > p2.elo ? -1 : (p1.elo < p2.elo) ? 1 : 0;
			}
		});

		doMatching(players);

		//TODO [Ivan] Jo afegiria aixo per tal que tothom jugui :)
		/*
		if (removedPlayerID != null){
            int idx = (int)(Math.random() * players.size());
            Integer scrimmageId = DbController.insertScrimmage(removedPlayerID, players.get(idx).id, 3, tournamentId);
            DbController.insertTask(scrimmageId);
        }*/

		/* I think we can remove this :)

		// Iterate while there are players to match
		while (!players.isEmpty()) {
			// Get a random player
			int idx = (int)(Math.random() * players.size());
			// Compute all square distances
			List<Double> probs = new ArrayList<Double>();
			for (int i = 0; i < players.size(); ++i) {
				double dist = Math.abs(
					(double)(players.get(i).elo - players.get(idx).elo));
				if (i == idx) probs.add(0.0);
				else probs.add(1 / (dist * dist + Math.random() + 20));
			}
			// Compute all probabilities
			Double sumP = 0.0;
			for (int i = 0; i < players.size(); ++i) sumP = sumP + probs.get(i);
			for (int i = 0; i < players.size(); ++i) {
				double dist = (double)(players.get(i).elo - players.get(idx).elo);
				probs.set(i, probs.get(i) / sumP);
			}
			// Select one player based on probabilities
			double randomNumber = Math.random();
			Double acumP = 0.0;
			Integer idx2 = null;
			for (int i = 0; idx2 == null && i < players.size(); ++i) {
				acumP = acumP + probs.get(i);
				if (i != idx && randomNumber < acumP) idx2 = i;
			}
			if (idx2 == null) idx2 = players.size() - 1;
			// Create scrimmage and task
			Player p1 = players.get(idx);
			Player p2 = players.get(idx2);
			Integer scrimmageId = DbController.insertScrimmage(p1.id, p2.id, 3, tournamentId);
			DbController.insertTask(scrimmageId);
			// Remove players from the list
			players.remove(p1);
			players.remove(p2);
		}
		*/
	}
}
