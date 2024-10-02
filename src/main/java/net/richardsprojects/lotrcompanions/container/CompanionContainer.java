package net.richardsprojects.lotrcompanions.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CompanionContainer extends Container {
    private final IInventory container;
    private final int containerRows = 1;

    private int entityId;

    private PlayerEntity player;
/*
    private Slot[] armorSlots = new Slot[4];
    private Slot mainHand;
    private Slot offHand;

    public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
    public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
    private static final EquipmentSlotType[] SLOT_IDS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
*/
    private static final int[] yPos = new int[]{31, 49, 67, 85};

    public PlayerEntity getPlayer() {
       return player;
    }

    public CompanionContainer(int p_39230_, PlayerInventory p_39231_, IInventory companionInv, int entityId) {
        super(null, p_39230_);
        checkContainerSize(companionInv, companionInv.getContainerSize());
        this.container = companionInv;
        this.entityId = entityId;
        this.player = p_39231_.player;
        companionInv.startOpen(p_39231_.player);

        // add the 9 companion inventory slots
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(companionInv, k, 8 + k * 18, 110));
        }

        // add the 3 rows of player inventory
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(p_39231_, j1 + l * 9 + 9, 8 + j1 * 18, 142 + l * 18));
            }
        }

        // add the player's hotbar
        for (int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(p_39231_, i1, 8 + i1 * 18, 200));
        }

        /*
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

        // add the companion main hand and off hand slots
        mainHand = this.addSlot(new Slot(companionInv, 13,44,67));
        offHand = this.addSlot(new Slot(companionInv, 14,44,85) {
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(PlayerContainer.BLOCK_ATLAS, EMPTY_ARMOR_SLOT_SHIELD);
            }
        });
        */
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

        // handle shift-clicking the main hand and sending it back to the player's inventory
        /*if (slot != null && slot.equals(mainHand) && slot.hasItem()) {
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
        } else*/ if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            boolean slotUpdated = false;
            /*
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
            }*/

            if (slotUpdated) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                return ItemStack.EMPTY;
            }

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
}

