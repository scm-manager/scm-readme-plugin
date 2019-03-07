package com.cloudogu.scm.readme;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.api.BrowseCommandBuilder;
import sonia.scm.repository.api.CatCommandBuilder;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(configuration = "classpath:com/cloudogu/scm/readme/shiro.ini")
public class ReadmeManagerTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  @Mock
  RepositoryServiceFactory serviceFactory;

  @InjectMocks
  ReadmeManager readmeManager;

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereNoFiles() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    BrowserResult br = null;
    when(builder.getBrowserResult()).thenReturn(br);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.isPresent()).isFalse();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereIsNoReadmeFiles() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    childFile1.setName("README.haha");
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.isPresent()).isFalse();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeFile() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    String readmeFile = "README";
    childFile1.setName(readmeFile);
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    CatCommandBuilder catCommand = mock(CatCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getCatCommand()).thenReturn(catCommand);
    String content = "content of the readme file";
    when(catCommand.getContent(readmeFile)).thenReturn(content);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.get()).isEqualTo(content);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeTxtFile() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    String readmeFile = "readme.tXt";
    childFile1.setName(readmeFile);
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    CatCommandBuilder catCommand = mock(CatCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getCatCommand()).thenReturn(catCommand);
    String content = "content of the readme file";
    when(catCommand.getContent(readmeFile)).thenReturn(content);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.get()).isEqualTo(content);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMarkdownFile() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    String readmeFile = "ReadMe.markdown";
    childFile1.setName(readmeFile);
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    CatCommandBuilder catCommand = mock(CatCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getCatCommand()).thenReturn(catCommand);
    String content = "content of the readme file";
    when(catCommand.getContent(readmeFile)).thenReturn(content);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.get()).isEqualTo(content);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMDFile() throws IOException {
    RepositoryService service = mock(RepositoryService.class);
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", "space", "name", "", "", p);
    when(service.getRepository()).thenReturn(repository);
    BrowseCommandBuilder builder = mock(BrowseCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getBrowseCommand()).thenReturn(builder);
    when(builder.setPath("/").setDisableCache(true)).thenReturn(builder);
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    String readmeFile = "ReadMe.md";
    childFile1.setName(readmeFile);
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    CatCommandBuilder catCommand = mock(CatCommandBuilder.class, RETURNS_DEEP_STUBS);
    when(service.getCatCommand()).thenReturn(catCommand);
    String content = "content of the readme file";
    when(catCommand.getContent(readmeFile)).thenReturn(content);

    Optional<String> readmeContent = readmeManager.getReadmeContent("space", "name");

    assertThat(readmeContent.get()).isEqualTo(content);
  }

}
