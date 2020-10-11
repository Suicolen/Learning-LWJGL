package main.rs.model;




import main.rs.buffer.Buffer;

public class Model {

    public static Model decode(byte[] data) {
        Model model = new Model();
        if (data[data.length - 1] == -1 && data[data.length - 2] == -1) {
            model.decode_new(data);
        } else {
            model.decode_old(data);
        }

        return model;
    }


    public void decode_old(byte[] data) {
        boolean has_face_type = false;
        boolean has_texture_type = false;
        Buffer first = new Buffer(data);
        Buffer second = new Buffer(data);
        Buffer third = new Buffer(data);
        Buffer fourth = new Buffer(data);
        Buffer fifth = new Buffer(data);
        first.pos = data.length - 18;
        vertices = first.get_unsigned_short();
        faces = first.get_unsigned_short();
        texture_faces = first.get_unsigned_byte();
        int model_render_type_opcode = first.get_unsigned_byte();
        int model_render_priority_opcode = first.get_unsigned_byte();
        int model_alpha_opcode = first.get_unsigned_byte();
        int model_muscle_opcode = first.get_unsigned_byte();
        int model_bones_opcode = first.get_unsigned_byte();
        int model_vertex_x = first.get_unsigned_short();
        int model_vertex_y = first.get_unsigned_short();
        int model_vertex_z = first.get_unsigned_short();
        int model_vertex_points = first.get_unsigned_short();
        int pos = 0;

        int vertex_flag_offset = pos;
        pos += vertices;

        int model_face_compress_type_offset = pos;
        pos += faces;

        int model_face_pri_offset = pos;
        if (model_render_priority_opcode == 255)
            pos += faces;

        int model_muscle_offset = pos;
        if (model_muscle_opcode == 1)
            pos += faces;

        int model_render_type_offset = pos;
        if (model_render_type_opcode == 1)
            pos += faces;

        int model_bones_offset = pos;
        if (model_bones_opcode == 1)
            pos += vertices;

        int model_alpha_offset = pos;
        if (model_alpha_opcode == 1)
            pos += faces;

        int model_points_offset = pos;
        pos += model_vertex_points;

        int model_color_offset = pos;
        pos += faces * 2;

        int model_simple_texture_offset = pos;
        pos += texture_faces * 6;

        int model_vertex_x_offset = pos;
        pos += model_vertex_x;

        int model_vertex_y_offset = pos;
        pos += model_vertex_y;

        int model_vertex_z_offset = pos;
        pos += model_vertex_z;

        particle_vertices = new int[vertices];
        vertex_x = new int[vertices];
        vertex_y = new int[vertices];
        vertex_z = new int[vertices];
        triangle_edge_a = new int[faces];
        triangle_edge_b = new int[faces];
        triangle_edge_c = new int[faces];
        if (texture_faces > 0) {
            texture_map = new byte[texture_faces];
            triangle_texture_edge_a = new short[texture_faces];
            triangle_texture_edge_b = new short[texture_faces];
            triangle_texture_edge_c = new short[texture_faces];
        }

        if (model_bones_opcode == 1)
            bone_skin = new int[vertices];

        if (model_render_type_opcode == 1) {
            render_type = new int[faces];
            face_texture = new byte[faces];
            face_material = new short[faces];
        }

        if (model_render_priority_opcode == 255)
            render_priorities = new byte[faces];
        else
            model_pri = (byte) model_render_priority_opcode;

        if (model_alpha_opcode == 1)
            face_alpha = new int[faces];

        if (model_muscle_opcode == 1)
            muscle_skin = new int[faces];

        face_color = new short[faces];
        first.pos = vertex_flag_offset;
        second.pos = model_vertex_x_offset;
        third.pos = model_vertex_y_offset;
        fourth.pos = model_vertex_z_offset;
        fifth.pos = model_bones_offset;
        int start_x = 0;
        int start_y = 0;
        int start_z = 0;
        for (int point = 0; point < vertices; point++) {
            int position_mask = first.get_unsigned_byte();
            int x = 0;
            if ((position_mask & 0x1) != 0)
                x = second.get_signed_smart();
            int y = 0;
            if ((position_mask & 0x2) != 0)
                y = third.get_signed_smart();
            int z = 0;
            if ((position_mask & 0x4) != 0)
                z = fourth.get_signed_smart();

            vertex_x[point] = start_x + x;
            vertex_y[point] = start_y + y;
            vertex_z[point] = start_z + z;
            start_x = vertex_x[point];
            start_y = vertex_y[point];
            start_z = vertex_z[point];
            if (model_bones_opcode == 1)
                bone_skin[point] = fifth.get_unsigned_byte();

        }
        first.pos = model_color_offset;
        second.pos = model_render_type_offset;
        third.pos = model_face_pri_offset;
        fourth.pos = model_alpha_offset;
        fifth.pos = model_muscle_offset;
        for (int face = 0; face < faces; face++) {
            face_color[face] = (short) first.get_unsigned_short();
            ;

            if (model_render_type_opcode == 1) {
                int render_mask = second.get_unsigned_byte();
                if ((render_mask & 0x1) == 1) {
                    render_type[face] = 1;
                    has_face_type = true;
                } else {
                    render_type[face] = 0;
                }

                if ((render_mask & 0x2) != 0) {
                    face_texture[face] = (byte) (render_mask >> 2);
                    face_material[face] = face_color[face];
                    face_color[face] = 127;
                    if (face_material[face] != -1)
                        has_texture_type = true;


                } else {
                    face_texture[face] = -1;
                    face_material[face] = -1;
                }
            }
            if (model_render_priority_opcode == 255)
                render_priorities[face] = third.get_signed_byte();

            if (model_alpha_opcode == 1) {
                face_alpha[face] = fourth.get_signed_byte();
                if (face_alpha[face] < 0)
                    face_alpha[face] = (256 + face_alpha[face]);

            }
            if (model_muscle_opcode == 1)
                muscle_skin[face] = fifth.get_unsigned_byte();

        }
        first.pos = model_points_offset;
        second.pos = model_face_compress_type_offset;
        int coordinate_a = 0;
        int coordinate_b = 0;
        int coordinate_c = 0;
        int offset = 0;
        int coordinate;
        for (int face = 0; face < faces; face++) {
            int opcode = second.get_unsigned_byte();
            if (opcode == 1) {
                coordinate_a = (first.get_signed_smart() + offset);
                offset = coordinate_a;
                coordinate_b = (first.get_signed_smart() + offset);
                offset = coordinate_b;
                coordinate_c = (first.get_signed_smart() + offset);
                offset = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 2) {
                coordinate_b = coordinate_c;
                coordinate_c = (first.get_signed_smart() + offset);
                offset = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 3) {
                coordinate_a = coordinate_c;
                coordinate_c = (first.get_signed_smart() + offset);
                offset = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 4) {
                coordinate = coordinate_a;
                coordinate_a = coordinate_b;
                coordinate_b = coordinate;
                coordinate_c = (first.get_signed_smart() + offset);
                offset = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
        }
        first.pos = model_simple_texture_offset;
        for (int face = 0; face < texture_faces; face++) {
            texture_map[face] = 0;
            triangle_texture_edge_a[face] = (short) first.get_unsigned_short();
            triangle_texture_edge_b[face] = (short) first.get_unsigned_short();
            triangle_texture_edge_c[face] = (short) first.get_unsigned_short();
        }
        if (face_texture != null) {
            boolean textured = false;
            for (int face = 0; face < faces; face++) {
                coordinate = face_texture[face] & 0xff;
                if (coordinate != 255) {
                    if (((triangle_texture_edge_a[coordinate] & 0xffff) == triangle_edge_a[face]) && ((triangle_texture_edge_b[coordinate] & 0xffff) == triangle_edge_b[face]) && ((triangle_texture_edge_c[coordinate] & 0xffff) == triangle_edge_c[face])) {
                        face_texture[face] = -1;
                    } else {
                        textured = true;
                    }
                }
            }
            if (!textured)
                face_texture = null;
        }
        if (!has_texture_type)
            face_material = null;

        if (!has_face_type)
            render_type = null;


		/*if(model_id == 9638) {
			this.aClass158Array3788 = new ModelParticleEmitter[1];
			for (int i_198_ = 0; i_198_ < 1; i_198_++) {
				final int particleId = 0;
				final int i_200_ = 1;
				this.aClass158Array3788[i_198_] = new ModelParticleEmitter(particleId, this.edge_a[i_200_], this.edge_b[i_200_], this.edge_c[i_200_]);
			}
		}*/

    }

    public void decode_new(byte data[]) {
        Buffer first = new Buffer(data);
        Buffer second = new Buffer(data);
        Buffer third = new Buffer(data);
        Buffer fourth = new Buffer(data);
        Buffer fifth = new Buffer(data);
        Buffer sixth = new Buffer(data);
        Buffer seventh = new Buffer(data);

        first.pos = data.length - 23;
        vertices = first.get_unsigned_short();
        faces = first.get_unsigned_short();
        texture_faces = first.get_unsigned_byte();


        // System.err.println("Vertices: " + vertices + " | Faces: " + faces + " | Texture faces: " + texture_faces);

        header_data = data;
        header_vertices = vertices;
        header_faces = faces;
        header_texture_faces = texture_faces;

        int model_render_type_opcode = first.get_unsigned_byte();//texture flag 00 false, 01+ true
        int model_priority_opcode = first.get_unsigned_byte();
        int model_alpha_opcode = first.get_unsigned_byte();
        int model_muscle_opcode = first.get_unsigned_byte();
        int model_texture_opcode = first.get_unsigned_byte();
        int model_bones_opcode = first.get_unsigned_byte();
        int model_vertex_x = first.get_unsigned_short();
        int model_vertex_y = first.get_unsigned_short();
        int model_vertex_z = first.get_unsigned_short();
        int model_vertex_points = first.get_unsigned_short();
        int model_texture_indices = first.get_unsigned_short();
        int texture_id_simple = 0;
        int texture_id_complex = 0;
        int texture_id_cube = 0;
        int face;
        if (texture_faces > 0) {
            texture_map = new byte[texture_faces];
            first.pos = 0;
            for (face = 0; face < texture_faces; face++) {
                byte opcode = texture_map[face] = first.get_signed_byte();
                if (opcode == 0) {
                    texture_id_simple++;
                }
                if (opcode >= 1 && opcode <= 3) {
                    texture_id_complex++;
                }
                if (opcode == 2) {
                    texture_id_cube++;
                }

            }
        }
        int pos = texture_faces;

        int model_vertex_offset = pos;
        pos += vertices;

        int model_render_type_offset = pos;
        if (model_render_type_opcode == 1)
            pos += faces;

        int model_face_offset = pos;
        pos += faces;

        int model_face_priorities_offset = pos;
        if (model_priority_opcode == 255)
            pos += faces;

        int model_muscle_offset = pos;
        if (model_muscle_opcode == 1)
            pos += faces;

        int model_bones_offset = pos;
        if (model_bones_opcode == 1)
            pos += vertices;

        int model_alpha_offset = pos;
        if (model_alpha_opcode == 1)
            pos += faces;

        int model_points_offset = pos;
        pos += model_vertex_points;

        int model_texture_id = pos;
        if (model_texture_opcode == 1)
            pos += faces * 2;

        int model_texture_coordinate_offset = pos;
        pos += model_texture_indices;

        int model_color_offset = pos;
        pos += faces * 2;

        int model_vertex_x_offset = pos;
        pos += model_vertex_x;

        int model_vertex_y_offset = pos;
        pos += model_vertex_y;

        int model_vertex_z_offset = pos;
        pos += model_vertex_z;

        int model_simple_texture_offset = pos;
        pos += texture_id_simple * 6;

        int model_complex_texture_offset = pos;
        pos += texture_id_complex * 6;

        int model_texture_scale_offset = pos;
        pos += texture_id_complex * 6;

        int model_texture_rotation_offset = pos;
        pos += texture_id_complex * 2;

        int model_texture_direction_offset = pos;
        pos += texture_id_complex;

        int model_texture_translate_offset = pos;
        pos += texture_id_complex * 2 + texture_id_cube * 2;

        particle_vertices = new int[vertices];
        vertex_x = new int[vertices];
        vertex_y = new int[vertices];
        vertex_z = new int[vertices];
        triangle_edge_a = new int[faces];
        triangle_edge_b = new int[faces];
        triangle_edge_c = new int[faces];
        if (model_bones_opcode == 1)
            bone_skin = new int[vertices];

        if (model_render_type_opcode == 1)
            render_type = new int[faces];

        if (model_priority_opcode == 255)
            render_priorities = new byte[faces];
        else
            model_pri = (byte) model_priority_opcode;

        if (model_alpha_opcode == 1)
            face_alpha = new int[faces];

        if (model_muscle_opcode == 1)
            muscle_skin = new int[faces];

        if (model_texture_opcode == 1)
            face_material = new short[faces];

        if (model_texture_opcode == 1 && texture_faces > 0)
            face_texture = new byte[faces];

        face_color = new short[faces];
        if (texture_faces > 0) {
            triangle_texture_edge_a = new short[texture_faces];
            triangle_texture_edge_b = new short[texture_faces];
            triangle_texture_edge_c = new short[texture_faces];
        }
        first.pos = model_vertex_offset;
        second.pos = model_vertex_x_offset;
        third.pos = model_vertex_y_offset;
        fourth.pos = model_vertex_z_offset;
        fifth.pos = model_bones_offset;
        int start_x = 0;
        int start_y = 0;
        int start_z = 0;
        for (int point = 0; point < vertices; point++) {
            int position_mask = first.get_unsigned_byte();
            int x = 0;
            if ((position_mask & 1) != 0) {
                x = second.get_signed_smart();
            }
            int y = 0;
            if ((position_mask & 2) != 0) {
                y = third.get_signed_smart();
            }
            int z = 0;
            if ((position_mask & 4) != 0) {
                z = fourth.get_signed_smart();
            }
            vertex_x[point] = start_x + x;
            vertex_y[point] = start_y + y;
            vertex_z[point] = start_z + z;
            start_x = vertex_x[point];
            start_y = vertex_y[point];
            start_z = vertex_z[point];
            if (bone_skin != null)
                bone_skin[point] = fifth.get_unsigned_byte();

        }
        first.pos = model_color_offset;
        second.pos = model_render_type_offset;
        third.pos = model_face_priorities_offset;
        fourth.pos = model_alpha_offset;
        fifth.pos = model_muscle_offset;
        sixth.pos = model_texture_id;
        seventh.pos = model_texture_coordinate_offset;
        for (face = 0; face < faces; face++) {
            face_color[face] = (short) (first.get_unsigned_short() & 0xFFFF);
            if (model_render_type_opcode == 1) {
                render_type[face] = second.get_signed_byte();
            }
            if (model_priority_opcode == 255) {
                render_priorities[face] = third.get_signed_byte();
            }
            if (model_alpha_opcode == 1) {
                face_alpha[face] = fourth.get_signed_byte();
                if (face_alpha[face] < 0)
                    face_alpha[face] = (256 + face_alpha[face]);

            }
            if (model_muscle_opcode == 1)
                muscle_skin[face] = fifth.get_unsigned_byte();

            if (model_texture_opcode == 1) {
                face_material[face] = (short) (sixth.get_unsigned_short() - 1);
                if (face_material[face] >= 0) {
                    if (render_type != null) {
                        if (render_type[face] < 2
                                && face_color[face] != 127
                                && face_color[face] != -27075
                                && face_color[face] != 8128
                                && face_color[face] != 7510) {
                            face_material[face] = -1;
                        }
                    }
                }
                if (face_material[face] != -1)
                    face_color[face] = 127;

            }
            if (face_texture != null && face_material[face] != -1) {
                face_texture[face] = (byte) (seventh.get_unsigned_byte() - 1);
            }
        }
        first.pos = model_points_offset;
        second.pos = model_face_offset;
        int coordinate_a = 0;
        int coordinate_b = 0;
        int coordinate_c = 0;
        int last_coordinate = 0;
        for (face = 0; face < faces; face++) {
            int opcode = second.get_unsigned_byte();
            if (opcode == 1) {
                coordinate_a = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_a;
                coordinate_b = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_b;
                coordinate_c = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 2) {
                coordinate_b = coordinate_c;
                coordinate_c = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 3) {
                coordinate_a = coordinate_c;
                coordinate_c = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
            if (opcode == 4) {
                int l14 = coordinate_a;
                coordinate_a = coordinate_b;
                coordinate_b = l14;
                coordinate_c = first.get_signed_smart() + last_coordinate;
                last_coordinate = coordinate_c;
                triangle_edge_a[face] = coordinate_a;
                triangle_edge_b[face] = coordinate_b;
                triangle_edge_c[face] = coordinate_c;
            }
        }
        first.pos = model_simple_texture_offset;
        second.pos = model_complex_texture_offset;
        third.pos = model_texture_scale_offset;
        fourth.pos = model_texture_rotation_offset;
        fifth.pos = model_texture_direction_offset;
        sixth.pos = model_texture_translate_offset;
        for (face = 0; face < texture_faces; face++) {
            int opcode = texture_map[face] & 0xff;
            if (opcode == 0) {
                triangle_texture_edge_a[face] = (short) first.get_unsigned_short();
                triangle_texture_edge_b[face] = (short) first.get_unsigned_short();
                triangle_texture_edge_c[face] = (short) first.get_unsigned_short();
            }
            if (opcode == 1) {
                triangle_texture_edge_a[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_b[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_c[face] = (short) second.get_unsigned_short();
            }
            if (opcode == 2) {
                triangle_texture_edge_a[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_b[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_c[face] = (short) second.get_unsigned_short();
            }
            if (opcode == 3) {
                triangle_texture_edge_a[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_b[face] = (short) second.get_unsigned_short();
                triangle_texture_edge_c[face] = (short) second.get_unsigned_short();
            }
        }
        first.pos = pos;
        face = first.get_unsigned_byte();
        if (face != 0) {
            first.get_unsigned_short();
            first.get_unsigned_short();
            first.get_unsigned_short();
            first.get_int();
        }
    }

    public int hsbToRGB(int hsb) {
        float h = hsb >> 10 & 0x3f;
        float s = hsb >> 7 & 0x07;
        float b = hsb & 0x7f;
        return java.awt.Color.HSBtoRGB(h / 63, s / 7, b / 127);
    }

    //*Added*//
    public short[] face_material;
    public byte[] face_texture;
    public byte[] texture_map;

    public int[] particle_vertices;

    public static int anInt1620;
    public static Model EMPTY_MODEL = new Model();
    private static int replace_vertex_x[] = new int[2000];
    private static int replace_vertex_y[] = new int[2000];
    private static int replace_vertex_z[] = new int[2000];
    private static int replace_face_alpha[] = new int[2000];
    public int vertices;
    public int vertex_x[];
    public int vertex_y[];
    public int vertex_z[];
    public int faces;
    public int triangle_edge_a[];
    public int triangle_edge_b[];
    public int triangle_edge_c[];
    public int triangle_hue_a[];
    public int triangle_hue_b[];
    public int triangle_hue_c[];
    public int render_type[];
    public byte render_priorities[];
    public int face_alpha[];
    public short face_color[];
    public byte model_pri = 0;
    public int texture_faces;
    public short triangle_texture_edge_a[];
    public short triangle_texture_edge_b[];
    public short triangle_texture_edge_c[];
    public int min_x;
    public int max_x;
    public int max_z;
    public int min_z;
    public int diagonal_2D;
    public int max_y;
    public int scene_depth;
    public int diagonal_3D;
    public int obj_height;
    public int bone_skin[];
    public int muscle_skin[];
    public int vertex_skin[][];
    public int face_skin[][];
    public boolean within_tile;

    /**
     * Model header vars start
     */
    public byte header_data[];
    public int header_vertices;
    public int header_faces;
    public int header_texture_faces;
    public int vertex_offset;
    public int vertex_x_offset;
    public int vertex_y_offset;
    public int vertex_z_offset;
    public int bones_offset;
    public int points_offset;
    public int face_offset;
    public int color_id;
    public int render_type_offset;
    public int face_pri_offset;
    public int alpha_offset;
    public int muscle_offset;
    public int texture_id;
    /**
     * Model header vars end
     */

    static int depth_indices[] = new int[1600];//1500
    static int face_indices[][] = new int[1600][512];//1500 / 512 //anIntArrayArray3809 //64
    static int animation_roll;
    static int animation_pitch;
    static int animation_yaw;


}
