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
    static List<SpigotMachine> mfList = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        ItemStack MFSummoner = new ItemStack(Material.PAPER);
        ItemMeta meta = MFSummoner.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Mechanical Furnace");
        meta.setLore(Collections.singletonList(ChatColor.AQUA + "Right click to place!"));
        MFSummoner.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "MFSummoner");
        ShapedRecipe recipe = new ShapedRecipe(key, MFSummoner);
        recipe.shape("IBI", "IPI", "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('P', Material.FURNACE);
        recipe.setIngredient('B', Material.PISTON_BASE);
        recipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(recipe);
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        if (savedMachines.exists()) {
            System.out.println("File found! Extracting contents...");
            try {
                FileInputStream fis = new FileInputStream("plugins/SpigotMachinery/savedMachines.data");
                // FileOutputStream fos = new FileOutputStream("plugins/SpigotMachinery/savedMachines.data");
                ObjectInputStream ois = new ObjectInputStream(fis);
                System.out.println("OIS ready");
                mfList = (List<SpigotMachine>) ois.readObject();
                System.out.println(mfList.size() + " objects found");
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Critical error, printing stack trace...");
                e.printStackTrace();
            }
            System.out.println("Successfully loaded " + mfList.size() + "SpigotMachines");

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (mfList.size() > 0) {
            System.out.println("Found " + mfList.size() + " SpigotMachine(s) on server, saving on file...");

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
                oos.writeObject(mfList);
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
