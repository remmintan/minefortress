package net.remmintan.mods.minefortress.core.interfaces.automation;

import net.minecraft.network.PacketByteBuf;
import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

public interface IAutomationAreaReader extends INetworkingReader<IAutomationAreaInfo> {

    IAutomationAreaInfo readBuffer(PacketByteBuf buf);

}
