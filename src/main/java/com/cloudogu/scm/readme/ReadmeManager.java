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

@Slf4j
public class ReadmeManager {

  public static final List<String> README_FILES = Arrays.asList("readme.md", "readme.txt", "readme", "readme.markdown");
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ReadmeManager(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }

  Optional<String> getReadmeContent(String namespace, String name) {
    String content = null;
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      Optional<String> readmePath = getReadmePath(repositoryService);
      if (readmePath.isPresent()) {
       content = repositoryService.getCatCommand().getContent(readmePath.get());
      }
    } catch (IOException e) {
      log.error("There is an error while getting the content of the readme file", e);
    }
    return Optional.ofNullable(content);
  }

  public Optional<String> getReadmePath(RepositoryService repositoryService) throws IOException {
    Optional<String> readmePath = Optional.empty();
    BrowserResult browserResult = repositoryService.getBrowseCommand()
      .setPath("/")
      .setDisableCache(false)
      .getBrowserResult();
    if (browserResult != null) {
      readmePath = browserResult.getFile().getChildren()
        .stream()
        .map(FileObject::getName)
        .filter(fileName -> README_FILES.contains(fileName.toLowerCase()))
        .findFirst();
    }
    return readmePath;
  }

}
