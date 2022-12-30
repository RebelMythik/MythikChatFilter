package me.rebelmythik.mythikchatfilter;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MythikChatFilter extends JavaPlugin implements Listener {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        config = getConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        List<String> blockedMessages = config.getStringList("blocked-messages");
        ConfigurationSection equivalentCharactersSection = config.getConfigurationSection("equivalent-characters");
        Set<String> equivalentCharactersKeys = equivalentCharactersSection.getKeys(false);
        Map<String, List<String>> equivalentCharactersMap = new HashMap<>();
        for (String key : equivalentCharactersKeys) {
            List<String> equivalentCharacters = equivalentCharactersSection.getStringList(key);
            equivalentCharactersMap.put(key, equivalentCharacters);
        }
        String patternString = "\\b(";
        for (int i = 0; i < blockedMessages.size(); i++) {
            String blockedMessage = blockedMessages.get(i);
            for (int j = 0; j < blockedMessage.length(); j++) {
                char c = blockedMessage.charAt(j);
                if (equivalentCharactersMap.containsKey(String.valueOf(c))) {
                    patternString += "[" + c;
                    List<String> equivalentCharacters = equivalentCharactersMap.get(String.valueOf(c));
                    for (String equivalentCharacter : equivalentCharacters) {
                        patternString += equivalentCharacter;
                    }
                    patternString += "]";
                } else {
                    patternString += c;
                }
            }
            if (i < blockedMessages.size() - 1) {
                patternString += "|";
            }
        }
        patternString += ")\\b";
        Pattern pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Sorry, your message was blocked because it contained a blocked word.");
        }
    }
}