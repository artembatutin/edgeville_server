package com.rageps.net.refactor.packet.in.handler;

import com.rageps.net.refactor.packet.in.model.FollowPlayerPacketPacket;
import com.rageps.net.refactor.packet.PacketHandler;
import com.rageps.world.entity.actor.player.Player;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class FollowPlayerPacketPacketHandler implements PacketHandler<FollowPlayerPacketPacket> {

    @Override
    public void handle(Player player, FollowPlayerPacketPacket packet) {
    }
}
