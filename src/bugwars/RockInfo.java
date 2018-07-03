package bugwars;

/**
 * Classe que conte la informacio sobre una pedra al taulell.
 */
public class RockInfo {
    /**
     * Posicio de la pedra.
     */
    final Location location;
    /**
     * Resistencia actual de la pedra en punts de vida.
     */
    final int durability;
    /**
     * Resistencia original de la pedra en punts de vida.
     */
    final int initialDurability;

    RockInfo(int _durability, int _initialDurability, Location _location){
        location = _location;
        durability = _durability;
        initialDurability = _initialDurability;
    }

    /**
     * Retorna la resistencia actual de la pedra en punts de vida. Costa 1 d'energia.
     * @return Retorna la resistencia actual de la pedra en punts de vida.
     */
    public int getDurability() { return durability; }

    /**
     * Retorna la resistencia original de la pedra en punts de vida. Costa 1 d'energia.
     * @return Retorna la resistencia original de la pedra en punts de vida.
     */
    public int getInitialDurability() { return initialDurability; }

    /**
     * Retorna la posicio de la pedra. Costa 1 d'energia.
     * @return Retorna la posicio de la pedra.
     */
    public Location getLocation() { return location; }


}
