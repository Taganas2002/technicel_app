package com.technicel.repository;

import com.technicel.dto.DashboardSummaryResponse;
import com.technicel.dto.DropRankingRowResponse;
import com.technicel.dto.TechnicianProgressRowResponse;
import com.technicel.entity.ListType;
import com.technicel.entity.WorkListStatus;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DashboardQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public DashboardSummaryResponse loadSummary() {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM users WHERE role = 'TECHNICIAN') AS totalTechnicians,
                  (SELECT COUNT(*) FROM users WHERE role = 'TECHNICIAN' AND active = 1) AS activeTechnicians,
                  (SELECT COUNT(DISTINCT technician_id) FROM daily_progress WHERE work_date = CURRENT_DATE) AS techniciansWorkingToday,
                  (SELECT COUNT(*) FROM work_lists WHERE status = 'IN_PROGRESS') AS activeLists,
                  (SELECT COUNT(*) FROM work_lists WHERE status = 'FINISHED') AS finishedLists,
                  (SELECT COALESCE(SUM(completed_drops), 0) FROM daily_progress WHERE work_date = CURRENT_DATE) AS dropsCompletedToday,
                  (SELECT COUNT(*) FROM day_off_requests WHERE status = 'PENDING') AS pendingDayOffRequests
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource(), (rs, rowNum) -> new DashboardSummaryResponse(
                rs.getLong("totalTechnicians"),
                rs.getLong("activeTechnicians"),
                rs.getLong("techniciansWorkingToday"),
                rs.getLong("activeLists"),
                rs.getLong("finishedLists"),
                rs.getLong("dropsCompletedToday"),
                rs.getLong("pendingDayOffRequests")
        ));
    }

    public long countTechnicians(String search, ListType listType, Boolean active, WorkListStatus listStatus) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*) FROM users u
                LEFT JOIN work_lists wl ON wl.id = (
                  SELECT w2.id FROM work_lists w2
                  WHERE w2.technician_id = u.id
                  ORDER BY CASE WHEN w2.status = 'IN_PROGRESS' THEN 0 ELSE 1 END, w2.updated_at DESC
                  LIMIT 1
                )
                WHERE u.role = 'TECHNICIAN'
                """);
        appendFilters(sql, params, search, listType, active, listStatus);
        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count == null ? 0 : count;
    }

    public List<TechnicianProgressRowResponse> findTechnicians(
            String search,
            ListType listType,
            Boolean active,
            WorkListStatus listStatus,
            Pageable pageable
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT
                  u.id AS technicianId,
                  u.technician_code AS technicianCode,
                  CONCAT(u.first_name, ' ', u.last_name) AS fullName,
                  u.nip AS nip,
                  u.active AS active,
                  wl.id AS currentListId,
                  wl.list_type AS listType,
                  wl.initial_drop_count AS initialDrops,
                  COALESCE(totals.completed, 0) AS completedDrops,
                  wl.status AS status,
                  COALESCE(wl.problem_drop_count, 0) AS problemDropCount,
                  COALESCE(today.today_drops, 0) AS dropsCompletedToday,
                  wl.reference_date AS referenceDate
                FROM users u
                LEFT JOIN work_lists wl ON wl.id = (
                  SELECT w2.id FROM work_lists w2
                  WHERE w2.technician_id = u.id
                  ORDER BY CASE WHEN w2.status = 'IN_PROGRESS' THEN 0 ELSE 1 END, w2.updated_at DESC
                  LIMIT 1
                )
                LEFT JOIN (
                  SELECT work_list_id, SUM(completed_drops) AS completed
                  FROM daily_progress
                  GROUP BY work_list_id
                ) totals ON totals.work_list_id = wl.id
                LEFT JOIN (
                  SELECT work_list_id, SUM(completed_drops) AS today_drops
                  FROM daily_progress
                  WHERE work_date = CURRENT_DATE
                  GROUP BY work_list_id
                ) today ON today.work_list_id = wl.id
                WHERE u.role = 'TECHNICIAN'
                """);
        appendFilters(sql, params, search, listType, active, listStatus);
        sql.append(" ORDER BY u.technician_code ASC LIMIT :limit OFFSET :offset");
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        return jdbc.query(sql.toString(), params, (rs, rowNum) -> {
            TechnicianProgressRowResponse row = new TechnicianProgressRowResponse();
            row.setTechnicianId(rs.getLong("technicianId"));
            row.setTechnicianCode(rs.getString("technicianCode"));
            row.setFullName(rs.getString("fullName"));
            row.setNip(rs.getString("nip"));
            row.setActive(rs.getBoolean("active"));
            long listId = rs.getLong("currentListId");
            if (!rs.wasNull()) {
                row.setCurrentListId(listId);
                String type = rs.getString("listType");
                row.setListType(type == null ? null : ListType.valueOf(type));
                int initial = rs.getInt("initialDrops");
                int completed = rs.getInt("completedDrops");
                row.setInitialDrops(initial);
                row.setCompletedDrops(completed);
                row.setProgressPercentage(initial == 0 ? 0 : Math.min(100.0, Math.round(completed * 10000.0 / initial) / 100.0));
                String status = rs.getString("status");
                row.setStatus(status == null ? null : WorkListStatus.valueOf(status));
                int problem = rs.getInt("problemDropCount");
                row.setProblemDropCount(problem);
                row.setRemainingDrops("FINISHED".equals(status) ? 0 : Math.max(initial - completed, 0));
                Date reference = rs.getDate("referenceDate");
                row.setReferenceDate(reference == null ? null : reference.toLocalDate());
            } else {
                row.setCompletedDrops(0);
                row.setRemainingDrops(null);
                row.setProgressPercentage(null);
            }
            row.setDropsCompletedToday(rs.getInt("dropsCompletedToday"));
            return row;
        });
    }

    public List<DropRankingRowResponse> findDropRankings(LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                  u.id AS technicianId,
                  u.technician_code AS technicianCode,
                  CONCAT(u.first_name, ' ', u.last_name) AS fullName,
                  COALESCE(SUM(dp.completed_drops), 0) AS completedDrops
                FROM users u
                LEFT JOIN daily_progress dp
                  ON dp.technician_id = u.id
                 AND dp.work_date BETWEEN :fromDate AND :toDate
                WHERE u.role = 'TECHNICIAN'
                GROUP BY u.id, u.technician_code, u.first_name, u.last_name
                ORDER BY completedDrops DESC, u.technician_code ASC
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("fromDate", fromDate)
                .addValue("toDate", toDate);
        return jdbc.query(sql, params, (rs, rowNum) -> new DropRankingRowResponse(
                0,
                rs.getLong("technicianId"),
                rs.getString("technicianCode"),
                rs.getString("fullName"),
                rs.getInt("completedDrops")
        ));
    }

    private void appendFilters(
            StringBuilder sql,
            MapSqlParameterSource params,
            String search,
            ListType listType,
            Boolean active,
            WorkListStatus listStatus
    ) {
        if (search != null && !search.isBlank()) {
            sql.append("""
                     AND (
                      LOWER(u.first_name) LIKE :search
                      OR LOWER(u.last_name) LIKE :search
                      OR LOWER(CONCAT(u.first_name, ' ', u.last_name)) LIKE :search
                      OR LOWER(u.technician_code) LIKE :search
                      OR LOWER(u.nip) LIKE :search
                    )
                    """);
            params.addValue("search", "%" + search.trim().toLowerCase() + "%");
        }
        if (listType != null) {
            sql.append(" AND wl.list_type = :listType");
            params.addValue("listType", listType.name());
        }
        if (active != null) {
            sql.append(" AND u.active = :active");
            params.addValue("active", active);
        }
        if (listStatus != null) {
            sql.append(" AND wl.status = :listStatus");
            params.addValue("listStatus", listStatus.name());
        }
    }
}
