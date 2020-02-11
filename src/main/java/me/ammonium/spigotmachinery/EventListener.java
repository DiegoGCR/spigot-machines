package me.ammonium.spigotmachinery;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.WorldData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.*;

import static me.ammonium.spigotmachinery.SpigotMachinery.mfList;


class MechanicalFurnace extends SpigotMachine {

    @Override
    boolean processInput(ItemStack input) {
        // Check if input has a product
        Material product = null;
        int amount = 0;
        if (input.getType().equals(Material.IRON_ORE)) {
            product = Material.IRON_INGOT;
            amount = 2;
        } else if (input.getType().equals(Material.GOLD_ORE)) {
            product = Material.GOLD_INGOT;
            amount = 2;
        } else if (input.getType().equals(Material.COBBLESTONE)) {
            product = Material.STONE;
            amount = 1;
        } else if (input.getType().equals(Material.REDSTONE)) {
            product = Material.OBSIDIAN;
            amount = 1;
        }

        // Consume input, decrease fuel, and output product
        if (product != null && fuelRemaining > 0) {
            fuelRemaining--;
            Hopper outputHop = (Hopper) Location.deserialize(outputHopperLoc).getBlock().getState();
            Inventory output = outputHop.getInventory();
            output.addItem(new ItemStack(product, amount));
            Hopper inputHop = (Hopper) Location.deserialize(inputHopperLoc).getBlock().getState();
            Inventory inputInv = inputHop.getInventory();
            inputInv.removeItem(input);
            return true;
        } else {
            return false;
        }

    }
}


public final class EventListener implements Listener {
    private File file = new File("plugins/SpigotMachinery/mf.schematic");

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e) {
        Inventory source = e.getDestination();
        String actionType = null;

        if (source.getType().equals(InventoryType.HOPPER) && mfList.size() > 0){
            SpigotMachine mySM = null;
            for (SpigotMachine mechanicalFurnace : mfList) {
                Location tempinput = mechanicalFurnace.getInputHopperLoc();
                Location tempFuel = mechanicalFurnace.getFuelHopperLoc();
                if (source.getLocation().equals(tempinput)) {
                    mySM = mechanicalFurnace;
                    actionType = "input";
                    break;
                } else if (source.getLocation().equals(tempFuel)) {
                    mySM = mechanicalFurnace;
                    actionType = "fuel";
                    break;
                }
            }
            if (mySM != null) {
                switch (actionType){
                    case "input":
                        e.setCancelled(!(mySM.processInput(e.getItem())));
                        break;
                    case "fuel":
                        e.setCancelled(!(mySM.processFuel(e.getItem())));
                        break;
                }



            }

        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) throws IOException, MaxChangedBlocksException {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        World world = new BukkitWorld(player.getWorld());
        Vector position = new Vector(loc.getX(), loc.getY(), loc.getZ());

        // Check if it is a SpigotMachine summoner
        if (e.hasItem()) {
            try {
                ItemStack item = e.getItem();
                if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mechanical Furnace")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Mechanical Furnace...");

                    try {
                        player.getInventory().removeItem(item);
                    } catch (IllegalArgumentException iae) {
                        System.out.println("An unexpected error occured while attempting to remove item:");
                        iae.printStackTrace();
                    }

                    // Paste MF schematic
                    WorldData worldData = world.getWorldData();
                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(file)).read(worldData);
                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                    AffineTransform transform = new AffineTransform();
                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
                    if (!transform.isIdentity()) copy.setTransform(transform);
                    copy.setSourceMask(new ExistingBlockMask(clipboard));
                    Operations.completeLegacy(copy);
                    extent.flushQueue();

                    // Get location of inputHopper, outputHopper, and fuelHopper
                    Location inputHopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location outputHopper = new Location(player.getWorld(), (loc.getBlockX() + 3), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location fuelHopper = new Location(player.getWorld(), (loc.getBlockX() - 1), (loc.getBlockY() + 5), (loc.getBlockZ() - 4));
                    Location machineBottom = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY()), (loc.getBlockZ() - 3));
                    Location machineTop = new Location(player.getWorld(), (loc.getBlockX() + 3), (loc.getBlockY() + 5), (loc.getBlockZ() - 5));
                    Location coreBlock = new Location(player.getWorld(), (loc.getBlockX()), (loc.getBlockY() + 1), (loc.getBlockZ() - 3));
                    MechanicalFurnace temp = new MechanicalFurnace();
                    temp.setFuelHopperLoc(fuelHopper);
                    temp.setInputHopperLoc(inputHopper);
                    temp.setOutputHopperLoc(outputHopper);
                    temp.setMachineBottom(machineBottom);
                    temp.setMachineTop(machineTop);
                    temp.setCoreBlock(coreBlock);
                    mfList.add(temp);
                }
            } catch (NullPointerException ignored) {

            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location breakLocation = e.getBlock().getLocation();
        if (mfList != null) {
            for (int i = 0; i < mfList.size(); i++) {
                Location coreBlock = mfList.get(i).getCoreBlock();
                if (breakLocation.equals(coreBlock)) {
                    mfList.remove(i);
                    e.getPlayer().sendMessage(ChatColor.AQUA + "This SM has been disabled");
                    break;
                } else if (breakLocation.getBlockX() >= mfList.get(i).getMachineBottom().getBlockX() && breakLocation.getBlockX() <= mfList.get(i)
                        .getMachineTop().getBlockX() &&
                        breakLocation.getBlockY() >= mfList.get(i).getMachineBottom().getBlockY() && breakLocation.getBlockY() <= mfList.get(i)
                        .getMachineTop().getBlockY() &&
                        breakLocation.getBlockZ() <= mfList.get(i).getMachineBottom().getBlockZ() && breakLocation.getBlockZ() >= mfList.get(i)
                        .getMachineTop().getBlockY()) {
                    e.getPlayer().sendMessage(ChatColor.AQUA + "You can't mine this block! Break the obsidian to disable this.");
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
}

