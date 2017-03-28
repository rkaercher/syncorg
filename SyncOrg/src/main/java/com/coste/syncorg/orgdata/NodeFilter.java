package com.coste.syncorg.orgdata;


import java.util.HashSet;
import java.util.Set;

public class NodeFilter {

    public static final String DEFAULT ="DEFAULT";


    /* Do not reorder these and append new values only at the end as the ordinal ist used in the DB */
    public enum FilterType {
        AGENDA,
        TODO
    }

    private Long filterId;
    private Set<Long> includedNodeIds;
    private String name;
    private FilterType type;

    public Set<Long> getIncludedNodeIds() {
        return includedNodeIds;
    }

    public String getName() {
        return name;
    }

    public FilterType getType() {
        return type;
    }

    public NodeFilter(FilterType type) {
        this.type = type;
        this.filterId = null;
        this.includedNodeIds = new HashSet<>();
        this.name = DEFAULT;
    }

    public NodeFilter(Long filterId, Set<Long> includedNodeIds, String name, FilterType type) {
        this.filterId = filterId;
        this.includedNodeIds = includedNodeIds;
        this.name = name;
        this.type = type;
    }

    public Long getFilterId() {
        return filterId;
    }

    public void setIncludedNodeIds(Set<Long> includedNodeIds) {
        this.includedNodeIds = includedNodeIds;
    }
}
