# Changelog

## [2.0.0] - Unreleased

_A redesign: the implementation is hidden behind the exported packages `io.github.sugarcubes.cloner` and `io.github.sugarcubes.cloner.spi`, the low-level mechanisms form a toolset chosen for the running JVM, copiers are generated at runtime or at build time._

### Changed

- **Breaking:** export only `io.github.sugarcubes.cloner` and `io.github.sugarcubes.cloner.spi`, move the implementation to internal packages ([`f34cdac`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/f34cdac), [`d84221d`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/d84221d), [`2c7ca7b`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/2c7ca7b), [`f187ff0`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/f187ff0)) (Maxim Butov)
- **Breaking:** rename `Cloners.reflection()` to `Cloners.defaults()` and `Cloners.reflectionClone()` to `Cloners.clone()` ([`f9c96db`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/f9c96db)) (Maxim Butov)
- **Breaking:** replace `objectFactoryProvider()`, `fieldCopierFactory()` and `unsafe()` of the builder with `toolset(Toolset)`: allocation, field access and module opening ([`3c77e0c`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/3c77e0c), [`193ccba`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/193ccba), [`224a7eb`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/224a7eb), [`e9d8160`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/e9d8160)) (Maxim Butov)
- **Breaking:** choose the default toolset by the running JVM: `Unsafe` field access unless its memory access is denied, `Unsafe` or `ReflectionFactory` allocation, module opening by the agent, `Unsafe` or Narcissus; on JDK 24+ the JVM warns about `sun.misc.Unsafe` ([`7e2e9d8`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/7e2e9d8)) (Maxim Butov)
- **Breaking:** fail with `UncopyableObjectException` on threads, references, open files, sockets, channels, processes, timers and executors unless a copy action is chosen for them; share `ThreadLocal`, `ClassValue` and `ProcessHandle` ([`27dcf64`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/27dcf64)) (Maxim Butov)
- **Breaking:** create instances without constructors by `sun.reflect.ReflectionFactory`, call constructors only as an explicit fallback ([`460d7be`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/460d7be), [`7b69701`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/7b69701)) (Maxim Butov)
- Copy lambdas and instances of hidden classes by reference ([`582034b`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/582034b)) (Maxim Butov)
- Clone records in reference cycles, copy record components completely ([`ef3e4dc`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/ef3e4dc), [`33a7d8a`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/33a7d8a)) (Maxim Butov)
- Speed up copying: identity maps with open addressing, smaller slots, fewer allocations per object, values of immutable final types copied as is ([`a352153`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/a352153), [`f98b239`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/f98b239), [`6f6f0aa`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/6f6f0aa), [`078d586`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/078d586), [`943b8d1`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/943b8d1), [`18cc9b3`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/18cc9b3), [`92d2bbb`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/92d2bbb), [`a9e4e7a`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/a9e4e7a), [`4b65f31`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/4b65f31)) (Maxim Butov)

### Added

- Add `Toolset` with the profiles of `Toolsets` (`defaults()`, `conservative()`, `reflection()`, `unsafe()`, `narcissus()`) and `ToolsetBuilder` ([`224a7eb`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/224a7eb), [`e9d8160`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/e9d8160), [`155a928`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/155a928)) (Maxim Butov)
- Add module opening by `Unsafe`, by Narcissus and by the cloner agent (`ModuleOpening`), working on JDK 26 ([`0d949ea`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/0d949ea), [`d972ce6`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/d972ce6)) (Maxim Butov)
- Add generated copiers, enabled by default (`generatedCopiers(boolean)`): hidden classes on JDK 15+, anonymous classes on JDK 8-14, written by the ClassFile API on JDK 24+ or by ASM ([`c36497d`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/c36497d), [`a6ab15b`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/a6ab15b), [`615f9c3`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/615f9c3), [`9d8a61e`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/9d8a61e), [`ff1bf09`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/ff1bf09), [`98ac3bc`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/98ac3bc)) (Maxim Butov)
- Clone classes by copy constructors added at build time (`copyConstructors(boolean)`) ([`d368870`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/d368870)) (Maxim Butov)
- Add the module `sugar-cubes-cloner-tools`: an annotation processor, a Maven plugin and the Gradle plugin `io.github.sugar-cubes.cloner` which add copy constructors ([`c0d8467`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/c0d8467), [`45a7f47`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/45a7f47), [`cfda379`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/cfda379)) (Maxim Butov)
- Clone common object graphs on JDK 16+ without `--add-opens` ([`e313a29`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/e313a29)) (Maxim Butov)
- Add the documentation site with the Javadoc ([`8176350`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/8176350)) (Maxim Butov)

### Removed

- **Breaking:** remove Objenesis support (`ObjenesisObjectFactoryProvider`): `ReflectionFactory` is the same mechanism without a dependency ([`320bd0e`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/320bd0e)) (Maxim Butov)

### Fixed

- Return the only non-default policy from `CopyPolicy.compound()` ([`bf5d7e6`](https://github.com/sugar-cubes/sugar-cubes-cloner/commit/bf5d7e6)) (Maxim Butov)

_Releases before 2.0.0 are described on [GitHub](https://github.com/sugar-cubes/sugar-cubes-cloner/releases)._

[2.0.0]: https://github.com/sugar-cubes/sugar-cubes-cloner/releases/tag/v2.0.0
