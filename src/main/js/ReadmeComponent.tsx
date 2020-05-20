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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository, Link } from "@scm-manager/ui-types";
import { MarkdownView, Loading, ErrorNotification } from "@scm-manager/ui-components";
import { getReadme, Readme } from "./api";

type Props = WithTranslation & {
  repository: Repository;
};

type State = {
  readme?: Readme;
  loading?: boolean;
  error?: Error;
};

class ReadmeComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: true
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    if (repository._links.readme) {
      const link = repository._links.readme as Link;
      getReadme(link.href)
        .then(readme => {
          this.setState({
            loading: false,
            readme
          });
        })
        .catch(error => {
          this.setState({
            loading: false,
            error
          });
        });
    }
  }

  render() {
    const { repository } = this.props;
    const { loading, error, readme } = this.state;
    if (error) {
      return <ErrorNotification error={error} />;
    }
    if (loading || !readme) {
      return <Loading />;
    }

    return (
      <MarkdownView
        content={readme.content}
        enableAnchorHeadings={true}
        basePath={`/repo/${repository.namespace}/${repository.name}/code/sources/${readme.revision}`}
      />
    );
  }
}

export default withTranslation("plugin")(ReadmeComponent);
