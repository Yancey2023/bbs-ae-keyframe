/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */package yancey.bbsaekeyframe.mixin.client;

import mchorse.bbs_mod.BBSModClient;
import mchorse.bbs_mod.client.BBSRendering;
import mchorse.bbs_mod.utils.StringUtils;
import mchorse.bbs_mod.utils.VideoRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.bbsaekeyframe.util.AEKeyframeGenerator;

@Mixin(value = VideoRecorder.class, remap = false)
public class VideoRecorderMixin {

    @Unique
    AEKeyframeGenerator aeKeyframeGenerator = new AEKeyframeGenerator();

    @Redirect(method = "startRecording", at = @At(value = "INVOKE", target = "Lmchorse/bbs_mod/utils/StringUtils;createTimestampFilename()Ljava/lang/String;"))
    public String injectStartRecording() {
        String movieName = StringUtils.createTimestampFilename();
        aeKeyframeGenerator.startRecording(
                BBSRendering.getVideoFolder().toPath().resolve(movieName + ".aekeyframe.txt"),
                BBSRendering.getVideoWidth(),
                BBSRendering.getVideoHeight(),
                BBSRendering.getVideoFrameRate()
        );
        return movieName;
    }

    @Inject(method = "stopRecording", at = @At(value = "TAIL"))
    public void injectStopRecording(CallbackInfo ci) {
        aeKeyframeGenerator.stopRecording();
    }

    @Inject(method = "recordFrame", at = @At(value = "TAIL"))
    public void injectRecordFrame(CallbackInfo ci) {
        aeKeyframeGenerator.recordFrame(BBSModClient.getCameraController().camera);
    }

}
