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

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.net.URI;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryLinkEnricherTest {

  private Provider<ScmPathInfoStore> scmPathInfoStoreProvider;

  @Mock
  private Subject subject;
  @Mock
  private ReadmeManager readmeManager;
  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock
  private HalAppender appender;
  private RepositoryLinkEnricher enricher;

  @BeforeEach
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(Repository.class))).thenReturn(service);
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    lenient().when(readmeManager.getReadmePath(any())).thenReturn(Optional.of("/README"));

    ThreadContext.bind(subject);
  }

  @Test
  void shouldEnrich() {
    Repository repo = new Repository("id", "type", "space", "name");
    when(subject.isPermitted("repository:read:id")).thenReturn(true);

    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, readmeManager, serviceFactory);
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender).appendLink("readme", "https://scm-manager.org/scm/api/v2/plugins/readme/space/name");
  }

  @Test
  void shouldNotEnrichBecauseOfMissingPermission() {
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichBecauseOfMissingReadmeFile() {
    when(readmeManager.getReadmePath(any())).thenReturn(Optional.empty());
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(), any());
  }

  @Test
  void shouldNotEnrichBecauseOfInternalRepositoryError() {
    doThrow(InternalRepositoryException.class).when(readmeManager).getReadmePath(any());
    enricher = new RepositoryLinkEnricher(scmPathInfoStoreProvider, readmeManager, serviceFactory);
    Repository repo = new Repository("id", "type", "space", "name");
    HalEnricherContext context = HalEnricherContext.of(repo);
    enricher.enrich(context, appender);
    verify(appender, never()).appendLink(any(), any());
  }
}
