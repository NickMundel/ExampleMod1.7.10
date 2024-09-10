package com.mrjake.aunis.packet;

import com.mrjake.aunis.Aunis;
import com.mrjake.aunis.state.State;
import com.mrjake.aunis.state.StateProviderInterface;
import com.mrjake.aunis.state.StateTypeEnum;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import scala.NotImplementedError;

public class StateUpdatePacketToClient extends PositionedPacket {
	public StateUpdatePacketToClient() {}

	private StateTypeEnum stateType;
	private State state;

	private ByteBuf stateBuf;

	public StateUpdatePacketToClient(BlockPos pos, StateTypeEnum stateType, State state) {
		super(pos);

		this.stateType = stateType;
		this.state = state;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(stateType.id);

		state.toBytes(buf);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		stateType = StateTypeEnum.byId(buf.readInt());
		stateBuf = buf.copy();
	}

	public static class StateUpdateClientHandler implements IMessageHandler<StateUpdatePacketToClient, IMessage> {

		@Override
		public IMessage onMessage(StateUpdatePacketToClient message, MessageContext ctx) {
			EntityPlayer player = Aunis.proxy.getPlayerClientSide();
			World world = player.getEntityWorld();

			Aunis.proxy.addScheduledTaskClientSide(() -> {

				StateProviderInterface te = (StateProviderInterface) world.getTileEntity(message.pos.getX(), message.pos.getY(), message.pos.getZ());

				try {
					if (te == null)
						return;

					State state = te.createState(message.stateType);

					if (state != null) {
						state.fromBytes(message.stateBuf);

						if (te != null)
							te.setState(message.stateType, state);
					}

					else {
						throw new NotImplementedError("State not implemented on " + te.toString());
					}
				}

				catch (UnsupportedOperationException e) {
					e.printStackTrace();
				}
			});

			return null;
		}

	}
}
