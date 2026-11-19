# AGENTS.md

libGDX 1.12.1 desktop game ("FoodFlow"). Two Gradle modules: `core/` holds all game code and depends only on the `gdx` API; `lwjgl3/` is the desktop launcher. Java 17 (`options.release = 17`), UTF-8, Gradle wrapper 8.14. `README.md` (Spanish) covers gameplay, controls, and the asset list.

## Commands

- Run the game: `./gradlew lwjgl3:run` (Windows: `gradlew.bat lwjgl3:run`). First run needs internet to download libGDX.
- Compile check: `./gradlew lwjgl3:classes`.
- There are no tests, linter, formatter, or CI. `test` exercises nothing; verify changes by compiling and playing the game.

## Architecture

- `logica/Mundo.java` owns all game state and rules and imports no libGDX. Screens call `mundo.actualizar(dt)` once per frame, then read getters.
- Screens in `pantallas/` extend `PantallaBase`, which clears the frame, applies the fixed 960x600 `FitViewport`, unprojects mouse coordinates, and handles the entry fade. Draw only between `empezarFrame()` and `terminarFrame()`.
- Flow is `MenuScreen` → `JuegoScreen` ⇄ `TiendaScreen` → `FinScreen`, switched from `render()` with `juego.setScreen(...)`.
- All drawing goes through the single shared `FoodFlowGame.pincel` (`ui/Pincel`): one SpriteBatch, shapes generated from white/circle textures, colors as `0xRRGGBB` ints. No Scene2D or UI toolkit; don't create extra SpriteBatch/BitmapFont instances.
- `ui/Dibujos` composes customers, burgers, and ingredient icons from those primitives. A new ingredient needs an entry in `Catalogo` plus handling in the private `Dibujos.capa` (burger layer) and `Dibujos.icono` (standalone icon).

## Gotchas

- On-screen text must be ASCII (no accents/ñ): the stock libGDX `BitmapFont` does not cover them. Source comments/identifiers may use accents.
- Recipes compare ingredients by identity (`plato.get(i) != receta.get(i)` in `Mundo.platoCoincide`). Always use the `Catalogo` constants; never construct an equivalent `Ingrediente`.
- Keep the 50 ms `dt` clamp in the screens when calling `Mundo.actualizar`; it prevents a window hitch from skipping game time.
- Asset lookup is case-sensitive on Linux/macOS. Code loads lowercase `madera`/`nubes`, but git currently tracks `assets/Madera.jpg` and `assets/Nubes.jpg`; name new assets in lowercase and fix that mismatch if touching them.
- Missing backgrounds are non-fatal: `FoodFlowGame.cargarFondo` tries `.jpg/.jpeg/.png/.webp`, logs to stderr, and falls back to a flat color (null texture).
- No `.gitignore` exists: `build/`, `.gradle/`, `.lwjgl/`, and editor backups (`MenuScreen.java~`, `.MenuScreen.java.un~`) are tracked. Don't stage their churn.
- Game code uses Spanish names (`Mundo`, `pantallas`, `Pincel`, `Dibujos`); follow that convention.
