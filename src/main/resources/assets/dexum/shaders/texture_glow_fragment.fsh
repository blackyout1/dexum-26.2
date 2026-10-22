#version 150

layout(std140) uniform Uniforms {
    mat4 uProjection;
    vec4 uRect;
    vec4 uColor;
    vec4 uRadius_Padding;
    vec4 uZ_Padding;
    vec4 uGlow_Padding; // x = glow size, y = glow intensity, z = unused, w = unused
};

in vec2 vUV;
in vec2 vPos;
out vec4 fragColor;

uniform sampler2D Sampler0;

float sdRoundedRect(vec2 p, vec2 b, float r) {
    vec2 q = abs(p - b) - b + r;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

void main() {
    vec2 size = uRect.zw;
    float dist = sdRoundedRect(vPos, size * 0.5, min(uRadius_Padding.x, min(size.x, size.y) * 0.5));
    float alpha = 1.0 - smoothstep(-1.0, 0.0, dist);
    
    if (alpha <= 0.0) discard;

    vec4 texColor = texture(Sampler0, vUV);
    
    // Свечение: сэмплируем текстуру вокруг для создания эффекта свечения
    float glowSize = uGlow_Padding.x;
    float glowIntensity = uGlow_Padding.y;
    vec2 texelSize = 1.0 / textureSize(Sampler0, 0);
    
    float glow = 0.0;
    int samples = 8;
    for (int i = 0; i < samples; i++) {
        float angle = float(i) * 6.28318 / float(samples);
        vec2 offset = vec2(cos(angle), sin(angle)) * texelSize * glowSize;
        glow += texture(Sampler0, vUV + offset).a;
    }
    glow /= float(samples);
    
    // Комбинируем текстуру и свечение
    vec3 finalColor = texColor.rgb * uColor.rgb;
    float finalAlpha = max(texColor.a, glow * glowIntensity);
    
    // Если свечение сильнее текстуры, делаем цвет белым
    if (glow > texColor.a && glow > 0.1) {
        finalColor = mix(finalColor, vec3(1.0), (glow - texColor.a) * glowIntensity);
    }
    
    fragColor = vec4(finalColor, finalAlpha * uColor.a);
    fragColor.a *= alpha;
}
