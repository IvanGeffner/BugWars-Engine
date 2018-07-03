
package grassmann.example;

import bugwars.*;

public class Queen extends Unit {

	void staySafe(UnitInfo[] enemies) {
		if (enemies.length > 0) {
			double x = 0, y = 0;
			for (UnitInfo enemy : enemies) {
				x += enemy.getLocation().x;
				y += enemy.getLocation().y;
			}
			x /= enemies.length;
			y /= enemies.length;
			mc.moveAwayFrom(new Location((int)x, (int)y), 5);
		}
	}

	void spawn() {
		int nBeetle = rc.countEnemy(UnitType.BEETLE);
		int nSpider = rc.countEnemy(UnitType.SPIDER);
		int nBee = rc.countEnemy(UnitType.BEE);
		int total = nBeetle + nSpider + nBee;

		if (total == 0) sc.spawn(utils.getRandomType());
		else {
			double rand = Math.random();
			if (rand < (double)nBeetle / total) sc.spawn(UnitType.SPIDER);
			else if (rand < (double)(nBeetle + nSpider) / total) sc.spawn(UnitType.BEE);
			else sc.spawn(UnitType.BEETLE);
		}
	}

	void handleTurn() {
		uc.drawPoint(uc.getLocation(), "yellow");
		uc.drawPoint(uc.getTeam().getOpponent().getInitialLocations()[0], "red");

		UnitInfo[] enemies = uc.senseUnits(uc.getTeam().getOpponent());
		handleInitialMessages(enemies);

		staySafe(enemies);
		spawn();
	}
}
