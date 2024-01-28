package zone.rong.blahajasm.client.models.bakedquad.mixins;

import com.google.gson.GsonBuilder;
import epicsquid.mysticallib.hax.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@Pseudo
@Mixin(value = Hax.class, remap = false)
public class HaxMixin {

    @Shadow public static Field field_ModelBakery_blockModelShapes;

    /**
     * @author Rongmario
     * @reason What the fuck? Half of these fields aren't even being used, and most of their application fucking sucks.
     */
    @Overwrite
    public static void init() throws IllegalAccessException, NoSuchFieldException, SecurityException, IllegalArgumentException {
        Field f = ObfuscationReflectionHelper.findField(ModelBlock.class, "field_178319_a");
        f.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
        f.set(null, new GsonBuilder()
                .registerTypeAdapter(ModelBlock.class, new ModelBlock.Deserializer())
                .registerTypeAdapter(BlockPart.class, new BPDeserializer())
                .registerTypeAdapter(BlockPartFace.class, new BPFDeserializer())
                .registerTypeAdapter(BlockFaceUV.class, new BFUVDeserializer())
                .registerTypeAdapter(ItemTransformVec3f.class, new ITV3FDeserializer())
                .registerTypeAdapter(ItemCameraTransforms.class, new ICTDeserializer())
                .registerTypeAdapter(ItemOverride.class, new IODeserializer())
                .create());
        field_ModelBakery_blockModelShapes = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177610_k");
        field_ModelBakery_blockModelShapes.setAccessible(true);
    }

}
