package net.edge.world.node.entity.npc.strategy.impl;

import net.edge.task.Task;
import net.edge.util.rand.RandomUtils;
import net.edge.World;
import net.edge.content.combat.CombatSessionData;
import net.edge.content.combat.CombatType;
import net.edge.content.combat.magic.CombatNormalSpell;
import net.edge.world.node.NodeState;
import net.edge.world.node.entity.EntityNode;
import net.edge.world.Animation;
import net.edge.world.Graphic;
import net.edge.world.Projectile;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.npc.strategy.DynamicCombatStrategy;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * The dynamic combat strategy for the tormented demon boss.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class TormentedDemonCombatStrategy extends DynamicCombatStrategy<Npc> {
	
	/**
	 * The task which is dependant for switching the prayers for this demon.
	 */
	private Optional<Task> switchPrayerTask = Optional.empty();
	
	/**
	 * The task which is dependant for switching attack styles for this demon.
	 */
	private Optional<Task> switchAttackTask = Optional.empty();
	
	/**
	 * The task which is dependant for restoring the tormented demon's shield.
	 */
	private Optional<Task> shieldRestorationTask = Optional.empty();
	
	/**
	 * The current combat style this demon is using.
	 */
	private CombatType combatType;
	
	/**
	 * Represents the id for the melee demon.
	 */
	public static final int MELEE_DEMON = 8349;
	
	/**
	 * Represents the id for the magic demon.
	 */
	public static final int MAGIC_DEMON = 8350;
	
	/**
	 * Represents the id for the ranged demon.
	 */
	public static final int RANGED_DEMON = 8351;
	
	/**
	 * Represents an array of all the demons.
	 */
	public static final int[] STATES = new int[]{MELEE_DEMON, MAGIC_DEMON, RANGED_DEMON};
	
	/**
	 * Constructs a new {@link TormentedDemonCombatStrategy}.
	 * @param npc the npc to register this combat strategy for.
	 */
	public TormentedDemonCombatStrategy(Npc npc) {
		super(npc);
	}
	
	/**
	 * The combat session data which returns a combat session data dependant on the specified
	 * {@code type}.
	 * @param character the character this combat session data is for.
	 * @param victim    the victim being hit.
	 * @param type      the type of combat being attacked with.
	 * @return the combat session data.
	 */
	private CombatSessionData type(EntityNode character, EntityNode victim, CombatType type) {
		switch(type) {
			case MELEE:
				return melee(character, victim);
			case RANGED:
				return ranged(character, victim);
			case MAGIC:
				return magic(character, victim);
			default:
				return magic(character, victim);
		}
	}
	
	/**
	 * Determines if this demon has the protect melee prayer on.
	 * @param npc the npc to determine this for.
	 * @return true if the npc has the protect melee prayer on, false otherwise.
	 */
	private boolean isMeleeDemon(Npc npc) {
		int id = npc.getTransform().isPresent() ? npc.getTransform().getAsInt() : npc.getId();
		return id == MELEE_DEMON;
	}
	
	/**
	 * The melee combat session data.
	 * @param character the character this combat session data is for.
	 * @param victim    the victim being hit.
	 * @return the combat session data for a melee attack.
	 */
	private CombatSessionData melee(EntityNode character, EntityNode victim) {
		character.animation(new Animation(10922));
		character.graphic(new Graphic(1886));
		return new CombatSessionData(character, victim, 1, CombatType.MELEE, true);
	}
	
	/**
	 * The ranged combat session data.
	 * @param character the character this combat session data is for.
	 * @param victim    the victim being hit.
	 * @return the combat session data for a ranged attack.
	 */
	private CombatSessionData ranged(EntityNode character, EntityNode victim) {
		character.animation(new Animation(10919));
		character.graphic(new Graphic(1888));
		World.get().submit(new Task(1, false) {
			@Override
			public void execute() {
				this.cancel();
				if(character.getState() != NodeState.ACTIVE || victim.getState() != NodeState.ACTIVE || character.isDead() || victim.isDead())
					return;
				new Projectile(character, victim, 1887, 30, 35, 34, 16, 16).sendProjectile();
			}
		});
		return new CombatSessionData(character, victim, 1, CombatType.RANGED, false);
	}
	
	/**
	 * The magic combat session data.
	 * @param character the character this combat session data is for.
	 * @param victim    the victim being hit.
	 * @return the combat session data for a magic attack.
	 */
	private CombatSessionData magic(EntityNode character, EntityNode victim) {
		character.setCurrentlyCasting(TORMENTED_DEMON_BLAST);
		TORMENTED_DEMON_BLAST.castAnimation().ifPresent(character::animation);
		World.get().submit(new Task(1, false) {
			@Override
			public void execute() {
				this.cancel();
				if(character.getState() != NodeState.ACTIVE || victim.getState() != NodeState.ACTIVE || character.isDead() || victim.isDead())
					return;
				TORMENTED_DEMON_BLAST.projectile(character, victim).get().sendProjectile();
			}
		});
		return new CombatSessionData(character, victim, 1, CombatType.MAGIC, false);
	}
	
	/**
	 * Switches the {@code npc}s state to a random id from the {@link #STATES} array.
	 */
	private void switchState() {
		npc.transform(RandomUtils.random(STATES));
	}
	
	/**
	 * Gets the protected prayer currently active for the specified {@code npcId}.
	 * @param npc the npc to return the protected prayer from.
	 * @return the combat type protected from.
	 */
	public static CombatType getProtectedFrom(Npc npc) {
		int id = npc.getTransform().isPresent() ? npc.getTransform().getAsInt() : npc.getId();
		switch(id) {
			case MELEE_DEMON:
				return CombatType.MELEE;
			case RANGED_DEMON:
				return CombatType.RANGED;
			case MAGIC_DEMON:
				return CombatType.MAGIC;
		}
		return CombatType.NONE;
	}
	
	@Override
	public boolean canOutgoingAttack(EntityNode victim) {
		return true;
	}
	
	@Override
	public CombatSessionData outgoingAttack(EntityNode victim) {
		CombatType[] types = new CombatType[]{CombatType.MELEE, CombatType.RANGED, CombatType.MAGIC};
		
		if(combatType == null) {
			combatType = RandomUtils.random(types);
		}
		
		CombatSessionData data = type(npc, victim, combatType);
		
		if(!switchAttackTask.isPresent()) {
			switchAttackTask = Optional.of(new SwitchAttackTask(this));
			World.get().submit(switchAttackTask.get());
		}
		
		if(!switchPrayerTask.isPresent()) {
			switchPrayerTask = Optional.of(new SwitchPrayerTask(this));
			World.get().submit(switchPrayerTask.get());
		}
		return data;
	}
	
	@Override
	public void incomingAttack(EntityNode attacker, CombatSessionData data) {
		CombatType type = getProtectedFrom(npc);
		
		if(!shieldRestorationTask.isPresent()) {
			npc.graphic(new Graphic(1885));
		}
		
		if(data.getType().equals(type)) {
			Arrays.stream(data.getHits()).filter(Objects::nonNull).forEach(h -> h.setAccurate(false));
			return;
		}
		
		if(attacker.isPlayer() && !shieldRestorationTask.isPresent()) {
			Player player = attacker.toPlayer();
			
			Arrays.stream(data.getHits()).forEach(hit -> {
				int modifier = (int) (hit.getDamage() * 0.25);// 75% is absorbed
				hit.setDamage(modifier);
			});
			
			if(player.getEquipment().containsAny(2402, 6746) && Arrays.stream(data.getHits()).anyMatch(hit -> hit.getDamage() > 0) && !isMeleeDemon(npc)) {
				player.message("The demon is temporarily weakened by your weapon.");
				shieldRestorationTask = Optional.of(new ShieldRestorationTask(this));
				World.get().submit(shieldRestorationTask.get());
			}
		}
	}
	
	@Override
	public int attackDelay() {
		return npc.getAttackSpeed();
	}
	
	@Override
	public int attackDistance() {
		return 1;
	}
	
	/**
	 * The task which is responsible for switching restoring the
	 * tormented demon's shield.
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	private static final class ShieldRestorationTask extends Task {
		
		/**
		 * The strategy this task is dependant of.
		 */
		private final TormentedDemonCombatStrategy strategy;
		
		/**
		 * Constructs a new {@link SwitchAttackTask}.
		 * @param strategy {@link #strategy}.
		 */
		public ShieldRestorationTask(TormentedDemonCombatStrategy strategy) {
			super(100);
			this.strategy = strategy;
		}
		
		@Override
		protected void execute() {
			this.cancel();
			if(strategy.npc.getState() != NodeState.ACTIVE || strategy.npc.isDead() || strategy.npc.getCombatBuilder().getVictim() == null) {
				return;
			}
			
			EntityNode entity = strategy.npc.getCombatBuilder().getVictim();
			
			entity.ifPlayer(pl -> pl.message("The demon restored it's shield."));
		}
		
		@Override
		protected void onCancel() {
			strategy.shieldRestorationTask = Optional.empty();
		}
	}
	
	/**
	 * The task which is responsible for switching attack styles for
	 * the tormented demon.
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	private static final class SwitchAttackTask extends Task {
		
		/**
		 * The strategy this task is dependant of.
		 */
		private final TormentedDemonCombatStrategy strategy;
		
		/**
		 * Constructs a new {@link SwitchAttackTask}.
		 * @param strategy {@link #strategy}.
		 */
		public SwitchAttackTask(TormentedDemonCombatStrategy strategy) {
			super(30);
			this.strategy = strategy;
		}
		
		@Override
		protected void execute() {
			this.cancel();
			if(strategy.npc.getState() != NodeState.ACTIVE || strategy.npc.isDead() || strategy.npc.getCombatBuilder().getVictim() == null) {
				return;
			}
			CombatType[] types = new CombatType[]{CombatType.MELEE, CombatType.RANGED, CombatType.MAGIC};
			strategy.npc.getCombatBuilder().resetAttackTimer();
			strategy.npc.animation(new Animation(10917, Animation.AnimationPriority.HIGH));
			strategy.combatType = RandomUtils.random(types);
		}
		
		@Override
		protected void onCancel() {
			strategy.switchAttackTask = Optional.empty();
		}
	}
	
	/**
	 * The task which is responsible for switching protection prayers
	 * for the tormented demon.
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	private static final class SwitchPrayerTask extends Task {
		
		/**
		 * The strategy this task is dependant of.
		 */
		private final TormentedDemonCombatStrategy strategy;
		
		/**
		 * Constructs a new {@link SwitchAttackTask}.
		 * @param strategy {@link #strategy}.
		 */
		public SwitchPrayerTask(TormentedDemonCombatStrategy strategy) {
			super(20);
			this.strategy = strategy;
		}
		
		@Override
		protected void execute() {
			this.cancel();
			if(strategy.npc.getState() != NodeState.ACTIVE || strategy.npc.isDead() || strategy.npc.getCombatBuilder().getVictim() == null) {
				return;
			}
			strategy.switchState();
		}
		
		@Override
		protected void onCancel() {
			strategy.switchPrayerTask = Optional.empty();
		}
	}
	
	/**
	 * The spell casted when a magic attack is executed.
	 */
	private static final CombatNormalSpell TORMENTED_DEMON_BLAST = new CombatNormalSpell() {
		
		@Override
		public int spellId() {
			return -1;
		}
		
		@Override
		public int maximumHit() {
			return 28;
		}
		
		@Override
		public Optional<Animation> castAnimation() {
			return Optional.of(new Animation(10918));
		}
		
		@Override
		public Optional<Graphic> startGraphic() {
			return Optional.of(new Graphic(1883, 96 << 16));
		}
		
		@Override
		public Optional<Projectile> projectile(EntityNode cast, EntityNode castOn) {
			return Optional.of(new Projectile(cast, castOn, 1884, 30, 35, 34, 16, 16));
		}
		
		@Override
		public Optional<Graphic> endGraphic() {
			return Optional.empty();
		}
		
		@Override
		public int levelRequired() {
			return -1;
		}
		
		@Override
		public double baseExperience() {
			return -1;
		}
		
		@Override
		public Optional<Item[]> itemsRequired(Player player) {
			return Optional.empty();
		}
		
		@Override
		public Optional<Item[]> equipmentRequired(Player player) {
			return Optional.empty();
		}
	};
}