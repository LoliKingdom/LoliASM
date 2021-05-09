package zone.rong.loliasm.common.memory.mixins;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import zone.rong.loliasm.api.StringPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(value = ModCandidate.class, remap = false)
public class ModCandidateMixin {

    @Shadow private Set<String> foundClasses;
    @Shadow private ASMDataTable table;

    @Unique private final Set<String> packagesSet = new ObjectOpenHashSet<>();

    /**
     * @author Rongmario
     * @reason Canonize package strings. This also helps canonize those that use getOwnedPackages() indirectly (e.g - LibrarianLib)
     */
    @Overwrite
    public void addClassEntry(String name) {
        String className = name.substring(0, name.lastIndexOf('.')); // strip the .class
        foundClasses.add(className);
        className = className.replace('/','.');
        int pkgIdx = className.lastIndexOf('.');
        if (pkgIdx > -1) {
            String pkg = StringPool.canonize(className.substring(0, pkgIdx));
            packagesSet.add(pkg);
            this.table.registerPackage((ModCandidate) (Object) this, pkg);
        }
    }

    /**
     * @author Rongmario
     * @reason packagesSet => ArrayList
     */
    @Overwrite
    public List<String> getContainedPackages() {
        return new ArrayList<>(packagesSet);
    }

}
