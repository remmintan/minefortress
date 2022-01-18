package org.minefortress.village;


public class ColonistsManager {

    private int colonistsCount = 0;

    public void addColonist() {
        colonistsCount++;
    }

    public void removeColonist() {
        colonistsCount--;
    }

    public int getColonistsCount() {
        return colonistsCount;
    }

}
