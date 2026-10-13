package pt.upt.studymatch.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import pt.upt.studymatch.dto.HealthResponse;

@Service
public class HealthService {

    private static final int DB_TIMEOUT_SECONDS = 2;

    private final DataSource dataSource;

    public HealthService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HealthResponse check() {
        return new HealthResponse("UP", isDatabaseReachable() ? "UP" : "DOWN");
    }

    private boolean isDatabaseReachable() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(DB_TIMEOUT_SECONDS);
        } catch (SQLException ex) {
            return false;
        }
    }
}
