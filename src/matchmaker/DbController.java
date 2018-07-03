package matchmaker;

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

	// Tournament
	static List<Integer> getTournamentsIds() {
		List<Integer> tournamentsIds = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id FROM tournaments");
			tournamentsIds = new ArrayList<Integer>();
			while (rs.next()) tournamentsIds.add(new Integer(rs.getInt("id")));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return tournamentsIds;
	}

	static List<Player> getTournamentPlayers(Integer tournamentId) {
		List<Player> players = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
			"SELECT aa.id, aa.fk_user as user_id, bb.elo" +
			" FROM players aa" +
			" INNER JOIN elos bb on aa.fk_user = bb.fk_user and aa.fk_tournament = bb.fk_tournament" +
			" WHERE aa.fk_tournament = " + tournamentId + " and aa.status = 'Validated'" +
			" ORDER BY bb.elo DESC");
			players = new ArrayList<Player>();
			while (rs.next()) {
				players.add(new Player(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("elo")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return players;
	}

	static Integer getRandomMapId(Integer tournamentId) {
		Integer res = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT id" +
				" FROM maps" +
				" WHERE fk_tournament = " + tournamentId +
				" ORDER BY RAND()" +
				" LIMIT 1"
			);
			if (rs.next()) res = new Integer(rs.getInt("id"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return res;
	}

	static String getMapName(Integer mapId) {
		String res = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT name" +
				" FROM maps" +
				" WHERE id = " + mapId
			);
			if (rs.next()) res = new String(rs.getString("name"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return res;
	}

	// Games, Scrimmage and Task
	static Integer insertGame(Integer k, Integer scrimmageId, Integer mapId) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 4);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"INSERT INTO games VALUES " +
				" (NULL, " + k + ", '" + dtStr + "', '" + dtStr + "', NULL, " +
			 		scrimmageId + ", " + mapId + ")"
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static Integer insertScrimmage(Integer player1Id, Integer player2Id,
	 																Integer nGames, Integer tournamentId) {
		List<Integer> mapIds = new ArrayList<Integer>();
		String maps = "";
		for (int k = 1; k <= nGames; ++k) {
			Integer mapId = DbController.getRandomMapId(tournamentId);
			if (maps != "") maps = maps + ",";
			maps = maps + DbController.getMapName(mapId);
			mapIds.add(mapId);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 4);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer scrimmageId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(
				"INSERT INTO scrimmages VALUES " +
				" (NULL, 1, '" + dtStr + "', 0, 0, " + nGames + ", '" + maps + "'," +
				" 'Queued', '', '', '" + dtStr + "', '" + dtStr + "'," +
				" " + player1Id + ", " + player2Id + ")"
			);
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			scrimmageId = keys.getInt(1);
			keys.close();
			for (int k = 1; k <= nGames; ++k) {
				DbController.insertGame(k, scrimmageId, mapIds.get(k - 1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return scrimmageId;
	}

	static Integer insertTask(Integer scrimmageId) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.HOUR_OF_DAY, 4);
		Date dt = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"INSERT INTO tasks VALUES " +
				" (NULL, 'Tournament', 'Queued', '" + dtStr + "', null, '" + dtStr +
				 	"', '" + dtStr + "', null, " + scrimmageId + ")" // TODO 'Tournament'
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}
}
