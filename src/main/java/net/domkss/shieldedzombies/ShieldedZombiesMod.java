package net.domkss.shieldedzombies;

import net.domkss.shieldedzombies.config.ModConfig;
import net.fabricmc.api.ModInitializer;


import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShieldedZombiesMod implements ModInitializer {

	public static Logger LOGGER;
	public static ModConfig modConfig;
	@Override
	public void onInitialize() {
		initLogger();
		modConfig = new ModConfig(LOGGER);
		LOGGER.info("Successfully loaded!");
	}


	private void initLogger() {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tT] [%4$s] [ShieldedZombies] %5$s %n");
		LOGGER = Logger.getLogger("ShieldedZombies");
		LOGGER.setLevel(Level.INFO);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(LOGGER.getLevel());
		LOGGER.addHandler(handler);
		LOGGER.setUseParentHandlers(false);
	}
}
