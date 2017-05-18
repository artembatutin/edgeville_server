package net.edge.world.content.skill.thieving;

import net.edge.task.Task;
import net.edge.utils.rand.RandomUtils;
import net.edge.world.content.skill.SkillData;
import net.edge.world.content.skill.Skills;
import net.edge.world.content.skill.action.SkillAction;
import net.edge.world.locale.Position;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.Optional;

/**
 * Represents functionality for a thieving {@link SkillAction}.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public abstract class Thieving extends SkillAction {
	
	/**
	 * Constructs a new {@link Thieving} {@link SkillAction}.
	 * @param player   the player performing this skill action.
	 * @param position the position this player should face.
	 */
	public Thieving(Player player, Position position) {
		super(player, Optional.of(position));
	}
	
	/**
	 * The requirement required.
	 * @return the identifier which identifies the requirement.
	 */
	public abstract int requirement();
	
	/**
	 * The loot the player receives upon stealing from this stall.
	 * @return the array of items.
	 */
	public abstract Item[] loot();
	
	/**
	 * The method executed upon stealing from this stall.
	 */
	public abstract void onExecute(Task t);
	
	/**
	 * The method which gets executed as soon as the skill action is stopped.
	 * @param success determines whether the theft was a success or not.
	 */
	public void onStop(boolean success) {
		
	}
	
	/**
	 * The method which identifies if this player can initialise the theft.
	 * @return <true> if the player can, <false> otherwise.
	 */
	public boolean canInit() {
		return true;
	}
	
	/**
	 * Indicates if this player failed to thief.
	 * @return <true> if the player did, <false> otherwise.
	 */
	public boolean failure() {
		return false;
	}
	
	@Override
	public boolean init() {
		return canInit();
	}
	
	@Override
	public void execute(Task t) {
		onExecute(t);
	}
	
	@Override
	public void onStop() {
		if(!failure() && canExecute()) {
			onStop(true);
			getPlayer().getInventory().add(RandomUtils.random(loot()));
			Skills.experience(getPlayer(), experience(), skill().getId());
		}
		onStop(false);
	}
	
	@Override
	public boolean isPrioritized() {
		return false;
	}
	
	@Override
	public SkillData skill() {
		return SkillData.THIEVING;
	}
	
}
