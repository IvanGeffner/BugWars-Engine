package bugwars;

public enum Symmetry {
    HORIZONTAL ("horizontal"),
    VERTICAL ("vertical"),
    ROTATIONAL ("rotational");

    String symName;

    static Symmetry symmetry;

    static int dimX, dimY;

    static void setMapSize(){
        dimY = Game.getInstance().world.getNCols();
        dimX = Game.getInstance().world.getNRows();
    }

    static GameLocation getSymmetric (GameLocation gloc){
        switch (symmetry){
            case HORIZONTAL:
                return new GameLocation(dimX - 1 - gloc.x, gloc.y);
            case VERTICAL:
                return new GameLocation(gloc.x, dimY - 1 - gloc.y);
            case ROTATIONAL:
                return new GameLocation(dimX - 1 - gloc.x, dimY - 1 - gloc.y);
        }
        return null;
    }

    Symmetry(String name){
        this.symName = name;
    }

}
