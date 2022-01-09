package com.rageps.content.itemBoxes.impl;

import com.rageps.content.itemBoxes.BoxLoot;
import com.rageps.content.itemBoxes.ItemBox;
import com.rageps.content.itemBoxes.ItemBoxLootParser;
import com.rageps.util.rand.Chance;
import com.rageps.world.entity.actor.player.Player;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.stream.Collectors;

public class OblivionBox implements ItemBox {

    private ObjectArrayList<BoxLoot> loot;
    private ObjectArrayList<Chance> chances;

    public OblivionBox() {
        loot = new ItemBoxLootParser("oblivion.json").getLoot();
        chances = loot.stream().map(BoxLoot::getChance).distinct().collect(Collectors.toCollection(ObjectArrayList::new));
    }

    @Override
    public int freeSlotsRequired() {
        return 2;
    }

    @Override
    public int lootCount(Player player) {
        return 1;
    }

    @Override
    public ObjectArrayList<Chance> getChances() {
        return chances;
    }

    @Override
    public ObjectArrayList<BoxLoot> getItems() {
        return loot;
    }

    @Override
    public String name() {
        return "Oblivion Box";
    }
}
