/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */

package yancey.bbsaekeyframe.util;

import com.mojang.logging.LogUtils;
import mchorse.bbs_mod.camera.Camera;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AEKeyframeGenerator {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static @Nullable String lastKeyframeStr = null;
    private int videoHeight;
    private Path path;
    private StringBuilder aeKeyframeStr, zoomStr, expressionStr, orientationStr, positionStr;
    private int count;

    public void startRecording(Path path, int videoWidth, int videoHeight, double frameRate) {
        this.path = path;
        this.videoHeight = videoHeight;
        aeKeyframeStr = new StringBuilder();
        zoomStr = new StringBuilder();
        expressionStr = new StringBuilder();
        orientationStr = new StringBuilder();
        positionStr = new StringBuilder();
        aeKeyframeStr.append("Adobe After Effects 8.0 Keyframe Data\n");
        aeKeyframeStr.append(String.format("\tUnits Per Second\t%.2f\n", frameRate));
        aeKeyframeStr.append(String.format("\tSource Width\t%d\n", videoWidth));
        aeKeyframeStr.append(String.format("\tSource Height\t%d\n", videoHeight));
        aeKeyframeStr.append("""
                \tSource Pixel Aspect Ratio\t1
                \tComp Pixel Aspect Ratio\t1
                """);
        zoomStr.append("Camera Options\tZoom\n\tFrame\n");
        expressionStr.append("""
                Expression Data
                // Keep fov value when scaling composite.
                """);
        expressionStr.append(String.format("thisComp.height / %d * cameraOption.zoom\n", videoHeight));
        expressionStr.append("""
                End of Expression Data
                Transform\tPoint of Interest
                \tFrame
                \t\t0\t0\t0
                Expression Data
                // These tracking data only support one-node camera
                transform.position
                End of Expression Data
                """);
        orientationStr.append("Transform\tOrientation\n\tFrame\n");
        positionStr.append("Transform\tPosition\n\tFrame\n");
        count = 0;
    }

    public void stopRecording() {
        aeKeyframeStr.append(zoomStr);
        aeKeyframeStr.append(expressionStr);
        aeKeyframeStr.append(orientationStr);
        aeKeyframeStr.append(positionStr);
        aeKeyframeStr.append("End of Keyframe Data\n");
        lastKeyframeStr = aeKeyframeStr.toString();
        try {
            Files.writeString(path, lastKeyframeStr);
        } catch (IOException e) {
            LOGGER.warn("Failed to write keyframe data to file", e);
        }
    }

    public void recordFrame(Camera camera) {
        // Zoom
        // make sure 1 unit in AE equal 1 block in minecraft
        zoomStr.append('\t').append(count).append('\t')
                .append((videoHeight / 2.0) / Math.tan(camera.fov / 2))
                .append('\n');

        // Orientation
        Matrix4f mat = new Matrix4f()
                // transform to AE coordinate system
                .rotateX((float) Math.PI)
                // apply camera rotation
                .rotateY(camera.rotation.y)
                .rotateX(-camera.rotation.x)
                .rotateZ(camera.rotation.z);
        Vector3f rotation = new Vector3f();
        mat.getEulerAnglesXYZ(rotation); // AE use x-y-z euler angles
        orientationStr.append('\t').append(count)
                .append('\t').append(Math.toDegrees(rotation.x))
                .append('\t').append(Math.toDegrees(rotation.y))
                .append('\t').append(Math.toDegrees(rotation.z))
                .append('\n');

        // Position
        positionStr.append('\t').append(count)
                .append('\t').append(camera.position.x)
                .append('\t').append(camera.position.y)
                .append('\t').append(camera.position.z)
                .append('\n');

        count++;
    }

}
