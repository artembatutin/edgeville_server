package net.arrav.net.packet.in;

import io.netty.buffer.ByteBuf;
import net.arrav.content.skill.construction.furniture.ConstructFurniture;
import net.arrav.content.skill.construction.furniture.Furniture;
import net.arrav.content.skill.construction.room.RoomData;
import net.arrav.content.skill.construction.room.RoomManipulation;
import net.arrav.net.packet.IncomingPacket;
import net.arrav.world.entity.actor.player.Player;

import static net.arrav.content.skill.construction.room.RoomData.*;

/**
 * This message sent from the client when the player clicks a construction panel button.
 * @author Artem Batutin<artembatutin@gmail.com>
 */
public final class ConstructionPacket implements IncomingPacket {
	
	private static final RoomData[] DATA = {GARDEN, PARLOUR, KITCHEN, DINING_ROOM, WORKSHOP, BEDROOM, SKILL_ROOM, QUEST_HALL_DOWN, SKILL_HALL_DOWN, GAMES_ROOM, COMBAT_ROOM, QUEST_ROOM, MENAGERY, STUDY, CUSTOME_ROOM, CHAPEL, PORTAL_ROOM, FORMAL_GARDEN, THRONE_ROOM, OUBLIETTE, PIT, DUNGEON_STAIR_ROOM, CORRIDOR, JUNCTION, TREASURE_ROOM,};
	
	@Override
	public void handle(Player player, int opcode, int size, ByteBuf buf) {
		int click = buf.get();
		if(click < 0 || click >= DATA.length) {
			click -= 40;
			Furniture[] plans = player.getHouse().get().getPlan().getPanel();
			if(plans == null)
				return;
			player.getHouse().get().getPlan().setSelected(plans[click]);
			ConstructFurniture cons = new ConstructFurniture(player, player.getHouse().get().getPlan());
			cons.start();
		} else {
			RoomData data = ConstructionPacket.DATA[click];
			RoomManipulation.createRoom(data, player, player.getPosition().getZ());
		}
	}
	
}
