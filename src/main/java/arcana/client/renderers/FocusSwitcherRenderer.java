package arcana.client.renderers;

import arcana.Arcana;
import arcana.items.WandItem;
import arcana.network.PkSwapFocus;
import arcana.util.ArrayInventory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static arcana.Arcana.arcId;

// see https://git.sleeping.town/unascribed-mods/Yttr/src/branch/1.20.1/src/main/java/diy/y2k/yttr/client/render/ui/RifleHUDRenderer.java
public final class FocusSwitcherRenderer{
	
	private static final int openAnimationTime = 9, closeAnimationTime = 10, swapAnimationTime = 5;

    private static int radius = 70;
	private static int openTicks = -1, closedTicks = -1;
	private static boolean inStorage = true;
	
	// TODO: combine with HudRenderer, or otherwise adjust for wand HUD position
	public static void renderHud(DrawContext ctx, RenderTickCounter counter){
		float delta = counter.getTickDelta(true);
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem)))
			return;
		
		if(openTicks == -1)
			return;

        int origin_x = ctx.getScaledWindowWidth()/2, origin_y = ctx.getScaledWindowHeight()/2;

        Identifier focusSwitcherId = arcId("textures/gui/hud/focus_switcher_proto.png");
        ctx.drawTexture(focusSwitcherId, origin_x-96, origin_y-96, 0, 0, 192, 192, 256, 256);

        ArrayInventory foci = WandItem.getFoci(wandStack);
        ItemStack currentFocus = WandItem.focusFrom(wandStack);

        ctx.drawItem(currentFocus, origin_x-8, origin_y-8);

        for(int i = 0; i < 4; i++)
            ctx.drawItem(foci.getStack(i),
                    (int)Math.floor(radius * Math.sin(i*(2f/4)*Math.PI)) + origin_x-8,
                    -(int)Math.floor(radius * Math.cos(i*(2f/4)*Math.PI)) + origin_y-8);

	}
	
	public static void tick(MinecraftClient client){
		MinecraftClient mc = MinecraftClient.getInstance();
		PlayerEntity player = mc.player;
		if(player == null)
			return;
		ItemStack wandStack;
		ItemStack mainHand = player.getMainHandStack(), offHand = player.getOffHandStack();
		if((!((wandStack = mainHand).getItem() instanceof WandItem) && !((wandStack = offHand).getItem() instanceof WandItem))){
			openTicks = closedTicks = -1;
			return;
		}
		
		if(mc.options.swapHandsKey.isPressed() || mc.options.swapHandsKey.wasPressed()){
			closedTicks = -1;
			openTicks++;
			// drain `timesPressed`
			//noinspection StatementWithEmptyBody
			while(mc.options.swapHandsKey.wasPressed()){}
			
			// TODO: scrolling
			if(mc.options.useKey.wasPressed()){
				mc.options.useKey.setPressed(false);
				new PkSwapFocus(true).sendToServer();
				PkSwapFocus.go(player, true, wandStack);
			}
			
			if(mc.options.attackKey.wasPressed()){
				mc.options.attackKey.setPressed(false);
				new PkSwapFocus(false).sendToServer();
				PkSwapFocus.go(player, false, wandStack);
			}
			
			if(mc.options.sneakKey.wasPressed()){
				mc.options.sneakKey.setPressed(false);
				inStorage = !inStorage;
			}
		}else{
			openTicks = -1;
			closedTicks++;
			inStorage = false;
		}
	}
}