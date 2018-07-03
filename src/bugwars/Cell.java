package bugwars;

class Cell {
    int rockDurability;
    int maxRockDurability;
    private Unit unit;
    int food;
    int maxFood;
    final GameLocation gameLocation;
    
    Cell(GameLocation _Game_location) {
        this(0, 0, null, 0, _Game_location);
    }

    Cell(int _rockDurability, int _maxRockDurability, Unit _unit, int _food, GameLocation _Game_location) {
        rockDurability = _rockDurability;
        maxRockDurability = _maxRockDurability;
        unit = _unit;
        food = _food;
        maxFood = _food;
        gameLocation = _Game_location;
    }
    
    boolean isWall() { return rockDurability > 0; }

    void setRockDurability (int _rockDurability){
        rockDurability = _rockDurability;
    }

    void setMaxRockDurability (int _maxRockDurability){
        rockDurability = _maxRockDurability;
        maxRockDurability = _maxRockDurability;
    }

    
    Unit getUnit() { return unit; }
    void setUnit(Unit _unit) { unit = _unit; }
    
    int getFood() { return food; }
    void setFood(int _food) { food = _food; }

    int getMaxFood() {return maxFood; }

    void setMaxFood(int _maxFood){
        maxFood = _maxFood;
        food = _maxFood;
    }
    
    boolean isEmpty() {
        return !isWall() && unit == null && food == 0;
    }

    FoodInfo getFoodInfo(){
        return new FoodInfo(food, maxFood, gameLocation.toLocation());
    }

    RockInfo getRockInfo() { return new RockInfo(rockDurability, maxRockDurability, gameLocation.toLocation()); }

    void decreaseDurability(float dmg) {
        rockDurability -= dmg;
        if (rockDurability < 0) rockDurability = 0;
    }
}


