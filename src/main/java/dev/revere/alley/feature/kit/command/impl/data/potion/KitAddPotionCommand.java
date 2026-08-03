package dev.revere.alley.feature.kit.command.impl.data.potion;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedHashMap;
import java.util.Map;

public class KitAddPotionCommand extends BaseCommand {
    @CommandData(
            name = "kit.addpotion",
            aliases = {"kit.potion"},
            isAdminOnly = true,
            usage = "kit addpotion <kitName>",
            description = "Add permanent potion effects to a kit from the potion in your hand."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 1) {
            command.sendUsage();
            return;
        }

        KitService kitService = this.plugin.getService(KitService.class);
        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.KIT_NOT_FOUND)));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (!(heldItem.getItemMeta() instanceof PotionMeta potionMeta)) {
            player.sendMessage(CC.translate("&cYou must hold a potion bottle to set effects for this kit!"));
            return;
        }

        Map<PotionEffectType, PotionEffect> effects = new LinkedHashMap<>();
        for (PotionEffect effect : potionMeta.getAllEffects()) {
            effects.put(effect.getType(), effect);
        }
        if (effects.isEmpty()) {
            player.sendMessage(CC.translate("&cThe potion you are holding has no effects!"));
            return;
        }

        kit.getPotionEffects().removeIf(effect -> effects.containsKey(effect.getType()));
        for (PotionEffect effect : effects.values()) {
            kit.getPotionEffects().add(new PotionEffect(
                    effect.getType(),
                    PotionEffect.INFINITE_DURATION,
                    effect.getAmplifier(),
                    effect.isAmbient(),
                    effect.hasParticles(),
                    effect.hasIcon()
            ));
        }

        kitService.saveKit(kit);
        player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.KIT_POTION_EFFECTS_SET)).replace("{kit-name}", kit.getName()));
    }
}
