package tasksmanager;

import java.io.IOException;
import java.util.Date;

import net.lingala.zip4j.exception.ZipException;

public class Main {
	final static long NANOSEC_PER_SEC = 1000l*1000*1000;

	public static void main(String[] args) {
		System.out.println("Taskmanager started!");
		System.out.println(new Date());

		long startTime = System.nanoTime();
		while ((System.nanoTime() - startTime) < 55 * NANOSEC_PER_SEC) {
			Integer nRunningTasks = Task.getNRunningTasks();
			if (nRunningTasks == 0) {
				Task task = Task.getPendingTask();
				if (task != null) {
					task.update("Running");
					System.out.println("Managing task " + task.type + " " + task.id +
					 										" (" + task.refId + ")");
					if (task.type.equals("Validation")) {
						// Get player info
						Player player = Player.getPlayer(task.refId);
						if (player != null) {
							// Execute validation and update status
							System.out.println(player.tournament + ": Validating player " +
					 				player.user);
							try {
								player.validate();
								player.updateStatus("Validated");
								player.updateLog("Validation succeeded", "");
								task.update("Completed");
								System.out.println("Validation succeeded");
							} catch (TaskException e) {
								player.updateStatus("Erroneous");
								player.updateLog(e.shortMsg, e.longMsg);
								task.update("Erroneous");
								e.printStackTrace();
							} catch (IOException | InterruptedException | ZipException e) {
								player.updateStatus("Erroneous");
								player.updateLog("Validation failed", e.getMessage());
								task.update("Erroneous");
								e.printStackTrace();
							}
							player.cleanValidation();
						} else {
							System.err.println("Error: Player not found");
							task.update("Erroneous");
						}
					} else if (task.type.equals("Scrimmage") || task.type.equals("Tournament")) {
						// Get scrimmage info
						Scrimmage scrimmage = Scrimmage.getScrimmage(task.refId, task.type);
						if (scrimmage != null) {
							// Execute scrimmage and update status
							System.out.println(scrimmage.tournament1 + ": Scrimmage " +
									scrimmage.user1 + " vs " + scrimmage.user2);
							try {
								scrimmage.execute();
								scrimmage.updateStatus("Played");
								scrimmage.updateLog("Scrimmage succeeded", "");
								if (task.type.equals("Tournament")) {
									scrimmage.updateElos();
								}
								task.update("Completed");
								System.out.println("Scrimmage succeeded");
							} catch (TaskException e) {
								scrimmage.updateStatus("Erroneous");
								scrimmage.updateLog(e.shortMsg, e.longMsg);
								task.update("Erroneous");
								e.printStackTrace();
							} catch (IOException | InterruptedException | ZipException e) {
								scrimmage.updateStatus("Erroneous");
								scrimmage.updateLog("Scrimmage failed", e.getMessage());
								task.update("Erroneous");
								e.printStackTrace();
							}
							scrimmage.cleanScrimmage();
						} else {
							System.err.println("Error: Scrimmage not found");
							task.update("Erroneous");
						}
					} else {
						System.err.println("Error: Type " + task.type + " is not a valid task type");
						task.update("Erroneous");
					}
				}/* else {
					System.out.println("No queued tasks");
				}*/
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.err.println("Task manager was interrupted");
				System.exit(0);
			}
		}
	}
}
