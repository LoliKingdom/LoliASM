package zone.rong.loliasm.common.modfixes.crafttweaker.mixins;

import java.util.Map;

import crafttweaker.api.data.IData;
import crafttweaker.mc1120.data.NBTConverter;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import zone.rong.loliasm.api.datastructures.LoliTagMap;

@Mixin(value = NBTConverter.class, remap = false)
public class NBTConverterMixin {
    @ModifyVariable(method = "from(Lnet/minecraft/nbt/NBTBase;Z)Lcrafttweaker/api/data/IData;",
            slice = @Slice(from = @At(value = "NEW", target = "()Ljava/util/HashMap;")),
            at = @At(value = "STORE", opcode = Opcodes.ASTORE), ordinal = 0)
    private static Map<String, IData> replaceDefaultHashMap(Map<String, IData> original) {
        return new LoliTagMap<>();
    }
}
