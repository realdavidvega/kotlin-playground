package com.functional.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator

class DatabaseConfig : AbstractR2dbcConfiguration() {
  override fun connectionFactory(): ConnectionFactory =
    PostgresqlConnectionFactory(
      PostgresqlConnectionConfiguration.builder()
        .host("localhost")
        .database("personDb")
        .username("someUser")
        .password("somePassword")
        .build()
    )

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
