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
    private static final String LINE_SEPARATOR = System.lineSeparator();
    public static @Nullable String lastKeyframeStr = null;
    private int videoHeight;
    private Path path;
    private StringBuilder aeKeyframeStr, zoomStr, expressionStr, orientationStr, positionStr;
    private int count;

    public void startRecording(Path path, int videoWidth, int videoHeight, double frameRate) {
        this.path = path;
        this.videoHeight = videoHeight;
        this.aeKeyframeStr = new StringBuilder();
        this.zoomStr = new StringBuilder();
        this.expressionStr = new StringBuilder();
        this.orientationStr = new StringBuilder();
        this.positionStr = new StringBuilder();
        this.count = 0;
        aeKeyframeStr.append("Adobe After Effects 8.0 Keyframe Data").append(LINE_SEPARATOR);
        aeKeyframeStr.append("\tUnits Per Second\t").append(frameRate).append(LINE_SEPARATOR);
        aeKeyframeStr.append("\tSource Width\t").append(videoWidth).append(LINE_SEPARATOR);
        aeKeyframeStr.append("\tSource Height\t").append(videoWidth).append(LINE_SEPARATOR);
        aeKeyframeStr.append("\tSource Pixel Aspect Ratio\t1").append(LINE_SEPARATOR);
        aeKeyframeStr.append("\tComp Pixel Aspect Ratio\t1").append(LINE_SEPARATOR);
        zoomStr.append("Camera Options\tZoom").append(LINE_SEPARATOR).append("\tFrame").append(LINE_SEPARATOR);
        expressionStr.append("Expression Data").append(LINE_SEPARATOR);
        expressionStr.append("// Keep fov value when scaling composite.").append(LINE_SEPARATOR);
        expressionStr.append("thisComp.height / ").append(videoHeight).append(" * cameraOption.zoom").append(LINE_SEPARATOR);
        expressionStr.append("End of Expression Data").append(LINE_SEPARATOR);
        expressionStr.append("Transform\tPoint of Interest").append(LINE_SEPARATOR);
        expressionStr.append("\tFrame").append(LINE_SEPARATOR);
        expressionStr.append("\t\t0\t0\t0").append(LINE_SEPARATOR);
        expressionStr.append("Expression Data").append(LINE_SEPARATOR);
        expressionStr.append("// These tracking data only support one-node camera").append(LINE_SEPARATOR);
        expressionStr.append("transform.position").append(LINE_SEPARATOR);
        expressionStr.append("End of Expression Data").append(LINE_SEPARATOR);
        orientationStr.append("Transform\tOrientation").append(LINE_SEPARATOR);
        orientationStr.append("\tFrame").append(LINE_SEPARATOR);
        positionStr.append("Transform\tPosition").append(LINE_SEPARATOR);
        positionStr.append("\tFrame").append(LINE_SEPARATOR);
    }

    public void stopRecording() {
        aeKeyframeStr.append(zoomStr);
        aeKeyframeStr.append(expressionStr);
        aeKeyframeStr.append(orientationStr);
        aeKeyframeStr.append(positionStr);
        aeKeyframeStr.append("End of Keyframe Data").append(LINE_SEPARATOR);
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
                .append(LINE_SEPARATOR);

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
                .append(LINE_SEPARATOR);

        // Position
        positionStr.append('\t').append(count)
                .append('\t').append(camera.position.x)
                .append('\t').append(camera.position.y)
                .append('\t').append(camera.position.z)
                .append(LINE_SEPARATOR);

        count++;
    }

}
