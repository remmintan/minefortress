package net.remmintan.mods.minefortress.core.interfaces.entities.pawns;

public interface IProfessional {

    String getProfessionId();
    void resetProfession();

    default void reserve() {
    }

}
