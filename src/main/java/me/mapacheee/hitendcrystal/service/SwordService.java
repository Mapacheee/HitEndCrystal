package me.mapacheee.hitendcrystal.service;

import com.google.inject.Inject;
import com.thewinterframework.service.annotation.Service;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.Config;
import me.mapacheee.hitendcrystal.config.ConfigService;
import me.mapacheee.hitendcrystal.util.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@Service
public class SwordService {

    private final ConfigService configService;
    private final MessageUtil messageUtil;
    private final HitEndCrystalPlugin plugin;
    private NamespacedKey swordKey;

    @Inject
    public SwordService(ConfigService configService, MessageUtil messageUtil, HitEndCrystalPlugin plugin) {
        this.configService = configService;
        this.messageUtil = messageUtil;
        this.plugin = plugin;
    }

    private NamespacedKey getSwordKey() {
        if (swordKey == null) {
            swordKey = new NamespacedKey(plugin, "click_sword");
        }
        return swordKey;
    }

    public ItemStack createSword() {
        Config.SwordItem swordConfig = configService.getConfig().swordItem();

        ItemStack sword = new ItemStack(swordConfig.material());
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.displayName(messageUtil.formatComponent(swordConfig.name()));

            List<Component> lore = new ArrayList<>();
            for (String line : swordConfig.lore()) {
                lore.add(messageUtil.formatComponent(line));
            }
            meta.lore(lore);

            if (swordConfig.unbreakable()) {
                meta.setUnbreakable(true);
            }

            meta.getPersistentDataContainer().set(getSwordKey(), PersistentDataType.BYTE, (byte) 1);

            sword.setItemMeta(meta);

            for (String enchantStr : swordConfig.enchantments()) {
                String[] parts = enchantStr.split(":");
                if (parts.length == 2) {
                    try {
                        Enchantment enchant = RegistryAccess.registryAccess()
                            .getRegistry(RegistryKey.ENCHANTMENT)
                            .get(NamespacedKey.minecraft(parts[0].toLowerCase()));
                        int level = Integer.parseInt(parts[1]);
                        if (enchant != null) {
                            sword.addUnsafeEnchantment(enchant, level);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Invalid enchantment: " + enchantStr);
                    }
                }
            }
        }

        return sword;
    }

    public void giveSword(Player player) {
        ItemStack sword = createSword();
        int slot = configService.getConfig().swordItem().slot();

        int invSize = player.getInventory().getSize();
        if (slot < 0 || slot >= invSize) {
            plugin.getLogger().warning("Configured sword slot out of range: " + slot + ". Using first empty slot instead.");
            slot = -1;
        }

        ItemStack itemInSlot = slot == -1 ? null : player.getInventory().getItem(slot);
        if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
            int targetSlot = slot == -1 ? player.getInventory().firstEmpty() : slot;
            player.getInventory().setItem(targetSlot, sword);
        } else {
            int emptySlot = player.getInventory().firstEmpty();
            if (emptySlot != -1) {
                player.getInventory().setItem(emptySlot, sword);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), sword);
            }
        }
    }

    public void removeSword(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isClickSword(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }

    public boolean isClickSword(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(getSwordKey(), PersistentDataType.BYTE);
    }
}