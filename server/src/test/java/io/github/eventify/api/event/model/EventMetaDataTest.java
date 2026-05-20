package io.github.eventify.api.event.model;

import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.SearchType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Unit Test - EventMetaData")
public class EventMetaDataTest extends UnitTest {

    private EventMetaData metaData;

    @BeforeEach
    public void setUp() {
        metaData = new EventMetaData();
    }

    @Test
    @DisplayName("Should register channelIds field with MULTI_NUMERIC type mapping to channel.id")
    public void shouldRegisterChannelIdsFieldWithMultiNumericType() {
        // Given: EventMetaData is constructed

        // When: Inspecting registered search types
        final SearchType channelIdsType = metaData.getSearchTypes().get(EventMetaData.CHANNEL_IDS_TERM);

        // Then: channelIds is registered as MULTI_NUMERIC
        assertThat(channelIdsType, is(notNullValue()));
        assertThat(channelIdsType, is(equalTo(SearchType.MULTI_NUMERIC)));
    }

    @Test
    @DisplayName("Should map channelIds field to channel.id column")
    public void shouldMapChannelIdsFieldToChannelIdColumn() {
        // Given: EventMetaData is constructed

        // When: Inspecting column name mapping for channelIds
        final java.util.List<String> columns = metaData.getColumnNames().get(EventMetaData.CHANNEL_IDS_TERM);

        // Then: Maps to channel.id
        assertThat(columns, is(notNullValue()));
        assertThat(columns, hasItem(EventMetaData.CHANNEL_ID_FIELD));
    }

    @Test
    @DisplayName("Should still register channelId field with NUMERIC type")
    public void shouldStillRegisterChannelIdFieldWithNumericType() {
        // Given: EventMetaData is constructed

        // When: Inspecting channelId search type
        final SearchType channelIdType = metaData.getSearchTypes().get(EventMetaData.CHANNEL_ID_TERM);

        // Then: channelId remains NUMERIC
        assertThat(channelIdType, is(equalTo(SearchType.NUMERIC)));
    }

    @Test
    @DisplayName("Should still register timestamp field with DATE type")
    public void shouldStillRegisterTimestampFieldWithDateType() {
        // Given: EventMetaData is constructed

        // When: Inspecting timestamp search type
        final SearchType timestampType = metaData.getSearchTypes().get(EventMetaData.TIMESTAMP);

        // Then: timestamp remains DATE
        assertThat(timestampType, is(equalTo(SearchType.DATE)));
    }

    @Test
    @DisplayName("Should still register severity field with ENUM type")
    public void shouldStillRegisterSeverityFieldWithEnumType() {
        // Given: EventMetaData is constructed

        // When: Inspecting severity search type
        final SearchType severityType = metaData.getSearchTypes().get(EventMetaData.SEVERITY);

        // Then: severity remains ENUM
        assertThat(severityType, is(equalTo(SearchType.ENUM)));
    }
}
