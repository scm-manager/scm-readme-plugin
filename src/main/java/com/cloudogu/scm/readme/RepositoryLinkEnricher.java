package com.cloudogu.scm.readme;

import lombok.extern.slf4j.Slf4j;
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

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Repository.class)
@Slf4j
public class RepositoryLinkEnricher implements HalEnricher {

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
      if (RepositoryPermissions.read(repository).isPermitted() && readmeManager.getReadmePath(repositoryService).isPresent()) {
        LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), ReadmeResource.class);
        appender.appendLink("readme", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
      }
    }
  }
}
