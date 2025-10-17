package com.techcorp.model;

import java.util.List;

public class ImportSummary {
    private int importedCount;
    private List<String> errors;
    
    public ImportSummary(int importedCount, List<String> errors) {
        this.importedCount = importedCount;
        this.errors = errors;
    }
    
    public int getImportedCount() {
        return importedCount;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    @Override
    public String toString() {
        return "ImportSummary{" +
                "importedCount=" + importedCount +
                ", errors=" + errors +
                '}';
    }
}
