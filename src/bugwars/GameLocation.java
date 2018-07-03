package bugwars;

class GameLocation {
    public int x, y;

    static int offsetX, offsetY;

    public Location toLocation(){
        return new Location(x + offsetX, y + offsetY);
    }

    public GameLocation(){}

    public GameLocation(Location loc){
        if (loc != null) {
            x = loc.x - offsetX;
            y = loc.y - offsetY;
        }
    }

    public GameLocation(int x_, int y_) {
        x = x_;
        y = y_;
    }

    public GameLocation add(Direction dir) {
        if (dir == null) {
            return this;
        }
        return new GameLocation(x + dir.dx, y + dir.dy);
    }

    public GameLocation add(int _x, int _y) {
        return new GameLocation(x + _x, y + _y);
    }

    public Direction directionTo(GameLocation loc) {
        if (loc == null) {
            return Direction.ZERO;
        }
        return Direction.getDirection(loc.x - x, loc.y - y);
    }

    public boolean isEqual(GameLocation loc) {
        if (loc == null) return false;
        if (x != loc.x) return false;
        if (y != loc.y) return false;
        return true;
    }

    public int distanceSquared(GameLocation loc) {
        if (loc == null) {
            return 0;
        }
        int dx = x - loc.x, dy = y-loc.y;
        return dx*dx + dy*dy;
    }
    
    public String toJson() {
    	return "{x:" + x + ",y:" + y + "}";
    }
    
    public String toString() {
    	return toJson();
    }

}
