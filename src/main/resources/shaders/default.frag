#version 330 core
uniform vec4 uColor;
uniform sampler2D uTexture;
uniform int uUseTexture;
in vec2 vTexCoord;
out vec4 fragColor;

void main() {
    if (uUseTexture == 1) {
        fragColor = texture(uTexture, vTexCoord) * uColor;
    } else {
        fragColor = uColor;
    }
}