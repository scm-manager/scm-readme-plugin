/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
