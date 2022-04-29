/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.fs.s3a;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.s3a.impl.ChangeDetectionPolicy;
import org.apache.hadoop.fs.s3a.statistics.S3AStatisticsContext;
import org.apache.hadoop.fs.store.audit.AuditSpan;

import javax.annotation.Nullable;

import org.apache.hadoop.util.Preconditions;

import static org.apache.hadoop.util.Preconditions.checkNotNull;

/**
 * Read-specific operation context struct.
 */
public class S3AReadOpContext extends S3AOpContext {

  /**
   * Path of read.
   */
  private final Path path;

  /**
   * Initial input policy of the stream.
   */
  private final S3AInputPolicy inputPolicy;

  /**
   * How to detect and deal with the object being updated during read.
   */
  private final ChangeDetectionPolicy changeDetectionPolicy;

  /**
   * Readahead for GET operations/skip, etc.
   */
  private final long readahead;

  private final AuditSpan auditSpan;

  /**
   * Vectored IO context for vectored read api
   * in {@code S3AInputStream#readVectored(List, IntFunction)}.
   */
  private final VectoredIOContext vectoredIOContext;

  /**
   * Instantiate.
   * @param path path of read
   * @param invoker invoker for normal retries.
   * @param stats Fileystem statistics (may be null)
   * @param instrumentation statistics context
   * @param dstFileStatus target file status
   * @param inputPolicy the input policy
   * @param changeDetectionPolicy change detection policy.
   * @param readahead readahead for GET operations/skip, etc.
   * @param auditSpan active audit
   * @param vectoredIOContext context for vectored read operation.
   */
  public S3AReadOpContext(
        final Path path,
        Invoker invoker,
        @Nullable FileSystem.Statistics stats,
        S3AStatisticsContext instrumentation,
        FileStatus dstFileStatus,
        S3AInputPolicy inputPolicy,
        ChangeDetectionPolicy changeDetectionPolicy,
        final long readahead,
        final AuditSpan auditSpan,
        VectoredIOContext vectoredIOContext) {

    super(invoker, stats, instrumentation,
        dstFileStatus);
    this.path = checkNotNull(path);
    this.auditSpan = auditSpan;
    Preconditions.checkArgument(readahead >= 0,
        "invalid readahead %d", readahead);
    this.inputPolicy = checkNotNull(inputPolicy);
    this.changeDetectionPolicy = checkNotNull(changeDetectionPolicy);
    this.readahead = readahead;
    this.vectoredIOContext = checkNotNull(vectoredIOContext);
  }

  /**
   * Get invoker to use for read operations.
   * @return invoker to use for read codepaths
   */
  public Invoker getReadInvoker() {
    return invoker;
  }

  /**
   * Get the path of this read.
   * @return path.
   */
  public Path getPath() {
    return path;
  }

  /**
   * Get the IO policy.
   * @return the initial input policy.
   */
  public S3AInputPolicy getInputPolicy() {
    return inputPolicy;
  }

  public ChangeDetectionPolicy getChangeDetectionPolicy() {
    return changeDetectionPolicy;
  }

  /**
   * Get the readahead for this operation.
   * @return a value {@literal >=} 0
   */
  public long getReadahead() {
    return readahead;
  }

  /**
   * Get the audit which was active when the file was opened.
   * @return active span
   */
  public AuditSpan getAuditSpan() {
    return auditSpan;
  }

  /**
   * Get Vectored IO context for this this read op.
   * @return vectored IO context.
   */
  public VectoredIOContext getVectoredIOContext() {
    return vectoredIOContext;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(
        "S3AReadOpContext{");
    sb.append("path=").append(path);
    sb.append(", inputPolicy=").append(inputPolicy);
    sb.append(", readahead=").append(readahead);
    sb.append(", changeDetectionPolicy=").append(changeDetectionPolicy);
    sb.append('}');
    return sb.toString();
  }
}
