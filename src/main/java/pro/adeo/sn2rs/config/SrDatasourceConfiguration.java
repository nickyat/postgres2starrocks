package pro.adeo.sn2rs.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

//@Configuration
public class SrDatasourceConfiguration {
    @Bean
    @ConfigurationProperties("spring.datasource.sr")
    public DataSourceProperties srDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.sr.hikari")
    public DataSource srDataSource() {
        return srDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }
    @Bean
    public JdbcTemplate srJdbcTemplate(@Qualifier("srDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
