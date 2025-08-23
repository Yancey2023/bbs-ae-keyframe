package yancey.bbsaekeyframe.util;

import mchorse.bbs_mod.camera.Camera;
import mchorse.bbs_mod.camera.clips.CameraClipContext;
import mchorse.bbs_mod.camera.data.Position;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.graphics.window.Window;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.l10n.keys.StringKey;
import mchorse.bbs_mod.utils.MathUtils;
import mchorse.bbs_mod.utils.clips.Clip;
import mchorse.bbs_mod.utils.clips.Clips;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class AEKeyframeUtil {

    public static final IKey FILM_COPT_AE_KEYFRAME = new StringKey("Copy AE keyframe");

    public static void copyAEKeyframe(Clips clips, Camera camera) {
        setClipboard(getAEKeyframe(clips, camera));
    }

    public static String getAEKeyframe(Clips clips, Camera camera) {
        int videoWidth = BBSRendering.getVideoWidth();
        int videoHeight = BBSRendering.getVideoHeight();
        double frameRate = BBSRendering.getVideoFrameRate();
        CameraClipContext context = new CameraClipContext();
        context.clips = clips;
        int duration = context.clips.calculateDuration();
        int keyframeSize = (int) (duration * frameRate / 20.0);
        Position position = new Position();
        StringBuilder aeKeyframeData = new StringBuilder();
        StringBuilder zoomData = new StringBuilder();
        StringBuilder orientationData = new StringBuilder();
        StringBuilder positionData = new StringBuilder();
        // header
        aeKeyframeData.append("Adobe After Effects 8.0 Keyframe Data\n");
        aeKeyframeData.append(String.format("\tUnits Per Second\t%.2f\n", frameRate));
        aeKeyframeData.append(String.format("\tSource Width\t%d\n", videoWidth));
        aeKeyframeData.append(String.format("\tSource Height\t%d\n", videoHeight));
        aeKeyframeData.append("""
                \tSource Pixel Aspect Ratio\t1
                \tComp Pixel Aspect Ratio\t1
                """);
        zoomData.append("Camera Options\tZoom\n\tFrame\n");
        orientationData.append("Transform\tOrientation\n\tFrame\n");
        positionData.append("Transform\tPosition\n\tFrame\n");
        for (int i = 0; i < keyframeSize; i++) {
            double tick0 = i / frameRate * 20;
            int tick = (int) tick0;
            float tickDelta = (float) (tick0 - tick);
            context.setup(tick, tickDelta);
            position.set(camera);
            for (Clip clip : context.clips.getClips(tick)) {
                context.apply(clip, position);
            }
            // Zoom
            zoomData.append('\t').append(i).append('\t')
                    .append((videoHeight / 2.0) / Math.tan(position.angle.fov * Math.PI / 360.0))
                    .append('\n');
            // Orientation
            Matrix4f mat = new Matrix4f()
                    .rotateX(MathUtils.PI)
                    .rotateY(MathUtils.toRad(position.angle.yaw))
                    .rotateX(MathUtils.toRad(-position.angle.pitch))
                    .rotateZ(MathUtils.toRad(position.angle.roll));
            Vector3f eulerAnglesXYZ = new Vector3f();
            mat.getEulerAnglesXYZ(eulerAnglesXYZ);
            orientationData.append('\t').append(i)
                    .append('\t').append(MathUtils.toDeg(eulerAnglesXYZ.x))
                    .append('\t').append(MathUtils.toDeg(eulerAnglesXYZ.y))
                    .append('\t').append(MathUtils.toDeg(eulerAnglesXYZ.z))
                    .append('\n');
            // Position
            positionData.append('\t').append(i)
                    .append('\t').append(position.point.x)
                    .append('\t').append(position.point.y)
                    .append('\t').append(position.point.z)
                    .append('\n');
        }
        aeKeyframeData.append(zoomData);
        aeKeyframeData.append("""
                Expression Data
                // Keep fov value when scaling composite.
                """);
        aeKeyframeData.append(String.format("thisComp.height / %d * cameraOption.zoom\n", videoHeight));
        aeKeyframeData.append("""
                End of Expression Data
                Transform\tPoint of Interest
                \tFrame
                \t\t0\t0\t0
                Expression Data
                // These tracking data only support one-node camera
                transform.position
                End of Expression Data
                """);
        aeKeyframeData.append(orientationData);
        aeKeyframeData.append(positionData);
        aeKeyframeData.append("End of Keyframe Data\n");
        return aeKeyframeData.toString();
    }


    public static void setClipboard(String text) {
        if (Util.getOperatingSystem() == Util.OperatingSystem.WINDOWS) {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_16LE);
            ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-Command", "$input | Set-Clipboard");
            try {
                Process process = builder.start();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(text);
                writer.flush();
                writer.close();
                try (OutputStream out = process.getOutputStream()) {
                    out.write(bytes);
                }
                if (process.waitFor() == 0) {
                    return;
                }
            } catch (IOException | InterruptedException ignored) {

            }
        }
        Window.setClipboard(text);
    }

}
