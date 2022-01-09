package com.rageps.net.refactor.packet.in.model.object_actions;

import com.rageps.net.refactor.packet.Packet;

public class ThirdActionObjectPacket extends Packet {

    private final int objectId;

    private final int objectX;

    private final int objectY;

    public ThirdActionObjectPacket(int objectId, int objectX, int objectY) {
        this.objectId = objectId;
        this.objectX = objectX;
        this.objectY = objectY;
    }

    public int getObjectY() {
        return objectY;
    }

    public int getObjectX() {
        return objectX;
    }

    public int getObjectId() {
        return objectId;
    }
}