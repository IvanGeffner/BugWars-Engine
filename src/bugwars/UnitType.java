package bugwars;

/**
 * Classe que conte tots els tipus d'unitats del joc i les seves propietats.
 */
public enum UnitType {
    /**
     * Unitat de tipus reina.
     */
    QUEEN (null, GameConstants.QUEEN_MAX_HEALTH, GameConstants.QUEEN_COST, GameConstants.QUEEN_ATTACK_RANGE_SQUARED, GameConstants.MIN_QUEEN_ATTACK_RANGE_SQUARED, GameConstants.QUEEN_ATTACK, GameConstants.QUEEN_ATTACK_DELAY, GameConstants.QUEEN_SIGHT_RANGE_SQUARED, GameConstants.QUEEN_MOVEMENT_RANGE_SQUARED, GameConstants.QUEEN_MOVEMENT_DELAY, GameConstants.QUEEN_MINING_RANGE_SQUARED, GameConstants.QUEEN_HEALING_RANGE, GameConstants.QUEEN_HEALING_POWER),
    /**
     * Unitat de tipus formiga.
     */
    ANT (UnitType.QUEEN, GameConstants.ANT_MAX_HEALTH, GameConstants.ANT_COST, GameConstants.ANT_ATTACK_RANGE_SQUARED, GameConstants.MIN_ANT_ATTACK_RANGE_SQUARED, GameConstants.ANT_ATTACK, GameConstants.ANT_ATTACK_DELAY, GameConstants.ANT_SIGHT_RANGE_SQUARED, GameConstants.ANT_MOVEMENT_RANGE_SQUARED, GameConstants.ANT_MOVEMENT_DELAY, GameConstants.ANT_MINING_RANGE_SQUARED, GameConstants.ANT_HEALING_RANGE, GameConstants.ANT_HEALING_POWER),
    /**
     * Unitat de tipus escarabat.
     */
    BEETLE (UnitType.QUEEN, GameConstants.BEETLE_MAX_HEALTH, GameConstants.BEETLE_COST, GameConstants.BEETLE_ATTACK_RANGE_SQUARED, GameConstants.MIN_BEETLE_ATTACK_RANGE_SQUARED, GameConstants.BEETLE_ATTACK, GameConstants.BEETLE_ATTACK_DELAY, GameConstants.BEETLE_SIGHT_RANGE_SQUARED, GameConstants.BEETLE_MOVEMENT_RANGE_SQUARED, GameConstants.BEETLE_MOVEMENT_DELAY, GameConstants.BEETLE_MINING_RANGE_SQUARED, GameConstants.BEETLE_HEALING_RANGE, GameConstants.BEETLE_HEALING_POWER),
    /**
     * Unitat de tipus aranya.
     */
    SPIDER (UnitType.QUEEN, GameConstants.SPIDER_MAX_HEALTH, GameConstants.SPIDER_COST, GameConstants.SPIDER_ATTACK_RANGE_SQUARED, GameConstants.MIN_SPIDER_ATTACK_RANGE_SQUARED, GameConstants.SPIDER_ATTACK, GameConstants.SPIDER_ATTACK_DELAY, GameConstants.SPIDER_SIGHT_RANGE_SQUARED, GameConstants.SPIDER_MOVEMENT_RANGE_SQUARED, GameConstants.SPIDER_MOVEMENT_DELAY, GameConstants.SPIDER_MINING_RANGE_SQUARED, GameConstants.SPIDER_HEALING_RANGE, GameConstants.SPIDER_HEALING_POWER),
    /**
     * Unitat de tipus abella.
     */
    BEE (UnitType.QUEEN, GameConstants.BEE_MAX_HEALTH, GameConstants.BEE_COST, GameConstants.BEE_ATTACK_RANGE_SQUARED, GameConstants.MIN_BEE_ATTACK_RANGE_SQUARED, GameConstants.BEE_ATTACK, GameConstants.BEE_ATTACK_DELAY, GameConstants.BEE_SIGHT_RANGE_SQUARED, GameConstants.BEE_MOVEMENT_RANGE_SQUARED, GameConstants.BEE_MOVEMENT_DELAY, GameConstants.BEE_MINING_RANGE_SQUARED, GameConstants.BEE_HEALING_RANGE, GameConstants.BEE_HEALING_POWER),
    ;

    /**
     * Tipus d'unitat tal que pot crear aquest tipus d'unitat.
     */
    public UnitType spawner;
    /**
     * Vida maxima d'aquest tipus d'unitat
     */
    public final int maxHealth;
    /**
     * Cost de crear aquest tipus d'unitat en menjar.
     */
    public final int cost;
    /**
     * Quadrat del rang d'atac d'aquest tipus d'unitat.
     */
    public final int attackRangeSquared;
    /**
     * Quadrat del minim rang d'atac d'aquest tipus d'unitat.
     */
    public final int minAttackRangeSquared;
    /**
     * Poder d'atac d'aquest tipus d'unitat en punts de mal per atac.
     */
    public final float attack;
    /**
     * Cooldown d'atac d'aquest tipus d'unitat.
     */
    public final float attackDelay;
    /**
     * Quadrat del rang de visio d'aquest tipus d'unitat.
     */
    public final int sightRangeSquared;
    /**
     * Quadrat del rang de moviment d'aquest tipus d'unitat.
     */
    public final int movementRangeSquared;
    /**
     * Cooldown de moviment d'aquest tipus d'unitat.
     */
    public final float movementDelay;
    /**
     * Quadrat del rang on aquest tipus d'unitat pot minar.
     */
    public final int miningRangeSquared;
    /**
     * Quadrat del rang on aquest tipus d'unitat pot curar.
     */
    public final int healingRangeSquared;
    /**
     * Poder de curacio d'aquest tipus d'unitat en vida regenerada per torn.
     */
    public final int healingPower;


    UnitType (UnitType _spawner, int _maxHealth, int _cost, int _attackRangeSquared, int _minAttackRangeSquared, float _attack, float _attackDelay, int _sightRangeSquared, int _movementRangeSquared, float _movementDelay, int _miningRangeSquared, int _healingRangeSquared, int _healingPower){
        spawner = _spawner;
        maxHealth = _maxHealth;
        cost = _cost;
        attack = _attack;
        attackDelay = _attackDelay;
        attackRangeSquared = _attackRangeSquared;
        minAttackRangeSquared = _minAttackRangeSquared;
        sightRangeSquared = _sightRangeSquared;
        movementRangeSquared = _movementRangeSquared;
        movementDelay = _movementDelay;
        miningRangeSquared = _miningRangeSquared;
        healingPower = _healingPower;
        healingRangeSquared = _healingRangeSquared;
    }

    /**
     * Retorna si aquest tipus d'unitat pot atacar. Costa 1 d'energia.
     * @return Retorna si aquest tipus d'unitat pot atacar.
     */
    public boolean canAttack(){
        return (attack > 0);
    }

    /**
     * Retorna si aquest tipus t'unitat pot moure's. Costa 1 d'energia.
     * @return Retorna si aquest tipus d'unitat pot moure's.
     */
    public boolean canMove(){
        return (movementRangeSquared > 0);
    }

    /**
     * Retorna si aquest tipus d'unitat pot minar. Costa 1 d'energia.
     * @return Retorna si aquest tipus d'unitat pot minar.
     */
    public boolean canMine(){
        return (miningRangeSquared > 0);
    }

    /**
     * Retorna el poder d'atac d'aquest tipus d'unitat en punts de mal per atac. Costa 1 d'energia.
     * @return Retorna el poder d'atac d'aquest tipus d'unitat en punts de mal per atac.
     */
    public float getAttack(){
        return attack;
    }

    /**
     * Retorna el cooldown d'atac d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el cooldown d'atac d'aquest tipus d'unitat.
     */
    public float getAttackDelay() {
    	return attackDelay;
    }

    /**
     * Retorna el quadrat del rang d'atac d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el quadrat del rang d'atac d'aquest tipus d'unitat.
     */
    public int getAttackRangeSquared() {
        return attackRangeSquared;
    }

    /**
     * Retorna el quadrat del minim rang d'atac d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna quadrat del minim rang d'atac d'aquest tipus d'unitat.
     */
    public int getMinAttackRangeSquared() { return minAttackRangeSquared; }

    /**
     * Retorna la vida maxima d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna la vida maxima d'aquest tipus d'unitat.
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Retorna el cost de crear aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el cost de crear aquest tipus d'unitat.
     */
    public int getCost() {
        return cost;
    }

    /**
     * Retorna el tipus d'unitat tal que pot crear aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el tipus d'unitat tal que pot crear aquest tipus d'unitat.
     */
    public UnitType getSpawner() {
        return spawner;
    }

    /**
     * Retorna el quadrat del rang de visio d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el quadrat del rang de visio d'aquest tipus d'unitat.
     */
    public int getSightRangeSquared() {
        return sightRangeSquared;
    }

    /**
     * Retorna el quadrat del rang de moviment d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el quadrat del rang de moviment d'aquest tipus d'unitat.
     */
    public int getMovementRangeSquared() { return movementRangeSquared; }

    /**
     * Retorna el cooldown de moviment d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el cooldown de moviment d'aquest tipus d'unitat.
     */
    public float getMovementDelay() {
    	return movementDelay;
    }

    /**
     * Retorna el quadrat del rang on aquest tipus d'unitat pot minar. Costa 1 d'energia.
     * @return Retorna el quadrat del rang on aquest tipus d'unitat pot minar.
     */
    public int getMiningRangeSquared() {
        return miningRangeSquared;
    }

    /**
     * Retorna si aquest tipus d'unitat pot curar. Costa 1 d'energia.
     * @return Retorna si aquest tipus d'unitat pot curar.
     */
    public boolean canHeal(){
        return healingPower > 0;
    }

    /**
     * Retorna el quadrat del rang de curacio d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el quadrat del rang de curacio d'aquest tipus d'unitat.
     */
    public int getHealingRangeSquared(){ return healingRangeSquared; }

    /**
     * Retorna el poder de curacio d'aquest tipus d'unitat. Costa 1 d'energia.
     * @return Retorna el poder de curacio d'aquest tipus d'unitat.
     */
    public int getHealingPower(){ return healingPower; }

}
