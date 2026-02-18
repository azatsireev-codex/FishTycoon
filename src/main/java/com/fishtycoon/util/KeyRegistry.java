package com.fishtycoon.util;

import com.fishtycoon.FishTycoonPlugin;
import org.bukkit.NamespacedKey;

public class KeyRegistry {
    public final NamespacedKey fishId;
    public final NamespacedKey fishSize;
    public final NamespacedKey fishRarity;
    public final NamespacedKey rodOwner;
    public final NamespacedKey backpackOwner;
    public final NamespacedKey backpackItem;

    public KeyRegistry(FishTycoonPlugin plugin) {
        this.fishId = new NamespacedKey(plugin, "fishtycoon_fishId");
        this.fishSize = new NamespacedKey(plugin, "fishtycoon_size");
        this.fishRarity = new NamespacedKey(plugin, "fishtycoon_rarity");
        this.rodOwner = new NamespacedKey(plugin, "fishtycoon_owner");
        this.backpackOwner = new NamespacedKey(plugin, "fishtycoon_backpack_owner");
        this.backpackItem = new NamespacedKey(plugin, "fishtycoon_backpack_item");
    }
}
