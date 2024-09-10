package com.mrjake.aunis.raycaster;

import com.mrjake.aunis.raycaster.util.Box;
import com.mrjake.aunis.raycaster.util.DHDVertex;
import com.mrjake.aunis.raycaster.util.Ray;
import com.mrjake.aunis.util.minecraft.BlockPos;
import com.mrjake.aunis.util.minecraft.Vec3d;
import com.mrjake.vector.Vector2f;
import com.mrjake.vector.Vector3f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class Raycaster {
	protected abstract List<Vector3f> getVertices();
	protected abstract int getRayGroupCount();

	protected abstract Vector3f getTranslation(World world, BlockPos pos);

	protected abstract void check(World world, BlockPos pos, EntityPlayer player, int x, int i);
	protected abstract boolean brbCheck(List<Ray> brbRayList, Vec3d lookVec, EntityPlayer player, BlockPos pos);

	public boolean onActivated(World world, BlockPos pos, EntityPlayer player, float rotation) {
		// Last common function, x=a, y=b
		Ray lastRay = null;
		Ray firstRay = null;
		List<Ray> brbRayList = new ArrayList<Ray>();
		Vec3d lookVec = new Vec3d(player.getLookVec().xCoord, player.getLookVec().yCoord, player.getLookVec().zCoord);

		boolean found = false;

		for (int x=1; x<=getVertices().size()/getRayGroupCount(); x++) {
		//for (int x=1; x<=1; x++) {
			Ray currentRay;

			// Last run, current ray should be the first one
			if (x == getVertices().size()/getRayGroupCount())
				currentRay = firstRay;
			else
				currentRay = new Ray( getTransposedRay(x, rotation, world, pos, player) );

			// First run, we need to calculate the first right limiter function
			if (lastRay == null) {
				lastRay = firstRay = new Ray( getTransposedRay(0, rotation, world, pos, player) );
				//brbRayList.add(firstRay);
			}

			List<Ray> transverseRays = new ArrayList<Ray>();

			for (int i=0; i<getRayGroupCount(); i++) {
				Ray r = new Ray( currentRay.getVert(i), lastRay.getVert(i) );
				transverseRays.add( r );

				if ( i == 2 ) {
					brbRayList.add( r );
				}
			}

			for (int i=0; i<getRayGroupCount()-1; i++) {
				Box box = new Box(currentRay, lastRay, transverseRays.get(i), transverseRays.get(i+1), i);

				if (box.checkForPointInBox( new Vector2f( (float)lookVec.x, (float)lookVec.z ) )) {
//					int button = x-1;
//					if (i>0)
//						button += 19;
//
//					Aunis.info("i:"+i+" x:"+x+" btn:"+button);

					check(world, pos, player, x, i);

					found = true;
					break;
				}
			}

			if (found)
				break;

			lastRay = currentRay;
		}

		found |= brbCheck(brbRayList, lookVec, player, pos);

		return found;
	}

	private Vector2f getTransposed(Vector3f v, float rotation, World world, BlockPos pos, EntityPlayer player) {
		DHDVertex current = new DHDVertex(v.x, v.y, v.z);

		return current.rotate( rotation ).localToGlobal(pos, getTranslation(world, pos)).calculateDiffrence(player).getViewport( new Vec3d(player.getLookVec().xCoord, player.getLookVec().yCoord, player.getLookVec().zCoord) );
	}

	// Ray = set of getRayGroupCount() vertices
	private List<Vector2f> getTransposedRay(int rayIndex, float rotation, World world, BlockPos pos, EntityPlayer player) {
		List<Vector2f> out = new ArrayList<Vector2f>();

		for (int i=0; i<getRayGroupCount(); i++) {
			out.add( getTransposed( getVertices().get(rayIndex*getRayGroupCount() + i), rotation, world, pos, player) );
		}

		return out;
	}
}
