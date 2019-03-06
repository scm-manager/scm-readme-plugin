// @flow
import React from "react";
import { binder } from "@scm-manager/ui-extensions";
import ReadmeNavLink from "./ReadmeNavLink";
import type {Repository} from "@scm-manager/ui-types";
import {Route} from "react-router-dom";

const predicate = (props: Object) => {
  return props.repository && props.repository._links.readme;
};

function matches(route: any) {
  const regex = new RegExp(".*(/readme)");
  return (
    route.location.pathname.match(regex)
  );
}

const ReadmeNavigationLink = ({ url }) => {
  return <ReadmeNavLink url={url} activeWhenMatch={matches} />;
};

binder.bind(
  "repository.navigation.topLevel",
  ReadmeNavigationLink,
  predicate
);

type Props = {
  url: string,
  repository: Repository
};

class ReadmeRoute extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
  }

  renderReadme = () => {
    const { url } = this.props;
    return <div > Hallo {url}</div>;
  };

  render() {
    const { url } = this.props;
    return (
      <Route
        path={`${url}/readme`}
        render={this.renderReadme}
      />
    );
  }
}

binder.bind("repository.route", ReadmeRoute);
