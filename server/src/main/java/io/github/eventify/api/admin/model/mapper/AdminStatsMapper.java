package io.github.eventify.api.admin.model.mapper;

import io.github.eventify.api.admin.model.AdminCounts;
import io.github.eventify.api.admin.model.AdminEventVolume;
import io.github.eventify.api.admin.model.AdminGrowth;
import io.github.eventify.api.admin.model.DailyVolumeData;
import io.github.eventify.api.admin.model.EventStats;
import io.github.eventify.api.admin.model.StorageStats;
import io.github.eventify.api.admin.model.projection.StorageSizeProjection;
import io.github.eventify.api.admin.model.response.AdminCountsResponse;
import io.github.eventify.api.admin.model.response.AdminEventStatsResponse;
import io.github.eventify.api.admin.model.response.AdminEventVolumeResponse;
import io.github.eventify.api.admin.model.response.AdminGrowthResponse;
import io.github.eventify.api.admin.model.response.DailyVolumePoint;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;

/** MapStruct mapper for admin stats domain objects to response DTOs. */
@Mapper(config = SharedMapperConfig.class)
public abstract class AdminStatsMapper {

    /** Maps AdminCounts domain object to AdminCountsResponse DTO. */
    public abstract AdminCountsResponse toCountsResponse(AdminCounts counts);

    /** Maps AdminGrowth domain object to AdminGrowthResponse DTO. */
    public abstract AdminGrowthResponse toGrowthResponse(AdminGrowth growth);

    /** Maps AdminEventVolume domain object to AdminEventVolumeResponse DTO. */
    public abstract AdminEventVolumeResponse toEventVolumeResponse(AdminEventVolume volume);

    /** Maps DailyVolumeData domain object to DailyVolumePoint DTO. */
    public abstract DailyVolumePoint toDailyVolumePoint(DailyVolumeData data);

    /** Maps list of DailyVolumeData to list of DailyVolumePoint DTOs. */
    public abstract List<DailyVolumePoint> toDailyVolumePoints(List<DailyVolumeData> data);

    /** Maps EventStats domain object to AdminEventStatsResponse DTO. */
    public abstract AdminEventStatsResponse toEventStatsResponse(EventStats data);

    /** Maps StorageStats domain object to TableSizeEntry DTO. */
    public abstract TableSizeEntry toTableSizeEntry(StorageStats data);

    /** Maps list of StorageStats to list of TableSizeEntry DTOs. */
    public abstract List<TableSizeEntry> toTableSizeEntryList(List<StorageStats> data);

    /** Maps StorageSizeProjection to StorageStats domain object. */
    public abstract StorageStats toStorageStats(StorageSizeProjection projection);

    /** Maps list of StorageSizeProjection to list of StorageStats domain objects. */
    public abstract List<StorageStats> toStorageStatsList(List<StorageSizeProjection> projections);
}
