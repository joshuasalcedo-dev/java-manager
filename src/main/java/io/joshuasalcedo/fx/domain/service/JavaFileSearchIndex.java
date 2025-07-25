package io.joshuasalcedo.fx.domain.service;

import io.joshuasalcedo.fx.domain.model.java.JavaFile;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * In-memory search index for fast Java file content searching.
 * Uses inverted index for efficient full-text search.
 *
 * @author JoshuaSalcedo
 * @since ${PROJECT.version}
 */
public class JavaFileSearchIndex {
    
    // Inverted index: token -> set of file paths containing that token
    private final Map<String, Set<String>> tokenIndex = new ConcurrentHashMap<>();
    
    // Type name index: type name -> file paths
    private final Map<String, Set<String>> typeIndex = new ConcurrentHashMap<>();
    
    // Package index: package name -> file paths
    private final Map<String, Set<String>> packageIndex = new ConcurrentHashMap<>();
    
    // Method index: method name -> file paths
    private final Map<String, Set<String>> methodIndex = new ConcurrentHashMap<>();
    
    // Import index: import -> file paths
    private final Map<String, Set<String>> importIndex = new ConcurrentHashMap<>();
    
    // File content cache for faster repeated searches
    private final Map<String, JavaFile> fileCache = new ConcurrentHashMap<>();
    
    // Token delimiter pattern
    private static final Pattern TOKEN_DELIMITER = Pattern.compile("[^a-zA-Z0-9_$]+");
    
    // Minimum token length to index
    private static final int MIN_TOKEN_LENGTH = 3;
    
    /**
     * Indexes a Java file for searching
     */
    public void indexFile(JavaFile file) {
        if (file == null || file.getFilePath() == null) return;
        
        String filePath = file.getFilePath();
        
        // Cache the file
        fileCache.put(filePath, file);
        
        // Index content tokens
        if (file.getFileContent() != null) {
            indexContent(filePath, file.getFileContent());
        }
        
        // Index package
        if (file.getPackageName() != null) {
            packageIndex.computeIfAbsent(file.getPackageName(), k -> ConcurrentHashMap.newKeySet())
                       .add(filePath);
        }
        
        // Index types
        if (file.getTypeDeclarations() != null) {
            file.getTypeDeclarations().forEach(type -> {
                typeIndex.computeIfAbsent(type.name(), k -> ConcurrentHashMap.newKeySet())
                        .add(filePath);
                
                // Index methods
                if (type.methods() != null) {
                    type.methods().forEach(method -> 
                        methodIndex.computeIfAbsent(method.name(), k -> ConcurrentHashMap.newKeySet())
                                  .add(filePath));
                }
            });
        }
        
        // Index imports
        if (file.getImports() != null) {
            file.getImports().forEach(imp -> 
                importIndex.computeIfAbsent(imp, k -> ConcurrentHashMap.newKeySet())
                          .add(filePath));
        }
    }
    
    /**
     * Removes a file from the index
     */
    public void removeFile(String filePath) {
        // Remove from all indices
        tokenIndex.values().forEach(set -> set.remove(filePath));
        typeIndex.values().forEach(set -> set.remove(filePath));
        packageIndex.values().forEach(set -> set.remove(filePath));
        methodIndex.values().forEach(set -> set.remove(filePath));
        importIndex.values().forEach(set -> set.remove(filePath));
        
        // Remove from cache
        fileCache.remove(filePath);
        
        // Clean up empty entries
        cleanupIndices();
    }
    
    /**
     * Searches for files containing the given text
     */
    public Set<JavaFile> searchContent(String searchText, boolean caseSensitive, boolean wholeWord) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return Set.of();
        }
        
        Set<String> tokens = tokenize(searchText, caseSensitive);
        
        if (tokens.isEmpty()) {
            return Set.of();
        }
        
        // Find files containing all tokens (AND search)
        Set<String> matchingFiles = null;
        
        for (String token : tokens) {
            Set<String> filesWithToken = tokenIndex.getOrDefault(token, Set.of());
            
            if (matchingFiles == null) {
                matchingFiles = new HashSet<>(filesWithToken);
            } else {
                matchingFiles.retainAll(filesWithToken);
            }
            
            if (matchingFiles.isEmpty()) {
                break;
            }
        }
        
        if (matchingFiles == null || matchingFiles.isEmpty()) {
            return Set.of();
        }
        
        // If whole word search, verify matches
        if (wholeWord) {
            matchingFiles = verifyWholeWordMatches(matchingFiles, searchText, caseSensitive);
        }
        
        // Return cached JavaFile objects
        return matchingFiles.stream()
                .map(fileCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Searches by type name
     */
    public Set<JavaFile> searchByType(String typeName) {
        Set<String> files = typeIndex.getOrDefault(typeName, Set.of());
        return files.stream()
                .map(fileCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Searches by package name
     */
    public Set<JavaFile> searchByPackage(String packageName) {
        Set<String> files = packageIndex.getOrDefault(packageName, Set.of());
        return files.stream()
                .map(fileCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Searches by method name
     */
    public Set<JavaFile> searchByMethod(String methodName) {
        Set<String> files = methodIndex.getOrDefault(methodName, Set.of());
        return files.stream()
                .map(fileCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Searches by import
     */
    public Set<JavaFile> searchByImport(String importPattern) {
        return importIndex.entrySet().stream()
                .filter(entry -> entry.getKey().contains(importPattern))
                .flatMap(entry -> entry.getValue().stream())
                .distinct()
                .map(fileCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    /**
     * Rebuilds the entire index
     */
    public void rebuild(Collection<JavaFile> files) {
        clear();
        files.forEach(this::indexFile);
    }
    
    /**
     * Clears the index
     */
    public void clear() {
        tokenIndex.clear();
        typeIndex.clear();
        packageIndex.clear();
        methodIndex.clear();
        importIndex.clear();
        fileCache.clear();
    }
    
    /**
     * Gets index statistics
     */
    public IndexStatistics getStatistics() {
        return new IndexStatistics(
                fileCache.size(),
                tokenIndex.size(),
                typeIndex.size(),
                packageIndex.size(),
                methodIndex.size(),
                importIndex.size()
        );
    }
    
    // Private helper methods
    
    private void indexContent(String filePath, String content) {
        Set<String> tokens = tokenize(content, false);
        
        for (String token : tokens) {
            if (token.length() >= MIN_TOKEN_LENGTH) {
                tokenIndex.computeIfAbsent(token, k -> ConcurrentHashMap.newKeySet())
                         .add(filePath);
            }
        }
    }
    
    private Set<String> tokenize(String text, boolean caseSensitive) {
        if (!caseSensitive) {
            text = text.toLowerCase();
        }
        
        return Arrays.stream(TOKEN_DELIMITER.split(text))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }
    
    private Set<String> verifyWholeWordMatches(Set<String> files, String searchText, boolean caseSensitive) {
        String pattern = "\\b" + Pattern.quote(searchText) + "\\b";
        Pattern wordPattern = caseSensitive ? 
                Pattern.compile(pattern) : 
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        
        return files.stream()
                .filter(filePath -> {
                    JavaFile file = fileCache.get(filePath);
                    return file != null && 
                           file.getFileContent() != null && 
                           wordPattern.matcher(file.getFileContent()).find();
                })
                .collect(Collectors.toSet());
    }
    
    private void cleanupIndices() {
        // Remove empty entries from indices
        tokenIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        typeIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        packageIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        methodIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        importIndex.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Statistics about the search index
     */
    public record IndexStatistics(
            int totalFiles,
            int uniqueTokens,
            int uniqueTypes,
            int uniquePackages,
            int uniqueMethods,
            int uniqueImports
    ) {
        public String toSummary() {
            return String.format(
                "Index Statistics: %d files, %d tokens, %d types, %d packages, %d methods, %d imports",
                totalFiles, uniqueTokens, uniqueTypes, uniquePackages, uniqueMethods, uniqueImports
            );
        }
    }
}