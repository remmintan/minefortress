package org.minefortress.fortress.resources.server;

import net.minecraft.item.Item;

class EasyItemStack {

    private final Item item;

    EasyItemStack(Item item) {
        this.item = item;
    }

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
        if(this.amount  < amount) throw new RuntimeException("Tried to decrease by more than the amount of items in the stack");
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

    Item getItem() {
        return item;
    }

}
