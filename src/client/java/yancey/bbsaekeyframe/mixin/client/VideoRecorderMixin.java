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

@Mixin(value = VideoRecorder.class, remap = false)
public abstract class VideoRecorderMixin {

    @Shadow
    private boolean recording;

    @Unique
    AEKeyframeGenerator aeKeyframeGenerator = new AEKeyframeGenerator();

    @Redirect(method = "startRecording", at = @At(value = "INVOKE", target = "Lmchorse/bbs_mod/utils/StringUtils;createTimestampFilename()Ljava/lang/String;"))
    public String injectStartRecording() {
        String movieName = StringUtils.createTimestampFilename();
        if (BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.startRecording(
                    BBSRendering.getVideoFolder().toPath().resolve(movieName + ".aekeyframe.txt"),
                    BBSRendering.getVideoWidth(),
                    BBSRendering.getVideoHeight(),
                    BBSRendering.getVideoFrameRate()
            );
        }
        return movieName;
    }

    @Inject(method = "stopRecording", at = @At(value = "HEAD"))
    public void injectStopRecording(CallbackInfo ci) {
        if (recording && BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.stopRecording();
        }
    }

    @Inject(method = "recordFrame", at = @At(value = "HEAD"))
    public void injectRecordFrame(CallbackInfo ci) {
        if (recording && BBSModClient.getCameraController().getCurrent() != null) {
            aeKeyframeGenerator.recordFrame(BBSModClient.getCameraController().camera);
        }
    }

}
