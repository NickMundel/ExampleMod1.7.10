package com.mrjake.aunis.packet;

import com.mrjake.aunis.state.State;
import com.mrjake.aunis.state.StateProviderInterface;
import com.mrjake.aunis.state.StateTypeEnum;
import com.mrjake.aunis.util.minecraft.BlockPos;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.WorldServer;
import scala.NotImplementedError;

public class StateUpdateRequestToServer extends PositionedPacket {
	public StateUpdateRequestToServer() {}

	StateTypeEnum stateType;

	public StateUpdateRequestToServer(BlockPos pos, StateTypeEnum stateType) {
		super(pos);

		this.stateType = stateType;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		super.toBytes(buf);

		buf.writeInt(stateType.id);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		super.fromBytes(buf);

		stateType = StateTypeEnum.byId(buf.readInt());
	}


	public static class StateUpdateServerHandler implements IMessageHandler<StateUpdateRequestToServer, IMessage> {

		@Override
		public StateUpdatePacketToClient onMessage(StateUpdateRequestToServer message, MessageContext ctx) {
            NetHandlerPlayServer temp = ctx.getServerHandler();
			EntityPlayerMP player = temp.playerEntity;
			WorldServer world = player.getServerForPlayer();

			if (world.blockExists(message.pos.getX(), message.pos.getY(), message.pos.getZ())) {

					StateProviderInterface te = (StateProviderInterface) world.getTileEntity(message.pos.getX(), message.pos.getY(), message.pos.getZ());

					if (te != null) {
                        try {
                            State state = te.getState(message.stateType);

                            if (state != null)
                                AunisPacketHandler.INSTANCE.sendTo(new StateUpdatePacketToClient(message.pos, message.stateType, state), player);
                            else
                                throw new NotImplementedError("State not implemented on " + te.toString());
                        } catch (UnsupportedOperationException e) {
                            e.printStackTrace();
                        }
                    }
			}

			return null;
		}

	}
}
