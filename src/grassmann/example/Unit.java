package grassmann.example;

import java.util.ArrayList;
import java.util.List;

import grassmann.example.utils.AttackController;
import grassmann.example.utils.MessageController;
import grassmann.example.utils.MovementController;
import grassmann.example.utils.PathController;
import grassmann.example.utils.SpawnController;
import grassmann.example.utils.Utils;
import bugwars.*;

public class Unit {

	UnitController uc;
	MovementController mc;
	PathController pc;
	AttackController ac;
	SpawnController sc;
	MessageController rc;

	Utils utils;

	void run(UnitController uc) {
		this.uc = uc;
		mc = new MovementController(uc);
		ac = new AttackController(uc);
		sc = new SpawnController(uc);
		rc = new MessageController(uc);

		utils = new Utils();

		pc = new PathController(uc, uc.getLocation(), uc.getTeam().getOpponent().getInitialLocations()[0]);

		while (true) {
			handleTurn();
			uc.yield();
		}
	}

	void handleInitialMessages(UnitInfo[] enemies) {
		for (UnitInfo enemy : enemies) {
			rc.incEnemy(enemy.getType());
		}
		rc.incAlly(uc.getType());
	}

	void handleInitialMessages() {
		UnitInfo[] enemies = uc.senseUnits(uc.getTeam().getOpponent());
		handleInitialMessages(enemies);
	}

	void updateObjective() {
		Location[] enemyQueensLocs = rc.getEnemyQueensLocations();
		if (enemyQueensLocs.length == 0) return;
		Location minObj = null;
		double minDist = 9999999999.0;
		for (Location loc : enemyQueensLocs) {
			double dist = uc.getLocation().distanceSquared(loc);
			if (dist < minDist) {
				minDist = dist;
				minObj = loc;
			}
		}
		pc = new PathController(uc, uc.getLocation(), minObj);
	}

	void handleTurn() {
		uc.println("This is a test " + uc.getRound());

		UnitInfo[] enemies = uc.senseUnits(uc.getTeam().getOpponent());
		handleInitialMessages(enemies);

		updateObjective();

		//UnitInfo attackedUnit = ac.attackUnit(enemies);
		//if (attackedUnit != null) mc.moveTowards(attackedUnit.getGameLocation(), 5);
		ac.attackUnit(enemies);
		Location dangerousLoc = utils.getDangerLocation(enemies);
		Direction dir = null;
		if (dangerousLoc != null) dir = uc.getLocation().directionTo(dangerousLoc);
		if (dir != null && dir != Direction.ZERO) {
			UnitInfo[] allies = uc.senseUnits(uc.getTeam());
			if (getDangerousEnemies(dir, enemies).size() <= getHelpfullAllies(dir, allies).size()) {
				mc.moveTowards(dir, 1);
			} else {
				mc.moveAwayFrom(dir, 3);
			}
		}

		if (uc.canMove()) {
			dir = pc.getMoveDir();
			if (dir != null && dir != Direction.ZERO) {
				UnitInfo[] allies = uc.senseUnits(uc.getTeam());
				if (getDangerousEnemies(dir, enemies).size() <= getHelpfullAllies(dir, allies).size()) {
					mc.moveTowards(dir, 3);
				} else {
					mc.moveAwayFrom(dir, 3);
				}
			}
		}

		ac.attackUnit();
	}

	private List<UnitInfo> getHelpfullAllies(Direction dir, UnitInfo[] allies) {
		Location newLoc = uc.getLocation().add(dir);
		List<UnitInfo> helpfulAllies = new ArrayList<UnitInfo>();
		for (UnitInfo ally : allies) {
			if (ally.getType().canAttack() && newLoc.distanceSquared(ally.getLocation()) <= ally.getType().attackRangeSquared + 5) {
				helpfulAllies.add(ally);
			}
		}
		return helpfulAllies;
	}

	private List<UnitInfo> getDangerousEnemies(Direction dir, UnitInfo[] enemies) {
		Location newLoc = uc.getLocation().add(dir);
		List<UnitInfo> dangerousEnemies = new ArrayList<UnitInfo>();
		for (UnitInfo enemy : enemies) {
			if (enemy.getType().canAttack() && newLoc.distanceSquared(enemy.getLocation()) <= enemy.getType().attackRangeSquared) {
				dangerousEnemies.add(enemy);
			}
		}
		return dangerousEnemies;
	}
}
