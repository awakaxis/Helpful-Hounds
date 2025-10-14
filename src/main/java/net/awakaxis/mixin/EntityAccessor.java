package net.awakaxis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {

    @Accessor("random")
    RandomSource helpfulhounds$getRandom();

}
