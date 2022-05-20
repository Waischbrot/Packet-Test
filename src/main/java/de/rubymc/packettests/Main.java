package de.rubymc.packettests;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import de.rubymc.packettests.schedulers.RepeatingTask;
import de.rubymc.packettests.utils.EntityIdProvider;
import de.rubymc.packettests.utils.MessageUtil;
import de.rubymc.packettests.utils.ValueUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class Main extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private EntityIdProvider entityIdProvider;

    @Override
    public void onEnable() {

        protocolManager = ProtocolLibrary.getProtocolManager();

        entityIdProvider = new EntityIdProvider(1000000, 2000000);

        Bukkit.getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();

        int entity = entityIdProvider.getNewEntityId();

        location.setX(location.getX() + 0.5);
        location.setY(location.getY());
        location.setZ(location.getZ() + 0.5);

        createNew(entity, player, location);

        double exp = ValueUtil.getRandomDouble(2.00, 4.50);
        exp = ValueUtil.round(exp, 2);

        if (exp < 2.5) {
            applyMeta(entity, player, MessageUtil.getMessageColour("#66ff4d⛏ " + exp + " EXP"));
        } else if (exp < 3) {
            applyMeta(entity, player, MessageUtil.getMessageColour("#50ff3b⛏ " + exp + " EXP"));
        } else if (exp < 3.5) {
            applyMeta(entity, player, MessageUtil.getMessageColour("#36ff26⛏ " + exp + " EXP"));
        } else if (exp < 4) {
            applyMeta(entity, player, MessageUtil.getMessageColour("#00ff00⛏ " + exp + " EXP"));
        } else {
            applyMeta(entity, player, MessageUtil.getMessageColour("#00f000⛏ " + exp + " EXP"));
        }

        Location newLocation = location;
        newLocation.setY(newLocation.getY() + 1.5);

        RepeatingTask repeatingTask = new RepeatingTask(this, 1, 1) {

            int count = 0;

            @Override
            public void run() {

                if (count >= 25) {
                    removeEntity(entity, player);
                    cancel();
                }

                smoothMove(entity, player);

                count++;
            }
        };



        smoothMove(entity, player);
    }

    private void createNew(int entity, Player player, Location location) {

        PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawnPacket.getIntegers()
                .write(0, entity)
                .write(1, 0);
        spawnPacket.getEntityTypeModifier()
                .write(0, EntityType.AREA_EFFECT_CLOUD);
        spawnPacket.getUUIDs()
                .write(0, UUID.randomUUID());
        spawnPacket.getDoubles()
                .write(0, location.getX())
                .write(1, location.getY())
                .write(2, location.getZ());

        try {
            protocolManager.sendServerPacket(player, spawnPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private void applyMeta(int entity, Player player, String text) {
        PacketContainer metaPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        WrappedDataWatcher metadata = new WrappedDataWatcher();

        //set custom name
        Optional<?> opt = Optional.of(
                WrappedChatComponent.fromChatMessage(text)
                        [0].getHandle());
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), opt);

        //custom name visible
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);

        //set visibility
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.get(Float.class)), 0F);

        //set silent
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), true);

        metaPacket.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());
        metaPacket.getIntegers().write(0, entity);

        try {
            protocolManager.sendServerPacket(player, metaPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void removeEntity(int entity, Player player) {

        List<Integer> entities = new ArrayList<>();
        entities.add(entity);

        removeEntities(entities, player);

    }

    private void removeEntities(List<Integer> entities, Player player) {

        PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists()
                .write(0, entities);

        try {
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void smoothMove(int entity, Player player) {

        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE);
        packetContainer.getIntegers().write(0, entity);
        packetContainer.getShorts().write(0 , (short) 0);
        packetContainer.getShorts().write(1 , (short) (150));
        packetContainer.getShorts().write(2 , (short)0);

        try {
            protocolManager.sendServerPacket(player, packetContainer);
            protocolManager.broadcastServerPacket(packetContainer);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
