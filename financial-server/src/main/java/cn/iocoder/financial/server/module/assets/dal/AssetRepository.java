package cn.iocoder.financial.server.module.assets.dal;

import cn.iocoder.financial.framework.tenant.core.context.TenantContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public AssetRepository(DataSource dataSource) {
        this.jdbc = new NamedParameterJdbcTemplate(dataSource);
    }

    private MapSqlParameterSource params(Map<String, ?> values) {
        Map<String, Object> scoped = new HashMap<>(values);
        scoped.put("tenantId", TenantContextHolder.getRequiredTenantId());
        return new MapSqlParameterSource(scoped);
    }

    public List<Map<String, Object>> list(String sql, Map<String, ?> values) {
        return jdbc.queryForList(sql, params(values));
    }

    public Map<String, Object> one(String sql, Map<String, ?> values) {
        List<Map<String, Object>> rows = list(sql, values);
        return rows.isEmpty() ? null : rows.getFirst();
    }

    public int update(String sql, Map<String, ?> values) {
        return jdbc.update(sql, params(values));
    }

    public long insert(String sql, Map<String, ?> values) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(sql, params(values), keys, new String[]{"id"});
        return keys.getKey().longValue();
    }
}
