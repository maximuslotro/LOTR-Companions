package net.richardsprojects.lotrcompanions.client;

import net.minecraft.client.gui.ScreenManager;
import net.richardsprojects.lotrcompanions.client.screen.CompanionEquipmentScreen;
import net.richardsprojects.lotrcompanions.client.screen.CompanionScreen;
import net.richardsprojects.lotrcompanions.container.CompanionsContainers;

public class ContainerScreenHelper {

    public static void registerScreens() {
        ScreenManager.register(CompanionsContainers.COMPANION_EQUIPMENT_CONTAINER.get(), CompanionEquipmentScreen::new);
        ScreenManager.register(CompanionsContainers.COMPANION_MAIN_CONTAINER.get(), CompanionScreen::new);
    }
}
