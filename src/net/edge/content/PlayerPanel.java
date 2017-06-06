package net.edge.content;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.edge.game.GameConstants;
import net.edge.util.TextUtils;
import net.edge.util.Utility;
import net.edge.content.dialogue.impl.OptionDialogue;
import net.edge.content.dialogue.impl.StatementDialogue;
import net.edge.content.scoreboard.PlayerScoreboardStatistic;
import net.edge.locale.loc.Location;
import net.edge.world.World;
import net.edge.world.node.entity.player.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The enumerated type whose elements represent functionality for the quest tab.
 * @author Artem Batutin <artembatutin@gmail.com>
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public enum PlayerPanel {
	TOOLS(62154),
	COMMUNITY(62155) {
		@Override
		public void onClick(Player player) {
			player.getMessages().sendLink("community/");
		}
	},
	DISCORD(62156) {
		@Override
		public void onClick(Player player) {
			player.getMessages().sendLink("discord");
		}
	},
	VOTE(62157) {
		@Override
		public void onClick(Player player) {
			player.getMessages().sendLink("vote");
		}
	},
	DONATE(62158) {
		@Override
		public void onClick(Player player) {
			player.getMessages().sendLink("donate/");
		}
	},
	NPC_TOOL(62159) {
		@Override
		public void onClick(Player player) {
			player.getMessages().sendNpcInformation(0, null);
		}
	},
	TOOL3(62160),
	
	SERVER_STATISTICS(62161),
	UPTIME(621562),
	PLAYERS_ONLINE(62163) {
		@Override
		public void onClick(Player player) {
			player.message("There is currently " + World.get().getPlayers().size() + " players online.");
		}
	},
	STAFF_ONLINE(62164) {
		@Override
		public void onClick(Player player) {
			List<Player> staff = World.get().getPlayers().findAll(p -> p != null && p.getRights().isStaff());
			if(staff.isEmpty()) {
				player.message("There is currently no staff online to assist you.");
				player.message("You can post a thread on our forums in the support section.");
			} else {
				player.getDialogueBuilder().append(new StatementDialogue("Are you requesting staff assistance?"), new OptionDialogue(t -> {
					if(t == OptionDialogue.OptionType.FIRST_OPTION) {
						staff.forEach(s -> s.message(player.getFormatUsername() + " is requesting assistance. Do ::assist " + player.getUsername() + " to help him."));
						player.message("A staff member should contact you shortly.");
					}
					player.getMessages().sendCloseWindows();
				}, "Yes please!", "No thanks."));
			}
		}
	},
	PLAYERS_IN_WILD(62165),
	
	EMPTY(62166),
	
	PLAYER_STATISTICS(62167),
	USERNAME(62168),
	PASSWORD(62169) {
		@Override
		public void onClick(Player player) {
			player.getDialogueBuilder().append(new StatementDialogue("You sure you want to change your password?"), new OptionDialogue(t -> {
				if(t == OptionDialogue.OptionType.FIRST_OPTION) {
					player.getMessages().sendCloseWindows();
					player.getMessages().sendEnterName("Your new password to set:", s -> () -> {
						String name = s;
						player.setPassword(name);
						player.message("You have successfully changed your password. Log out to save it.");
						PlayerPanel.PASSWORD.refresh(player, "@or2@ - Password: " + TextUtils.passwordCheck(name));
					});
				} else if(t == OptionDialogue.OptionType.SECOND_OPTION) {
					player.getMessages().sendCloseWindows();
				}
			}, "Yes please!", "No thanks."));
		}
	},
	RANK(62170),
	NIGHT(62171) {
		@Override
		public void onClick(Player player) {
			if(player.isNight()) {
				player.getDialogueBuilder().append(new StatementDialogue("You want to quit the night's watch?"), new OptionDialogue(t -> {
					if(t == OptionDialogue.OptionType.FIRST_OPTION) {
						player.setNight(0);
						player.teleport(GameConstants.STARTING_POSITION);
					}
					player.getMessages().sendCloseWindows();
				}, "Yes, want to be a regular player.", "No, I want to keep the nightmare mode."));
				return;
			}
			player.message("You can only become a night's watch member in the beginning.");
		}
	},
	SLAYER_POINTS(62172),
	SLAYER_TASK(62173),
	SLAYER_COUNT(62174),
	
	EMPTY1(62175),
	
	PVE_HEADER(62176),
	
	HIGHEST_KILLSTREAK(62177),
	CURRENT_KILLSTREAK(62178),
	TOTAL_PLAYER_KILLS(62179),
	TOTAL_PLAYER_DEATHS(62180),
	TOTAL_NPC_KILLS(62181),
	TOTAL_NPC_DEATHS(62182),
	
	EMPTY2(62183),
	
	INDIVIDUAL_SCOREBOARD_STATISTICS(62184),
	
	INDIVIDUAL_HIGHEST_KILLSTREAKS(62185),
	INDIVIDUAL_CURRENT_KILLSTREAKS(62186),
	INDIVIDUAL_KILLS(62187),
	INDIVIDUAL_DEATHS(62188);
	
	/**
	 * Caches our enum values.
	 */
	private static final ImmutableSet<PlayerPanel> VALUES = Sets.immutableEnumSet(EnumSet.allOf(PlayerPanel.class));
	
	/**
	 * The button identification.
	 */
	private final int buttonId;
	
	/**
	 * Constructs a new {@link PlayerPanel}.
	 * @param buttonId {@link #buttonId}.
	 */
	PlayerPanel(int buttonId) {
		this.buttonId = buttonId;
	}
	
	/**
	 * Gets the button id of the line.
	 * @return the button id.
	 */
	public int getButtonId() {
		return buttonId;
	}
	
	/**
	 * Refreshes every tab for the specified {@code player}.
	 * @param player the player logging in.
	 */
	public static void refreshAll(Player player) {
		for(int i = 16016; i < 16016 + VALUES.size(); i++) {
			player.getMessages().sendString("", i);
		}
		PlayerPanel.TOOLS.refresh(player, "@or1@Quickies:");
		PlayerPanel.COMMUNITY.refresh(player, "@or2@ - Forums");
		PlayerPanel.DISCORD.refresh(player, "@or2@ - Discord");
		PlayerPanel.VOTE.refresh(player, "@or2@ - Vote");
		PlayerPanel.DONATE.refresh(player, "@or2@ - Donate");
		PlayerPanel.NPC_TOOL.refresh(player, "@or2@ - Monster Database");
		PlayerPanel.SERVER_STATISTICS.refresh(player, "@or1@Server Information:");
		PlayerPanel.UPTIME.refreshAll("@or2@ - Uptime: @yel@" + Utility.timeConvert(World.getRunningTime().elapsedTime(TimeUnit.MINUTES)));
		PlayerPanel.PLAYERS_IN_WILD.refreshAll("@or2@ - Players in wild: @yel@" + World.get().getPlayers().findAll(p -> p != null && Location.inWilderness(p)).size());
		PlayerPanel.STAFF_ONLINE.refreshAll("@or2@ - Staff online: @yel@" + World.get().getPlayers().findAll(p -> p != null && p.getRights().isStaff()).size());
		PlayerPanel.PLAYER_STATISTICS.refresh(player, "@or1@Player Information:");
		PlayerPanel.EMPTY.refresh(player, "");
		PlayerPanel.USERNAME.refresh(player, "@or2@ - Username: @yel@" + TextUtils.capitalize(player.getUsername()));
		PlayerPanel.PASSWORD.refresh(player, "@or2@ - Password: " + TextUtils.capitalize(TextUtils.passwordCheck(player.getPassword())));
		PlayerPanel.NIGHT.refresh(player, "@or2@ - Nightmare: @yel@" + (player.isNight() ? "@gre@yes" : "@red@no"));
		PlayerPanel.RANK.refresh(player, "@or2@ - Rank: @yel@" + TextUtils.capitalize(player.getRights().toString()));
		PlayerPanel.SLAYER_POINTS.refresh(player, "@or2@ - Slayer points: @yel@" + player.getSlayerPoints());
		PlayerPanel.SLAYER_TASK.refresh(player, "@or2@ - Slayer task: @yel@" + (player.getSlayer().isPresent() ? (player.getSlayer().get().toString()) : "none"));
		PlayerPanel.SLAYER_COUNT.refresh(player, "@or2@ - Completed tasks: @yel@" + player.getAttr().get("slayer_tasks").getInt());
		
		PlayerPanel.PVE_HEADER.refresh(player, "@or1@PvE Statistics:");
		PlayerPanel.HIGHEST_KILLSTREAK.refresh(player, "@or2@ - Highest Killstreak: @yel@" + player.getHighestKillstreak().get());
		PlayerPanel.CURRENT_KILLSTREAK.refresh(player, "@or2@ - Current Killstreak: @yel@" + player.getCurrentKillstreak().get());
		PlayerPanel.TOTAL_PLAYER_KILLS.refresh(player, "@or2@ - Total Players killed: @yel@" + player.getPlayerKills().get());
		PlayerPanel.TOTAL_PLAYER_DEATHS.refresh(player, "@or2@ - Total Player deaths: @yel@" + player.getDeathsByPlayer().get());
		PlayerPanel.TOTAL_NPC_KILLS.refresh(player, "@or2@ - Total Npcs killed: @yel@" + player.getNpcKills().get());
		PlayerPanel.TOTAL_NPC_DEATHS.refresh(player, "@or2@ - Total Npc deaths: @yel@" + player.getDeathsByNpc().get());
		
		PlayerPanel.EMPTY2.refresh(player, "");
		
		PlayerPanel.INDIVIDUAL_SCOREBOARD_STATISTICS.refresh(player, "@or1@Indiv. Scoreboard Statistics:");
		PlayerScoreboardStatistic s = World.getScoreboardManager().getPlayerScoreboard().get(player.getFormatUsername());
		PlayerPanel.INDIVIDUAL_HIGHEST_KILLSTREAKS.refresh(player, "@or2@ - Highest Killstreak: @yel@" + (s == null ? 0 : s.getHighestKillstreak()));
		PlayerPanel.INDIVIDUAL_CURRENT_KILLSTREAKS.refresh(player, "@or2@ - Current Killstreak: @yel@" + (s == null ? 0 : s.getCurrentKillstreak()));
		PlayerPanel.INDIVIDUAL_KILLS.refresh(player, "@or2@ - Players killed: @yel@" + (s == null ? 0 : s.getKills()));
		PlayerPanel.INDIVIDUAL_DEATHS.refresh(player, "@or2@ - Player deaths: @yel@" + (s == null ? 0 : s.getDeaths()));
	}
	
	/**
	 * Loops the button click interactions.
	 * @param player   the player clicking a button.
	 * @param buttonId the button id he clicked.
	 * @return {@code true} of he clicked a tab id, {@code false} otherwise.
	 */
	public static boolean interaction(Player player, int buttonId) {
		Optional<PlayerPanel> panel = VALUES.stream().filter(def -> def.getButtonId() == buttonId).findAny();
		
		if(!panel.isPresent()) {
			return false;
		}
		panel.get().onClick(player);
		return true;
		
	}
	
	/**
	 * The action to be done on the click.
	 * @param player the player doing the click.
	 */
	public void onClick(Player player) {
		//Empty
	}
	
	/**
	 * Refreshes the specified tab asset for all the players on the world.
	 * @param update the updated string for that tab.
	 */
	public void refreshAll(String update) {
		World.get().getPlayers().forEach(player -> refresh(player, update));
	}
	
	/**
	 * Refreshes the tab asset for a specified player
	 * @param player the player we're refreshing this {@code enumerator} for.
	 * @param text   the new string to set.
	 */
	public void refresh(Player player, String text) {
		player.getMessages().sendString(text, 16026 + ordinal());
	}
}