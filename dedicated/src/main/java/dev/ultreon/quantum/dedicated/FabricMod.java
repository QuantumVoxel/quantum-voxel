package dev.ultreon.quantum.dedicated;

import com.badlogic.gdx.files.FileHandle;
import dev.ultreon.quantum.Mod;
import dev.ultreon.quantum.ModOrigin;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implements {@link Mod} for {@link ModContainer}
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
public class FabricMod implements Mod {
    private final String id;
    private final String name;
    private final String version;
    private final String description;
    private final Collection<String> authors;
    private final ModMetadata metadata;
    private final ModContainer container;

    public FabricMod(ModContainer container) {
        metadata = container.getMetadata();
        this.container = container;
        this.id = metadata.getId();
        this.name = metadata.getName();
        this.version = metadata.getVersion().getFriendlyString();
        this.description = metadata.getDescription();
        this.authors = metadata.getAuthors().stream().map(Person::getName).collect(Collectors.toList());
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getName() {
        if (id.equals("java")) {
            return System.getProperty("java.vm.name") + " " + System.getProperty("java.version");
        }
        return name;
    }

    @Override
    public @NotNull String getVersion() {
        if (id.equals("java")) {
            return System.getProperty("java.version");
        }
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public @NotNull Optional<String> getIconPath(int size) {
        return this.metadata.getIconPath(size);
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        if (id.equals("java")) {
            return List.of(System.getProperty("java.vendor"));
        }
        return authors;
    }

    @Override
    public @NotNull ModOrigin getOrigin() {
        switch (container.getOrigin().getKind()) {
            case PATH:
                return ModOrigin.ACTUAL_PATH;
            case NESTED:
                return ModOrigin.BUNDLED;
            case UNKNOWN:
                return ModOrigin.OTHER;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public @Nullable Iterable<FileHandle> getRootPaths() {
        return null;
    }

    @Override
    public String getSources() {
        return metadata.getContact().get("sources").orElse(null);
    }

    @Override
    public @Nullable String getHomepage() {
        return metadata.getContact().get("homepage").orElse(null);
    }

    @Override
    public @Nullable String getIssues() {
        return metadata.getContact().get("issues").orElse(null);
    }

    @Override
    public @Nullable String getDiscord() {
        return metadata.getContact().get("discord").orElse(null);
    }

    @Override
    public String getLicense() {
        String join = String.join(", ", metadata.getLicense());
        if (join.isEmpty()) {
            join = "None";
        }
        return join;
    }
}
