package grassmann.example.utils;

import bugwars.Direction;
import bugwars.Location;
import bugwars.UnitInfo;
import bugwars.UnitType;

public class Utils {
	private DirectionIterator dirIt;

	public Utils() {
		dirIt = new DirectionIterator();
	}

	public Utils(DirectionIterator _dirIt) {
		dirIt = _dirIt;
	}

	public int getRandom(int n) {
		return (int) (Math.random() * n);
	}

	public UnitType getRandomType() {
	    return UnitType.values()[getRandom(UnitType.values().length)];
	}

	public Direction getRandomDir() {
	    return dirIt.directions[getRandom(dirIt.directions.length)];
	}

	public Location getDangerLocation(UnitInfo[] enemies) {
		if (enemies.length == 0) return null;
		float x = 0.0f, y = 0.0f;
		int n = 0;
		for (UnitInfo enemy : enemies) {
			if (enemy.getType().canAttack()) {
				x += enemy.getLocation().x;
				y += enemy.getLocation().y;
				n++;
			}
		}
		return new Location(Math.round(x / n), Math.round(y / n));
	}
}
