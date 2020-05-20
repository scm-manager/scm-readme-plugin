package com.cloudogu.scm.readme;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Readme {
  private final String branch;
  private final String content;
}
