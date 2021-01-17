package com.rageps.net.refactor.packet.out.encoder;

import com.rageps.net.refactor.codec.game.GamePacket;
import com.rageps.net.refactor.codec.game.GamePacketBuilder;
import com.rageps.net.refactor.packet.out.PacketEncoder;
import com.rageps.net.refactor.packet.out.model.PrivateMessageStatusPacket;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class PrivateMessageStatusPacketEncoder implements PacketEncoder<PrivateMessageStatusPacket> {

    @Override
    public GamePacket encode(PrivateMessageStatusPacket message) {
        GamePacketBuilder builder = new GamePacketBuilder(221);
        builder.put(message.getCode());
        return builder.toGamePacket();
    }
}