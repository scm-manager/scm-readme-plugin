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
import { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-core";
import { MarkdownView } from "@scm-manager/ui-components";
import { RepositoryRevisionContextProvider, useRepositoryRevisionContext } from "@scm-manager/ui-api";
import { useReadme } from "./api";
import ReadmeBreadcrumb from "./ReadmeBreadcrumb";

type Props = {
  sources: File;
  repository: Repository;
};

const ReadmeComponent: FC<Props> = ({ sources, repository }) => {
  const { isLoading, error, data: readme } = useReadme({ sources, repository });
  let revision = useRepositoryRevisionContext()

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  if (!readme) {
    return null;
  }

  if (!revision) {
    revision = readme.revision
  }

  return (
    <div className="panel" id="readme">
      <ReadmeBreadcrumb path={readme.path} />
      <hr className="m-0" />
      <div className="panel-block">
        <RepositoryRevisionContextProvider revision={revision}>
          <MarkdownView
            basePath={`/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/${sources.path}`}
            content={readme.content}
            enableAnchorHeadings={true}
            permalink={`/repo/${repository.namespace}/${repository.name}/code/sources/${revision}/${sources.path}`}
          />
        </RepositoryRevisionContextProvider>
      </div>
    </div>
  );
};

export default ReadmeComponent;
