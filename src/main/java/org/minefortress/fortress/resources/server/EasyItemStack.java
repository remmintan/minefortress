package org.minefortress.fortress.resources.server;

class EasyItemStack {

    private int amount = 0;

    void decrease() {
        amount--;
        if (amount < 0) {
            amount = 0;
        }
    }

    int getAmount() {
        return amount;
    }

    void increaseBy(int amount) {
        this.amount += amount;
    }

    void decreaseBy(int amount) {
        this.amount -= amount;
        if (this.amount < 0) {
            this.amount = 0;
        }
    }

    void setAmount(int amount) {
        this.amount = amount;
    }

    boolean hasEnough(int amount) {
        return this.amount >= amount;
    }

}
