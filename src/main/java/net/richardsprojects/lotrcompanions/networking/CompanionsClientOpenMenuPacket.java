package net.richardsprojects.lotrcompanions.networking;

import io.netty.buffer.Unpooled;
import lotr.common.entity.npc.ExtendedHirableEntity;
import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.richardsprojects.lotrcompanions.container.CompanionContainer;
import net.richardsprojects.lotrcompanions.container.CompanionsContainers;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;

import java.util.Objects;
import java.util.function.Supplier;

public class CompanionsClientOpenMenuPacket {
    private final int entityId;

    public CompanionsClientOpenMenuPacket(int entityId) {
        this.entityId = entityId;
    }

    public static CompanionsClientOpenMenuPacket decode(PacketBuffer buf) {
        return new CompanionsClientOpenMenuPacket(buf.readInt());
    }

    public static void encode(CompanionsClientOpenMenuPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
    }

    public int getId() {
        return this.entityId;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(CompanionsClientOpenMenuPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Entity entity = Objects.requireNonNull(context.get().getSender()).level.getEntity(msg.entityId);

            // Prepare PacketBuffer with initialization data
            PacketBuffer initData = new PacketBuffer(Unpooled.buffer());
            CompanionContainer.writeContainerInitData(initData, msg.entityId);

            ExtendedHirableEntity hirableEntity = null;
            if (entity instanceof ExtendedHirableEntity) hirableEntity = (ExtendedHirableEntity) entity;

            if (entity instanceof NPCEntity) {
                NPCEntity npcEntity = (NPCEntity) entity;
                if (npcEntity instanceof HiredBreeGuard) {
                    ((HiredBreeGuard) npcEntity).setEquipmentOpen(true);
                }
                if (npcEntity instanceof HiredGondorSoldier) {
                    ((HiredGondorSoldier) npcEntity).setEquipmentOpen(true);
                }
            }

            // Open GUI on client side
            assert hirableEntity != null;
            NetworkHooks.openGui(Objects.requireNonNull(context.get().getSender()),
                    new SimpleNamedContainerProvider(
                            (windowId, playerInventory, player) ->
                                    CompanionsContainers.COMPANION_MAIN_CONTAINER.get().create(windowId, playerInventory, initData),
                           hirableEntity.getHiredUnitName()
                    ),
                    buf -> CompanionContainer.writeContainerInitData(buf, msg.entityId)
            );

            context.get().setPacketHandled(true);
        });
    }

}