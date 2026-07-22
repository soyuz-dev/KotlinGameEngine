# Bump

A 2D game engine built in Kotlin with LWJGL. Designed for a DSL-driven workflow where entities own their data directly — no ECS, no ceremony.

## Status

Feature-complete for 2D games. The engine runs, the commit messages are unhinged, the DSL is next.

## Quickstart

See [GUIDE.md](GUIDE.md) for a quickstart to using Bump.

### What works
- **Application & Windowing:** `Application` object owns GLFW lifecycle and global config. `Window` is a pure GLFW wrapper with mutable properties and recursion-safe callbacks. `WindowManager` orchestrates multiple windows with context switching and per-window input routing.
- **Engine:** `RuntimeEngine` scopes `UISystem`, `UI` factory, `Input`, timers (`everyFrame`, `forEvery`, `after`, `during`), and the `Dynamic` update loop. No GLFW knowledge. Fixed-timestep physics.
- **Rendering:** Modern OpenGL 3.3 core profile, shader-based (`Shader`, `Mesh`, `Camera`), `Painter` interface with `SolidColor`, `ImagePainter`, and `TextPainter`, `DynamicPainter` for animated visuals, VAO/VBO circle/quad/triangle meshes with UV support, STB-based `Texture` loading, STB TrueType `Font` rendering, context-aware `Assets` caching, alpha blending, `RenderSystem` abstraction, `Camera` with offset support for screen shake
- **Audio:** OpenAL-based `AudioSystem`, `AudioClip` (OGG via STB Vorbis), `AudioSource` with volume/pitch/looping, spatial audio support
- **Input:** Per-window `Input` instance wrapping `KeyListener`/`MouseListener` with window-handle-keyed state
- **UI System:** `Interactive` interface with decorator chain (`clickable`, `hoverable`, `draggable`, `scrollable`, `focusable`, `doubleClickable`, `keyboardInput`), `UISystem` input router with hover/press/drag/focus/scroll state management, `UI` convenience factory (`button`, `label`, `slider`, `textInput`), hit-testing via existing collider infrastructure, `UIState` per entity, `Interactive.empty { }` + `Interactive { }` invoke shorthand
- **Math:** `Vector2D` with infix `dot` and `cross`, `Transform` with local↔world helpers, `Polynomial` class with arithmetic/calculus, `Easing` library (30+ curves, polynomial-backed), `MathUtil` with `modelMatrix` and `orthoMatrix`, `Color` utility
- **Shapes:** `CircleShape`, `RectangleShape`, `TriangleShape` with `equilateral` and `isosceles` factories, `Aabb2D`, `ShapeQueries`
- **Physics bodies:** `PointMass` (Verlet integration), `KinematicBody`, `RigidBody` (linear + angular with moment of inertia)
- **Force fields:** `ConstantForceField`, `ConstantAccelerationField`, `VelocityForceField` (polynomial-based drag/thrust), `GravityField` (n-body with IEEE 754 bit-hack exponent optimization), `EntityAwareForceField` interface for multi-body forces
- **Joints:** `RodJoint` (hard constraint with Baumgarte stabilization), `RopeJoint` (one-way constraint), `SpringJoint` (soft constraint with damping), sealed `Joint` interface with `StrictJoint` and `PermissiveJoint`
- **Collision detection:** `CircleCollider`, `RectangleCollider` with full SAT and clipped-edge contact points, `TriangleCollider` with SAT and barycentric point test, circle-rect closest-point, circle-triangle, bounding circle broadphase
- **Collision resolution:** Impulse-based with positional correction, friction, restitution, angular effects for rigidbodies, approach-speed threshold for resting contacts, CCD contact tracking with ENTER/EXIT/STAY events
- **CCD:** Swept circle-vs-AABB with substepping and corner mitigation (tested to 300k+ px/s without tunneling)
- **Event bus:** `RuntimeEventBus` with type-based pub/sub, `CollisionEvent` with `CollisionEventType` (ENTER/EXIT/STAY)
- **Scene graph:** `Scene`, `RuntimeScene`, entity management
- **Observable entities:** Properties like `position`, `rotation`, and `shape` fire change listeners for reactive updates
- **Timer system:** Deferred registration via `pendingTimers` to prevent concurrent modification during iteration. `everyFrame`, `forEvery`, `after`, `during`.
- **Debug utility:** Zero-overhead inline logging via `Debug` object
- **Cross-platform:** Windows, Linux, macOS (Intel + Apple Silicon) via Gradle build with auto-detected natives

### In progress
- DSL (`engine { scene { ... } }`)
- Spatial hash broadphase
- `PainterModifier` system (tint, opacity, clip)
- Rotatable windows (see [IDEAS.md](IDEAS.md) — architecturally supported by `Window`/`WindowManager` split)

### Demos
- **Main.kt (BrickPit):** Full physics sandbox. Click to spawn balls and rigidbody bricks with angular physics, friction, and CCD collision. Drag entities with mouse. Collision sounds on impact.
- **AVTest.kt:** Multimedia demo. Clickable cat/dog with sounds, FPS counter, title text, pulsing circle, styled play button with camera shake, dynamic window resizing and repositioning with easing curves.
- **DragTest.kt:** UI interaction demo. Draggable panel with cat texture, draggable circle with hover effects, volume slider, text input field with cursor navigation.
- **TriangleTest.kt:** Triangle rendering and collision. Static colored triangles, draggable triangle, pulsing triangle, continuously rotating triangle.
- **FirstGame.kt (Asteroids):** Playable Asteroids clone. Ship with thrust/rotation/shooting, asteroids with size-based splitting, screen wrapping, score tracking.
- **MultiWindowTest.kt:** Two independent windows running simultaneously via `WindowManager` — rotating triangle in one, pulsing circle in the other.

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

Or build a fat jar:
```bash
./gradlew jar
java -jar build/libs/KotlinGameEngine.jar
```

## Architecture

```
org.soyuz
├── engine
│   ├── audio          — AudioSystem, AudioClip, AudioSource
│   ├── collision      — Collider, CircleCollider, RectangleCollider, TriangleCollider, CollisionSystem, Contact
│   ├── core           — Application (object), Engine, RuntimeEngine (timers, update, render)
│   ├── entity         — GameEntity, DefaultGameEntity (observable properties)
│   ├── events         — EventBus, RuntimeEventBus, CollisionEvent, CollisionEventType
│   ├── physics
│   │   ├── forcefields — ForceField, EntityAwareForceField, ConstantForceField, ConstantAccelerationField, VelocityForceField, GravityField
│   │   └── joints     — Joint, StrictJoint, PermissiveJoint, RodJoint, RopeJoint, SpringJoint
│   ├── physics        — PhysicsBody, PointMass, RigidBody, KinematicBody, PhysicsSystem
│   ├── render
│   │   ├── image      — Texture, ImagePainter
│   │   └── text       — Font, TextPainter
│   ├── render         — Shader, Mesh, Camera, Painter, SolidColor, DynamicPainter, RenderSystem, RuntimeRenderSystem
│   ├── scene          — Scene, RuntimeScene
│   └── ui             — Interactive, InteractiveDecorator, UISystem, UIState, UI (factory), TextInputEntity
├── input              — Input (per-window instance), KeyListener, MouseListener (window-handle-keyed)
├── util
│   └── math           — Vector2D, Transform, Polynomial, Easing, MathUtil
├── util               — Aabb2D, ShapeQueries, Assets (context-aware), Color, Debug, Dynamic
└── windowing          — Window, WindowManager, WindowRuntime
```

## Design decisions

- **Doubles everywhere.** No floats. `Vector2D`, `Transform`, `Aabb2D` all use `Double`. Floats only at the GL/AL boundary.
- **Immutable data where possible.** `Vector2D` operators return new instances. `Transform` is a data class with `copy()`.
- **Bodies own their forces.** No global force registry. Bodies hold a list of `ForceField` instances. `EntityAwareForceField` for multi-body interactions.
- **Physics is multi-phase.** Force accumulation → permissive joint forces → CCD position update → discrete collision detection → impulse resolution + friction + positional correction → strict joint solving (5 iterations) → velocity finalization → entity-aware field position updates.
- **No ECS.** Entities are objects with direct references to their data. Same `GameEntity` type used for physics objects and UI elements.
- **Application owns the platform.** `Application` object manages GLFW init/terminate and audio lifecycle. `WindowManager` orchestrates multiple windows. `RuntimeEngine` knows nothing about GLFW.
- **Engine-scoped dependencies.** `UISystem`, `UI`, and `Input` are per-engine instances, not singletons. Each window has fully isolated UI state.
- **Per-window input.** `KeyListener` and `MouseListener` key state by window handle. `Input` wraps them for a clean per-engine API.
- **Mass-zero means infinite mass.** No separate `isStatic` boolean. `mass = 0.0` → immovable.
- **CCD by default.** Circle bodies use swept collision against AABBs with substepping. No tunneling.
- **Painter for rendering.** Entities carry their own appearance via the `Painter` interface. `SolidColor`, `ImagePainter`, and `TextPainter` implementations. `DynamicPainter` for per-frame updates.
- **Interactive decorator chain.** UI behavior composed via chained modifiers: `.clickable { }.hoverable { }.draggable { }`. `Interactive { }` shorthand for starting chains. No subclass explosion.
- **Observable entities.** Properties like `position`, `rotation`, and `shape` fire change listeners. Labels follow panels, sliders update readouts — no per-frame polling.
- **Polynomials for curves.** `Polynomial` class with full arithmetic and calculus powers easing functions, force curves, and animation blending.
- **Context-aware Assets.** `Assets` caches shaders, textures, and fonts keyed by OpenGL context handle. Works transparently for single and multi-window.
- **Deferred timer registration.** Timers added during callbacks go to `pendingTimers`, merged after iteration. Prevents concurrent modification.
- **Cross-platform natives.** Gradle build auto-detects OS and architecture for LWJGL native libraries.

## Performance

Tested on a Lenovo Yoga L13 Gen 2 (integrated graphics, passive cooling):
- 50-body n-body simulation with CCD and per-pair gravity: locked 60fps without fan activation
- Broadphase bounding circles: 3-7x FPS improvement over brute-force narrowphase
- Triple pendulum with rod constraints: stable for 10+ minutes with no energy drift
- Desktop (dedicated GPU): brickpit simulation at 3,000+ FPS
- Idle (AV test with UI): 4% CPU, 120MB RAM
- All tests with IntelliJ and Chrome running in the background

## Ideas

See [IDEAS.md](IDEAS.md) for the chaos board.

## License

LGPL-3.0