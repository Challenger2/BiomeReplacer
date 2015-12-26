package org.challenger2.BiomeReplacer;

public enum BiomeID {
	OCEAN(0),
	PLAINS(1),
	DESERT(2),
	EXTREME_HILLS(3),
	FOREST(4),
	TAIGA(5),
	SWAMPLAND(6),
	RIVER(7),
	HELL(8),
	THE_END(9),
	FROZEN_OCEAN(10),
	FROZEN_RIVER(11),
	ICE_PLAINS(12),
	ICE_MOUNTAINS(13),
	MUSHROOM_ISLAND(14),
	MUSHROOM_ISLAND_SHORE(15),
	BEACH(16),
	DESERT_HILLS(17),
	FOREST_HILLS(18),
	TAIGA_HILLS(19),
	EXTREME_HILLS_EDGE(20),
	JUNGLE(21),
	JUNGLE_HILLS(22),
	JUNGLE_EDGE(23),
	DEEP_OCEAN(24),
	STONE_BEACH(25),
	COLD_BEACH(26),
	BIRCH_FOREST(27),
	BIRCH_FOREST_HILLS(28),
	ROOFED_FORESET(29),
	COLD_TAIGA(30),
	COLD_DAIGA_HILLS(31),
	MEGA_TAIGA(32),
	MEGA_TAIGA_HILLS(33),
	EXTREME_HILLS_PLUS(34),
	SAVANNA(35),
	SAVANNA_PLATEAU(36),
	MESA(37),
	MESA_PLATEAU_F(38),
	MESA_PLATEAU(39),
	PLAINS_M(128),
	SUNFLOWER_PLAINS(129),
	DESERT_M(130),
	EXTREME_HILLS_M(131),
	FLOWER_FOREST(132),
	TAIGA_M(133),
	SWAMPLAND_M(134),
	ICE_PLAINS_SPIKES(140),
	JUNGLE_M(149),
	JUNGLE_EDGE_M(151),
	BIRCH_FOREST_M(155),
	BIRCH_FOREST_HILLS_M(156),
	ROOFED_FOREST_M(157),
	COLD_TAIGA_M(158),
	MEGA_SPRUCE_TAIGA(160),
	REDWOOD_TAIGA_HILLS_M(161),
	EXTREME_HILLS_PLUS_M(162),
	SAVANNA_M(163),
	SAVANNA_PLATEAU_M(164),
	MESA_BRYCE(165),
	MESA_PLATEAU_F_M(166),
	MESA_PLATEAU_M(167);

	private final int value;

	private BiomeID(int value) {
		this.value = value;
	}
	
	/**
	 * Get the biome ID
	 * 
	 * @return Biome ID number
	 */
	public int getID() {
		return value;
	}

	/**
	 * Get a biome ID by name, case insensitive
	 * 
	 * This is almost the same as valueOf, Except that the parameter is case insensitive and does not throw exceptions
	 * 
	 * @param name Name of the biome to get
	 * @return Returns a biome, or null
	 */
	public static BiomeID lookupName(String name) {
		try {
			return valueOf(name.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Get biome by ID
	 * @param id Biome ID to lookup
	 * @return Biome with the requested id, or null
	 */
	public static BiomeID lookupID(int ID) {
		for (BiomeID b : values()) {
			if (b.getID() == ID) {
				return b;
			}
		}
		return null;
	}
}
