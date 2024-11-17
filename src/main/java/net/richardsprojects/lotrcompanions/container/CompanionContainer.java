package net.richardsprojects.lotrcompanions.container;

import lotr.common.entity.npc.NPCEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;

import java.util.List;

public class CompanionContainer extends Container {
    private final IInventory container;
    private final int containerRows = 1;

    private int entityId;

    private NPCEntity companion;

    private PlayerEntity player;

    public PlayerEntity getPlayer() {
       return player;
    }
    public CompanionContainer(int p_39230_, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(CompanionsContainers.COMPANION_MAIN_CONTAINER.get(), p_39230_);

        // read in packet data
        CompoundNBT nbt = extraData.readAnySizeNbt();
        this.entityId = nbt.getInt("entityId");

        this.player = playerInventory.player;
        this.companion = (NPCEntity) this.player.level.getEntity(entityId);

        IInventory companionInv = new Inventory(9);
        if (companion instanceof HiredBreeGuard) {
            companionInv = ((HiredBreeGuard) companion).inventory;
        } else if (companion instanceof HiredGondorSoldier) {
            companionInv = ((HiredGondorSoldier) companion).inventory;
        }

        checkContainerSize(companionInv, companionInv.getContainerSize());
        this.container = companionInv;
        companionInv.startOpen(playerInventory.player);

        // add the 9 companion inventory slots
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(companionInv, k, 8 + k * 18, 110));
        }

        // add the 3 rows of player inventory
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 142 + l * 18));
            }
        }

        // add the player's hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 200));
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public boolean stillValid(PlayerEntity p_39242_) {
        return this.container.stillValid(p_39242_);
    }

    public ItemStack quickMoveStack(PlayerEntity player, int p_39254_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39254_);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (p_39254_ < this.containerRows * 9) {
                if (!this.moveItemStackTo(itemstack1, this.containerRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.containerRows * 9, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public void removed(PlayerEntity p_39251_) {
        super.removed(p_39251_);
        this.container.stopOpen(p_39251_);
    }

    public int getRowCount() {
        return this.containerRows;
    }

    public static void writeContainerInitData(PacketBuffer extraData, int entityId) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("entityId", entityId); // write the entity Id

        // write their equipment including empty slots
        // TODO: Not sure if this will need to be re-implemented so leaving it for now
        /*ListNBT listnbt = new ListNBT();
        for (int i = 0; i < 6; i++) {
            ItemStack stack = equipment.get(i);

            CompoundNBT compoundnbt = new CompoundNBT();
            CompoundNBT itemTag = new CompoundNBT();
            itemTag = stack.save(itemTag);
            compoundnbt.put("item", itemTag);
            listnbt.add(compoundnbt);
        }
        nbt.put("equipment", listnbt);*/

        extraData.writeNbt(nbt);
    }
}

