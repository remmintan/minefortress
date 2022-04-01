package org.minefortress.professions;

import org.minefortress.fortress.AbstractFortressManager;

import java.util.function.Supplier;

public class ServerProfessionManager extends ProfessionManager{

    public ServerProfessionManager(Supplier<AbstractFortressManager> fortressManagerSupplier) {
        super(fortressManagerSupplier);
    }

    @Override
    public void increaseAmount(String professionId) {
        if(super.getFreeColonists() <= 0) return;
    }

    @Override
    public void decreaseAmount(String professionId) {

    }
}
