/**
 * Copyright (c) 2025 Yancey
 * Licensed under the MIT License
 */

package yancey.bbsaekeyframe.util;

import mchorse.bbs_mod.graphics.window.Window;
import net.minecraft.util.Util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class ClipboardUtil {

    public static void setClipboard(String text) {
        // if we use glfwSetClipboardString directly, we can't paste keyframe in AE. So we use powershell to set clipboard.
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
