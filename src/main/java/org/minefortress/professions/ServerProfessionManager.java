package org.minefortress.professions;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.minefortress.fortress.AbstractFortressManager;
import org.minefortress.network.ClientboundProfessionSyncPacket;
import org.minefortress.network.helpers.FortressChannelNames;
import org.minefortress.network.helpers.FortressServerNetworkHelper;

import java.util.function.Supplier;

public class ServerProfessionManager extends ProfessionManager{

    private boolean needsUpdate = false;

    public ServerProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    @Override
    public void increaseAmount(String professionId) {
        if(super.getFreeColonists() <= 0) return;
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(!super.isRequirementsFulfilled(profession)) return;

        profession.setAmount(profession.getAmount() + 1);
        scheduleSync();
    }

    @Override
    public void decreaseAmount(String professionId) {
        final Profession profession = super.getProfession(professionId);
        if(profession == null) return;
        if(profession.getAmount() <= 0) return;

        profession.setAmount(profession.getAmount() - 1);
        scheduleSync();
    }

    public void tick(ServerPlayerEntity player) {
        if(needsUpdate) {
            ClientboundProfessionSyncPacket packet = new ClientboundProfessionSyncPacket(this.professions);
            FortressServerNetworkHelper.send(player, FortressChannelNames.FORTRESS_PROFESSION_SYNC, packet);
            needsUpdate = false;
        }
    }

    public void scheduleSync() {
        needsUpdate = true;
    }

    public void writeToNbt(NbtCompound tag){
        professions.forEach((key, value) -> tag.put(key, value.toNbt()));
    }

    public void readFromNbt(NbtCompound tag){
        for(String key : tag.getKeys()){
            final Profession profession = super.getProfession(key);
            if(profession == null) continue;
            profession.readNbt(tag.getCompound(key));
            scheduleSync();
        }
    }

}
