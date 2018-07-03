package bugwars;

/**
 * Classe que representa les posicions al taulell en dos dimensions i totes les possibles operacions relacionades. La coordenada x creix cap a la
 * l'est (cap a la dreta a la pantalla) i la coordenada y creix cap al nord (cap a dalt a la pantalla). Per no donar informacio sobre els limits del
 * taulell, la posicio inferior esquerra comenca en un valor (x,y) per defecte, on x,y estan entre 0 i 1000.
 */
public class Location {
    /**
     * Coordenada x de la posicio.
     */
    public int x;

    /**
     * Coordenada y de la posicio.
     */
    public int y;

    /**
     * Constructor d'una nova Location. Inicialitza les coordenades als valors donats.
     * @param x_ Coordenada x.
     * @param y_ Coordenada y.
     */
    public Location(int x_, int y_) {
        x = x_;
        y = y_;
    }

    /**
     * Constructor buit. Inicialitza (x,y) a (0,0).
     */
    public Location () {
        x = 0;
        y = 0;
    }

    /**
     * Calcula la nova posicio despres de moure's en una certa direccio. Hi ha penalitzacio al passar una direccio null.  Costa 2 d'energia.
     * @param dir Direccio donada
     * @return Retorna la nova posicio despres de moure's en la direccio donada.
     */
    public Location add(Direction dir) {
        if (dir == null) {
            if (Game.printWarnings) System.err.println("Trying to add a null direction!");
            ThreadManager.punish();
            return this;
        }
        return new Location(x + dir.dx, y + dir.dy);
    }

    /**
     * Calcula la posicio despres de moure's des de la casella actual seguint el vector (_x,_y). Costa 2 d'energia.
     * @param _x Coordenada _x.
     * @param _y Coordenada _y.
     * @return Retorna la nova posicio despres de moure's seguint (_x,_y).
     */
    public Location add(int _x, int _y) {
        return new Location(x + _x, y + _y);
    }

    /**
     * Calcula la direccio necessaria per anar en linia recta a una altra posicio donada. Hi ha penalitzacio al passar una posicio null. Costa 5 d'energia.
     * @param loc Altra posicio.
     * @return Retorna la direccio cap a la nova posicio.
     */
    public Direction directionTo(Location loc) {
        if (loc == null) {
            if (Game.printWarnings) System.err.println("Trying to get the direction to a null Location!");
            ThreadManager.punish();
            return Direction.ZERO;
        }
        return Direction.getDirection(loc.x - x, loc.y - y);
    }

    /**
     * Calcula si la posicio actual es igual en coordenades a una altra posicio.  Costa 1 d'energia.
     * @param loc Una altra posicio.
     * @return Retorna true si i nomes si les dues posicions tenen les mateixes coordenades.
     */
    public boolean isEqual(Location loc) {
        if (loc == null) return false;
        if (x != loc.x) return false;
        if (y != loc.y) return false;
        return true;
    }

    /**
     * Calcula la distancia quadrada entre la posicio actual i una altra posicio donada. Hi ha penalitzacio al passar una posicio nul.la. Costa 5 d'energia.
     * @param loc Una altra posicio.
     * @return Retorna la distancia al quadrat entre la posicio actual i la donada.
     */
    public int distanceSquared(Location loc) {
        if (loc == null) {
            if (Game.printWarnings) System.err.println("Trying to take the distance to with a null Location!");
            ThreadManager.punish();
            return 0;
        }
        int dx = x - loc.x, dy = y-loc.y;
        return dx*dx + dy*dy;
    }

}
