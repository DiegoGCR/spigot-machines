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
import org.bukkit.inventory.meta.ItemMeta;

import java.io.*;
import java.util.Collections;
import java.util.Map;

import static me.ammonium.spigotmachinery.SpigotMachinery.mfList;


class MechanicalFurnace extends SpigotMachine {

    @Override
    boolean processInput(ItemStack input, Location inputLoc) {
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
            Hopper inputHop = (Hopper) inputLoc.getBlock().getState();
            Inventory inputInv = inputHop.getInventory();
            inputInv.removeItem(input);
            return true;
        } else {
            return false;
        }

    }
}

class BlastFurnace extends SpigotMachine {
    private int ironCount;
    private ItemStack steel;
    public BlastFurnace() {
        ironCount = 0;
        steel = new ItemStack(Material.IRON_INGOT);
        ItemMeta steelMeta = steel.getItemMeta();
        steelMeta.setDisplayName(ChatColor.GRAY + "Steel Ingot");
        steelMeta.setLore(Collections.singletonList(ChatColor.AQUA + "SpigotMachinery Ingredient"));
        steel.setItemMeta(steelMeta);
    }

    @Override
    boolean processInput(ItemStack input, Location inputLoc) {
        boolean result = false;
        // Add iron to ironCount
        if (input.getType().equals(Material.IRON_INGOT) && fuelRemaining > 0) {
            result = true;
            Hopper inputHop = (Hopper) inputLoc.getBlock().getState();
            Inventory inputInv = inputHop.getInventory();
            inputInv.removeItem(input);
            ironCount += input.getAmount();
            fuelRemaining--;

            // If theres 5+ iron, make steel and decrease ironCount
            if (ironCount >= 5) {
                Hopper outputHop = (Hopper) Location.deserialize(outputHopperLoc).getBlock().getState();
                Inventory output = outputHop.getInventory();
                output.addItem(steel);
                ironCount -= 5;
            }
        } else {
            System.out.println(input.getType());

        }
        return result;
    }

}

// TODO: Add recipes and selection GUI to Assembler
class Assembler extends SpigotMachine {
    Map<String, Object> input2HopperLoc;
    Map<String, Object> input3HopperLoc;
    int steelCount = 0;

    void setInput2HopperLoc(Location inputHopperLoc) {
        this.input2HopperLoc = inputHopperLoc.serialize();
    }
    void setInput3HopperLoc(Location inputHopperLoc) {
        this.input3HopperLoc = inputHopperLoc.serialize();
    }

    Location getInput2HopperLoc() {
        return Location.deserialize(input2HopperLoc);
    }
    Location getInput3HopperLoc() {
        return Location.deserialize(input3HopperLoc);
    }

    @Override
    boolean processInput(ItemStack input, Location inputLoc) {
        boolean result = false;
        try{
            if(input.getItemMeta().getDisplayName().equals(ChatColor.GRAY + "Steel Ingot")) {
                result = true;
                steelCount += input.getAmount();
                Hopper inputHop = (Hopper) inputLoc.getBlock().getState();
                Inventory inputInv = inputHop.getInventory();
                inputInv.removeItem(input);
            }

        } catch (NullPointerException ignored) {
            // Ignored. Reason: doesn't happen with custom items
        }
        return result;
    }
}


public final class EventListener implements Listener {
    private File mf_file = new File("plugins/SpigotMachinery/mf.schematic");
    private File bf_file = new File("plugins/SpigotMachinery/bf.schematic");
    private File asm_file = new File("plugins/SpigotMachinery/basm.schematic");

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e) {
        Inventory source = e.getDestination();
        String actionType = null;

        if (source.getType().equals(InventoryType.HOPPER) && mfList.size() > 0){
            SpigotMachine mySM = null;
            Location inputLoc = null;
            for (SpigotMachine spigotMachine : mfList) {
                Location tempinput = spigotMachine.getInputHopperLoc();
                Location tempFuel = spigotMachine.getFuelHopperLoc();
                if (source.getLocation().equals(tempinput)) {
                    mySM = spigotMachine;
                    inputLoc = tempinput;
                    actionType = "input";
                    break;
                } else if (source.getLocation().equals(tempFuel)) {
                    mySM = spigotMachine;
                    actionType = "fuel";
                    break;
                }
            }
            if (mySM != null) {
                switch (actionType){
                    case "input":
                        boolean inputResult = mySM.processInput(e.getItem(), inputLoc);
                        e.setCancelled(!inputResult);
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
                    player.getInventory().removeItem(item);

                    // Paste MF schematic
                    WorldData worldData = world.getWorldData();
                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(mf_file)).read(worldData);
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
                else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Blast Furnace")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Blast Furnace...");
                    player.getInventory().removeItem(item);

                    // Paste MF schematic
                    WorldData worldData = world.getWorldData();
                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(bf_file)).read(worldData);
                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                    AffineTransform transform = new AffineTransform();
                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
                    if (!transform.isIdentity()) copy.setTransform(transform);
                    copy.setSourceMask(new ExistingBlockMask(clipboard));
                    Operations.completeLegacy(copy);
                    extent.flushQueue();

                    // Get location of inputHopper, outputHopper, and fuelHopper
                    Location inputHopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location outputHopper = new Location(player.getWorld(), (loc.getBlockX() + 5), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location fuelHopper = new Location(player.getWorld(), (loc.getBlockX()), (loc.getBlockY() + 2), (loc.getBlockZ() - 5));
                    Location machineBottom = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY()), (loc.getBlockZ() - 3));
                    Location machineTop = new Location(player.getWorld(), (loc.getBlockX() + 5), (loc.getBlockY() + 3), (loc.getBlockZ() - 5));
                    Location coreBlock = new Location(player.getWorld(), (loc.getBlockX()), (loc.getBlockY() + 1), (loc.getBlockZ() - 3));
                    BlastFurnace temp = new BlastFurnace();
                    temp.setFuelHopperLoc(fuelHopper);
                    temp.setInputHopperLoc(inputHopper);
                    temp.setOutputHopperLoc(outputHopper);
                    temp.setMachineBottom(machineBottom);
                    temp.setMachineTop(machineTop);
                    temp.setCoreBlock(coreBlock);
                    mfList.add(temp);

                }
                else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Assenvbler")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Blast Furnace...");
                    player.getInventory().removeItem(item);

                    // Paste MF schematic
                    WorldData worldData = world.getWorldData();
                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(bf_file)).read(worldData);
                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
                    AffineTransform transform = new AffineTransform();
                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
                    if (!transform.isIdentity()) copy.setTransform(transform);
                    copy.setSourceMask(new ExistingBlockMask(clipboard));
                    Operations.completeLegacy(copy);
                    extent.flushQueue();

                    // Get location of inputHopper, outputHopper, and fuelHopper
                    Location inputHopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location input2Hopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 6));
                    Location input3Hopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 8));
                    Location outputHopper = new Location(player.getWorld(), (loc.getBlockX() + 5), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location fuelHopper = new Location(player.getWorld(), (loc.getBlockX() - 1), (loc.getBlockY() + 3), (loc.getBlockZ() - 3));
                    Location machineBottom = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY()), (loc.getBlockZ() - 3));
                    Location machineTop = new Location(player.getWorld(), (loc.getBlockX() + 7), (loc.getBlockY() + 3), (loc.getBlockZ() - 9));
                    Location coreBlock = new Location(player.getWorld(), (loc.getBlockX()), (loc.getBlockY() + 2), (loc.getBlockZ() - 3));
                    Assembler temp = new Assembler();
                    temp.setFuelHopperLoc(fuelHopper);
                    temp.setInputHopperLoc(inputHopper);
                    temp.setInput2HopperLoc(input2Hopper);
                    temp.setInput3HopperLoc(input3Hopper);
                    temp.setOutputHopperLoc(outputHopper);
                    temp.setMachineBottom(machineBottom);
                    temp.setMachineTop(machineTop);
                    temp.setCoreBlock(coreBlock);
                    mfList.add(temp);

                }
            } catch (NullPointerException ignored) {
                // Ignored. Reason: doesn't happen with custom items
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

