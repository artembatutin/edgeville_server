package net.arrav.world.entity.actor.combat.attack.listener.other.prayer.regular.defence;

import net.arrav.world.entity.actor.Actor;
import net.arrav.world.entity.actor.combat.attack.listener.SimplifiedListener;
import net.arrav.world.entity.actor.player.Player;

public class RockSkinListener extends SimplifiedListener<Player> {

	@Override
	public int modifyDefenceLevel(Actor attacker, Player defender, int damage) {
		return damage * 11 / 10;
	}

}
