/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */

package yancey.bbsaekeyframe.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.VideoRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.bbsaekeyframe.util.AEKeyframeGenerator;

import java.io.File;

@Mixin(value = VideoRecorder.class, remap = false)
public class VideoRecorderFsMixin {

    @Shadow
    private boolean recording;

    @Unique
    private final AEKeyframeGenerator aeKeyframeGenerator = new AEKeyframeGenerator();

    @Inject(method = "startRecording", at = @At("HEAD"))
    private void injectStartRecording(String movieName, File audioFile, int textureId, int width, int height, CallbackInfo ci) {
        if (movieName != null && !movieName.isEmpty()) {
            startKeyframeRecording(movieName);
        }
    }

    @Redirect(method = "startRecording", at = @At(value = "INVOKE", target = "Lmchorse/bbs_mod/utils/StringUtils;createTimestampFilename()Ljava/lang/String;"))
    private String injectGeneratedMovieName() {
        String movieName = StringUtils.createTimestampFilename();
        startKeyframeRecording(movieName);
        return movieName;
    }

    @Unique
    private void startKeyframeRecording(String movieName) {
        if (!recording && BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.startRecording(
                    BBSRendering.getVideoFolder().toPath().resolve(movieName + ".aekeyframe.txt"),
                    BBSRendering.getVideoWidth(),
                    BBSRendering.getVideoHeight(),
                    BBSRendering.getVideoFrameRate()
            );
        }
    }

    @Inject(method = "stopRecording()V", at = @At("HEAD"))
    private void injectStopRecording(CallbackInfo ci) {
        if (recording && BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.stopRecording();
        }
    }

    @Inject(method = "recordFrame()V", at = @At("HEAD"))
    private void injectRecordFrame(CallbackInfo ci) {
        if (recording && BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.recordFrame(BBSModClient.getCameraController().camera);
        }
    }
}
