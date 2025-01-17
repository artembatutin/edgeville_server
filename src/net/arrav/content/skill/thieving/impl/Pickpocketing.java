package net.arrav.content.skill.thieving.impl;

import net.arrav.action.impl.MobAction;
import net.arrav.content.skill.Skills;
import net.arrav.content.skill.thieving.Thieving;
import net.arrav.task.Task;
import net.arrav.util.TextUtils;
import net.arrav.util.rand.RandomUtils;
import net.arrav.world.Animation;
import net.arrav.world.Graphic;
import net.arrav.world.entity.actor.combat.hit.Hit;
import net.arrav.world.entity.actor.combat.hit.HitIcon;
import net.arrav.world.entity.actor.combat.hit.Hitsplat;
import net.arrav.world.entity.actor.mob.Mob;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.item.Item;

import java.util.Optional;

/**
 * Represents functionality for pickpocketing from various npcs.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class Pickpocketing extends Thieving {

	/**
	 * The definition for this theft.
	 */
	private final PickpocketData definition;

	/**
	 * The current mob we're interacting with.
	 */
	private final Mob mob;

	/**
	 * The possible loot for pickpocketing.
	 */
	private final Item loot;

	/**
	 * Represents the animation specific to pickpocketing.
	 */
	private static final Animation ANIMATION = new Animation(881);

	/**
	 * Represents the mob animation(hitting player).
	 */
	private static final Animation NPC_ANIMATION = new Animation(422);

	/**
	 * Represents the animation specific to pickpocketing.(block animation)
	 */
	private static final Animation STUN_ANIMATION = new Animation(424);

	/**
	 * The graphic id when player is stunned
	 */
	private static final Graphic STUN_GRAPHIC = new Graphic(80, 100);

	/**
	 * Constructs a new {@link Pickpocketing}.
	 * @param player {@link #getPlayer()}.
	 * @param data the definition of this theft.
	 * @param mob the mob this player is stealing from.
	 */
	private Pickpocketing(Player player, PickpocketData data, Mob mob) {
		super(player, mob.getPosition());
		this.definition = data;
		this.mob = mob;
		this.loot = RandomUtils.random(definition.loot);
	}

	public static void action() {
		for(PickpocketData data : PickpocketData.values()) {
			MobAction e = new MobAction() {
				@Override
				public boolean click(Player player, Mob npc, int click) {
					Pickpocketing thieving = new Pickpocketing(player, data, npc);
					thieving.start();
					return true;
				}
			};
			for(int n : data.npcId) {
				if(n == 3299) {//master farmer
					e.registerFourth(n);
				} else {
					e.registerSecond(n);
				}
			}
		}
	}

	/**
	 * The lower the return value, the lower the failure rate
	 * @return an integer to determine how often you will fail.
	 */
	//	private double failureRate() {
	//		double successRate = (5/833)*getPlayer().getSkills()[Skills.THIEVING].getLevel() + 17/49;
	//		return (levelFactor + npcFactor) / 2;
	//	}
	@Override
	public boolean failure() {
		if(mob.getDefinition().getName().contains("Master")) {
			return !RandomUtils.success(((((double) 5 / 833) * getPlayer().getSkills()[Skills.THIEVING].getLevel()) + ((double) 17 / 49)));
		}
		return (RandomUtils.inclusive(getPlayer().getSkills()[Skills.THIEVING].getLevel() + RandomUtils.inclusive(5)) < (RandomUtils.inclusive(definition.requirement) + RandomUtils.inclusive((5))));
	}

	@Override
	public int requirement() {
		return definition.requirement;
	}

	@Override
	public Optional<Animation> startAnimation() {
		return Optional.of(ANIMATION);
	}

	@Override
	public boolean canInit() {
		String name = mob.getDefinition().getName();
		if(!getPlayer().getSkills()[skill().getId()].reqLevel(requirement())) {
			getPlayer().message("You need a thieving level of " + requirement() + " to steal from " + TextUtils.appendIndefiniteArticle(name) + ".");
			return false;
		}

		if(!getPlayer().getInventory().hasCapacityFor(loot)) {
			player.message("You don't have enough inventory space for the loot.");
			return false;
		}
		if(getPlayer().isStunned()) {
			return false;
		}
		if(!player.getSkills()[skill().getId()].getDelay().elapsed(1800)) {
			return false;
		}
		player.getSkills()[skill().getId()].getDelay().reset();
		return true;
	}

	@Override
	public Item loot() {
		return loot;
	}

	@Override
	public void onSubmit() {

	}

	@Override
	public void onExecute(Task t) {
		if(failure()) {
			mob.forceChat("What do you think you're doing?");
			if(mob.getDefinition().isAttackable())
				mob.animation(new Animation(mob.getDefinition().getAttackAnimation()));
			else
				mob.animation(NPC_ANIMATION);
			int hit = RandomUtils.inclusive(1, definition.damage);
			getPlayer().damage(new Hit(hit, Hitsplat.NORMAL, HitIcon.NONE));
			getPlayer().animation(STUN_ANIMATION);
			getPlayer().graphic(STUN_GRAPHIC);
			getPlayer().stun(definition.seconds);
		} else {
			getPlayer().getInventory().add(loot);
			getPlayer().message("You pick the victims pocket.");
		}
		t.cancel();
	}

	@Override
	public int delay() {
		return 3;
	}

	@Override
	public boolean instant() {
		return false;
	}

	@Override
	public boolean canExecute() {
		return true;
	}

	@Override
	public double experience() {
		return definition.experience;
	}

	private enum PickpocketData {
		MAN(new int[]{1, 2, 3}, new Item[]{new Item(995, 3)}, 1, 8, 3, 10),
		WOMAN(new int[]{4, 5, 6}, new Item[]{new Item(995, 3)}, 1, 8, 3, 10),
		FARMER(new int[]{7, 1757, 1758, 1759, 1760, 1761}, new Item[]{new Item(995, 9), new Item(5318, 1)}, 10, 14.5, 3, 10),
		FEMALE_HAM_MEMBER(new int[]{1715}, new Item[]{new Item(882, 20), new Item(1351, 1), new Item(1265, 1), new Item(1349, 1), new Item(1267, 1), new Item(886, 20), new Item(1353, 1), new Item(1207, 1), new Item(1129, 1), new Item(4170, 1), new Item(4302, 1), new Item(4298, 1), new Item(4300, 1), new Item(4304, 1), new Item(4306, 1), new Item(4308, 1), new Item(4310, 1), new Item(995, 21), new Item(319, 1), new Item(2138, 1), new Item(668, 1), new Item(453, 1), new Item(440, 1), new Item(1739, 1), new Item(314, 5), new Item(1734, 6), new Item(1733, 1), new Item(1511, 1), new Item(686, 1), new Item(697, 1), new Item(1625, 1), new Item(1627, 1), new Item(1617, 1), new Item(199, 1), new Item(201, 1), new Item(203, 1)}, 15, 18.5, 3, 30),
		MALE_HAM_MEMBER(new int[]{1714, 1715}, new Item[]{new Item(882, 20), new Item(1351, 1), new Item(1265, 1), new Item(1349, 1), new Item(1267, 1), new Item(886, 20), new Item(1353, 1), new Item(1207, 1), new Item(1129, 1), new Item(4170, 1), new Item(4302, 1), new Item(4298, 1), new Item(4300, 1), new Item(4304, 1), new Item(4306, 1), new Item(4308, 1), new Item(4310, 1), new Item(995, 21), new Item(319, 1), new Item(2138, 1), new Item(668, 1), new Item(453, 1), new Item(440, 1), new Item(1739, 1), new Item(314, 5), new Item(1734, 6), new Item(1733, 1), new Item(1511, 1), new Item(686, 1), new Item(697, 1), new Item(1625, 1), new Item(1627, 1), new Item(199, 1), new Item(201, 1), new Item(203, 1)}, 20, 22.5, 3, 30),
		HAM_GUARD(new int[]{1710, 1711, 1712}, new Item[]{new Item(882, 20), new Item(1351, 1), new Item(1265, 1), new Item(1349, 1), new Item(1267, 1), new Item(886, 20), new Item(1353, 1), new Item(1207, 1), new Item(1129, 1), new Item(4170, 1), new Item(4302, 1), new Item(4298, 1), new Item(4300, 1), new Item(4304, 1), new Item(4306, 1), new Item(4308, 1), new Item(4310, 1), new Item(995, 21), new Item(319, 1), new Item(2138, 1), new Item(668, 1), new Item(453, 1), new Item(440, 1), new Item(1739, 1), new Item(314, 5), new Item(1734, 6), new Item(1733, 1), new Item(1511, 1), new Item(686, 1), new Item(697, 1), new Item(1625, 1), new Item(1627, 1), new Item(199, 1), new Item(201, 1), new Item(203, 1), new Item(5321, 4), new Item(5323, 4), new Item(5319, 4), new Item(5324, 1)}, 20, 22.5, 3, 30),
		WARRIOR_WOMAN(new int[]{15}, new Item[]{new Item(995, 18)}, 25, 26, 3, 20),
		AL_KHARID_WARRIOR(new int[]{18}, new Item[]{new Item(995, 18)}, 25, 26, 3, 20),
		ROGUE(new int[]{187}, new Item[]{new Item(995, 120), new Item(556, 8), new Item(1523, 1), new Item(1219, 1), new Item(1993, 1), new Item(2357, 1), new Item(1227, 1)}, 32, 35.5, 3, 20),
		GUARD(new int[]{9, 10, 5920, 3408}, new Item[]{new Item(995, 700), new Item(2351, 1), new Item(199, 1), new Item(313, 4), new Item(453, 1), new Item(562, 12), new Item(712, 1), new Item(950, 1), new Item(1623, 1)}, 40, 46.5, 3, 20),
		POLLNIVIAN_BEARDED_BANDIT(new int[]{1880, 1881}, new Item[]{new Item(995, 40)}, 45, 65, 3, 50),
		DESERT_BANDIT(new int[]{1926, 1927, 1928, 1929, 1930, 1931}, new Item[]{new Item(995, 30), new Item(179, 1), new Item(1523, 1)}, 53, 79.5, 3, 30),
		KNIGHT_OF_ARDOUGNE(new int[]{23, 26}, new Item[]{new Item(995, 50)}, 55, 84.3, 3, 30),
		POLLNIVIAN_BANDIT(new int[]{1883, 1884}, new Item[]{new Item(995, 50)}, 55, 84.3, 3, 50),
		YANILLE_WATCHMEN(new int[]{32}, new Item[]{new Item(995, 60), new Item(2309, 1)}, 65, 137.5, 3, 30),
		MENAPHITE_THUG(new int[]{1904, 1905}, new Item[]{new Item(995, 60)}, 65, 137.5, 3, 50),
		PALADIN(new int[]{20, 365, 2256}, new Item[]{new Item(995, 80), new Item(562, 2)}, 70, 151.75, 4, 3),
		GNOME(new int[]{66, 67, 68}, new Item[]{new Item(995, 300), new Item(557, 1), new Item(444, 1), new Item(569, 1), new Item(2150, 1), new Item(2162, 1)}, 75, 198.5, 4, 10),
		MASTER_FARMER(new int[]{2234, 2235, 3299}, new Item[]{new Item(5318, 1), new Item(5319, 1), new Item(5324, 3), new Item(5323, 2), new Item(5321, 2), new Item(5305, 4), new Item(5307, 2), new Item(5308, 2), new Item(5306, 3), new Item(5309, 2), new Item(5310, 1), new Item(5311, 1), new Item(5101, 1), new Item(5102, 1), new Item(5103, 1), new Item(5104, 1), new Item(5105, 1), new Item(5106, 1), new Item(5096, 1), new Item(5097, 1), new Item(5098, 1), new Item(5099, 1), new Item(5100, 1), new Item(5291, 1), new Item(5292, 1), new Item(5293, 1), new Item(5294, 1), new Item(5295, 1), new Item(5296, 1), new Item(5297, 1), new Item(5298, 1), new Item(5299, 1), new Item(5300, 1), new Item(5301, 1), new Item(5302, 1), new Item(5303, 1), new Item(5304, 1), new Item(5280, 1), new Item(5281, 1)}, 38, 43, 3, 30),
		HERO(new int[]{21}, new Item[]{new Item(995, 300), new Item(560, 2), new Item(565, 1), new Item(444, 1), new Item(1993, 1), new Item(569, 1), new Item(1601, 1)}, 82, 273.3, 5, 40);

		/**
		 * The identifiers which represents this mob.
		 */
		private final int[] npcId;

		/**
		 * The loot obtained upon pickpocketing this mob.
		 */
		private final Item[] loot;

		/**
		 * The requirement required for pickpocketing this mob.
		 */
		private final int requirement;

		/**
		 * The experience gained upon pickpockting this mob.
		 */
		private final double experience;

		/**
		 * The amount of seconds this player stays stunned upon failing to pickpocket this mob.
		 */
		private final int seconds;

		/**
		 * The amount of damage this players get inflicted upon failing to pickpocket this mob.
		 */
		private final int damage;

		PickpocketData(int[] npcId, Item[] loot, int requirement, double experience, int seconds, int damage) {
			this.npcId = npcId;
			this.loot = loot;
			this.requirement = requirement;
			this.experience = experience / 2;
			this.seconds = seconds;
			this.damage = damage;
		}
	}

}
