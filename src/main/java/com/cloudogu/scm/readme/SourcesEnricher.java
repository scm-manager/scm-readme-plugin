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

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

@Extension
@Enrich(BrowserResult.class)
@Slf4j
class SourcesEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  SourcesEnricher(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @Override
  public void enrich(HalEnricherContext halEnricherContext, HalAppender halAppender) {
    NamespaceAndName namespaceAndName = halEnricherContext.oneRequireByType(NamespaceAndName.class);
    BrowserResult result = halEnricherContext.oneRequireByType(BrowserResult.class);
    FileObject file = result.getFile();

    if (containsReadMe(file)) {
      halAppender.appendLink(
        "readme",
        createReadmeLink(
          namespaceAndName.getNamespace(),
          namespaceAndName.getName(),
          result.getRequestedRevision(),
          file.getPath()
        )
      );
    }
  }

  private boolean containsReadMe(FileObject file) {
    return file.getChildren().stream()
      .filter(fo -> !fo.isDirectory())
      .anyMatch(fo -> ReadmeManager.README_FILES.contains(fo.getName().toLowerCase()));
  }

  private String createReadmeLink(String namespace, String name, String revision, String path) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get().get(), ReadmeResource.class);
    return linkBuilder
      .method("getByRevisionAndPath")
      .parameters(namespace, name, revision, path)
      .href();
  }
}
