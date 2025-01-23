/**
 * This file has been modified from the Human Companions Mod
 * which can be found here:
 *
 * https://github.com/justinwon777/LOTRCompanions/tree/main
 */
package net.richardsprojects.lotrcompanions.core;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import net.richardsprojects.lotrcompanions.LOTRCompanions;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE =
            NetworkRegistry.newSimpleChannel(new ResourceLocation(LOTRCompanions.MOD_ID,
                    "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void register() {
        // All networking packets are now in Renewed Extended
        /**
        int id = 0;
        INSTANCE.registerMessage(id++, SetAlertPacket.class, SetAlertPacket::encode, SetAlertPacket::decode,
                SetAlertPacket::handle);
        INSTANCE.registerMessage(id++, ClearTargetPacket.class, ClearTargetPacket::encode, ClearTargetPacket::decode,
                ClearTargetPacket::handle);
        INSTANCE.registerMessage(id++, SetStationaryPacket.class, SetStationaryPacket::encode, SetStationaryPacket::decode,
                SetStationaryPacket::handle);
        INSTANCE.registerMessage(id++, ReleasePacket.class, ReleasePacket::encode, ReleasePacket::decode,
                ReleasePacket::handle);
        INSTANCE.registerMessage(id++, CompanionsClientOpenEquipmentPacket.class, CompanionsClientOpenEquipmentPacket::encode, CompanionsClientOpenEquipmentPacket::decode, CompanionsClientOpenEquipmentPacket::handle);
        INSTANCE.registerMessage(id++, CompanionsClientOpenMenuPacket.class, CompanionsClientOpenMenuPacket::encode, CompanionsClientOpenMenuPacket::decode, CompanionsClientOpenMenuPacket::handle);
        **/
    }

    public static void sendToServer(Object msg) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

}
