package grassmann.example.utils;

import java.util.List;
import java.util.ArrayList;

import bugwars.*;

public class MessageController {

	//GameConstants.TEAM_ARRAY_SIZE;
	final private int QUEEN_SLOTS = 4;
	final private int E_QUEEN_COUNT		= 12;
	final private int E_ANT_COUNT		= 13;
	final private int E_BEETLE_COUNT	= 14;
	final private int E_SPIDER_COUNT	= 15;
	final private int E_BEE_COUNT		= 16;
	final private int A_QUEEN_COUNT		= 17;
	final private int A_ANT_COUNT		= 18;
	final private int A_BEETLE_COUNT	= 19;
	final private int A_SPIDER_COUNT	= 20;
	final private int A_BEE_COUNT		= 21;

	private UnitController uc;

	public MessageController(UnitController _uc) {
		uc = _uc;
	}

	public void setEnemyQueenLocation(int id, Location loc) {
		int i = 0;
		while (i < QUEEN_SLOTS * 3 && uc.read(i) != id) i += 3;
		if (i == QUEEN_SLOTS * 3) i = 0;
		uc.write(i, id);
		uc.write(i + 1, loc.x);
		uc.write(i + 2, loc.y);
	}

	public Location[] getEnemyQueensLocations() {
		List<Location> locs = new ArrayList<Location>();
		for (int i = 0; i < QUEEN_SLOTS * 3; i += 3) {
			if (uc.read(i) != 0) locs.add(new Location(uc.read(i + 1), uc.read(i + 2)));
		}
		return locs.toArray(new Location[locs.size()]);
	}

	public void incEnemy(UnitType type) {
		if (type == UnitType.QUEEN) uc.write(E_QUEEN_COUNT, uc.read(E_QUEEN_COUNT) + 1);
		else if (type == UnitType.ANT) uc.write(E_ANT_COUNT, uc.read(E_ANT_COUNT) + 1);
		else if (type == UnitType.BEETLE) uc.write(E_BEETLE_COUNT, uc.read(E_BEETLE_COUNT) + 1);
		else if (type == UnitType.SPIDER) uc.write(E_SPIDER_COUNT, uc.read(E_SPIDER_COUNT) + 1);
		else if (type == UnitType.BEE) uc.write(E_BEE_COUNT, uc.read(E_BEE_COUNT) + 1);
	}

	public void incAlly(UnitType type) {
		if (type == UnitType.QUEEN) uc.write(A_QUEEN_COUNT, uc.read(A_QUEEN_COUNT) + 1);
		else if (type == UnitType.ANT) uc.write(A_ANT_COUNT, uc.read(A_ANT_COUNT) + 1);
		else if (type == UnitType.BEETLE) uc.write(A_BEETLE_COUNT, uc.read(A_BEETLE_COUNT) + 1);
		else if (type == UnitType.SPIDER) uc.write(A_SPIDER_COUNT, uc.read(A_SPIDER_COUNT) + 1);
		else if (type == UnitType.BEE) uc.write(A_BEE_COUNT, uc.read(A_BEE_COUNT) + 1);
	}

	public int countEnemy(UnitType type) {
		if (type == UnitType.QUEEN) return uc.read(E_QUEEN_COUNT);
		else if (type == UnitType.ANT) return uc.read(E_ANT_COUNT);
		else if (type == UnitType.BEETLE) return uc.read(E_BEETLE_COUNT);
		else if (type == UnitType.SPIDER) return uc.read(E_SPIDER_COUNT);
		else if (type == UnitType.BEE) return uc.read(E_BEE_COUNT);
		return 0;
	}

	public int countAlly(UnitType type) {
		if (type == UnitType.QUEEN) return uc.read(A_QUEEN_COUNT);
		else if (type == UnitType.ANT) return uc.read(A_ANT_COUNT);
		else if (type == UnitType.BEETLE) return uc.read(A_BEETLE_COUNT);
		else if (type == UnitType.SPIDER) return uc.read(A_SPIDER_COUNT);
		else if (type == UnitType.BEE) return uc.read(A_BEE_COUNT);
		return 0;
	}
}
