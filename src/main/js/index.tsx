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

const ReadmeNavigationLink: FC<{ url: string }> = ({ url }) => {
  return <ReadmeNavLink url={url} />;
};

binder.bind<extensionPoints.RepositoryNavigationTopLevel>(
  "repository.navigation.topLevel",
  ReadmeNavigationLink,
  isReadmeAvailable
);

binder.bind<extensionPoints.RepositoryCodeOverviewContent>(
  "repository.code.overview.content",
  ReadmeComponent,
  containsReadme
);
