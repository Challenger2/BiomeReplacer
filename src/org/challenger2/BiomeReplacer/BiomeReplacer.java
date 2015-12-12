package org.challenger2.BiomeReplacer;

import java.util.Arrays;

import org.bukkit.plugin.java.JavaPlugin;

import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk.ChunkMap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class BiomeReplacer extends JavaPlugin {
	
	private final int BIOME_ARRAY_SIZE = 256;
	
	
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

	private void TranslateChunk(PacketEvent event)
	{
		PacketContainer packet = event.getPacket();

		ChunkMap chunk = (ChunkMap)packet.getModifier().read(2);
		byte[] data = chunk.a;
		//int primaryBitMask = chunk.b;

		StructureModifier<Boolean> bools = packet.getBooleans();
		boolean hasContinous = bools.read(0);

		if (hasContinous) {
			ReplaceBiome(data);
		}
	}

	private void TranslateBulk(PacketEvent event)
	{
		PacketContainer packet = event.getPacket();

		ChunkMap[] chunks = (ChunkMap[])packet.getModifier().read(2);

		for (ChunkMap chunk : chunks) {
			byte[] data = chunk.a;
			// int primaryBitMask = chunk.b;

			ReplaceBiome(data);
		}
	}

	private void ReplaceBiome(byte[] data)
	{		
		if (data.length > BIOME_ARRAY_SIZE) {
			Arrays.fill(data, data.length - BIOME_ARRAY_SIZE, data.length, (byte)140); // Ice Spikes
		}
	}


	/*
	 *  This one works!!!
	 *
	private void TranslateBulk(PacketEvent event)
	{

		PacketContainer packet = event.getPacket();

		ChunkMap[] chunks = (ChunkMap[])packet.getModifier().read(2);

		for (ChunkMap chunk : chunks) {
			byte[] data = chunk.a;
			int length = chunk.b;
			
			if (length > data.length) {
				length = data.length;
			}
	
			StructureModifier<Boolean> bools = packet.getBooleans();
			Boolean hasContinousObj = bools.readSafely(0);
			boolean hasContinous;
			if (hasContinousObj  == null) {
				hasContinous = true;
			} else {
				hasContinous = hasContinousObj;
			}
	
			if (hasContinous && data.length > BIOME_ARRAY_SIZE) {
				// There should be biome data if chunk mask is nonzero
				for (int i = data.length - BIOME_ARRAY_SIZE; i < data.length; i++) {
					data[i] = (byte)140; // Ice Spikes
				}
			}
		}
	}
	*/
	
}
