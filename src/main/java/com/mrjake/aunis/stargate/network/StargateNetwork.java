package com.mrjake.aunis.stargate.network;

import com.mrjake.aunis.Aunis;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class StargateNetwork extends WorldSavedData {

	private static final String DATA_NAME = Aunis.MODID + "_StargateNetworkData";

	public StargateNetwork() {
		super(DATA_NAME);
		init();
	}

	public StargateNetwork(String dataName) {
		super(dataName);
		init();
	}

	private void init() {
		for (SymbolTypeEnum symbolType : SymbolTypeEnum.values())
			stargateNetworkMap.put(symbolType, new HashMap<>());
	}

	private Map<StargateAddress, StargatePos> getMapFromAddress(StargateAddress address) {
		return stargateNetworkMap.get(address.getSymbolType());
	}


	// ---------------------------------------------------------------------------------------------------------
	// Stargate Network

	private Map<SymbolTypeEnum, Map<StargateAddress, StargatePos>> stargateNetworkMap = new HashMap<>();

	public Map<SymbolTypeEnum, Map<StargateAddress, StargatePos>> getMap() {
		return stargateNetworkMap;
	}

	public boolean isStargateInNetwork(StargateAddress gateAddress) {
		return getMapFromAddress(gateAddress).containsKey(gateAddress);
	}

	@Nullable
	public StargatePos getStargate(StargateAddress address) {
		if (address == null)
			return null;

		return getMapFromAddress(address).get(address);
	}

	public void addStargate(StargateAddress gateAddress, StargatePos stargatePos) {
		getMapFromAddress(gateAddress).put(gateAddress, stargatePos);

		markDirty();
	}

	public void removeStargate(StargateAddress gateAddress) {
		getMapFromAddress(gateAddress).remove(gateAddress);

		markDirty();
	}


	// ---------------------------------------------------------------------------------------------------------
	// Nether gate

	public static final StargateAddressDynamic EARTH_ADDRESS = new StargateAddressDynamic(SymbolTypeEnum.MILKYWAY);

	static {
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.AURIGA);
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.CETUS);
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.CENTAURUS);
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.CANCER);
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.SCUTUM);
		EARTH_ADDRESS.addSymbol(SymbolMilkyWayEnum.ERIDANUS);
	}

	private StargateAddress netherGateAddress;
	private StargateAddress lastActivatedOrlins;

	public boolean hasNetherGate() {
		return netherGateAddress != null;
	}

	public void setNetherGate(StargateAddress address) {
		netherGateAddress = address;
		markDirty();
	}

	public StargateAddress getNetherGate() {
		return netherGateAddress;
	}

	public void setLastActivatedOrlins(StargateAddress address) {
		lastActivatedOrlins = address;
		markDirty();
	}

	public StargateAddress getLastActivatedOrlins() {
		return lastActivatedOrlins;
	}

	// ---------------------------------------------------------------------------------------------------------
	// Reading and writing

	@Override
	public void readFromNBT(NBTTagCompound compound) {

		NBTTagList stargateTagList = compound.getTagList("stargates", NBT.TAG_COMPOUND);

        for (int i = 0; i < stargateTagList.tagCount(); i++) {
			NBTTagCompound stargateCompound = stargateTagList.getCompoundTagAt(i);

			StargateAddress stargateAddress = new StargateAddress(stargateCompound.getCompoundTag("address"));
			StargatePos stargatePos = new StargatePos(stargateAddress.getSymbolType(), stargateCompound.getCompoundTag("pos"));

			getMapFromAddress(stargateAddress).put(stargateAddress, stargatePos);
		}

		if (compound.hasKey("netherGateAddress"))
			netherGateAddress = new StargateAddress(compound.getCompoundTag("netherGateAddress"));

		if (compound.hasKey("lastActivatedOrlins"))
			lastActivatedOrlins = new StargateAddress(compound.getCompoundTag("lastActivatedOrlins"));
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		NBTTagList stargateTagList = new NBTTagList();

		for (Map<StargateAddress, StargatePos> stargateMap : stargateNetworkMap.values()) {

			for (Map.Entry<StargateAddress, StargatePos> stargateEntry : stargateMap.entrySet()) {

				NBTTagCompound stargateCompound = new NBTTagCompound();
				stargateCompound.setTag("address", stargateEntry.getKey().serializeNBT());
				stargateCompound.setTag("pos", stargateEntry.getValue().serializeNBT());
				stargateTagList.appendTag(stargateCompound);
			}
		}

		compound.setTag("stargates", stargateTagList);

		if (netherGateAddress != null)
			compound.setTag("netherGateAddress", netherGateAddress.serializeNBT());

		if (lastActivatedOrlins != null)
			compound.setTag("lastActivatedOrlins", lastActivatedOrlins.serializeNBT());
	}


	// ---------------------------------------------------------------------------------------------------------
	// Static

	public static StargateNetwork get(World world) {
		MapStorage storage = world.mapStorage;
		StargateNetwork instance = (StargateNetwork) storage.loadData(StargateNetwork.class, DATA_NAME);

		if (instance == null) {
			instance = new StargateNetwork();
			storage.setData(DATA_NAME, instance);
		}

		return instance;
	}
}
