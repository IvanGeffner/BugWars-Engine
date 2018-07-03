
package grassmann.example.utils;

import bugwars.*;

public class AttackController {

	private UnitController uc;

	public AttackController(UnitController _uc) {
		uc = _uc;
	}

	public UnitInfo attackUnit(UnitInfo[] units) {
		int minHealth = 99999;
		UnitInfo minUnit = null;
		for (int i = 0; i < units.length && uc.canAttack(); ++i) {
			if (units[i].getTeam() != uc.getTeam() && uc.canAttack(units[i])) {
				if (units[i].getHealth() < minHealth) {
					minUnit = units[i];
					minHealth = units[i].getHealth();
				}
			}
		}
		if (minUnit != null) {
			UnitInfo attackedUnit = minUnit;
			uc.attack(attackedUnit);
		}
		return null;
	}

	public UnitInfo attackUnit() {
		return attackUnit(uc.senseUnits(uc.getTeam().getOpponent()));
	}
}
