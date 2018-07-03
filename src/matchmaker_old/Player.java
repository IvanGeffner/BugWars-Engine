package matchmaker;

import java.lang.Comparable;

class Player implements Comparable<Player> {
	Integer id;
	Integer userId;
	Integer initialElo;
	Integer currentElo;
	Integer points;
	Integer nGames;

	Player(Integer _id, Integer _userId, Integer _initialElo, Integer _currentElo,
	 				Integer _points, Integer _nGames) {
		id = _id;
		userId = _userId;
		initialElo = _initialElo;
		currentElo = _currentElo;
		points = _points;
		nGames = _nGames;
	}

	@Override
	public int compareTo(Player p) {
		return p.points.compareTo(this.points);
	}
}
