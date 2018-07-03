package bugwars;

import java.util.LinkedList;
import java.util.List;

class TeamManager {
	World world;
  private List<Team> teams;

  TeamManager(World world) {
  	this.world = world;
      teams = new LinkedList<>();
  }

	void addResourcesToAll(int x) {
		for (Team team : teams){
			team.addResources(x);
		}
	}

	Team newTeam(String packageName) {
		if (teams.size() < world.getNTeams()) {
			GameLocation[] initialGameLocation = world.getTeamLocation(teams.size());
			Team team = new Team(packageName, initialGameLocation);
			teams.add(team);
			return team;
		}
		return null;
	}

	Team getOpponent(Team team) {
		for (Team otherTeam : teams) {
			if (otherTeam != team && otherTeam.isPlayer()) return otherTeam;
		}
		return null;
	}

	List<Team> getTeams() { return teams; }
}
