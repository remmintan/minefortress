package org.minefortress.registries.events

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.event.player.AttackEntityCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.item.ItemGroups
import net.minecraft.util.ActionResult
import net.remmintan.mods.minefortress.core.FortressState
import net.remmintan.mods.minefortress.core.interfaces.entities.pawns.IFortressAwareEntity
import net.remmintan.mods.minefortress.core.interfaces.selections.ClickType
import net.remmintan.mods.minefortress.core.interfaces.tasks.ITasksInformationHolder
import net.remmintan.mods.minefortress.core.isClientInFortressGamemode
import net.remmintan.mods.minefortress.core.isFortressGamemode
import net.remmintan.mods.minefortress.core.utils.ClientModUtils
import net.remmintan.mods.minefortress.core.utils.getMineFortressVersion
import net.remmintan.mods.minefortress.networking.c2s.C2SClientReadyPacket
import net.remmintan.mods.minefortress.networking.helpers.FortressClientNetworkHelper
import org.minefortress.controls.MouseEvents
import org.minefortress.interfaces.IFortressMinecraftClient
import org.minefortress.registries.FortressKeybindings
import org.minefortress.registries.events.client.ToastEvents
import org.minefortress.utils.ModUtils


object FortressClientEvents {
    @JvmStatic
    fun registerEvents() {
        ToastEvents().register()
        InputTracker.initialize()

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> ClientModUtils.getFortressManager().reset() }
        ClientTickEvents.START_CLIENT_TICK.register { startClientTick(it) }
        ClientTickEvents.END_CLIENT_TICK.register { endClientTick(it) }
        ClientPlayConnectionEvents.JOIN.register { handler, _, client ->
            ItemGroups.updateDisplayContext(handler.enabledFeatures, false, client.world!!.registryManager)

            val version = FabricLoader.getInstance().getMineFortressVersion()
            FortressClientNetworkHelper.send(C2SClientReadyPacket.CHANNEL, C2SClientReadyPacket(version))
        }

        UseEntityCallback.EVENT.register { player, world, _, entity, _ ->
            if (!isFortressGamemode(player) || !world.isClient()) return@register ActionResult.PASS
            if (ClientModUtils.getFortressManager().state == FortressState.COMBAT) {
                val provider = ClientModUtils.getManagersProvider()
                val selectionManager = provider.targetedSelectionManager
                val fightManager = ClientModUtils.getFortressManager().fightManager

                fightManager.setTarget(entity, selectionManager)
            }
            ActionResult.FAIL
        }

        AttackEntityCallback.EVENT.register { player, _, _, entity, _ ->
            if (isFortressGamemode(player)) {
                if (entity is IFortressAwareEntity) {
                    val selectionManager = ClientModUtils.getManagersProvider()._PawnsSelectionManager
                    selectionManager.selectSingle(entity)
                }
                ActionResult.FAIL
            } else {
                ActionResult.PASS
            }
        }
    }

    private fun startClientTick(client: MinecraftClient) {
        if (!isClientInFortressGamemode()) return

        MouseEvents.checkMouseStateAndFireEvents()

        val mouse = client.mouse
        if (ModUtils.shouldReleaseCamera()) {
            if (!mouse.isCursorLocked) mouse.lockCursor()
        } else {
            if (mouse.isCursorLocked) mouse.unlockCursor()
        }

        val fortressClient = client as IFortressMinecraftClient
        fortressClient._FortressHud.tick()
        val provider = ClientModUtils.getManagersProvider()
        provider._ClientFortressManager.tick()
        provider._FortressCenterManager.tick()
    }

    private fun endClientTick(client: MinecraftClient) {
        val selectionManager = ClientModUtils.getSelectionManager()

        if (InputTracker.wasLeftMouseButtonPressedLastTick()) {
            if (selectionManager.clickType in listOf(ClickType.ROADS, ClickType.BUILD)) {
                selectionManager.resetSelection()
            }
        }

        if (InputTracker.wasRightMouseButtonPressedLastTick()) {
            if (selectionManager.clickType in listOf(ClickType.REMOVE)) {
                selectionManager.resetSelection()
            }
        }


        while (FortressKeybindings.switchSelectionKeybinding.wasPressed()) {
            selectionManager.toggleSelectionType()
        }

        while (FortressKeybindings.cancelTaskKeybinding.wasPressed()) {
            val world = client.world
            if (world != null) {
                val clientVisualTasksHolder = (world as ITasksInformationHolder)._ClientTasksHolder

                if (client.options.sprintKey.isPressed) {
                    clientVisualTasksHolder.cancelAllTasks()
                } else {
                    clientVisualTasksHolder.cancelLatestTask()
                }
            }
        }
    }
}
