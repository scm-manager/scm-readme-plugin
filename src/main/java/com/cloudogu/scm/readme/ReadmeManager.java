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

import com.github.legman.Subscribe;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.*;

@Slf4j
@Singleton
public class ReadmeManager {

  private static final String CACHE_NAME = "sonia.scm.readme-plugin";

  private static final List<String> README_FILES = Arrays.asList("readme.md", "readme.txt", "readme", "readme.markdown");

  private final RepositoryServiceFactory serviceFactory;
  private final RevisionResolver revisionResolver;
  private final Cache<String, CachedResult> cache;

  @EqualsAndHashCode
  private static class CachedResult {
    private String path;
    public CachedResult(String path) {
      this.path = path;
    }
  }

  @Inject
  public ReadmeManager(RepositoryServiceFactory serviceFactory, RevisionResolver revisionResolver, CacheManager cacheManager) {
    this.serviceFactory = serviceFactory;
    this.revisionResolver = revisionResolver;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  @Subscribe
  public void clearCache(PostReceiveRepositoryHookEvent event) {
    String repositoryId = event.getRepository().getId();
    log.debug("clear readme path cache for repository {}", repositoryId);
    cache.remove(repositoryId);
  }

  Optional<Readme> getReadme(String namespace, String name) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      return getReadmePath(repositoryService)
        .flatMap(rp -> readFile(repositoryService, rp))
        .flatMap(content -> revisionResolver.resolve(repositoryService).map(branch -> new Readme(branch, content)));
    }
  }

  private Optional<String> readFile(RepositoryService repositoryService, String readmePath) {
    try {
      return of(repositoryService.getCatCommand().getContent(readmePath));
    } catch (IOException e) {
      log.error("There is an error while getting the content of the readme file", e);
      return empty();
    }
  }

  Optional<String> getReadmePath(RepositoryService repositoryService) {
    String repositoryId = repositoryService.getRepository().getId();

    CachedResult cachedResult = cache.get(repositoryId);
    if (cachedResult != null) {
      log.trace("return readme path {} from cache", cachedResult.path);
      return Optional.ofNullable(cachedResult.path);
    }

    log.trace("try to find readme for {}", repositoryId);
    Optional<String> readmePath = findReadmePath(repositoryService);
    cache.put(repositoryId, new CachedResult(readmePath.orElse(null)));
    return readmePath;
  }

  private Optional<String> findReadmePath(RepositoryService repositoryService) {
    return browse(repositoryService)
      .map(BrowserResult::getFile)
      .flatMap(fo -> fo.getChildren()
        .stream()
        .map(FileObject::getName)
        .filter(fileName -> README_FILES.contains(fileName.toLowerCase()))
        .findFirst());
  }

  private Optional<BrowserResult> browse(RepositoryService repositoryService) {
    try {
      return ofNullable(repositoryService.getBrowseCommand()
        .setDisableCache(true)
        .setDisableLastCommit(true)
        .setPath("/")
        .getBrowserResult());
    } catch (IOException e) {
      log.error("error on getting browsing repository {}", repositoryService.getRepository().getNamespaceAndName(), e);
      return empty();
    }
  }
}
