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

import com.google.inject.util.Providers;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SourcesEnricherTest {

  private final NamespaceAndName namespaceAndName = new NamespaceAndName("namespace", "name");

  private SourcesEnricher sourcesEnricher;

  @Mock
  private HalAppender appender;

  @BeforeEach
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    sourcesEnricher = new SourcesEnricher(scmPathInfoStoreProvider);
  }

  @Test
  void shouldNotEnrichBecauseDirectoryDoesNotContainReadme() {
    FileObject file = new FileObject();
    file.setDirectory(false);
    file.setName("some_random.txt");
    file.setPath("/some_random.txt");

    FileObject directory = new FileObject();
    directory.setDirectory(true);
    directory.setName("/");
    directory.setPath("/");
    directory.setChildren(List.of(file));
    BrowserResult result = new BrowserResult("rev", "main", directory);

    HalEnricherContext context = HalEnricherContext.of(result, namespaceAndName);
    sourcesEnricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

  @Test
  void shouldNotEnrichBecauseFileIsNotDirectory() {
    FileObject file = new FileObject();
    file.setDirectory(false);
    file.setName("some_random.txt");
    file.setPath("/some_random.txt");

    BrowserResult result = new BrowserResult("rev", "main", file);

    HalEnricherContext context = HalEnricherContext.of(result, namespaceAndName);
    sourcesEnricher.enrich(context, appender);

    verifyNoInteractions(appender);
  }

  @ParameterizedTest
  @ValueSource(strings = {"READme.txt", "ReAdMe.md", "readME", "README.MARKDOWN"})
  void shouldEnrichWithReadmeLink(String readmeFileName) {
    FileObject file = new FileObject();
    file.setDirectory(false);
    file.setName(readmeFileName);
    file.setPath("sub/" + readmeFileName);

    FileObject directory = new FileObject();
    directory.setDirectory(true);
    directory.setName("sub");
    directory.setPath("sub");
    directory.setChildren(List.of(file));
    BrowserResult result = new BrowserResult("rev", "main", directory);

    HalEnricherContext context = HalEnricherContext.of(result, namespaceAndName);
    sourcesEnricher.enrich(context, appender);

    verify(appender).appendLink(
      "readme",
      String.format(
        "https://scm-manager.org/scm/api/v2/plugins/readme/%s/%s/main/sub",
        namespaceAndName.getNamespace(),
        namespaceAndName.getName()
      )
    );
  }
}
