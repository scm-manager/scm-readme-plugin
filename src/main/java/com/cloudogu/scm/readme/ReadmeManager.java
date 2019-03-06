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
import java.util.Optional;

@Slf4j
public class ReadmeManager {

  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public ReadmeManager(RepositoryServiceFactory serviceFactory) {
    this.serviceFactory = serviceFactory;
  }


  String getReadmeContent(String namespace, String name) {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Repository repository = repositoryService.getRepository();
      RepositoryPermissions.read(repository).check();
      BrowserResult browserResult = repositoryService.getBrowseCommand()
        .setPath("/")
        .setDisableCache(true)
        .getBrowserResult();
      if (browserResult != null) {
        Optional<String> readmePath = browserResult.getFile().getChildren()
          .stream()
          .map(FileObject::getName)
          .filter(fileName -> Arrays.asList("readme.md", "readme.txt", "readme", "readme.markdown").contains(fileName.toLowerCase()))
          .findFirst();
        if (readmePath.isPresent()) {
          return repositoryService.getCatCommand().getContent(readmePath.get());
        }
      }
    } catch (IOException e) {
      log.error("There is an error while getting the content of the readme file", e);
    }
    return null;
  }

}
