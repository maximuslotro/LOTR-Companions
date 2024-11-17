package net.richardsprojects.lotrcompanions.networking;

import io.netty.buffer.Unpooled;
import lotr.common.entity.npc.ExtendedHirableEntity;
import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;
import net.richardsprojects.lotrcompanions.container.CompanionContainer;
import net.richardsprojects.lotrcompanions.container.CompanionEquipmentContainer;
import net.richardsprojects.lotrcompanions.container.CompanionsContainers;
import net.richardsprojects.lotrcompanions.core.PacketHandler;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;
import net.richardsprojects.lotrcompanions.utils.Constants;

import java.util.ArrayList;
import java.util.List;
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

    /*public static void handle(CompanionsClientOpenMenuPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            CompanionsClientOpenMenuPacket.processPacket(Objects.requireNonNull(context.get().getSender()), msg);
        });

        context.get().setPacketHandled(true);
    }*/

    public static void handle(CompanionsClientOpenMenuPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // fill in equipment array list
            final List<ItemStack> equipment = new ArrayList<>(6);
            Entity entity = Objects.requireNonNull(context.get().getSender()).level.getEntity(msg.entityId);

            /*
            Inventory inventory = null;
            ItemStack[] baseGear = Constants.getBaseGear(entity);
            if (entity instanceof HiredGondorSoldier) {
                inventory = ((HiredGondorSoldier) entity).inventory;
            } else if (entity instanceof HiredBreeGuard) {
                inventory = ((HiredBreeGuard) entity).inventory;
            }
            for (int i = 9; i < 15; i++) {
                if (inventory != null) {
                    ItemStack item = inventory.getItem(i);
                    if (baseGear[i - 9] != null) {
                        if (msg.areItemStacksExactlyEqual(baseGear[i - 9], item)) {
                            item = ItemStack.EMPTY;
                        }
                    }

                    equipment.add(item);
                } else {
                    equipment.add(ItemStack.EMPTY);
                }
            }*/

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

    // TODO: Check if this is no longer necessary and remove it
/*
        public static void processPacket(ServerPlayerEntity player, CompanionsClientOpenMenuPacket msg) {
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }

        IInventory companionInventory = null;

        if (player.level.getEntity(msg.getEntityId()) instanceof NPCEntity) {
           NPCEntity npcEntity = (NPCEntity) player.level.getEntity(msg.getEntityId());
           if (npcEntity instanceof HiredBreeGuard) {
               ((HiredBreeGuard) npcEntity).setInventoryOpen(true);
               companionInventory = ((HiredBreeGuard) npcEntity).inventory;
           }
            if (npcEntity instanceof HiredGondorSoldier) {
                ((HiredGondorSoldier) npcEntity).setInventoryOpen(true);
                companionInventory = ((HiredGondorSoldier) npcEntity).inventory;
            }
        }

        if (companionInventory != null) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new OpenInventoryPacket(
                    player.containerCounter, companionInventory.getContainerSize(), msg.getEntityId()));
            player.nextContainerCounter();

            player.containerMenu = new CompanionContainer(
                    player.containerCounter, player.inventory, companionInventory, msg.getEntityId()
            );

            player.containerMenu.addSlotListener(player);
            MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, player.containerMenu));
        }
    }
    */
}