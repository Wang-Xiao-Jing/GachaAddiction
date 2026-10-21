#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D BackgroundSampler;
uniform float FadeTop;
uniform float FadeBottom;

in vec2 texCoord;
out vec4 fragColor;

const float OPAQUE_CENTER_FRACTION = 0.25;
const float MINIMUM_HALF_HEIGHT = 0.0001;

void main() {
    float center = (FadeTop + FadeBottom) * 0.5;
    float halfHeight = max((FadeBottom - FadeTop) * 0.5, MINIMUM_HALF_HEIGHT);
    float distance = abs((1.0 - texCoord.y - center) / halfHeight);
    float opacity = 1.0 - smoothstep(OPAQUE_CENTER_FRACTION, 1.0, distance);
    vec3 background = texture(BackgroundSampler, texCoord).rgb;
    vec3 foreground = texture(DiffuseSampler, texCoord).rgb;
    fragColor = vec4(mix(background, foreground, opacity), 1.0);
}
