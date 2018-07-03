package grassmann.example.utils;

import bugwars.Direction;
import bugwars.Location;
import bugwars.UnitController;

public class PathController {

	UnitController uc;
	Location start, goal;
	Location obstacle;
	String direction;

	public PathController(UnitController _uc, Location _start, Location _goal) {
		uc = _uc;
		start = _start;
		goal = _goal;
		obstacle = null;
		direction = Math.random() < .5 ? "right" : "left";
		//direction = "right";
	}

	public Direction getMoveDir() {
		if (!uc.canMove()) return Direction.ZERO;

		if (obstacle == null) return headTowardGoal();
		return followObstacle();
	}

	Direction headTowardGoal() {
		Direction nextDir = uc.getLocation().directionTo(goal);
		if (uc.canMove(nextDir)) {
			return nextDir;
		}
		Location potObstacle = uc.getLocation().add(nextDir);
		if (uc.hasObstacle(potObstacle)) {
			obstacle = potObstacle;
			start = uc.getLocation();
			return followObstacle();
		}
		return Direction.ZERO;
	}

	Direction followObstacle() {
		Direction obstacleDir = uc.getLocation().directionTo(obstacle);
		Direction dir;
		if (direction == "right") dir = obstacleDir.rotateRight();
		else dir = obstacleDir.rotateLeft();
		while (dir != obstacleDir && !uc.canMove(dir)) {
			Location potObstacle = uc.getLocation().add(dir);
			if (uc.hasObstacle(potObstacle)) obstacle = potObstacle;
			if (direction == "right") dir = dir.rotateRight();
			else dir = dir.rotateLeft();
		}
		if (dir == obstacleDir) return Direction.ZERO;
		if (hasFoundLine(dir)) {
			obstacle = null;
			return headTowardGoal();
		}
		return dir;
	}

	boolean hasFoundLine(Direction dir) {
		Location currLoc = uc.getLocation();
		if (start.isEqual(currLoc)) return false;
		Location nextLoc = uc.getLocation().add(dir);
		int vecProd1 = (currLoc.x - start.x) * (goal.y - start.y) - (currLoc.y - start.y) * (goal.x - start.x);
		int vecProd2 = (nextLoc.x - start.x) * (goal.y - start.y) - (nextLoc.y - start.y) * (goal.x - start.x);
		return vecProd1 * vecProd2 <= 0;
	}

}
