package net.islandearth.anvillogin.translation;

import net.islandearth.anvillogin.AnvilLogin;
import net.islandearth.languagy.api.language.Language;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum Translations {
	KICKED("&7[&9AnvilLogin&7] &cYou have been kicked for not entering the password within 30 seconds."),
	LOGGED_IN("&7[&9AnvilLogin&7] &aYou logged in to the server!"),
	GUI_TITLE("Enter Password"),
	GUI_TEXT("Enter Password"),
	GUI_WRONG("Incorrect.");

	private final String defaultValue;
	private final boolean isList;
	
	Translations(String defaultValue) {
		this.defaultValue = defaultValue;
		this.isList = false;
	}

	Translations(String defaultValue, boolean isList) {
		this.defaultValue = defaultValue;
		this.isList = isList;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}

	public boolean isList() {
		return isList;
	}

	private String getPath() {
		return this.toString().toLowerCase();
	}

	public void send(Player player) {
		String message = AnvilLogin.getAPI().getTranslator().getTranslationFor(player, this.getPath());
		player.sendMessage(message);
	}

	public void send(Player player, String... values) {
		String message = AnvilLogin.getAPI().getTranslator().getTranslationFor(player, this.getPath());
		message = replaceVariables(message, values);
		player.sendMessage(message);
	}

	public void sendList(Player player) {
		List<String> message = AnvilLogin.getAPI().getTranslator().getTranslationListFor(player, this.getPath());
		message.forEach(player::sendMessage);
	}

	public void sendList(Player player, String... values) {
		List<String> messages = AnvilLogin.getAPI().getTranslator().getTranslationListFor(player, this.getPath());
		messages.forEach(message -> {
			message = replaceVariables(message, values);
			player.sendMessage(message);
		});
	}

	public String get(Player player) {
		return AnvilLogin.getAPI().getTranslator().getTranslationFor(player, this.getPath());
	}
	
	public String get(Player player, String... values) {
		String message = AnvilLogin.getAPI().getTranslator().getTranslationFor(player, this.getPath());
		message = replaceVariables(message, values);
		return message;
	}

	public List<String> getList(Player player) {
		return AnvilLogin.getAPI().getTranslator().getTranslationListFor(player, this.getPath());
	}

	public List<String> getList(Player player, String... values) {
		List<String> messages = new ArrayList<>();
		AnvilLogin.getAPI().getTranslator()
				.getTranslationListFor(player, this.getPath())
				.forEach(message -> messages.add(replaceVariables(message, values)));
		return messages;
	}
	
	public static void generateLang(AnvilLogin plugin) {
		File lang = new File(plugin.getDataFolder() + "/lang/");
		lang.mkdirs();
		
		for (Language language : Language.values()) {
			try {
				plugin.saveResource("lang/" + language.getCode() + ".yml", false);
				plugin.getLogger().info("Generated " + language.getCode() + ".yml");
			} catch (IllegalArgumentException ignored) { }

			File file = new File(plugin.getDataFolder() + "/lang/" + language.getCode() + ".yml");
			if (file.exists()) {
				FileConfiguration config = YamlConfiguration.loadConfiguration(file);
				for (Translations key : values()) {
					if (config.get(key.toString().toLowerCase()) == null) {
						plugin.getLogger().warning("No value in translation file for key "
								+ key.toString() + " was found. Regenerate language files?");
					}
				}
			}
		}
	}

	private String replaceVariables(String message, String... values) {
		String modifiedMessage = message;
		for (int i = 0; i < 10; i++) {
			if (values.length > i) modifiedMessage = modifiedMessage.replaceAll("%" + i, values[i]);
			else break;
		}

		return modifiedMessage;
	}
}
