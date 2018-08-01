package simpleplayer.test1;

import bugwars.*;

public class UnitPlayer {

	public UnitPlayer() {}

	public void run(UnitController uc) throws InterruptedException {
	/*Your code goes here*/

		/*If this is your first attempt we suggest not coding outside the while(true){}*/
		while (true){
			/*Each iteration of this while(true) represents a round of the game*/

			Location enemyPos =  uc.getEnemyQueensLocation()[0];
			Direction dirToQueen = uc.getLocation().directionTo(enemyPos);

			if (uc.getType() == UnitType.QUEEN) {
				if (uc.canSpawn(dirToQueen, UnitType.BEE)) uc.spawn(dirToQueen, UnitType.BEE);
			}

			else{
				for (int i = 0; i < 8; ++i){
					if (uc.canMove(dirToQueen)){
						uc.move(dirToQueen);
						break;
					}
					dirToQueen = dirToQueen.rotateLeft();
				}

				UnitInfo[] enemyUnits = uc.senseUnits(uc.getType().attackRangeSquared, uc.getTeam().getOpponent());
				if (enemyUnits.length > 0) uc.attack(enemyUnits[0]);
			}




			uc.yield();
		}

	}
}

