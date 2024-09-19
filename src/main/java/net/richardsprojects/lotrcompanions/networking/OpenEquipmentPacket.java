package net.richardsprojects.lotrcompanions.networking;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.richardsprojects.lotrcompanions.core.PacketHandler;

import java.util.function.Supplier;

public class OpenEquipmentPacket {
    private final int id;
    private final int size;
    private final int entityId;

    public OpenEquipmentPacket(int id, int size, int entityId) {
        this.id = id;
        this.size = size;
        this.entityId = entityId;
    }

    public static OpenEquipmentPacket decode(PacketBuffer buf) {
        return new OpenEquipmentPacket(buf.readUnsignedByte(), buf.readVarInt(), buf.readInt());
    }

    public static void encode(OpenEquipmentPacket msg, PacketBuffer buf) {
        buf.writeByte(msg.id);
        buf.writeVarInt(msg.size);
        buf.writeInt(msg.entityId);
    }

    public int getId() {
        return this.id;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(OpenEquipmentPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            PacketHandler.openEquipmentMenu(msg);
        });
        context.get().setPacketHandled(true);
    }
}