package com.example.r2dbc.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

@Configuration
@EnableR2dbcRepositories
class DatabaseConfig : AbstractR2dbcConfiguration() {

  @Bean
  override fun connectionFactory(): ConnectionFactory =
    PostgresqlConnectionFactory(
      PostgresqlConnectionConfiguration.builder()
        .host("localhost")
        .database("someDb")
        .username("someUser")
        .password("somePassword")
        .build()
    )

  @Bean
  fun databaseInitializer(connectionFactory: ConnectionFactory): ConnectionFactoryInitializer =
    ConnectionFactoryInitializer().apply {
      setConnectionFactory(connectionFactory)
      setDatabasePopulator(
        CompositeDatabasePopulator().apply {
          addPopulators(
            ResourceDatabasePopulator(
              ClassPathResource("sql/schema.sql"),
              ClassPathResource("sql/data.sql")
            )
          )
        }
      )
    }
}
