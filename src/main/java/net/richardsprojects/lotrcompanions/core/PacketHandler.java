/**
 * This file has been modified from the Human Companions Mod
 * which can be found here:
 *
 * https://github.com/justinwon777/LOTRCompanions/tree/main
 */
package net.richardsprojects.lotrcompanions.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import net.richardsprojects.lotrcompanions.LOTRCompanions;
import net.richardsprojects.lotrcompanions.client.screen.CompanionEquipmentScreen;
import net.richardsprojects.lotrcompanions.client.screen.CompanionScreen;
import net.richardsprojects.lotrcompanions.container.CompanionContainer;
import net.richardsprojects.lotrcompanions.container.CompanionEquipmentContainer;
import net.richardsprojects.lotrcompanions.npcs.HiredBreeGuard;
import net.richardsprojects.lotrcompanions.npcs.HiredGondorSoldier;
import net.richardsprojects.lotrcompanions.networking.*;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(new ResourceLocation(LOTRCompanions.MOD_ID,
                    "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, OpenInventoryPacket.class, OpenInventoryPacket::encode, OpenInventoryPacket::decode, OpenInventoryPacket::handle);
        INSTANCE.registerMessage(id++, SetAlertPacket.class, SetAlertPacket::encode, SetAlertPacket::decode,
                SetAlertPacket::handle);
        INSTANCE.registerMessage(id++, SetPatrollingPacket.class, SetPatrollingPacket::encode, SetPatrollingPacket::decode,
                SetPatrollingPacket::handle);
        INSTANCE.registerMessage(id++, ClearTargetPacket.class, ClearTargetPacket::encode, ClearTargetPacket::decode,
                ClearTargetPacket::handle);
        INSTANCE.registerMessage(id++, SetStationaryPacket.class, SetStationaryPacket::encode, SetStationaryPacket::decode,
                SetStationaryPacket::handle);
        INSTANCE.registerMessage(id++, ReleasePacket.class, ReleasePacket::encode, ReleasePacket::decode,
                ReleasePacket::handle);
        INSTANCE.registerMessage(id++, CompanionsClientOpenEquipmentPacket.class, CompanionsClientOpenEquipmentPacket::encode, CompanionsClientOpenEquipmentPacket::decode, CompanionsClientOpenEquipmentPacket::handle);
        INSTANCE.registerMessage(id++, CompanionsClientOpenMenuPacket.class, CompanionsClientOpenMenuPacket::encode, CompanionsClientOpenMenuPacket::decode, CompanionsClientOpenMenuPacket::handle);
    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    public static void openInventory(OpenInventoryPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.getEntityId());

            if (entity instanceof HiredGondorSoldier) {
                HiredGondorSoldier companion = (HiredGondorSoldier) entity;

                ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;

                CompanionContainer container = new CompanionContainer(packet.getId(), player.inventory, companion.inventory, companion.getId());
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new CompanionScreen(container, player.inventory, companion));
            } else if (entity instanceof HiredBreeGuard) {
                HiredBreeGuard companion = (HiredBreeGuard) entity;

                ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;

                CompanionContainer container = new CompanionContainer(packet.getId(), player.inventory, companion.inventory, companion.getId());
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new CompanionScreen(container, player.inventory, companion));
            }
        }
    }

    /*
    @SuppressWarnings("resource")
    @OnlyIn(Dist.CLIENT)
    public static void openEquipmentMenu(CompanionsServerOpenEquipmentPacket packet) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Entity entity = player.level.getEntity(packet.getEntityId());

            if (entity instanceof HiredGondorSoldier) {
                HiredGondorSoldier companion = (HiredGondorSoldier) entity;
                ITextComponent title = new TranslationTextComponent("container.lotrcompanions.equipment",
                        new StringTextComponent(companion.getPersonalInfo().getName()));

                ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;

                CompanionEquipmentContainer container = new CompanionEquipmentContainer(packet.getId(), player.inventory, companion.inventory, companion.getId());
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new CompanionEquipmentScreen(container, player.inventory, companion, title));
            } else if (entity instanceof HiredBreeGuard) {
                HiredBreeGuard companion = (HiredBreeGuard) entity;
                companion.getPersonalInfo().getName();
                ITextComponent title = new TranslationTextComponent("container.lotrcompanions.equipment",
                        new StringTextComponent(companion.getPersonalInfo().getName()));

                ClientPlayerEntity clientplayerentity = Minecraft.getInstance().player;

                CompanionEquipmentContainer container = new CompanionEquipmentContainer(packet.getId(), player.inventory, companion.inventory, companion.getId());
                clientplayerentity.containerMenu = container;
                Minecraft.getInstance().setScreen(new CompanionEquipmentScreen(container, player.inventory, companion, title));
            }
        }
    }*/
}
