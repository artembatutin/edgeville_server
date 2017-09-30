package net.edge.world.entity.actor.combat.strategy.basic;

import net.edge.content.skill.Skills;
import net.edge.world.World;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.combat.attack.FightType;
import net.edge.world.entity.actor.combat.hit.Hit;
import net.edge.world.entity.actor.combat.strategy.CombatStrategy;
import net.edge.world.entity.actor.move.MovementQueue;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.locale.Boundary;

/**
 * @author Michael | Chex
 */
public abstract class MeleeStrategy<T extends Actor> extends CombatStrategy<T> {
	
	private static final int BASE_EXPERIENCE_MULTIPLIER = 4;
	
	@Override
	public boolean withinDistance(T attacker, Actor defender) {
		MovementQueue movement = attacker.getMovementQueue();
		MovementQueue otherMovement = defender.getMovementQueue();
		FightType fightType = attacker.getCombat().getFightType();
		int distance = getAttackDistance(attacker, fightType);
		if (movement.isRunning()) {
			distance += 1;
		}
		if(!new Boundary(attacker.getPosition(), attacker.size()).within(defender.getPosition(), defender.size(), distance)) {
			return false;
		}
		
		return World.getSimplePathChecker().checkLine(attacker.getPosition(), defender.getPosition(), attacker.size());
	}
	
	@Override
	public boolean canAttack(T attacker, Actor defender) {
		return true;
	}
	
	public static void addCombatExperience(Player player, Hit... hits) {
		int exp = 0;
		for(Hit hit : hits) {
			exp += hit.getDamage();
		}
		
		exp = Math.round(exp / 10F);
		exp *= BASE_EXPERIENCE_MULTIPLIER;
		Skills.experience(player, exp / 3, Skills.HITPOINTS);
		switch(player.getCombat().getFightType().getStyle()) {
			case ACCURATE:
				Skills.experience(player, exp, Skills.ATTACK);
				break;
			case AGGRESSIVE:
				Skills.experience(player, exp, Skills.STRENGTH);
				break;
			case DEFENSIVE:
				Skills.experience(player, exp, Skills.DEFENCE);
				break;
			case CONTROLLED:
				exp /= 3;
				Skills.experience(player, exp, Skills.ATTACK);
				Skills.experience(player, exp, Skills.STRENGTH);
				Skills.experience(player, exp, Skills.DEFENCE);
				break;
		}
	}
	
}
