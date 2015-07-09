#version 150 core

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

in vec4 in_Position;
in vec4 in_Color;
in vec3 in_Normal;
in vec2 in_TextureCoord;

out vec4 pass_Color;
out vec2 pass_TextureCoord;
out vec3 out_Normal;

void main(void) {
	
	// position, color and texture
	gl_Position = projectionMatrix * viewMatrix * modelMatrix * in_Position;
	pass_Color = in_Color;
	pass_TextureCoord = in_TextureCoord;
	
	// normals, use inverted transposed model view matrix
	vec4 N = transpose(inverse(viewMatrix * modelMatrix)) * vec4(in_Normal,0);
	out_Normal = normalize(N.xyz);
}