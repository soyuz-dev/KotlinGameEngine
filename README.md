# Bump

A 2D game engine built in Kotlin with LWJGL. Designed for a DSL-driven workflow where entities own their data directly — no ECS, no ceremony.

## Status

In heavy development. Things move fast, commit messages are unhinged, the engine runs.

### What works
- **Rendering:** Modern OpenGL 3.3 core profile, shader-based (`Shader`, `Mesh`, `Camera`), `Painter` interface with `SolidColor` and `ImagePainter`, VAO/VBO circle, quad, and triangle meshes with UV support, STB-based `Texture` loading, `Assets` object for resource caching
- **Input:** Keyboard, mouse, scroll — single `Input` object
- **Vector math:** `Vector2D` with infix `dot` and `cross`, `Transform` with local↔world space helpers, `MathUtil` with `modelMatrix` and `orthoMatrix`
- **Shapes:** `CircleShape`, `RectangleShape`, `TriangleShape`, `Aabb2D`, `ShapeQueries`
- **Physics bodies:** `PointMass` (Verlet integration), `KinematicBody`, `RigidBody` (linear + angular)
- **Force fields:** `ConstantForceField`, `ConstantAccelerationField`, `GravityField` (n-body with IEEE 754 bit-hack exponent optimization), `DynamicForceField` interface for multi-body forces
- **Joints:** `RodJoint` (hard constraint with Baumgarte stabilization), `RopeJoint` (one-way constraint), `SpringJoint` (soft constraint with damping), sealed `Joint` interface with `StrictJoint` and `PermissiveJoint`
- **Collision detection:** `CircleCollider`, `RectangleCollider` with full SAT, `TriangleCollider` with SAT and barycentric point test, circle-rect closest-point, circle-triangle
- **Collision resolution:** Impulse-based with positional correction, restitution, approach-speed threshold for resting contacts
- **CCD:** Swept circle-vs-AABB with substepping and corner mitigation (tested to 300k+ px/s without tunneling)
- **Event bus:** `RuntimeEventBus` with type-based pub/sub, `CollisionEvent` fired on contact
- **Scene graph:** `Scene`, `RuntimeScene`, entity management
- **Engine loop:** Fixed-timestep `RuntimeEngine` with accumulator
- **Debug utility:** Zero-overhead inline logging via `Debug` object

### In progress
- DSL (`engine { scene { ... } }`)
- Broadphase collision (spatial hash)
- `PainterModifier` system (tint, opacity, clip)
- Audio system
- Rectangle CCD

### Demos
`Main.kt` has been home to several demos during development:
- **n-body gravity:** Click to spawn balls with random mass, size, and velocity. Balls attract each other via `GravityField`, collide with screen-edge walls via CCD.
- **Triple pendulum:** Three rods connecting an anchor and three bobs, driven by gravity. Chaotic motion with perfect constraint satisfaction (tested stable for 10+ minutes).
- **Spring pendulum:** Single spring-suspended bob, oscillating with configurable stiffness and damping.
- **Textured sprites:** Cat and dog images rendered on rotated/scaled rectangles via `ImagePainter`.

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
│   ├── collision       — Collider, CircleCollider, RectangleCollider, TriangleCollider, CollisionSystem, Contact
│   ├── core            — Engine, RuntimeEngine (fixed-timestep loop)
│   ├── entity          — GameEntity, DefaultGameEntity
│   ├── events          — EventBus, RuntimeEventBus, CollisionEvent
│   ├── physics
│   │   ├── forcefields — ForceField, DynamicForceField, ConstantForceField, ConstantAccelerationField, GravityField
│   │   └── joints      — Joint, StrictJoint, PermissiveJoint, RodJoint, RopeJoint, SpringJoint
│   ├── physics         — PhysicsBody, PointMass, RigidBody, KinematicBody, PhysicsSystem
│   ├── policy          — Engine configuration (collision, physics bounds)
│   ├── render
│   │   └── image       — Texture, ImagePainter
│   ├── render          — Shader, Mesh, Camera, Painter, SolidColor
│   └── scene           — Scene, RuntimeScene
├── input               — Input (object), KeyListener, MouseListener
└── util                — Vector2D, Transform, Debug, MathUtil, Aabb2D, ShapeQueries, Assets
```

## Design decisions

- **Doubles everywhere.** No floats. `Vector2D`, `Transform`, `Aabb2D` all use `Double`. Floats only at the GL boundary.
- **Immutable data where possible.** `Vector2D` operators return new instances. `Transform` is a data class with `copy()`.
- **Bodies own their forces.** No global force registry. Bodies hold a list of `ForceField` instances. `DynamicForceField` for multi-body interactions.
- **Physics is multi-phase.** Force accumulation → permissive joint forces → CCD position update → discrete collision detection → impulse resolution + positional correction → strict joint solving (5 iterations) → velocity finalization → dynamic field position updates.
- **No ECS.** Entities are objects with direct references to their data. Callbacks for update/collision.
- **Input is a singleton.** `object Input` wraps `KeyListener` and `MouseListener`. No injection needed.
- **Mass-zero means infinite mass.** No separate `isStatic` boolean. `mass = 0.0` → immovable.
- **CCD by default.** Circle bodies use swept collision against AABBs with substepping. No tunneling.
- **Painter for rendering.** Entities carry their own appearance via the `Painter` interface. `SolidColor` and `ImagePainter` implementations. Separates visuals from physics/shape.
- **Assets cached centrally.** `Assets` object provides lazy-loaded, cached access to shaders and textures. Convention-based paths.

## Performance

Tested on a Lenovo Yoga L13 Gen 2 (integrated graphics, passive cooling):
- 50-body n-body simulation with CCD and per-pair gravity: locked 60fps without fan activation
- Triple pendulum with rod constraints: stable for 10+ minutes with no energy drift
- All tests with IntelliJ and Chrome running in the background

## Ideas

See [IDEAS.md](IDEAS.md) for the chaos board.

## License

LGPL-3.0
