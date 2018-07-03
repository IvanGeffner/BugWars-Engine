package bugwars;

/**
 * Classe que conte tota la informacio de cadascun dels equips de la partida.
 */
public class Team {
	String packageName;

	private Class<?> unitPlayerClass;

	private static int nextId = 1;
	private int id;

	private boolean isPlayer;
	private int resources;
	private int nQueens;
	private Location[] initialLocations;
	private Unit[] myQueens;

	private int[] teamArray;

	Team(String _packageName, GameLocation[] _initialGameLocations) {
		id = nextId;
		nextId++;

		packageName = _packageName;
		unitPlayerClass = findUnitPlayerClass(packageName);

		isPlayer = true;
		resources = 0;
		nQueens = 0;
		initialLocations = new Location[_initialGameLocations.length];
		for (int i = 0; i < _initialGameLocations.length; ++i) initialLocations[i] = _initialGameLocations[i].toLocation();
		teamArray = new int[GameConstants.TEAM_ARRAY_SIZE];
		myQueens = new Unit[_initialGameLocations.length];
	}

	/**
	 * Dona l'array amb les posicions inicials de les reines de l'equip. Costa 10 d'energia.
	 * @return Retorna l'array amb les posicions inicials de les reines de l'equip.
	 */
	public Location[] getInitialLocations() { return initialLocations; }

	/**
	 * Dona l'equip contrari a la partida. Costa 1 d'energia.
	 * @return Retorna l'equip contrari.
	 */
	public Team getOpponent() { return Game.getInstance().teamManager.getOpponent(this); }

	/**
	 * Calcula si l'equip donat i aquest equip son el mateix.
	 * @param team Un altre equip
	 * @return Retorna si els aquest equip es igual a l'equip donat.
	 */
	public boolean isEqual(Team team){
		return this.id == team.id;
	}

	Class<?> getUnitPlayerClass() { return unitPlayerClass; }

	boolean isPlayer() { return isPlayer; }

	int getResources() { return resources; }

	void addResources(int x) { resources += x; }

	boolean haveResources(int x) { return (resources >= x); }

	void addQueen(){ ++nQueens; }

	void removeQueen() { --nQueens; }

	int getQueens() { return nQueens; }

	int getTeamId() { return id; }

	void setQueen(int i, Unit queen) {
		myQueens[i] = queen;
	}

	Unit[] getQueenArray(){
		return myQueens;
	}

    // UnitPlayer class loader
	private Class<?> findUnitPlayerClass(String packageName) {
		MyClassLoader cl = new MyClassLoader();
		Class<?> unitPlayerClass = null;
    try {
    	unitPlayerClass = cl.loadClass(packageName.toLowerCase() + ".UnitPlayer");
		} catch (ClassNotFoundException e) {
			if (Game.printWarnings) {
				System.err.println(e);
				System.err.println("Error: UnitPlayer class not found for " + packageName);
			}
			System.exit(1);
		}
    	return unitPlayerClass;
	}

	boolean setArray(int i, int a){
		if (i < 0 || i >= GameConstants.TEAM_ARRAY_SIZE) return false;
		teamArray[i] = a;
		return true;
	}

	int readArray(int i){
		return teamArray[i];
	}
}
