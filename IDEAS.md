# Ideas

Random brain dumps worth revisiting later. No deadlines, no promises.

---

## Giant Idea

### Rotatable Windows
- Simulation window (A): rotated, fully physical, contains the scene
- OS window (B): axis-aligned, borderless, transparent background
- Render A inside B, clip to A's rotated rect via stencil buffer
- Custom window chrome (title bar, buttons) rendered as entities inside A
- Needs: `GLFW_DECORATED`, `GLFW_TRANSPARENT_FRAMEBUFFER`, `DwmExtendFrameIntoClientArea`
- Architecturally supported by `Window`/`WindowRuntime`/`WindowManager` split
- Custom chrome buttons call `window.quit()`, `window.minimize()`, etc.

## Physics

### Window as RigidBody
Window bounds are physical entities with colliders. Dragging/resizing the window pushes objects in the scene. The OS window becomes a game mechanic, not a container.

### Growing window on collision
Ball hits window edge → window expands in that direction. The viewport fights back.

### Buoyancy / fluid simulation
Simple density-based buoyancy. Bodies with `density < fluidDensity` float upward. `BuoyancyField` applying force based on body volume and submersion depth.

### Weld / fixed joints
Glue two bodies together at a fixed relative offset. Zero degrees of freedom. Useful for building compound objects from simple shapes.

### Soft body / jelly physics
Mesh of `PointMass` instances connected by `SpringJoint`s. Deforms on collision. Satisfying squish.

---

## Rendering

### Debug drawing layer
Toggleable overlay for colliders, contact points, normals, AABBs, velocity vectors. Press F1 to show/hide. Critical for physics debugging.

### PainterModifier chain
```kotlin
painter = SolidColor(1.0, 0.3, 0.1) + OpacityModifier(0.8) + TintModifier(0.2, 0.0, 0.1)
```
Modifiers applied in order at bind time. Composability without subclass explosion.

### Particle system
Lightweight GPU-particle system for trails, sparks, debris on collision. `ParticleEmitter` as a component. Fires on `CollisionEvent`.

### Render layers / z-ordering
Entities sorted by layer before drawing. Background, game, foreground, UI. Prevents painter's algorithm issues.

### Sprite sheet / animation
`AnimatedPainter` that cycles through UV regions of a texture atlas. Frame-based or time-based animation.

---

## UI System

### Checkbox / Toggle
Clickable entity with on/off state. `UI.toggle(id, x, y, initial) { value -> ... }`. Uses observable state internally.

### Dropdown / Context menu
List of options that appears on click. Uses `Interactive` for each item. Renders on top of other elements.

### Layout system
Flexbox-like or anchor-based layout. UI elements position relative to parent or screen edges. `UI.hbox { }`, `UI.vbox { }` builders.

### UI themes
Predefined color schemes and painter configurations. `UI.theme = Theme.DARK` applies consistent styling to all UI factory elements.

---

## Entities / DSL

### Entity templates
Pre-configured entity blueprints (bouncy ball, heavy box, static wall, attractor) that the DSL can reference by name.

### Entity lifecycle hooks
`onSpawn`, `onDestroy`, `onCollisionEnter`, `onCollisionExit`, `onCollisionStay`. EventBus already supports this — just needs wiring to entity callbacks.

### Prefabs
Reusable entity definitions. Instantiate with overrides. `prefab("bouncy_ball") { position(100, 200); velocity(300, 0) }`

### Child entities
Optional parent-child relationships. World transform computed by walking up the tree. Cleanup cascades. Useful for compound objects and complex UI.

---

## Systems

### Broadphase (spatial hash)
Replace O(n²) collision detection with spatial partitioning. Divide world into grid cells, only check pairs in same or adjacent cells. (Bounding circle broadphase already implemented.)

### Serialization
Scene save/load. Serialize entity graph, transforms, colliders, physics bodies. JSON or a custom binary format. Needed for level editing later.

### Profiling / metrics overlay
FPS, frame time, body count, collision pairs checked, force calculations skipped (via bit-hack counter). Useful for tuning the threshold in `GravityField`.

### Property bindings
Formalize the observable entity pattern into a `Binding<T>` system. `label.position bind panel.position + offset`. Animated transitions via `Easing`.

---

## Platform

### Threaded simulation
Physics step runs on a worker thread while main thread handles GLFW events + rendering. Avoids visual pause during window drag/resize on Windows. Requires double-buffered scene state.

### Headless mode
Run without a window for unit testing physics, AI, or server-side simulation. No GLFW dependency.

### WebAssembly target
Kotlin/Wasm + WebGL. Bump in the browser. Long-term fever dream.

---

## Done (graduated from ideas)
- [x] Velocity Verlet integration
- [x] Circle + Rectangle colliders with SAT
- [x] Triangle collider with SAT and barycentric point test
- [x] Force fields attached to bodies
- [x] Kinematic body type
- [x] Per-window input with window-handle-keyed state
- [x] Transform with local↔world helpers
- [x] Shader-based rendering (core profile)
- [x] CCD with substepping and corner mitigation
- [x] EventBus with collision events (ENTER/EXIT/STAY)
- [x] EntityAwareForceField for multi-body interactions
- [x] n-body gravity with bit-hack exponent optimization
- [x] Painter interface with SolidColor, ImagePainter, TextPainter
- [x] DynamicPainter for per-frame visual updates
- [x] Context-aware Assets caching (per GL context)
- [x] RodJoint with Baumgarte stabilization
- [x] RopeJoint (one-way constraint)
- [x] SpringJoint with damping
- [x] Triple pendulum (tested stable 10+ minutes)
- [x] Audio system (OpenAL, OGG playback, spatial audio)
- [x] Bounding circle broadphase (3-7x performance improvement)
- [x] Rigidbody with angular physics, friction, proper clipped-edge contact points
- [x] Cross-platform build (Windows, Linux, macOS Intel + Apple Silicon)
- [x] Color utility class
- [x] UI System with Interactive decorator chain
- [x] UISystem input router with full state management
- [x] UI factory (button, label, slider, textInput)
- [x] Text input field with cursor navigation
- [x] RuntimeEngine orchestrating UI → Physics → Dynamics → Render
- [x] RenderSystem abstraction
- [x] Dynamic interface for per-frame update dispatch
- [x] Observable entity properties (position, rotation, shape)
- [x] Camera shake effect with NDC-correct offsets
- [x] Engine timer system (everyFrame, forEvery, after, during)
- [x] GLFW fully encapsulated — Window, WindowManager, Application
- [x] Multi-window support via WindowManager with context switching
- [x] Polynomial class with arithmetic and calculus
- [x] Easing library (30+ curves, polynomial-backed)
- [x] VelocityForceField with polynomial-based drag/thrust curves
- [x] TriangleShape with equilateral and isosceles factories
- [x] Camera with offset support for screen effects
- [x] Playable Asteroids demo game
- [x] Engine-scoped dependencies (UISystem, UI, Input per-engine)
- [x] Deferred timer registration (pendingTimers pattern)
- [x] Interactive { } invoke shorthand