package me.ammonium.spigotmachinery;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class SpigotMachinery extends JavaPlugin {
    private File savedMachines = new File("plugins/SpigotMachinery/savedMachines.data");
    static List<SpigotMachine> smList = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        ItemStack mfSummoner = new ItemStack(Material.PAPER);
        ItemMeta mfSummonerItemMeta = mfSummoner.getItemMeta();
        mfSummonerItemMeta.setDisplayName(ChatColor.GOLD + "Mechanical Furnace");
        mfSummonerItemMeta.setLore(Collections.singletonList(ChatColor.AQUA + "Right click to place!"));
        mfSummoner.setItemMeta(mfSummonerItemMeta);

        NamespacedKey mfKey = new NamespacedKey(this, "mfSummoner");
        ShapedRecipe mfRecipe = new ShapedRecipe(mfKey, mfSummoner);
        mfRecipe.shape("IBI", "IPI", "IRI");
        mfRecipe.setIngredient('I', Material.IRON_INGOT);
        mfRecipe.setIngredient('P', Material.FURNACE);
        mfRecipe.setIngredient('B', Material.PISTON_BASE);
        mfRecipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(mfRecipe);

        ItemStack bfSummoner = new ItemStack(Material.PAPER);
        ItemMeta bfSummonerItemMeta = bfSummoner.getItemMeta();
        bfSummonerItemMeta.setDisplayName(ChatColor.GOLD + "Blast Furnace");
        bfSummonerItemMeta.setLore(Collections.singletonList(ChatColor.AQUA + "Right click to place!"));
        bfSummoner.setItemMeta(bfSummonerItemMeta);

        NamespacedKey bfKey = new NamespacedKey(this, "bfSummoner");
        ShapedRecipe bfRecipe = new ShapedRecipe(bfKey, bfSummoner);
        bfRecipe.shape("IBI", "KPK", "IRI");
        bfRecipe.setIngredient('I', Material.IRON_BLOCK);
        bfRecipe.setIngredient('P', Material.OBSERVER);
        bfRecipe.setIngredient('B', Material.PISTON_BASE);
        bfRecipe.setIngredient('R', Material.REDSTONE);
        bfRecipe.setIngredient('K', Material.OBSIDIAN);

        Bukkit.addRecipe(bfRecipe);

        ItemStack asmSummoner = new ItemStack(Material.PAPER);
        ItemMeta asmSummonerMeta = asmSummoner.getItemMeta();
        asmSummonerMeta.setDisplayName(ChatColor.GOLD + "Assembler");
        asmSummonerMeta.setLore(Collections.singletonList(ChatColor.AQUA + "Right click to place!"));
        asmSummoner.setItemMeta(asmSummonerMeta);

        NamespacedKey asmKey = new NamespacedKey(this, "asmSummoner");
        ShapedRecipe asmRecipe = new ShapedRecipe(asmKey, asmSummoner);
        asmRecipe.shape("IPI", "HCH", "IPI");
        asmRecipe.setIngredient('I', Material.IRON_BLOCK);
        asmRecipe.setIngredient('P', Material.PISTON_BASE);
        asmRecipe.setIngredient('H', Material.HOPPER);
        asmRecipe.setIngredient('C', Material.WORKBENCH);

        Bukkit.addRecipe(asmRecipe);


        getServer().getPluginManager().registerEvents(new EventListener(), this);

        if (savedMachines.exists()) {
            System.out.println("File found! Extracting contents...");
            try {
                FileInputStream fis = new FileInputStream("plugins/SpigotMachinery/savedMachines.data");
                // FileOutputStream fos = new FileOutputStream("plugins/SpigotMachinery/savedMachines.data");
                ObjectInputStream ois = new ObjectInputStream(fis);
                System.out.println("OIS ready");
                smList = (List<SpigotMachine>) ois.readObject();
                System.out.println(smList.size() + " objects found");
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Critical error, printing stack trace...");
                e.printStackTrace();
            }
            System.out.println("Successfully loaded " + smList.size() + " SpigotMachines");

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (smList.size() > 0) {
            System.out.println("Found " + smList.size() + " SpigotMachine(s) on server, saving on file...");

            if (!savedMachines.exists()) {
                System.out.println("No file found on disable, making one...");
                try {
                    boolean savedCorrectly = savedMachines.createNewFile();
                    System.out.println(savedCorrectly);
                } catch (IOException e) {
                    System.out.println("Fatal error while creating file, printing stack trace:");
                    e.printStackTrace();
                }
            }

            try {
                System.out.println("Saving on savedMachines.tmp ...");
                FileOutputStream fos = new FileOutputStream("plugins/SpigotMachinery/savedMachines.data");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(smList);
                oos.close();
            } catch (IOException e) {
                System.out.println("Fatal error while writing file, printing stack trace:");
                e.printStackTrace();
            }
        } else if (savedMachines.exists()) {
            System.out.println("0 SpigotMachines found, deleting file...");
            System.out.println(savedMachines.delete());
        }
    }
}
