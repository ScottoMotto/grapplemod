package com.yyon.grapplinghook.entities;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import com.yyon.grapplinghook.grapplemod;
import com.yyon.grapplinghook.vec;

/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

public class magnetArrow extends grappleArrow
{
	public vec prevpos = null;
	public boolean foundblock = false;
	public boolean wasinair = false;
	public BlockPos magnetblock = null;
	public int repelconf = 0;
	
	public magnetArrow(World worldIn) {
		super(worldIn);
	}
	
	public magnetArrow(World worldIn, EntityLivingBase shooter,
			boolean righthand) {
		super(worldIn, shooter, righthand);
	}
	
	public magnetArrow(World worldIn, EntityLivingBase shooter, boolean righthand, int repelconf) {
		this(worldIn, shooter, righthand);
		this.repelconf = repelconf;
	}
	
	@Override
    public void writeSpawnData(ByteBuf data)
    {
		super.writeSpawnData(data);
		data.writeInt(this.repelconf);
    }
	
	@Override
    public void readSpawnData(ByteBuf data)
    {
		super.readSpawnData(data);
		this.repelconf = data.readInt();
    }

	
    public float getVelocity()
    {
        return 20F;
    }
    
    @Override
    public void onEntityUpdate() {
    	super.onEntityUpdate();
    	if (this.shootingEntity == null) {return;}
    	if (!this.foundblock) {
    		if (!this.world.isRemote) {
    			vec playerpos = vec.positionvec(this.shootingEntity);
    			vec pos = vec.positionvec(this);
    			if (magnetblock == null) {
	    			if (prevpos != null) {
		    			HashMap<BlockPos, Boolean> checkedset = new HashMap<BlockPos, Boolean>();
		    			vec vector = pos.sub(prevpos);
		    			vec normvector = vector.normalize();
		    			for (int i = 0; i < vector.length(); i++) {
		    				double dist = prevpos.sub(playerpos).length();
		    				int radius = (int) dist / 4;
		    				BlockPos found = this.check(prevpos, checkedset);
		    				if (found != null) {
		    					if (wasinair) {
						    		vec distvec = new vec(found.getX(), found.getY(), found.getZ());
						    		distvec.sub_ip(prevpos);
						    		if (distvec.length() < radius) {
				    					this.setPositionAndUpdate(prevpos.x, prevpos.y, prevpos.z);
				    					pos = prevpos;
				    					
				    					magnetblock = found;
				    					
				    					break;
						    		}
		    					}
		    				} else {
		    					wasinair = true;
		    				}
		    				
		    				prevpos.add_ip(normvector);
		    			}
	    			}
    			}
    			
    			if (magnetblock != null) {
			    	IBlockState blockstate = this.world.getBlockState(magnetblock);
			    	AxisAlignedBB BB = blockstate.getCollisionBoundingBox(this.world, magnetblock);

					vec blockvec = new vec(magnetblock.getX() + (BB.maxX + BB.minX) / 2, magnetblock.getY() + (BB.maxY + BB.minY) / 2, magnetblock.getZ() + (BB.maxZ + BB.minZ) / 2);
					vec newvel = blockvec.sub(pos);
					
					double l = newvel.length();
					
					newvel.changelen(this.getVelocity());
					
					this.motionX = newvel.x;
					this.motionY = newvel.y;
					this.motionZ = newvel.z;
					
					if (l < 0.2) {
						this.serverAttach(magnetblock, blockvec, EnumFacing.UP);
					}
    			}
    			
    			prevpos = pos;
    		}
    	}
    }
	
    public final int radius = 3;
    
    public BlockPos check(vec p, HashMap<BlockPos, Boolean> checkedset) {
    	BlockPos closestpos = null;
    	double closestdist = 0;
    	for (int x = (int)p.x - radius; x <= (int)p.x + radius; x++) {
        	for (int y = (int)p.y - radius; y <= (int)p.y + radius; y++) {
            	for (int z = (int)p.z - radius; z <= (int)p.z + radius; z++) {
			    	BlockPos pos = new BlockPos(x, y, z);
			    	if (pos != null) {
				    	if (hasblock(pos, checkedset)) {
				    		vec distvec = new vec(pos.getX(), pos.getY(), pos.getZ());
				    		distvec.sub_ip(p);
				    		double dist = distvec.length();
				    		if (closestpos == null || dist < closestdist) {
				    			closestpos = pos;
				    			closestdist = dist;
				    		}
				    	}
			    	}
            	}
	    	}
    	}
		return closestpos;
	}

	public boolean hasblock(BlockPos pos, HashMap<BlockPos, Boolean> checkedset) {
    	if (!checkedset.containsKey(pos)) {
    		boolean isblock = false;
	    	IBlockState blockstate = this.world.getBlockState(pos);
	    	Block b = blockstate.getBlock();
			if (!grapplemod.anyblocks && ((!grapplemod.removeblocks && !grapplemod.grapplingblocks.contains(b))
						|| (grapplemod.removeblocks && grapplemod.grapplingblocks.contains(b)))) {
			} else {
		    	if (!(b.isAir(blockstate, this.world, pos))) {
			    	AxisAlignedBB BB = blockstate.getCollisionBoundingBox(this.world, pos);
			    	if (BB != null) {
			    		isblock = true;
			    	}
		    	}
			}
			
	    	checkedset.put(pos, (Boolean) isblock);
	    	return isblock;
    	} else {
    		return checkedset.get(pos);
    	}
	}

	@Override
	protected void onImpact(RayTraceResult movingobjectposition) {
		super.onImpact(movingobjectposition);
		this.foundblock = true;
	}

	@Override
	public int getControlId() {
		return grapplemod.MAGNETID;
	}
}
