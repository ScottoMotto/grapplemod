package com.yyon.grapplinghook;

import com.yyon.grapplinghook.controllers.grappleController;
import com.yyon.grapplinghook.entities.RenderGrappleArrow;
import com.yyon.grapplinghook.entities.grappleArrow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class ClientProxyClass extends CommonProxyClass {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
	}
	
	@Override
	public void init(FMLInitializationEvent event, grapplemod grappleModInst) {
		super.init(event, grappleModInst);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(grapplemod.grapplebowitem, 0, new ModelResourceLocation("grapplemod:grapplinghook", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(grapplemod.hookshotitem, 0, new ModelResourceLocation("grapplemod:hookshot", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(grapplemod.launcheritem, 0, new ModelResourceLocation("grapplemod:launcheritem", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(grapplemod.longfallboots, 0, new ModelResourceLocation("grapplemod:longfallboots", "inventory"));
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(grapplemod.enderhookitem, 0, new ModelResourceLocation("grapplemod:enderhook", "inventory"));
		RenderingRegistry.registerEntityRenderingHandler(grappleArrow.class, 
				new RenderGrappleArrow(Minecraft.getMinecraft().getRenderManager(), Items.iron_pickaxe, Minecraft.getMinecraft().getRenderItem()));
		
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
	}
	
	/*
	@Override
	public void sendplayermovementmessage(grappleArrow grappleArrow, int playerid, int arrowid) {
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player.getEntityId() == playerid) {
			grapplemod.network.sendToServer(new PlayerMovementMessage(arrowid, player.moveStrafing, player.moveForward, ((EntityPlayerSP) player).movementInput.jump));
			grappleArrow.receivePlayerMovementMessage(player.moveStrafing, player.moveForward, ((EntityPlayerSP) player).movementInput.jump);
		}
	}
	*/
	
	@Override
	public void getplayermovement(grappleController control, int playerid) {
//		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
//		if (player == null) {
//			System.out.println("NULL PLAYER!");
//			return;
//		}
		Entity entity = control.entity;
		if (entity instanceof EntityPlayerSP) {
//		if (player.getEntityId() == playerid) {
			EntityPlayerSP player = (EntityPlayerSP) entity;
			control.receivePlayerMovementMessage(player.moveStrafing, player.moveForward, player.movementInput.jump);
		}
	}
	
	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event) {
		for (grappleController controller : grapplemod.controllers.values()) {
			controller.doClientTick();
		}
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		resetlaunchertime(player);
	}
}