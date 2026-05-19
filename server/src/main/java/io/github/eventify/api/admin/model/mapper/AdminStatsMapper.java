package io.github.eventify.api.admin.model.mapper;

import io.github.eventify.api.admin.model.EventStats;
import io.github.eventify.api.admin.model.StorageStats;
import io.github.eventify.api.admin.model.projection.StorageSizeProjection;
import io.github.eventify.api.admin.model.response.AdminEventStatsResponse;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;

/** MapStruct mapper for admin stats domain objects to response DTOs. */
@Mapper(config = SharedMapperConfig.class)
public abstract class AdminStatsMapper {

    public abstract AdminEventStatsResponse toEventStatsResponse(EventStats data);

    public abstract TableSizeEntry toTableSizeEntry(StorageStats data);

    public abstract List<TableSizeEntry> toTableSizeEntryList(List<StorageStats> data);

    public abstract StorageStats toStorageStats(StorageSizeProjection projection);

    public abstract List<StorageStats> toStorageStatsList(List<StorageSizeProjection> projections);
}
