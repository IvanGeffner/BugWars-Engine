package bugwars;

/**
 * Classe que conte totes les direccions normalitzades en les quals les unitats es poden moure i totes les possibles operacions relacionades.
 */
public enum Direction {
    /**
     * Direccio nord.
     */
    NORTH(0,1),
    /**
     * Direccio nord-oest.
     */
    NORTHWEST(-1,1),
    /**
     * Direccio oest.
     */
    WEST(-1, 0),
    /**
     * Direccio sud-oest.
     */
    SOUTHWEST(-1, -1),
    /**
     * Direccio sud.
     */
    SOUTH(0, -1),
    /**
     * Direccio sud-est.
     */
    SOUTHEAST(1, -1),
    /**
     * Direccio est.
     */
    EAST(1, 0),
    /**
     * Direccio nord-est.
     */
    NORTHEAST(1, 1),
    /**
     * Direccio zero.
     */
    ZERO(0,0);

    /**
     * Coordenada dx del vector que representa la direccio.
     */
    public final int dx;

    /**
     * Coordenada dy del vector que representa la direccio.
     */
    public final int dy;

    Direction(int x_, int y_) {
        dx = (int) Math.signum(x_);
        dy = (int) Math.signum(y_);
    }

    /**
     * Indica si aquesta direccio es igual a una direccio donada.
     * Costa 1 d'energia.
     * @param dir Una altra direccio.
     * @return Retorna true si la direccio donada es igual a aquesta.
     */
    public boolean isEqual(Direction dir) {
        if (dir == null) {
            return false;
        }
        return (dir.dx == dx && dir.dy == dy);
    }

    /**
     * Calcula la direccio obtinguda de rotar la direccio actual cap a l'esquerra 45 graus.
     * Costa 1 d'energia.
     * @return Retorna la direccio obtinguda despres de rotar.
     */
    public Direction rotateLeft() {
        if (ordinal() == 8) return this;
        if (ordinal() == 7) return Direction.values()[0];
        return Direction.values()[ordinal()+1];
    }

    /**
     * Calcula la direccio obtinguda de rotar la direccio actual cap a la dreta 45 graus. Costa 1 d'energia.
     * @return Retorna la direccio obtinguda despres de rotar.
     */
    public Direction rotateRight() {
        if (ordinal() == 8) return this;
        if (ordinal() == 0) return Direction.values()[7];
        return Direction.values()[ordinal()-1];
    }

    /**
     * Retorna la direccio obtinguda mitjancant un canvi de sentit (180 graus). Costa 1 d'energia.
     * @return Retorna la direccio actual en sentit contrari.
     */
    public Direction opposite() {
        if (ordinal() == 8) return this;
        if (ordinal() >= 4) return Direction.values()[ordinal()-4];
        return (Direction.values()[ordinal()+4]);
    }

    /**
     * Calcula la norma euclidiana de la direccia. Les quatre direccions principals tenen norma 1, mentres que les diagonals tenen de norma arrel de 2.  Costa 2 d'energia.
     * @return Retorna la longitud de la direccio.
     */
    public float length() {
        return (float)Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Construeix la direccio normalitzada optima per anar en direccio (dx,dy). Costa 5 d'energia.
     * @param dx Coordenada dx
     * @param dy Coordenada dy
     * @return Retorna la direccio obtinguda normalitzant (dx,dy).
     */
    public static Direction getDirection(int dx, int dy){
        dx = (int)Math.signum(dx);
        dy = (int)Math.signum(dy);
        if (dx == 0 && dy == 1) return NORTH;
        if (dx == -1 && dy == 1) return NORTHWEST;
        if (dx == -1 && dy == 0) return WEST;
        if (dx == -1 && dy == -1) return SOUTHWEST;
        if (dx == 0 && dy == -1) return SOUTH;
        if (dx == 1 && dy == -1) return SOUTHEAST;
        if (dx == 1 && dy == 0) return EAST;
        if (dx == 1 && dy == 1) return NORTHEAST;
        return ZERO;
    }

}
