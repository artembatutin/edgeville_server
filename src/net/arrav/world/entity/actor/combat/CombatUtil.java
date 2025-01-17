package net.arrav.world.entity.actor.combat;

import net.arrav.content.minigame.Minigame;
import net.arrav.content.minigame.MinigameHandler;
import net.arrav.content.skill.prayer.Prayer;
import net.arrav.net.packet.out.SendMessage;
import net.arrav.util.rand.RandomUtils;
import net.arrav.world.Animation;
import net.arrav.world.Projectile;
import net.arrav.world.entity.actor.Actor;
import net.arrav.world.entity.actor.combat.effect.CombatEffect;
import net.arrav.world.entity.actor.combat.effect.CombatEffectType;
import net.arrav.world.entity.actor.combat.hit.CombatHit;
import net.arrav.world.entity.actor.combat.hit.Hit;
import net.arrav.world.entity.actor.combat.hit.HitIcon;
import net.arrav.world.entity.actor.combat.hit.Hitsplat;
import net.arrav.world.entity.actor.combat.strategy.CombatStrategy;
import net.arrav.world.entity.actor.combat.weapon.WeaponInterface;
import net.arrav.world.entity.actor.mob.Mob;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.actor.player.assets.AntifireDetails;
import net.arrav.world.entity.item.Item;
import net.arrav.world.entity.item.container.impl.Equipment;
import net.arrav.world.locale.loc.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A collection of utility methods and constants related to combat.
 * @author lare96 <http://github.com/lare96>
 */
public final class CombatUtil {
	
	/**
	 * The default constructor.
	 * @throws UnsupportedOperationException if this class is instantiated.
	 */
	private CombatUtil() {
		throw new UnsupportedOperationException("This class cannot be instantiated!");
	}
	
	/**
	 * Gets the ranged distance based on {@code weapon}.
	 * @param weapon the weapon you have equipped.
	 * @return the ranged distance.
	 * @throws IllegalArgumentException if the weapon interface type is
	 * invalid.
	 */
	public static int getRangedDistance(WeaponInterface weapon) {
		switch(weapon) {
			case SALAMANDER:
				return 1;
			case CHINCHOMPA:
				return 8;
			case DART:
			case THROWNAXE:
				return 4;
			case KNIFE:
			case JAVELIN:
				return 5;
			case CROSSBOW:
			case LONGBOW:
			case COMPOSITE_BOW:
				return 8;
			case SHORTBOW:
				return 7;
			default:
				throw new IllegalArgumentException("Invalid weapon interface type:  " + weapon);
		}
	}
	
	/**
	 * Sends an action to {@link Actor} instance which is within a {@code distance}.
	 * @param attacker attacker.
	 * @param defender defender.
	 * @param action action consumer.
	 */
	public static void areaAction(Actor attacker, Actor defender, Consumer<Actor> action) {
		areaAction(attacker, defender, 1, 9, action);
	}
	
	/**
	 * Sends an action to {@link Actor} instance which is within a {@code distance}.
	 * @param attacker attacker.
	 * @param defender defender.
	 * @param distance distance parsed.
	 * @param max the max amount of actors action applied.
	 * @param action action consumer.
	 */
	public static void areaAction(Actor attacker, Actor defender, int distance, int max, Consumer<Actor> action) {
		action.accept(defender);
		if(!attacker.inMulti() || !defender.inMulti())
			return;
		int added = 0;
		for(Player other : defender.getLocalPlayers()) {
			if(other == null)
				continue;
			if(other.getInstance() != defender.getInstance())
				continue;
			if(!other.getPosition().withinDistance(defender.getPosition(), distance))
				continue;
			if(other.same(attacker) || other.same(defender))
				continue;
			if(other.getCurrentHealth() <= 0 || other.isDead())
				continue;
			if(!other.inMulti())
				continue;
			if(attacker.isPlayer() && attacker.inWilderness() && other.inWilderness())
				continue;
			action.accept(other);
			if(++added == max)
				return;
		}
		for(Mob other : defender.getLocalMobs()) {
			if(other == null)
				continue;
			if(other.getInstance() != defender.getInstance())
				continue;
			if(!other.getPosition().withinDistance(defender.getPosition(), distance))
				continue;
			if(other.same(attacker) || other.same(defender))
				continue;
			if(other.getCurrentHealth() <= 0 || other.isDead())
				continue;
			if(!other.getDefinition().isAttackable())
				continue;
			if(!other.inMulti())
				continue;
			action.accept(other);
			if(++added == max)
				return;
		}
	}
	
	public static <A extends Actor> List<A> actorsWithinDistance(Actor player, Set<A> actors, int radius) {
		List<A> collected = new LinkedList<>();
		for(A other : actors) {
			if(other == null)
				continue;
			if(!other.getPosition().withinDistance(player.getPosition(), radius))
				continue;
			if(other.same(player))
				continue;
			if(other.getCurrentHealth() <= 0 || other.isDead())
				continue;
			collected.add(other);
		}
		return collected;
	}
	
	/**
	 * Gets the corresponding combat prayer to {@code type}.
	 * @param type the combat type to get the prayer for.
	 * @return the corresponding combat prayer.
	 * @throws IllegalArgumentException if the combat type is invalid.
	 */
	public static Prayer[] getProtectingPrayer(CombatType type) {
		switch(type) {
			case MELEE:
				return new Prayer[]{Prayer.PROTECT_FROM_MELEE, Prayer.DEFLECT_MELEE};
			case MAGIC:
				return new Prayer[]{Prayer.PROTECT_FROM_MAGIC, Prayer.DEFLECT_MAGIC};
			case RANGED:
				return new Prayer[]{Prayer.PROTECT_FROM_MISSILES, Prayer.DEFLECT_MISSILES};
			default:
				throw new IllegalArgumentException("Invalid combat type: " + type);
		}
	}
	
	/**
	 * Gets the hit delay for the specified {@code type}.
	 * @param attacker the character doing the hit
	 * @param defender the victim being hit
	 * @param type the combat type of this hit
	 * @return the delay for the combat type
	 * @throws IllegalArgumentException if the combat type is invalid
	 */
	public static int getHitDelay(Actor attacker, Actor defender, CombatType type) {
		if(type.equals(CombatType.MELEE)) {
			return 1;
		}
		
		int distance = (int) attacker.getPosition().getDistance(defender.getPosition());
		
		if(type.equals(CombatType.MAGIC)) {
			return Projectile.MAGIC_DELAYS[distance > 10 ? 10 : distance];
		}
		
		if(type.equals(CombatType.RANGED)) {
			return Projectile.RANGED_DELAYS[distance > 10 ? 10 : (distance - 1)];
		}
		
		return 1;
	}
	
	/**
	 * Gets the hitsplat delay for the specified {@code type}.
	 * @param type the combat type of this hit
	 * @return the delay for the combat type.
	 * @throws IllegalArgumentException if the combat type is invalid.
	 */
	public static int getHitsplatDelay(CombatType type) {
		return type != CombatType.MELEE ? 1 : 0;
	}
	
	/**
	 * Applies the {@code effect} in any context.
	 * @param effect the effect that must be applied.
	 * @return {@code true} if it was successfully applied, {@code false}
	 * otherwise.
	 */
	public static boolean effect(Actor character, CombatEffectType effect) {
		return CombatEffect.EFFECTS.get(effect).start(character);
	}
	
	/**
	 * Calculates the combat level difference for wilderness player vs. player
	 * combat.
	 * @param combatLevel the combat level of the first person.
	 * @param otherCombatLevel the combat level of the other person.
	 * @return the combat level difference.
	 */
	public static int combatLevelDifference(int combatLevel, int otherCombatLevel) {
		if(combatLevel > otherCombatLevel) {
			return (combatLevel - otherCombatLevel);
		} else if(otherCombatLevel > combatLevel) {
			return (otherCombatLevel - combatLevel);
		} else {
			return 0;
		}
	}
	
	/**
	 * Calculates a pseudo-random hit for {@code character} based on {@code
	 * victim} and {@code type}.
	 * @param victim the victim of this hit that will be used as a factor.
	 * @param hit the hit being processed in the combat.
	 * @return the generated hit, will most likely return a different result if
	 * called on two different occasions even with the same arguments.
	 */
	public static Hit calculateSoaking(Actor victim, CombatType type, Hit hit) {
		if(victim.isPlayer()) {
			Player player = victim.toPlayer();
			int i = type == CombatType.MAGIC ? 2 : type == CombatType.RANGED ? 1 : 0;
			int reducedDamage = (int) ((hit.getDamage() - 200) * player.getEquipment().getBonuses()[CombatConstants.ABSORB_MELEE + i] / 100.D);
			if(hit.getDamage() - reducedDamage > 200 && victim.getCurrentHealth() > 200) {
				if(reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoak(reducedDamage);
				}
			}
		}
		return hit;
	}
	
	static Animation getBlockAnimation(Actor actor) {
		if(actor.isPlayer()) {
			int animation = 404;
			Player player = actor.toPlayer();
			if(player.getShieldAnimation() != null) {
				animation = player.getShieldAnimation().getBlock();
			} else if(player.getWeaponAnimation() != null) {
				animation = player.getWeaponAnimation().getBlocking();
			}
			return new Animation(animation, Animation.AnimationPriority.LOW);
		}
		
		return new Animation(actor.toMob().getDefinition().getDefenceAnimation(), Animation.AnimationPriority.LOW);
	}
	
	static boolean validateMobs(Actor attacker, Actor defender) {
		if(!validate(attacker) || !validate(defender)) {
			attacker.getCombat().reset(true, true);
			return false;
		}
		
		if(!canAttack(attacker, defender)) {
			attacker.getCombat().reset(true, true);
			return false;
		}
		return true;
	}
	
	public static boolean canAttack(Actor attacker, Actor defender) {
		boolean multi = attacker.inMulti() && defender.inMulti();
		boolean attacked = defender.getCombat().isUnderAttack();
		if(attacked && !multi && !attacker.getCombat().isAttacking(defender)) {
			if(attacker.isPlayer()) {
				Player player = attacker.toPlayer();
				player.message("This player is already in combat.");
			}
			attacker.getCombat().reset(true, true);
			return false;
		}
		if(attacker.isPlayer() && defender.isPlayer()) {
			Player player = attacker.toPlayer();
			Player victim = defender.toPlayer();
			Optional<Minigame> optional = MinigameHandler.getMinigame(player);
			if(!optional.isPresent()) {
				if(Location.inFunPvP(attacker) && Location.inFunPvP(victim)) {
					return true;
				}
				if(!attacker.inWilderness() || !victim.inWilderness()) {
					player.message("Both you and " + victim.getFormatUsername() + " need to be in the wilderness to fight!");
					player.getCombat().reset(true, true);
					return false;
				}
				int combatDifference = CombatUtil.combatLevelDifference(player.determineCombatLevel(), victim.determineCombatLevel());
				if(combatDifference > player.getWildernessLevel() || combatDifference > victim.getWildernessLevel()) {
					player.message("Your combat level difference is too great to attack that player here.");
					player.getCombat().reset(true, true);
					return false;
				}
			}
		}
		return true;
	}
	
	private static boolean validate(Actor actor) {
		return actor != null && !actor.isDead() && actor.isVisible() && !actor.isTeleporting() && !actor.isNeedsPlacement();
	}
	
	public static CombatHit generateDragonfire(Mob attacker, Actor defender) {
		return generateDragonfire(attacker, defender, 600, true);
	}
	
	public static CombatHit generateDragonfire(Mob attacker, Actor defender, int max, boolean prayer) {
		int hitDelay = getHitDelay(attacker, defender, CombatType.MAGIC);
		int hitsplatDelay = getHitsplatDelay(CombatType.MAGIC);
		return generateDragonfire(attacker, defender, max, hitDelay, hitsplatDelay, prayer);
	}
	
	public static CombatHit generateDragonfire(Mob attacker, Actor defender, int max, int hitDelay, int hitsplatDelay, boolean prayer) {
		int damage;
		
		if(defender.isPlayer()) {
			Player player = defender.toPlayer();
			prayer &= player.getPrayerActive().contains(Prayer.PROTECT_FROM_MAGIC);
			boolean shield = player.getEquipment().containsAny(1540, 11283);
			boolean potion = player.getAntifireDetails().isPresent();
			
			if(shield && potion) {
				max = 0;
			} else if(potion) {
				AntifireDetails.AntifireType type = player.getAntifireDetails().get().getType();
				max -= type.getReduction();
				if(max <= 0) {
					max = 0;
				}
			} else if(shield) {
				max -= 500;
			} else if(prayer) {
				max -= 450;
			}
			
			damage = max == 0 ? 0 : RandomUtils.inclusive(max);
			if(damage >= 150) {
				player.out(new SendMessage("You are horribly burned by the dragonfire!"));
			} else if(!shield && !potion && !prayer && damage < 90 && damage > 0) {
				player.out(new SendMessage("You manage to resist some of the dragonfire."));
			}
		} else {
			damage = max == 0 ? 0 : RandomUtils.inclusive(max);
		}
		
		Hit hit = new Hit(damage, Hitsplat.NORMAL, HitIcon.NONE, true, attacker.getSlot());
		return new CombatHit(hit, hitDelay, hitsplatDelay);
	}
	
	public static CombatStrategy<Mob> randomStrategy(CombatStrategy<Mob>[] strategies) {
		return RandomUtils.random(strategies);
	}
	
	@SafeVarargs
	public static CombatStrategy<Mob>[] createStrategyArray(CombatStrategy<Mob>... strategies) {
		return strategies;
	}
	
	/**
	 * Determines if {@code character} is wearing full void.
	 * @param character the character to determine this for.
	 * @return {@code true} if this character is wearing full void,
	 * {@code false} otherwise.
	 */
	public static boolean isFullVoid(Actor character) {
		if(character.isMob()) {
			return character.toMob().getDefinition().getName().contains("Void Knight");
		}
		Item top = ((Player) character).getEquipment().get(Equipment.CHEST_SLOT);
		return top != null && !(top.getId() != 8839 && top.getId() != 10611) && character.toPlayer().getEquipment().containsAll(8840, 8842);
	}
	
}
