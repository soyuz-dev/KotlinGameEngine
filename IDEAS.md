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

---

## Rendering

### Debug drawing layer
Immediate-mode-style overlay for colliders, contact points, normals, AABBs. Toggle with a key. Critical for physics debugging.

### Camera shake on impact
Camera entity responds to collision events with a decaying offset. Window edge hits, explosions, heavy landings.

---

## Entities / DSL

### Spawn on click
Click anywhere → spawn a new entity at cursor position with a random initial velocity. Instant particle playground.

### Gravity wells
Click and hold → create a temporary `PointGravityField` that attracts nearby entities. Release to let them fly.

### Entity templates
Pre-configured entity blueprints (bouncy ball, heavy box, static wall) that the DSL can reference by name.

---

## Systems

### Broadphase (spatial hash)
Replace O(n²) collision detection with spatial partitioning when entity count gets high enough to matter.

### Constraint solver
Springs, distance joints, hinges. `ConstraintForceField` that generates forces based on relative positions/velocities.

### Serialization
Scene save/load. Serialize entity graph, transforms, colliders, physics bodies. Needed for level editing later.

---

## Platform

### Threaded simulation
Physics step runs on a worker thread while main thread handles GLFW events + rendering. Avoids visual pause during window drag/resize on Windows. Requires double-buffered scene state.

### Headless mode
Run without a window for unit testing physics, AI, or server-side simulation.

---

## Done (graduated from ideas)
- [x] Velocity Verlet integration
- [x] Circle + Rectangle colliders with SAT
- [x] Force fields attached to bodies
- [x] Kinematic body type
- [x] Input as singleton object
- [x] Transform with local↔world helpers