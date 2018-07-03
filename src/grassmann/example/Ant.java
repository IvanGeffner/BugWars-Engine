package grassmann.example;

import bugwars.*;

public class Ant extends Unit {

	void staySafe(UnitInfo[] enemies) {
		if (enemies.length > 0) {
			int danger = 0;
			double x = 0, y = 0;
			for (UnitInfo enemy : enemies) {
				if (uc.getLocation().distanceSquared(enemy.getLocation()) <= enemy.getType().getAttackRangeSquared()) {
					danger++;
					x += enemy.getLocation().x;
					y += enemy.getLocation().y;
				}
			}
			if (danger > 0) {
				x /= danger;
				y /= danger;
				mc.moveAwayFrom(new Location((int)x, (int)y), 5);
			}
		}
	}

	boolean mine() {
		FoodInfo[] foods = uc.senseFood();
		int minDist = 9999999;
		FoodInfo minFood = null;
		for (FoodInfo food : foods) {
			if (uc.canMine(food.getLocation())) {
				uc.mine(food.getLocation());
			}
			int dist = uc.getLocation().distanceSquared(food.getLocation());
			if (dist < minDist) {
				minDist = dist;
				minFood = food;
			}
		}
		if (minFood != null && minDist > 0) {
			mc.moveTowards(minFood.getLocation(), 5);
		}
		return foods.length != 0;
	}

	void handleTurn() {
		UnitInfo[] enemies = uc.senseUnits(uc.getTeam().getOpponent());
		handleInitialMessages(enemies);

		staySafe(enemies);
		if (!mine()) {
			mc.moveRandom();
		}
	}
}
