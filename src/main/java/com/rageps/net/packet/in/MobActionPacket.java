package com.rageps.net.packet.in;

import com.rageps.Arrav;
import com.rageps.action.ActionContainer;
import com.rageps.action.impl.MobAction;
import com.rageps.content.item.pets.Pet;
import com.rageps.content.minigame.MinigameHandler;
import com.rageps.content.skill.slayer.Slayer;
import com.rageps.content.skill.summoning.Summoning;
import com.rageps.net.codec.ByteOrder;
import com.rageps.net.codec.ByteTransform;
import com.rageps.net.codec.game.GamePacket;
import com.rageps.net.packet.IncomingPacket;
import com.rageps.world.World;
import com.rageps.world.entity.actor.combat.magic.CombatSpell;
import com.rageps.world.entity.actor.mob.Mob;
import com.rageps.world.entity.actor.mob.MobDefinition;
import com.rageps.world.entity.actor.player.Player;
import com.rageps.world.entity.actor.player.assets.Rights;
import com.rageps.world.entity.actor.player.assets.activity.ActivityManager;
import com.rageps.world.locale.Boundary;
import com.rageps.world.locale.Position;

/**
 * The message sent from the client when a player attacks or clicks on an NPC.
 * @author Artem Batutin <artembatutin@gmail.com
 */
public final class MobActionPacket implements IncomingPacket {
	
	/*
	 * All of the npc events.
	 */
	public static final ActionContainer<MobAction> FIRST = new ActionContainer<>();
	public static final ActionContainer<MobAction> SECOND = new ActionContainer<>();
	public static final ActionContainer<MobAction> THIRD = new ActionContainer<>();
	public static final ActionContainer<MobAction> FOURTH = new ActionContainer<>();
	
	@Override
	public void handle(Player player, int opcode, int size, GamePacket buf) {
		if(player.getActivityManager().contains(ActivityManager.ActivityType.NPC_ACTION))
			return;
		switch(opcode) {
			case 72:
				attackOther(player, buf);
				break;
			case 131:
				attackMagic(player, buf);
				break;
			case 155:
				firstClick(player, buf);
				break;
			case 17:
				secondClick(player, buf);
				break;
			case 21:
				thirdClick(player, buf);
				break;
			case 18:
				fourthClick(player, buf);
				break;
		}
		player.getActivityManager().execute(ActivityManager.ActivityType.NPC_ACTION);
	}
	
	/**
	 * Handles the melee and ranged attacks on an NPC.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void attackOther(Player player, GamePacket buf) {
		int index = buf.getShort(false, ByteTransform.A);
		Mob mob = World.get().getMobs().get(index - 1);
		if(mob == null || !checkAttack(player, mob))
			return;
		player.getTolerance().reset();
		player.getCombat().attack(mob);
	}
	
	/**
	 * Handles the magic attacks on an NPC.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void attackMagic(Player player, GamePacket buf) {
		int index = buf.getShort(true, ByteTransform.A, ByteOrder.LITTLE);
		int spellId = buf.getShort(true, ByteTransform.A);
		Mob mob = World.get().getMobs().get(index - 1);
		CombatSpell spell = CombatSpell.get(spellId);
		if(mob == null || spell == null || !checkAttack(player, mob)) {
			return;
		}
		player.getTolerance().reset();
		player.setSingleCast(spell);
		player.getCombat().attack(mob);
	}
	
	/**
	 * Handles the first click NPC slot.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void firstClick(Player player, GamePacket buf) {
		int index = buf.getShort(true, ByteOrder.LITTLE);
		Mob mob = World.get().getMobs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		if(mob.getId() == 4650) {
			player.getMovementQueue().smartWalk(new Position(3079, 3508));
		}
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), mob.getId() == 4650 ? 3 : 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(!MinigameHandler.execute(player, m -> m.onFirstClickNpc(player, mob))) {
					return;
				}
				if(Summoning.interact(player, mob, 1)) {
					return;
				}
				if(Pet.pickup(player, mob)) {
					return;
				}
				MobAction e = FIRST.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 1);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Arrav.DEBUG)
			player.message("[NPC1]:" + mob.toString());
	}
	
	/**
	 * Handles the second click NPC slot.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void secondClick(Player player, GamePacket buf) {
		int index = buf.getShort(false, ByteTransform.A, ByteOrder.LITTLE);
		Mob mob = World.get().getMobs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(!MinigameHandler.execute(player, m -> m.onSecondClickNpc(player, mob))) {
					return;
				}
				if(Summoning.interact(player, mob, 2)) {
					return;
				}
				MobAction e = SECOND.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 2);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Arrav.DEBUG)
			player.message("[NPC2]:" + mob.toString());
	}
	
	/**
	 * Handles the third click NPC slot.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void thirdClick(Player player, GamePacket buf) {
		int index = buf.getShort(true);
		Mob mob = World.get().getMobs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(Summoning.interact(player, mob, 3)) {
					return;
				}
				MobAction e = THIRD.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 3);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Arrav.DEBUG)
			player.message("[NPC3]:" + mob.toString());
	}
	
	/**
	 * Handles the fourth click NPC slot.
	 * @param player the player this will be handled for.
	 * @param buf the payload buffer that will read the sent data.
	 */
	private void fourthClick(Player player, GamePacket buf) {
		int index = buf.getShort(true, ByteOrder.LITTLE);
		Mob mob = World.get().getMobs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				
				if(Summoning.interact(player, mob, 4)) {
					return;
				}
				MobAction e = FOURTH.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 4);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Arrav.DEBUG)
			player.message("[NPC4]:" + mob.toString());
	}
	
	/**
	 * Determines if {@code player} can make an attack on {@code mob}.
	 * @param player the player attempting to make an attack.
	 * @param mob the mob being attacked.
	 * @return {@code true} if the player can make an attack, {@code false}
	 * otherwise.
	 */
	private boolean checkAttack(Player player, Mob mob) {
		if(!MobDefinition.DEFINITIONS[mob.getId()].isAttackable())
			return false;
		if(!player.inMulti() && player.getCombat().isUnderAttack() && !player.getCombat().isUnderAttackBy(mob)) {
			player.message("You are already under attack!");
			player.getMovementQueue().reset();
			return false;
		}
		return Slayer.canAttack(player, mob);
	}
}