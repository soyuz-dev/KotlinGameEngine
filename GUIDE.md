# Bump — User Guide

A quickstart guide to building things with Bump. For architecture details and design philosophy, see [README.md](README.md).

## Table of Contents
1. [Minimal Program](#minimal-program)
2. [Entities & Shapes](#entities--shapes)
3. [Rendering](#rendering)
4. [Physics](#physics)
5. [Joints](#joints)
6. [UI System](#ui-system)
7. [Audio](#audio)
8. [Timers & Game Loop](#timers--game-loop)
9. [Multi-Window](#multi-window)
10. [Camera & Screen Effects](#camera--screen-effects)
11. [Observable Entities](#observable-entities)
12. [Where Files Go](#where-files-go)
13. [Common Issues and Quick Fixes](#common-issues-and-quick-fixes)
14. [File Format Issues](#file-format-issues)
15. [Quick Reference](#quick-reference)

---

## Minimal Program

```kotlin
fun main() {
    Application.init()
    val camera = Camera()
    val window = Window("Hello Bump", 800, 600)
    val engine = RuntimeEngine(window, physicsSystem = null, camera)
    
    Application.windows.add(window, engine) {
        engine.shader = Assets.shader("default")
        engine.renderSystem = RuntimeRenderSystem(Mesh.quad(), Mesh.circle(32))
    }
    
    val scene = RuntimeScene("main")
    engine.loadScene(scene)
    Application.run()
}
```

This creates a blank window. No entities yet, just the engine running.

---

## Entities & Shapes

Entities are the core of Bump. Everything — physics objects, UI elements, sprites — can be made with a `GameEntity`.

```kotlin
val circle = DefaultGameEntity("my_circle")
circle.position = Vector2D(400.0, 300.0)       // world position
circle.shape = CircleShape(50.0)               // 50px radius
circle.painter = SolidColor(Color(255, 100, 80)) // orange-red
scene.addEntity(circle)
```

**Shapes available:**
- `CircleShape(radius)` — circle
- `RectangleShape(width, height)` — rectangle
- `TriangleShape(a, b, c)` — arbitrary triangle
- `TriangleShape.equilateral(size)` — equilateral triangle
- `TriangleShape.isosceles(width, height)` — isosceles triangle

**Colliders** are created automatically from shapes:
```kotlin
circle.collider = Collider(circle.shape!!)
```

The `Collider(shape)` factory returns the right collider type (`CircleCollider`, `RectangleCollider`, `TriangleCollider`).

---

## Rendering

### Solid Colors
```kotlin
entity.painter = SolidColor(Color(255, 100, 80))        // RGB 0-255
entity.painter = SolidColor(Color(255, 100, 80, 128))   // with alpha
```

### Textures (Images)
```kotlin
entity.painter = ImagePainter(Assets.texture("cat"))  // loads /textures/cat.png
```

### Text
```kotlin
val font = Assets.font("roboto")  // loads /fonts/roboto.ttf
val textPainter = TextPainter(font)
textPainter.text = "Score: 100"
textPainter.fontSize = 24f
textPainter.color = Color(255, 255, 255)
entity.painter = textPainter
```

### Custom Colors
```kotlin
Color(255, 128, 0)           // orange
Color(255, 100, 80, 150)     // semi-transparent
Color.random()                // random color
```

---

## Physics

### PointMass (no rotation)
```kotlin
val body = PointMass(mass = 1.0, restitution = 0.7)
body.velocity = Vector2D(200.0, -300.0)
physicsSystem.registerBody("my_circle", body)
```

### RigidBody (with rotation)
```kotlin
val body = RigidBody(mass = 2.0, restitution = 0.5, friction = 0.4, width = 60.0, height = 40.0)
body.angularVelocity = 5.0  // spin
physicsSystem.registerBody("my_brick", body)
```

### Infinite Mass
Set mass to `0.0` for immovable objects (walls, floors):
```kotlin
val wall = PointMass(mass = 0.0)
```

### Force Fields
```kotlin
val gravity = ConstantAccelerationField(Vector2D(0.0, 981.0))

// Add to body:
body.addField(gravity)

// Or with infix:
val body = PointMass(mass = 1.0) with gravity
```

**Available forces:**
- `ConstantForceField(force)` — constant force
- `ConstantAccelerationField(acceleration)` — force proportional to mass
- `VelocityForceField(Polynomial(...))` — drag or thrust based on speed
- `GravityField(G)` — n-body gravitational attraction

### Drag & Thrust via Polynomials
```kotlin
// Linear drag: force = -velocity * 0.5
body.addField(VelocityForceField(Polynomial.linear(0.5)))

// Quadratic air resistance: force = -velocity * 0.1 * |velocity|
body.addField(VelocityForceField(Polynomial.quadratic(0.1)))

// Constant thrust: always push forward
body.addField(VelocityForceField(Polynomial(-0.5)))  // negative = thrust!
```

---

## Joints

Connect two bodies together:

```kotlin
val rod = RodJoint(bodyA, bodyB, restLength = 150.0)     // fixed distance
val rope = RopeJoint(bodyA, bodyB, maxLength = 200.0)    // one-way (can be closer)
val spring = SpringJoint(bodyA, bodyB, restLength = 100.0, stiffness = 500.0, damping = 10.0)

physicsSystem.addJoint(rod)
```

- `RodJoint` — rigid bar, exact length
- `RopeJoint` — can go slack, constrains max distance only
- `SpringJoint` — soft, force-based with damping

---

## UI System

### Buttons
```kotlin
val btn = engine.ui.button("play", 400.0, 300.0, 200.0, 50.0) {
    println("Clicked!")
}
scene.addEntity(btn)
```

### Labels
```kotlin
val label = engine.ui.label("score", 10.0, 10.0, "Score: 0", font, 20f, Color.WHITE)
scene.addEntity(label)
```

### Sliders
```kotlin
val (track, thumb) = engine.ui.slider("volume", 400.0, 500.0, 300.0, 30.0,
    min = 0.0, max = 100.0, initial = 50.0
) { value -> println("Volume: $value") }
scene.addEntity(track)
scene.addEntity(thumb)
```

### Text Input
```kotlin
val (input, bg) = engine.ui.textInput("name", 400.0, 300.0, font = font,
    placeholder = "Enter name...",
    onSubmit = { text -> println("Hello, $text!") }
)
scene.addEntity(bg)
scene.addEntity(input)
```

### Custom Interactive Entities
Chain modifiers for custom behavior:
```kotlin
entity.interactive = Interactive { pos -> entity.collider!!.containsPoint(pos, entity.transform) }
    .clickable { println("clicked") }
    .hoverable(
        onHoverEnter = { entity.painter = hoverColor },
        onHoverExit = { entity.painter = normalColor }
    )
    .draggable(
        onDrag = { _, currentPos -> entity.position = currentPos }
    )
```

**Available modifiers:** `clickable`, `hoverable`, `draggable`, `scrollable`, `focusable`, `doubleClickable`, `keyboardInput`

---

## Audio

```kotlin
// One-time setup (called by Application.init())
AudioSystem.init()

// Load and play
val sound = AudioSource()
sound.play(Assets.audio("meow"))    // loads /audio/meow.ogg

// Controls
sound.volume = 0.5f
sound.pitch = 1.2f
sound.looping = true
sound.stop()
```

Audio files go in `src/main/resources/audio/`. OGG format only.

---

## Timers & Game Loop

```kotlin
// Every frame
engine.everyFrame { dt ->
    // dt = seconds since last frame
}

// Or with invoke shorthand:
engine { dt ->
    // runs every frame
}

// After a delay (one-shot)
engine.after(2000.0) {
    println("2 seconds passed")
}

// Repeating interval
engine.forEvery(1000.0) {
    println("Every second")
}

// For a duration (fires every frame with progress 0..1)
engine.during(500.0) { progress ->
    entity.position = start + (end - start) * progress
}
```

---

## Multi-Window

```kotlin
val window1 = Window("Window 1", 800, 600)
val window2 = Window("Window 2", 600, 400)
val engine1 = RuntimeEngine(window1, null, camera1)
val engine2 = RuntimeEngine(window2, null, camera2)

Application.windows.add(window1, engine1) { /* setup */ }
Application.windows.add(window2, engine2) { /* setup */ }

// Setup scenes, entities...
Application.run()  // runs both windows
```

Each window has its own engine, scene, and UI system. Input is per-window.

---

## Camera & Screen Effects

### Camera Shake
```kotlin
engine.during(500.0) { progress ->
    val intensity = (1.0 - progress) * 20.0
    val ox = ((Math.random() - 0.5) * intensity).toFloat()
    val oy = ((Math.random() - 0.5) * intensity).toFloat()
    camera.setPosition(ox, oy)
}
engine.after(500.0) {
    camera.setPosition(0f, 0f)
}
```

### Zoom
```kotlin
camera.setZoom(2.0f)   // zoom out (see more)
camera.setZoom(0.5f)   // zoom in (magnify)
camera.zoom(1.1f)       // multiply current zoom by 1.1
```

The camera centers zoom on the middle of the view. Camera movement and zoom are handled independently. Zoom changes the visible world area, while position changes the center point being viewed.

Take note that UI elements are rendered in screen space and are unaffected by camera movement or zoom.

### Easing Functions
Use the `Easing` library for smooth animations:
```kotlin
engine.during(1000.0) { progress ->
    val eased = Easing.cubicOut(progress)
    entity.position = start + (end - start) * eased
}
```

**Available easings:** `linear`, `quadIn/Out/InOut`, `cubicIn/Out/InOut`, `sinIn/Out/InOut`, `expoIn/Out`, `bounceOut`, `elasticOut`, `backOut`, and more. See `Easing` for the full list.

---

## Observable Entities

Entities fire callbacks when properties change:

```kotlin
entity.onPositionChanged { newPos ->
    label.position = newPos + Vector2D(0.0, -50.0)  // label follows entity
}

entity.onRotationChanged { newRotation ->
    indicator.rotation = newRotation
}

entity.onShapeChanged { newShape ->
    // update collider, etc.
}
```

---

## Where Files Go

```
src/main/resources/
├── shaders/     — default.vert, default.frag (GLSL 330 core)
├── textures/    — PNG images only
├── fonts/       — TTF fonts only
└── audio/       — OGG Vorbis only
```

---

## Common Issues and Quick Fixes

### "No context is current" crash
OpenGL functions must be called when a window's context is active. If you get this error, make sure you're calling GL operations after `engine.init()` or inside `engine.everyFrame { }`. The engine manages context switching automatically.

### Window opens but nothing renders
- Did you add entities to the scene? `scene.addEntity(entity)`
- Did you call `engine.loadScene(scene)` before `Application.run()`?
- Are your entities positioned within the visible area? The default ortho maps `(0,0)` top-left to `(width, height)` bottom-right.

### Entity doesn't move with physics
- Did you register the body? `physicsSystem.registerBody(entity.id, body)`
- Did you register the collider? `collisionSystem.registerCollider(entity.id, collider)`
- Is the mass zero? `mass = 0.0` means infinite mass — the body won't move.
- Did you add forces? Bodies need force fields to accelerate (gravity, thrust, etc.).

### Collision detection isn't working
- Both entities must have colliders registered with the `CollisionSystem`.
- Both entities must have bodies registered with the `PhysicsSystem`.
- Are the entities actually overlapping? Check positions and shape sizes.
- Collisions only fire `ENTER` once per contact. Check `CollisionEventType.ENTER`.

### Text doesn't appear
- Is `GL_BLEND` enabled? The engine does this automatically via `Window.init()`.
- Did you call `textPainter.update(0f)` after setting text? UI factory methods handle this.
- Is the font loaded? Check `Assets.font("name")` matches the file in `resources/fonts/`.

### Audio doesn't play
- Did you call `Application.init()`? This initializes `AudioSystem`.
- Are your audio files OGG format? WAV is not supported.
- Check the file path: `Assets.audio("meow")` loads `/audio/meow.ogg`.

### Timer callbacks not firing
- Timers added during a timer callback (e.g., `after` inside `everyFrame`) are deferred to the next frame. This prevents concurrent modification.
- `everyFrame` / `engine { }` must be called during setup, not inside another timer callback.

### Window resize makes things disappear
- The camera's `setOrtho` is called automatically on resize. If you're using `camera.setPosition()` for shake, the offset is correctly converted to NDC internally.

### Multi-window textures/shaders appear white
- Each window needs its own OpenGL context. `Assets` handles this automatically — just use `Assets.shader("default")` and it caches per-context.
- Don't share `Shader` or `Texture` objects between engines. Let `Assets` manage them.

### Key presses not detected
- Input is per-window. Use `engine.input.isKeyDown(key)` or `KeyListener.isKeyDown(window.handle, key)`.
- GLFW key codes: `GLFW_KEY_SPACE`, `GLFW_KEY_ESCAPE`, `GLFW_KEY_LEFT`, etc. Import `org.lwjgl.glfw.GLFW.*`.
- Some key combinations don't work due to keyboard ghosting (hardware limitation). Try different key combinations.

### Massive FPS drop when many entities are on screen
- The collision broadphase uses bounding circles. If entities are clustered, narrowphase SAT still runs on all pairs that pass the circle check. For 100+ entities, consider spreading them out.
- Text re-rasterization is expensive. Don't update `TextPainter.text` every frame unless needed.

---

## File Format Issues

### The file extension says one thing, the data says another

Bump uses the file extension to decide how to load a file. If the bytes inside don't match the extension, you'll get crashes, garbage output, or silent failures.

**Always verify your files, not just their names.**

### Textures (`/textures/`)
- **Expected:** PNG format
- **What happens if wrong:** `Texture.fromResource()` uses STB Image, which detects the actual format from the file header. A `.png` file that's actually a JPEG will fail with "Failed to load texture."
- **Fix:** Open the file in an image editor and re-export as PNG. Don't just rename `.jpg` to `.png`.

### Fonts (`/fonts/`)
- **Expected:** TrueType (`.ttf`)
- **What happens if wrong:** `Font` uses STB TrueType, which expects TTF format. An OpenType `.otf` file renamed to `.ttf` may fail silently — the font loads but glyphs render as boxes or nothing.
- **Fix:** Use actual TrueType fonts. Google Fonts lets you download TTF specifically.

### Audio (`/audio/`)
- **Expected:** OGG Vorbis (`.ogg`)
- **What happens if wrong:** `AudioClip.fromResource()` uses STB Vorbis. A `.ogg` file that's actually a different codec (Opus, FLAC, or just a renamed WAV/MP3) will either:
    - Crash with "Failed to decode OGG"
    - Play garbage (static, screeching)
    - Play nothing
- **Fix:** Convert your audio to OGG Vorbis using a tool like Audacity, ffmpeg, or an online converter. Renaming `.wav` to `.ogg` will **not** work.

### Shaders (`/shaders/`)
- **Expected:** GLSL 330 core (`.vert`, `.frag`)
- **What happens if wrong:** The shader compiles but renders black/white or crashes the GPU driver. If you see solid colors where textures should be, check the shader's `uUseTexture` uniform.
- **Fix:** Make sure your shaders declare `#version 330 core` and match the engine's uniform names (`uProjection`, `uModel`, `uColor`, `uTexture`, `uUseTexture`).

### How to check what a file actually is

**Windows (PowerShell):**
```powershell
# Check first few bytes (magic number)
Get-Content .\file.ogg -Encoding Byte -TotalCount 4
```

**Common magic numbers:**
| Format | First bytes (hex) |
|--------|-------------------|
| PNG    | `89 50 4E 47`     |
| OGG    | `4F 67 67 53`     |
| TTF    | `00 01 00 00`     |
| WAV    | `52 49 46 46`     |
| MP3    | `FF FB` or `ID3`  |

**Linux/macOS:**
```bash
file file.ogg          # prints detected format
hexdump -C file.ogg | head -1
```

---

## Quick Reference

| Task | Code |
|------|------|
| Create entity | `DefaultGameEntity("id")` |
| Set position | `entity.position = Vector2D(x, y)` |
| Set shape | `entity.shape = CircleShape(r)` |
| Set color | `entity.painter = SolidColor(Color(r,g,b))` |
| Add collider | `entity.collider = Collider(shape)` |
| Add physics | `body = PointMass(mass) with gravity` |
| Register physics | `physicsSystem.registerBody(id, body)` |
| Register collider | `collisionSystem.registerCollider(id, collider)` |
| Add to scene | `scene.addEntity(entity)` |
| Load font | `Assets.font("roboto")` |
| Load texture | `Assets.texture("cat")` |
| Load audio | `Assets.audio("meow")` |
| Every frame | `engine { dt -> ... }` |
| After delay | `engine.after(ms) { ... }` |
| Make button | `engine.ui.button("id", x, y, w, h) { ... }` |
| Make label | `engine.ui.label("id", x, y, "text", font, size, color)` |