package com.mrjake.aunis.state;

import com.mrjake.aunis.util.BaseUtils;
import com.mrjake.aunis.util.minecraft.IBlockState;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;

/**
 * Holds {@link IBlockState} of camouflage block to be displayed instead.
 *
 * @author MrJake
 */
public class StargateCamoState extends State {
	public StargateCamoState() {}

	private IBlockState state;

	public StargateCamoState(IBlockState state) {
		this.state = state;
	}

	public IBlockState getState() {
		return state;
	}


	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(state != null);
		if (state != null) {
			buf.writeInt(Block.getIdFromBlock(state.getBlock()));
			buf.writeInt(BaseUtils.getMetaFromBlockState(state));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void fromBytes(ByteBuf buf) {
		if (buf.readBoolean()) {
			Block block = Block.getBlockById(buf.readInt());
			state = BaseUtils.getBlockStateFromMeta(block, buf.readInt());
		}
	}
}
