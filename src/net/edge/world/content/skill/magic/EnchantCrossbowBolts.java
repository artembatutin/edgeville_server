package net.edge.world.content.skill.magic;

import com.google.common.collect.ImmutableMap;
import net.edge.net.message.OutputMessages;
import net.edge.world.content.container.impl.Inventory;
import net.edge.world.content.skill.Skill;
import net.edge.world.content.skill.SkillData;
import net.edge.world.content.skill.Skills;
import net.edge.world.content.skill.action.impl.ProducingSkillAction;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class which is responsible for enchanting crossbow bolts.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class EnchantCrossbowBolts extends ProducingSkillAction {
	
	/**
	 * The data this skill action is dependent of.
	 */
	private final BoltData data;
	
	/**
	 * Constructs a new {@link EnchantCrossbowBolts}.
	 * @param player {@link #getPlayer()}.
	 */
	public EnchantCrossbowBolts(Player player, BoltData data) {
		super(player, Optional.empty());
		this.data = data;
	}
	
	/**
	 * Attempts to enchant our bolts.
	 * @param player   {@link #getPlayer()}.
	 * @param buttonId the button clicked.
	 * @return {@code true} if any of our bolts are enchanted, {@code false} otherwise.
	 */
	public static boolean enchant(Player player, int buttonId) {
		BoltData data = BoltData.VALUES.get(buttonId);
		
		if(data == null) {
			return false;
		}
		
		EnchantCrossbowBolts magic = new EnchantCrossbowBolts(player, data);
		magic.start();
		return true;
	}
	
	/**
	 * Attempts to open the enchant crossbow bolts interface.
	 * @param player   {@link #getPlayer()}.
	 * @param buttonId the button the player clicked.
	 * @return {@code true} if the interface was opened, {@code false} otherwise.
	 */
	public static boolean openInterface(Player player, int buttonId) {
		if(buttonId != 75007) {
			return false;
		}
		
		Skill magic = player.getSkills()[Skills.MAGIC];
		
		if(!magic.reqLevel(4)) {
			player.message("You need a magic level of 4 to cast this spell.");
			return false;
		}
		
		OutputMessages encoder = player.getMessages();
		
		Inventory inventory = player.getInventory();
		
		encoder.sendString("@gre@Magic 4", 49009);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49012);
		encoder.sendString((inventory.contains(new Item(556, 20)) ? "@gre@" : "@red@") + "20x", 49013);
		
		encoder.sendString((magic.reqLevel(7) ? "@gre@" : "@red@") + "Magic 7", 49017);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49020);
		encoder.sendString((inventory.contains(new Item(558, 10)) ? "@gre@" : "@red@") + "10x", 49021);
		encoder.sendString((inventory.contains(new Item(555, 10)) ? "@gre@" : "@red@") + "10x", 49088);
		
		encoder.sendString((magic.reqLevel(14) ? "@gre@" : "@red@") + "Magic 14", 49025);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49028);
		encoder.sendString((inventory.contains(new Item(557, 20)) ? "@gre@" : "@red@") + "20x", 49029);
		
		encoder.sendString((magic.reqLevel(24) ? "@gre@" : "@red@") + "Magic 24", 49033);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49036);
		encoder.sendString((inventory.contains(new Item(555, 20)) ? "@gre@" : "@red@") + "20x", 49037);
		
		encoder.sendString((magic.reqLevel(27) ? "@gre@" : "@red@") + "Magic 27", 49041);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49044);
		encoder.sendString((inventory.contains(new Item(556, 30)) ? "@gre@" : "@red@") + "30x", 49089);
		encoder.sendString((inventory.contains(new Item(561, 10)) ? "@gre@" : "@red@") + "10x", 49045);
		
		encoder.sendString((magic.reqLevel(29) ? "@gre@" : "@red@") + "Magic 29", 49049);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49052);
		encoder.sendString((inventory.contains(new Item(554, 20)) ? "@gre@" : "@red@") + "20x", 49053);
		
		encoder.sendString((magic.reqLevel(49) ? "@gre@" : "@red@") + "Magic 49", 49057);
		encoder.sendString((inventory.contains(new Item(564, 10)) ? "@gre@" : "@red@") + "10x", 49090);
		encoder.sendString((inventory.contains(new Item(554, 50)) ? "@gre@" : "@red@") + "50x", 49060);
		encoder.sendString((inventory.contains(new Item(565, 10)) ? "@gre@" : "@red@") + "10x", 49061);
		
		encoder.sendString((magic.reqLevel(57) ? "@gre@" : "@red@") + "Magic 57", 49065);
		encoder.sendString((inventory.contains(new Item(557, 100)) ? "@gre@" : "@red@") + "100x", 49068);
		encoder.sendString((inventory.contains(new Item(563, 20)) ? "@gre@" : "@red@") + "20x", 49069);
		
		encoder.sendString((magic.reqLevel(68) ? "@gre@" : "@red@") + "Magic 68", 49073);
		encoder.sendString((inventory.contains(new Item(563, 150)) ? "@gre@" : "@red@") + "150x", 49076);
		encoder.sendString((inventory.contains(new Item(566, 10)) ? "@gre@" : "@red@") + "10x", 49077);
		
		encoder.sendString((magic.reqLevel(87) ? "@gre@" : "@red@") + "Magic 87", 49081);
		encoder.sendString((inventory.contains(new Item(554, 200)) ? "@gre@" : "@red@") + "200x", 49084);
		encoder.sendString((inventory.contains(new Item(560, 10)) ? "@gre@" : "@red@") + "10x", 49085);
		
		encoder.sendInterface(49000);
		return true;
	}
	
	@Override
	public Optional<Item[]> removeItem() {
		return Optional.of(data.required);
	}
	
	@Override
	public Optional<Item[]> produceItem() {
		return Optional.of(new Item[]{data.produced});
	}
	
	@Override
	public int delay() {
		return 3;
	}
	
	@Override
	public boolean instant() {
		return true;
	}
	
	@Override
	public boolean init() {
		player.getMessages().sendCloseWindows();
		return checkMagic();
	}
	
	@Override
	public boolean canExecute() {
		return checkMagic();
	}
	
	@Override
	public double experience() {
		return data.experience;
	}
	
	@Override
	public SkillData skill() {
		return SkillData.MAGIC;
	}
	
	private boolean checkMagic() {
		if(!player.getSkills()[skill().getId()].reqLevel(data.level)) {
			player.message("You need a magic level of " + data.level + " to cast this spell.");
			return false;
		}
		return true;
	}
	
	/**
	 * The enumerated type whose elements represent a set of constants used to
	 * define the bolts that can be enchanted.
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	private enum BoltData {
		OPAL(191110, new Item[]{new Item(564, 10), new Item(556, 20), new Item(879, 10)}, 9236, 4, 9),
		SAPPHIRE(191119, new Item[]{new Item(564, 10), new Item(558, 10), new Item(555, 10), new Item(9337, 10)}, 9240, 7, 17),
		JADE(191127, new Item[]{new Item(564, 10), new Item(557, 20), new Item(9335, 10)}, 9237, 14, 19),
		PEARL(191135, new Item[]{new Item(564, 10), new Item(555, 20), new Item(880, 10)}, 9238, 24, 29),
		EMERALD(191143, new Item[]{new Item(564, 10), new Item(561, 10), new Item(556, 30), new Item(9338, 10)}, 9241, 27, 37),
		TOPAZ(191151, new Item[]{new Item(564, 10), new Item(554, 20), new Item(9336, 10)}, 9239, 29, 33),
		RUBY(191159, new Item[]{new Item(564, 10), new Item(565, 10), new Item(554, 50), new Item(9339, 10)}, 9242, 49, 59),
		DIAMOND(191167, new Item[]{new Item(564, 10), new Item(563, 20), new Item(557, 100), new Item(9340, 10)}, 9243, 57, 67),
		DRAGONSTONE(191175, new Item[]{new Item(564, 10), new Item(566, 10), new Item(557, 150), new Item(9341, 10)}, 9244, 68, 78),
		ONYX(191183, new Item[]{new Item(564, 10), new Item(554, 200), new Item(560, 10), new Item(9342, 10)}, 9245, 87, 97);
		
		/**
		 * Caches our enum values.
		 */
		private static final ImmutableMap<Integer, BoltData> VALUES = ImmutableMap.copyOf(Stream.of(values()).collect(Collectors.toMap(t -> t.buttonId, Function.identity())));
		
		/**
		 * The button identification for this bolt.
		 */
		private final int buttonId;
		
		/**
		 * The items required.
		 */
		private final Item[] required;
		
		/**
		 * The item produced.
		 */
		private final Item produced;
		
		/**
		 * The level required to cast this spell.
		 */
		private final int level;
		
		/**
		 * The experience gained upon casting this spell.
		 */
		private final double experience;
		
		/**
		 * Constructs a new {@link BoltData}.
		 * @param buttonId   {@link #buttonId}
		 * @param required   {@link #required}.
		 * @param produced   {@link #produced}.
		 * @param level      {@link #level}.
		 * @param experience {@link #experience}.
		 */
		private BoltData(int buttonId, Item[] required, int produced, int level, double experience) {
			this.buttonId = buttonId;
			this.required = required;
			this.produced = new Item(produced);
			this.produced.setAmount(10);
			this.level = level;
			this.experience = experience;
		}
	}
}
