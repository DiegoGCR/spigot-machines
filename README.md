# spigot-machines
A proof of concept spigot mod that makes physical machines automatable with hoppers
This spigot plugin has only been tested with **Spigot Minecraft 1.12.2** and probably wont work with 1.14+. 
If you have tested it with another version, let me know if it worked or not.

## What is it?
This plugin adds many "SpigotMachines", which are physical structures that can be interacted with 
directly via the use of hoppers.

## How does it work?
SpigotMachines have four basic functions: 
* The fuel hopper, is where you input either coal or a coal block to fuel it.
Coal can do 6 operations while a coal block can do 54.
* The input hopper. Here you can input any compatible items.
* The output hopper. Here is where any compatible products will appear
Some machines may have more than these basic hoppers.
* The core block, breaking this will deactivate the machine and allow you to 
destroy it (you can't do that if it is active).
For optimization reasons, you can't break blocks within the cubic area, so if 
you place something within that area you won't be able to remove it unless you
deactivate the SM
**Please note that you can't input directly to the hoppers! Use chests or other hoppers**


## What can it do?
As of right now, there are three basic SpigotMachines:

The MechanicalFurnace can process 4 materials:
* Converts 1 iron ore to 2 iron ingot
* Converts 1 gold ore to 2 gold ingot
* Converts 1 cobblestone to 1 stone
* Converts 1 redstone to 1 obsidian

The BlastFurnace can process 5 iron ingots into 1 steel ingot.

Assemblers can craft multiple items/materials, and to select which one you need to right-click the core block (obsidian)
As of right now, they can craft:
* Steel armor (Iron armor with prot V, unbreaking VI, and blast prot 5). Each piece requires the standard amound.

More Assembler recipes are planned, such as SMs crafted via the Assembler.

## How do I create a SM?
1. Obtain the summoner by crafting it with the recipes shown below:
![Mechanical Furnace](https://i.imgur.com/ZF5uQ2k.png)
![Blast Furnace](https://i.imgur.com/LBnhQQe.png)
![Assembler](https://i.imgur.com/a9rlbWt.png)
2. Right click the summoner to place it. (Note: as of now, SMs will only place facing North of the player)
Here is an example setup of MechanicalFurnaces:
![Example setup](https://i.imgur.com/M6XmZKV.png)
![Blast Furnace design](https://i.imgur.com/Ww6kBev.png)
![Assembler Design](https://i.imgur.com/Ebuwlh2.png)

## How do you install it?
1. Go to https://www.mediafire.com/file/carlzfsmn4z7mrj/SM-Zipped.zip/file and download the zipped folder.
2. **Extract** the zipped folder's contents and move them to your plugins folder. **DO NOT move the zipped folder on the plugins folder**.

## Why did you do this?
Again, this is just a *proof of concept* plugin, since I got tired of seeing that every other plugin uses armor stand entities and
inventories for GUI's. In my mind, a plugin should complement the game, not overhaul it. This is just an example of how that can be done.
