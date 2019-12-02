package com.cloudogu.scm.readme;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

@Slf4j
public class ReadmeManager {

  private static final List<String> README_FILES = Arrays.asList("readme.md", "readme.txt", "readme", "readme.markdown");
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ReadmeManager(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  Optional<String> getReadmeContent(String namespace, String name) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      return getReadmePath(repositoryService)
        .flatMap(rp -> readFile(repositoryService, rp));
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
        .setPath("/")
        .getBrowserResult());
    } catch (IOException e) {
      log.error("error on getting browsing repository {}", repositoryService.getRepository().getNamespaceAndName(), e);
      return empty();
    }
  }
}
