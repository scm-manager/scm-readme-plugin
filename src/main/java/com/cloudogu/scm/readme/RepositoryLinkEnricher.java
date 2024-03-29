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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

@Extension
@Enrich(Repository.class)
@Slf4j
public class RepositoryLinkEnricher implements HalEnricher {

  private static final Logger LOG = LoggerFactory.getLogger(RepositoryLinkEnricher.class);
  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  private final ReadmeManager readmeManager;

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, ReadmeManager readmeManager, RepositoryServiceFactory serviceFactory) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.readmeManager = readmeManager;
    this.serviceFactory = serviceFactory;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      boolean readmeFileExists = readmeManager.getReadmePath(repositoryService).isPresent();
      if (RepositoryPermissions.read(repository).isPermitted() && readmeFileExists) {
        LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), ReadmeResource.class);
        appender.appendLink("readme", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
      }
    } catch (Exception e) {
      LOG.error("Could not append readme link to repository", e);
    }
  }
}
