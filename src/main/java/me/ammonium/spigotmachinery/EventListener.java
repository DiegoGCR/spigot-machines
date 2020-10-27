package me.ammonium.spigotmachinery;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static me.ammonium.spigotmachinery.SpigotMachinery.smList;


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
    private final ItemStack steel;
    public BlastFurnace() {
        ironCount = 0;
        steel = new ItemStack(Material.IRON_INGOT);
        ItemMeta steelMeta = steel.getItemMeta();
        assert steelMeta != null;
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
        }
        return result;
    }

}

class Assembler extends SpigotMachine implements InventoryHolder {
    private Map<String, Object> input2HopperLoc;
    private Map<String, Object> input3HopperLoc;
    private final Inventory inv;
    private int steelCount = 0;
    private String recipe = null;


    void setInput2HopperLoc(Location inputHopperLoc) {
        this.input2HopperLoc = inputHopperLoc.serialize();
    }
    void setInput3HopperLoc(Location inputHopperLoc) {
        this.input3HopperLoc = inputHopperLoc.serialize();
    }
    void setRecipe(String recipe) {
        this.recipe = recipe;
    }
    void openInventory(Player p) {
        p.openInventory(inv);
    }

    Location getInput2HopperLoc() {
        return Location.deserialize(input2HopperLoc);
    }
    Location getInput3HopperLoc() {
        return Location.deserialize(input3HopperLoc);
    }

    public Assembler() throws NullPointerException {
        inv = Bukkit.createInventory(this, 9, "Select a recipe");
        ItemStack steelHelmetRecipe = new ItemStack(Material.IRON_HELMET);
        ItemMeta steelHelmetRecipeMeta = steelHelmetRecipe.getItemMeta();
        assert steelHelmetRecipeMeta != null;
        steelHelmetRecipeMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
        steelHelmetRecipeMeta.addEnchant(Enchantment.DURABILITY, 6, true);
        steelHelmetRecipeMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
        steelHelmetRecipeMeta.setDisplayName(ChatColor.GRAY + "Steel Helmet");
        steelHelmetRecipeMeta.setLore(Arrays.asList(ChatColor.GOLD + "Requires:", "5 steel ingots"));
        steelHelmetRecipe.setItemMeta(steelHelmetRecipeMeta);
        inv.addItem(steelHelmetRecipe);
        ItemStack steelChestplateRecipe = new ItemStack(Material.IRON_CHESTPLATE);
        ItemMeta steelChestplateRecipeMeta = steelChestplateRecipe.getItemMeta();
        assert steelChestplateRecipeMeta != null;
        steelChestplateRecipeMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
        steelChestplateRecipeMeta.addEnchant(Enchantment.DURABILITY, 6, true);
        steelChestplateRecipeMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
        steelChestplateRecipeMeta.setDisplayName(ChatColor.GRAY + "Steel Chestplate");
        steelChestplateRecipeMeta.setLore(Arrays.asList(ChatColor.GOLD + "Requires:", "8 steel ingots"));
        steelChestplateRecipe.setItemMeta(steelChestplateRecipeMeta);
        inv.addItem(steelChestplateRecipe);
        ItemStack steelLeggingsRecipe = new ItemStack(Material.IRON_LEGGINGS);
        ItemMeta steelLeggingsRecipeMeta = steelLeggingsRecipe.getItemMeta();
        steelLeggingsRecipeMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
        steelLeggingsRecipeMeta.addEnchant(Enchantment.DURABILITY, 6, true);
        steelLeggingsRecipeMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
        steelLeggingsRecipeMeta.setDisplayName(ChatColor.GRAY + "Steel Leggings");
        steelLeggingsRecipeMeta.setLore(Arrays.asList(ChatColor.GOLD + "Requires:", "7 steel ingots"));
        steelLeggingsRecipe.setItemMeta(steelLeggingsRecipeMeta);
        inv.addItem(steelLeggingsRecipe);
        ItemStack steelBootsRecipe = new ItemStack(Material.IRON_BOOTS);
        ItemMeta steelBootsRecipeMeta = steelBootsRecipe.getItemMeta();
        steelBootsRecipeMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
        steelBootsRecipeMeta.addEnchant(Enchantment.DURABILITY, 6, true);
        steelBootsRecipeMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
        steelBootsRecipeMeta.setDisplayName(ChatColor.GRAY + "Steel Boots");
        steelBootsRecipeMeta.setLore(Arrays.asList(ChatColor.GOLD + "Requires:", "4 steel ingots"));
        steelBootsRecipe.setItemMeta(steelBootsRecipeMeta);
        inv.addItem(steelBootsRecipe);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    @Override
    boolean processInput(ItemStack input, Location inputLoc) {
        boolean result = false;
        Hopper outputHop = (Hopper) Location.deserialize(outputHopperLoc).getBlock().getState();
        Inventory output = outputHop.getInventory();
        try{
            if(input.getItemMeta().getDisplayName().equals(ChatColor.GRAY + "Steel Ingot") && steelCount < 20) {
                result = true;
                steelCount += input.getAmount();
                Hopper inputHop = (Hopper) inputLoc.getBlock().getState();
                Inventory inputInv = inputHop.getInventory();
                inputInv.removeItem(input);
            }

        } catch (NullPointerException ignored) {
            // Ignored. Reason: doesn't happen with custom items
        }

        switch(recipe) {
            case "steel helmet":
                if (steelCount >= 5) {
                    ItemStack steelHelmet = new ItemStack(Material.IRON_HELMET);
                    ItemMeta steelHelmetMeta = steelHelmet.getItemMeta();
                    steelHelmetMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
                    steelHelmetMeta.addEnchant(Enchantment.DURABILITY, 6, true);
                    steelHelmetMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
                    steelHelmetMeta.setDisplayName(ChatColor.GRAY + "Steel Helmet");
                    steelHelmet.setItemMeta(steelHelmetMeta);
                    output.addItem(steelHelmet);
                    steelCount -= 5;

                }
                break;

            case "steel chestplate":
                if (steelCount >= 8) {
                    ItemStack steelChestplate = new ItemStack(Material.IRON_CHESTPLATE);
                    ItemMeta steelChestplateMeta = steelChestplate.getItemMeta();
                    steelChestplateMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
                    steelChestplateMeta.addEnchant(Enchantment.DURABILITY, 6, true);
                    steelChestplateMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
                    steelChestplateMeta.setDisplayName(ChatColor.GRAY + "Steel Chestplate");
                    steelChestplate.setItemMeta(steelChestplateMeta);
                    output.addItem(steelChestplate);
                    steelCount -= 5;
                }
                break;

            case "steel leggings":
                if(steelCount >= 7) {
                    ItemStack steelLeggings = new ItemStack(Material.IRON_LEGGINGS);
                    ItemMeta steelLeggingsMeta = steelLeggings.getItemMeta();
                    steelLeggingsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
                    steelLeggingsMeta.addEnchant(Enchantment.DURABILITY, 6, true);
                    steelLeggingsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
                    steelLeggingsMeta.setDisplayName(ChatColor.GRAY + "Steel Helmet");
                    steelLeggings.setItemMeta(steelLeggingsMeta);
                    output.addItem(steelLeggings);
                    steelCount -= 5;
                }
                break;

            case "steel boots":
                if(steelCount >= 4) {
                    ItemStack steelBoots = new ItemStack(Material.IRON_BOOTS);
                    ItemMeta steelBootsMeta = steelBoots.getItemMeta();
                    steelBootsMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 5, true);
                    steelBootsMeta.addEnchant(Enchantment.DURABILITY, 6, true);
                    steelBootsMeta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 5, true);
                    steelBootsMeta.setDisplayName(ChatColor.GRAY + "Steel Boots");
                    steelBoots.setItemMeta(steelBootsMeta);
                    output.addItem(steelBoots);
                    steelCount -= 5;
                }
                break;
        }
        return result;
    }
}


public final class EventListener implements Listener {
    private final File mfFile = new File("plugins/SpigotMachinery/mf.schematic");
    private final File bfFile = new File("plugins/SpigotMachinery/bf.schematic");
    private final File asmFile = new File("plugins/SpigotMachinery/asm.schematic");

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent e) {
        Inventory source = e.getDestination();
        String actionType = null;

        if (source.getType().equals(InventoryType.HOPPER) && smList.size() > 0){
            SpigotMachine mySM = null;
            Location inputLoc = null;
            for (SpigotMachine spigotMachine : smList) {
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
    public void onPlayerInteract(PlayerInteractEvent e) throws DataException, IOException, MaxChangedBlocksException {
        Player player = e.getPlayer();
        Location loc = player.getLocation();
        World world = new BukkitWorld(player.getWorld());
        Vector position = new Vector(loc.getX(), loc.getY(), loc.getZ());

        Inventory inventory = player.getInventory();

        // Check if it is a SpigotMachine summoner
        if (e.hasItem()) {
            try {
                ItemStack item = e.getItem();
                if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Mechanical Furnace")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Mechanical Furnace...");
                    ItemStack item2 = new ItemStack(item);
                    item2.setAmount(1);
                    inventory.removeItem(item2);


                    // Paste MF schematic
//                    WorldData worldData = world.getWorldData();
//                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(mfFile)).read(worldData);
//                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
//                    AffineTransform transform = new AffineTransform();
//                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
//                    if (!transform.isIdentity()) copy.setTransform(transform);
//                    copy.setSourceMask(new ExistingBlockMask(clipboard));
//                    Operations.completeLegacy(copy);
//                    extent.flushQueue();

                    EditSession editSession = Objects.requireNonNull(ClipboardFormats.findByFile(mfFile)).load(mfFile)
                            .paste(world, position);
//                    EditSession es = new EditSession(BukkitUtil.getLocalWorld(world), 10000);
//                    CuboidClipboard cc = CuboidClipboard.loadSchematic(mfFile);
//                    cc.paste(es, position, true);

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
                    smList.add(temp);
                }
                else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Blast Furnace")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Blast Furnace...");
                    ItemStack item2 = new ItemStack(item);
                    item2.setAmount(1);
                    inventory.removeItem(item2);

                    // Paste MF schematic
//                    WorldData worldData = world.getWorldData();
//                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(bfFile)).read(worldData);
//                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
//                    AffineTransform transform = new AffineTransform();
//                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
//                    if (!transform.isIdentity()) copy.setTransform(transform);
//                    copy.setSourceMask(new ExistingBlockMask(clipboard));
//                    Operations.completeLegacy(copy);
//                    extent.flushQueue();
                    EditSession editSession = Objects.requireNonNull(ClipboardFormats.findByFile(bfFile)).load(bfFile)
                            .paste(world, position);

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
                    smList.add(temp);

                }
                else if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Assembler")) {
                    e.getPlayer().sendMessage(ChatColor.DARK_BLUE + "Summoning Assembler...");
                    ItemStack item2 = new ItemStack(item);
                    item2.setAmount(1);
                    inventory.removeItem(item2);

                    // Paste MF schematic
//                    WorldData worldData = world.getWorldData();
//                    Clipboard clipboard = ClipboardFormat.SCHEMATIC.getReader(new FileInputStream(asmFile)).read(worldData);
//                    EditSession extent = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1);
//                    AffineTransform transform = new AffineTransform();
//                    ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), clipboard.getOrigin(), extent, position);
//                    if (!transform.isIdentity()) copy.setTransform(transform);
//                    copy.setSourceMask(new ExistingBlockMask(clipboard));
//                    Operations.completeLegacy(copy);
//                    extent.flushQueue();

                    EditSession editSession = Objects.requireNonNull(ClipboardFormats.findByFile(asmFile)).load(asmFile)
                            .paste(world, position);

                    // Get location of inputHopper, outputHopper, and fuelHopper
                    Location inputHopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location input2Hopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 6));
                    Location input3Hopper = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY() + 2), (loc.getBlockZ() - 8));
                    Location outputHopper = new Location(player.getWorld(), (loc.getBlockX() + 5), (loc.getBlockY() + 2), (loc.getBlockZ() - 4));
                    Location fuelHopper = new Location(player.getWorld(), (loc.getBlockX() - 1), (loc.getBlockY() + 3), (loc.getBlockZ() - 3));
                    Location machineBottom = new Location(player.getWorld(), (loc.getBlockX() - 2), (loc.getBlockY()), (loc.getBlockZ() - 3));
                    Location machineTop = new Location(player.getWorld(), (loc.getBlockX() + 7), (loc.getBlockY() + 4), (loc.getBlockZ() - 9));
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
                    smList.add(temp);

                }
            } catch (NullPointerException ignored) {
                // Ignored. Reason: doesn't happen with custom items
            }
        } else {

            Location clickedLoc = e.getClickedBlock().getLocation();
            for (SpigotMachine spigotMachine: smList) {
                if (spigotMachine instanceof Assembler && spigotMachine.getCoreBlock().equals(clickedLoc)) {
                    // Open selection GUI
                    System.out.println("Opening selection GUI for Assembler");
                    ((Assembler) spigotMachine).openInventory(e.getPlayer());
                    break;

                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Location breakLocation = e.getBlock().getLocation();
        if (smList != null) {
            for (int i = 0; i < smList.size(); i++) {
                Location coreBlock = smList.get(i).getCoreBlock();
                if (breakLocation.equals(coreBlock)) {
                    smList.remove(i);
                    e.getPlayer().sendMessage(ChatColor.AQUA + "This SM has been disabled");
                    break;
                } else if (breakLocation.getBlockX() >= smList.get(i).getMachineBottom().getBlockX() && breakLocation.getBlockX() <= smList.get(i)
                        .getMachineTop().getBlockX() &&
                        breakLocation.getBlockY() >= smList.get(i).getMachineBottom().getBlockY() && breakLocation.getBlockY() <= smList.get(i)
                        .getMachineTop().getBlockY() &&
                        breakLocation.getBlockZ() <= smList.get(i).getMachineBottom().getBlockZ() && breakLocation.getBlockZ() >= smList.get(i)
                        .getMachineTop().getBlockY()) {
                    e.getPlayer().sendMessage(ChatColor.AQUA + "You can't mine this block! Break the obsidian to disable this.");
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(e.getInventory().getHolder() instanceof Assembler) {
            Assembler assembler = (Assembler) e.getInventory().getHolder();
            int slot = e.getRawSlot();
            switch(slot) {
                case 0:
                    assembler.setRecipe("steel helmet");
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Set recipe to steel helmet");
                    break;

                case 1:
                    assembler.setRecipe("steel chestplate");
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Set recipe to steel chestplate");
                    break;

                case 2:
                    assembler.setRecipe("steel leggings");
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Set recipe to steel leggings");
                    break;

                case 3:
                    assembler.setRecipe("steel boots");
                    e.getWhoClicked().sendMessage(ChatColor.AQUA + "Set recipe to steel boots");
                    break;
            }
            e.setCancelled(true);
        }
    }
}
