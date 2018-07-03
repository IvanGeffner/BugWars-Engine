package bugwars;

/**
 * Classe que conte la informacio sobre el menjar que hi ha en una casella.
 */
public class FoodInfo {
    public final int food;
    public final Location location;
    public final int initialFood;

    FoodInfo (int _food, int _initialFood, Location loc) {
        food = _food;
        initialFood = _initialFood;
        location = loc;
    }

    /**
     * Retorna la quantitat actual de menjar a la casella. Costa 1 d'energia.
     * @return Retorna la quantitat actual de menjar a la casella.
     */
    public int getFood() {
        return food;
    }

    /**
     * Retorna la casella on esta el menjar. Costa 1 d'energia.
     * @return Retorna la casella associada a aquest menjar.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Dona la quantitat inicial de menjar que hi havia a la casella. Si aquest valor es positiu el menjar es va regenerant cada torn fins arribar a aquest valor. Costa 1 d'energia.
     * @return Retorna la quantitat inicial de menjar en aquesta casella.
     */
    public int getInitialFood(){
        return initialFood;
    }

}
