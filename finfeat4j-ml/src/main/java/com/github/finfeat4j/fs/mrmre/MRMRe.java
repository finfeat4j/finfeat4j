package com.github.finfeat4j.fs.mrmre;

import com.github.rcaller.scriptengine.RCallerScriptEngine;
import com.github.rcaller.util.Globals;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class MRMRe {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(MRMRe.class);
    private static final File scriptFile;

    static {
        String rExecutable = "/usr/bin/R";
        try {
            // get R path using which command
            var pb = new ProcessBuilder("which", "R");
            var process = pb.start();
            rExecutable = new String(process.getInputStream().readAllBytes()).trim();
        } catch (Exception e) {
            log.error("Error loading RCaller", e);
        }
        try {
            scriptFile = File.createTempFile("mRMRe", ".R");
            scriptFile.deleteOnExit();
            writeScript(scriptFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Globals.R_current = rExecutable;
    }

    public static String[] mRMRe(String filePath, int chunkSize, int per_chunk_size,
                                 String miThresholdChunk, String miThresholdFinal,
                                 String causalityThresholdChunk, String causalityThresholdFinal) {
        try {
            var engine = new RCallerScriptEngine();
            var scriptPath = scriptFile.getAbsolutePath();
            engine.eval("source(\"" + scriptPath +"\")");
            StringBuilder thresholds = new StringBuilder();
            String[] thrsParams = {miThresholdChunk, causalityThresholdChunk, miThresholdFinal, causalityThresholdFinal};
            String[] paramNames = {"mi_threshold_chunk", "causality_threshold_chunk", "mi_threshold_final", "causality_threshold_final"};
            for (int i = 0; i < thrsParams.length; i++) {
                if (thrsParams[i] != null) {
                    thresholds.append(", ").append(paramNames[i]).append(" = ").append(thrsParams[i]);
                }
            }
            engine.eval("a <- feature_selection('" + filePath + "', " + chunkSize + ", " + per_chunk_size + thresholds + ")");
            String[] features = (String[]) engine.get("a$features");
            // double[] scores = (double[]) engine.get("a$scores");
            System.out.println("Selected Features: " + features.length);
            engine.close();
            return features;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeScript(File scriptFile) {
        try {
            var scriptStream = Objects.requireNonNull(MRMRe.class.getResource("mRMRe.R"))
                    .openConnection()
                    .getInputStream();
            java.nio.file.Files.writeString(scriptFile.toPath(), new String(scriptStream.readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] reverse(String[] arr) {
        String[] reversed = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            reversed[i] = arr[arr.length - i - 1];
        }
        return reversed;
    }
}
