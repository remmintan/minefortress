package org.minefortress.renderer;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

class GLU {

    private static final float[] in = new float[4];
    private static final float[] out = new float[4];

    private static final FloatBuffer finalMatrix = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer tempMatrix = BufferUtils.createFloatBuffer(16);

    private static final float[] IDENTITY_MATRIX =
            new float[] {
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f };

    public static boolean gluUnProject(
            float winx,
            float winy,
            float winz,
            FloatBuffer modelMatrix,
            FloatBuffer projMatrix,
            IntBuffer viewport,
            FloatBuffer obj_pos) {
        float[] in = GLU.in;
        float[] out = GLU.out;

        __gluMultMatricesf(modelMatrix, projMatrix, finalMatrix);

        if (!__gluInvertMatrixf(finalMatrix, finalMatrix))
            return false;

        in[0] = winx;
        in[1] = winy;
        in[2] = winz;
        in[3] = 1.0f;

        // Map x and y from window coordinates
        in[0] = (in[0] - viewport.get(viewport.position() + 0)) / viewport.get(viewport.position() + 2);
        in[1] = (in[1] - viewport.get(viewport.position() + 1)) / viewport.get(viewport.position() + 3);

        // Map to range -1 to 1
        in[0] = in[0] * 2 - 1;
        in[1] = in[1] * 2 - 1;
        in[2] = in[2] * 2 - 1;

        __gluMultMatrixVecf(finalMatrix, in, out);

        if (out[3] == 0.0)
            return false;

        out[3] = 1.0f / out[3];

        obj_pos.put(obj_pos.position() + 0, out[0] * out[3]);
        obj_pos.put(obj_pos.position() + 1, out[1] * out[3]);
        obj_pos.put(obj_pos.position() + 2, out[2] * out[3]);

        return true;
    }

    private static void __gluMultMatricesf(FloatBuffer a, FloatBuffer b, FloatBuffer r) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                r.put(r.position() + i*4 + j,
                        a.get(a.position() + i*4 + 0) * b.get(b.position() + 0*4 + j) + a.get(a.position() + i*4 + 1) * b.get(b.position() + 1*4 + j) + a.get(a.position() + i*4 + 2) * b.get(b.position() + 2*4 + j) + a.get(a.position() + i*4 + 3) * b.get(b.position() + 3*4 + j));
            }
        }
    }

    private static boolean __gluInvertMatrixf(FloatBuffer src, FloatBuffer inverse) {
        int i, j, k, swap;
        float t;
        FloatBuffer temp = GLU.tempMatrix;


        for (i = 0; i < 16; i++) {
            temp.put(i, src.get(i + src.position()));
        }
        __gluMakeIdentityf(inverse);

        for (i = 0; i < 4; i++) {
            /*
             * * Look for largest element in column
             */
            swap = i;
            for (j = i + 1; j < 4; j++) {
                /*
                 * if (fabs(temp[j][i]) > fabs(temp[i][i])) { swap = j;
                 */
                if (Math.abs(temp.get(j*4 + i)) > Math.abs(temp.get(i* 4 + i))) {
                    swap = j;
                }
            }

            if (swap != i) {
                /*
                 * * Swap rows.
                 */
                for (k = 0; k < 4; k++) {
                    t = temp.get(i*4 + k);
                    temp.put(i*4 + k, temp.get(swap*4 + k));
                    temp.put(swap*4 + k, t);

                    t = inverse.get(i*4 + k);
                    inverse.put(i*4 + k, inverse.get(swap*4 + k));
                    //inverse.put((i << 2) + k, inverse.get((swap << 2) + k));
                    inverse.put(swap*4 + k, t);
                    //inverse.put((swap << 2) + k, t);
                }
            }

            if (temp.get(i*4 + i) == 0) {
                /*
                 * * No non-zero pivot. The matrix is singular, which shouldn't *
                 * happen. This means the user gave us a bad matrix.
                 */
                return false;
            }

            t = temp.get(i*4 + i);
            for (k = 0; k < 4; k++) {
                temp.put(i*4 + k, temp.get(i*4 + k)/t);
                inverse.put(i*4 + k, inverse.get(i*4 + k)/t);
            }
            for (j = 0; j < 4; j++) {
                if (j != i) {
                    t = temp.get(j*4 + i);
                    for (k = 0; k < 4; k++) {
                        temp.put(j*4 + k, temp.get(j*4 + k) - temp.get(i*4 + k) * t);
                        inverse.put(j*4 + k, inverse.get(j*4 + k) - inverse.get(i*4 + k) * t);
						/*inverse.put(
							(j << 2) + k,
							inverse.get((j << 2) + k) - inverse.get((i << 2) + k) * t);*/
                    }
                }
            }
        }
        return true;
    }

    private static void __gluMultMatrixVecf(FloatBuffer m, float[] in, float[] out) {
        for (int i = 0; i < 4; i++) {
            out[i] =
                    in[0] * m.get(m.position() + 0*4 + i)
                            + in[1] * m.get(m.position() + 1*4 + i)
                            + in[2] * m.get(m.position() + 2*4 + i)
                            + in[3] * m.get(m.position() + 3*4 + i);

        }
    }

    private static void __gluMakeIdentityf(FloatBuffer m) {
        int oldPos = m.position();
        m.put(IDENTITY_MATRIX);
        m.position(oldPos);
    }

}
