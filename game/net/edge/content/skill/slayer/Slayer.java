package net.edge.content.skill.slayer;

import net.edge.locale.Position;
import net.edge.util.TextUtils;
import net.edge.util.rand.RandomUtils;
import net.edge.content.PlayerPanel;
import net.edge.content.dialogue.Dialogue;
import net.edge.content.dialogue.impl.*;
import net.edge.content.market.currency.Currency;
import net.edge.content.skill.Skill;
import net.edge.content.skill.Skills;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.npc.NpcDefinition;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds functionality for the Slayer skill.
 * @author <a href="http://www.rune-server.org/members/Stand+Up/">Stan</a>
 */
public final class Slayer {
	
	/**
	 * A map containing all the slayer masters with the possible tasks they can give.
	 */
	public static final Map<SlayerMaster, SlayerKeyPolicy[]> SLAYER_KEYS = new HashMap<>();
	
	/**
	 * A map which contains each slayer key by the position of the npcs.
	 */
	public static final Map<String, SlayerLocationPolicy> SLAYER_LOCATIONS = new HashMap<>();
	
	/**
	 * A map which contains each slayer key by the level required slayer.
	 */
	public static final Map<String, Integer> SLAYER_LEVELS = new HashMap<>();
	
	/**
	 * The slayer master this player is on.
	 */
	private final SlayerMaster master;
	
	/**
	 * The slayer key policy this player has.
	 */
	private final String key;
	
	/**
	 * The slayer key policy this player has.
	 */
	private final SlayerDifficulty difficulty;
	
	/**
	 * The amount of points received at the end of the task.
	 */
	public final int points;
	
	/**
	 * The amount of times this player has to kill the task.
	 */
	private int amount;
	
	/**
	 * Constructs a new {@link Slayer}.
	 */
	public Slayer(SlayerMaster master, SlayerKeyPolicy policy) {
		this.master = master;
		this.key = policy.getKey();
		this.amount = RandomUtils.random(policy.getAmount());
		this.difficulty = policy.getDifficulty();
		this.points = (int) (amount * 0.2 * ((difficulty.ordinal() + 1) * 2.4));
	}
	
	/**
	 * Attempts to use contact the slayer master through the slayer gem.
	 * @param player the player attempting to contact the slayer master.
	 * @param item   the item that was interacted with.
	 * @param option the option of the gem that was used.
	 * @return {@code true} if the player managed to contact, {@code false} otherwise.
	 */
	public static boolean contact(Player player, Item item, int option) {
		if(item.getId() != 4155) {
			return false;
		}
		
		SlayerMaster master = player.getSlayer().isPresent() ? player.getSlayer().get().getMaster() : SlayerMaster.SPRIA;
		
		switch(option) {
			case 1:
				player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "Ughh, what do you want?"), new OptionDialogue(t -> {
							if(t.equals(OptionDialogue.OptionType.FIRST_OPTION)) {
								player.getDialogueBuilder().go(3);
							} else if(t.equals(OptionDialogue.OptionType.SECOND_OPTION)) {
								player.getDialogueBuilder().advance();
							} else {
								player.getDialogueBuilder().last();
							}
						}, "Can I get a new task?", "Howmany kills are left?", "Nevermind"), new PlayerDialogue("Howmany kills are left?"), new NpcDialogue(master.getNpcId(), player.getSlayer().isPresent() ? new String[]{"You must kill another " + player.getSlayer().get().amount + " " + player.getSlayer().get().toString() + "."} : new String[]{"You don't have a slayer task, come speak to ", "me or another slayer master in order to get assigned ", "to a task."}).attach(() -> player.getMessages().sendCloseWindows()), new PlayerDialogue("Can I get a new task?"), new NpcDialogue(master.getNpcId(), player.getSlayer().isPresent() ? new String[]{"You already are assigned to a slayer task..."} : new String[]{"Come speak to me or another slayer master ", "in order to get assigned to a task."}).attach(() -> player.getMessages().sendCloseWindows()), new PlayerDialogue("Nevermind").attach(() -> player.getMessages().sendCloseWindows())
				
				);
				break;
			case 2:
				if(player.getSlayer().isPresent()) {
					player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "You must kill another " + player.getSlayer().get().amount + " " + player.getSlayer().get().toString() + "."));
				} else {
					player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "You don't have a slayer task, come speak to ", "me or another slayer master in order to get assigned ", "to a task."));
				}
				break;
		}
		
		return true;
	}
	
	/**
	 * Opens the slayer panel for a particular player.
	 * @param player the player interacting with the panel.
	 */
	public static void openPanel(Player player) {
		player.getMessages().sendInterface(-10);
		updateBlocked(player);
	}
	
	/**
	 * Appends a slayer task for the specified player with a dialogue.
	 * @param player the player this task should be appended to.
	 * @param npcId  the npc id the player interacted with.
	 * @return <true> if the task was appended, <false> otherwise.
	 */
	public static boolean append(Player player, int npcId) {
		Optional<SlayerMaster> has_master = SlayerMaster.getDefinition(npcId);
		
		if(!has_master.isPresent()) {
			return false;
		}
		
		SlayerMaster master = has_master.get();
		
		if(!player.getSkills()[Skills.SLAYER].reqLevel(master.getRequirement())) {
			player.message("You need a slayer level of " + master.getRequirement() + " to access " + TextUtils.capitalize(master.toString().toLowerCase()) + ".");
			return false;
		}
		
		player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "'Ello, and what are you after, then?"), new OptionDialogue(t -> {
			if(t.equals(OptionDialogue.OptionType.FIRST_OPTION)) {
				Optional<Slayer> task = getTask(player, master);
				
				Dialogue[] dialogues = player.getSlayer().isPresent() ? new Dialogue[]{new PlayerDialogue("I need another assignment."), new NpcDialogue(master.getNpcId(), "You already have a slayer task.", "Speak to me once you have completed it.")} : !task.isPresent() ? new Dialogue[]{new PlayerDialogue("I need another assignment."), new NpcDialogue(master.getNpcId(), "There was no task found, please try again shortly.")} : new Dialogue[]{new PlayerDialogue("I need another assignment."), new NpcDialogue(master.getNpcId(), "Excellent, you're doing great, your new task", "is to kill " + task.get().amount + " " + TextUtils.capitalize(task.get().getKey().toLowerCase() + ".")).attach(() -> player.setSlayer(Optional.of(task.get())))};
				
				player.getDialogueBuilder().append(dialogues);
			} else if(t.equals(OptionDialogue.OptionType.SECOND_OPTION)) {
				teleport(player, master);
			} else if(t.equals(OptionDialogue.OptionType.THIRD_OPTION)) {
				player.getDialogueBuilder().advance();
			} else {
				player.getDialogueBuilder().last();
			}
		}, "I need another assignment.", "I want to teleport to my assignment.", "Can I buy a slayer gem?", "Nevermind."), new PlayerDialogue("Can I buy a slayer gem?"), new NpcDialogue(master.getNpcId(), "Yes, it costs 10,000 gp."), new OptionDialogue(t -> {
			if(t.equals(OptionDialogue.OptionType.FIRST_OPTION)) {
				player.getDialogueBuilder().advance();
			} else {
				player.getDialogueBuilder().skip();
			}
		}, "Buy the slayer gem", "Nevermind"), new RequestItemDialogue(new Item(995, 10000), new Item(4155), "You hand over 10,000 coins to buy \\na slayer gem.", Optional.empty()).attach(() -> player.getMessages().sendCloseWindows()), new PlayerDialogue("Nevermind").attach(() -> player.getMessages().sendCloseWindows()));
		return true;
	}
	
	/**
	 * Checks if the specified {@code player} can attack the {@code npc}.
	 * @param player the player to check for.
	 * @param npc    the npc being attacked.
	 * @return <true> if the player can, <false> otherwise.
	 */
	public static boolean canAttack(Player player, Npc npc) {
		if(!player.getSkills()[Skills.SLAYER].reqLevel(npc.getDefinition().getSlayerRequirement())) {
			player.message("You need a slayer level of " + npc.getDefinition().getSlayerRequirement() + " to slay this creature.");
			return false;
		}
		return true;
	}
	
	/**
	 * Decrements the remaining slayer task by 1 if the specified {@code npc}
	 * was assigned as a task.
	 * @param player the player to decrement this for.
	 * @param npc    the npc to check for.
	 * @return <true> if the remaining slayer task was decremented, <false> otherwise.
	 */
	public static boolean decrement(Player player, Npc npc) {
		if(!player.getSlayer().isPresent()) {
			return false;
		}
		
		Slayer slayer = player.getSlayer().get();
		
		if(!slayer.getKey().equals(npc.getDefinition().getSlayerKey())) {
			return false;
		}
		
		slayer.amount--;
		
		if(slayer.amount < 1) {
			player.message("You have completed your slayer task.");
			player.message("To get another slayer task go talk to a slayer master.");
			Skills.experience(player, slayer.getDifficulty().getValue() * (100 + RandomUtils.inclusive(5, 5 + (15 * (slayer.getAmount() / 2)))), Skills.SLAYER);
			player.updateSlayers(slayer.points);
			player.setSlayer(Optional.empty());
			player.getAttr().get("slayer_tasks").set(player.getAttr().get("slayer_tasks").getInt() + 1);
			PlayerPanel.SLAYER_COUNT.refresh(player, "@or2@ - Completed tasks: @yel@" + player.getAttr().get("slayer_tasks").getInt());
			return true;
		} else {
			PlayerPanel.SLAYER_TASK.refresh(player, "@or2@ - Slayer task: @yel@" + (player.getSlayer().isPresent() ? (player.getSlayer().get().getAmount() + " " + player.getSlayer().get().toString()) : "none"));
		}
		
		String npc_indefinite_article = TextUtils.appendIndefiniteArticle(npc.getDefinition().getName().toLowerCase());
		player.message("You have defeated " + npc_indefinite_article + ", only " + slayer.amount + " more to go.");
		Skills.experience(player, slayer.getDifficulty().getValue() * (50 + RandomUtils.inclusive(1, 25)), Skills.SLAYER);
		return true;
	}
	
	public static boolean clickButton(Player player, int button) {
		SlayerMaster master = player.getSlayer().isPresent() ? player.getSlayer().get().getMaster() : SlayerMaster.SPRIA;
		if(button >= 113 && button <= 117) {
			player.getBlockedTasks()[button - 113] = null;
			updateBlocked(player);
			return true;
		}
		switch(button) {
			case 110://teleport
				teleport(player, master);
				return true;
			case 111:
				skip(player, master);
				return true;
			case 112:
				block(player, master);
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Blocks a slayer task.
	 * @param player the player doing slayer.
	 * @param master the master associated to the task.
	 */
	public static void block(Player player, SlayerMaster master) {
		if(!player.getSlayer().isPresent()) {
			player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "You don't have a slayer assignment."));
			return;
		}
		int open = getOpen(player);
		if(open != -1 && Currency.SLAYER_POINTS.getCurrency().currencyAmount(player) >= 100) {
			Currency.SLAYER_POINTS.getCurrency().takeCurrency(player, 100);
			player.getBlockedTasks()[open] = player.getSlayer().get().getKey();
			player.setSlayer(Optional.empty());
			player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "I have successfully blocked and cleared this", "assignment for you."));
			updateBlocked(player);
		} else if(open == -1) {
			player.message("You cannot block this slayer task as you have no more space to block it.");
		} else {
			player.message("You do not have enough slayer points to do this.");
		}
	}
	
	/**
	 * Skips a slayer task.
	 * @param player the player doing slayer.
	 * @param master the master associated to the task.
	 */
	private static void skip(Player player, SlayerMaster master) {
		if(!player.getSlayer().isPresent()) {
			player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "You don't have a slayer assignment."));
			return;
		}
		
		if(Currency.SLAYER_POINTS.getCurrency().currencyAmount(player) >= 30) {
			Currency.SLAYER_POINTS.getCurrency().takeCurrency(player, 30);
			player.setSlayer(Optional.empty());
			player.getDialogueBuilder().append(new NpcDialogue(master.getNpcId(), "I have successfully reset your slayer assignment"));
		} else {
			player.message("You do not have enough slayer points to do this.");
		}
	}
	
	public static int getOpen(Player player) {
		for(int i = 0; i < 5; i++) {
			if(player.getBlockedTasks()[i] == null) {
				return i;
			}
		}
		return -1;
	}
	
	private static void updateBlocked(Player player) {
		//Blocked tasks.
		for(int i = 0; i < 5; i++) {
			String blocked = player.getBlockedTasks()[i];
			if(blocked == null)
				player.getMessages().sendString("empty", 254 + i);
			else
				player.getMessages().sendString(TextUtils.capitalize(blocked.toLowerCase()), 254 + i);
		}
	}
	
	/**
	 * The teleport to task function.
	 * @param player the player doing slayer.
	 * @param master the master associated to the task.
	 */
	public static void teleport(Player player, SlayerMaster master) {
		List<Dialogue> dialogues = new ArrayList<>();
		
		dialogues.add(new PlayerDialogue("I want to teleport to my assignment."));
		
		if(!player.getSlayer().isPresent()) {
			dialogues.add(new NpcDialogue(master.getNpcId(), "You currently don't have a slayer task."));
			player.getDialogueBuilder().append(dialogues.toArray(new Dialogue[dialogues.size()]));
			return;
		}
		
		SlayerLocationPolicy location = SLAYER_LOCATIONS.get(player.getSlayer().get().getKey());
		
		if(location == null) {
			dialogues.add(new NpcDialogue(master.getNpcId(), "This location currently doesn't exist, please report", "it on the forums."));
			player.getDialogueBuilder().append(dialogues.toArray(new Dialogue[dialogues.size()]));
			return;
		}
		
		if(location.getPrice() == 0) {
			dialogues.add(new NpcDialogue(master.getNpcId(), "Alright, teleporting to this task will be free."));
		} else {
			dialogues.add(new NpcDialogue(master.getNpcId(), "Alright, that will be " + location.getPrice() + " coins."));
		}
		
		Dialogue dialogue = location.getPrice() == 0 ? new StatementDialogue("You teleport to your task for free.").attach(() -> player.move(RandomUtils.random(location.getPositions()))) : new RequestItemDialogue(new Item(995, location.getPrice()), "You handed over " + location.getPrice() + " coins to be \\nteleported to your assignment.", Optional.of(() -> player.move(RandomUtils.random(location.getPositions()))));
		
		player.getMessages().sendInterface(-1);
		
		dialogues.add(dialogue);
		
		player.getDialogueBuilder().append(dialogues.toArray(new Dialogue[dialogues.size()]));
	}
	
	/**
	 * Decrements the players remaining slayer task by the specified {@code amount}.
	 * @param player the player to decrement this for.
	 * @param amount the amount to decrement.
	 * @return <true> if the remaining slayer task was decremented, <false> otherwise.
	 */
	public static boolean decrement(Player player, int amount) {
		if(!player.getSlayer().isPresent()) {
			return false;
		}
		
		Slayer slayer = player.getSlayer().get();
		
		slayer.amount = slayer.amount - amount < 1 ? 0 : slayer.amount - amount;
		return true;
	}
	
	/**
	 * Gets and assigns a task for the specified {@code player} from the specified
	 * {@code master} with the specified {@code difficulty}.
	 * @param player the player this task is for.
	 * @param master the master this task is from.
	 */
	public static Optional<Slayer> getTask(Player player, SlayerMaster master) {
		Skill skill = player.getSkills()[Skills.SLAYER];
		int combat = player.determineCombatLevel();
		List<String> blocked = Arrays.asList(player.getBlockedTasks());
		List<SlayerKeyPolicy> policy_values = Arrays.stream(SLAYER_KEYS.get(master)).filter(s -> {
			if(skill.getRealLevel() < SLAYER_LEVELS.getOrDefault(s.getKey(), 99))
				return false;//Player wont be able to do slayer on a higher boss slayer requirement.
			if(!NpcDefinition.fromSlayerKey(s.getKey()).isPresent())
				return false;//Awful way to check through a loop but alright... - we checking if any npc key exist of that type.
			if(blocked.contains(s.getKey()))
				return false;//The player blocked this task.
			return true;
		}).collect(Collectors.toList());
		
		if(policy_values.isEmpty()) {
			return Optional.empty();
		}
		
		SlayerKeyPolicy policy = RandomUtils.random(policy_values);
		return Optional.of(new Slayer(master, policy));
	}
	
	/**
	 * @return the master
	 */
	public SlayerMaster getMaster() {
		return master;
	}
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * @return the difficulty
	 */
	public SlayerDifficulty getDifficulty() {
		return difficulty;
	}
	
	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}
	
	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	@Override
	public String toString() {
		return TextUtils.capitalize(key.toLowerCase());
	}
	
}