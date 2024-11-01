package net.richardsprojects.lotrcompanions.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lotr.common.entity.npc.ExtendedHirableEntity;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.richardsprojects.lotrcompanions.LOTRCompanions;
import net.richardsprojects.lotrcompanions.container.CompanionEquipmentContainer;
import net.richardsprojects.lotrcompanions.core.PacketHandler;
import net.richardsprojects.lotrcompanions.networking.CompanionsClientOpenMenuPacket;
import net.richardsprojects.lotrcompanions.utils.Constants;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CompanionEquipmentScreen extends ContainerScreen<CompanionEquipmentContainer> implements IHasContainer<CompanionEquipmentContainer> {

    private Button backButton;

    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(LOTRCompanions.MOD_ID,"textures/upgrade_equipment_menu.png");
    private final ExtendedHirableEntity companion;
    DecimalFormat df = new DecimalFormat("#.#");
    int sidebarX;

    private ItemStack[] baseGear;

    private LivingEntity entity = null;

    public CompanionEquipmentScreen(CompanionEquipmentContainer container, PlayerInventory p_98410_,
                                    ExtendedHirableEntity companion, ITextComponent title) {
        super(container, p_98410_, title);

        this.companion = companion;
        this.passEvents = false;
        this.imageHeight = 256;
        this.inventoryLabelY = 130;
        this.imageWidth = 226;
        df.setRoundingMode(RoundingMode.CEILING);
        sidebarX = 90;

        if (p_98410_.player.level.getEntity(container.getEntityId()) != null) {
           if (p_98410_.player.level.getEntity(container.getEntityId()) instanceof LivingEntity) {
               this.entity = (LivingEntity) p_98410_.player.level.getEntity(container.getEntityId());
           }
        }

        if (entity != null) baseGear = Constants.getBaseGear(entity);
    }

    @Override
    public void render(MatrixStack p_98418_, int p_98419_, int p_98420_, float p_98421_) {
        this.renderBackground(p_98418_);
        super.render(p_98418_, p_98419_, p_98420_, p_98421_);

        renderBaseGearSlot(leftPos + 25, topPos + 31, baseGear[0]);
        renderBaseGearSlot(leftPos + 25, topPos + 49, baseGear[1]);
        renderBaseGearSlot(leftPos + 25, topPos + 67, baseGear[2]);
        renderBaseGearSlot(leftPos + 25, topPos + 85, baseGear[3]);
        renderBaseGearSlot(leftPos + 61 + 18, topPos + 67, baseGear[4]);
        renderBaseGearSlot(leftPos + 61 + 18, topPos + 85, baseGear[5]);

        this.renderTooltip(p_98418_, p_98419_, p_98420_);
    }

    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        backButton = this.addButton(new Button(this.leftPos + 7, this.topPos + 105, 57, 20,  new TranslationTextComponent("gui.lotrextended.menu.back"), button -> openMainMenu()));
    }

    @SuppressWarnings("deprecation")
	protected void renderBg(MatrixStack p_98413_, float p_98414_, int p_98415_, int p_98416_) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CONTAINER_BACKGROUND);
        int i = (this.width - this.imageWidth) / 2;
        int j = ((this.height - this.imageHeight) / 2) + 2;
        this.blit(p_98413_, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(MatrixStack matrix, int p_230451_2_, int p_230451_3_) {
        this.font.draw(matrix, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(matrix, this.inventory.getDisplayName(), (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);

        // show armor points
        double armor = 0.0;
        if (entity.getAttribute(Attributes.ARMOR) != null) {
            armor = entity.getAttribute(Attributes.ARMOR).getValue();
        }
        StringTextComponent armorPoints = new StringTextComponent("Armor: " + armor);
        this.font.draw(matrix, armorPoints, this.sidebarX, this.titleLabelY + 20, 4210752);
    }

    @Override
    protected void renderTooltip(MatrixStack stack, int x, int y) {
        super.renderTooltip(stack, x, y);

        // render tooltips for base gear
        if (isHovering(25, 31, x, y)) {
            this.renderTooltip(stack, baseGear[0], x, y);
        }
        if (isHovering(25, 49, x, y)) {
            this.renderTooltip(stack, baseGear[1], x, y);
        }
        if (isHovering(25, 67, x, y)) {
            this.renderTooltip(stack, baseGear[2], x, y);
        }
        if (isHovering(25, 85, x, y)) {
            this.renderTooltip(stack, baseGear[3], x, y);
        }
        if (isHovering(62 + 18, 67, x, y)) {
            this.renderTooltip(stack, baseGear[4], x, y);
        }
        if (isHovering(62 + 18, 85, x, y)) {
            this.renderTooltip(stack, baseGear[5], x, y);
        }
    }

    private void openMainMenu() {
        PacketHandler.sendToServer(new CompanionsClientOpenMenuPacket(menu.getEntityId()));
    }

    private void renderBaseGearSlot(int slotX, int slotY, ItemStack item) {
        String itemStackCountText = null;

        // Set rendering offsets
        this.setBlitOffset(100);
        this.itemRenderer.blitOffset = 100.0F;

        // Render the item in the slot
        RenderSystem.enableDepthTest();
        this.itemRenderer.renderAndDecorateItem(this.minecraft.player, item, slotX, slotY);
        this.itemRenderer.renderGuiItemDecorations(this.font, item, slotX, slotY, itemStackCountText);

        // Reset rendering offsets
        this.itemRenderer.blitOffset = 0.0F;
        this.setBlitOffset(0);
    }

    private boolean isHovering(int hoverX, int hoverY, double mouseX, double mouseY) {
        // Adjust the mouse coordinates based on the position of the component
        double adjustedMouseX = mouseX - this.leftPos;
        double adjustedMouseY = mouseY - this.topPos;

        // Check if the adjusted mouse coordinates are within the bounds of the component
        boolean isWithinXBounds = adjustedMouseX >= (hoverX - 1) && adjustedMouseX < (hoverX + 16 + 1);
        boolean isWithinYBounds = adjustedMouseY >= (hoverY - 1) && adjustedMouseY < (hoverY + 16 + 1);

        return isWithinXBounds && isWithinYBounds;
    }

}
