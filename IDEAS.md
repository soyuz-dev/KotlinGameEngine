# Ideas

Random brain dumps worth revisiting later. No deadlines, no promises.

---

## Physics

### Window as RigidBody
Window bounds are physical entities with colliders. Dragging/resizing the window pushes objects in the scene. The OS window becomes a game mechanic, not a container.

### Rotatable Windows
- Simulation window (A): rotated, fully physical, contains the scene
- OS window (B): axis-aligned, borderless, transparent background
- Render A inside B, clip to A's rotated rect via stencil buffer
- Custom window chrome (title bar, buttons) rendered as entities inside A
- Needs: `GLFW_DECORATED`, `GLFW_TRANSPARENT_FRAMEBUFFER`, `DwmExtendFrameIntoClientArea`
- Separate module: `bump-windowing` or `org.soyuz.windowing`

### Growing window on collision
Ball hits window edge → window expands in that direction. The viewport fights back.

### Constraint solver
Springs, distance joints, hinges. `ConstraintForceField` that generates forces based on relative positions/velocities between paired bodies.

### Buoyancy / fluid simulation
Simple density-based buoyancy. Bodies with `density < fluidDensity` float upward. Could be a `BuoyancyField` that applies force based on body volume and submersion depth.

---

## Rendering

### Debug drawing layer
Toggleable overlay for colliders, contact points, normals, AABBs, velocity vectors. Press a key to show/hide. Critical for physics debugging.

### Camera shake on impact
Camera entity responds to `CollisionEvent` with a decaying positional offset. Window edge hits, explosions, heavy landings.

### ImagePainter
`Painter` implementation that loads a texture from file and renders it on a quad. Entity gets a sprite instead of a solid color.

### PainterModifier chain
```kotlin
painter = SolidColor(1.0, 0.3, 0.1) + OpacityModifier(0.8) + TintModifier(0.2, 0.0, 0.1)
```
Modifiers applied in order at bind time. Composability without subclass explosion.

### Particle system
Lightweight GPU-particle system for trails, sparks, debris on collision. `ParticleEmitter` as a component. Could fire on `CollisionEvent`.

---

## Entities / DSL

### Spawn on click with type selection
Right-click for menu, or keyboard shortcuts to choose what spawns: bouncy ball, heavy box, gravity well.

### Entity templates
Pre-configured entity blueprints (bouncy ball, heavy box, static wall, attractor) that the DSL can reference by name.

### Entity lifecycle hooks
`onSpawn`, `onDestroy`, `onCollisionEnter`, `onCollisionExit`, `onCollisionStay`. EventBus already supports this — just needs wiring to entity callbacks.

---

## Systems

### Broadphase (spatial hash)
Replace O(n²) collision detection with spatial partitioning. Divide world into grid cells, only check pairs in same or adjacent cells.

### Serialization
Scene save/load. Serialize entity graph, transforms, colliders, physics bodies. JSON or a custom binary format. Needed for level editing later.

### Profiling / metrics overlay
FPS, frame time, body count, collision pairs checked, force calculations skipped (via bit-hack counter). Useful for tuning the threshold in `GravityField`.

---

## Platform

### Threaded simulation
Physics step runs on a worker thread while main thread handles GLFW events + rendering. Avoids visual pause during window drag/resize on Windows. Requires double-buffered scene state.

### Headless mode
Run without a window for unit testing physics, AI, or server-side simulation. No GLFW dependency.

### WebAssembly target
Kotlin/Wasm + WebGL. Bump in the browser. Probably a long-term fever dream.

---

## Done (graduated from ideas)
- [x] Velocity Verlet integration
- [x] Circle + Rectangle colliders with SAT
- [x] Force fields attached to bodies
- [x] Kinematic body type
- [x] Input as singleton object
- [x] Transform with local↔world helpers
- [x] Shader-based rendering (core profile)
- [x] CCD with substepping and corner mitigation
- [x] EventBus with collision events
- [x] DynamicForceField for multi-body interactions
- [x] n-body gravity with bit-hack exponent optimization
- [x] Painter interface with SolidColor
