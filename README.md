
# Bump

A 2D game engine built in Kotlin with LWJGL. Designed for a DSL-driven workflow where entities own their data directly — no ECS, no ceremony.

## Status

In heavy development. 

### What works for now
- Window + OpenGL context (compatibility profile for now)
- Input: keyboard, mouse, scroll — single `Input` object
- Vector math: `Vector2D`, `Transform` with local↔world space helpers
- Shapes: `CircleShape`, `RectangleShape`, `Aabb2D`, `ShapeQueries`
- Physics: `PointMass` (Verlet integration), `KinematicBody`, force fields
- Collision: `CircleCollider`, `RectangleCollider` with SAT
- Scene graph: `Scene`, `RuntimeScene`, entity management
- Engine loop: fixed-timestep `RuntimeEngine` with accumulator

### In progress
- `CollisionSystem` detection loop
- `Contact` generation for collision resolution
- Event bus for collision callbacks

### Next up
- Collision resolution (impulse-based)
- Rectangle collider tests
- Collision system ↔ physics system integration

## Building

```bash
./gradlew build
```

## Running

```bash
./gradlew run
```

Right now `Main.kt` has a demo: an orange dot bouncing around under gravity with screen-edge collision.

## Architecture

```
org.soyuz
├── engine
│   ├── collision    — Collider, CircleCollider, RectangleCollider, CollisionSystem
│   ├── core         — Engine, RuntimeEngine (fixed-timestep loop)
│   ├── entity       — GameEntity, DefaultGameEntity, Transform
│   ├── events       — EventBus (stub)
│   ├── physics      — PhysicsBody, PointMass, KinematicBody, ForceField, PhysicsSystem
│   ├── policy       — Engine configuration (collision, physics bounds)
│   ├── render       — RenderSystem, Renderable (stubs)
│   └── scene        — Scene, RuntimeScene
├── input            — Input, KeyListener, MouseListener
└── util             — Vector2D, Debug, MathUtil, Aabb2D, ShapeQueries, Transform
```

## Ideas

See [IDEAS.md](IDEAS.md) for the chaos board.

## License

TBD
