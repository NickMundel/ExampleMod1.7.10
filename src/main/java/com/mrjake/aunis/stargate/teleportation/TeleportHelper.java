package com.mrjake.aunis.stargate.teleportation;

import com.mrjake.aunis.stargate.network.StargatePos;
import com.mrjake.aunis.tileentity.stargate.StargateAbstractBaseTile;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.EnumFacing;
import com.mrjake.aunis.util.minecraft.MathHelper;
import com.mrjake.aunis.util.minecraft.Vec3d;
import com.mrjake.vector.Matrix2f;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeHooks;

import javax.vecmath.Vector2f;
import java.util.List;

public class TeleportHelper {

	enum EnumFlipAxis {
		X(0x01),
		Z(0x02);

		public int mask;

		EnumFlipAxis(int mask) {
			this.mask = mask;
		}

		public static boolean masked(EnumFlipAxis flipAxis, int in) {
			return (in & flipAxis.mask) != 0;
		}
	}

	private static void translateTo00(Vector2f center, Vector2f v) {
		v.x -= center.x;
		v.y -= center.y;
	}

	public static void rotateAround00(Vector2f v, float rotation, int flip) {
		Matrix2f m = new Matrix2f();
		Matrix2f p = new Matrix2f();

		float sin = MathHelper.sin(rotation);
		float cos = MathHelper.cos(rotation);

		if ( EnumFlipAxis.masked(EnumFlipAxis.X, flip) )
			v.x *= -1;

		if ( EnumFlipAxis.masked(EnumFlipAxis.Z, flip) )
			v.y *= -1;
		/*if (flip != null) {
			if ( flip == Axis.X )
				v.x *= -1;
			else
				v.y *= -1;
		}*/

		m.m00 = cos;	m.m10 = -sin;
		m.m01 = sin;	m.m11 =  cos;
		p.m00 = v.x;	p.m10 = 0;
		p.m01 = v.y;	p.m11 = 0;

		Matrix2f out = Matrix2f.mul(m, p, null);

		v.x = out.m00;
		v.y = out.m01;
	}

	private static void translateToDest(Vector2f v, Vector2f dest) {
		v.x += dest.x;
		v.y += dest.y;
	}

	public static void teleportEntity(Entity entity, BlockPos sourceGatePos, StargatePos targetGatePos, float rotation, Vector2f motionVector) {
		List<Entity> passengers = null;

		if (entity.isRiding())
			return;

        if (entity.riddenByEntity != null) {
            Entity passenger = entity.riddenByEntity;
            entity.riddenByEntity = null;
            passenger.mountEntity(null);

            teleportEntity(passenger, sourceGatePos, targetGatePos, rotation, motionVector);
        }

        World world = entity.worldObj;
		int sourceDim = world.provider.dimensionId;

		StargateAbstractBaseTile sourceTile = (StargateAbstractBaseTile) world.getTileEntity(sourceGatePos.getX(), sourceGatePos.getY(), sourceGatePos.getZ());
		StargateAbstractBaseTile targetTile = targetGatePos.getTileEntity();

		int flipAxis = 0;

		if (sourceTile.getFacing().getAxis() == targetTile.getFacing().getAxis())
			flipAxis |= EnumFlipAxis.X.mask;
		else
			flipAxis |= EnumFlipAxis.Z.mask;

        BlockPos pos = null;
		BlockPos tPos = targetGatePos.gatePos;

		//if (sourceTile instanceof StargateOrlinBaseTile)
		//	pos = new Vec3d(tPos.getX() + 0.5, tPos.getY() + 2.0, tPos.getZ() + 0.5);
		//else if (targetTile instanceof StargateOrlinBaseTile)
		//	pos = new Vec3d(tPos.getX() + 0.5, tPos.getY() + 0.5, tPos.getZ() + 0.5);
        //else
			pos = getPosition(entity, sourceTile.getGateCenterPos(), targetTile.getGateCenterPos(), rotation, targetTile.getFacing().getAxis()== EnumFacing.Axis.Z ? ~flipAxis : flipAxis);

        final float yawRotated;
        if (entity.riddenByEntity != null && entity.riddenByEntity instanceof EntityLivingBase) {
            yawRotated = getRotation((EntityLivingBase) entity.riddenByEntity, rotation, flipAxis);
        } else {
            yawRotated = getRotation(entity, rotation, flipAxis);
        }
		boolean isPlayer = entity instanceof EntityPlayerMP;

		if (sourceDim == targetGatePos.dimensionID) {
			setRotationAndPosition(entity, yawRotated, pos);
		}

		else {
			final BlockPos posFinal = pos;

            if (isPlayer) {
                EntityPlayerMP player = (EntityPlayerMP) entity;
                player.mcServer.getConfigurationManager().transferPlayerToDimension(player, targetGatePos.dimensionID, new Teleporter(player.mcServer.worldServerForDimension(targetGatePos.dimensionID)) {
                    @Override
                    public void placeInPortal(Entity entity, double x, double y, double z, float yaw) {
                        setRotationAndPosition(entity, yawRotated, posFinal);
                    }
                });
            }

            else {
                entity.travelToDimension(targetGatePos.dimensionID);
                setRotationAndPosition(entity, yawRotated, posFinal);
            }
		}

		setMotion(entity, rotation, motionVector);

		sourceTile.entityPassing(entity, false);
		targetTile.entityPassing(entity, true);

        if (entity.riddenByEntity != null) {
            Entity passenger = entity.riddenByEntity;
            passenger.mountEntity(entity);
        }
	}

    public static void teleportWithRiders(Entity entity, float yawRotated, BlockPos pos) {
        if (entity.riddenByEntity != null) {
            Entity entity2 = entity.riddenByEntity;
            setRotationAndPosition(entity2, yawRotated, pos);
        }

        setRotationAndPosition(entity, yawRotated, pos);
    }

    public static void setRotationAndPosition(Entity entity, float yawRotated, BlockPos pos) {
        entity.rotationYaw = yawRotated;
        entity.setPosition(pos.getX(), pos.getY(), pos.getZ());
        entity.worldObj.updateEntityWithOptionalForce(entity, true);
    }

	public static float getRotation(Entity player, float rotation, int flipAxis) {
		Vec3 lookVec = player.getLookVec();
		Vector2f lookVec2f = new Vector2f( (float)(lookVec.xCoord), (float)(lookVec.zCoord) );

		rotateAround00(lookVec2f, rotation, flipAxis);

		return (float) Math.toDegrees(MathHelper.atan2(lookVec2f.x, lookVec2f.y));
	}

	public static void setMotion(Entity player, float rotation, Vector2f motionVec2f) {
		if (motionVec2f != null) {
			rotateAround00(motionVec2f, rotation, 0);

			player.motionX = motionVec2f.x;
			player.motionZ = motionVec2f.y;
			player.velocityChanged = true;
		}
	}

	public static BlockPos getPosition(Entity player, BlockPos sourceGatePos, BlockPos targetGatePos, float rotation, int flipAxis) {
		Vector2f sourceCenter = new Vector2f( sourceGatePos.getX()+0.5f, sourceGatePos.getZ()+0.5f );
		Vector2f destCenter = new Vector2f( targetGatePos.getX()+0.5f, targetGatePos.getZ()+0.5f );
		Vector2f playerPosition = new Vector2f( (float)(player.posX), (float)(player.posZ) );

		translateTo00(sourceCenter, playerPosition);
		rotateAround00(playerPosition, rotation, flipAxis);
		translateToDest(playerPosition, destCenter);

		float y = (float) (targetGatePos.getY() + ( player.posY - sourceGatePos.getY() ));
		return new BlockPos(playerPosition.x, y, playerPosition.y);
	}

    public static World getWorldObj(int dimension) {
        World world = DimensionManager.getWorld(0);

        if (dimension == 0)
            return world;

        return MinecraftServer.getServer().worldServerForDimension(dimension);
    }

	public static boolean frontSide(EnumFacing sourceFacing, Vector2f motionVec) {
		EnumFacing.Axis axis = sourceFacing.getAxis();
		EnumFacing.AxisDirection direction = sourceFacing.getAxisDirection();
		float motion;

		if (axis == EnumFacing.Axis.X)
			motion = motionVec.x;
		else
			motion = motionVec.y;

		// If facing positive, then player should move negative
		if (direction == EnumFacing.AxisDirection.POSITIVE)
			return motion <= 0;
		else
			return motion >= 0;
	}

}
