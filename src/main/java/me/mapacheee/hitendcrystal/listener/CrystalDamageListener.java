package me.mapacheee.hitendcrystal.listener;

import com.google.inject.Inject;
import com.thewinterframework.paper.listener.ListenerComponent;
import me.mapacheee.hitendcrystal.HitEndCrystalPlugin;
import me.mapacheee.hitendcrystal.config.ConfigService;
import me.mapacheee.hitendcrystal.service.ClickCounterService;
import me.mapacheee.hitendcrystal.service.CrystalService;
import me.mapacheee.hitendcrystal.service.SwordService;
import me.mapacheee.hitendcrystal.service.WorldGuardService;
import me.mapacheee.hitendcrystal.util.MessageUtil;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

@ListenerComponent
public class CrystalDamageListener implements Listener {

    private final ConfigService configService;
    private final ClickCounterService clickCounterService;
    private final CrystalService crystalService;
    private final WorldGuardService worldGuardService;
    private final SwordService swordService;
    private final MessageUtil messageUtil;
    private final HitEndCrystalPlugin plugin;

    @Inject
    public CrystalDamageListener(
        ConfigService configService,
        ClickCounterService clickCounterService,
        CrystalService crystalService,
        WorldGuardService worldGuardService,
        SwordService swordService,
        MessageUtil messageUtil,
        HitEndCrystalPlugin plugin
    ) {
        this.configService = configService;
        this.clickCounterService = clickCounterService;
        this.crystalService = crystalService;
        this.worldGuardService = worldGuardService;
        this.swordService = swordService;
        this.messageUtil = messageUtil;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrystalDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) return;
        if (!crystalService.isCrystal(crystal)) return;

        event.setCancelled(true);

        if (!(event.getDamager() instanceof Player player)) return;

        if (!worldGuardService.isInRegion(player)) {
            if (configService.getConfig() != null && configService.getConfig().feedbackMessage() && configService.getMessages() != null) {
                String message = messageUtil.format(configService.getMessages().notInRegion());
                player.sendMessage(message);
            }
            return;
        }

        ItemStack itemMain = player.getInventory().getItemInMainHand();
        ItemStack itemOff = player.getInventory().getItemInOffHand();

        boolean isSword = swordService.isClickSword(itemMain) || swordService.isClickSword(itemOff);
        if (!isSword) {
            return;
        }

        if (clickCounterService.isOnCooldown(player)) {
            if (configService.getConfig() != null && configService.getConfig().feedbackMessage() && configService.getMessages() != null) {
                long remaining = clickCounterService.getRemainingCooldown(player);
                String message = messageUtil.format(
                    configService.getMessages().clickCooldown()
                        .replace("{time}", String.valueOf(remaining))
                );
                player.sendMessage(message);
            }
            return;
        }

        clickCounterService.registerClick(player);

        crystalService.playCrystalAnimation(crystal.getLocation());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCrystalDamageGeneric(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal crystal)) return;
        if (!crystalService.isCrystal(crystal)) return;

        event.setCancelled(true);
    }
}
