package yancey.bbsaekeyframe.mixin.client;

import mchorse.bbs_mod.ui.film.UIFilmPanel;
import mchorse.bbs_mod.ui.film.UIFilmPreview;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.bbsaekeyframe.util.AEKeyframeUtil;

@Mixin(UIFilmPreview.class)
public class UIFilmPreviewMixin {

    @Shadow
    public UIIcon recordVideo;

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lmchorse/bbs_mod/ui/framework/elements/buttons/UIIcon;context(Ljava/util/function/Consumer;)Lmchorse/bbs_mod/ui/framework/elements/UIElement;",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            ))
    private void addCopyAEKeyframeMenu(UIFilmPanel filmPanel, CallbackInfo ci) {
        recordVideo.context(menu -> menu.action(Icons.COPY, AEKeyframeUtil.FILM_COPT_AE_KEYFRAME,
                () -> AEKeyframeUtil.copyAEKeyframe(filmPanel.getData().camera, filmPanel.getCamera())));
    }

}
