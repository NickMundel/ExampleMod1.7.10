package com.mrjake.aunis.api.event;


import com.mrjake.aunis.stargate.StargateOpenResult;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;

/**
 * Event that posted when stargate dial failed
 */
public final class StargateDialFailEvent extends StargateAbstractEvent {
    private final StargateOpenResult reason;

    public StargateDialFailEvent(StargateAbstractBaseTile tile, StargateOpenResult result) {
        super(tile);
        this.reason = result;
    }

    public StargateOpenResult getReason(){
        return reason;
    }
}
