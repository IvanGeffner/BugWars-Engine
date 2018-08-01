package bugwars;

import java.io.*;

class MapReader {

    final private String dirPath = System.getProperty("user.dir") + "/maps/";

    //private List<GameLocation> teamGameLocations = new ArrayList<GameLocation>();

    private GameLocation[][] teamGameLocations;
    private Cell[][] map = null;
    private Symmetry symmetry;
    
    void loadMap(String mapName) throws NumberFormatException, IOException {
        FileReader fr = new FileReader(dirPath + mapName + ".txt");
        
        BufferedReader mapReader = new BufferedReader(fr);

        //Read dimensions
        String[] dims = mapReader.readLine().trim().split("\\s+");
        int dimX = Integer.parseInt(dims[0]), dimY = Integer.parseInt(dims[1]);
        
        // Generate map grid
        map = new Cell[dimX][dimY];
        for (int i = 0; i < dimX; ++i){
            for (int j = 0; j < dimY; ++j){
            	map[i][j] = new Cell(new GameLocation(i, j));
            }
        }

        //Read offsets
        String[] offset = mapReader.readLine().trim().split("\\s+");
        GameLocation.offsetX = Integer.parseInt(offset[0]);
        GameLocation.offsetY = Integer.parseInt(offset[1]);

        // Read symmetry
        String[] sym = mapReader.readLine().trim().split("\\s+");
        for (int i = 0; i < Symmetry.values().length; ++i){
            if (sym[0].equals(Symmetry.values()[i].symName)) symmetry = Symmetry.values()[i];
        }

        // Read walls
        int nWalls = Integer.parseInt(mapReader.readLine());
        for (int i = 0; i < nWalls; ++i){
            String[] wallPos = mapReader.readLine().trim().split("\\s+");
            if (wallPos.length <= 2) {
                map[Integer.parseInt(wallPos[0])][Integer.parseInt(wallPos[1])].setMaxRockDurability(1000);
            }
            else map[Integer.parseInt(wallPos[0])][Integer.parseInt(wallPos[1])].setMaxRockDurability(Integer.parseInt(wallPos[2]));
        }

        //Read food
        int nFood = Integer.parseInt(mapReader.readLine());
        for (int i = 0; i < nFood; ++i){
            String[] foodInfo = mapReader.readLine().trim().split("\\s+");
            map[Integer.parseInt(foodInfo[0])][Integer.parseInt(foodInfo[1])].setMaxFood(Integer.parseInt(foodInfo[2]));
        }

        int nteams = 2; //for this game
        teamGameLocations = new GameLocation[2][0];

        //initial locations
        for (int i = 0; i < nteams; ++i){
            int nqueens = Integer.parseInt(mapReader.readLine());
            teamGameLocations[i] = new GameLocation[nqueens];
            for(int j = 0; j < nqueens; ++j){
                String[] queenLoc = mapReader.readLine().trim().split("\\s+");
                teamGameLocations[i][j] = new GameLocation(Integer.parseInt(queenLoc[0]),Integer.parseInt(queenLoc[1]));
            }
        }
        
        mapReader.close();
    }
    
    Cell[][] getMap(String mapName) throws NumberFormatException, IOException {
    	if (map == null) loadMap(mapName);
    	return getMap();
    }
    
    Cell[][] getMap() {
    	if (map == null && Game.printWarnings) System.err.println("Warning: Map not loaded when calling 'getMap'");
    	return map;
    }
    
    int getNTeams() {
    	return teamGameLocations.length;
    }

	public GameLocation[][] getTeamGameLocations() {
    	if (map == null && Game.printWarnings) System.err.println("Warning: Map not loaded when calling 'getTeamGameLocations'");
    	return teamGameLocations;
	}
}
