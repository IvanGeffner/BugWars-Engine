package cerebrus.antcomplete;

import navgame.*;
/**
 * Created by Ivan on 3/20/2018.
 */
public class Explore {

    int channel = 0;
    Messaging mes;
    UnitController uc;
    Location target;

    public Explore(UnitController uc, Messaging mes){
        this.mes = mes;
        this.uc = uc;
        getNewTarget();
    }

    public void getNewTarget() {
        target = uc.getTeam().getInitialLocations()[0];
        double angle = Math.random() * Math.PI * 2;
        target = target.add((int) (Math.cos(angle)*100.0), (int) (Math.sin(angle)*100.0));
    }

    public void checkTarget(int maxTimes){
        if (maxTimes == 0){
            target = randomTarget();
            return;
        }
        if (target == null || uc.getLocation().distanceSquared(target) <= 10){
            getNewTarget();
            checkTarget(maxTimes-1);
            return;
        }
        Direction dir = uc.getLocation().directionTo(target);
        if (dir == Direction.ZERO){
            getNewTarget();
            checkTarget(maxTimes-1);
            return;
        }
        if (isOK(dir)) return;
        getNewTarget();
        checkTarget(maxTimes-1);
    }

    public boolean isOK(Direction dir){
        Location newTarget = uc.getLocation().add(dir);
        if (uc.isOutOfMap(newTarget)) return false;
        int minDistAlly = 100000, minDistEnemy = 100000;
        Location[] allyLoc = uc.getTeam().getInitialLocations(), enemyLoc = uc.getTeam().getOpponent().getInitialLocations();
        for (Location loc : allyLoc){
            int dist = loc.distanceSquared(uc.getLocation());
            if (dist < minDistAlly) minDistAlly = dist;
        }
        for (Location loc : enemyLoc){
            int dist = loc.distanceSquared(uc.getLocation());
            if (dist < minDistEnemy) minDistEnemy = dist;
        }
        return minDistAlly <= Math.min(minDistEnemy, minDistEnemy/2 + 5);
    }

    public Location randomTarget(){
        Direction[] directions = Direction.values();
        int start = (int) Math.floor(Math.random() * 8);
        for (int i = 0; i < 8; ++i) {
            if (uc.canMove(directions[start])){
                Location ans = uc.getLocation();
                for (int j = 0; j < 10; ++j) ans = ans.add(directions[start]);
                return ans;
            }
            start++;
            if (start >= 8) start = 0;
        }
        return null;
    }

}
