package bugwars;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

class GameLog {
	final String ln = System.lineSeparator();

	Game game;
	String logName;
	boolean prints, drawings;

	int nActions1, nActions2;
	StringBuffer actions1, actions2;

	public GameLog(String _logName, boolean _prints, boolean _drawings) {
		this.game = Game.getInstance();

		// Generate log directory and name
		logName = _logName;
		(new File(logName)).mkdirs();
		prints = _prints;
		drawings = _drawings;

		printGameInfo(game.team1.packageName, game.team2.packageName,
		 							game.world.getNRows(), game.world.getNCols());

		nActions1 = 0;
		nActions2 = 0;
		actions1 = new StringBuffer();
		actions2 = new StringBuffer();
	}

	void addAction(Team team, String type, GameLocation fromLoc, GameLocation toLoc) {
		int typeCode = type == "Attack" ? 0 : 1;
		if (team == game.team1) {
			nActions1++;
			actions1.append("" + typeCode + " " + fromLoc.x + " " + fromLoc.y + " " + toLoc.x + " " + toLoc.y + ln);
		} else if (team == game.team2) {
			nActions2++;
			actions2.append("" + typeCode + " " + fromLoc.x + " " + fromLoc.y + " " + toLoc.x + " " + toLoc.y + ln);
		}
	}

	void printGameInfo(String packageName1, String packageName2, int nr, int nc) {
		String file = logName + File.separator + "game_info.txt";
		try (
			FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			out.println("" + packageName1);
			out.println("" + packageName2);
			out.println("" + nr + " " + nc);
		} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write game info");
		}
	}

	void printTurn() {
		StringBuffer walls = new StringBuffer();
		StringBuffer foods = new StringBuffer();
		StringBuffer units1 = new StringBuffer();
		StringBuffer units2 = new StringBuffer();
		// Walls
		List<RockInfo> wall = game.world.getWalls();
		for (RockInfo w : wall) {
			GameLocation gloc = new GameLocation(w.location);
			walls.append("" + gloc.x + " " + gloc.y + " " + w.durability + ln);
		}
		// Food
		List<FoodInfo> food = game.world.getFood();
		for (FoodInfo f : food) {
			GameLocation gloc = new GameLocation(f.location);
			foods.append("" + gloc.x + " " + gloc.y + " " + f.food + ln);
		}
		// Units
		List<Unit> units = game.world.getUnits();
		int nUnits1 = 0, nUnits2 = 0;
		for (Unit unit : units) {
			String unitLog = "" +
				unit.getUnitId() + " " +
				(Arrays.asList(UnitType.values()).indexOf(unit.getType())) + " " +
				unit.gameLocation.x + " " + unit.gameLocation.y + " " +
				unit.prevGameLocation.x + " " + unit.prevGameLocation.y + " " +
				unit.health + " " +
				unit.moveCooldown + " " + unit.attackCooldown + " " +
				unit.cocoonTurnsLeft + " " +
				unit.bytecodesUsed;
			if (unit.getTeam() == game.team1) {
				nUnits1++;
				units1.append(unitLog + ln);
			} else if (unit.getTeam() == game.team2) {
				nUnits2++;
				units2.append(unitLog + ln);
			}
		}
		String file = logName + File.separator + "game.txt";
		try (
				FileWriter fw = new FileWriter(file, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				out.println(wall.size());
				out.print(walls);
				out.println(food.size());
				out.print(foods);
				out.println(game.team1.getResources());
				out.println(nUnits1);
				out.print(units1);
				out.println(nActions1);
				out.print(actions1);
				out.println(game.team2.getResources());
				out.println(nUnits2);
				out.print(units2);
				out.println(nActions2);
				out.print(actions2);
			} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write obstacles");
			}
		nActions1 = 0;
		nActions2 = 0;
		actions1 = new StringBuffer();
		actions2 = new StringBuffer();
	}

	void printWinner(Team winner, String winCondition) {
		String file = logName + File.separator + "game_info.txt";
		try (
			FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			if (winner == null) out.println(0);
			else if (winner == game.team1) out.println(1);
			else if (winner == game.team2) out.println(2);
			out.println(winCondition);
		} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write game info");
		}
	}

	void printNumberOfRounds() {
		String file = logName + File.separator + "game_info.txt";
		try (
			FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			out.println(game.world.getRound() + 2);
		} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write game info");
		}
	}

	void println(int round, int unitId, String string) {
		if (!prints) return;
		String file = logName + File.separator + unitId + ".txt";
		try (
			FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			out.print("R" + round + "> ");
		    out.println(string);
		} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write unit log message");
		}
	}

	void drawPoint(int round, int teamId, int unitId, GameLocation loc, String color) {
		if (!drawings) return;
		String file = logName + File.separator + "draw" + teamId + ".txt";
		try (
			FileWriter fw = new FileWriter(file, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    PrintWriter out = new PrintWriter(bw))
		{
			out.print("R" + round + "> ");
		    out.println("" + unitId + " " + loc.x + " " + loc.y + " " + color);
		} catch (IOException e) {
			if (Game.printWarnings) System.err.println("Error: Could not write unit log message");
		}
	}
}
