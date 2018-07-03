package grassmann.example.utils;

import bugwars.*;

public class SpawnController {

	UnitController uc;

	public SpawnController(UnitController _uc) {
		uc = _uc;
	}

	public Direction spawn(UnitType unitType) {
		DirectionIterator dirIt = new DirectionIterator();
		for (int i = 0; i < 8; ++i) {
			Direction dir = dirIt.next();
			if (uc.canSpawn(dir, unitType)) {
				uc.spawn(dir, unitType);
				return dir;
			}
		}
		return null;
	}
}
