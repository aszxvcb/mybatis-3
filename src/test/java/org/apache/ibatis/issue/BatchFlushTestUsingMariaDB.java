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
package org.apache.ibatis.issue;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.issue.testcontainers.MariadbContainer;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatchFlushTestUsingMariaDB {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    Configuration configuration = new Configuration();
    Environment environment = new Environment("development", new JdbcTransactionFactory(),
      MariadbContainer.getUnpooledDataSource());
    configuration.setEnvironment(environment);
    configuration.addMapper(Mapper.class);
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);

    BaseDataTest.runScript(sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(),
      "org/apache/ibatis/issue/CreateDB.sql");
  }

  @Test
  void whenExecutorTypeIsBatch_ThenInvokeFlushStatementsViaMapper() {
    try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {

      List<Integer> retValList = new ArrayList<>();;
      Mapper mapper = session.getMapper(Mapper.class);

      User user1 = new User(1, "User1", "new_ash");
      retValList.add(Integer.valueOf(mapper.updateNickname(user1))); // actual affectedRow -> 1 row
      User user2 = new User(2, "User2", "new_bread");
      retValList.add(Integer.valueOf(mapper.updateNickname(user2))); // actual affectedRow -> 1 row
      User user3 = new User(3, "User3", "new_cat");
      retValList.add(Integer.valueOf(mapper.updateNickname(user3))); // actual affectedRow -> 1 row
      User user4 = new User(4, "User4", "new_daddy");
      retValList.add(Integer.valueOf(mapper.updateNickname(user4))); // actual affectedRow -> 1 row

      // test
      assertThat(retValList.get(0).intValue()).isNotEqualTo(1); // no return affectedRow because using 'ExecutorType.Batch'

      List<BatchResult> results = session.flushStatements();

      assertThat(results.size()).isEqualTo(1);
      assertThat(results.get(0).getUpdateCounts().length).isEqualTo(retValList.size());

      for (Integer retVal : retValList) {
        assertThat(retVal).isGreaterThanOrEqualTo(0); // test failed!
      }

      session.rollback();
    }

  }

}
