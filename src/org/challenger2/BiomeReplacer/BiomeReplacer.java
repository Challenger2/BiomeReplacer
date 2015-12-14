package org.challenger2.BiomeReplacer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.logging.Logger;

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
	
	private class MyPacketAdapter extends PacketAdapter {
			public MyPacketAdapter(BiomeReplacer p) {
				super(p, PacketType.Play.Server.MAP_CHUNK, PacketType.Play.Server.MAP_CHUNK_BULK);
			}

			@Override
			public void onPacketSending(PacketEvent event) {
				final PacketType type = event.getPacketType();
				if (type == PacketType.Play.Server.MAP_CHUNK) {
					TranslateChunk(event);
				} else if (type == PacketType.Play.Server.MAP_CHUNK_BULK) {
					TranslateBulk(event);
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
	 * @param event
	 */
	private void TranslateChunk(PacketEvent event)
	{
		try{
			PacketContainer packet = event.getPacket();
			Object chunk = packet.getModifier().read(2); // grab field 'c'
			Field[] fields = chunk.getClass().getDeclaredFields(); // get all the fields in the chunk
			byte[] data = byte[].class.cast(fields[0].get(chunk)); // read the first field from the chunk
	
			StructureModifier<Boolean> bools = packet.getBooleans(); // Read field 'd', hasContinous
			boolean hasContinous = bools.read(0);

			// Biome data is only present if this flag is set.
			if (hasContinous) {
				ReplaceBiome(data);
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
	 * @param event
	 */
	private void TranslateBulk(PacketEvent event)
	{
		try {
			PacketContainer packet = event.getPacket();
			Object chunkArray = packet.getModifier().read(2);
			int numChunks = Array.getLength(chunkArray);
			
			for (int i = 0; i < numChunks; i++) {
				Object chunk = Array.get(chunkArray, i);
				Field[] fields = chunk.getClass().getDeclaredFields();
				byte[] data = byte[].class.cast(fields[0].get(chunk));
				ReplaceBiome(data);
			}
		} catch (IllegalAccessException e) {
			log.severe("Failed to get bunk chunk data:\n" + e.toString());
		}
	}

	private void ReplaceBiome(byte[] data)
	{		
		if (data.length > BIOME_ARRAY_SIZE) {
			Arrays.fill(data, data.length - BIOME_ARRAY_SIZE, data.length, (byte)140); // Ice Spikes
		}
	}

}
