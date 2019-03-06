package com.cloudogu.scm.readme;

import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;
import javax.inject.Provider;

@Extension
@Enrich(Repository.class)
public class RepositoryLinkEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  private final ReadmeManager readmeManager;

  @Inject
  public RepositoryLinkEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider, ReadmeManager readmeManager) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.readmeManager = readmeManager;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (RepositoryPermissions.read(repository).isPermitted() && readmeManager.getReadmeContent(repository.getNamespace(), repository.getName()) != null ) {
      LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), ReadmeResource.class);
      appender.appendLink("readme", linkBuilder.method("get").parameters(repository.getNamespace(), repository.getName()).href());
    }
  }
}
