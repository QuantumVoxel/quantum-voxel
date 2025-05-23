// ==Blockbench Plugin==
(() => {
    Plugin.register('g3dj_exporter', {
        title: 'G3DJ Exporter',
        icon: 'icon-export',
        author: 'XyperCode & ChatGPT',
        description: 'Export models to G3DJ format for Quantum Voxel or LibGDX.',
        version: '1.0.0',
        variant: 'desktop',
        onload() {

            const codec = new Codec('g3dj', {
                name: 'G3DJ Model',
                extension: 'g3dj',
                remember: true,
                compile(options) {
                    return JSON.stringify(encodeG3DJ(Project), null, 2);
                }
            });
        },
        onunload() {}
    });

    const G3DJFormat = new ModelFormat({
        id: 'g3dj_format',
        name: 'G3DJ Model',
        extension: 'g3dj',
        icon: 'icon_model',
        format: 'json',
        model_id: 'g3dj_model',

        // Default template
        default_name: 'g3dj_model',
        default_export_name: 'model',
        animation_mode: 'timeline',
        bone_rig: false,

        // Export: Blockbench project → G3DJ JSON
        export(model) {
            const output = G3DJCodec.encodeModel(model);
            return autoStringify(output);
        },

        // Import: G3DJ JSON → Blockbench model
        parse(model, data, add) {
            G3DJCodec.decodeModel(data, add);
        }
    });

    ModelFormat.register(G3DJFormat);
    
    function encodeG3DJ(project) {
        const meshes = [];
        const nodes = [];
        const materials = [];
        const mesh = {
            attributes: ['POSITION', 'NORMAL', 'TEXCOORD0'],
            vertices: [],
            parts: []
        };
        let vertexOffset = 0;

        let materialMap = {};
        let materialId = 'default_material';
        let textureName = 'default.png';

        if (Textures[0]) {
            textureName = Textures[0].name;
            materialId = textureName;
            materialMap[textureName] = true;
        }

        Outliner.traverse(cube => {
            if (!(cube instanceof Cube)) return;

            const partId = cube.name || `part${mesh.parts.length}`;
            const { vertices, normals, uvs } = getCubeMeshData(cube);

            const v = [];
            for (let i = 0; i < vertices.length; i++) {
                v.push(...vertices[i], ...normals[i], ...uvs[i]);
            }

            mesh.vertices.push(...v.flat());

            const indices = [];
            for (let i = 0; i < 6; i++) {
                const offset = vertexOffset + i * 4;
                indices.push(
                    offset, offset + 1, offset + 2,
                    offset + 2, offset + 3, offset
                );
            }
            vertexOffset += 24;

            mesh.parts.push({
                id: partId,
                type: 'TRIANGLES',
                indices
            });

            nodes.push({
                id: cube.name,
                translation: cube.origin,
                rotation: [0, 0, 0, 1],
                parts: [{
                    meshpartid: partId,
                    materialid: materialId,
                    uvMapping: [[]]
                }]
            });
        });

        meshes.push(mesh);

        materials.push({
            id: materialId,
            ambient: [0.2, 0.2, 0.2],
            diffuse: [0.8, 0.8, 0.8],
            emissive: [0, 0, 0],
            opacity: 1,
            textures: [{
                id: materialId,
                filename: textureName,
                type: 'DIFFUSE'
            }]
        });

        return {
            version: [0, 1],
            id: project.name,
            meshes,
            materials,
            nodes,
            animations: getG3DJAnimations()
        };
    }

    function getCubeMeshData(cube) {
        const from = cube.from;
        const to = cube.to;
        const size = [
            to[0] - from[0],
            to[1] - from[1],
            to[2] - from[2]
        ];

        const [x, y, z] = from;
        const [dx, dy, dz] = size;

        // Basic cube vertex positions (6 faces × 4 vertices)
        const faceVerts = [
            // Right
            [[x+dx,y,z], [x+dx,y+dy,z], [x+dx,y+dy,z+dz], [x+dx,y,z+dz]],
            // Left
            [[x,y,z], [x,y,y+dy], [x,y+dy,z+dz], [x,y,z+dz]],
            // Top
            [[x,y+dy,z], [x+dx,y+dy,z], [x+dx,y+dy,z+dz], [x,y+dy,z+dz]],
            // Bottom
            [[x,y,z], [x+dx,y,z], [x+dx,y,z+dz], [x,y,z+dz]],
            // Front
            [[x,y,z+dz], [x+dx,y,z+dz], [x+dx,y+dy,z+dz], [x,y+dy,z+dz]],
            // Back
            [[x,y,z], [x+dx,y,z], [x+dx,y+dy,z], [x,y+dy,z]]
        ];

        const faceNormals = [
            [1, 0, 0], [-1, 0, 0],
            [0, 1, 0], [0, -1, 0],
            [0, 0, 1], [0, 0, -1]
        ];

        const faceUVs = [
            [[0,0],[1,0],[1,1],[0,1]],
            [[0,0],[1,0],[1,1],[0,1]],
            [[0,0],[1,0],[1,1],[0,1]],
            [[0,0],[1,0],[1,1],[0,1]],
            [[0,0],[1,0],[1,1],[0,1]],
            [[0,0],[1,0],[1,1],[0,1]],
        ];

        return {
            vertices: faceVerts.flat(),
            normals: faceNormals.flatMap(n => Array(4).fill(n)),
            uvs: faceUVs.flat()
        };
    }

    function getG3DJAnimations() {
        if (!Animator.animations.length) return [];
        return Animator.animations.map(animation => ({
            id: `animation.${animation.name}`,
            bones: animation.animators.map(animator => ({
                boneId: animator.name,
                keyframes: animator.keyframes.map(kf => {
                    const data = { keytime: kf.time };
                    if (kf.rotation) data.rotation = kf.rotation;
                    if (kf.translation) data.translation = kf.translation;
                    if (kf.scale) data.scale = kf.scale;
                    return data;
                })
            }))
        }));
    }
})();