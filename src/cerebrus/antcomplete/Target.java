package cerebrus.antcomplete;

import navgame.Location;
import navgame.UnitInfo;
import navgame.UnitType;

/**
 * Created by Ivan on 2/4/2018.
 */
public class Target {
    Location loc;
    int type;
    int value;
    Messaging mes;
    Integer rel = null;

    public Target(){
    }

    public Target(Location _loc, int _type, int _value, Messaging _mes){
        loc = _loc;
        type = _type;
        value = _value;
        mes = _mes;
    }

    public int encode(){
        return mes.encode(type, loc.x, loc.y, value);
    }

    public Target(Messaging _mes, UnitInfo u){
        mes = _mes;
        type = mes.ENEMY;
        loc = u.getLocation();
        if (u.getType() == UnitType.ANT) value = mes.ANT;
        else if (u.getType() == UnitType.BEETLE) value = mes.BEETLE;
        else if (u.getType() == UnitType.QUEEN) value = mes.QUEEN;
        else if (u.getType() == UnitType.BEE) value = mes.BEE;
        else value = mes.SPIDER;
        value = (value << 8) | u.getHealth();
    }

    int getRelation(UnitType type){
        if (rel != null) return rel;
        if (type == UnitType.BEETLE){
            if (this.type == mes.BEE) rel = 1;
            else if (this.type == mes.SPIDER) rel = -1;
        } else if (type == UnitType.BEE){
            if (this.type == mes.SPIDER) rel = 1;
            else if (this.type == mes.BEETLE) rel = -1;
        } else if (type == UnitType.SPIDER){
            if (this.type == mes.BEETLE) rel = 1;
            else if (this.type == mes.BEE) rel = -1;
        }
        if (rel == null) rel = 0;
        return rel;
    }

    boolean isBetterThan(Target B, Location myLoc, UnitType type){
        if (B == null) return true;
        int d1 = myLoc.distanceSquared(loc);
        int d2 = myLoc.distanceSquared(B.loc);

        if (d1 <= mes.attackRangeExpanded && d2 <= mes.attackRangeExpanded){
            int HP1 = value&0xFF;
            int HP2 = B.value&0xFF;
            return HP1 < HP2;
        }

        int r1 = getRelation(type);
        int r2 = B.getRelation(type);

        if (r1 > 0){
            d1 /= 2;
        }
        if (r1 < 0){
            d1 += 5;
            d1 *= 2;
        }

        if (r2 > 0){
            d2/=2;
        }
        if(r2 < 0){
            d2+=5;
            d2*=2;
        }

        return d1 < d2;

    }

}
