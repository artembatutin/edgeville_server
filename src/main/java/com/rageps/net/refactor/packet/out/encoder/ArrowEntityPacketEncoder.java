package com.rageps.net.refactor.packet.out.encoder;

import com.rageps.net.refactor.codec.game.GamePacket;
import com.rageps.net.refactor.codec.game.GamePacketBuilder;
import com.rageps.net.refactor.packet.out.PacketEncoder;
import com.rageps.net.refactor.packet.out.model.ArrowEntityPacket;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class ArrowEntityPacketEncoder implements PacketEncoder<ArrowEntityPacket> {

    @Override
    public GamePacket encode(ArrowEntityPacket message) {
        GamePacketBuilder builder = new GamePacketBuilder(248);
        builder.put(message.getEntity().isMob() ? 1 : 10);
        builder.putShort(message.getEntity().getSlot());
        builder.put(0);
        return builder.toGamePacket();
    }
}