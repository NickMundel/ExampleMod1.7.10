package com.mrjake.aunis.api.event;


import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;

/**
 * Event that posted when stargate is fully closed
 */
public final class StargateClosedEvent extends StargateAbstractEvent {
    public StargateClosedEvent(StargateAbstractBaseTile tile) {
        super(tile);
    }

}
