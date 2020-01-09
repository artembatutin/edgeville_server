package net.arrav.net.packet.out;

import io.netty.buffer.ByteBuf;
import net.arrav.net.codec.game.GamePacket;
import net.arrav.net.codec.game.GamePacketType;
import net.arrav.net.packet.OutgoingPacket;
import net.arrav.world.entity.actor.player.Player;

public final class SendTask implements OutgoingPacket {
	
	private final String task;
	
	public SendTask(String task) {
		this.task = task;
	}
	
	@Override
	public GamePacket write(Player player, ByteBuf buf) {
		GamePacket out = new GamePacket(this, buf);
		out.message(90, GamePacketType.VARIABLE_SHORT);
		out.putCString(task);
		out.endVarSize();
		return out;
	}
}
