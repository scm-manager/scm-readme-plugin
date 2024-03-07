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
import { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-core";
import { MarkdownView } from "@scm-manager/ui-components";
import { RepositoryRevisionContextProvider } from "@scm-manager/ui-api";
import { useReadme } from "./api";
import ReadmeBreadcrumb from "./ReadmeBreadcrumb";

type Props = {
  sources: File;
  repository: Repository;
};

const ReadmeComponent: FC<Props> = ({ sources, repository }) => {
  const { isLoading, error, data: readme } = useReadme({ sources, repository });

  if (error) {
    return <ErrorNotification error={error} />;
  }

  if (isLoading) {
    return <Loading />;
  }

  if (!readme) {
    return null;
  }

  return (
    <div className="panel" id="readme">
      <ReadmeBreadcrumb path={readme.path} />
      <hr className="m-0" />
      <div className="panel-block">
        <RepositoryRevisionContextProvider revision={readme.revision}>
          <MarkdownView
            content={readme.content}
            enableAnchorHeadings={true}
            permalink={`/repo/${repository.namespace}/${repository.name}/code/sources/${sources.revision}/${sources.path}`}
          />
        </RepositoryRevisionContextProvider>
      </div>
    </div>
  );
};

export default ReadmeComponent;
