package bugwars;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class Unit extends Thread {
	
	UnitManager unitManager;
	
	private Object unitPlayer;
	private Method unitPlayerRunMethod;
	
	private UnitController unitController;

	private int id;
	
	private Team team;
	private UnitType type;

	private boolean shouldExit = false;
	
	int health;

	int cocoonTurnsLeft;

	GameLocation gameLocation;
	GameLocation prevGameLocation;
	
	float moveCooldown, attackCooldown;
	
	boolean hasMined, hasSpawned, hasHealed;

	private boolean dead;

	int bytecodesUsed;
	
	Unit(UnitManager unitManager, Team team, UnitType type) {
		id = Game.getInstance().world.getNewId();
		
		this.unitManager = unitManager;
		this.team = team;
		this.type = type;

		if (type != UnitType.QUEEN) cocoonTurnsLeft = GameConstants.COCOON_TURNS;
		
		health = type.maxHealth;
		dead = false;
		moveCooldown = attackCooldown = 0;
		
		unitController = new UnitController(this);

		resetCooldowns();
	}

	void resetCooldowns() {
		prevGameLocation = gameLocation;
		moveCooldown = Math.max(0, moveCooldown - 1);
		attackCooldown = Math.max(0, attackCooldown - 1);
		hasSpawned = false;
		hasMined = false;
		hasHealed = false;
	}

	void receiveDamage(float dmg){
		health -= dmg;
		if (health <= 0) {
			unitManager.killUnit(this);
			//System.out.println("I should die.");
		}
		if (health > getType().getMaxHealth()) health = getType().getMaxHealth();
	}
	
	@Override
	public void run() {
		getUnitPlayerInstanceAndRunMathod();
		
		do {
			if (isDead()){
				ThreadManager.eraseThread();
				return;
			}
			pause();
			cocoonTurnsLeft--;
		} while (cocoonTurnsLeft > 0);
		
		// Call UnitPlayer run method
		try {
			unitPlayerRunMethod.invoke(unitPlayer, unitController);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof DeathException) {
				//System.out.println("Unit just died.");
				shouldExit = true;
			} else if (e.getCause() instanceof InterruptedException) {
				if (Game.printWarnings) System.out.println("Forced to finish the turn.");
			} else {
				if (Game.printWarnings) e.printStackTrace();
			}
		} catch (Exception e) {
			if (Game.printWarnings) e.printStackTrace();
		} finally{
			if (!shouldExit){
				unitManager.killCurrentUnit();
				ThreadManager.eraseThread();
			}
		}
	}
	
	void pause() {
		ThreadManager.resumeMaster();
	}

	void kill() { dead = true; }
	boolean isDead() { return dead; }
	
	int getUnitId() { return id; }
	UnitType getType() { return type; }
	Team getTeam() { return team; }
	GameLocation getGameLocation() { return gameLocation; }
	int getHealth() { return health; }
	float getMoveCooldown(){
		return moveCooldown;
	}
	float getAttackCooldown() {
		return attackCooldown;
	}

	void move(Direction dir) { moveCooldown += Math.max(1.0, dir.length() * type.getMovementDelay()); }
	void attack() { attackCooldown += type.getAttackDelay(); }
	void spawn() { hasSpawned = true; }
	void mine() { hasMined = true; }
	void heal() { hasHealed = true; }
	
	boolean canMove() { return moveCooldown < 1; }
	boolean canAttack() { return attackCooldown < 1; }

	boolean hasSpawned() { return hasSpawned; }
	boolean hasMined() { return hasMined; }
	boolean hasHealed() { return hasHealed; }
	
	UnitInfo toUnitInfo() { return new UnitInfo(this); }
	UnitController getUnitController() { return unitController; }
	
	// Get UnitPlayer instance and its run method
	private void getUnitPlayerInstanceAndRunMathod() {
		Class<?> unitPlayerClass = team.getUnitPlayerClass();
		try {
			unitPlayerRunMethod = unitPlayerClass.getMethod("run", UnitController.class);
		} catch (NoSuchMethodException | SecurityException e) {
			if (Game.printWarnings) e.printStackTrace();
		}
		try {
			// Player could be a troll and implement a while(true) in the constructor ...
			unitPlayer = unitPlayerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			if (Game.printWarnings) e.printStackTrace();
		}
	}

}
