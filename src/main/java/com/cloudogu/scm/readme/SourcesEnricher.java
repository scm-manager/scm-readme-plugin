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
