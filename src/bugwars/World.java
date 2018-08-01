package bugwars;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


class World {
	
	private Team winner = null;
	private WinCondition winCondition = null;

	private int round;
	private GameLocation[][] teamGameLocations;
	
	private Cell[][] grid;

	final ArrayList<Cell> foodCells;

	private HashSet<Integer> IDs;


	
	World(String mapName) throws NumberFormatException, IOException {
		//turn -1 is to start units (it is done in resumeSlave)
		round = -2;
		MapReader mapReader = new MapReader();
		mapReader.loadMap(mapName);
		grid = mapReader.getMap();
		foodCells = getFoodCells();
		teamGameLocations = mapReader.getTeamGameLocations();
		IDs = new HashSet<>();
	}

	int getNewId(){
		for (int i = 0; i < 1000; ++i){
			int ans = (int)(Math.random()*GameConstants.MAX_ID) +1;
			if (!IDs.contains(ans)) return ans;
		}
		for (int i = 1; i <= GameConstants.MAX_ID; ++i) if (!IDs.contains(i)) return i;
		return 1;
	}

	ArrayList<Cell> getFoodCells(){
		ArrayList<Cell> ans = new ArrayList<>();
		for (int i = 0; i < grid.length; ++i){
			for (int j = 0; j < grid[i].length; ++j){
				if (grid[i][j].maxFood > 0) ans.add(grid[i][j]);
			}
		}
		return ans;
	}

	void updateFood(){
		for (Cell cell : foodCells) cell.setFood(Math.min(cell.getMaxFood(), cell.getFood()+GameConstants.FOOD_REGENERATION));
	}

	// Basic getters and setters
	Team getWinner() { return winner; }
	WinCondition getWinCondition() { return winCondition; }

	void setWinner(Team team, WinCondition winCondition) {
	    if (winner != null) return;
    	winner = team;
    	this.winCondition = winCondition;
    }

	int getNTeams() { return teamGameLocations.length; }
	GameLocation[] getTeamLocation(int index) { return teamGameLocations[index]; }
    
	int getNRows() { return grid.length; }
    int getNCols() { return grid[0].length; }

    // Public methods
    boolean isAccessible(GameLocation gameLocation) {
        if (outOfMap(gameLocation)) return false;
        Cell cell = get(gameLocation);
        if (cell.getUnit() != null || cell.isWall()) return false;
		return true;
    }

    boolean outOfMap(GameLocation gameLocation) {
        if (gameLocation.x < 0 || gameLocation.x >= grid.length) return true;
        if (gameLocation.y < 0 || gameLocation.y >= grid[0].length) return true;
        return false;
    }

    void moveUnit(Unit unit, Direction direction) {
		GameLocation loc = unit.gameLocation;
		GameLocation newLoc = loc.add(direction);

		if (newLoc.isEqual(loc)) return;

        put(newLoc, unit);
        removeUnit(loc,unit);
        unit.prevGameLocation = loc;
        unit.gameLocation = newLoc;
    }

    void setFood(GameLocation loc, int food) {
    	get(loc).setFood(food);
	}
    
    void putUnit(Unit unit, GameLocation loc) {
        if (loc == null) return;
        if (outOfMap(loc) || !isAccessible(loc)) {
			if (Game.printWarnings) System.err.println("Trying to put a unit in a non-empty spot!!");
        	return;
		}
        unit.prevGameLocation = loc;
        unit.gameLocation = loc;
        put(loc, unit);
        if (unit.getType() == UnitType.QUEEN) unit.getTeam().addQueen();
        IDs.add(unit.getUnitId());
    }

    void removeUnit(Unit unit) {
		removeUnit(unit.gameLocation, unit);
		if (unit.getType() == UnitType.QUEEN){
			unit.getTeam().removeQueen();
			if (unit.getTeam().getQueens() == 0) {
				setWinner(unit.getTeam().getOpponent(), WinCondition.DESTRUCTION);
			}
		}
		IDs.remove(unit.getUnitId());
	}

	void mine(Unit unit, GameLocation gameLocation) {
		if (outOfMap(gameLocation)) {
			if (Game.printWarnings) System.err.println("Trying to mine out of map!!");
			return;
		}
		Cell cell = get(gameLocation);
		int food = cell.getFood();
		if (food > GameConstants.ANT_MINING) food = GameConstants.ANT_MINING;
		unit.getTeam().addResources(food);
		cell.setFood(cell.getFood() - food);
	}
	
    void print() {
		if (!Game.printWarnings) return;
    	int teamId = 0;
    	HashMap<Team, Integer> teamIds = new HashMap<Team, Integer>();
        for (int j  = grid.length - 1; j >= 0; --j) {
        	for (int i = 0; i < grid[0].length; ++i) {
            	if (grid[i][j].getUnit() != null){
					Team team = grid[i][j].getUnit().getTeam();
					if (teamIds.get(team) == null) {
						teamIds.put(team, teamId);
						teamId++;
					}
					int currentTeamId = teamIds.get(team)*('A' - 'a');
					char a = 'a';
					switch (grid[i][j].getUnit().getType()) {
						case QUEEN:
							a = 'q';
							break;
						case ANT:
							a = 'a';
							break;
						case BEETLE:
							a = 'e';
							break;
						case SPIDER:
							a = 's';
							break;
						case BEE:
							a = 'b';
							break;
					}
					a += currentTeamId;
					System.out.print(a + " ");
				} else if (grid[i][j].isWall()) {
					System.out.print('P' + " ");
				} else if (grid[i][j].getFood() > 0) {
					System.out.print('F' + " ");
				} else System.out.print('.' + " ");
			}
            System.out.println();
        }
    }
    
    List<GameLocation> getObstacles() {
    	List<GameLocation> obstacles = new ArrayList<GameLocation>();
    	for (int i = 0; i < grid.length; ++i) {
        	for (int j = 0; j < grid[i].length; ++j) {
        		if (grid[i][j].isWall()) obstacles.add(new GameLocation(i, j));
        	}
    	}
    	return obstacles;
    }

    //return locations with positive walls
    List<RockInfo> getWalls() {
    	List<RockInfo> walls = new ArrayList<RockInfo>();
    	for (int i = 0; i < grid.length; ++i) {
        	for (int j = 0; j < grid[i].length; ++j) {
        		RockInfo w = grid[i][j].getRockInfo();
        		if (w.getDurability() > 0) {
        			walls.add(w);
        		}
        	}
    	}
    	return walls;
    }

    //return locations with positive food
    List<FoodInfo> getFood() {
    	List<FoodInfo> food = new ArrayList<FoodInfo>();
    	for (int i = 0; i < grid.length; ++i) {
        	for (int j = 0; j < grid[i].length; ++j) {
        		FoodInfo f = grid[i][j].getFoodInfo();
        		if (f.getFood() > 0) {
        			food.add(f);
        		}
        	}
    	}
    	return food;
    }
    
    List<Unit> getUnits() {
    	List<Unit> units = new ArrayList<Unit>();
    	for (int i = 0; i < grid.length; ++i) {
        	for (int j = 0; j < grid[i].length; ++j) {
        		Unit unit = grid[i][j].getUnit(); 
        		if (unit != null) units.add(unit);
        	}
    	}
    	return units;
    }
    
    // Private methods
    GameLocation getFreeLocation(int tries) {
    	for (int i = 0; i < tries; ++i) {
            int x = (int)(Math.random() * grid.length);
            int y = (int)(Math.random() * grid[0].length);
            if (grid[x][y].isEmpty()) return new GameLocation(x,y);
    	}
        return getFirstFreeLocation();
    }

    private GameLocation getFirstFreeLocation() {
        for (int i = 0; i < grid.length; ++i) {
            for (int j = 0; j < grid[0].length; ++j) {
                if (grid[i][j].isEmpty()) return new GameLocation(i, j);
            }
        }
        return null;
    }

	private void put(GameLocation loc, Unit unit) {
		put(loc.x, loc.y, unit);
	}
	private void put(int x, int y, Unit unit) {
		grid[x][y].setUnit(unit);
	}
	private void removeUnit(GameLocation loc, Unit unit) {
		removeUnit(loc.x, loc.y, unit);
	}
	private void removeUnit(int x, int y, Unit unit) { if (grid[x][y].getUnit() == unit) grid[x][y].setUnit(null); }

	Cell get(GameLocation loc) {
		return get(loc.x, loc.y);
	}
	Cell get (int x, int y) {
		return grid[x][y];
	}

	Cell getIns(int x, int y){
		if (x < 0 || x >= grid.length) return null;
		if (y < 0 || y >= grid[x].length) return null;
		return grid[x][y];
	}

	public boolean nextRound() {
		if (round < GameConstants.MAX_TURNS) {
			round++;
			return true;
		}
		return false;
	}

	public int getRound() { return round; }


	Team getFirstElement(HashSet<Team> winners) {
		if (winners.size() < 1) {
			if (Game.printWarnings) System.err.println("No winners!");
			return null;
		}
		for (Team key : winners) return key;
		return null;
	}

	public void endGame() {
		if (winner != null) return;

		HashSet<Team> winners = new HashSet<Team>();
		
		for (Team team : Game.getInstance().teamManager.getTeams()) {
			if (team.isPlayer()) winners.add(team);
		}

		if (winners.size() <= 1) {
			setWinner(getFirstElement(winners), WinCondition.SINGLE_PLAYER);
			return;
		}

		/*-----------------first tie-break: number of queens ------------------*/
		int maxQueens = 0;

		//compute maxQueens
		for (Team team : winners) {
			if (team.getQueens() > maxQueens) {
				maxQueens = team.getQueens();
			}
		}

		for (Iterator<Team> it = winners.iterator(); it.hasNext();) {
			Team team = it.next();
			if (team.getQueens() < maxQueens) {
				it.remove();
			}
		}
		
		if (winners.size() <= 1) {
			setWinner(getFirstElement(winners), WinCondition.NB_QUEENS);
			return;
		}
		
		/*-----------------second tie-break: total HP of queens ------------------*/
		HashMap<Team, Integer> totalHealthQueens = new HashMap<Team, Integer>();
		for (Team team : winners) {
			totalHealthQueens.put(team, 0);
		}
		
		for (int i = 0; i < grid.length; ++i) {
			for (int j = 0; j < grid[i].length; ++j) {
				Unit unit = grid[i][j].getUnit();
				if (unit != null && unit.getType() == UnitType.QUEEN) {
					Team team = unit.getTeam();
					if (totalHealthQueens.containsKey(team)){
						totalHealthQueens.put(team, totalHealthQueens.get(team) + unit.getHealth());
					}
				}
			}
		}

		int maxHealthQueens = 0;

		for (Team team : totalHealthQueens.keySet()) {
			if (totalHealthQueens.get(team) > maxHealthQueens) maxHealthQueens = totalHealthQueens.get(team);
		}

		for (Iterator<Team> it = totalHealthQueens.keySet().iterator(); it.hasNext();) {
			Team team = it.next();
			if (totalHealthQueens.get(team) < maxHealthQueens) {
				winners.remove(team);
			}
		}

		if (winners.size() <= 1) {
			setWinner(getFirstElement(winners), WinCondition.HP_QUEENS);
			return;
		}

		/*-----------------third tie-break: Sum of all resources and units ------------------*/
		HashMap<Team, Integer> totalResources = new HashMap<Team, Integer>();
		for (Team team : winners) {
			totalResources.put(team, team.getResources());
		}

		for (int i = 0; i < grid.length; ++i) {
			for (int j = 0; j < grid[i].length; ++j) {
				Unit unit = grid[i][j].getUnit();
				if (unit != null) {
					Team team = unit.getTeam();
					if (totalResources.containsKey(team)) {
						totalResources.put(team, totalResources.get(team) + unit.getType().cost);
					}
				}
			}
		}

		int maxResources = 0;

		for (Team team : totalResources.keySet()) {
			if (totalResources.get(team) > maxResources) maxResources = totalResources.get(team);
		}

		for (Iterator<Team> it = totalResources.keySet().iterator(); it.hasNext();) {
			Team team = it.next();
			if (totalResources.get(team) < maxResources) {
				winners.remove(team);
			}
		}
		
		if (winners.size() <= 1) {
			setWinner(getFirstElement(winners), WinCondition.VALUE);
			return;
		}

		/*-----------------fourth tie-break: RNG ------------------*/
		int randomNum = ThreadLocalRandom.current().nextInt(0, winners.size());
		int cont = 0;
		for (Team key : winners){
			if (cont == randomNum){
				setWinner(key, WinCondition.RANDOM);
				break;
			}
			++cont;
		}
	}

}
