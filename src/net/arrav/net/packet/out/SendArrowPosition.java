package net.arrav.net.packet.out;

import io.netty.buffer.ByteBuf;
import net.arrav.net.packet.OutgoingPacket;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.locale.Position;

public final class SendArrowPosition implements OutgoingPacket {
	
	private final Position position;
	private final int direction;
	
	/**
	 * The message that sends a hint arrow on a position.
	 * @param position the position to send the arrow on.
	 * @param direction the direction on the position to send the arrow on. The
	 * possible directions to put the arrow on are as follows:
	 * <p>
	 * <p>
	 * Middle: 2
	 * <p>
	 * West: 3
	 * <p>
	 * East: 4
	 * <p>
	 * South: 5
	 * <p>
	 * North: 6
	 * <p>
	 * <p>
	 */
	public SendArrowPosition(Position position, int direction) {
		this.position = position;
		this.direction = direction;
	}
	
	@Override
	public ByteBuf write(Player player, ByteBuf buf) {
		buf.message(254);
		buf.put(direction);
		buf.putShort(position.getX());
		buf.putShort(position.getY());
		buf.put(position.getZ());
		return buf;
	}
}
