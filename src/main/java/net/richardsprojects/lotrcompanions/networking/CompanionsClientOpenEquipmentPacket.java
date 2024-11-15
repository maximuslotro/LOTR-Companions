package net.richardsprojects.lotrcompanions.networking;

import io.netty.buffer.Unpooled;
import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import net.richardsprojects.lotrcompanions.container.CompanionEquipmentContainer;
import net.richardsprojects.lotrcompanions.container.CompanionsContainers;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;
import net.richardsprojects.lotrcompanions.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class CompanionsClientOpenEquipmentPacket {
    private final int entityId;

    public CompanionsClientOpenEquipmentPacket(int entityId) {
        this.entityId = entityId;
    }

    public static CompanionsClientOpenEquipmentPacket decode(PacketBuffer buf) {
        return new CompanionsClientOpenEquipmentPacket(buf.readInt());
    }

    public static void encode(CompanionsClientOpenEquipmentPacket msg, PacketBuffer buf) {
        buf.writeInt(msg.entityId);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public static void handle(CompanionsClientOpenEquipmentPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // fill in equipment array list
            final List<ItemStack> equipment = new ArrayList<>(6);
            Entity entity = Objects.requireNonNull(context.get().getSender()).level.getEntity(msg.entityId);
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
            }

            // Prepare PacketBuffer with initialization data
            PacketBuffer initData = new PacketBuffer(Unpooled.buffer());
            CompanionEquipmentContainer.writeContainerInitData(initData, msg.entityId, equipment);

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
            NetworkHooks.openGui(context.get().getSender(),
                    new SimpleNamedContainerProvider(
                            (windowId, playerInventory, player) ->
                                    CompanionsContainers.COMPANION_EQUIPMENT_CONTAINER.get().create(windowId, playerInventory, initData),
                            CompanionEquipmentContainer.CONTAINER_TITLE
                    ),
                    buf -> CompanionEquipmentContainer.writeContainerInitData(buf, msg.entityId, equipment)
            );

            context.get().setPacketHandled(true);
        });
    }

    private boolean areItemStacksExactlyEqual(ItemStack stack1, ItemStack stack2) {
        // first check for direct equality
        if (stack1 == stack2) {
            return true;
        }

        // if either of the stacks is empty, they're not equal
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return false;
        }

        // check item type
        if (!stack1.getItem().equals(stack2.getItem())) {
            return false;
        }

        // check stack size
        if (stack1.getCount() != stack2.getCount()) {
            return false;
        }

        // check NBT tags for exact equality
        CompoundNBT nbt1 = stack1.getTag();
        CompoundNBT nbt2 = stack2.getTag();
        return nbt1 == null ? nbt2 == null : nbt1.equals(nbt2);
    }
}