package xyz.duncanruns.jingle.script.lua;

import org.apache.logging.log4j.Level;
import org.luaj.vm2.Globals;
import xyz.duncanruns.jingle.Jingle;
import xyz.duncanruns.jingle.script.ScriptFile;
import xyz.duncanruns.jingle.util.ExceptionUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public final class LuaLibraries {
    private static final List<BiFunction<ScriptFile, Globals, LuaLibrary>> LIBRARY_PROVIDERS = new ArrayList<>(Collections.singletonList(JingleLuaLibrary::new));

    private LuaLibraries() {
    }

    public static void registerLuaLibrary(BiFunction<ScriptFile, Globals, LuaLibrary> libraryProvider) {
        LIBRARY_PROVIDERS.add(libraryProvider);
    }

    static void addLibraries(ScriptFile script, Globals globals) {
        LIBRARY_PROVIDERS.forEach(provider -> globals.load(provider.apply(script, globals)));
    }

    public static void generateLuaDocs() {
        Path libsFolder = Jingle.FOLDER.resolve("scripts").resolve("libs");
        try {
            Files.createDirectories(libsFolder);
        } catch (Exception e) {
            Jingle.logError("Failed to write lua documentation for libraries:", e);
            return;
        }
        LIBRARY_PROVIDERS.forEach(f -> {
            LuaLibrary library = f.apply(null, null);
            try {
                File file = libsFolder.resolve(library.getLibraryName() + ".lua").toAbsolutePath().toFile();
                FileWriter writer = new FileWriter(file);
                library.writeLuaFile(writer);
                writer.close();
                Jingle.log(Level.INFO, "Generated " + file.getName());
            } catch (Exception e) {
                Jingle.log(Level.ERROR, "Failed to write lua documentation for library " + library.getLibraryName() + ": " + ExceptionUtil.toDetailedString(e));
            }
        });
    }
}
