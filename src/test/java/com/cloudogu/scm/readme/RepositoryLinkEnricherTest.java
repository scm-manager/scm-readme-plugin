package com.cloudogu.scm.readme;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:com/cloudogu/scm/readme/shiro.ini", username = "trillian", password = "secret")
public class RepositoryLinkEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  ReadmeManager readmeManager;
  @Mock
  RepositoryServiceFactory serviceFactory;
  @Mock
  private HalAppender appender;
  private RepositoryLinkEnricher enricher;

  public RepositoryLinkEnricherTest() {
    // cleanup state that might have been left by other tests
    ThreadContext.unbindSecurityManager();
    ThreadContext.unbindSubject();
    ThreadContext.remove();
  }

  @Before
  public void setUp() throws IOException {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    when(readmeManager.getReadmePath(any())).thenReturn(Optional.of("/README"));
  }

  @Test
  @SubjectAware(username = "dent", password = "secret")
  public void shouldEnrich() {
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider,readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender).appendLink("readme", "https://scm-manager.org/scm/api/v2/plugins/readme/space/name");
  }

  @Test
  @SubjectAware(username = "noAccess", password = "secret")
  public void shouldNotEnrichBecauseOfMissingPermission() {
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider,readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(),any());
  }

  @Test
  @SubjectAware(username = "dent", password = "secret")
  public void shouldNotEnrichBecauseOfMissingReadmeFile() throws IOException {
    when(readmeManager.getReadmePath(any())).thenReturn(Optional.ofNullable(null));
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider,readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(),any());
  }
}
