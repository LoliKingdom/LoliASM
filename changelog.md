# LoliASM Changelog

## 2.5
- Cleanups.
- Canonicalize strings by using Java's String#intern. This utilizes Java's own implementation and as of Java 7 through 8, it has gotten better. Where using it is probably better than a custom structure.
- Now uses Forge's own config, but because of earlier-loading, it is manually baked.
- Deprecated old json config system.
- More config options! Check your new configs: `loliasm.cfg`
    - Launchwrapper optimizations into more specific configuration settings.
    - Modfixes are now more specific too.
- NBTTagString objects now have their strings canonicalized.
- Hard-dependency on MixinBooter (2.0).
- Fixes MysticalLib incompatibility (#2).

## 2.4.1
- Fixes static methods containing `BakedQuad::new` not targeted correctly therefore not being redirected.

## 2.4
- Added new module `modFixes`.
- Prevent `ArrayIndexOutOfBoundsException` from occurring in `BlockIEBase#getPushReaction` with special cases.
- Fixes incompatibility with *Wings* because Wings' coremod loads some Forge classes too early for us to mixin into.

## 2.3.1
- Fixes `bakedQuadPatchClasses` not being properly targeted by mixins. This means, LoliASM now requires MixinBooter. It was going to some time down the line anyways.
- Added `miscOptimizations` => aims to optimize smaller areas of the game, nothing game-breaking will be introduced under this.
- `FluidRegistryMixin` => first of `miscOptimizations`, quicker check @ `FluidRegistry::enableUniversalBucket`
- Some more logging

## 2.3
- Reworked config boilerplate, in the worse possible way possible. (thank you `DefaultArtifactVersion`)
- `ModCandidateMixin`: addresses large amounts of duplicate package Strings created by `ModCandidate#addClassEntry`, now it is internally backed by a Set instead and strings are added to the StringPool. `getContainedPackages` return a List constructed from the Set each time.
- New config option: optimizeBitsOfRendering.
- `VisGraphMixin`: removes unboxing business from `floodFill`, and made it use `EnumFacing::values` => `EnumFacing::VALUES`.
- ASM change to `RenderGlobal` => fixes `setupTerrain` using `EnumFacing::values` as opposed to `EnumFacing::VALUES`.
- When ChickenASM is loaded, LoliASM fixes ChickenASM caching a shit ton of superclasses and whatnot in `ClassHierarchyManager`. This isn't used in any ASMing from what I can observe. This change is EXPERIMENTAL!

## 2.2.1
- Fixed crashing when RegistrySimples are created in-world.

## 2.2
- Fixed client classes from being loaded on the server.
- Fixed DummyMap's ClassCircularityError.
- Prevented harmless errors from being logged when mixins aren't being added in certain configurations.

## 2.1
- WIP: Dynamic Model Baking.
- WIP: Speedy Fluid Rendering.
- Optifine now does not crash with LoliASM. Same effect as if you disabled `bakedQuadsSquasher` in configs. The real fix will be addressed soon:tm:.
- I wrote a mixin with ASM, counter-intuitive?
- Added branding, don't be pissed off :^).
- Cleanups.

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