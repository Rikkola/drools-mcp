package org.drools.agentic.example.registry;

import java.util.Optional;
import java.util.Set;
import java.util.List;

/**
 * Registry for managing DRL fact type declarations across authoring sessions.
 * Provides storage and retrieval of fact type definitions for reuse and modification.
 */
public interface FactTypeRegistry {

    /**
     * Registers a new fact type definition.
     * If a fact type with the same name already exists, it will be replaced.
     *
     * @param typeName the name of the fact type
     * @param definition the fact type definition
     */
    void registerFactType(String typeName, FactTypeDefinition definition);

    /**
     * Retrieves a fact type definition by name.
     *
     * @param typeName the name of the fact type
     * @return the fact type definition, or empty if not found
     */
    Optional<FactTypeDefinition> getFactType(String typeName);

    /**
     * Gets all registered fact type names.
     *
     * @return set of all fact type names
     */
    Set<String> getAllFactTypeNames();

    /**
     * Gets all registered fact type definitions.
     *
     * @return list of all fact type definitions
     */
    List<FactTypeDefinition> getAllFactTypes();

    /**
     * Updates an existing fact type definition or creates a new one.
     *
     * @param typeName the name of the fact type
     * @param definition the updated fact type definition
     */
    void updateFactType(String typeName, FactTypeDefinition definition);

    /**
     * Removes a fact type from the registry.
     *
     * @param typeName the name of the fact type to remove
     * @return true if the fact type was removed, false if it didn't exist
     */
    boolean removeFactType(String typeName);

    /**
     * Generates DRL declare blocks for all registered fact types.
     *
     * @return complete DRL declarations as a string
     */
    String generateDRLDeclarations();

    /**
     * Generates DRL declare blocks for specified fact types.
     *
     * @param typeNames the names of fact types to include
     * @return DRL declarations as a string
     */
    String generateDRLDeclarations(Set<String> typeNames);

    /**
     * Checks if a fact type is registered.
     *
     * @param typeName the name of the fact type
     * @return true if the fact type exists
     */
    boolean hasFactType(String typeName);

    /**
     * Clears all registered fact types.
     */
    void clear();

    /**
     * Returns the number of registered fact types.
     *
     * @return count of registered fact types
     */
    int size();

    /**
     * Loads fact type definitions from existing DRL content.
     * This method parses DRL declare blocks and registers the found fact types.
     *
     * @param drlContent the DRL content to parse
     * @param packageName the package name to associate with parsed fact types
     * @return number of fact types loaded
     */
    int loadFromDRL(String drlContent, String packageName);

    /**
     * Merges another registry into this one.
     * Existing fact types with the same name will be updated with merged fields.
     *
     * @param other the registry to merge from
     */
    void merge(FactTypeRegistry other);

    /**
     * Creates a copy of this registry.
     *
     * @return a new registry with copied fact type definitions
     */
    FactTypeRegistry copy();
}