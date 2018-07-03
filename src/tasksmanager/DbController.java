package tasksmanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

class DbController {
	static final String HOST = "localhost";
	static final int PORT = 3306;
	static final String USER = "navgame";
	static final String PASS = "Nav17db.";
	static final String DB = "navgame";

	static DataSource getMysqlDataSource() {
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setServerName(HOST);
		dataSource.setPortNumber(PORT);
		dataSource.setDatabaseName(DB);
		dataSource.setUser(USER);
		dataSource.setPassword(PASS);
		return dataSource;
	}

	// Task
	static Task getFirstQueuedTask(String type) {
		Task task = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT *" +
				" FROM tasks" +
				" WHERE type = '" + type + "' and status = 'Queued'" +
				" ORDER BY createdAt DESC LIMIT 1");
			if (rs.next()) {
				task = new Task(rs.getInt("id"), rs.getString("type"), rs.getInt("fk_player"), rs.getInt("fk_scrimmage"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return task;
	}

	static Task getFirstQueuedTask() {
		Task task = DbController.getFirstQueuedTask("Tournament");
		if (task == null) task = DbController.getFirstQueuedTask("Validation");
		if (task == null) task = DbController.getFirstQueuedTask("Scrimmage");
		return task;
	}

	static Integer getNRunningTasks() {
		Integer nRunningTasks = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT count(*) as n" +
				" FROM tasks" +
				" WHERE status = 'Running'");
			if (rs.next()) {
				nRunningTasks = new Integer(rs.getInt("n"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return nRunningTasks;
	}

	static Integer updateTaskStatus(Integer id, String status) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 4);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE tasks " +
				" SET status = '" + status + "', exe_date = '" + sdf.format(dt) + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	// Game
	static Integer updateGameWinner(Integer id, Integer winner) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE games " +
				" SET fk_winner = '" + winner + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	// Player
	static Player getPlayer(Integer id) {
		Player player = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT a.id," +
				"   b.id as userId, b.name as user," +
				"   c.id as tournamentId, c.name as tournament " +
				" FROM players a " +
				" INNER JOIN users b ON a.fk_user = b.id " +
				" INNER JOIN tournaments c ON a.fk_tournament = c.id " +
				" WHERE a.id = " + id
			);
			if (rs.next()) {
				player = new Player(rs.getInt("id"), rs.getInt("userId"),
				 										rs.getString("user"), rs.getInt("tournamentId"),
														rs.getString("tournament"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return player;
	}

	static Player getPlayer(String userName, String tournamentName) {
		Player player = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT a.id," +
				"   b.id as userId, b.name as user," +
				"   c.id as tournamentId, c.name as tournament " +
				" FROM players a " +
				" INNER JOIN users b ON a.fk_user = b.id " +
				" INNER JOIN tournaments c ON a.fk_tournament = c.id " +
				" WHERE b.name = '" + userName + "' and c.name = '" + tournamentName + "'"
			);
			if (rs.next()) {
				player = new Player(rs.getInt("id"), rs.getInt("userId"),
				 										rs.getString("user"), rs.getInt("tournamentId"),
														rs.getString("tournament"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return player;
	}

	static Integer updatePlayerStatus(Integer id, String status) {
		if (status == "Validated") {
			DbController.replacePlayers(id);
		}
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE players " +
				" SET status = '" + status + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static Integer updatePlayerLog(Integer id, String logShort, String logLong) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE players " +
				" SET log_short = '" + logShort + "', log_long = '" + logLong + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static void replacePlayers(Integer id) {
		Statement stmt = null, stmt2 = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT fk_user, fk_tournament" +
				" FROM players" +
				" WHERE id = " + id
			);
			if (rs.next()) {
				stmt2 = conn.createStatement();
				stmt.executeUpdate(
					"UPDATE players " +
					" SET status = 'Replaced' " +
					" WHERE fk_user = " + rs.getInt("fk_user") + " and fk_tournament = " +
						rs.getInt("fk_tournament") + " and status = 'Validated'"
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
			if (stmt2 != null) try{ stmt2.close(); } catch(SQLException e) {}
		}
	}

	// Game
	static Game getGame(Integer id) {
		Game game = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT a.id, b.name as map " +
				" FROM games a " +
				" INNER JOIN maps b ON a.fk_map = b.id " +
				" WHERE a.id = " + id
			);
			if (rs.next()) {
				game = new Game(rs.getInt("id"), rs.getString("map"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return game;
	}

	// Scrimmage
	static Scrimmage getScrimmage(Integer id) {
		Scrimmage scrimmage = null;
		Statement stmt = null, stmt2 = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT " +
				"   a.id, " +
				"   b2.id as userId1, b2.name as user1," +
				"   b3.id as tournamentId1, b3.name as tournament1," +
				"   a.req_wins as wins1, " +
				"   c2.id as userId2, c2.name as user2," +
				"   c3.id as tournamentId2, c3.name as tournament2," +
				"   a.res_wins as wins2 " +
				" FROM scrimmages a " +
				" INNER JOIN players b1 ON a.fk_req_player = b1.id " +
				" INNER JOIN users b2 ON b1.fk_user = b2.id " +
				" INNER JOIN tournaments b3 ON b1.fk_tournament = b3.id " +
				" INNER JOIN players c1 ON a.fk_res_player = c1.id " +
				" INNER JOIN users c2 ON c1.fk_user = c2.id " +
				" INNER JOIN tournaments c3 ON b1.fk_tournament = c3.id " +
				" WHERE a.id = " + id
			);
			if (rs.next()) {
				stmt2 = conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(
					"SELECT id " +
					" FROM games " +
					" WHERE fk_scrimmage = " + id + " and fk_winner is null"
				);
				List<Game> games = new ArrayList<Game>();
				while (rs2.next()) games.add(DbController.getGame(rs2.getInt("id")));
				scrimmage = new Scrimmage(
					rs.getInt("id"),
					rs.getInt("userId1"), rs.getString("user1"),
					rs.getInt("tournamentId1"), rs.getString("tournament1"),
					0, rs.getInt("wins1"),
					rs.getInt("userId2"), rs.getString("user2"),
					rs.getInt("tournamentId2"), rs.getString("tournament2"),
					0, rs.getInt("wins2"),
					games
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
			if (stmt2 != null) try{ stmt2.close(); } catch(SQLException e) {}
		}
		return scrimmage;
	}

	static Scrimmage getRankedScrimmage(Integer id) {
		Scrimmage scrimmage = null;
		Statement stmt = null, stmt2 = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT " +
				"   a.id, " +
				"   b2.id as userId1, b2.name as user1," +
				"   b3.id as tournamentId1, b3.name as tournament1," +
				"   b4.elo as elo1, " +
				"   a.req_wins as wins1, " +
				"   c2.id as userId2, c2.name as user2," +
				"   c3.id as tournamentId2, c3.name as tournament2," +
				"   c4.elo as elo2, " +
				"   a.res_wins as wins2 " +
				" FROM scrimmages a " +
				" INNER JOIN players b1 ON a.fk_req_player = b1.id " +
				" INNER JOIN users b2 ON b1.fk_user = b2.id " +
				" INNER JOIN tournaments b3 ON b1.fk_tournament = b3.id " +
				" INNER JOIN elos b4 ON b1.fk_tournament = b4.fk_tournament and " +
				 	" b1.fk_user = b4.fk_user " +
				" INNER JOIN players c1 ON a.fk_res_player = c1.id " +
				" INNER JOIN users c2 ON c1.fk_user = c2.id " +
				" INNER JOIN tournaments c3 ON b1.fk_tournament = c3.id " +
				" INNER JOIN elos c4 ON c1.fk_tournament = c4.fk_tournament and " +
				 	" c1.fk_user = c4.fk_user " +
				" WHERE a.id = " + id
			);
			if (rs.next()) {
				stmt2 = conn.createStatement();
				ResultSet rs2 = stmt2.executeQuery(
					"SELECT id " +
					" FROM games " +
					" WHERE fk_scrimmage = " + id + " and fk_winner is null"
				);
				List<Game> games = new ArrayList<Game>();
				while (rs2.next()) games.add(DbController.getGame(rs2.getInt("id")));
				scrimmage = new Scrimmage(
					rs.getInt("id"),
					rs.getInt("userId1"), rs.getString("user1"),
					rs.getInt("tournamentId1"), rs.getString("tournament1"),
					rs.getInt("elo1"), rs.getInt("wins1"),
					rs.getInt("userId2"), rs.getString("user2"),
					rs.getInt("tournamentId2"), rs.getString("tournament2"),
					rs.getInt("elo2"), rs.getInt("wins2"),
					games
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
			if (stmt2 != null) try{ stmt2.close(); } catch(SQLException e) {}
		}
		return scrimmage;
	}

	static Integer updateScrimmageStatus(Integer id, String status) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE scrimmages " +
				" SET status = '" + status + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static Integer updateScrimmageWins(Integer id, Integer wins1, Integer wins2) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE scrimmages " +
				" SET req_wins = '" + wins1 + "', res_wins = '" + wins2 + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static Integer updateScrimmageLog(Integer id, String logShort, String logLong) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE scrimmages " +
				" SET log_short = '" + logShort + "', log_long = '" + logLong + "' " +
				" WHERE id = " + id
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	// ELO
	static Integer updateElo(Integer tournamentId, Integer userId, Integer elo) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE elos " +
				" SET elo_prev = elo, elo = " + elo + ", n_games = n_games + " + 1 +
				" WHERE fk_user = " + userId + " and fk_tournament = " + tournamentId
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		DbController.updateRanks(tournamentId);
		return resId;
	}

	static void updateRanks(Integer tournamentId) {
		Statement stmt = null, stmt2 = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT e.id, @curRank := @curRank + 1 AS rank, e.rank_prev" +
				" FROM elos e" +
				" JOIN (SELECT @curRank := 0) r" +
				" WHERE fk_tournament = " + tournamentId +
				" ORDER BY e.elo DESC, e.n_games DESC, e.elo_prev DESC, e.createdAt DESC"
			);
			while (rs.next()) {
				stmt2 = conn.createStatement();
				stmt2.executeUpdate(
					"UPDATE elos" +
					" SET rank_prev = " + rs.getInt("rank_prev") +
			 					", rank = " + rs.getInt("rank") +
					" WHERE id = " + rs.getInt("id")
				);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
			if (stmt2 != null) try{ stmt2.close(); } catch(SQLException e) {}
		}
	}

	static Integer insertElo(Integer tournamentId, Integer userId) {
		Statement stmt = null, stmt2 = null;
		Integer resId = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT 1" +
				" FROM elos e" +
				" WHERE fk_tournament = " + tournamentId + " AND fk_user = " + userId
			);
			System.out.println("SELECT 1" +
			" FROM elos e" +
			" WHERE fk_tournament = " + tournamentId + " AND fk_user = " + userId);
			if (!rs.next()) {
				System.out.println("rs.next is false");
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.HOUR_OF_DAY, 4);
				Date dt = cal.getTime();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dtStr = sdf.format(dt);
				stmt2 = conn.createStatement();
				resId = stmt2.executeUpdate(
					"INSERT INTO elos VALUES " +
					" (NULL, 1200, 1200, NULL, NULL, 0, '" + dtStr + "', '" + dtStr + "', " +
				 		userId + ", " + tournamentId + ")"
				);
					System.out.println(
						"INSERT INTO elos VALUES " +
						" (NULL, 1200, 1200, NULL, NULL, 0, '" + dtStr + "', '" + dtStr + "', " +
					 		userId + ", " + tournamentId + ")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
			if (stmt2 != null) try{ stmt2.close(); } catch(SQLException e) {}
		}
		return resId;
	}
}
