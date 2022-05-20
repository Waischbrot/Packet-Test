package de.rubymc.packettests.utils;

import org.bukkit.Bukkit;

public class EntityIdProvider {

    private final int start;
    private final int end;

    private int lastId;

    public EntityIdProvider(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getNewEntityId() {

        if (lastId >= end) {
            resetCustomIds();
        }

        lastId += 1;
        return lastId;

    }

    public void resetCustomIds() {
        lastId = start;
        Bukkit.getConsoleSender().sendMessage("custom entityUniqueId provider was reset!");
    }
}
