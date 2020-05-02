package org.hancho.plugin.nukkit.doorlock;

import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.util.HashSet;
import java.util.LinkedHashMap;

public class DoorLock extends PluginBase implements Listener {
    public HashSet<Integer> doorList;
    public HashSet<String> queue = new HashSet<>();
    public LinkedHashMap<String, Object> lockedDoor;

    @Override
    public void onEnable() {
        Config config = this.getConfig();
        this.lockedDoor = (LinkedHashMap<String, Object>) config.getAll();
        this.doorList = new HashSet<Integer>(){{
            add(Block.DOOR_BLOCK );
            add(Block.ACACIA_DOOR_BLOCK );
            add(Block.SPRUCE_DOOR_BLOCK);
            add(Block.BIRCH_DOOR_BLOCK);
            add(Block.JUNGLE_DOOR_BLOCK);
            add(Block.DARK_OAK_DOOR_BLOCK);
            add(Block.IRON_DOOR_BLOCK);
        }};
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Config config = this.getConfig();
        config.setAll(this.lockedDoor);
        config.save();
    }

    public static String getKey(Block block) {
        String locationString;
        if(block.getDamage() != 8 && block.getDamage() != 9) {
            locationString = block.getLevel().getName() + ":" + block.getLocationHash();
        }else {
            Location location = block.getLocation();
            locationString = new StringBuilder()
                    .append(block.getLevel().getName())
                    .append(":")
                    .append(location.getFloorX())
                    .append(":")
                    .append(location.getFloorY() - 1)
                    .append(":")
                    .append(location.getFloorZ()).toString();
        }
        return locationString;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent ev){
        if(!this.queue.contains(ev.getPlayer().getName())) return;
        Block block = ev.getBlock();
        if(this.doorList.contains(block.getId())){
            String key = this.getKey(ev.getBlock());
            this.lockedDoor.put(key, ev.getPlayer().getName());
            ev.getPlayer().sendMessage("§f[ §g! §f] 문이 잠궈졌습니다.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInt(PlayerInteractEvent ev){
        if(ev.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        if(ev.getPlayer().isOp()) return;
        /*public static int DOOR_OPEN_BIT = 4;
        public static int DOOR_TOP_BIT = 8;
        public static int DOOR_HINGE_BIT = 1;
        public static int DOOR_POWERED_BIT = 2;*/
        Block block = ev.getBlock();
        if(!this.doorList.contains(block.getId())) return;
        String locationString = this.getKey(block);
        if(!this.lockedDoor.containsKey(locationString)) return;
        String owner = (String) this.lockedDoor.get(locationString);
        if(!ev.getPlayer().getName().equals(owner)) {
            ev.getPlayer().sendMessage("§f[ §c! §f] 문과 상호작용할 권한이 없습니다. 소유자 :§d "  + owner);
            ev.setCancelled();
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent ev){
        if(!ev.getPlayer().isOp()) return;
        Block block = ev.getBlock();
        if(!this.doorList.contains(block.getId())) return;
        String locationString = this.getKey(block);
        this.lockedDoor.remove(locationString);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equals("문잠금")) {
            if(this.queue.contains(sender.getName())){
                this.queue.remove(sender.getName());
                sender.sendMessage("§f[ §g! §f] 문 잠금설정이 중단되었습니다.");
                return true;
            }
            this.queue.add(sender.getName());
            sender.sendMessage("§f[ §g! §f] 문 잠금설정이 시작되었습니다. 문을 설치하면 잠궈집니다.");
            return true;
        }
        return true;
    }

}