package com.mrjake.aunis.config;

import com.mrjake.aunis.util.ItemMetaPair;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ItemMetaParser {

	/**
	 * Parses array of configured items/blocks. For format see {@link ItemMetaParser#getBlockStateFromString(String)}
	 *
	 * @param config Array of single lines
	 * @return List of {@link IBlockState}s or empty list.
	 */
	@Nonnull
	static List<ItemMetaPair> parseConfig(String[] config) {
		List<ItemMetaPair> list = new ArrayList<>();

		for (String line : config) {
			ItemMetaPair stack = getItemMetaPairFromString(line);

			if(stack != null) {
				list.add(stack);
			}
		}

		return list;
	}

	/**
	 * Parses single line of the config.
	 *
	 * @param line Consists of modid:blockid[:meta]
	 * @return {@link IBlockState} when valid block, {@code null} otherwise.
	 */
	@Nullable
	static ItemMetaPair getItemMetaPairFromString(String line) {
        String[] parts = line.trim().split(":", 3);

        Item item = GameRegistry.findItem(parts[0], parts[1]);

        if (item != null) {
        	if (parts.length == 2 || parts[2].equals("*"))
        		return new ItemMetaPair(item, 0);

            try {
            	return new ItemMetaPair(item, Integer.parseInt(parts[2]));
            }

        	catch (NumberFormatException e) {
    			return null;
    		}
        }

        return null;
    }
}
