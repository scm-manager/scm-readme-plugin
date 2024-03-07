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
import React, { FC } from "react";
import { binder, extensionPoints } from "@scm-manager/ui-extensions";
import { Repository, File } from "@scm-manager/ui-types";
import ReadmeNavLink from "./ReadmeNavLink";
import ReadmeComponent from "./ReadmeComponent";

const isReadmeAvailable = ({ repository }: { repository: Repository }) => {
  return repository._links.readme;
};

const containsReadme = ({ sources }: { sources: File }) => {
  return sources._links.readme;
};

function matches(route: any) {
  const regex = new RegExp(".*(/readme)$");
  return !!route.location.pathname.match(regex);
}

const ReadmeNavigationLink: FC<{ url: string }> = ({ url }) => {
  return <ReadmeNavLink url={url} activeWhenMatch={matches} />;
};

binder.bind<extensionPoints.RepositoryNavigationTopLevel>(
  "repository.navigation.topLevel",
  ReadmeNavigationLink,
  isReadmeAvailable
);

binder.bind<extensionPoints.RepositoryCodeOverviewContent>(
  "repository.code.sources.content",
  ReadmeComponent,
  containsReadme
);
