package matchmaker;

class Player {
	Integer id;
	Integer userId;
	Integer elo;

	Player(Integer _id, Integer _userId, Integer _elo) {
		id = _id;
		userId = _userId;
		elo = _elo;
	}
}
