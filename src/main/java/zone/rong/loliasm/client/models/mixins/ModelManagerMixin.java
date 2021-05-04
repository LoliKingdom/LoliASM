package zone.rong.loliasm.client.models.mixins;

import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import zone.rong.loliasm.client.models.ModelRegistry;

@Mixin(ModelManager.class)
public class ModelManagerMixin {

    @Shadow @Final private TextureMap texMap;
    @Shadow @Final private BlockModelShapes modelProvider;

    @Shadow private IRegistry<ModelResourceLocation, IBakedModel> modelRegistry;

    @Shadow private IBakedModel defaultModel;

    /**
     * @author Rongmario
     */
    @Overwrite
    public void onResourceManagerReload(IResourceManager resourceManager) {
        if (FMLClientHandler.instance().hasError()) {
            return;
        }
        ModelLoader modelbakery = new ModelLoader(resourceManager, this.texMap, this.modelProvider);
        // this.modelRegistry = modelbakery.setupModelRegistry();
        this.modelRegistry = new ModelRegistry();
        this.defaultModel = this.modelRegistry.getObject(ModelBakery.MODEL_MISSING);
        ForgeHooksClient.onModelBake((ModelManager) (Object) this, this.modelRegistry, modelbakery);
        this.modelProvider.reloadModels();
    }

    /**
     * @author Rongmario
     */
    @Overwrite
    public IBakedModel getModel(ModelResourceLocation modelLocation) {
        if (modelLocation == null) {
            return this.defaultModel;
        } else {
            IBakedModel ibakedmodel = this.modelRegistry.getObject(modelLocation);
            return ibakedmodel == null ? this.defaultModel : ibakedmodel;
        }
    }

}
