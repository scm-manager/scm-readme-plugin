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
