package org.minefortress;

import net.remmintan.mods.minefortress.networking.registries.NetworkingReadersRegistry;
import org.minefortress.fortress.automation.areas.AutomationAreaInfoReader;

public class NetworkReaders {

    public static void register() {
        NetworkingReadersRegistry.addReader(new AutomationAreaInfoReader());
    }

}
