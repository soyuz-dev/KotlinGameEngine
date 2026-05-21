
# Bump

A 2D game engine built in Kotlin with LWJGL. Designed for a DSL-driven workflow where entities own their data directly — no ECS, no ceremony.

## Status

In heavy development. Things move fast, commit messages are unhinged, the engine runs.

### What works
- Window + OpenGL context (compatibility profile for now)
- Input: keyboard, mouse, scroll — single `Input` object
- Vector math: `Vector2D` with infix `dot` and `cross`, `Transform` with local↔world space helpers
- Shapes: `CircleShape`, `RectangleShape`, `Aabb2D`, `ShapeQueries`
- Physics bodies: `PointMass` (Verlet integration), `KinematicBody`, `RigidBody` (linear + angular)
- Force fields: `ConstantForceField`, attachable to individual bodies
- Collision detection: `CircleCollider`, `RectangleCollider` with full SAT
- Collision resolution: impulse-based with positional correction, restitution, friction stubs
- CCD: swept circle-vs-AABB with substepping and corner mitigation (handles 300k+ px/s without tunneling)
- Scene graph: `Scene`, `RuntimeScene`, entity management
- Engine loop: fixed-timestep `RuntimeEngine` with accumulator
- Debug utility: zero-overhead inline logging via `Debug` object

### In progress
- `EventBus` for collision callbacks
- `RenderSystem` abstraction (currently immediate-mode debug rendering)
- Rectangle CCD

### Next up
- Event system (collision enter/exit/stay callbacks)
- Renderer abstraction (shader-based, batching)
- Broadphase collision (spatial hash)
- DSL (`engine { scene { ... } }`)

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

`Main.kt` demo: a ball bouncing inside a box with full CCD collision, gravity, and screen-edge walls.

## Architecture

```
org.soyuz
├── engine
│   ├── collision    — Collider, CircleCollider, RectangleCollider, CollisionSystem, Contact
│   ├── core         — Engine, RuntimeEngine (fixed-timestep loop)
│   ├── entity       — GameEntity, DefaultGameEntity
│   ├── events       — EventBus (stub)
│   ├── physics      — PhysicsBody, PointMass, RigidBody, KinematicBody, ForceField, PhysicsSystem
│   ├── policy       — Engine configuration (collision, physics bounds)
│   ├── render       — RenderSystem, Renderable (stubs)
│   └── scene        — Scene, RuntimeScene
├── input            — Input (object), KeyListener, MouseListener
└── util             — Vector2D, Transform, Debug, MathUtil, Aabb2D, ShapeQueries
```

## Ideas

See [IDEAS.md](IDEAS.md) for the chaos board.

## License

TBD
