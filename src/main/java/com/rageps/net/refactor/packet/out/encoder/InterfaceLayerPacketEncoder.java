package com.rageps.net.refactor.packet.out.encoder;

import com.rageps.net.refactor.codec.game.GamePacket;
import com.rageps.net.refactor.codec.game.GamePacketBuilder;
import com.rageps.net.refactor.packet.out.PacketEncoder;
import com.rageps.net.refactor.packet.out.model.InterfaceLayerPacket;

/**
 * @author Tamatea <tamateea@gmail.com>
 */
public class InterfaceLayerPacketEncoder implements PacketEncoder<InterfaceLayerPacket> {

    @Override
    public GamePacket encode(InterfaceLayerPacket message) {
        GamePacketBuilder builder = new GamePacketBuilder(171);
        builder.put(message.isHide() ? 1 : 0);
        builder.putShort(message.getId());
        return builder.toGamePacket();
    }
}
