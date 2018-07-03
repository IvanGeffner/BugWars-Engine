package bugwars;

/**
 * Classe que conte tota la informacio sobre una unitat a la partida. Les variables d'aquesta classe son immutables, i per tant no s'actualitzen immediatament despres d'efectuar una accio. Per exemple,
 * al atacar una unitat enemiga, la seva vida no s'actualita en aquesta classe (es fa internament). Aixi que es recomanable demanar aquesta informacio despres d'efectuar una accio.
 */
public class UnitInfo {

    /**
     * L'equip de la unitat.
     */
    final private Team team;
    /**
     * Tipus de la unitat.
     */
    final private UnitType type;
    /**
     * Vida actual de la unitat.
     */
    final private int health;
    /**
     * Posicio actual de la unitat.
     */
    final private Location location;
    /**
     * Cooldown de moviment actual de la unitat.
     */
    final private float movementCooldown;
    /**
     * Cooldown d'atac actual de la unitat.
     */
    final private float attackCooldown;

    /**
     * Nombre d'identificacio de la unitat.
     */
    final private int id;

    /**
     * Nombre de torns de capoll restants de la unitat.
     */
    final private int cocoonTurns;

    UnitInfo(Unit unit) {
    	team = unit.getTeam();
    	type = unit.getType();
        health = unit.getHealth();
        location = unit.getGameLocation().toLocation();
        movementCooldown = unit.getMoveCooldown();
        attackCooldown = unit.getAttackCooldown();
        id = unit.getUnitId();
        cocoonTurns = unit.cocoonTurnsLeft;
    }

    /**
     * Retorna l'equip de la unitat. Costa 1 d'energia.
     * @return Retorna l'equip de la unitat.
     */
    public Team getTeam() { return team; }

    /**
     * Retorna el tipus de la unitat. Costa 1 d'energia.
     * @return Retorna el tipus de la unitat.
     */
    public UnitType getType() { return type; }

    /**
     * Retorna la vida actual de la unitat. Costa 1 d'energia.
     * @return Retorna la vida actual de la unitat.
     */
    public int getHealth() { return health; }

    /**
     * Retorna la posicio actual de la unitat. Costa 1 d'energia.
     * @return Retorna la posicio actual de la unitat.
     */
    public Location getLocation() { return location; }

    /**
     * Retorna el cooldown de moviment actual de la unitat. Costa 1 d'energia.
     * @return Retorna el cooldown de moviment actual de la unitat.
     */
    public float getMovementCooldown() { return movementCooldown; }

    /**
     * Retorna el cooldown d'atac actual de la unitat. Costa 1 d'energia.
     * @return Retorna el cooldown d'atac actual de la unitat.
     */
    public float getAttackCooldown() {return attackCooldown; }

    /**
     * Retorna el nombre d'identificacio de la unitat. Costa 1 d'energia.
     * @return Retorna el nombre d'identificacio de la unitat.
     */
    public int getID() { return id; }

    /**
     * Retorna si la unitat es encara un capoll.
     * @return Retorna si la unitat es encara un capoll.
     */
    public boolean isCocoon(){
        return cocoonTurns > 0;
    }

    /**
     * Retorna el nombre de torns de capoll restants de la unitat.
     * @return Retorna el nombre de torns de capoll restants de la unitat.
     */
    public int getCocoonTurns(){
        return cocoonTurns;
    }

}
