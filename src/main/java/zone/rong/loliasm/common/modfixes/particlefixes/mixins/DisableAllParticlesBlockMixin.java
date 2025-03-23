package zone.rong.loliasm.common.modfixes.particlefixes.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
        "mrtjp.projectred.illumination.BlockLamp",
        "mrtjp.projectred.expansion.BlockMachine",
        "mrtjp.projectred.relocation.BlockFrame",
        "mrtjp.projectred.exploration.BlockDecorativeWall",
        "mrtjp.projectred.fabrication.BlockICMachine",
        "thefloydman.moremystcraft.block.BlockLockedLectern",
        "thefloydman.moremystcraft.block.BlockLockedBookstand",
        "buildcraft.lib.engine.BlockEngineBase_BC8",
        "buildcraft.factory.block.BlockDistiller"
})
public abstract class DisableAllParticlesBlockMixin extends Block {

    protected DisableAllParticlesBlockMixin(Material material) {
        super(material);
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {
        return true;
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World world, BlockPos pos, Entity entity) {
        return true;
    }

    @Override
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

}
