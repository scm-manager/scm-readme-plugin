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
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Slf4j
@Singleton
public class ReadmeManager {

  static final List<String> README_FILES = List.of("readme.md", "readme.txt", "readme", "readme.markdown");
  private static final String CACHE_NAME = "sonia.scm.readme-plugin";
  private final RepositoryServiceFactory serviceFactory;
  private final RevisionResolver revisionResolver;
  private final Cache<String, CachedResult> cache;

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
    cache.removeAll(key -> key.startsWith(repositoryId));
  }

  Optional<Readme> getReadme(String namespace, String name) {
    return getReadmeByRevisionAndPath(namespace, name, null, null);
  }

  Optional<Readme> getReadmeByRevisionAndPath(String namespace, String name, String revision, String path) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();

      Optional<String> revisionOrDefaultBranch = evalRevisionOrDefaultBranch(repositoryService, revision);

      return revisionOrDefaultBranch
        .flatMap(revOrBranch -> getReadmePathByRevisionAndPath(repositoryService, revOrBranch, path)
          .flatMap(readmePath -> readFile(repositoryService, revOrBranch, readmePath)
            .flatMap(content -> Optional.of(new Readme(revOrBranch, content, readmePath)))));
    }
  }

  private Optional<String> evalRevisionOrDefaultBranch(RepositoryService repositoryService, String revision) {
    if (!Strings.isNullOrEmpty(revision)) {
      return Optional.of(revision);
    }

    return revisionResolver.resolve(repositoryService);
  }

  private Optional<String> readFile(RepositoryService repositoryService, String revision, String readmePath) {
    try {
      return of(repositoryService.getCatCommand().setRevision(revision).getContent(readmePath));
    } catch (IOException e) {
      log.error("There is an error while getting the content of the readme file", e);
      return empty();
    }
  }

  Optional<String> getReadmePath(RepositoryService repositoryService) {
    return getReadmePathByRevisionAndPath(repositoryService, null, null);
  }

  Optional<String> getReadmePathByRevisionAndPath(RepositoryService repositoryService, String revision, String path) {
    String validPath = Strings.isNullOrEmpty(path) ? "/" : path;
    String cacheKey = String.format("%s-%s-%s", repositoryService.getRepository().getId(), revision, validPath);

    CachedResult cachedResult = cache.get(cacheKey);
    if (cachedResult != null) {
      log.trace("return readme path {} from cache", cachedResult.path);
      return Optional.ofNullable(cachedResult.path);
    }

    log.trace("try to find readme for {}", cacheKey);
    Optional<String> readmePath = findReadmePath(repositoryService, revision, validPath);
    cache.put(cacheKey, new CachedResult(readmePath.orElse(null)));
    return readmePath;
  }

  private Optional<String> findReadmePath(RepositoryService repositoryService, String revision, String path) {
    return browse(repositoryService, revision, path)
      .map(BrowserResult::getFile)
      .flatMap(fo -> fo.getChildren()
        .stream()
        .filter(fileObject -> !fileObject.isDirectory())
        .filter(fileObject -> README_FILES.contains(fileObject.getName().toLowerCase()))
        .map(FileObject::getPath)
        .findFirst());
  }

  private Optional<BrowserResult> browse(RepositoryService repositoryService, String revision, String path) {
    try {
      return ofNullable(repositoryService.getBrowseCommand()
        .setDisableCache(true)
        .setDisableLastCommit(true)
        .setPath(path)
        .setRevision(revision)
        .getBrowserResult());
    } catch (IOException e) {
      log.error("error on getting browsing repository {}", repositoryService.getRepository().getNamespaceAndName(), e);
      return empty();
    }
  }

  @EqualsAndHashCode
  private static class CachedResult {
    private final String path;

    public CachedResult(String path) {
      this.path = path;
    }
  }
}
