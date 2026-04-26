package com.giwon.otakufeed.bootstrap

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

data class HikariProps(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 1,
    val idleTimeout: Long = 60_000,
    val connectionTimeout: Long = 5_000,
)

@ConfigurationProperties(prefix = "otaku-feed.jdbc")
data class JdbcProperties(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val driverClassName: String = "org.postgresql.Driver",
    val hikari: HikariProps = HikariProps(),
)

@Configuration
class JdbcConfig(private val props: JdbcProperties) {

    /**
     * HikariCP 커넥션 풀 — DriverManagerDataSource 대신 사용.
     * 매 요청 new connection 이슈 해소 (DriverManager는 풀링 안 함).
     * Railway 작은 인스턴스(512MB) 대응으로 max-pool-size 작게.
     */
    @Bean
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = props.driverClassName
            val base = props.url
            jdbcUrl = if (base.contains('?')) "$base&currentSchema=otaku" else "$base?currentSchema=otaku"
            username = props.username
            password = props.password
            maximumPoolSize = props.hikari.maximumPoolSize
            minimumIdle = props.hikari.minimumIdle
            idleTimeout = props.hikari.idleTimeout
            connectionTimeout = props.hikari.connectionTimeout
            poolName = "otaku-feed-pool"
        }
        return HikariDataSource(config)
    }

    @Bean
    fun jdbcTemplate(ds: DataSource) = JdbcTemplate(ds)
}
