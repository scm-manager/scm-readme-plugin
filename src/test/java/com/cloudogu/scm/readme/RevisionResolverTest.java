/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cloudogu.scm.readme;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevisionResolverTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryService repositoryService;

  private final RevisionResolver resolver = new RevisionResolver();

  @Test
  void shouldReturnBranchFromResult() throws IOException {
    mockLogResult("develop");

    Optional<String> revision = resolver.resolve(repositoryService);
    assertThat(revision).contains("develop");
  }

  @Test
  void shouldReturnBranchChangesetBranches() throws IOException {
    Changeset changeset = new Changeset();
    changeset.setBranches(Collections.singletonList("master"));
    mockLogResult(changeset);

    Optional<String> revision = resolver.resolve(repositoryService);
    assertThat(revision).contains("master");
  }

  @Test
  void shouldReturnRevisionFromChangeset() throws IOException {
    mockLogResult(new Changeset("42", 1L, null));

    Optional<String> revision = resolver.resolve(repositoryService);
    assertThat(revision).contains("42");
  }

  @Test
  void shouldReturnEmptyOptionalWithoutChangesetResult() {
    Optional<String> revision = resolver.resolve(repositoryService);
    assertThat(revision).isEmpty();
  }

  @Test
  void shouldReturnEmptyOptionalBranchAndChangeset() throws IOException {
    mockLogResult(new ChangesetPagingResult(0, Collections.emptyList()));
    Optional<String> revision = resolver.resolve(repositoryService);
    assertThat(revision).isEmpty();
  }

  private void mockLogResult(String branchName) throws IOException {
    mockLogResult(new ChangesetPagingResult(0, Collections.emptyList(), branchName));
  }

  private void mockLogResult(Changeset changeset) throws IOException {
    mockLogResult(new ChangesetPagingResult(1, Collections.singletonList(changeset)));
  }

  private void mockLogResult(ChangesetPagingResult result) throws IOException {
    when(repositoryService.getLogCommand().setPagingLimit(1).getChangesets()).thenReturn(result);
  }

}
