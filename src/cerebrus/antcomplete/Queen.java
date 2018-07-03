package cerebrus.antcomplete;

import navgame.*;

/**
 * Created by Ivan on 2/4/2018.
 */
public class Queen {

    Bugpath bugpath;
    int contUnit;
    int mine_index;
    Messaging mes;
    UnitController uc;

    Direction[] directions = {
            Direction.NORTH, Direction.NORTHWEST, Direction.WEST, Direction.SOUTHWEST,
            Direction.SOUTH, Direction.SOUTHEAST, Direction.EAST, Direction.NORTHEAST
    };

    UnitType getType(){
        if (contUnit%6 != 1 && contUnit%6 != 3) return combatType(contUnit%12);
        int mine_number = uc.read(mes.FIRST_MINING_MESSAGE);
        if (mine_number == 0){
            ++contUnit;
            return getType();
        }
        boolean adv = false;
        int i = mine_index;
        for (; (!adv || mine_index != i) && uc.getEnergyUsed() < 8000; ){
            Target target2 = mes.getTarget(uc.read(mes.FIRST_MINING_MESSAGE + i + 1));
            if (!mes.isMineFree(target2.loc)){
                ++i;
                if (i >= mine_number) i = 0;
                continue;
            }
            return UnitType.ANT;
        }
        mine_index = i;

        ++contUnit;
        return getType();
    }

    UnitType defaultCombatType(int type){
        if (uc.getRound() < 250) {
            if (type == 0) return UnitType.BEE;
            else if (type == 2) return UnitType.BEETLE;
            else return UnitType.SPIDER;
        }
        else{
            if (type == 0 || type == 4) return UnitType.BEETLE;
            return UnitType.SPIDER;
        }
    }

    double getValue(double a, double b, double c, double A, double B, double C){
        double auxA = a, auxB = b, auxC = c;
        a -= (A*0.35 + B*0.6 + C*0.15);
        b -= (A*0.15 + B*0.35 + C*0.6);
        c -= (A*0.6 + B*0.15 + C*0.35);
        if (a < 0) a = 0;
        if (b < 0) b = 0;
        if (c < 0) c = 0;
        return -(a+b+c);
    }

    UnitType combatType(int type){
        if (contUnit < 6 || uc.getRound() < 250) {
            return defaultCombatType(type%6);
        }

        double enemySpiders = 0, enemyBeetles = 0, enemyBees = 0;
        for (int i = 2; i < mes.TROOP_MEMORY; ++i){
            int r = 10*((uc.getRound() + i)%mes.TROOP_MEMORY);
            enemySpiders += uc.read(mes.FIRST_ENEMY_TROOP_MESSAGE + r + mes.SPIDER);
            enemyBeetles += uc.read(mes.FIRST_ENEMY_TROOP_MESSAGE + r + mes.BEETLE);
            enemyBees += uc.read(mes.FIRST_ENEMY_TROOP_MESSAGE + r + mes.BEE);
        }

        double spiders = 0, beetles = 0, bees = 0;
        int r = 10*((uc.getRound() + mes.TROOP_MEMORY-1)%mes.TROOP_MEMORY);
        spiders = uc.read(mes.FIRST_TROOP_MESSAGE + r + mes.SPIDER);
        beetles = uc.read(mes.FIRST_TROOP_MESSAGE + r + mes.BEETLE);
        bees = uc.read(mes.FIRST_TROOP_MESSAGE + r + mes.BEE);

        if (enemySpiders + enemyBeetles + enemyBees < 10) return defaultCombatType(type%6);

        double ratio = (spiders+beetles+bees+1)/(enemySpiders+enemyBeetles+enemyBees);
        enemySpiders*=ratio;
        enemyBeetles*=ratio;
        enemyBees*=ratio;

        //uc.println(enemyBeetles + " " + enemySpiders + " " + enemyBees);
        //uc.println(beetles + " " + spiders + " " + bees);

        UnitType bestType = null;
        double bestValue = -100000000;
        double newValue = getValue(enemyBeetles, enemySpiders, enemyBees, beetles+1, spiders, bees);
        //uc.println("Beetle Value: " + newValue);
        if (newValue > bestValue){
            bestValue = newValue;
            bestType = UnitType.BEETLE;
        }
        newValue = getValue(enemyBeetles, enemySpiders, enemyBees, beetles, spiders+1, bees);
        //uc.println("Spider Value: " + newValue);
        if (newValue > bestValue){
            bestValue = newValue;
            bestType = UnitType.SPIDER;
        }
        newValue = getValue(enemyBeetles, enemySpiders, enemyBees, beetles, spiders, bees+1);
        //uc.println("Bee Value: " + newValue);
        if (newValue > bestValue){
            bestValue = newValue;
            bestType = UnitType.BEE;
        }
        if (bestType != null) return bestType;
        return defaultCombatType(type);
    }

    void cleanChannels(){
        int nextRoundMod = 10*((uc.getRound()+1)%mes.TROOP_MEMORY);
        for (int i = 0; i < 5; ++i){
            uc.write(nextRoundMod+i + mes.FIRST_ENEMY_TROOP_MESSAGE, 0);
            uc.write(nextRoundMod+i + mes.FIRST_TROOP_MESSAGE, 0);
        }
    }

    void run(UnitController _uc) {

        uc = _uc;
        bugpath = new Bugpath(uc);
        contUnit = 0;
        mine_index = 0;
        mes = new Messaging(uc);


        while (true) {

            if (uc.canMove()){
                bugpath.fightMove();
                bugpath.safeMove();


                if (uc.canMove()) {
                    int start = (int) Math.floor(Math.random() * 8);
                    for (int i = 0; i < 8; ++i) {
                        if (uc.canMove(directions[start])) uc.move(directions[start]);
                        start++;
                        if (start >= 8) start = 0;
                    }
                }
            }

            UnitType type = getType();

            for (int i = 0; i < 8; ++i) {
                if (uc.canSpawn(directions[i], type)) {
                    uc.spawn(directions[i], type);
                    ++contUnit;
                }
            }

            int cont = 0;
            UnitInfo bestUnit = null;
            int minDif = 1000;
            UnitInfo[] units = uc.senseUnits(uc.getType().getHealingRangeSquared());
            for (int i = 0; i < units.length; ++i){
                if (units[i].getHealth() < units[i].getType().getMaxHealth() && units[i].getType() != UnitType.QUEEN){
                    ++cont;
                    int dif = units[i].getType().getMaxHealth() - units[i].getHealth();
                    if (dif < minDif && uc.canHeal(units[i])){
                        minDif = dif;
                        bestUnit = units[i];
                    }
                }
            }
            if (bestUnit != null) uc.heal(bestUnit);

            if (cont < 4){
                uc.write(mes.FIRST_QUEEN_BUSY_CHANNEL + getMyIndex(), uc.getRound());
            }

            mes.putVisibleMines();

            cleanChannels();

            // End turn
            uc.yield();
        }
    }

    int getMyIndex(){
        Location[] queens = uc.getEnemyQueensLocation();
        if (queens.length <= 0) return 0;
        int bestQueen = -1;
        Location myLoc = uc.getLocation();
        int bestDist = 100000000;
        for (int i = 0; i < queens.length; ++i){
            int d = myLoc.distanceSquared(queens[i]);
            if (bestDist > d){
                bestQueen = i;
                bestDist = d;
            }
        }
        return bestQueen;
    }

}
