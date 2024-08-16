package dev.ultreon.quantum.util;

import com.mojang.serialization.Codec;
import dev.ultreon.libs.commons.v0.exceptions.SyntaxException;
import dev.ultreon.libs.commons.v0.tuple.Pair;
import dev.ultreon.quantum.CommonConstants;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * An identifier for an object in the game.
 * The identifier consists of a domain and a path.
 * This class is immutable and thread-safe.
 * <p>
 * Parsing / formatting example:
 * <pre>
 *  Identifier id = Identifier.parse("quantum:crate");
 *  Identifier id = Identifier.tryParse("quantum:crate");
 *  Identifier id = new Identifier("quantum", "crate");
 *  String name = id.toString();
 * </pre>
 *
 * @author <a href="https://github.com/XyperCode">XyperCode</a>
 * @since 0.1.0
 */
public final class NamespaceID {
    public static final Codec<NamespaceID> CODEC = Codec.STRING.xmap(NamespaceID::parse, NamespaceID::toString);
    private final @NotNull String domain;
    private final @NotNull String path;

    public NamespaceID(@NotNull String domain, @NotNull String path) {
        testDomain(domain);
        testPath(path);

        this.domain = domain;
        this.path = path;
    }

    public NamespaceID(@NotNull String namespace) {
        String[] split = namespace.split(":", 2);
        if (split.length == 2) {
            this.domain = testDomain(split[0]);
            this.path = testPath(split[1]);
        } else {
            this.domain = CommonConstants.NAMESPACE;
            this.path = testPath(namespace);
        }
    }

    /**
     * Parses the given name into an Identifier object.
     *
     * @param name the name to be parsed
     * @return the Identifier object
     */
    @NotNull
    @Contract("_ -> new")
    public static NamespaceID parse(@NotNull String name) {
        return new NamespaceID(name);
    }

    /**
     * Tries to parse the given name into an Identifier.
     *
     * @param name The name to parse. Can be null.
     * @return The parsed Identifier if successful, null otherwise.
     */
    @Nullable
    @Contract("null -> null")
    public static NamespaceID tryParse(@Nullable String name) {
        // Return null if the name is null
        if (name == null) return null;

        try {
            // Try to create a new Identifier with the given name
            return new NamespaceID(name);
        } catch (Exception e) {
            // Return null if an exception occurs during parsing
            return null;
        }
    }

    /**
     * Validates the given domain string against a specific pattern.
     *
     * @param domain The domain string to be validated
     * @return The validated domain string
     * @throws SyntaxException if the domain string is invalid
     */
    @Contract("_ -> param1")
    public static @NotNull String testDomain(@NotNull String domain) {
        // Checks if the domain matches the specified pattern
        if (!Pattern.matches("([a-z\\d_]+)([.\\-][a-z\\-\\d_]+){0,16}", domain)) {
            throw new SyntaxException("Domain is invalid: " + domain);
        }
        return domain;
    }

    /**
     * Validates and returns the input path.
     *
     * @param path The path to be validated
     * @return The validated path
     * @throws SyntaxException If the path is invalid
     */
    @Contract("_ -> param1")
    public static @NotNull String testPath(@NotNull String path) {
        // Validate the path against a specific pattern
        if (!Pattern.matches("([a-z_.\\d]+)(/[a-z_.\\d]+){0,16}", path)) {
            throw new SyntaxException("Path is invalid: " + path);
        }

        return path;
    }

    /**
     * This method checks if two Identifiers are equal.
     *
     * @param o The object to compare with this Identifier.
     * @return True if the objects are equal, false otherwise.
     */
    @Override
    @Contract(value = "null -> false", pure = true)
    public boolean equals(Object o) {
        // Check if the objects are the same instance
        if (this == o) {
            return true;
        }

        // Check if the objects are null or not of the same class
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        // Cast the object to "NamespaceID" type
        NamespaceID that = (NamespaceID) o;

        // Check if the domains and paths of the Identifiers are equal
        return this.domain.equals(that.domain) && this.path.equals(that.path);
    }

    /**
     * This method calculates the hash code for the object.
     * It combines the hash codes of the domain and path fields.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {
        // Use Objects.hash() method to calculate the hash code
        return Objects.hash(this.domain, this.path);
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string in the format "domain:path"
     */
    @NotNull
    @Override
    @Contract(pure = true)
    public String toString() {
        return this.domain + ":" + this.path;
    }

    /**
     * @return object location (the mod id / domain).
     */
    @NotNull
    @Contract(pure = true)
    public String getDomain() {
        return this.domain;
    }

    /**
     * @return object path.
     */
    @NotNull
    @Contract(pure = true)
    public String getPath() {
        return this.path;
    }

    /**
     * Returns a new Identifier with the provided domain.
     *
     * @param domain the new domain to use
     * @return a new Identifier with the updated domain
     */
    @Contract("_ -> new")
    public NamespaceID withDomain(@NotNull String domain) {
        return new NamespaceID(domain, this.path);
    }

    /**
     * Returns a new Identifier with the provided path.
     *
     * @param path the new path to use
     * @return a new Identifier with the updated path
     */
    @Contract("_ -> new")
    public NamespaceID withPath(@NotNull String path) {
        return new NamespaceID(this.domain, path);
    }

    /**
     * Maps the domain using the provided UnaryOperator.
     *
     * @param domain the UnaryOperator to map the domain
     * @return a new Identifier with the mapped domain
     */
    @Contract("_ -> new")
    public NamespaceID mapDomain(@NotNull UnaryOperator<@NotNull String> domain) {
        return new NamespaceID(domain.apply(this.domain), this.path);
    }

    /**
     * Maps the path using the provided UnaryOperator.
     *
     * @param path the UnaryOperator to map the path
     * @return a new Identifier with the mapped path
     */
    @Contract("_ -> new")
    public NamespaceID mapPath(@NotNull UnaryOperator<@NotNull String> path) {
        return new NamespaceID(this.domain, path.apply(this.path));
    }

    /**
     * Maps both the path and domain using the provided UnaryOperators.
     *
     * @param domain the UnaryOperator to map the domain
     * @param path   the UnaryOperator to map the path
     * @return a new Identifier with the mapped path and domain
     */
    @Contract("_, _ -> new")
    public NamespaceID map(@NotNull UnaryOperator<@NotNull String> domain, @NotNull UnaryOperator<@NotNull String> path) {
        return new NamespaceID(domain.apply(this.domain), path.apply(this.path));
    }

    /**
     * Reduce the domain and path using the provided function.
     *
     * @param reducer the function to apply to the domain and path
     * @param <T>     the type of the result
     * @return the result of applying the function to the domain and path
     */
    public <T> T reduce(BiFunction<String, String, T> reducer) {
        return reducer.apply(this.domain, this.path);
    }

    /**
     * Returns the list representation of the domain and path.
     *
     * @return A list containing the domain and path.
     */
    @NotNull
    @Unmodifiable
    @Contract(value = "-> new", pure = true)
    public List<String> toList() {
        return Arrays.asList(this.domain, this.path);
    }

    /**
     * Converts the domain and path to an ArrayList of strings.
     *
     * @return ArrayList of strings containing the domain and path.
     */
    @NotNull
    @Contract(" -> new")
    public ArrayList<String> toArrayList() {
        // Create a new ArrayList to store the domain and path
        ArrayList<String> list = new ArrayList<>();

        // Add the domain and path to the list
        list.add(this.domain);
        list.add(this.path);

        // Return the list containing the domain and path
        return list;
    }

    /**
     * Returns an unmodifiable view of the collection as a list of strings.
     *
     * @return unmodifiable view of the collection as a list of strings
     */
    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Collection<String> toCollection() {
        return this.toList();
    }

    /**
     * Converts the domain and path to a Pair of strings.
     *
     * @return a Pair of strings representing the domain and path
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public Pair<String, String> toPair() {
        return new Pair<>(this.domain, this.path);
    }

    /**
     * Converts the domain and path to an array of strings.
     *
     * @return an array of strings representing the domain and path
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public String[] toArray() {
        return new String[]{this.domain, this.path};
    }
}
