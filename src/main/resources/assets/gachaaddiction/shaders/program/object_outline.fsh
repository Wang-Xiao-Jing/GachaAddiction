#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float OutlineWidth;

in vec2 texCoord;
out vec4 fragColor;

const float ALPHA_THRESHOLD = 0.05;

void main() {
    vec4 object = texture(DiffuseSampler, texCoord);
    vec2 pixel = vec2(OutlineWidth) / OutSize;
    float edge = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            edge = max(edge, texture(DiffuseSampler, texCoord + vec2(x, y) * pixel).a);
        }
    }
    edge = step(ALPHA_THRESHOLD, edge) * (1.0 - step(ALPHA_THRESHOLD, object.a));
    fragColor = vec4(1.0, 1.0, 1.0, edge);
}
