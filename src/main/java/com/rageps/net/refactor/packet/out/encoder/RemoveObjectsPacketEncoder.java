package com.rageps.net.refactor.packet.out.encoder;

import com.rageps.net.refactor.codec.game.GamePacket;
import com.rageps.net.refactor.codec.game.GamePacketBuilder;
import com.rageps.net.refactor.packet.out.PacketEncoder;
import com.rageps.net.refactor.packet.out.model.RemoveObjectsPacket;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class RemoveObjectsPacketEncoder implements PacketEncoder<RemoveObjectsPacket> {

    @Override
    public GamePacket encode(RemoveObjectsPacket message) {
        GamePacketBuilder builder = new GamePacketBuilder(131);
        return builder.toGamePacket();
    }
}