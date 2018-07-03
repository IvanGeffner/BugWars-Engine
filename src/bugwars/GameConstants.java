package bugwars;

/**
 * Classe que conte totes les constants del joc, a excepcio dels cost en bytecode de cada funcio.
 */
public class GameConstants {
    /**
     * Recursos amb els que cada equip comenca el joc.
     */
    public static final int INITIAL_RESOURCES = 200;
    /**
     * Recursos que guanya passivament cada equip al comencament de cada ronda.
     */
    public static final int RESOURCES_TURN = 10;
    /**
     * Recursos que obte un equip quan una formiga mina menjar.
     */
    public static final int ANT_MINING = 3;
    /**
     * Torns que triga una unitat des de que es crea fins que es pot controllar.
     */
    public static final int COCOON_TURNS = 10;
    /**
     * Quantitat de menjar que es regenera cada torn a cada mina.
     */
    public static final int FOOD_REGENERATION = 1;



    /**
     * Cost per crear una reina. Aquesta constant es irrellevant ja que les reines no es poden crear.
     */
    public static final int QUEEN_COST = 0;
    /**
     * Vida maxima d'una reina.
     */
    public static final int QUEEN_MAX_HEALTH = 250;
    /**
     * Quadrat del rang d'atac d'una reina. Si aquest valor es 0 vol dir que la reina no pot atacar.
     */
    public static final int QUEEN_ATTACK_RANGE_SQUARED = 0;
    /**
     * Quadrat del rang d'atac minim d'una reina.
     */
    public static final int MIN_QUEEN_ATTACK_RANGE_SQUARED = 0;
    /**
     * Punts de mal que fa una reina d'un atac.
     */
    public static final int QUEEN_ATTACK = 0;
    /**
     * Torns que ha d'esperar una reina per atacar despres d'haver atacat.
     */
    public static final float QUEEN_ATTACK_DELAY = 1.0f;
    /**
     * Quadrat del rang de visio d'una reina.
     */
    public static final int QUEEN_SIGHT_RANGE_SQUARED = 36;
    /**
     * Quadrat del rang de moviment d'una reina.
     */
    public static final int QUEEN_MOVEMENT_RANGE_SQUARED = 2;
    /**
     * Torns que ha d'esperar una reina per moure's despres d'haver-se mogut. Aquesta constant es multiplica per arrel de 2 (~1.4142) si es mou en diagonal.
     */
    public static final float QUEEN_MOVEMENT_DELAY = 3.0f;
    /**
     * Rang on una reina pot minar menjar. Si aquest valor es 0 vol dir que una reina no pot minar.
     */
    public static final int QUEEN_MINING_RANGE_SQUARED = 0;
    /**
     * Rang on una reina pot curar.
     */
    public static final int QUEEN_HEALING_RANGE = 9;
    /**
     * Vida que una reina pot curar a una unitat.
     */
    public static final int QUEEN_HEALING_POWER = 1;


    /**
     * Quantitat de menjar necessaria per crear una formiga.
     */
    public static final int ANT_COST = 150;
    /**
     * Vida maxima d'una formiga.
     */
    public static final int ANT_MAX_HEALTH = 15;
    /**
     * Quadrat del rang d'atac d'una formiga.
     */
    public static final int ANT_ATTACK_RANGE_SQUARED = 5;
    /**
     * Quadrat del rang d'atac minim d'una formiga.
     */
    public static final int MIN_ANT_ATTACK_RANGE_SQUARED = 0;
    /**
     * Punts de mal que fa una formiga d'un atac.
     */
    public static final int ANT_ATTACK = 1;
    /**
     * Torns que ha d'esperar una formiga per atacar despres d'haver atacat.
     */
    public static final float ANT_ATTACK_DELAY = 2f;
    /**
     * Quadrat del rang de visio d'una formiga.
     */
    public static final int ANT_SIGHT_RANGE_SQUARED = 24;
    /**
     * Quadrat del rang de moviment d'una formiga.
     */
    public static final int ANT_MOVEMENT_RANGE_SQUARED = 2;
    /**
     * Torns que ha d'esperar una formiga per moure's despres d'haver-se mogut. Aquesta constant es multiplica per arrel de 2 (~1.4142) si es mou en diagonal.
     */
    public static final float ANT_MOVEMENT_DELAY = 2.0f;
    /**
     * Rang on una formiga pot minar menjar.
     */
    public static final int ANT_MINING_RANGE_SQUARED = 2;
    /**
     * Rang on una formiga pot curar. Si aquest valor es 0 vol dir que una formiga no pot curar.
     */
    public static final int ANT_HEALING_RANGE = 0;
    /**
     * Vida que una formiga pot curar a una unitat.
     */
    public static final int ANT_HEALING_POWER = 0;

    /**
     * Quantitat de menjar necessaria per crear un escarabat.
     */
    public static final int BEETLE_COST = 200;
    /**
     * Vida maxima d'un escarabat.
     */
    public static final int BEETLE_MAX_HEALTH = 45;
    /**
     * Quadrat del rang d'atac d'un escarabat.
     */
    public static final int BEETLE_ATTACK_RANGE_SQUARED = 5;
    /**
     * Quadrat del rang d'atac minim d'un escarabat.
     */
    public static final int MIN_BEETLE_ATTACK_RANGE_SQUARED = 0;
    /**
     * Punts de mal que fa un escarabat.
     */
    public static final int BEETLE_ATTACK = 4;
    /**
     * Torns que ha d'esperar un escarabat per atacar despres d'haver atacat.
     */
    public static final float BEETLE_ATTACK_DELAY = 2f;
    /**
     * Quadrat del rang de visio d'un escarabat.
     */
    public static final int BEETLE_SIGHT_RANGE_SQUARED = 24;
    /**
     * Quadrat del rang de moviment d'un escarabat.
     */
    public static final int BEETLE_MOVEMENT_RANGE_SQUARED = 2;
    /**
     * Torns que ha d'esperar un escarabat per moure's despres d'haver-se mogut. Aquesta constant es multiplica per arrel de 2 (~1.4142) si es mou en diagonal.
     */
    public static final float BEETLE_MOVEMENT_DELAY = 2.0f;
    /**
     * Rang on un escarabat pot minar menjar. Si aquest valor es 0 vol dir que un escarabat no pot minar.
     */
    public static final int BEETLE_MINING_RANGE_SQUARED = 0;
    /**
     * Rang on un escarabat pot curar. Si aquest valor es 0 vol dir que un escarabat no pot curar.
     */
    public static final int BEETLE_HEALING_RANGE = 0;
    /**
     * Vida que un escarabat pot curar a una unitat.
     */
    public static final int BEETLE_HEALING_POWER = 0;


    /**
     * Quantitat de menjar necessaria per crear una aranya.
     */
    public static final int SPIDER_COST = 280;
    /**
     * Vida maxima d'una aranya.
     */
    public static final int SPIDER_MAX_HEALTH = 10;
    /**
     * Quadrat del rang d'atac d'una aranya.
     */
    public static final int SPIDER_ATTACK_RANGE_SQUARED = 18;
    /**
     * Quadrat del rang d'atac minim d'una aranya.
     */
    public static final int MIN_SPIDER_ATTACK_RANGE_SQUARED = 9;
    /**
     * Punts de mal que fa una aranya d'un atac.
     */
    public static final int SPIDER_ATTACK = 3;
    /**
     * Torns que ha d'esperar una aranya per atacar despres d'haver atacat.
     */
    public static final float SPIDER_ATTACK_DELAY = 2f;
    /**
     * Quadrat del rang de visio d'una aranya.
     */
    public static final int SPIDER_SIGHT_RANGE_SQUARED = 32;
    /**
     * Quadrat del rang de moviment d'una aranya.
     */
    public static final int SPIDER_MOVEMENT_RANGE_SQUARED = 2;
    /**
     * Torns que ha d'esperar una aranya per moure's despres d'haver-se mogut. Aquesta constant es multiplica per arrel de 2 (~1.4142) si es mou en diagonal.
     */
    public static final float SPIDER_MOVEMENT_DELAY = 2.0f;
    /**
     * Rang on una aranya pot minar menjar. Si aquest valor es 0 vol dir que una aranya no pot minar.
     */
    public static final int SPIDER_MINING_RANGE_SQUARED = 0;
    /**
     * Rang on una aranya pot curar. Si aquest valor es 0 vol dir que una aranya no pot curar.
     */
    public static final int SPIDER_HEALING_RANGE = 0;
    /**
     * Vida que una aranya pot curar a una unitat.
     */
    public static final int SPIDER_HEALING_POWER = 0;


    /**
     * Quantitat de menjar necessaria per crear una abella.
     */
    public static final int BEE_COST = 300;
    /**
     * Vida maxima d'una abella.
     */
    public static final int BEE_MAX_HEALTH = 100;
    /**
     * Quadrat del rang d'atac d'una abella.
     */
    public static final int BEE_ATTACK_RANGE_SQUARED = 5;
    /**
     * Quadrat del rang d'atac minim d'una abella.
     */
    public static final int MIN_BEE_ATTACK_RANGE_SQUARED = 0;
    /**
     * Punts de mal que fa una abella d'un atac.
     */
    public static final int BEE_ATTACK = 1;
    /**
     * Torns que ha d'esperar una abella per atacar despres d'haver atacat.
     */
    public static final float BEE_ATTACK_DELAY = 1f;
    /**
     * Quadrat del rang de visio d'una abella.
     */
    public static final int BEE_SIGHT_RANGE_SQUARED = 36;
    /**
     * Quadrat del rang de moviment d'una abella.
     */
    public static final int BEE_MOVEMENT_RANGE_SQUARED = 2;
    /**
     * Torns que ha d'esperar una abella per moure's despres d'haver-se mogut. Aquesta constant es multiplica per arrel de 2 (~1.4142) si es mou en diagonal.
     */
    public static final float BEE_MOVEMENT_DELAY = 1f;
    /**
     * Rang on una abella pot minar menjar. Si aquest valor es 0 vol dir que una abella no pot minar.
     */
    public static final int BEE_MINING_RANGE_SQUARED = 0;
    /**
     * Rang on una abella pot curar. Si aquest valor es 0 vol dir que una abella no pot curar.
     */
    public static final int BEE_HEALING_RANGE = 0;
    /**
     * Vida que una abella pot curar a una unitat.
     */
    public static final int BEE_HEALING_POWER = 0;

    /**
     * Quantitat de bytecode maxima que pot fer cada unitat a cada ronda.
     */
    public static final int MAX_BYTECODES = 15000;

    /**
     * Quantitat de bytecode afegit per penalitzacio quan salta una excepcio.
     */
    public static final int EXCEPTION_BYTECODE_PENALTY = 500;

    /**
     * Quantitat de rondes maxima en una partida.
     */
	public static final int MAX_TURNS = 3000;

    /**
     * Longitud del vector de comunicacio de cada equip.
     */
    public static final int TEAM_ARRAY_SIZE = 100000;

    /**
     * Maxima grandaria d'un mapa. Es garanteix que tots els mapes del joc tindran una alcada i una amplitud menor o igual a aquesta constant.
     */
    public static final int MAX_MAP_SIZE = 80;

    /**
     * Minima grandaria d'un mapa. Es garanteix que tots els mapes del joc tindran una alcada i una amplitud major o igual a aquesta constant.
     */
    public static final int MIN_MAP_SIZE = 20;

    /**
     * Maxim numero d'identificacio que pot tenir una unitat.
     */
    public static final int MAX_ID = 10000;

}
