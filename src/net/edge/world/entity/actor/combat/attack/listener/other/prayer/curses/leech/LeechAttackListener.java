package net.edge.world.entity.actor.combat.attack.listener.other.prayer.curses.leech;

import net.edge.content.skill.prayer.Prayer;
import net.edge.world.Animation;
import net.edge.world.Graphic;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.combat.attack.listener.SimplifiedListener;
import net.edge.world.entity.actor.combat.hit.Hit;
import net.edge.world.entity.actor.player.Player;

import static net.edge.world.Animation.AnimationPriority.HIGH;

public class LeechAttackListener extends SimplifiedListener<Player> {

    @Override
    public void start(Player attacker, Actor defender, Hit[] hits) {
        if (attacker.curseManager.isActivated(Prayer.LEECH_ATTACK)) {
            defender.graphic(new Graphic(2232, 0, 2));
            attacker.animation(new Animation(12575, HIGH));
            attacker.message("You leech the targets attack.");
            attacker.curseManager.deactivate(Prayer.LEECH_ATTACK);
        }
    }

    @Override
    public int modifyAttackLevel(Player attacker, Actor defender, int level) {
        return attacker.curseManager.modifyOutgoingLevel(level, 5, 10, Prayer.LEECH_ATTACK);
    }

}
