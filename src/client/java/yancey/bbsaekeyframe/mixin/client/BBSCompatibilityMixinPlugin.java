/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */

package yancey.bbsaekeyframe.mixin.client;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BBSCompatibilityMixinPlugin implements IMixinConfigPlugin {

    private boolean isBbsFs() {
        return FabricLoader.getInstance()
                .getModContainer("bbs")
                .map(container -> container.getMetadata().getName().equals("BBS FS mod"))
                .orElse(false);
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("VideoRecorderMixin")) {
            return !isBbsFs();
        }
        if (mixinClassName.endsWith("VideoRecorderFsMixin")) {
            return isBbsFs();
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
        IMixinConfigPlugin.super.onLoad(mixinPackage);
    }

    @Override
    public String getRefMapperConfig() {
        return IMixinConfigPlugin.super.getRefMapperConfig();
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        IMixinConfigPlugin.super.acceptTargets(myTargets, otherTargets);
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        IMixinConfigPlugin.super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        IMixinConfigPlugin.super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
