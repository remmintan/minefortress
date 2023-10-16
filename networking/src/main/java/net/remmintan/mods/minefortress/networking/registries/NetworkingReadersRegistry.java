package net.remmintan.mods.minefortress.networking.registries;

import net.remmintan.mods.minefortress.core.interfaces.networking.INetworkingReader;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkingReadersRegistry {

    public static HashSet<INetworkingReader<?>> registeredReaders =
            new HashSet<>(new ConcurrentHashMap<INetworkingReader<?>, Objects>().keySet());

    public static void registerReader(INetworkingReader<?> reader) {
        registeredReaders.add(reader);
    }

    @SuppressWarnings("unchecked")
    public static <T> INetworkingReader<T> findReader(Class<T> typeToRead) {
        for (INetworkingReader<?> reader : registeredReaders) {
            if (reader.canReadForType(typeToRead)) {
                return (INetworkingReader<T>)reader;
            }
        }

        throw new RuntimeException("No reader found for type " + typeToRead.getName());
    }

}
