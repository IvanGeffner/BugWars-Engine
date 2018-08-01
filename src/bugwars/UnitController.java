package bugwars;

import java.util.ArrayList;

/**
 * Classe associada a una unitat que conte totes les funcions per donar ordres com atacar o moure's i per accedir a les altres classes intrinsiques de la unitat, es la classe mes important del joc. Aquesta classe tambe conte
 * totes les instruccions per recollir informacio visible al mapa (altres unitats, menjar o obstacles).
 */
public class UnitController {

	private Unit unit;
	private World world;

	UnitController(Unit _unit) {
		unit = _unit;
		world = Game.getInstance().world;
	}

	/**
	 * Retorna la informacio de la unitat associada a aquest controlador. Costa 1 d'energia.
	 * @return Retorna la informacio de la unitat associada a aquest controlador.
	 */
	public UnitInfo getInfo() { return unit.toUnitInfo(); }

	/**
	 * Retorna la posicio de la unitat associada a aquest controlador. Costa 1 d'energia.
	 * @return Retorna la posicio de la unitat associada a aquest controlador.
	 */
	public Location getLocation() { return unit.getGameLocation().toLocation(); }

	/**
	 * Retorna el tipus de la unitat associada a aquest controlador. Costa 1 d'energia.
	 * @return Retorna el tipus de la unitat associada a aquest controlador.
	 */
	public UnitType getType() { return unit.getType(); }

	/**
	 * Retorna l'equip de la unitat associada a aquest controlador. Costa 1 d'energia.
	 * @return Retorna l'equip de la unitat associada a aquest controlador.
	 */
	public Team getTeam() { return unit.getTeam(); }

	/**
	 * Retorna l'equip rival de la unitat associada a aquest controlador. Costa 1 d'energia.
	 * @return Retorna l'equip rival de la unitat associada a aquest controlador.
	 */
	public Team getOpponent() { return getTeam().getOpponent(); }

	/**
	 * Retorna si la unitat associada a aquest controlador ja ha creat una unitat aquest torn. Costa 1 d'energia.
	 * @return Retorna true si la unitat associada a aquest controlador ja ha creat una unitat aquest torn.
	 */
	public boolean hasSpawned() { return unit.hasSpawned(); }

	/**
	 * Retorna si la unitat associada a aquest control/lador es pot moure aquest torn. Costa 1 d'energia.
	 * @return Retorna true si la unitat associada a aquest controlador es pot moure aquest torn.
	 */
	public boolean canMove() { return getType().canMove() && unit.canMove(); }

	/**
	 * Retorna si la unitat associada a aquest control/lador pot atacar aquest torn. En cas que la unitat no pugui atacar pel tipus, aquesta funcio sempre retorna false. Costa 5 d'energia.
	 * @return Retorna true si la unitat associada a aquest controlador pot atacar aquest torn.
	 */
	public boolean canAttack() { return getType().canAttack() && unit.canAttack(); }

	/**
	 * Retorna si la unitat associada a aquest control/lador pot minar menjar aquest torn. En cas que la unitat no pugui minar pel tipus, aquesta funcio sempre retorna false. Costa 5 d'energia.
	 * @return Retorna true si la unitat associada a aquest controlador pot minar menjar aquest torn.
	 */
	public boolean canMine() { return getType().canMine() && !unit.hasMined(); }

	/**
	 * Retorna si la unitat associada a aquest control/lador pot curar aquest torn. En cas que la unitat no pugui curar pel tipus, aquesta funcio sempre retorna false. Costa 5 d'energia.
	 * @return Retorna true si la unitat associada a aquest controlador pot curar aquest torn.
	 */
	public boolean canHeal() { return getType().canHeal() && !unit.hasHealed(); }


	void sanityCheck() {
		if (this != unit.unitManager.getCurrentUnit().getUnitController()){
			if (Game.printWarnings) System.err.println("Player " + unit.getTeam().packageName + " is cheating!");
			world.setWinner(unit.getTeam().getOpponent(), WinCondition.CHEATING);
			yield();
		}
	}

	/**
	 * Retorna si la unitat associada a aquest controlador es pot moure en la direccio donada. Hi ha penalitzacio si la direccio donada es nula. Costa 5 d'energia.
	 * @param direction Direccio objectiu.
	 * @return Retorna true si la unitat associada a aquest controlador es pot moure en la direccio donada.
	 */
	public boolean canMove(Direction direction) {
		sanityCheck();
		if (direction == null) {
			if (Game.printWarnings) System.err.println("Cannot move in a null direction!");
			ThreadManager.punish();
			return false;
		}
		if (!getType().canMove()) return false;
		if (!unit.canMove()) return false;
		if (direction == Direction.ZERO) return true;

		return world.isAccessible(unit.getGameLocation().add(direction));
	}

	/**
	 * Ordena la unitat associada a aquest controlador que es mogui en la direccio donada. Si la unitat no es pot moure en aquesta direccio o si la direccio es nula hi ha penalitzacio. Costa 5 d'energia.
	 * @param direction Direccio de moviment.
	 */
    public void move(Direction direction) {
		sanityCheck();
		if (direction == null) {
			if (Game.printWarnings) System.err.println("Trying to move in a null direction!");
			ThreadManager.punish();
			return;
		}
		if (!getType().canMove()) {
			if (Game.printWarnings) System.err.println("Unit can't move anymore!!");
			ThreadManager.punish();
			return;
		}
		if (!canMove(direction)) {
			if (Game.printWarnings) System.err.println("Unit can't move to destination!!");
			ThreadManager.punish();
			return;
		}
		unit.move(direction);
		if (direction == Direction.ZERO) return;
		world.moveUnit(unit, direction);
	}

	private boolean isObstructed(GameLocation loc1, GameLocation loc2){
		VisibleCells visibleCells = VisibleCells.getInstance();
		int dx = loc2.x - loc1.x + visibleCells.center, dy = loc2.y - loc1.y + visibleCells.center;
		for (int i = 0; i < visibleCells.offsetX[dx][dy].length; ++i){
			GameLocation gloc = loc1.add(visibleCells.offsetX[dx][dy][i], visibleCells.offsetY[dx][dy][i]);
			if (gloc.isEqual(loc2) || gloc.isEqual(loc1)) continue;
			if (world.get(gloc).isWall()) return true;
		}
		return false;
	}

	/**
	 * Retorna si hi ha algun obstacle al taulell que intersequi interiorment el segment entre les posicions donades. Si una de les posicions esta fora del mapa o si nomes hi ha obstacles als extrems retorna false. Costa 5 d'energia.
	 * @param loc1 Primera posicio.
	 * @param loc2 Segona posicio.
	 * @return Retorna true si hi ha algun obstacle al taulell que intersequi interiorment el segment entre les posicions donades.
	 */
	public boolean isObstructed(Location loc1, Location loc2){
		if (loc1 == null || loc2 == null){
			ThreadManager.punish();
			return true;
		}
		GameLocation gloc1 = new GameLocation(loc1), gloc2 = new GameLocation(loc2);
		if (!canSenseLocation(loc1) || !canSenseLocation(loc2)){
			if (Game.printWarnings) System.out.println("Called isObstructed on a location which cannot be sensed");
			ThreadManager.punish();
			return true;
		}
		if (world.outOfMap(gloc1) || world.outOfMap(gloc2)) return true;
		return isObstructed(gloc1, gloc2);
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot atacar la posicio donada. Si el segment esta obstruit, la unitat esta fora de rang o la posicio esta fora del mapa retorna false. Si la posicio donada es null hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna si la unitat associada a aquest controlador pot atacar la posicio donada.
	 */
	public boolean canAttack(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to attack a null location!");
			ThreadManager.punish();
			return false;
		}

		GameLocation gloc = new GameLocation(loc);

		if (!canAttack()) return false;
		if (world.outOfMap(gloc)) return false;
		if (gloc.distanceSquared(unit.getGameLocation()) > getType().getAttackRangeSquared() ||
				gloc.distanceSquared(unit.getGameLocation()) < getType().getMinAttackRangeSquared()) return false;

		return !isObstructed(unit.getGameLocation(), gloc);
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot atacar la unitat objectiu. Retorna true si i nomes si pot atacar la posicio de la unitat objectiu. Si la unitat donada es null hi ha penalitzacio. Costa 5 d'energia.
	 * @param targetUnitInfo Unitat objectiu.
	 * @return Retorna true si la unitat associada a aquest controlador pot atacar la unitat objectiu.
	 */
	public boolean canAttack(UnitInfo targetUnitInfo) {
		sanityCheck();
		if (targetUnitInfo == null){
			if (Game.printWarnings) System.err.println("Trying to attack a null unit!");
			ThreadManager.punish();
			return false;
		}
		return canAttack(targetUnitInfo.getLocation());
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot atacar la pedra objectiu. Retorna true si i nomes si pot atacar la posicio de la pedra objectiu. Si ;a\[edra donada es null hi ha penalitzacio. Costa 5 d'energia.
	 * @param targetRockInfo Pedra objectiu.
	 * @return Retorna true si la unitat associada a aquest controlador pot atacar la pedra objectiu.
	 */
	public boolean canAttack(RockInfo targetRockInfo){
		sanityCheck();
		if (targetRockInfo == null){
			if (Game.printWarnings) System.err.println("Trying to attack a null rock!");
			ThreadManager.punish();
			return false;
		}
		return canAttack(targetRockInfo.getLocation());
	}

	/**
	 * Ordena la unitat associada a aquest controlador que ataqui la unitat objectiu. Si aquesta unitat no pot atacar, o la unitat objectiu no existeix o esta fora de rang, hi ha penalitzacio. Costa 5 d'energia.
	 * @param targetUnitInfo Unitat objectiu.
	 */
	public void attack(UnitInfo targetUnitInfo) {
		sanityCheck();
		if (!canAttack(targetUnitInfo)) {
			if (Game.printWarnings) System.err.println("Cannot attack desired unit!");
			ThreadManager.punish();
			return;
		}
		attackLocation(new GameLocation(targetUnitInfo.getLocation()));
	}

	/**
	 * Ordena la unitat associada a aquest controlador que ataqui la pedra objectiu. Si aquesta unitat no pot atacar, o la pedra objectiu no existeix o esta fora de rang, hi ha penalitzacio. Costa 5 d'energia.
	 * @param targetRock Pedra objectiu.
	 */
	public void attack(RockInfo targetRock) {
		sanityCheck();
		if (!canAttack(targetRock.getLocation())) {
			if (Game.printWarnings) System.err.println("Cannot attack desired rock!");
			ThreadManager.punish();
			return;
		}
		attackLocation(new GameLocation(targetRock.getLocation()));
	}


	/**
	 * Ordena la unitat associada a aquest controlador que ataqui la posicio objectiu. Si aquesta unitat no pot atacar, o la posicio objectiu no existeix o esta fora de rang, hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 */
	public void attack(Location loc) {
		sanityCheck();
		if (!canAttack(loc)){
			if (Game.printWarnings) System.err.println("Cannot attack desired location!");
			ThreadManager.punish();
			return;
		}
		attackLocation(new GameLocation(loc));
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot curar la posicio objectiu. Hi ha penalitzacio si es passa una posicio null. Costa 5 d'energia.
	 * @param loc Posicio objectiu
	 * @return Retorna true si la unitat associada a aquest controlador pot curar la posicio objectiu.
	 */
	private boolean canHeal(Location loc){
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to heal a null location!");
			ThreadManager.punish();
			return false;
		}

		GameLocation gloc = new GameLocation(loc);

		if (!canHeal()) return false;
		if (world.outOfMap(gloc)) return false;
		if (gloc.distanceSquared(unit.getGameLocation()) > getType().getHealingRangeSquared()) return false;

		return !isObstructed(unit.getGameLocation(), gloc);
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot curar la unitat objectiu. Hi ha penalitzacio si es passa una unitat null. Costa 5 d'energia.
	 * @param unit Unitat objectiu
	 * @return Retorna true si la unitat associada a aquest controlador pot curar la unitat objectiu.
	 */
	public boolean canHeal(UnitInfo unit){
		if (unit == null){
			if (Game.printWarnings) System.err.println("Trying to heal a null unit!");
			ThreadManager.punish();
			return false;
		}
		if (unit.getType() == UnitType.QUEEN) return false;
		return canHeal(unit.getLocation());
	}

	/**
	 * Ordena la unitat associada a aquest controlador que curi la unitat objectiu. Si aquesta unitat no pot curar, la unitat objectiu no existeix o esta fora de rang, hi ha penalitzacio. Costa 5 d'energia.
	 * @param targetUnitInfo Unitat objectiu
	 */
	public void heal(UnitInfo targetUnitInfo) {
		sanityCheck();
		if (!canHeal(targetUnitInfo)) {
			if (Game.printWarnings) System.err.println("Cannot heal desired unit!");
			ThreadManager.punish();
			return;
		}
		healLocation(new GameLocation(targetUnitInfo.getLocation()));
	}


	/**
	 * Retorna si la unitat associada a aquest controlador pot minar la posicio objectiu. Hi ha penalitzacio si es passa una posicio null. Costa 5 d'energia.
	 * @param loc Posicio objectiu
	 * @return Retorna true si la unitat associada a aquest controlador pot minar la posicio objectiu.
	 */
	public boolean canMine(Location loc) {
		sanityCheck();
		if (loc == null){
			if (Game.printWarnings) System.err.println("Cannot gather a null Location");
			ThreadManager.punish();
			return false;
		}
		GameLocation gloc = new GameLocation(loc);
		if (!canMine()) return false;
		if (world.outOfMap(gloc)) return false;
		return (gloc.distanceSquared(unit.getGameLocation()) <= getType().getMiningRangeSquared());
	}

	/**
	 * Retorna si la unitat associada a aquest controlador pot minar el menjar objectiu. Hi ha penalitzacio si es passa un menjar null. Costa 5 d'energia.
	 * @param food Menjar objectiu
	 * @return Retorna true si la unitat associada a aquest controlador pot minar el menjar objectiu.
	 */
	public boolean canMine(FoodInfo food){
		sanityCheck();
		if (food == null){
			if (Game.printWarnings) System.err.println("Cannot gather a null food");
			ThreadManager.punish();
			return false;
		}
		return canMine(food.getLocation());
	}

	/**
	 * Ordena la unitat associada a aquest controlador a minar la posicio objectiu. Hi ha penalitzacio si la unitat no pot minar o si la posicio esta fora de rang o no existeix. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 */
	public void mine(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to gather a null Location");
			ThreadManager.punish();
			return;
		}
		if (!canMine(loc)) {
			if (Game.printWarnings) System.err.println("Cannot mine target Location");
			ThreadManager.punish();
			return;
		}
		unit.mine();

		GameLocation gloc = new GameLocation(loc);
		world.mine(unit, gloc);

		Game.getInstance().gameLog.addAction(unit.getTeam(), "Gather", unit.getGameLocation(), gloc);
	}

	/**
	 * Ordena la unitat associada a aquest controlador a minar el menjar objectiu. Hi ha penalitzacio si la unitat no pot minar o si el menjar esta fora de rang o no existeix. Costa 5 d'energia.
	 * @param food Menjar objectiu.
	 */
	public void mine (FoodInfo food){
		sanityCheck();
		if (food == null){
			if (Game.printWarnings) System.err.println("Trying to gather a null food");
			ThreadManager.punish();
			return;
		}
		mine(food.getLocation());
	}

	/**
	 * Retorna si la posicio objectiu esta al rang de visio de la unitat associada a aquest controlador. Si es passa una posicio null hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna true si la posicio objectiu esta al rang de visio de la unitat associada a aquest controlador.
	 */
	public boolean canSenseLocation(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Cannot sense a null Location");
			ThreadManager.punish();
			return false;
		}
		return (loc.distanceSquared(unit.getGameLocation().toLocation()) <= getType().sightRangeSquared);
	}

	/**
	 * Retorna si a la posicio objectiu hi ha una pedra o no. Si es passa una posicio null o si no esta en el rang de visio de la unitat associada a aquest contro.lador hi ha penalitzacio. Costa 1 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna true si a la posicio objectiu hi ha una pedra.
	 */
	public boolean hasObstacle(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to sense obstacle in null Location");
			ThreadManager.punish();
			return false;
		}
		if (!canSenseLocation(loc)) {
			if (Game.printWarnings) System.err.println("Cannot sense target Location");
			ThreadManager.punish();
			return false;
		}

		GameLocation gloc = new GameLocation(loc);

		if (world.outOfMap(gloc)) {
			return false;
		}
		return world.get(gloc).isWall();
	}

	/**
	 * Retorna la informacio de la pedra a la posicio objectiu. Si es passa una posicio null o si no esta en el rang de visio de la unitat associada a aquest contro.lador hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna la informacio de la pedra a la posicio objectiu.
	 */
	public RockInfo senseObstacle(Location loc){
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to sense obstacle in null Location");
			ThreadManager.punish();
			return null;
		}
		if (!canSenseLocation(loc)) {
			if (Game.printWarnings) System.err.println("Cannot sense target Location");
			ThreadManager.punish();
			return null;
		}

		GameLocation gloc = new GameLocation(loc);

		if (world.outOfMap(gloc)) {
			return null;
		}
		return world.get(gloc).getRockInfo();
	}

	/**
	 * Retorna la informacio de les pedres al voltant d'una posicio i radi (al quadrat) donat tals que siguin visibles per la unitat associada a aquest controlador. Si es passa una posicio null o si el radi donat es negatiu hi ha penalitzacio. Costa 100 d'energia.
	 * @param center Centre del cercle.
	 * @param radiusSquared Quadrat del radi del cercle.
	 * @return Retorna la informacio de la pedra a la posicio objectiu.
	 */
	public RockInfo[] senseObstacles(Location center, int radiusSquared) {
		sanityCheck();
		if (radiusSquared < 0){
			ThreadManager.punish();
			return new RockInfo[0];
		}
		GameLocation gcenter = new GameLocation(center);
		radiusSquared = Math.min(radiusSquared, getType().sightRangeSquared);
		ArrayList<RockInfo> ans = new ArrayList<>();

		GameLocation gLoc = unit.gameLocation;

		VisibleCells visibleCells = VisibleCells.getInstance();
		for (int i = 0; i < visibleCells.X[radiusSquared].length; ++i) {
			Cell cell =  world.getIns(gcenter.x + visibleCells.X[radiusSquared][i], gcenter.y + visibleCells.Y[radiusSquared][i]);
			if (cell == null) continue;
			if (gLoc.distanceSquared(cell.gameLocation) > unit.getType().sightRangeSquared) continue;
			if (cell.rockDurability > 0) ans.add(cell.getRockInfo());
		}
		return ans.toArray(new RockInfo[ans.size()]);
	}

	/**
	 * Retorna si una posicio donada esta fora del mapa o no. Si la posicio donada es null o esta fora del rang de visio de la unitat associada a aquest controlador hi ha penalitzacio. Costa 1 d'energia.
	 * @param loc Posicio donada.
	 * @return Retorna true si la posicio donada esta fora del mapa.
	 */
	public boolean isOutOfMap(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to check if null is out of map");
			ThreadManager.punish();
			return true;
		}
		if (!canSenseLocation(loc)) {
			if (Game.printWarnings) System.err.println("Cannot sense target Location");
			ThreadManager.punish();
			return false;
		}
		if (world.outOfMap(new GameLocation(loc))) {
			return true;
		}
		return false;
	}

	/**
	 * Retorna un array de totes les posicions visibles des de la posicio de la unitat associada a aquest controlador fins a cert radi al quadrat. Si el radi es negatiu hi ha penalitzacio. Costa 100 d'energia.
	 * @param radiusSquared Radi al quadrat
	 * @return Retorna un array de totes les posicions visibles des de la posicio de la unitat associada a aquest controlador fins al radi al quadrat donat.
	 */
	public Location[] getVisibleLocations(int radiusSquared) {
		sanityCheck();
		if (radiusSquared < 0){
			ThreadManager.punish();
			return new Location[0];
		}
		Location loc = getLocation();
		radiusSquared = Math.min(radiusSquared, getType().sightRangeSquared);
		VisibleCells visibleCells = VisibleCells.getInstance();
		Location[] ans = new Location[visibleCells.X[radiusSquared].length];
		for (int i = 0; i < ans.length; ++i){
			ans[i] = loc.add(visibleCells.X[radiusSquared][i], visibleCells.Y[radiusSquared][i]);
		}
		return ans;
	}

	/**
	 * Retorna la informacio de la unitat a la posicio objectiu. Si no hi ha cap unitat a la posicio objectiu retorna null. Si la posicio objectiu es null o no esta al rang de visio de la unitat associada a aquest controlador hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna la informacio de la unitat a la posicio objectiu.
	 */
	public UnitInfo senseUnit(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to sense a null Location");
			ThreadManager.punish();
			return null;
		}
		if (!canSenseLocation(loc)) {
			if (Game.printWarnings) System.err.println("Cannot sense target Location");
			ThreadManager.punish();
			return null;
		}

		GameLocation gloc = new GameLocation(loc);

		if (world.outOfMap(gloc)) {
			return null;
		}
		Unit u = world.get(gloc).getUnit();
		if (u != null) return u.toUnitInfo();
		return null;
	}

	private UnitInfo[] senseUnits(Location center, int radiusSquared, boolean myTeam, boolean enemyTeam){
		sanityCheck();
		if (center == null){
			ThreadManager.punish();
			return null;
		}
		if (radiusSquared < 0){
			ThreadManager.punish();
			return new UnitInfo[0];
		}
		radiusSquared = Math.min(radiusSquared, getType().sightRangeSquared);
		ArrayList<UnitInfo> ans = new ArrayList<UnitInfo>();

		Team team = getTeam(), opponent = getTeam().getOpponent();
		GameLocation gcenter = new GameLocation(center);

		GameLocation gLoc = unit.gameLocation;

		VisibleCells visibleCells = VisibleCells.getInstance();
		for (int i = 0; i < visibleCells.X[radiusSquared].length; ++i) {
			Cell cell =  world.getIns(gcenter.x + visibleCells.X[radiusSquared][i], gcenter.y + visibleCells.Y[radiusSquared][i]);
			if (cell == null) continue;
			if(gLoc.distanceSquared(cell.gameLocation) > unit.getType().sightRangeSquared) continue;
			Unit unit = cell.getUnit();
			if (unit == null || unit == this.unit) continue;
			if (myTeam && unit.getTeam() == team) ans.add(unit.toUnitInfo());
			else if (enemyTeam && unit.getTeam() == opponent) ans.add(unit.toUnitInfo());
		}
		return ans.toArray(new UnitInfo[ans.size()]);
	}

	/**
	 * Retorna un array amb l'informacio de totes les unitats visibles en un radi (al quadrat) donat al voltant de la unitat associada a aquest controlador. Hi ha penalitzacio si es passa un radi negatiu. Costa 100 d'energia.
	 * @param radiusSquared Quadrat del radi de cerca.
	 * @return Retorna un array amb l'informacio de totes les unitats visibles en el radi (al quadrat) donat al voltant de la unitat associada a aquest controlador.
	 */
	public UnitInfo[] senseUnits(int radiusSquared) {
		return senseUnits(getLocation(), radiusSquared, true, true);
	}

	/**
	 * Retorna un array amb l'informacio de totes les unitats d'un equip donat visibles en un radi (al quadrat) donat al voltant de la unitat associada a aquest controlador. Hi ha penalitzacio si es passa un radi negatiu o un equip null. Costa 100 d'energia.
	 * @param radiusSquared Quadrat del radi de cerca.
	 * @param team Equip objectiu.
	 * @return Retorna un array amb l'informacio de totes les unitats de l'equip donat visibles en el radi (al quadrat) donat al voltant de la unitat associada a aquest controlador.
	 */
	public UnitInfo[] senseUnits(int radiusSquared, Team team) {
		return senseUnits(getLocation(), radiusSquared, team);
	}

	/**
	 * Retorna un array amb l'informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador en un radi (al quadrat) donat al voltant d'una posicio. Hi ha penalitzacio si es passa un radi negatiu o una posicio null. Costa 100 d'energia.
	 * @param center Posicio central.
	 * @param radiusSquared Quadrat del radi de cerca.
	 * @return Retorna un array amb l'informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador visibles en un radi (al quadrat) donat al voltant d'una posicio.
	 */
	public UnitInfo[] senseUnits(Location center, int radiusSquared){
		if (center == null){
			ThreadManager.punish();
			return null;
		}
		return senseUnits(center, radiusSquared, true, true);
	}

	/**
	 * Retorna un array amb l'informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador de l'equip donat visibles en un radi (al quadrat) al voltant d'una posicio donada. Hi ha penalitzacio si es passa un radi negatiu, una posicio null o un equip null. Costa 100 d'energia.
	 * @param center Posicio central.
	 * @param radiusSquared Quadrat del radi de cerca.
	 * @param team Equip objectiu.
	 * @return Retorna un array amb l'informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador de l'equip donat visibles en un radi (al quadrat) al voltant d'una posicio donada.
	 */
	public UnitInfo[] senseUnits(Location center, int radiusSquared, Team team) {
		if (team == null || center == null){
			ThreadManager.punish();
			return null;
		}
		if (team == getTeam()){
			return senseUnits(center, radiusSquared, true, false);
		}
		return senseUnits(center, radiusSquared, false, true);
	}

	/**
	 * Retorna la informacio del menjar a la posicio objectiu. Si no hi ha menjar a la posicio objectiu retorna un FoodInfo amb maxFood = 0. Si la posicio objectiu es null o no esta al rang de visio de la unitat associada a aquest controlador hi ha penalitzacio. Costa 5 d'energia.
	 * @param loc Posicio objectiu.
	 * @return Retorna la informacio del menjar a la posicio objectiu.
	 */
	public FoodInfo senseFoodAtLocation(Location loc) {
		sanityCheck();
		if (loc == null) {
			if (Game.printWarnings) System.err.println("Trying to sense a null location");
			ThreadManager.punish();
			return null;
		}
		if (!canSenseLocation(loc)) {
			if (Game.printWarnings) System.err.println("Cannot sense target location");
			ThreadManager.punish();
			return null;
		}

		GameLocation gloc = new GameLocation(loc);

		if (world.outOfMap(gloc)) {
			if (Game.printWarnings) System.err.println("Sense target location out of map");
			ThreadManager.punish();
			return null;
		}
		return world.get(gloc).getFoodInfo();
	}

	/**
	 * Retorna un array amb l'informacio de tot el menjar (tal que el menjar inicial sigui positiu) al rang de visio de la unitat associada a aquest controlador en un radi (al quadrat) donat al voltant d'una posicio. Hi ha penalitzacio si es passa un radi negatiu o una posicio null. Costa 100 d'energia.
	 * @param center Posicio central.
	 * @param radiusSquared Quadrat del radi de cerca
	 * @return Retorna un array amb l'informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador en un radi (al quadrat) donat al voltant d'una posicio.
	 */
	public FoodInfo[] senseFood(Location center, int radiusSquared) {
		sanityCheck();
		if (radiusSquared < 0){
			ThreadManager.punish();
			return new FoodInfo[0];
		}
		GameLocation gcenter = new GameLocation(center);
		radiusSquared = Math.min(radiusSquared, getType().sightRangeSquared);
		ArrayList<FoodInfo> ans = new ArrayList<FoodInfo>();

		GameLocation gLoc = unit.gameLocation;

		VisibleCells visibleCells = VisibleCells.getInstance();
		for (int i = 0; i < visibleCells.X[radiusSquared].length; ++i) {
			Cell cell =  world.getIns(gcenter.x + visibleCells.X[radiusSquared][i], gcenter.y + visibleCells.Y[radiusSquared][i]);
			if (cell == null) continue;
			if (gLoc.distanceSquared(cell.gameLocation) > unit.getType().sightRangeSquared) continue;
			if (cell.getMaxFood() > 0) ans.add(cell.getFoodInfo());
		}

		return ans.toArray(new FoodInfo[ans.size()]);
	}

	/**
	 * Retorna un array amb la informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador. Costa 100 d'energia.
	 * @return Retorna un array amb l'informacio de totes les unitats al rang de visio de la unitat associada a aquest controlador.
	 */
	public UnitInfo[] senseUnits() {return senseUnits(getType().sightRangeSquared); }
	/**
	 * Retorna un array amb la informacio de totes les unitats d'un equip donat al rang de visio de la unitat associada a aquest controlador. Costa 100 d'energia.
	 * @param team Equip objectiu.
	 * @return Retorna un array amb l'informacio de totes les unitats d'un equip donat al rang de visio de la unitat associada a aquest controlador.
	 */
	public UnitInfo[] senseUnits(Team team) { return senseUnits(getType().sightRangeSquared, team); }
	/**
	 * Retorna un array amb la informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador. Costa 100 d'energia.
	 * @return Retorna un array amb l'informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador.
	 */
	public FoodInfo[] senseFood() { return senseFood(getLocation(), getType().sightRangeSquared); }
	/**
	 * Retorna un array amb la informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador. Costa 100 d'energia.
	 * @return Retorna un array amb la informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador.
	 */
	public RockInfo[] senseObstacles() { return senseObstacles(getLocation(), getType().sightRangeSquared); }
	/**
	 * Retorna un array amb la informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador donat el quadrat del radi. Hi ha penalitzacio si es passa un radi negatiu. Costa 100 d'energia.
	 * @param radiusSquared Quadrat del rang de cerca.
	 * @return Retorna un array amb la informacio de tot el menjar al rang de visio de la unitat associada a aquest controlador donat el quadrat del radi.
	 */
	public FoodInfo[] senseFood(int radiusSquared) { return senseFood(getLocation(), radiusSquared); }

	/**
	 * Retorna un array amb la informacio de totes les pedres al rang de visio de la unitat associada a aquest controlador donat el quadrat del radi. Hi ha penalitzacio si es passa un radi negatiu. Costa 100 d'energia.
	 * @param radiusSquared Quadrat del rang de cerca.
	 * @return Retorna un array amb la informacio de totes les pedres al rang de visio de la unitat associada a aquest controlador donat el quadrat del radi.
	 */
	public RockInfo[] senseObstacles(int radiusSquared) {return senseObstacles(getLocation(), radiusSquared); }

	/**
	 * Retorna un array amb totes les posicions al rang de visio de la unitat associada a aquest controlador. No es garanteix que estiguin dins el mapa. Costa 100 d'energia.
	 * @return Retorna un array amb totes les posicions al rang de visio de la unitat associada a aquest controlador.
	 */
	public Location[] getVisibleLocations() { return getVisibleLocations(getType().sightRangeSquared); }

	/**
	 * Retorna si la unitat associada a aquest controlador pot crear una unitat d'un cert tipus en la direccio donada.
	 * Si es passa un tipus d'unitat null o una direccio null hi ha penalitzacio. Retorna true si i nomes si la unitat pot crear el tipus desitjat, no ha creat cap unitat en aquest torn i l'equip te els recursos suficients per crear la unitat. Costa 5 d'energia.
	 * @param dir Direccio objectiu.
	 * @param type Tipus d'unitat.
	 * @return Retorna si la unitat associada a aquest controlador pot crear una unitat d'un cert tipus en la direccio donada.
	 */
	public boolean canSpawn(Direction dir, UnitType type) {
		sanityCheck();
		if (dir == null || type == null) {
			if (Game.printWarnings) System.err.println("Trying to check spawn for null direction or type");
			ThreadManager.punish();
			return false;
		}
		if (unit.hasSpawned()) return false;
		if (type.spawner != getType()) return false;
		if (!unit.getTeam().haveResources(type.cost)) return false;
		GameLocation spawnGameLocation = unit.getGameLocation().add(dir);
		if (!world.isAccessible(spawnGameLocation)) return false;
		return true;
	}

	/**
	 * Ordena la unitat associada a aquest controlador a crear una unitat d'un cert tipus en una direccio donada. Si la unitat associada a aquest controlador no pot crear la unitat desitjada hi ha penalitzacio. Costa 5 d'energia.
	 * @param dir Direccio objectiu.
	 * @param type Tipus d'unitat.
	 */
	public void spawn(Direction dir, UnitType type) {
		sanityCheck();
		if (!canSpawn(dir, type)){
			if (Game.printWarnings) System.err.println("Cannot spawn the desired type at the desired Location");
			return;
		}
		unit.getTeam().addResources(-type.cost);
		unit.spawn();
		unit.unitManager.newUnit(unit.getTeam(), unit.getGameLocation().add(dir), unit, type, false);
	}

	/**
	 * Retorna el numero de ronda actual de la partida. Costa 1 d'energia.
	 * @return Retorna el numero de ronda actual de la partida.
	 */
	public int getRound() { return world.getRound(); }

	/**
	 * Retorna la quantitat d'energia utilitzada fins aquest punt. Costa 1 d'energia.
	 * @return Retorna la quantitat d'energia utilitzada fins aquest punt.
	 */
	public int getEnergyUsed() { return ThreadManager.getRunningInstance().getCurrentBytecode(); }

	/**
	 * Retorna l'energia disponible pel que queda de torn. Costa 1 d'energia.
	 * @return Retorna l'energia disponible pel que queda de torn.
	 */
	public int getEnergyLeft() { return GameConstants.MAX_BYTECODES - getEnergyUsed(); }

	private double toPercentage (double energy){
		return energy/GameConstants.MAX_BYTECODES;
	}

	/**
	 * Retorna el percentatge d'energia utilitzada fins aquest punt. Costa 1 d'energia.
	 * @return Retorna el percentatge d'energia utilitzada fins aquest punt.
	 */
	public double getPercentageOfEnergyLeft() { return toPercentage(getEnergyLeft()); }

	/**
	 * Retorna el percentatge d'energia disponible pel que queda de torn. Costa 1 d'energia.
	 * @return Retorna el percentatge d'energia disponible pel que queda de torn.
	 */
	public double getPercentageOfEnergyUsed() { return toPercentage(getEnergyUsed()); }

	/**
	 * Escriu un valor donat a la posicio indicada del vector comu de l'equip. Costa 1 d'energia.
	 * @param i Posicio del vector.
	 * @param val Valor a escriure.
	 */
	public void write(int i, int val) {
		sanityCheck();
		if (!unit.getTeam().setArray(i, val)) ThreadManager.punish();
	}

	/**
	 * Llegeix el valor a la posicio indicada del vector comu de l'equip. Costa 1 d'energia.
	 * @param i Posicio del vector
	 * @return Retorna el valor a la posicio donada del vector comu de l'equip.
	 */
	public int read(int i) {
		sanityCheck();
		if (i < 0 || i >= GameConstants.TEAM_ARRAY_SIZE) ThreadManager.punish();
		return unit.getTeam().readArray(i);
	}

	/**
	 * Escriu per pantalla l'objecte donat. Costa 5 d'energia.
	 * @param o Objecte a escriure.
	 */
	public void println(Object o) {
		if (!Game.printWarnings) return;
		sanityCheck();
		if (o == null){
			System.err.println("Cannot print a null Object");
			ThreadManager.punish();
		}
		System.err.println(o);
		Game.getInstance().gameLog.println(getRound(), unit.getUnitId(), o.toString());
	}

	/**
	 * Dibuixa un punt d'un cert color a la posicio donada. Costa 1 d'energia.
	 * @param loc Posicio objectiu.
	 * @param color Color donat.
	 */
	public void drawPoint(Location loc, String color) {
		sanityCheck();
		if (!Game.printWarnings) return;
		if (loc == null){
			if (Game.printWarnings) System.err.println("Cannot draw point at null Location");
			ThreadManager.punish();
		}
		Game.getInstance().gameLog.drawPoint(getRound(),
				unit.getTeam().getTeamId(), unit.getUnitId(), new GameLocation(loc), color);
	}

	/**
	 * Retorna un array amb la posicio de totes les reines vives de l'equip. Costa 10 d'energia.
	 * @return Retorna un array amb la posicio de totes les reines vives de l'equip.
	 */
	public Location[] getMyQueensLocation(){
		return getQueensLocation(getTeam());
	}

	/**
	 * Retorna un array amb la posicio de totes les reines vives de l'equip enemic. Costa 10 d'energia.
	 * @return Retorna un array amb la posicio de totes les reines vives de l'equip enemic.
	 */
	public Location[] getEnemyQueensLocation(){
		return getQueensLocation(getTeam().getOpponent());
	}

	/**
	 * Retorna un array amb la posicio de totes les reines vives de l'equip indicat. Costa 10 d'energia.
	 * @param team Equip objectiu.
	 * @return Retorna un array amb la posicio de totes les reines vives de l'equip indicat.
	 */
	public Location[] getQueensLocation(Team team){
		if (team == null){
			if (Game.printWarnings) System.err.println("Getting queen locations from a null team!");
			ThreadManager.punish();
			return null;
		}
		ArrayList<Location> ans = new ArrayList<>();
		Unit[] queens = team.getQueenArray();
		for (int i = 0; i < queens.length; ++i){
			if (!queens[i].isDead()) ans.add(queens[i].getGameLocation().toLocation());
		}
		return ans.toArray(new Location[ans.size()]);
	}

	/**
	 * Retorna la quantitat total de menjar disponible per l'equip. Costa 1 d'energia.
	 * @return Retorna la quantitat total de menjar disponible per l'equip.
	 */
	public int getResources(){
		return getTeam().getResources();
	}

	/**
	 * Acaba el torn. Costa 0 d'energia.
	 */
    public void yield() {
		sanityCheck();
		unit.pause();
	}

	// Non-public methods
	private void attackLocation(GameLocation targetGameLocation) {
		unit.attack();
		Unit targetUnit = world.get(targetGameLocation).getUnit();
		if (targetUnit != null) targetUnit.receiveDamage(getType().getAttack());
		world.get(targetGameLocation).decreaseDurability(getType().getAttack());
		Game.getInstance().gameLog.addAction(unit.getTeam(), "Attack", unit.getGameLocation(), targetGameLocation);
	}

	private void healLocation(GameLocation targetGameLocation) {
		unit.heal();
		Unit targetUnit = world.get(targetGameLocation).getUnit();
		if (targetUnit != null) targetUnit.receiveDamage(-getType().getHealingPower());
		Game.getInstance().gameLog.addAction(unit.getTeam(), "Attack", unit.getGameLocation(), targetGameLocation);
	}

}
