package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public class UDPServerChunkVoicePacket extends UDPPacket {

    byte[] data;
    boolean direct;
    byte chunkSize;
    int entityId;
    int fade;
    int distance;

    public UDPServerChunkVoicePacket(byte[] samples, int entityID, boolean direct, byte chunkSize, int fade, int distance) {
        this.data = samples;
        this.entityId = entityID;
        this.direct = direct;
        this.chunkSize = chunkSize;
        this.fade = fade;
        this.distance = distance;
    }

    @Override
    public byte id() {
        return (byte) 5;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        out.writeInt(this.entityId);
        out.writeByte(this.chunkSize);
        out.writeBoolean(this.direct);
        UDPByteUtilities.writeBytes(this.data, out);
        out.writeInt(fade);
        out.writeInt(distance);
    }
}
