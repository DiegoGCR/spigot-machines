package me.ammonium.spigotmachinery;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Serializable;
import java.util.Map;

@SuppressWarnings("WeakerAccess") // Public class
public class SpigotMachine implements Serializable {
    Map<String, Object> inputHopperLoc;
    Map<String, Object> outputHopperLoc;
    private Map<String, Object> machineBottom;
    private Map<String, Object> machineTop;
    private Map<String, Object> coreBlock;
    private Map<String, Object> fuelHopperLoc;
    int fuelRemaining = 0;

    void setCoreBlock(Location coreBlock) {
        this.coreBlock = coreBlock.serialize();
    }
    void setMachineBottom(Location machineBottom) {
        this.machineBottom = machineBottom.serialize();
    }
    void setMachineTop(Location machineTop) {
        this.machineTop = machineTop.serialize();
    }
    void setFuelHopperLoc(Location fuelHopperLoc) {
        this.fuelHopperLoc = fuelHopperLoc.serialize();
    }
    void setInputHopperLoc(Location inputHopperLoc) {
        this.inputHopperLoc = inputHopperLoc.serialize();
    }
    void setOutputHopperLoc(Location outputHopperLoc) {
        this.outputHopperLoc = outputHopperLoc.serialize();
    }
    Location getFuelHopperLoc() {
        return Location.deserialize(fuelHopperLoc);
    }
    Location getInputHopperLoc() {
        return Location.deserialize(inputHopperLoc);
    }
    Location getMachineBottom() {
        return Location.deserialize(machineBottom);
    }
    Location getMachineTop() {
        return Location.deserialize(machineTop);
    }
    Location getCoreBlock() {
        return Location.deserialize(coreBlock);
    }

    boolean processFuel(ItemStack input) {
        if (fuelRemaining < 100 && input.getType().equals(Material.COAL)) {
            fuelRemaining += 6;
            Hopper fuelHop = (Hopper) Location.deserialize(fuelHopperLoc).getBlock().getState();
            Inventory fuelInv = fuelHop.getInventory();
            fuelInv.removeItem(input);
            return true;
        } else if (fuelRemaining < 100 && input.getType().equals(Material.COAL_BLOCK)) {
            fuelRemaining += 54;
            Hopper fuelHop = (Hopper) Location.deserialize(fuelHopperLoc).getBlock().getState();
            Inventory fuelInv = fuelHop.getInventory();
            fuelInv.removeItem(input);
            return true;
        } else {
            return false;
        }
    }

    boolean processInput(ItemStack input) {
        return false;
    }
}
