package net.edge.net.packet.out;

import net.edge.content.TabInterface;
import net.edge.net.codec.ByteTransform;
import net.edge.net.codec.GameBuffer;
import net.edge.net.packet.OutgoingPacket;
import net.edge.world.node.entity.player.Player;

public final class SendTab implements OutgoingPacket {
	
	private final int id;
	private final TabInterface tab;
	
	public SendTab(int id, TabInterface tab) {
		this.id = id;
		this.tab = tab;
	}
	
	@Override
	public void write(Player player) {
		GameBuffer msg = player.getSession().getStream();
		msg.message(71);
		msg.putShort(id);
		msg.put(tab.getOld(), ByteTransform.A);
		msg.put(tab.getNew(), ByteTransform.A);
	}
}