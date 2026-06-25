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

### Buoyancy / fluid simulation
Simple density-based buoyancy. Bodies with `density < fluidDensity` float upward. `BuoyancyField` applying force based on body volume and submersion depth.

### Weld / fixed joints
Glue two bodies together at a fixed relative offset. Zero degrees of freedom. Useful for building compound objects from simple shapes.

---

## Rendering

### Debug drawing layer
Toggleable overlay for colliders, contact points, normals, AABBs, velocity vectors. Press a key to show/hide. Critical for physics debugging.

### PainterModifier chain
```kotlin
painter = SolidColor(1.0, 0.3, 0.1) + OpacityModifier(0.8) + TintModifier(0.2, 0.0, 0.1)
```
Modifiers applied in order at bind time. Composability without subclass explosion.

### Particle system
Lightweight GPU-particle system for trails, sparks, debris on collision. `ParticleEmitter` as a component. Could fire on `CollisionEvent`.

### Render layers / z-ordering
Entities sorted by layer before drawing. Background, game, foreground, UI. Prevents painter's algorithm issues.

---

## UI System

### Text input field
Focusable text field entity. Captures keyboard input via `Interactive.keyboardInput`. Renders text with blinking cursor. Returns value on enter or focus loss.

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
Formalize the observable entity pattern into a `Binding<T>` system. `label.position bind panel.position + offset`. Animated transitions between values.

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
- [x] Triangle collider with SAT and barycentric point test
- [x] Force fields attached to bodies
- [x] Kinematic body type
- [x] Input as singleton object
- [x] Transform with local↔world helpers
- [x] Shader-based rendering (core profile)
- [x] CCD with substepping and corner mitigation
- [x] EventBus with collision events
- [x] EntityAwareForceField for multi-body interactions
- [x] n-body gravity with bit-hack exponent optimization
- [x] Painter interface with SolidColor
- [x] ImagePainter with STB texture loading
- [x] TextPainter with STB TrueType font rendering
- [x] DynamicPainter for per-frame visual updates
- [x] Assets object for shader/texture/font/audio caching
- [x] RodJoint with Baumgarte stabilization
- [x] RopeJoint (one-way constraint)
- [x] SpringJoint with damping
- [x] Triple pendulum (tested stable 10+ minutes)
- [x] Audio system (OpenAL, OGG playback, spatial audio)
- [x] Bounding circle broadphase (3-7x performance improvement)
- [x] Rigidbody with angular physics, friction, proper contact points
- [x] Cross-platform build (Windows, Linux, macOS Intel + Apple Silicon)
- [x] Color utility class
- [x] UI System with Interactive decorator chain (clickable, hoverable, draggable, scrollable, focusable, doubleClickable)
- [x] UISystem input router with hover/press/drag/focus/scroll state management
- [x] UI factory (button, label, slider convenience functions)
- [x] RuntimeEngine orchestrating UI → Physics → Dynamics → Render
- [x] RenderSystem abstraction
- [x] Dynamic interface for per-frame update dispatch
- [x] Observable entity properties (onPositionChanged, onRotationChanged)
- [x] Camera shake effect via engine.during()
- [x] Engine timer system (forever, forEvery, after, during)
- [x] GLFW fully encapsulated in RuntimeEngine