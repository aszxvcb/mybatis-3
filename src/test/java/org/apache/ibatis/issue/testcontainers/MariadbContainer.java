/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.issue.testcontainers;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.testcontainers.containers.MariaDBContainer;

import javax.sql.DataSource;

public class MariadbContainer {

  private static final String DB_NAME = "mariadb_test";
  private static final String USERNAME = "u";
  private static final String PASSWORD = "p";
  private static final String DRIVER = "org.mariadb.jdbc.Driver";

  private static final MariaDBContainer<?> INSTANCE = initContainer();

  private static MariaDBContainer<?> initContainer() {
    @SuppressWarnings("resource")
    MariaDBContainer<?> container = new MariaDBContainer<>().withDatabaseName(DB_NAME).withUsername(USERNAME)
      .withPassword(PASSWORD).withUrlParam("useSSL", "false");
    container.start();
    return container;
  }

  public static DataSource getUnpooledDataSource() {
    return new UnpooledDataSource(MariadbContainer.DRIVER, INSTANCE.getJdbcUrl(), MariadbContainer.USERNAME,
      MariadbContainer.PASSWORD);
  }

  public static PooledDataSource getPooledDataSource() {
    return new PooledDataSource(MariadbContainer.DRIVER, INSTANCE.getJdbcUrl(), MariadbContainer.USERNAME,
      MariadbContainer.PASSWORD);
  }

  private MariadbContainer() {
  }
}
