package dev.ultreon.quantum.crash;

import dev.ultreon.libs.commons.v0.util.StringUtils;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.util.DataSizes;
import dev.ultreon.quantum.util.Result;
import org.apache.commons.lang3.SystemProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrashLog extends CrashCategory {
    private static final String HEADER = "\n>>> FATAL ERROR: Crash Report <<<\n\n";
    private final List<CrashCategory> categories = new ArrayList<>();

    public CrashLog(String details, CrashLog report) {
        super(details);

        this.throwable = this.addCrashLog(report).throwable;
    }

    public CrashLog(String details, @Nullable CrashLog report, Throwable t) {
        this(details, t);

        if (report != null) this.addCrashLog(report);
    }

    public CrashLog(String details, Throwable t) {
        super(details, t);

        if (t instanceof ApplicationCrash) {
            ApplicationCrash crash = (ApplicationCrash) t;
            this.addCrash(crash);
        }
    }

    private CrashLog addCrashLog(CrashLog log) {
        CrashCategory cat = new CrashCategory(log.getDetails(), log.getThrowable());
        cat.entries.clear();
        cat.entries.addAll(log.entries);
        this.addCategory(cat);

        for (CrashCategory category : log.getCategories()) {
            this.addCategory(category);
        }

        return log;
    }

    private void addCrash(ApplicationCrash exception) {
        CrashLog crashLog = exception.getCrashLog();
        CrashLog crashLog1 = new CrashLog(crashLog.details, crashLog.throwable);
        if (!crashLog.categories.isEmpty()) {
            crashLog1.categories.addAll(crashLog.categories.subList(0, crashLog.categories.size() - 1));
        }
        crashLog1.entries.addAll(crashLog.entries);
        this.addCrashLog(crashLog1);
    }

    @NotNull
    public Throwable getThrowable() {
        return this.throwable;
    }

    public void addCategory(CrashCategory crashCategory) {
        this.categories.add(crashCategory);
    }

    public List<CrashCategory> getCategories() {
        return Collections.unmodifiableList(this.categories);
    }

    private CrashLog getFinalForm() {
        CrashLog crashLog = new CrashLog(this.details, this.throwable);
        crashLog.categories.addAll(this.categories);
        crashLog.entries.addAll(this.entries);

        Runtime runtime = Runtime.getRuntime();

        CrashCategory category = new CrashCategory("System Details");
        category.add("OS", SystemProperties.getOsName() + " " + SystemProperties.getOsVersion());
        category.add("Memory", DataSizes.format(runtime.totalMemory() - runtime.freeMemory()) + "/" + DataSizes.format(runtime.totalMemory()));

        crashLog.addCategory(category);
        return crashLog;
    }

    public ApplicationCrash createCrash() {
        return new ApplicationCrash(this.getFinalForm());
    }

    @Override
    public String toString() {
        String s1 = "// " + this.details + "\r\n";
        StringBuilder cs = new StringBuilder();
        StringBuilder sb = new StringBuilder();

        if (!this.entries.isEmpty()) {
            sb.append("Details:").append(System.lineSeparator());
            for (AbstractMap.SimpleEntry<String, String> entry : this.entries) {
                sb.append("  ").append(entry.getKey());
                sb.append(": ");
                sb.append(entry.getValue());
                sb.append("\r\n");
            }
        }

        for (CrashCategory category : this.categories) {
            cs.append(System.lineSeparator()).append(category);
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.throwable.printStackTrace(pw);

        return HEADER + "\r\n" + s1 + "\r\n" + sw + cs + "\r\n" + sb;
    }

    public static String getFileName() {
        return CrashLog.getFileNameWithoutExt() + ".txt";
    }

    @NotNull
    public static String getFileNameWithoutExt() {
        return "crash-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM.dd.yyyy-HH.mm.ss"));
    }

    public Result<@Nullable Void> defaultSave() {
        File file = new File("game-crashes");
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException e) {
                return Result.failure(e);
            }
        }

        this.writeToFile(new File(file, CrashLog.getFileName()));
        return Result.ok(null);
    }

    public Result<@Nullable Void> writeToFile(File file) {
        try (FileOutputStream stream = new FileOutputStream(file)) {
            this.writeToStream(stream);
        } catch (IOException e) {
            return Result.failure(e);
        }

        return Result.ok(null);
    }

    public void writeToStream(OutputStream stream) throws IOException {
        stream.write(this.toString().getBytes());
        stream.flush();
    }

    public void writeToLog() {
        StringUtils.splitIntoLines(this.toString()).forEach(CommonConstants.LOGGER::error);
    }
}
