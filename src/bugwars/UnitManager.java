package bugwars;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

class UnitManager {
	
	World world;
    private ListIterator<Unit> iterator;
	private LinkedList<Unit> units;
	private Unit currentUnit;
	
	UnitManager(World world) {
		this.world = world;
		units = new LinkedList<>();
	}
	
	//spawner = null -> last in the list
	//loc = null -> random
    Unit newUnit(Team teamLoader, GameLocation loc, Unit spawner, UnitType type, boolean initial) {
        if (loc == null) loc = world.getFreeLocation(10);

        Unit unit = new Unit(this, teamLoader, type, initial);

        //int spawnerIndex;
        if (spawner == null){
            units.addLast(unit);
        } else{
            iterator.add(unit);
            iterator.previous();
            iterator.previous();
            iterator.next();
        }
        world.putUnit(unit, loc);
        return unit;
    }

	void resetIndex() {
        iterator = units.listIterator();
	}

	Unit getCurrentUnit() {
		return currentUnit;
	}

	public boolean hasNextUnit() {
        return iterator.hasNext();
	}

	Unit nextUnit() {
        currentUnit = iterator.next();
        return currentUnit;
	}

	void killUnit(Unit unit) {
        unit.kill();
		world.removeUnit(unit);
	}

	//Should only be called when just before the thread is killed.
	//However we erase the unit from the map the instant it is killed (at some other place of the code).
	void removeCurrentUnit() {
        iterator.remove();
	}

	void killCurrentUnit() {
		killUnit(getCurrentUnit());
	}

	boolean currentUnitKilled() {
		return getCurrentUnit().isDead();
	}

	int getUnitCount(){
		return units.size();
	}

}
