#version 150

uniform sampler2D ItemSampler;
uniform sampler2D DiffuseSampler;
uniform vec2 OutSize;
uniform float RevealProgress;
uniform float EffectTime;
uniform float RarityRed;
uniform float RarityGreen;
uniform float RarityBlue;
uniform float BackdropOffset;
uniform float EntranceOpacity;
uniform float BackdropProgress;
uniform float RimProgress;

in vec2 texCoord;
out vec4 fragColor;

const float CORE_STRENGTH = 0.54;
const float HALO_STRENGTH = 0.44;
const float HAZE_STRENGTH = 0.12;
const float RIM_STRENGTH = 0.88;
const float RIM_RADIUS_PIXELS = 2.0;
const float GLOW_RADIUS_PIXELS = 9.0;
const float PARTICLE_GRID = 11.0;
const vec2 BACKDROP_STRETCH = vec2(1.65, 0.82);
const float SOFT_CENTER_RADIUS = 0.18;
const float PULSE_STRENGTH = 0.07;
const vec3 DARK_BACKGROUND = vec3(0.008, 0.005, 0.014);

float hash(vec2 point) {
    return fract(sin(dot(point, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 point) {
    vec2 cell = floor(point);
    vec2 local = fract(point);
    local = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), local.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0)), local.x), local.y);
}

float haze(vec2 point) {
    return noise(point) * 0.58 + noise(point * 2.03 + 9.2) * 0.28
         + noise(point * 4.01 + 17.4) * 0.14;
}

vec3 particles(vec2 point, vec3 tint, float revealed) {
    vec2 field = point * PARTICLE_GRID;
    field.y += EffectTime * 0.14;
    vec2 cell = floor(field);
    vec2 local = fract(field) - 0.5;
    float seed = hash(cell);
    vec2 center = vec2(hash(cell + 3.1), hash(cell + 7.7)) * 0.64 - 0.32;
    vec2 delta = abs(local - center);
    float size = mix(0.035, 0.095, hash(cell + 13.7));
    float square = 1.0 - smoothstep(size * 0.75, size, max(delta.x, delta.y));
    float enabledParticle = step(0.64, seed);
    float pulse = pow(0.5 + 0.5 * sin(EffectTime * 1.5 + seed * 30.0), 2.0);
    float distanceFade = exp(-dot(point, point) * 2.0);
    float crossShape = max(
        (1.0 - smoothstep(size * 0.2, size * 0.36, delta.x))
            * (1.0 - smoothstep(size, size * 2.6, delta.y)),
        (1.0 - smoothstep(size * 0.2, size * 0.36, delta.y))
            * (1.0 - smoothstep(size, size * 2.6, delta.x)));
    float sparkle = crossShape * revealed * step(0.85, seed) * pulse;
    return tint * enabledParticle * distanceFade * (square * pulse * 0.42 + sparkle * 0.8);
}

float alphaAt(vec2 offset) {
    return texture(ItemSampler, clamp(texCoord + offset, vec2(0.0), vec2(1.0))).a;
}

void main() {
    vec2 effectCoord = texCoord + vec2(0.0, BackdropOffset);
    if (effectCoord.y > 1.0) {
        fragColor = texture(DiffuseSampler, texCoord);
        return;
    }
    float shortSide = max(1.0, min(OutSize.x, OutSize.y));
    vec2 point = (effectCoord - 0.5) * OutSize / shortSide;
    vec2 eye = point / (BACKDROP_STRETCH * vec2(mix(0.92, 1.0, BackdropProgress), 1.0));
    eye.y *= 1.0 + 0.4 * eye.x * eye.x;
    float radiusSquared = dot(eye, eye);
    vec3 tint = clamp(vec3(RarityRed, RarityGreen, RarityBlue), 0.0, 1.0);
    float revealed = smoothstep(0.0, 1.0, RevealProgress);

    float cloud = haze(point * 5.0 + vec2(EffectTime * 0.035, -EffectTime * 0.025));
    float halo = exp(-radiusSquared * 6.0);
    float core = exp(-radiusSquared * 28.0);
    float softCenter = smoothstep(0.0, SOFT_CENTER_RADIUS * SOFT_CENTER_RADIUS, radiusSquared);
    float angle = atan(eye.y, eye.x + 0.000001);
    float rays = 0.9 + sin(angle * 7.0 + cloud * 3.0 + EffectTime * 0.07) * 0.07
                      + sin(angle * 13.0 - EffectTime * 0.05) * 0.03;
    rays = mix(1.0, rays, softCenter);
    float breathing = 1.0 + sin(EffectTime * 1.1) * 0.035
        + PULSE_STRENGTH * sin(EffectTime * 3.7 + cloud * 2.0) * sin(EffectTime * 2.3);
    vec3 background = DARK_BACKGROUND + tint * (
        HALO_STRENGTH * halo * rays + CORE_STRENGTH * core
        + HAZE_STRENGTH * cloud * halo) * breathing;
    background += particles(point, tint, revealed);
    background += mix(tint, vec3(1.0), 0.55)
        * sin(clamp(RevealProgress, 0.0, 1.0) * 3.14159265) * core * 0.22;
    background = mix(DARK_BACKGROUND, background, BackdropProgress);

    vec4 item = texture(ItemSampler, texCoord);
    vec2 rimStep = vec2(RIM_RADIUS_PIXELS) / OutSize;
    float nearAlpha = max(max(alphaAt(vec2(rimStep.x, 0.0)), alphaAt(vec2(-rimStep.x, 0.0))),
                          max(alphaAt(vec2(0.0, rimStep.y)), alphaAt(vec2(0.0, -rimStep.y))));
    vec2 glowStep = vec2(GLOW_RADIUS_PIXELS) / OutSize;
    float farAlpha = (alphaAt(vec2(glowStep.x, 0.0)) + alphaAt(vec2(-glowStep.x, 0.0))
                    + alphaAt(vec2(0.0, glowStep.y)) + alphaAt(vec2(0.0, -glowStep.y))) * 0.25;
    float rim = max(0.0, nearAlpha - item.a);
    float glow = max(0.0, farAlpha - item.a);
    background += tint * (rim * RIM_STRENGTH + glow * 0.3) * EntranceOpacity * RimProgress;

    vec3 itemColor = clamp(item.rgb / max(item.a, 0.001), 0.0, 1.0) * revealed;
    fragColor = vec4(mix(background, itemColor, clamp(item.a * EntranceOpacity, 0.0, 1.0)), 1.0);
}
