package bugwars;

import java.util.LinkedList;
import java.util.List;

class UnitManager {
	
	World world;
	private int index = -1;
	private List<Unit> units;
	
	UnitManager(World world) {
		this.world = world;
		units = new LinkedList<>();
	}
	
	//spawner = null -> last in the list
	//loc = null -> random
	Unit newUnit(Team teamLoader, GameLocation loc, Unit spawner, UnitType type) {
		if (loc == null) loc = world.getFreeLocation(10);

		int spawnerIndex;
		if (spawner == null) spawnerIndex = units.size() - 1;
		else spawnerIndex = units.indexOf(spawner);

		Unit unit = new Unit(this, teamLoader, type);
		units.add(spawnerIndex + 1, unit);
		world.putUnit(unit, loc);
		return unit;
	}

	void resetIndex() {
		index = -1;
	}

	Unit getCurrentUnit() {
		return units.get(index);
	}

	public boolean hasNextUnit() {
		return index + 1 < units.size();
	}

	Unit nextUnit() {
		++index;
		return units.get(index);
	}

	void killUnit(Unit unit) {
		world.removeUnit(unit);
		unit.kill();
	}
	
	//I dont think we need to use this separately
	void removeUnit(Unit unit) {
		int a = units.indexOf(unit);
		if (a <= index) --index;
		units.remove(a);
	}

	//Should only be called when just before the thread is killed.
	//However we erase the unit from the map the instant it is killed (at some other place of the code).
	void removeCurrentUnit() {
		removeUnit(getCurrentUnit());
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
