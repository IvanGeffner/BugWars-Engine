package bugwars;

/**
 * Instantiate world, unit manager, teams
 * Iterate turns
 */
class Game extends Thread {
	static boolean printWarnings = true;
	World world;
	UnitManager unitManager;
	TeamManager teamManager;
	GameLog gameLog;
	String logName;
	boolean prints, drawings;

	Team team1, team2;

	// Singleton pattern ******************************************************/
	private static Game instance = null;

    static Game getInstance() {
        return instance;
    }

    static Game getInstance(String packageName1, String packageName2,
		 												String mapName, String logName, boolean prints,
														boolean drawings, boolean _printWarnings) {
		if (instance == null) {
			printWarnings = _printWarnings;
			instance = new Game(packageName1, packageName2, mapName, logName,
								prints, drawings);
		}
		return instance;
    }

    private Game(String packageName1, String packageName2, String mapName,
	 								String _logName, boolean _prints, boolean _drawings) {
		// Read world from map file
		world = null;
		try {
			world = new World(mapName);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Managers
		unitManager = new UnitManager(world);
		teamManager = new TeamManager(world);

		// Teams
		team1 = teamManager.newTeam(packageName1);
		team2 = teamManager.newTeam(packageName2);

		// Log name
		logName = _logName;
		prints = _prints;
		drawings = _drawings;
    }
	// End Singleton pattern **************************************************/

	@Override
	public void run() {
		// Setup log
		gameLog = new GameLog(this.logName, this.prints, this.drawings);

		// Create teams
		teamManager.addResourcesToAll(GameConstants.INITIAL_RESOURCES);

		// Spawn barracks
		Location[] queenLoc1 = team1.getInitialLocations();
		for (int i = 0; i < queenLoc1.length; ++i) {
			Unit queen = unitManager.newUnit(team1, new GameLocation(queenLoc1[i]), null, UnitType.QUEEN);
			team1.setQueen(i, queen);
		}
		Location[] queenLoc2 = team2.getInitialLocations();
		for (int i = 0; i < queenLoc2.length; ++i) {
			Unit queen = unitManager.newUnit(team2, new GameLocation(queenLoc2[i]), null, UnitType.QUEEN);
			team2.setQueen(i, queen);
		}

		// Iterate over all rounds
		Team winner = null;
		while (winner == null && world.nextRound()) {
			//start cycle through units
			unitManager.resetIndex();
			//regenerate food
			world.updateFood();
			while (unitManager.hasNextUnit()) {
				Unit unit = unitManager.nextUnit();

				// Reset cooldowns
				unit.resetCooldowns();

				// Run turn
				ThreadManager.getInstance(unit).resumeSlave();

				// Check winner
				winner = world.getWinner();
				if (winner != null) break;
			}

			gameLog.printTurn();

			teamManager.addResourcesToAll(GameConstants.RESOURCES_TURN);
		}

		world.print();
		world.endGame();
		winner = world.getWinner();
		String winCondition = world.getWinCondition();

		// Finishes threads
		stopAllUnitThreads(unitManager);

		//Winner
		gameLog.printWinner(winner, winCondition);
		if (Game.printWarnings) System.out.println("Winner: " + winner.packageName);
		if (Game.printWarnings) System.out.println("WinCondition: " + winCondition);

		// Number of turns
		gameLog.printNumberOfRounds();
	}

	void stopAllUnitThreads(UnitManager unitManager) {
    	while (unitManager.getUnitCount() > 0) {
			unitManager.resetIndex();
			while (unitManager.hasNextUnit()) {
				Unit unit = unitManager.nextUnit();
				unitManager.killCurrentUnit();
				ThreadManager.getInstance(unit).resumeSlave();
			}
		}
	}
}
