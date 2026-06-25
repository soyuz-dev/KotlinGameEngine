# Bump

A 2D game engine built in Kotlin with LWJGL. Designed for a DSL-driven workflow where entities own their data directly — no ECS, no ceremony.

## Status

Feature-complete for 2D games. The engine runs, the commit messages are unhinged, the DSL is next.

### What works
- **Engine:** Self-contained `RuntimeEngine` — handles GLFW window, OpenGL context, main loop, input callbacks, timers (`forever`, `forEvery`, `after`, `during`), and cleanup. `Main.kt` is just configuration.
- **Rendering:** Modern OpenGL 3.3 core profile, shader-based (`Shader`, `Mesh`, `Camera`), `Painter` interface with `SolidColor`, `ImagePainter`, and `TextPainter`, `DynamicPainter` for animated visuals, VAO/VBO circle/quad/triangle meshes with UV support, STB-based `Texture` loading, STB TrueType `Font` rendering, `Assets` object for resource caching, alpha blending, `RenderSystem` abstraction
- **Audio:** OpenAL-based `AudioSystem`, `AudioClip` (OGG via STB Vorbis), `AudioSource` with volume/pitch/looping, spatial audio support
- **Input:** Keyboard, mouse, scroll — single `Input` object
- **UI System:** `Interactive` interface with decorator chain (`clickable`, `hoverable`, `draggable`, `scrollable`, `focusable`, `doubleClickable`), `UISystem` input router with hover/press/drag/focus/scroll state management, `UI` convenience factory (`button`, `label`, `slider`), hit-testing via existing collider infrastructure, `UIState` per entity, observable entity properties with `onPositionChanged`/`onRotationChanged`
- **Vector math:** `Vector2D` with infix `dot` and `cross`, `Transform` with local↔world space helpers, `MathUtil` with `modelMatrix` and `orthoMatrix`, `Color` utility
- **Shapes:** `CircleShape`, `RectangleShape`, `TriangleShape`, `Aabb2D`, `ShapeQueries`
- **Physics bodies:** `PointMass` (Verlet integration), `KinematicBody`, `RigidBody` (linear + angular with moment of inertia)
- **Force fields:** `ConstantForceField`, `ConstantAccelerationField`, `GravityField` (n-body with IEEE 754 bit-hack exponent optimization), `EntityAwareForceField` interface for multi-body forces
- **Joints:** `RodJoint` (hard constraint with Baumgarte stabilization), `RopeJoint` (one-way constraint), `SpringJoint` (soft constraint with damping), sealed `Joint` interface with `StrictJoint` and `PermissiveJoint`
- **Collision detection:** `CircleCollider`, `RectangleCollider` with full SAT, `TriangleCollider` with SAT and barycentric point test, circle-rect closest-point, circle-triangle, bounding circle broadphase
- **Collision resolution:** Impulse-based with positional correction, friction, restitution, angular effects for rigidbodies, approach-speed threshold for resting contacts
- **CCD:** Swept circle-vs-AABB with substepping and corner mitigation (tested to 300k+ px/s without tunneling)
- **Event bus:** `RuntimeEventBus` with type-based pub/sub, `CollisionEvent` fired on contact
- **Scene graph:** `Scene`, `RuntimeScene`, entity management
- **Observable entities:** Properties like `position` and `rotation` fire change listeners for reactive UI updates
- **Debug utility:** Zero-overhead inline logging via `Debug` object
- **Cross-platform:** Windows, Linux, macOS (Intel + Apple Silicon) via Gradle build

### In progress
- DSL (`engine { scene { ... } }`)
- Spatial hash broadphase
- `PainterModifier` system (tint, opacity, clip)
- Rectangle CCD (maybe not because discrete handles it pretty well)
- Text input field

### Demos
Three main demo files showcase different engine capabilities:
- **Main.kt (BrickPit):** Full physics sandbox. Click to spawn balls and rigidbody bricks with angular physics, friction, and CCD collision. Walls keep everything contained.
- **DragTest.kt:** UI interaction demo. Draggable panel with cat texture and label following via observable position, draggable circle with hover effects, volume slider with live value readout.
- **AVTest.kt:** Multimedia demo. Clickable cat/dog with sounds, FPS counter, title text, pulsing circle, styled play button with camera shake effect. No physics.

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
│   ├── core           — Engine, RuntimeEngine (window, loop, timers, lifecycle)
│   ├── entity         — GameEntity, DefaultGameEntity (observable properties)
│   ├── events         — EventBus, RuntimeEventBus, CollisionEvent
│   ├── physics
│   │   ├── forcefields — ForceField, EntityAwareForceField, ConstantForceField, ConstantAccelerationField, GravityField
│   │   └── joints     — Joint, StrictJoint, PermissiveJoint, RodJoint, RopeJoint, SpringJoint
│   ├── physics        — PhysicsBody, PointMass, RigidBody, KinematicBody, PhysicsSystem
│   ├── render
│   │   ├── image      — Texture, ImagePainter
│   │   └── text       — Font, TextPainter
│   ├── render         — Shader, Mesh, Camera, Painter, SolidColor, DynamicPainter, RenderSystem, RuntimeRenderSystem
│   ├── scene          — Scene, RuntimeScene
│   └── ui             — Interactive, InteractiveDecorator, UISystem, UIState, UI (factory)
├── input              — Input (object), KeyListener, MouseListener
└── util               — Vector2D, Transform, Debug, MathUtil, Aabb2D, ShapeQueries, Assets, Color, Dynamic
```

## Design decisions

- **Doubles everywhere.** No floats. `Vector2D`, `Transform`, `Aabb2D` all use `Double`. Floats only at the GL/AL boundary.
- **Immutable data where possible.** `Vector2D` operators return new instances. `Transform` is a data class with `copy()`.
- **Bodies own their forces.** No global force registry. Bodies hold a list of `ForceField` instances. `EntityAwareForceField` for multi-body interactions.
- **Physics is multi-phase.** Force accumulation → permissive joint forces → CCD position update → discrete collision detection → impulse resolution + friction + positional correction → strict joint solving (5 iterations) → velocity finalization → entity-aware field position updates.
- **No ECS.** Entities are objects with direct references to their data. Same `GameEntity` type used for physics objects and UI elements.
- **Engine encapsulates the platform.** `RuntimeEngine` owns the window, GL context, input callbacks, main loop, and cleanup. User writes configuration, not boilerplate.
- **Input is a singleton.** `object Input` wraps `KeyListener` and `MouseListener`. No injection needed.
- **Mass-zero means infinite mass.** No separate `isStatic` boolean. `mass = 0.0` → immovable.
- **CCD by default.** Circle bodies use swept collision against AABBs with substepping. No tunneling.
- **Painter for rendering.** Entities carry their own appearance via the `Painter` interface. `SolidColor`, `ImagePainter`, and `TextPainter` implementations. `DynamicPainter` for per-frame updates.
- **Interactive decorator chain.** UI behavior composed via chained modifiers: `.clickable { }.hoverable { }.draggable { }`. No subclass explosion.
- **Observable entities.** Properties like `position` fire change listeners. Labels follow panels, sliders update readouts — no per-frame polling.
- **Engine as orchestrator.** `RuntimeEngine` coordinates UI → Physics → Dynamics → Render via timers and the main loop. Systems are swappable.
- **Assets cached centrally.** `Assets` object provides lazy-loaded, cached access to shaders, textures, fonts, and audio. Convention-based paths.
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