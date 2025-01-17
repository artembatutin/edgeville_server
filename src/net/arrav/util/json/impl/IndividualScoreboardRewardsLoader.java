package net.arrav.util.json.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.arrav.content.scoreboard.ScoreboardManager;
import net.arrav.util.MutableNumber;
import net.arrav.util.json.JsonLoader;

/**
 * The {@link JsonLoader} implementation that loads all individual scoreboard rewards.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class IndividualScoreboardRewardsLoader extends JsonLoader {
	
	/**
	 * Constructs a new {@link IndividualScoreboardRewardsLoader}.
	 */
	public IndividualScoreboardRewardsLoader() {
		super("./data/def/score/individual_killstreak_rewards.json");
	}
	
	@Override
	public void load(JsonObject reader, Gson builder) {
		String username = reader.get("username").getAsString();
		int amount = reader.get("amount").getAsInt();
		ScoreboardManager.get().getPlayerScoreboardRewards().put(username, new MutableNumber(amount));
	}
	
}
