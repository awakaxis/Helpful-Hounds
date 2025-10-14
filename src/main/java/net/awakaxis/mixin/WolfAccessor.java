package net.awakaxis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;

@Mixin(Wolf.class)
public interface WolfAccessor {

    @Invoker("getSoundVariant")
    public Holder<WolfSoundVariant> helpfulhounds$getSoundVariant();

}
