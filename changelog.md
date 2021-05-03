# LoliASM Changelog

## 2.0
- Revamped configuration. Some new options there. **REFRESH YOUR CONFIGS**
- Removed soft/hard patch variations for optimizing BakedQuads. Hard patch remains as it is now stable and saves more RAM.
- Implemented 'cleanupLaunchClassLoader' - saves lots of memory in the `LaunchClassLoader` caching things relating to class transformation/loading. *Foamfix* does this already to some fields but I've done it on more fields.
- Implemented 'remapperMemorySaver' - saves lots of memory in `FMLDeobfuscatingRemapper` by deduplicating Strings as well as not caching non-Minecraft/Forge classes/fields/methods.
- Implemented 'optimizeDataStructures' - optimizes structures around Minecraft. This will be updated nearly every version if I find any places that gives tangible results.
- Implemented 'optimizeFurnaceRecipes' - optimizes tick time when searching for FurnaceRecipes. By Hashing recipes and queries are only a hash lookup now rather than a loop => if match => return.
- Starting to implement object canonization, or deduplication as Foamfix calls it, hopefully it will match Foamfix and beat it out. We'll see.
- Starting to implement BlockStateContainer, StateImplementation memory squashers.
- Added mixins to do some of the leg work for me as I'm too lazy to write ASM all the time.
- Cleaned up `LoliReflector`, potentially an API candidate.
- Relocated some coremod classes.

## 1.1
- Fixed issues in some cases (first found in Thaumcraft) where redirecting BakedQuad::new calls would fail because of stackframe issues.

## 1.0
- First release.
- Optimizations on BakedQuads, soft/hard patch variants.