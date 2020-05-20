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

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.api.RepositoryService;

import java.io.IOException;
import java.util.Optional;

import static java.util.Optional.empty;

public final class RevisionResolver {

  private static final Logger LOG = LoggerFactory.getLogger(RevisionResolver.class);

  Optional<String> resolve(RepositoryService repositoryService) {
    try {
      ChangesetPagingResult changesets = repositoryService.getLogCommand().setPagingLimit(1).getChangesets();
      if (changesets != null) {
        String branch = changesets.getBranchName();
        if (!Strings.isNullOrEmpty(branch)) {
          return Optional.of(branch);
        }

        Changeset changeset = Iterables.getFirst(changesets, null);
        if (changeset != null) {
          if (!changeset.getBranches().isEmpty()) {
            return Optional.of(changeset.getBranches().get(0));
          }
          return Optional.of(changeset.getId());
        } else {
          LOG.warn("repository service returned changeset result without changesets");
        }
      } else {
        LOG.warn("repository service returned no changeset result");
      }
    } catch (IOException e) {
      LOG.error("There is an error while getting the content of the readme file", e);
    }
    return empty();
  }

}
