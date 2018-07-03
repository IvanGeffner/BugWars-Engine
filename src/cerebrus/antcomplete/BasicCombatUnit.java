package cerebrus.antcomplete;

import navgame.*;

import java.util.HashSet;

/**
 * Created by Ivan on 2/14/2018.
 */
public class BasicCombatUnit {

    Target bestTarget;
    Location bestTargetLocation;
    int lastMessageRead;
    Messaging mes;
    UnitController uc;
    Explore explore;
    boolean hurt;

    BasicCombatUnit(UnitController _uc){
        uc = _uc;
        mes = new Messaging(_uc);
        lastMessageRead = uc.read(mes.MAX_CYCLE);
        explore = new Explore(uc, mes);
        hurt = false;
    }

    void getBestTarget(){
        bestTarget = null;
        Location myLoc = uc.getLocation();
        if (uc.getInfo().getHealth() <= Math.min(uc.getType().getMaxHealth()/2, 15)) hurt = true;
        else if (uc.getInfo().getHealth() >= uc.getType().getMaxHealth()) hurt = false;

        HashSet<Integer> readMessages = new HashSet<>();
        while (lastMessageRead != uc.read(mes.MAX_CYCLE) && uc.getEnergyUsed() < 2500){
            int code = uc.read(lastMessageRead);
            readMessages.add(code);
            Target newTarget = mes.getTarget(code);
            if (newTarget.isBetterThan(bestTarget, myLoc, uc.getType())) bestTarget = newTarget;
            lastMessageRead++;
            if (lastMessageRead >= mes.MAX_CYCLE) lastMessageRead = 0;
        }

        UnitInfo[] enemies = uc.senseUnits(uc.getTeam().getOpponent());

        for (int i = 0; i < enemies.length && uc.getEnergyUsed() < 4000; ++i){
            if (uc.isObstructed(myLoc, enemies[i].getLocation())) continue;
            Target newTarget = new Target(mes, enemies[i]);
            if (newTarget.isBetterThan(bestTarget, myLoc, uc.getType())) bestTarget = newTarget;
            int code = newTarget.encode();
            if (!readMessages.contains(code)) {
                mes.sendMessage(code);
                reportUnit(enemies[i].getType(), true);
            }
        }
        reportUnit(uc.getType(), false);
        lastMessageRead = uc.read(mes.MAX_CYCLE);
        if (!hurt) {
            if (bestTarget == null && uc.getRound() < 500) {
                explore.checkTarget(3);
                bestTargetLocation = explore.target;
            } else if (bestTarget != null) bestTargetLocation = bestTarget.loc;
        } else{
            bestTargetLocation = getClosestQueen(false);
            if (uc.getLocation().distanceSquared(bestTargetLocation) <= 5) bestTargetLocation = uc.getLocation();
        }
    }

    void reportUnit(UnitType type, boolean enemy){
        int channel = 10*(uc.getRound()%mes.TROOP_MEMORY);
        if (type == UnitType.BEETLE) {
            channel += mes.BEETLE;
        } else if (type == UnitType.BEE){
            channel += mes.BEE;
        } else if (type == UnitType.SPIDER){
            channel += mes.SPIDER;
        }
        if (enemy) channel += mes.FIRST_ENEMY_TROOP_MESSAGE;
        else channel += mes.FIRST_TROOP_MESSAGE;
        int mes = uc.read(channel);
        uc.write(channel, mes+1);
    }

    void tryAttack(){
        if (!uc.canAttack()) return;
        int bestIndex = -1;
        int leastHP = 1000;
        UnitInfo[] units = uc.senseUnits(uc.getType().getAttackRangeSquared(), uc.getTeam().getOpponent());
        for (int i = 0; i < units.length; ++i) {
            if(!uc.canAttack(units[i])) continue;
            if (units[i].getHealth() < leastHP){
                leastHP = units[i].getHealth();
                bestIndex = i;
            }
        }
        if (bestIndex >= 0) uc.attack(units[bestIndex]);
    }

    void tryAttackRock(){
        if (!uc.canAttack()) return;
        int bestIndex = -1;
        int leastDurability = 10000;
        RockInfo[] rocks = uc.senseObstacles(uc.getType().getAttackRangeSquared());
        for (int i = 0; i < rocks.length; ++i) {
            if(!uc.canAttack(rocks[i])) continue;
            if (rocks[i].getDurability() < leastDurability && rocks[i].getDurability() > 0){
                leastDurability = rocks[i].getDurability();
                bestIndex = i;
            }
        }
        if (bestIndex >= 0) uc.attack(rocks[bestIndex]);
    }

    Location getClosestQueen(boolean enemy){
        Location[] queens;
        if (enemy) queens = uc.getEnemyQueensLocation();
        else queens = uc.getMyQueensLocation();
        if (queens.length <= 0) return null;
        int bestQueen = -1;
        Location myLoc = uc.getLocation();
        int bestDist = 100000000;
        for (int i = 0; i < queens.length; ++i){
            if (!enemy && uc.getType() != UnitType.SPIDER && busy(i)) continue;
            int d = myLoc.distanceSquared(queens[i]);
            if (bestDist > d){
                bestQueen = i;
                bestDist = d;
            }
        }
        if (bestQueen >= 0) return queens[bestQueen];
        return null;
    }

    boolean busy(int i){
        return (uc.getRound() - uc.read(mes.FIRST_QUEEN_BUSY_CHANNEL + i) >= 2);
    }

}
