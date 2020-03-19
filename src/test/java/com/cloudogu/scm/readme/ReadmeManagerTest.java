/**
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
  private RepositoryService service;

  private ReadmeManager readmeManager;

  @Before
  public void setUpMocks() {
    when(service.getBrowseCommand()).thenReturn(builder);
    when(service.getCatCommand()).thenReturn(catCommand);
  }

  @Before
  public void setUpObjectUnderTest() {
    readmeManager = new ReadmeManager(serviceFactory, new MapCacheManager());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereNoFiles() throws IOException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    createRepository();
    BrowserResult br = null;
    when(builder.getBrowserResult()).thenReturn(br);

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);

    assertThat(readmeContent.isPresent()).isFalse();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnNullIfThereIsNoReadmeFiles() throws IOException {
    createReadme("README.haha");

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);

    assertThat(readmeContent).isNotPresent();
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeFile() throws IOException {
    String readmeFile = "README";
    createReadme(readmeFile);

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);
    assertThat(readmeContent).contains(CONTENT_OF_THE_README_FILE);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeTxtFile() throws IOException {
    String readmeFile = "readme.tXt";
    createReadme(readmeFile);

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);
    assertThat(readmeContent).contains(CONTENT_OF_THE_README_FILE);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMarkdownFile() throws IOException {
    String readmeFile = "ReadMe.markdown";
    createReadme(readmeFile);

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);
    assertThat(readmeContent).contains(CONTENT_OF_THE_README_FILE);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmeContentIfThereIsAReadmeMDFile() throws IOException {
    String readmeFile = "ReadMe.md";
    createReadme(readmeFile);

    Optional<String> readmeContent = readmeManager.getReadmeContent(NAMESPACE, NAME);
    assertThat(readmeContent).contains(CONTENT_OF_THE_README_FILE);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmePathFromCache() throws IOException {
    createReadme(README_TXT);

    Optional<String> readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains(README_TXT);

    createReadme(README_MD);

    readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains(README_TXT);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldReturnReadmePathAfterCacheClear() throws IOException {
    createReadme(README_TXT);

    Optional<String> readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains(README_TXT);

    readmeManager.clearCache(createHookEvent(service.getRepository()));
    createReadme(README_MD);

    readmePath = readmeManager.getReadmePath(service);
    assertThat(readmePath).contains(README_MD);
  }

  private PostReceiveRepositoryHookEvent createHookEvent(Repository repository) {
    PostReceiveRepositoryHookEvent event = mock(PostReceiveRepositoryHookEvent.class);
    when(event.getRepository()).thenReturn(repository);
    return event;
  }

  private void createReadme(String name) throws IOException {
    when(serviceFactory.create(any(NamespaceAndName.class))).thenReturn(service);
    createRepository();
    FileObject file = new FileObject();
    file.setPath("/");
    file.setDirectory(true);
    FileObject childFile1 = new FileObject();
    childFile1.setName(name);
    List<FileObject> children = Lists.newArrayList(childFile1);
    file.setChildren(children);
    BrowserResult br = new BrowserResult("rev", file);
    when(builder.getBrowserResult()).thenReturn(br);
    when(catCommand.getContent(name)).thenReturn(CONTENT_OF_THE_README_FILE);
  }

  private void createRepository() {
    RepositoryPermission p = new RepositoryPermission("id", Collections.singleton("read"), false);
    Repository repository = new Repository("id", "git", NAMESPACE, NAME, "", "", p);
    when(service.getRepository()).thenReturn(repository);
  }
}
