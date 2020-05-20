package com.cloudogu.scm.readme;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
@SuppressWarnings("java:S2160")
public class ReadmeDto extends HalRepresentation {

  private final String revision;
  private final String content;

  public ReadmeDto(Readme readme, String self) {
    super(Links.linkingTo().self(self).build());
    this.revision = readme.getBranch();
    this.content = readme.getContent();
  }
}
