package com.giwon.otakufeed.bootstrap

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

@ConfigurationProperties(prefix = "otaku-feed.jdbc")
data class JdbcProperties(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val driverClassName: String = "org.postgresql.Driver",
)

@Configuration
class JdbcConfig(private val props: JdbcProperties) {

    @Bean
    fun dataSource(): DataSource = DriverManagerDataSource().apply {
        setDriverClassName(props.driverClassName)
        val base = props.url
        url = if (base.contains('?')) "$base&currentSchema=otaku" else "$base?currentSchema=otaku"
        username = props.username
        password = props.password
    }

    @Bean
    fun jdbcTemplate(ds: DataSource) = JdbcTemplate(ds)
}
