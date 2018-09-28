/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hz.help;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RotationUtil {

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_90[] = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
    };
    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f,
    };
    public static final float TEXTURE_ROTATED_270[] = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    private RotationUtil() {
    }

    public static FloatBuffer getRotation(float[] rotatedTex, final boolean flipHorizontal,
                                                         final boolean flipVertical) {
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        FloatBuffer fb = ByteBuffer.allocateDirect(rotatedTex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(rotatedTex);
        fb.position(0);
        return fb;
    }


    private static float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }
}
