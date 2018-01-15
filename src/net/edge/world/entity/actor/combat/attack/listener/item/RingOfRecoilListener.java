package net.edge.world.entity.actor.combat.attack.listener.item;

import net.edge.net.packet.out.SendMessage;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.combat.CombatType;
import net.edge.world.entity.actor.combat.attack.listener.ItemCombatListenerSignature;
import net.edge.world.entity.actor.combat.attack.listener.SimplifiedListener;
import net.edge.world.entity.actor.combat.hit.Hit;
import net.edge.world.entity.actor.combat.hit.HitIcon;
import net.edge.world.entity.actor.combat.hit.Hitsplat;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.item.container.impl.Equipment;

/**
 * @author Michael | Chex
 */
@ItemCombatListenerSignature(items = {2550})
public class RingOfRecoilListener extends SimplifiedListener<Player> {

	@Override
	public void block(Actor attacker, Player defender, Hit hit, CombatType combatType) {
		if(hit.getDamage() < 10) {
			return;
		}

		int recoil = hit.getDamage() / 10;
		int charges = defender.ringOfRecoil;
		charges -= recoil;

		if(charges <= 0) {
			defender.out(new SendMessage("Your ring of recoil has shattered!"));
			defender.getEquipment().unequip(Equipment.RING_SLOT, null, true, -1);
			this.remove(defender);
			// if charge is negative, recoil was too high for it's charge
			// so we add the -charges to get the amount of recoil left
			recoil += charges;
			charges = 400;
		}

		defender.ringOfRecoil = charges;
		attacker.damage(new Hit(recoil, Hitsplat.NORMAL_LOCAL));
		attacker.getCombat().getDamageCache().add(defender, recoil);
	}

	@Override
	public boolean remove(Player defender) {
		defender.getCombat().removeListener(this);
		return true;
	}

}
