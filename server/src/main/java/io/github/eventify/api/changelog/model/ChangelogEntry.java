package io.github.eventify.api.changelog.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Represents a single changelog entry with version, date, and categorized changes. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class ChangelogEntry {

    @JsonProperty("version")
    private String version;

    @JsonProperty("date")
    private String date;

    @JsonProperty("features")
    private List<String> features;

    @JsonProperty("improvements")
    private List<String> improvements;

    @JsonProperty("fixes")
    private List<String> fixes;
}
