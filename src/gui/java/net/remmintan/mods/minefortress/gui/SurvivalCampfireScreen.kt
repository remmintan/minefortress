package net.remmintan.mods.minefortress.gui

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.remmintan.mods.minefortress.networking.c2s.C2SSwitchToFortressModePacket
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper

class SurvivalCampfireScreen : Screen(Text.literal("Campfire Options")) {

    private var manageButton: ButtonWidget? = null

    override fun init() {
        super.init()
        manageButton = ButtonWidget.builder(Text.literal("Manage the village")) {
            // Send packet to server to switch mode and teleport
            val packet = C2SSwitchToFortressModePacket()
            FortressClientNetworkHelper.send(C2SSwitchToFortressModePacket.CHANNEL, packet)
            // Close the screen
            this.close()
        }
            .dimensions(this.width / 2 - 100, this.height / 2 - 10, 200, 20)
            .build()

        this.addDrawableChild(manageButton)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.renderBackground(context, mouseX, mouseY, delta)
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF)
        super.render(context, mouseX, mouseY, delta)
    }

    override fun shouldPause(): Boolean {
        return false
    }
}
