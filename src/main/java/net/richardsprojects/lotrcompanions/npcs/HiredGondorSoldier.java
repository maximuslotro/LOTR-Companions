/**
 * This file has been modified from the Human Companions Mod
 * which can be found here:
 *
 * https://github.com/justinwon777/HumanCompanions/tree/main
 */

package net.richardsprojects.lotrcompanions.npcs;

import com.github.maximuslotro.lotrrextended.common.network.ExtendedCOpenHiredMenuPacket;
import com.github.maximuslotro.lotrrextended.common.network.ExtendedPacketHandler;
import lotr.common.entity.npc.ExtendedHirableEntity;
import lotr.common.entity.npc.GondorSoldierEntity;

import lotr.common.entity.npc.ai.goal.*;
import lotr.common.util.ExtendedHiredUnitHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.*;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class HiredGondorSoldier extends GondorSoldierEntity implements ExtendedHirableEntity {

    private static final int HEAL_RATE = 80;
    private int lastHealed = 0;
    protected static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.BYTE);
    protected static final DataParameter<Optional<UUID>> DATA_OWNERUUID_ID = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.OPTIONAL_UUID);
    private static final DataParameter<Integer> LVL = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.INT);
    private static final DataParameter<Float> CURRENT_XP = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.FLOAT);

    private static final DataParameter<Integer> BASE_HEALTH = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.INT);
    private static final DataParameter<Float> MAX_XP = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.FLOAT);

    private static final DataParameter<Integer> KILLS = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.INT);

    private static final DataParameter<Boolean> FOLLOWING = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);

    private static final DataParameter<Boolean> STATIONARY = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PATROLLING = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ALERT = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GUARDING = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);

    private static final DataParameter<Float> TMP_LAST_HEALTH = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.FLOAT);
    private boolean tmpHealthLoaded = false;
    private boolean healthUpdateFromTmpHealth = false;

    private static final DataParameter<Boolean> INVENTORY_OPEN = EntityDataManager.defineId(HiredGondorSoldier.class,
            DataSerializers.BOOLEAN);

    // Data Parameters for all equipment so it can be automatically synced
    private static final DataParameter<ItemStack> EQUIPMENT_CHEST = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> EQUIPMENT_HEAD = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> EQUIPMENT_LEGS = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> EQUIPMENT_FEET = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> EQUIPMENT_MAINHAND = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<ItemStack> EQUIPMENT_OFFHAND = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.ITEM_STACK);

    private static final DataParameter<Boolean> EQUIPMENT_OPEN = EntityDataManager.defineId(HiredGondorSoldier.class, DataSerializers.BOOLEAN);

    // 9 inventory slots + 6 player equipment slots + 6 base equipment slots
    private Inventory internalUnitInventory = new Inventory(21);
    // Use to initiate equipment sync. This is done on creation, nbt load, and on inventory change
    private boolean inventoryNeedsSetupSyncing = false;

    /* Remove consuming goals since we have our own */
    @Override
    protected void addConsumingGoals(int prio) {}

    @Override
    protected void addNPCAI() {
        // reimplement only minimum AI and attack Goals - the rest are added in the register method
        ((GroundPathNavigator)this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.setPathfindingMalus(PathNodeType.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1.0F);
        this.initialiseAttackGoals(getAttackGoalsHolder());
        this.addNPCTargetingAI();
        this.addAttackGoal(2);
    }

    public HiredGondorSoldier(EntityType<? extends GondorSoldierEntity> entityType, World level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setTame(false);
    }
   
    public ILivingEntityData finalizeSpawn(IServerWorld sw, DifficultyInstance diff, SpawnReason reason, ILivingEntityData spawnData, CompoundNBT dataTag) {
    	spawnData = super.finalizeSpawn(sw, diff, reason, spawnData, dataTag);
        // set initial entity base equipment based upon pools
    	// Also indicates if unit was spawned incorrectly if it spawns with no base equipment
        internalUnitInventory.setItem(15, getUnitProfile().getEquipmentPools().getRandomHelmetStack(random));
        internalUnitInventory.setItem(16, getUnitProfile().getEquipmentPools().getRandomChestplateStack(random));
        internalUnitInventory.setItem(17, getUnitProfile().getEquipmentPools().getRandomLeggingsStack(random));
        internalUnitInventory.setItem(18, getUnitProfile().getEquipmentPools().getRandomBootsStack(random));
        internalUnitInventory.setItem(19, getUnitProfile().getEquipmentPools().getRandomWeaponStack(random));
        internalUnitInventory.setItem(20, getUnitProfile().getEquipmentPools().getRandomOffhandStack(random));
        // Needed to sync unit equipment on unit creation
        inventoryNeedsSetupSyncing=true;
    	return spawnData;
    }
    
    @Override
	public void updateHeadSlot(ItemStack stack) {
    	internalUnitInventory.setItem(9, stack);
        inventoryNeedsSetupSyncing=true;
    }

    @Override
	public void updateChestSlot(ItemStack stack) {
    	internalUnitInventory.setItem(10, stack);
        inventoryNeedsSetupSyncing=true;
    }

    @Override
	public void updateLegsSlot(ItemStack stack) {
    	internalUnitInventory.setItem(11, stack);
        inventoryNeedsSetupSyncing=true;
    }

    @Override
	public void updateFeetSlot(ItemStack stack) {
    	internalUnitInventory.setItem(12, stack);
        inventoryNeedsSetupSyncing=true;
    }

    @Override
	public void updateMainhandSlot(ItemStack stack) {
    	internalUnitInventory.setItem(13, stack);
        inventoryNeedsSetupSyncing=true;
    }

    @Override
	public void updateOffhandSlot(ItemStack stack) {
    	internalUnitInventory.setItem(14, stack);
        inventoryNeedsSetupSyncing=true;
    }

	@Override
	public Inventory getCustomInventory() {
		return internalUnitInventory;
	}

    @Override
    public ItemStack eat(World world, ItemStack stack) {
        if (stack.isEdible()) {
            this.heal(stack.getItem().getFoodProperties().getNutrition());
        }
        super.eat(world, stack);
        return stack;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
        this.entityData.define(LVL, 1);
        this.entityData.define(CURRENT_XP, 0f);
        this.entityData.define(MAX_XP, 1f);
        this.entityData.define(KILLS, 0);
        this.entityData.define(FOLLOWING, false);
        this.entityData.define(GUARDING, false);
        this.entityData.define(PATROLLING, false);
        this.entityData.define(ALERT, false);
        this.entityData.define(STATIONARY, false);
        this.entityData.define(BASE_HEALTH, 30);
        this.entityData.define(INVENTORY_OPEN, false);
        this.entityData.define(EQUIPMENT_OPEN, false);
        this.entityData.define(TMP_LAST_HEALTH, 30f);

        // equipment slots
        this.entityData.define(EQUIPMENT_CHEST, ItemStack.EMPTY);
        this.entityData.define(EQUIPMENT_HEAD, ItemStack.EMPTY);
        this.entityData.define(EQUIPMENT_LEGS, ItemStack.EMPTY);
        this.entityData.define(EQUIPMENT_FEET, ItemStack.EMPTY);
        this.entityData.define(EQUIPMENT_MAINHAND, ItemStack.EMPTY);
        this.entityData.define(EQUIPMENT_OFFHAND, ItemStack.EMPTY);
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return GondorSoldierEntity.regAttrs()
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D);
    }

    /**
     * This method is run every tick on both the client and server to synchronize equipment
     * Server will only sync equipment when changes are detected (peformance reasons)
     * Both will set the equipment slot values from their cached entityData values 
     */
    public void synchronizeEquipment() {
        // only sync inventory when it actualy needs syncing
    	if(!level.isClientSide && inventoryNeedsSetupSyncing) {
    		 entityData.set(EQUIPMENT_HEAD, internalUnitInventory.getItem(9).isEmpty() ? internalUnitInventory.getItem(15) : internalUnitInventory.getItem(9));
        	 entityData.set(EQUIPMENT_CHEST, internalUnitInventory.getItem(10).isEmpty() ? internalUnitInventory.getItem(16) : internalUnitInventory.getItem(10));
        	 entityData.set(EQUIPMENT_LEGS, internalUnitInventory.getItem(11).isEmpty() ? internalUnitInventory.getItem(17) : internalUnitInventory.getItem(11));
        	 entityData.set(EQUIPMENT_FEET, internalUnitInventory.getItem(12).isEmpty() ? internalUnitInventory.getItem(18) : internalUnitInventory.getItem(12));
        	 entityData.set(EQUIPMENT_MAINHAND, internalUnitInventory.getItem(13).isEmpty() ? internalUnitInventory.getItem(19) : internalUnitInventory.getItem(13));
        	 entityData.set(EQUIPMENT_OFFHAND, internalUnitInventory.getItem(14).isEmpty() ? internalUnitInventory.getItem(20) : internalUnitInventory.getItem(14));
        	 inventoryNeedsSetupSyncing=false;
    	}
        // Set the {@Link MobEntity} equipment fields here, since the entityData fields 
        // are the correct values between player and base equipment, as sorted above
        setItemSlot(EquipmentSlotType.HEAD, entityData.get(EQUIPMENT_HEAD));
        setItemSlot(EquipmentSlotType.CHEST, entityData.get(EQUIPMENT_CHEST));
        setItemSlot(EquipmentSlotType.LEGS, entityData.get(EQUIPMENT_LEGS));
        setItemSlot(EquipmentSlotType.FEET, entityData.get(EQUIPMENT_FEET));
        setItemSlot(EquipmentSlotType.MAINHAND, entityData.get(EQUIPMENT_MAINHAND));
        setItemSlot(EquipmentSlotType.OFFHAND, entityData.get(EQUIPMENT_OFFHAND));
    }

    public void setExpLvl(int lvl) {
        this.entityData.set(LVL, lvl);
    }

    public int getExpLvl() {
        return this.entityData.get(LVL);
    }

    public void setMobKills(int kills) {
        this.entityData.set(KILLS, kills);
    }

    public int getMobKills() {
        return this.entityData.get(KILLS);
    }

    public ItemStack checkFood() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemstack = this.internalUnitInventory.getItem(i);
            if (itemstack.isEdible()) {
                return itemstack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void setCurrentXp(float currentXp) {
        this.entityData.set(CURRENT_XP, currentXp);
    }

    public float getCurrentXp() {
        return this.entityData.get(CURRENT_XP);
    }

    public boolean isInventoryOpen() {
        return this.entityData.get(INVENTORY_OPEN);
    }

    public void setMaxXp(float maxXp) {
        this.entityData.set(MAX_XP, maxXp);
    }

    public void setInventoryOpen(boolean isOpen) {
        this.entityData.set(INVENTORY_OPEN, isOpen);
    }

    public boolean isEquipmentOpen() {
        return this.entityData.get(EQUIPMENT_OPEN);
    }

    public void setEquipmentOpen(boolean isOpen) {
        this.entityData.set(EQUIPMENT_OPEN, isOpen);
    }

    public float getMaxXp() {
        return this.entityData.get(MAX_XP);
    }

    public void setBaseHealth(int health) {
        this.entityData.set(BASE_HEALTH, health);
    }

    public int getBaseHealth() {
        return this.entityData.get(BASE_HEALTH);
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            if (this.isAlliedTo(player)) {
                if (this.level.isClientSide()) {
                    ExtendedPacketHandler.sendToServer(new ExtendedCOpenHiredMenuPacket(getId()));
                }
            }
            return ActionResultType.sidedSuccess(this.level.isClientSide());
        }
        return super.mobInteract(player, hand);
    }

    public boolean isAlliedTo(Entity p_184191_1_) {
        if (this.isTame()) {
            LivingEntity livingentity = this.getOwner();
            if (p_184191_1_ == livingentity) {
                return true;
            }

            if (livingentity != null) {
                return livingentity.isAlliedTo(p_184191_1_);
            }
        }

        return super.isAlliedTo(p_184191_1_);
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    public void setOwnerUUID(@Nullable UUID p_184754_1_) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(p_184754_1_));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SwimGoal(this));
        this.goalSelector.addGoal(1, new ExtendedHiredSitGoal(this));
        this.goalSelector.addGoal(3, new ExtendedHiredFollowPlayerGoal(this, 1.3D, 8.0F, 2.0F, false));
        this.goalSelector.addGoal(5, new ExtendedHiredWaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(8, new OpenDoorGoal(this, true));
        // TODO: Re-enable food eating one when I figure out how to get it to not mess up
        //  their attack animations
        //this.goalSelector.addGoal(9, new LowHealthGoal(this,this));
        //this.goalSelector.addGoal(10, new EatGoal(this,this));
        this.targetSelector.addGoal(1, new ExtendedHiredPlayerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new ExtendedHiredPlayerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
    }

    public boolean wantsToAttack(LivingEntity p_142018_1_, LivingEntity p_142018_2_) {
        return true;
    }

    public void addAdditionalSaveData(CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        if (this.getOwnerUUID() != null) {
            tag.putUUID("Owner", this.getOwnerUUID());
        }

        // create temp NonNullList to save inventory to
        NonNullList<ItemStack> items = NonNullList.withSize(21, ItemStack.EMPTY);
        for (int i = 0; i < 21; i++) {
            ItemStack item = internalUnitInventory.getItem(i);
            items.set(i, item);
        }
        ItemStackHelper.saveAllItems(tag, items);

        tag.putBoolean("following", this.isFollowing());
        tag.putBoolean("stationary", this.isStationary());
        tag.putInt("mob_kills", this.getMobKills());
        tag.putInt("xp_level", this.getExpLvl());
        tag.putFloat("current_xp", this.getCurrentXp());
        tag.putFloat("max_xp", this.getMaxXp());
        tag.putInt("base_health", this.getBaseHealth());
        tag.putFloat("tmp_last_health", this.getHealth());
    }

    private float getTmpLastHealth() {
        return this.entityData.get(TMP_LAST_HEALTH);
    }

    public boolean isFollowing() {
        return this.entityData.get(FOLLOWING);
    }

    public boolean isPatrolling() {
        return this.entityData.get(PATROLLING);
    }

    @Override
    public float getHiredUnitMaxHealth() {
        return getMaxHealth();
    }

    @Override
    public float getHiredUnitHealth() {
        return getHealth();
    }

    public boolean isAlert() {
        return this.entityData.get(ALERT);
    }

    public boolean isStationary() {
        return this.entityData.get(STATIONARY);
    }

    public void setStationary(boolean stationary) {
        this.entityData.set(STATIONARY, stationary);
    }

    public void setFollowing(boolean following) {
        this.entityData.set(FOLLOWING, following);
    }

    public void tame(PlayerEntity p_193101_1_) {
        this.setTame(true);
        this.setOwnerUUID(p_193101_1_.getUUID());
        this.setFollowing(true);
    }

    public void setTame(boolean p_70903_1_) {
        byte b0 = this.entityData.get(DATA_FLAGS_ID);
        if (p_70903_1_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uuid = this.getOwnerUUID();
            return uuid == null ? null : this.level.getPlayerByUUID(uuid);
        } catch (IllegalArgumentException illegalargumentexception) {
            return null;
        }
    }

    public void readAdditionalSaveData(CompoundNBT tag) {
        super.readAdditionalSaveData(tag);

        UUID uuid;
        if (tag.hasUUID("Owner")) {
            uuid = tag.getUUID("Owner");
        } else {
            String ownerString = tag.getString("Owner");
            uuid = PreYggdrasilConverter.convertMobOwnerIfNecessary(this.getServer(), ownerString);
        }

        if (uuid != null) {
            try {
                this.setOwnerUUID(uuid);
                this.setTame(true);
            } catch (Throwable var4) {
                this.setTame(false);
            }
        }

        NonNullList<ItemStack> itemsStacks = NonNullList.withSize(21, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, itemsStacks);
        for (int i = 0; i < 21; i++) {
            this.internalUnitInventory.setItem(i, itemsStacks.get(i));
        }

        // Need to setup equipment on NBT load
        inventoryNeedsSetupSyncing = true;

        if (tag.contains("following")) {
            this.setFollowing(tag.getBoolean("following"));
        }
        if (tag.contains("xp_level")) {
            this.setExpLvl(tag.getInt("xp_level"));
        }
        if (tag.contains("current_xp")) {
            this.setCurrentXp(tag.getInt("current_xp"));
        }
        if (tag.contains("max_xp")) {
            this.setMaxXp(tag.getInt("max_xp"));
        }
        if (tag.contains("mob_kills")) {
            this.setMobKills(tag.getInt("mob_kills"));
        }
        if (tag.contains("base_health")) {
            this.setBaseHealth(tag.getInt("base_health"));
        }
        if (tag.contains("tmp_last_health")) {
            this.entityData.set(TMP_LAST_HEALTH, tag.getFloat("tmp_last_health"));
            this.setHealth(tag.getFloat("tmp_last_health"));
            tmpHealthLoaded = true;
        }

        if (tag.contains("stationary")) {
            this.setStationary(tag.getBoolean("stationary"));
        }
    }

    public void tick() {
        // heal
        if (lastHealed >= HEAL_RATE)  {
        	// Ensure that entity is not dead before healing
            if (this.getHealth() < this.getMaxHealth() && this.isAlive()) {
            	this.setHealth(this.getHealth() + 1);
            	lastHealed = 0;
            }
        } else {
            lastHealed++;
        }

        checkStats();
        synchronizeEquipment();

        if (tmpHealthLoaded && !healthUpdateFromTmpHealth) {
            this.setHealth(getTmpLastHealth());
            healthUpdateFromTmpHealth = true;
        }

        super.tick();
    }

    public void die(DamageSource p_70645_1_) {
        ExtendedHiredUnitHelper.die(this.level, p_70645_1_, this);
        super.die(p_70645_1_);
    }

    public boolean isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    public void checkStats() {
        if ((int) this.getMaxHealth() != getBaseHealth()) {
            modifyMaxHealth(getBaseHealth() - 30, "Base Health from current level", false);
        }
    }

    @Override
    public int getHiredUnitId() {
        return getId();
    }

    public void modifyMaxHealth(int change, String name, boolean permanent) {
        ModifiableAttributeInstance attributeInstance = this.getAttribute(Attributes.MAX_HEALTH);
        Set<AttributeModifier> modifiers = attributeInstance.getModifiers();
        if (!modifiers.isEmpty()) {
            Iterator<AttributeModifier> iterator = modifiers.iterator();
            while (iterator.hasNext()) {
                AttributeModifier attributeModifier = iterator.next();
                if (attributeModifier != null && attributeModifier.getName().equals(name)) {
                    this.getAttribute(Attributes.MAX_HEALTH).removeModifier(attributeModifier);
                }
            }
        }
        AttributeModifier HEALTH_MODIFIER = new AttributeModifier(name,
                change, AttributeModifier.Operation.ADDITION);
        if (permanent) {
            attributeInstance.addPermanentModifier(HEALTH_MODIFIER);
        } else {
            attributeInstance.addTransientModifier(HEALTH_MODIFIER);
        }
    }

    @Override
    public void setHiredUnitHealth(float p_70606_1_) {
        setHealth(p_70606_1_);
    }

    public void giveExperiencePoints(float points) {
        ExtendedHiredUnitHelper.giveExperiencePoints(this, points);
    }

	@Override
	public ResourceLocation getProfileLocation() {
		return new ResourceLocation("lotrcompanions:gondor_soldier");
	}

	@Override 
    protected void dropCustomDeathLoot(DamageSource damageSource, int looting, boolean player) {
    	// Disabled due to NPCEntity using new logic we don't want for unit drops
    	//super.dropCustomDeathLoot(damageSource, looting, player);
		ExtendedHiredUnitHelper.dropCustomDeathLoot(this, this, random, internalUnitInventory, looting, player);
    }

	@Override
	public float getHiredUnitEquipmentDropChance(EquipmentSlotType arg0) {
		return getEquipmentDropChance(arg0);
	}
}
