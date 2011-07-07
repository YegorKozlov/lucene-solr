package org.apache.solr.update;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.solr.core.SolrCore;

public final class DefaultIndexWriterProvider implements IndexWriterProvider {
  private int refCnt = 1;
  private IndexWriter indexWriter = null;
  private SolrCore core;

  public DefaultIndexWriterProvider(SolrCore core) {
    this.core = core;
  }
  
  @Override
  public synchronized IndexWriter getIndexWriter() throws IOException {
    if (indexWriter == null) {
      indexWriter = createMainIndexWriter("DirectUpdateHandler2", false);
    }
    return indexWriter;
  }

  @Override
  public synchronized void newIndexWriter() throws IOException {
    if (indexWriter != null) {
      indexWriter.close();
    }
    indexWriter = createMainIndexWriter("DirectUpdateHandler2",
        false);
  }

  @Override
  public synchronized void decref() throws IOException {
    refCnt--;
    if (refCnt == 0 && indexWriter != null) {
      indexWriter.close();
    }
  }

  @Override
  public synchronized void incref() {
    if (refCnt == 0) {
      throw new IllegalStateException("IndexWriter has been closed");
    }
    refCnt++;
  }

  @Override
  public synchronized void rollbackIndexWriter() throws IOException {
    indexWriter.rollback();
    newIndexWriter();
  }
  
  protected SolrIndexWriter createMainIndexWriter(String name,
      boolean removeAllExisting) throws IOException {
    return new SolrIndexWriter(name, core.getNewIndexDir(),
        core.getDirectoryFactory(), removeAllExisting, core.getSchema(),
        core.getSolrConfig().mainIndexConfig, core.getDeletionPolicy(), core.getCodecProvider());
  }
  
}
