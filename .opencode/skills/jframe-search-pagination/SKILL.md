# Skill: jframe-search-pagination

# jFrame Search & Pagination Pattern

Standard pattern for implementing searchable, paginated endpoints in Eventify using the jFrame library.

## Flow

```
Controller (POST /search, @RequestBody SortablePageInput)
    → Service (MetaData.toSort + toSearchSpecification → Repository.findAll(spec, pageable))
        → Mapper (PageMapper.toPageResource(page))
            → PageResource<Response>
```

## Implementation Checklist

| # | Layer | Class | Extends/Implements |
|---|-------|-------|--------------------|
| 1 | Entity | `XxxEntity` | `PageableItem` (marker) |
| 2 | Response DTO | `XxxResponse` | `PageableItemResource` (marker) |
| 3 | Repository | `XxxRepository` | `JpaSpecificationExecutor<Entity>` |
| 4 | MetaData | `XxxMetaData` (`@Component`) | `AbstractSortSearchMetaData` |
| 5 | Mapper | `XxxMapper` (abstract class) | `PageMapper<Response, Entity>` |
| 6 | Service | `XxxService` | — |
| 7 | Controller | `XxxController` | — |

## Layer Details

### 1. MetaData — Field Registration

```java
@Component
public class XxxMetaData extends AbstractSortSearchMetaData {
    public static final String FIELD_NAME = "fieldName";

    public XxxMetaData() {
        super();
        // Single column
        addField("title", "title", SearchType.FUZZY_TEXT, true);
        // Enum
        addField("category", "category", SearchType.ENUM, MyEnum.class, true);
        // Multi-column fuzzy (OR across columns)
        addField("search", List.of("user.email", "user.firstName", "user.lastName"),
                 SearchType.MULTI_COLUMN_FUZZY, false);
        // Nested path
        addField("sentByEmail", "sentBy.email", SearchType.FUZZY_TEXT, true);
        // Date range
        addField("createdAt", "createdAt", SearchType.DATE, true);
    }

    public Specification<XxxEntity> toSearchSpecification(final SortablePageInput input) {
        final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
        return new JpaSearchSpecification<>(criteria);
    }
}
```

**With additional fixed filter (e.g., parent ID):**
```java
public Specification<XxxEntity> toFilteredSpecification(
        final SortablePageInput input, final Long parentId) {
    final List<SearchCriterium> criteria = toSearchCriteria(input.getSearchInputs());
    final Specification<XxxEntity> searchSpec = new JpaSearchSpecification<>(criteria);
    return Specification.where(searchSpec)
        .and((root, query, cb) -> cb.equal(root.get("parent").get("id"), parentId));
}
```

### SearchType Reference

| SearchType | SQL | Input field |
|------------|-----|-------------|
| `TEXT` | `= value` | `textValue` |
| `FUZZY_TEXT` | `LIKE %value%` | `textValue` |
| `NUMERIC` | `= value` | `textValue` |
| `BOOLEAN` | `= true/false` | `textValue` |
| `DATE` | `BETWEEN from AND to` | `fromDateValue`, `toDateValue` |
| `ENUM` | `= ENUM_VALUE` | `textValue` |
| `MULTI_ENUM` | `IN (v1, v2, ...)` | `textValueList` |
| `MULTI_TEXT` | `IN (v1, v2, ...)` | `textValueList` |
| `MULTI_COLUMN_FUZZY` | `LIKE %value%` across columns (OR) | `textValue` |

### 2. Mapper — PageMapper

```java
@Mapper(config = SharedMapperConfig.class, uses = DateTimeMapper.class)
public abstract class XxxMapper extends PageMapper<XxxResponse, XxxEntity> {

    @Override
    @Named("toResourceObject")
    @Mapping(source = "user.email", target = "email")
    public abstract XxxResponse toResourceObject(XxxEntity source);
}
```

**Rules:**
- Must be `abstract class` (not interface)
- Override `toResourceObject` with `@Named("toResourceObject")`
- Use `@Mapping` for field name differences or nested paths

### 3. Service

```java
private static final int DEFAULT_PAGE_SIZE = 20;

public Page<XxxEntity> searchXxx(final SortablePageInput input) {
    final Sort sort = metaData.toSort(input.getSortOrder());
    final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
    final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);
    final Specification<XxxEntity> spec = metaData.toSearchSpecification(input);
    return repository.findAll(spec, pageable);
}
```

### 4. Controller

```java
@PostMapping(path = XXX_SEARCH_PATH, produces = APPLICATION_JSON_VALUE)
public ResponseEntity<PageResource<XxxResponse>> searchXxx(
        @RequestBody final SortablePageInput input) {
    final Page<XxxEntity> page = service.searchXxx(input);
    return ResponseEntity.status(OK).body(mapper.toPageResource(page));
}
```

## Request/Response Shapes

**Request (`SortablePageInput`):**
```json
{
  "pageNumber": 0,
  "pageSize": 20,
  "sortOrder": [{ "name": "createdAt", "direction": "DESC" }],
  "searchInputs": [
    { "fieldName": "search", "textValue": "john" },
    { "fieldName": "category", "textValue": "SYSTEM" }
  ]
}
```

**Response (`PageResource<T>`):**
```json
{
  "totalElements": 47,
  "totalPages": 3,
  "pageSize": 20,
  "pageNumber": 0,
  "content": [...]
}
```

## Conventions

- Endpoint path: `POST /v1/.../xxx/search`
- Default page size: 20 (in service)
- Page number: 0-based
- Sort: defaults to unsorted if not provided
- Unknown search fields: logged and skipped (no exception)
- Nested paths use dot notation: `"sentBy.email"`, `"user.firstName"`
