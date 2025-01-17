package net.arrav.world.entity.actor.combat.attack.listener.npc;

import net.arrav.world.entity.actor.Actor;
import net.arrav.world.entity.actor.combat.attack.listener.NpcCombatListenerSignature;
import net.arrav.world.entity.actor.combat.attack.listener.SimplifiedListener;
import net.arrav.world.entity.actor.combat.hit.Hit;
import net.arrav.world.entity.actor.combat.strategy.CombatStrategy;
import net.arrav.world.entity.actor.combat.strategy.npc.NpcMeleeStrategy;
import net.arrav.world.entity.actor.combat.strategy.npc.impl.DragonfireStrategy;
import net.arrav.world.entity.actor.mob.Mob;

import static net.arrav.world.entity.actor.combat.CombatUtil.createStrategyArray;
import static net.arrav.world.entity.actor.combat.CombatUtil.randomStrategy;
import static net.arrav.world.entity.actor.combat.projectile.CombatProjectile.getDefinition;

/**
 * @author Michael | Chex
 */
@NpcCombatListenerSignature(npcs = {
		/* Green */ 941, 4677, 4678, 4679, 4680, 10604, 10605, 10606, 10607, 10608, 10609,
		/* Red */ 53, 4669, 4670, 4671, 4672, 10815, 10816, 10817, 10818, 10819, 10820,
		/* Blue */ 55, 4681, 4682, 4683, 4684, 5178,
		/* Black */ 54, 4673, 4674, 4675, 4676, 10219, 10220, 10221, 10222, 10223, 10224})
public class ChromaticDragon extends SimplifiedListener<Mob> {
	private static DragonfireStrategy DRAGONFIRE;
	private static CombatStrategy<Mob>[] STRATEGIES;
	
	static {
		try {
			DRAGONFIRE = new DragonfireStrategy(getDefinition("Chromatic dragonfire"));
			STRATEGIES = createStrategyArray(NpcMeleeStrategy.get(), DRAGONFIRE);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean canAttack(Mob attacker, Actor defender) {
		if(!NpcMeleeStrategy.get().withinDistance(attacker, defender)) {
			attacker.setStrategy(DRAGONFIRE);
		}
		return attacker.getStrategy().canAttack(attacker, defender);
	}

	@Override
	public void start(Mob attacker, Actor defender, Hit[] hits) {
		if(!NpcMeleeStrategy.get().withinDistance(attacker, defender)) {
			attacker.setStrategy(DRAGONFIRE);
		} else {
			attacker.setStrategy(randomStrategy(STRATEGIES));
		}
	}
}
