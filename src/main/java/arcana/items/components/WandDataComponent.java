package arcana.items.components;

import arcana.api.Cap;
import arcana.api.Core;
import arcana.aspects.AspectMap;
import arcana.items.WandItem;
import arcana.util.ArrayInventory;
import arcana.util.MathUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class WandDataComponent{
	
	public static final Codec<WandDataComponent> CODEC = RecordCodecBuilder.create(i -> i.group(
			Cap.CODEC.lenientOptionalFieldOf("cap", Cap.MISSING_CAP).forGetter(x -> x.cap),
			Core.CODEC.lenientOptionalFieldOf("core", Core.MISSING_CORE).forGetter(x -> x.core),
            ArrayInventory.CODEC.fieldOf("foci").forGetter(x -> x.foci),
            Codec.INT.fieldOf("current_focus").forGetter(x -> x.currentFocusIdx),
			AspectMap.CODEC.fieldOf("stored").forGetter(x -> x.stored)
	).apply(i, WandDataComponent::new));
	
	public Cap cap;
	public Core core;
    public ArrayInventory foci;
    public int currentFocusIdx;
	public AspectMap stored;

	public WandDataComponent(Cap cap, Core core, ArrayInventory foci, int currentFocusIdx, AspectMap stored){
		this.cap = cap != null ? cap : Cap.MISSING_CAP;
		this.core = core != null ? core : Core.MISSING_CORE;
        this.foci = foci;
        this.currentFocusIdx = currentFocusIdx;
		this.stored = stored;
	}
	
	public WandDataComponent copy(){
		return new WandDataComponent(cap, core, foci, currentFocusIdx, stored);
	}
	
	public static WandDataComponent createDefault(){
		return new WandDataComponent(Cap.MISSING_CAP, Core.MISSING_CORE, new ArrayInventory(WandItem.FOCI_SIZE), 0, new AspectMap());
	}

	public static WandDataComponent withCapAndCore(Cap cap, Core core){
		return new WandDataComponent(cap, core, new ArrayInventory(WandItem.FOCI_SIZE), 0, new AspectMap());
	}
	
	public static <T> T apply(ItemStack stack, Function<WandDataComponent, T> body){
		WandDataComponent copy = stack.getOrDefault(ArcanaDataComponents.WAND_DATA, WandDataComponent.createDefault()).copy();
		T ret = body.apply(copy);
		stack.set(ArcanaDataComponents.WAND_DATA, copy);
		return ret;
	}
	
	public static void run(ItemStack stack, Consumer<WandDataComponent> body){
		WandDataComponent.apply(stack, component -> {
			body.accept(component);
			return null;
		});
	}

    public void setFocus(ItemStack focus){
        setFocusAt(currentFocusIdx, focus);
    }
    public void setFocusAt(int i, ItemStack focus){
        foci.setStack(i, focus);
    }

    public ItemStack getFocus(){
        return getFocusAt(currentFocusIdx);
    }

    public ItemStack getFocusAt(int i){
        return foci.getStack(i);
    }

    public int getFocusSlot(){
        return currentFocusIdx;
    }

    public void setFocusIdx(int i){
        currentFocusIdx = Math.clamp(i, 0, WandItem.FOCI_SIZE - 1);
    }

    public void cycleCurrentFocus(int i){
        setFocusIdx(MathUtil.posMod(currentFocusIdx + i, WandItem.FOCI_SIZE));
    }

    public void incrementCurrentFocus(){
        cycleCurrentFocus(1);
    }

    public void decrementCurrentFocus(){
        cycleCurrentFocus(-1);
    }
}