package net.skillwars.practice.commands.elo;

import net.skillwars.practice.Practice;
import net.skillwars.practice.kit.Kit;
import net.skillwars.practice.player.PlayerData;
import net.skillwars.practice.util.CC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ResetEloCommand extends Command {

    private Practice plugin = Practice.getInstance();

    public ResetEloCommand() {
        super("resetelo");
        this.setPermission("practice.command.resetelo");
        this.setAliases(Arrays.asList("eloreset"));
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(CC.translate("&cNo Console."));
            return true;
        }
        Player player = (Player) commandSender;
        if (strings.length == 0) {
            player.sendMessage(CC.translate("&cUsa: /" + s + " <kitName>"));
            return true;
        }
        PlayerData data = this.plugin.getPlayerManager().getPlayerData(player.getUniqueId());
        String kitName = strings[0];
        Kit kit = this.plugin.getKitManager().getKit(kitName);
        if (kit.isRanked()) {
            data.setElo(kitName, 1000);
        }
        return false;
    }
}
