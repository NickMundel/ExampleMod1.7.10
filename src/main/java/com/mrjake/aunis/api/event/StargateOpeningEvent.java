package com.mrjake.aunis.api.event;


import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import cpw.mods.fml.common.eventhandler.Cancelable;

/**
 * Event that posted when stargate is opening
 * This event is cancelable. You can cancel it and stargate will not open
 */
@Cancelable
public final class StargateOpeningEvent extends StargateConnectedAbstractEvent {

    public StargateOpeningEvent(StargateAbstractBaseTile tile, StargateAbstractBaseTile targetTile, boolean initiating) {
        super(tile, targetTile, initiating);
    }


}
