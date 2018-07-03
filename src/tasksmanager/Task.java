package tasksmanager;

class Task {
	Integer id;
	String type;
	Integer refId;

	Task(Integer _id, String _type, Integer _playerId, Integer _scrimmageId) {
		id = _id;
		type = _type;
		if (type.equals("Validation")) refId = _playerId;
		else refId = _scrimmageId;
	}

	static Task getPendingTask() {
		return DbController.getFirstQueuedTask();
	}

	static Integer getNRunningTasks() {
		return DbController.getNRunningTasks();
	}

	void update(String status) {
		DbController.updateTaskStatus(id, status);
	}
}
