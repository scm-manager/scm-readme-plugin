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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.cache.MapCacheManager;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("squid:S2068") // this hard coded passwords are ok
@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(configuration = "classpath:com/cloudogu/scm/readme/shiro.ini")
public class ReadmeManagerTest {

  private static final String CONTENT_OF_THE_README_FILE = "content of the readme file";
  private static final String NAMESPACE = "space";
  private static final String NAME = "name";
  private static final String README_TXT = "README.txt";
  private static final String README_MD = "README.md";

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  @Mock
  private RepositoryServiceFactory serviceFactory;
  @Mock(answer = Answers.RETURNS_SELF)
  private BrowseCommandBuilder builder;
  @Mock(answer = Answers.RETURNS_SELF)
  private CatCommandBuilder catCommand;
  @Mock
  private RevisionResolver revisionResolver;
  @Mock
  private RepositoryService service;

  private ReadmeManager readmeManager;

  @Before
  public void setUpMocks() {
    when(service.getBrowseCommand()).thenReturn(builder);
    when(service.getCatCommand()).thenReturn(catCommand);
  }

  @Before
  public void setUpObjectUnderTest() {
    readmeManager = new ReadmeManager(serviceFactory, revisionResolver, new MapCacheManager());
    lenient().when(revisionResolver.resolve(service)).thenReturn(Optional.of("develop"));
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereNoFiles() throws IOException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    createRepository();
    when(builder.getBrowserResult()).thenReturn(null);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme.isPresent()).isFalse();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereIsNoReadmeFiles() throws IOException {
    createReadme("README.haha");

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme).isNotPresent();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeFile() throws IOException {
    String readmeFile = "README";
    createReadme(readmeFile);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme.isPresent()).isTrue();
    assertThat(readme.get().getBranch()).isEqualTo("develop");
    assertThat(readme.get().getContent()).isEqualTo(CONTENT_OF_THE_README_FILE);
    assertThat(readme.get().getPath()).isEqualTo("/README");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeTxtFile() throws IOException {
    String readmeFile = "readme.tXt";
    createReadme(readmeFile);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme.isPresent()).isTrue();
    assertThat(readme.get().getBranch()).isEqualTo("develop");
    assertThat(readme.get().getContent()).isEqualTo(CONTENT_OF_THE_README_FILE);
    assertThat(readme.get().getPath()).isEqualTo("/readme.tXt");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMarkdownFile() throws IOException {
    String readmeFile = "ReadMe.markdown";
    createReadme(readmeFile);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme.isPresent()).isTrue();
    assertThat(readme.get().getBranch()).isEqualTo("develop");
    assertThat(readme.get().getContent()).isEqualTo(CONTENT_OF_THE_README_FILE);
    assertThat(readme.get().getPath()).isEqualTo("/ReadMe.markdown");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMDFile() throws IOException {
    String readmeFile = "ReadMe.md";
    createReadme(readmeFile);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);

    assertThat(readme.isPresent()).isTrue();
    assertThat(readme.get().getBranch()).isEqualTo("develop");
    assertThat(readme.get().getContent()).isEqualTo(CONTENT_OF_THE_README_FILE);
    assertThat(readme.get().getPath()).isEqualTo("/ReadMe.md");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldIgnoreDirectory() throws IOException {
    String readmeDirectory = "readme";
    FileObject directory = createReadme(readmeDirectory);
    directory.setDirectory(true);

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);
    assertThat(readme).isNotPresent();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnEmptyObjectWithoutRevision() throws IOException {
    String readmeFile = "ReadMe.md";
    createReadme(readmeFile);

    lenient().when(revisionResolver.resolve(service)).thenReturn(Optional.empty());

    Optional<Readme> readme = readmeManager.getReadme(NAMESPACE, NAME);
    assertThat(readme).isEmpty();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmePathFromCache() throws IOException {
    createReadme(README_TXT);

    Optional<String> readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains("/" + README_TXT);

    createReadme(README_MD);

    readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains("/" + README_TXT);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmePathAfterCacheClear() throws IOException {
    createReadme(README_TXT);

    Optional<String> readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains("/" + README_TXT);

    readmeManager.clearCache(createHookEvent(service.getRepository()));
    createReadme(README_MD);

    readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains("/" + README_MD);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeForSubdir() throws IOException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    createRepository();

    FileObject secondDir = new FileObject();
    secondDir.setName("second");
    secondDir.setPath("/first/second");
    secondDir.setDirectory(true);

    FileObject readmeFile = new FileObject();
    readmeFile.setName("readme.md");
    readmeFile.setPath("/first/second/readme.md");

    secondDir.setChildren(List.of(readmeFile));

    BrowserResult br = new BrowserResult("develop", secondDir);
    when(builder.getBrowserResult()).thenReturn(br);
    when(catCommand.getContent(readmeFile.getPath())).thenReturn(CONTENT_OF_THE_README_FILE);

    Optional<Readme> readme = readmeManager.getReadmeByRevisionAndPath(NAMESPACE, NAME, "develop", "first/second");
    assertThat(readme).isPresent();
    assertThat(readme.get().getBranch()).isEqualTo("develop");
    assertThat(readme.get().getContent()).isEqualTo(CONTENT_OF_THE_README_FILE);
    assertThat(readme.get().getPath()).isEqualTo("/first/second/readme.md");
  }

  private PostReceiveRepositoryHookEvent createHookEvent(Repository repository) {
    PostReceiveRepositoryHookEvent event = mock(PostReceiveRepositoryHookEvent.class);
    when(event.getRepository()).thenReturn(repository);
    return event;
  }

  private FileObject createReadme(String name) throws IOException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    createRepository();
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile = new FileObject();
    childFile.setName(name);
    childFile.setPath("/" + name);
    List<FileObject> children = Lists.newArrayList(childFile);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    when(catCommand.getContent("/" + name)).thenReturn(CONTENT_OF_THE_README_FILE);
    return childFile;
  }

  private void createRepository() {
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", NAMESPACE, NAME, "", "", p);
    when(service.getRepository()).thenReturn(repository);
  }
}
