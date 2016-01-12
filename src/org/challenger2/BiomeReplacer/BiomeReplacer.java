	package org.challenger2.BiomeReplacer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class BiomeReplacer extends JavaPlugin {
	
	private final int BIOME_ARRAY_SIZE = 256;

	private static final Logger log = Logger.getLogger("Minecraft");
	private Map<String, Integer> enabledWorlds = new HashMap<String, Integer>();

	/**
	 * Catch packet events
	 */
	private class MyPacketAdapter extends PacketAdapter {
		public MyPacketAdapter(BiomeReplacer p) {
			super(p, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK);
		}

		/**
		 * Catch and handle MAP_CHUNK and MAP_CHUNK_BULK packets
		 */
		@Override
		public void onPacketSending(PacketEvent event) {
			String worldName = event.getPlayer().getWorld().getName().toLowerCase();
			if(enabledWorlds.containsKey(worldName)) {
				int biomeID = enabledWorlds.get(worldName);
				final PacketType type = event.getPacketType();
				if (type == PacketType.Play.Server.MAP_CHUNK) {
					TranslateChunk(event.getPacket(), (byte)biomeID);
				} else if (type == PacketType.Play.Server.MAP_CHUNK_BULK) {
					TranslateBulk(event.getPacket(), (byte)biomeID);
				}
			}
		}
	}

	private PacketAdapter adapter;

	/**
	 * Plugin initiation
	 * 
	 * Register for outgoing chunk packets and rewrite biome data
	 */
	@Override
	public void onEnable() {
		LoadConfig();
		adapter = new MyPacketAdapter(this);
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
	}

	/**
	 * Shut everything down
	 */
	@Override
	public void onDisable() {
		ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
		adapter = null;
	}

	/**
	 * Handle player commands
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
		if (name.equalsIgnoreCase("BiomeReplacer"))  {
			if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
				PrintUsage(sender);
			} else if (args.length == 3 && args[0].equalsIgnoreCase("enable")) {
				CmdEnableWorld(sender, args);
			} else if (args.length == 2 && args[0].equalsIgnoreCase("disable")) {
				CmdDisableWorld(sender, args);
			} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
				CmdList(sender);
			} else if (args.length == 1 && args[0].equalsIgnoreCase("listbiomes")) {
				CmdListBiomes(sender);
			} else {
				PrintUsage(sender);
			}
			return true;
		} else {
			return false;
		}
	}

	public void PrintUsage(CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "USAGE: /BiomeReplacer enable WORLD {BIOME_NUMBER | BIOME_NAME}");
		sender.sendMessage(ChatColor.GREEN + "       /BiomeReplacer disable WORLD");
		sender.sendMessage(ChatColor.GREEN + "       /BiomeReplacer list");
		sender.sendMessage(ChatColor.GREEN + "       /BiomeReplacer listBiomes");
	}
	
	/**
	 * Enable the requested world
	 * 
	 */
	private void CmdEnableWorld(CommandSender sender, String[] args) {
		if(!sender.hasPermission("BiomeReplacer.enable")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use /BiomeReplace enable");
			return;
		}

		String worldName = args[1];
		String biomeArg = args[2];
		BiomeID biome = BiomeID.lookupName(biomeArg);
		if (biome == null) {
			sender.sendMessage(ChatColor.RED + "Invalid Biome");
			return;
		}

		for (World world : getServer().getWorlds()) {
			if (world.getName().equalsIgnoreCase(worldName)) {
				enabledWorlds.put(worldName.toLowerCase(), biome.getID());
				sender.sendMessage(ChatColor.GREEN + "Biome replacement enabled");
				SaveConfig();
				return;
			}
		}
		sender.sendMessage(ChatColor.RED + "Unknown world");
	}

	/**
	 * Disable the requested world
	 */
	private void CmdDisableWorld(CommandSender sender, String[] args) {
		if(!sender.hasPermission("BiomeReplacer.enable")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to use /BiomeReplace disable");
			return;
		}

		String worldName = args[1].toLowerCase();
		if (enabledWorlds.containsKey(worldName)) {
			enabledWorlds.remove(worldName);
			sender.sendMessage(ChatColor.GREEN + "World removed");
			SaveConfig();
		} else {
			sender.sendMessage(ChatColor.RED + "World not found or not enabled");
		}
	}

	/**
	 * List all biome replacements
	 */
	private void CmdList(CommandSender sender) {
		for (String key : enabledWorlds.keySet()) {
			int biomeID = enabledWorlds.get(key);
			BiomeID biome = BiomeID.lookupID(biomeID);
			String biomeName = null;
			if (biome == null) {
				biomeName = "<Unknown Biome = " + biomeID + ">";
			} else {
				biomeName = biome.name();
			}
			sender.sendMessage(ChatColor.GREEN + key + ": " + biomeName);;
		}
	}

	private void CmdListBiomes(CommandSender sender) {
		StringBuilder sb = new StringBuilder(ChatColor.GREEN + "");
		boolean first = true;
		for (BiomeID biome : BiomeID.values()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(biome.name());
		}
		sender.sendMessage(sb.toString());
	}

	/**
	 * Load config.yml from disk
	 */
	private void LoadConfig()
	{
		saveDefaultConfig();
		enabledWorlds.clear();
		try {
			ConfigurationSection s = getConfig().getConfigurationSection("BiomeReplacements");
			if (s != null) {
				for (String key : s.getKeys(false)) {
					enabledWorlds.put(key, s.getInt(key));
				}
			}
		} catch (Exception e) {
			log.warning("BiomeReplacer: Failed to load configuration file");
		}
	}

	/**
	 * Save biome data to disk
	 */
	private void SaveConfig()
	{
		try {
			ConfigurationSection s = getConfig().createSection("BiomeReplacements");
			for (String world : enabledWorlds.keySet()) {
				s.set(world, enabledWorlds.get(world));
			}
			saveConfig();
		} catch (Exception e) {
			log.warning("BiomeReplacer: Failed to save configuration file");
		}
	}

	/**
	 * Translate a Chunk object to a new biome
	 * 
	 * event.getPacket gets an NMS object with the following structure
	 * {
     *   private int a;
     *   private int b;
     *   private PacketPlayOutMapChunk.ChunkMap c;
     *   private boolean d;
	 * }
	 * Here, field 3 has the data we want.
	 * 
	 * After we get a ChunkMap object, we use reflection to get the data out of it
	 * A ChunkMap looks like this:
	 * 
	 * public static class ChunkMap {
     *   public byte[] a;
     *   public int b;
     *   public ChunkMap() {}
     * }
     * 
     * Here, a is the raw chunk data and b is the primaryMask
     * 
     * The biome data is always the last 256 bytes of the chunk data, if
     * the hasContinous flag is set (field d)
	 * 
	 * @param packet A packet to translate
	 * @param biomeID The Biome ID to use
	 */
	private void TranslateChunk(PacketContainer packet, byte biomeID)
	{
		try{
			Object chunk = packet.getModifier().read(2); // grab field 'c'
			Field[] fields = chunk.getClass().getDeclaredFields(); // get all the fields in the chunk
			byte[] data = byte[].class.cast(fields[0].get(chunk)); // read the first field from the chunk
	
			StructureModifier<Boolean> bools = packet.getBooleans(); // Read field 'd', hasContinous
			boolean hasContinous = bools.read(0);

			// Biome data is only present if this flag is set.
			if (hasContinous) {
				ReplaceBiome(data, biomeID);
			}
		} catch (IllegalAccessException e) {
			log.severe("Failed to get single chunk data:\n" + e.toString());
		}
	}

	/**
	 * translate a Chunk Builk object to a new biome
	 * 
	 * We use event.getPacket to get a structure like this one:
	 * {
	 *  private int[] a;
     *	private int[] b;
     *	private PacketPlayOutMapChunk.ChunkMap[] c;
     *	private boolean d;
     *	private World world; // Spigot
	 * }
	 * 
	 * What we want is field c, a ChunkMap array
	 * 
	 * After we get a ChunkMap object, we use reflection to get the data out of it
	 * A ChunkMap looks like this:
	 * 
	 * public static class ChunkMap {
     *   public byte[] a;
     *   public int b;
     *   public ChunkMap() {}
     * }
     * 
     * Here, a is the raw chunk data and b is the primaryMask
     * 
     * The biome data is always the last 256 bytes of the chunk data
	 * 
	 * @param packet A packet to translate
	 * @param biomeID Biome ID to use
	 */
	private void TranslateBulk(PacketContainer packet, byte biomeID)
	{
		try {
			Object chunkArray = packet.getModifier().read(2);
			int numChunks = Array.getLength(chunkArray);
			
			for (int i = 0; i < numChunks; i++) {
				Object chunk = Array.get(chunkArray, i);
				Field[] fields = chunk.getClass().getDeclaredFields();
				byte[] data = byte[].class.cast(fields[0].get(chunk));
				ReplaceBiome(data, biomeID);
			}
		} catch (IllegalAccessException e) {
			log.severe("Failed to get bunk chunk data:\n" + e.toString());
		}
	}

	private void ReplaceBiome(byte[] data, byte biomeID)
	{		
		if (data.length > BIOME_ARRAY_SIZE) {
			Arrays.fill(data, data.length - BIOME_ARRAY_SIZE, data.length, biomeID); // Ice Spikes 140
		}
	}

}
