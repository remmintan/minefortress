package org.minefortress.entity.renderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.remmintan.mods.minefortress.core.FortressGamemodeUtilsKt;
import net.remmintan.mods.minefortress.core.FortressState;
import net.remmintan.mods.minefortress.core.dtos.PawnSkin;
import net.remmintan.mods.minefortress.core.dtos.buildings.BarColor;
import net.remmintan.mods.minefortress.core.dtos.buildings.HudBar;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IProfessional;
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IWarrior;
import net.remmintan.mods.minefortress.core.utils.ClientModUtils;
import net.remmintan.mods.minefortress.core.utils.camera.CameraTools;
import org.jetbrains.annotations.NotNull;
import org.minefortress.entity.BasePawnEntity;
import org.minefortress.entity.renderer.models.PawnModel;

import java.util.List;

public class PawnRenderer extends BipedEntityRenderer<BasePawnEntity, PawnModel> {

    private static final Identifier GUY = new Identifier("minefortress", "textures/skins/guy.png");
    private static final Identifier GUY2 = new Identifier("minefortress", "textures/skins/guy2.png");
    private static final Identifier GUY3 = new Identifier("minefortress", "textures/skins/guy3.png");
    private static final Identifier GUY4 = new Identifier("minefortress", "textures/skins/guy4.png");
    private static final Identifier GUY_ZOMBIE = new Identifier("minefortress", "textures/skins/guy_zombie.png");

    public PawnRenderer(EntityRendererFactory.Context context) {
        super(context, new PawnModel(context), 0.5f);
        this.addFeature(new VillagerHeadFeatureRenderer(this, context));
        this.addFeature(new PawnClothesFeature(this));
        this.addFeature(new VillagerHeadwearFeatureRenderer(this, context));
    }

    @Override
    public Identifier getTexture(BasePawnEntity pawn) {
        final var pawnSkin = pawn.getPawnSkin();
        if (pawnSkin == PawnSkin.ZOMBIE || pawnSkin == PawnSkin.ZOMBIE_VILLAGER) {
            return GUY_ZOMBIE;
        }


        final var bodyTextureId = pawn.getBodyTextureId();
        return switch (bodyTextureId) {
            case 0 -> GUY;
            case 1 -> GUY2;
            case 2 -> GUY3;
            default -> GUY4;
        };
    }

    @Override
    protected boolean hasLabel(BasePawnEntity colonist) {
        return colonist.hasCustomName();
    }


    @NotNull
    private static BarColor getColorBaseOnMode(BasePawnEntity pawn) {
        final var state = ClientModUtils.getManagersProvider().get_ClientFortressManager().getState();
        final boolean warrior = pawn instanceof IWarrior;
        final var combatState = state == FortressState.COMBAT;
        if (combatState && warrior || !combatState && !warrior)
            return BarColor.GREEN;
        else
            return BarColor.YELLOW;
    }

    private boolean isThisPawnSelected(BasePawnEntity pawn) {
        return ClientModUtils.getManagersProvider().get_PawnsSelectionManager().isSelected(pawn);
    }

    private float getHealthFoodLevel(BasePawnEntity colonist) {
        final var health = colonist.getHealth();
        final var foodLevel = colonist.getCurrentFoodLevel();

        return Math.min(health, foodLevel);
    }

    @Override
    public void render(BasePawnEntity pawn, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        setClothesVilibility(pawn);

        final var pawnSkin = pawn.getPawnSkin();
        final var shouldRenderHead = pawnSkin == PawnSkin.STEVE || pawnSkin == PawnSkin.ZOMBIE;
        getModel().head.visible = shouldRenderHead;
        getModel().hat.visible = shouldRenderHead;

        super.render(pawn, f, g, matrixStack, vertexConsumerProvider, i);

        final MinecraftClient client = getClient();
        if (FortressGamemodeUtilsKt.isClientInFortressGamemode()) {
            final boolean hovering = client.crosshairTarget instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() == pawn;
            final var selected = isThisPawnSelected(pawn);
            var shouldShowBasedOnHealth = shouldShowBarsBasedOnHealth(pawn);
            if (hovering || shouldShowBasedOnHealth || selected) {
                final var pawnEyesPos = pawn.getEyePos();

                final var pawnScreenPos = CameraTools.projectToScreenSpace(pawnEyesPos, client);
                final var distance = client.getCameraEntity().getPos().distanceTo(pawnEyesPos);

                ItemStack profIcon = null;
                if ((hovering || selected) && pawn instanceof IProfessional prof) {
                    final var professionId = prof.getProfessionId();
                    if (!professionId.startsWith("warrior") || !professionId.startsWith("archer")) {
                        final var pawnProf = ClientModUtils.getProfessionManager().getProfession(professionId);
                        profIcon = pawnProf.getIcon();
                    }
                }

                final var health = pawn.getHealth();
                final var healthColor = shouldShowBasedOnHealth ? getHealthColor(pawn) : getColorBaseOnMode(pawn);
                final var healthBar = new HudBar(0, health / 20f, healthColor);

                final var foodLevel = pawn.getCurrentFoodLevel();
                final var foodColor = BarColor.GRAY;
                final var foodBar = new HudBar(1, foodLevel / 20f, foodColor);


                PawnDataHudRenderer.INSTANCE.addPawnData(
                        pawnScreenPos,
                        distance,
                        List.of(healthBar, foodBar),
                        profIcon
                );
            }
        }
    }

    private boolean shouldShowBarsBasedOnHealth(BasePawnEntity pawn) {
        final var healthFoodLevel = getHealthFoodLevel(pawn);

        return healthFoodLevel <= 10;
    }

    private BarColor getHealthColor(BasePawnEntity pawn) {
        final var health = pawn.getHealth();
        if (health < 5) return BarColor.RED;
        if (health < 10) return BarColor.YELLOW;
        return BarColor.GREEN;
    }

    private MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    private void setClothesVilibility(MobEntity colonist) {
        final var colonistModel = (PlayerEntityModel<BasePawnEntity>) this.getModel();
        colonistModel.hat.visible = true;
        colonistModel.jacket.visible = !colonist.isSleeping();
        colonistModel.leftPants.visible = !colonist.isSleeping();
        colonistModel.rightPants.visible = !colonist.isSleeping();
        colonistModel.leftSleeve.visible = !colonist.isSleeping();
        colonistModel.rightSleeve.visible = !colonist.isSleeping();
    }

}