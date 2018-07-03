package matchmaker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
				players.add(new Player(rs.getInt("id"), rs.getInt("user_id"),
				 												rs.getInt("elo"), rs.getInt("elo"), 2, 0));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return players;
	}

	// TDay
	static Integer getLastTDay(Integer tournamentId) {
		Integer res = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT max(tday) as tday " +
				" FROM tdays " +
				" WHERE fk_tournament = " + tournamentId
			);
			if (rs.next()) res = new Integer(rs.getInt("tday"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return res;
	}

	static Integer insertTDay(Integer tournamentId, Integer tDay) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer tDayId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(
				"INSERT INTO tdays VALUES " +
				" (NULL, " + tDay + ", 'Ongoing', '" + dtStr + "', '" + dtStr + "', " +
				 	tournamentId + ")"
			);
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			tDayId = keys.getInt(1);
			keys.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return tDayId;
	}

	static Integer updateTDayStatus(Integer id, String status) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE tdays " +
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

	// TDaysRanking
	static Integer insertTDaysRanking(Integer tDayId, Integer playerId,
	 																	Integer elo, Integer points) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"INSERT INTO tdaysrankings VALUES " +
				" (NULL, " + elo + ", " + elo + ", " + points + ", 0, '" + dtStr + "', '" +
				 	dtStr + "', " + tDayId + ", " + playerId + ")"
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static List<Player> getTDayPlayers(Integer tDayId) {
		List<Player> players = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT aa.fk_player, bb.fk_user as user_id, aa.ini_elo, aa.cur_elo, aa.points, aa.n_games" +
				" FROM tdaysrankings aa" +
				" INNER JOIN players bb on aa.fk_player = bb.id" +
				" WHERE aa.fk_tday = " + tDayId +
				" ORDER BY aa.points DESC"
			);
			players = new ArrayList<Player>();
			while (rs.next()) {
				players.add(new Player(rs.getInt("fk_player"), rs.getInt("user_id"),
																rs.getInt("ini_elo"), rs.getInt("cur_elo"),
																rs.getInt("points"), rs.getInt("n_games")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return players;
	}

	static Integer updateTDaysRankingElo(Integer tDayId, Integer playerId,
																				Integer incElo, Integer incPoints) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE tdaysrankings" +
				" SET cur_elo = cur_elo + " + incElo + ", points = points + " +
				 	incPoints + ", n_games = n_games + 1" +
				" WHERE fk_tday = " + tDayId + " and fk_player = " + playerId
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	// Round
	static Integer insertTRound(Integer tDayId, Integer scrimmageId,
	 														Integer round, Integer stage) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"INSERT INTO trounds VALUES" +
				" (NULL, " + round + ", " + stage + ", '" + dtStr + "', '" + dtStr +
				 	"', " + tDayId + ", " + scrimmageId + ")"
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	static Integer getQueuedTRounds(Integer tDayId, Integer round, Integer stage) {
		Integer res = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT count(*) as res" +
				" FROM trounds a" +
				" INNER JOIN scrimmages b ON a.fk_scrimmage = b.id" +
				" WHERE a.fk_tday = " + tDayId + " and a.round = " + round + " and " +
					" a.stage = " + stage + " and b.status = 'Queued'"
			);
			if (rs.next()) res = new Integer(rs.getInt("res"));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return res;
	}

	static List<Scrimmage> getTRoundsScrimmages(Integer tDayId, Integer round, Integer stage) {
		List<Scrimmage> scrimmagesIds = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(
				"SELECT b.fk_req_player, b.fk_res_player, req_wins, res_wins " +
				" FROM trounds a " +
				" INNER JOIN scrimmages b ON a.fk_scrimmage = b.id " +
				" WHERE a.fk_tday = " + tDayId + " and a.round = " + round + " and " +
					" a.stage = " + stage
			);
			scrimmagesIds = new ArrayList<Scrimmage>();
			while (rs.next()) {
				scrimmagesIds.add(new Scrimmage(
					rs.getInt("fk_req_player"), rs.getInt("fk_res_player"),
					rs.getInt("req_wins"), rs.getInt("res_wins")
				));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return scrimmagesIds;
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

	// Games, Scrimmage and Task
	static Integer insertGame(Integer k, Integer scrimmageId, Integer mapId) {
		Date dt = new Date();
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
	 																Integer mapId, Integer nGames) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer scrimmageId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(
				"INSERT INTO scrimmages VALUES " +
				" (NULL, '" + dtStr + "', 0, 0, " + nGames + ", 'Queued', '', '', '" +
				 	dtStr + "', '" + dtStr + "', " + player1Id + ", " + player2Id + ")"
			);
			ResultSet keys = stmt.getGeneratedKeys();
			keys.next();
			scrimmageId = keys.getInt(1);
			keys.close();
			for (int k = 1; k <= nGames; ++k) {
				DbController.insertGame(k, scrimmageId, mapId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return scrimmageId;
	}

	static Integer insertTask(Integer scrimmageId) {
		Date dt = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dtStr = sdf.format(dt);
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"INSERT INTO tasks VALUES " +
				" (NULL, 'Scrimmage', 'Queued', '" + dtStr + "', null, '" + dtStr +
				 	"', '" + dtStr + "', null, " + scrimmageId + ")" // TODO 'Tournament'
			);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) try{ stmt.close(); } catch(SQLException e) {}
		}
		return resId;
	}

	// Player
	static Integer updateElos(Integer userId, Integer tournamentId, Integer elo,
														Integer nGames) {
		Integer resId = null;
		Statement stmt = null;
		try {
			Connection conn = getMysqlDataSource().getConnection();
			stmt = conn.createStatement();
			resId = stmt.executeUpdate(
				"UPDATE elos " +
				" SET elo_prev = elo, elo = " + elo + ", n_games = n_games + " + nGames +
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
}
