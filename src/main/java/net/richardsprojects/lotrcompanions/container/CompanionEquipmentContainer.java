package net.richardsprojects.lotrcompanions.container;

import com.mojang.datafixers.util.Pair;
import lotr.common.item.SpearItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ResourceLocation;

public class CompanionEquipmentContainer extends Container {

    private final IInventory container;

    private Slot[] armorSlots = new Slot[4];
    private Slot mainHand;
    private Slot offHand;

    private int entityId;

    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlotType[] SLOT_IDS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};

    private static final int[] yPos = new int[]{31, 49, 67, 85};

    public CompanionEquipmentContainer(int p_39230_, PlayerInventory p_39231_, IInventory companionInv, int entityId) {
        super(null, p_39230_);
        this.container = companionInv;
        this.entityId = entityId;
        companionInv.startOpen(p_39231_.player);

        // setup 4 custom armor slots
        for(int slot = 9; slot < 13; slot++) {
            final EquipmentSlotType equipmentSlotType = SLOT_IDS[slot - 9];
            armorSlots[slot - 9] = this.addSlot(new Slot(companionInv, slot, 8, yPos[slot - 9]) {
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack itemStack) {
                    if (itemStack.getItem() instanceof ArmorItem) {
                        ArmorItem item = (ArmorItem) itemStack.getItem();
                        return item.getSlot() == equipmentSlotType;
                    } else {
                        return false;
                    }
                }

                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(PlayerContainer.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentSlotType.getIndex()]);
                }
            });
        }

        mainHand = this.addSlot(new Slot(companionInv, 13,61,64));
        offHand = this.addSlot(new Slot(companionInv, 14,61,82)  {
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return this.container.stillValid(player);
    }

    public ItemStack quickMoveStack(PlayerEntity player, int p_39254_) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(p_39254_);

        // handle shift-clicking the main hand and sending it back to the player's inventory
        if (slot != null && slot.equals(mainHand) && slot.hasItem()) {
            if (player.inventory.getFreeSlot() > -1 || player.inventory.getSlotWithRemainingSpace(slot.getItem()) > -1) {
                player.addItem(slot.getItem());
                mainHand.set(ItemStack.EMPTY);
                mainHand.setChanged();
            } else {
                return ItemStack.EMPTY;
            }
            // handle shift-clicking offhand and sending it back to the player's inventory
        } else if (slot != null && slot.equals(offHand) && slot.hasItem()) {
            if (player.inventory.getFreeSlot() > -1 || player.inventory.getSlotWithRemainingSpace(slot.getItem()) > -1) {
                player.addItem(slot.getItem());
                offHand.set(ItemStack.EMPTY);
                offHand.setChanged();
            } else {
                return ItemStack.EMPTY;
            }
        } else if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            boolean slotUpdated = false;
            if (itemstack1.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) itemstack1.getItem();
                if (armor.getSlot() == EquipmentSlotType.HEAD && !armorSlots[0].hasItem()) {
                    armorSlots[0].set(itemstack1);
                    armorSlots[0].setChanged();
                    slotUpdated = true;
                } else if (armor.getSlot() == EquipmentSlotType.CHEST && !armorSlots[1].hasItem()) {
                    armorSlots[1].set(itemstack1);
                    armorSlots[1].setChanged();
                    slotUpdated = true;
                } else if (armor.getSlot() == EquipmentSlotType.LEGS && !armorSlots[2].hasItem()) {
                    armorSlots[2].set(itemstack1);
                    armorSlots[2].setChanged();
                    slotUpdated = true;
                } else if (armor.getSlot() == EquipmentSlotType.FEET && !armorSlots[3].hasItem()) {
                    armorSlots[3].set(itemstack1);
                    armorSlots[3].setChanged();
                    slotUpdated = true;
                }
            }

            if (itemstack1.getItem() instanceof SwordItem || itemstack1.getItem() instanceof SpearItem) {
                mainHand.set(itemstack1);
                mainHand.setChanged();
                slotUpdated = true;
            }

            if (itemstack1.getItem() instanceof ShieldItem) {
                offHand.set(itemstack1);
                offHand.setChanged();
                slotUpdated = true;
            }

            if (slotUpdated) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
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

    public int getEntityId() {
        return entityId;
    }

}
