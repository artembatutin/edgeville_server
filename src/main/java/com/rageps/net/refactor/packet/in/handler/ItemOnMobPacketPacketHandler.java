package com.rageps.net.refactor.packet.in.handler;

import com.rageps.net.refactor.packet.in.model.ItemOnMobPacketPacket;
import com.rageps.net.refactor.packet.PacketHandler;
import com.rageps.world.entity.actor.player.Player;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class ItemOnMobPacketPacketHandler implements PacketHandler<ItemOnMobPacketPacket> {

    @Override
    public void handle(Player player, ItemOnMobPacketPacket packet) {
    }
}
