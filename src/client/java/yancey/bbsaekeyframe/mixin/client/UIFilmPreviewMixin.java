/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */

package yancey.bbsaekeyframe.mixin.client;

import mchorse.bbs_mod.graphics.window.Window;
import mchorse.bbs_mod.l10n.keys.IKey;
import mchorse.bbs_mod.l10n.keys.StringKey;
import mchorse.bbs_mod.ui.film.UIFilmPreview;
import mchorse.bbs_mod.ui.framework.elements.IUIElement;
import mchorse.bbs_mod.ui.framework.elements.UIElement;
import mchorse.bbs_mod.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs_mod.ui.utils.icons.Icons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yancey.bbsaekeyframe.util.AEKeyframeGenerator;

@Mixin(value = UIFilmPreview.class, remap = false)
public class UIFilmPreviewMixin {

    @Unique
    private static final IKey FILM_COPY_AE_KEYFRAME = new StringKey("Copy AE keyframe");

    @Shadow
    public UIIcon recordVideo;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lmchorse/bbs_mod/ui/framework/elements/UIElement;add([Lmchorse/bbs_mod/ui/framework/elements/IUIElement;)V"))
    private void addCopyAEKeyframeMenu(UIElement instance, IUIElement[] iuiElements) {
        recordVideo.context(menu -> menu.action(Icons.COPY, FILM_COPY_AE_KEYFRAME, () -> {
            if (AEKeyframeGenerator.lastKeyframeStr != null) {
                Window.setClipboard(AEKeyframeGenerator.lastKeyframeStr);
            }
        }));
        instance.add(iuiElements);
    }

}
