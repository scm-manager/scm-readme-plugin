import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Repository, Link } from "@scm-manager/ui-types";
import { MarkdownView } from "@scm-manager/ui-components";
import { getReadme } from "./api";

type Props = WithTranslation & {
  repository: Repository;
};

type State = {
  readmeContent: string;
  loading?: boolean;
  error?: Error;
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
    if (repository._links.readme) {
      const link = repository._links.readme as Link;
      getReadme(link.href)
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
    return <MarkdownView content={readmeContent} enableAnchorHeadings={true} />;
  }
}

export default withTranslation("plugin")(ReadmeComponent);
