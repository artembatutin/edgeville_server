package net.edge.content.skill.fishing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.edge.locale.loc.Location;
import net.edge.world.node.entity.player.Player;

import java.util.EnumSet;
import java.util.Optional;

public enum Catchable {
	// TODO: Some way of catching Manta ray?
	LEATHER_BOOTS(1061, 1, 0.30, 1),
	LEATHER_GLOVES(1059, 1, 0.30, 1),
	SEAWEED(401, 1, 0.30, 1),
	SHRIMP(317, 1, 0.20, 10),
	SARDINE(327, 5, 0.4, 20),
	HERRING(345, 10, 0.3, 30),
	ANCHOVY(321, 15, 0.3, 40),
	MACKEREL(353, 16, 0.2, 20),
	CASKET(405, 16, 0.01, 100),
	OYSTER(407, 16, 0.05, 80),
	TROUT(335, 20, 0.4, 50),
	COD(341, 23, 0.2, 45),
	PIKE(349, 25, 0.2, 60),
	SLIMY_EEL(3379, 28, 0.15, 65) {
		@Override
		public boolean catchable(Player player) {
			return Location.inWilderness(player);
		}
	},
	SALMON(331, 30, 0.18, 70),
	TUNA(359, 35, 0.15, 80),
	CAVE_EEL(5001, 38, 0.13, 80) {
		@Override
		public boolean catchable(Player player) {
			return Location.inWilderness(player);
		}
	},
	LOBSTER(377, 40, 0.12, 90),
	BASS(363, 46, 0.10, 100),
	SWORDFISH(371, 50, 0.10, 100),
	LAVA_EEL(2148, 53, 0.10, 60) {
		@Override
		public boolean catchable(Player player) {
			return Location.inWilderness(player);
		}
	},
	MONKFISH(7944, 62, 0.05, 120),
	SHARK(383, 76, 0.03, 110);

	private static final ImmutableSet<Catchable> VALUES = Sets.immutableEnumSet(EnumSet.allOf(Catchable.class));

	private final int id;
	private final int level;
	private final double chance;
	private final double experience;

	Catchable(int id, int level, double chance, double experience) {
		this.id = id;
		this.level = level;
		this.chance = chance;
		this.experience = experience;
	}

	@Override
	public final String toString() {
		return name().toLowerCase().replaceAll("_", " ");
	}

	public boolean catchable(Player player) {
		return true;
	}

	protected static Optional<Catchable> getCatchable(int id) {
		return VALUES.stream().filter(def -> def.id == id).findAny();
	}

	public static ImmutableSet<Catchable> getValues() {
		return VALUES;
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public double getChance() {
		return chance;
	}

	public double getExperience() {
		return experience;
	}
}