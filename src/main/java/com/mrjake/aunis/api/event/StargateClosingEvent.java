package com.mrjake.aunis.api.event;


import com.mrjake.aunis.stargate.StargateClosedReasonEnum;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import cpw.mods.fml.common.eventhandler.Cancelable;


/**
 * Event that posted when stargate is trying to close
 * This event is cancelable but you can cancel only if {@link #getReason()} == {@link StargateClosedReasonEnum#REQUESTED}
 */
@Cancelable
public final class StargateClosingEvent extends StargateConnectedAbstractEvent {
    private final StargateClosedReasonEnum reason;

    public StargateClosingEvent(StargateAbstractBaseTile tile, StargateAbstractBaseTile targetTile, boolean initiating, StargateClosedReasonEnum reason) {
        super(tile, targetTile, initiating);
        this.reason = reason;
    }

    public StargateClosedReasonEnum getReason() {
        return reason;
    }
}
