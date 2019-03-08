//@flow

import React from "react";
import { translate } from "react-i18next";
import type { Repository } from "@scm-manager/ui-types";
import { MarkdownView } from "@scm-manager/ui-components";
import { getReadme } from "./api";

type Props = {
  repository: Repository,

  // context props
  t: string => string
};

type State = {
  readmeContent: string,
  loading?: boolean,
  error?: Error
};

class ReadmeComponent extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      loading: true,
      readmeContent: ""
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    console.log("readme", repository);
    if (repository._links.readme) {
      getReadme(repository._links.readme.href)
        .then(readmeContent => {
          this.setState({
            loading: false,
            readmeContent
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
    const { readmeContent } = this.state;
    return <MarkdownView content={readmeContent} />;
  }
}

export default translate("plugin")(ReadmeComponent);
