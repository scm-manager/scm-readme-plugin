// @flow
import React from "react";
import { translate } from "react-i18next";
import { NavLink } from "@scm-manager/ui-components";

type Props = {
  url: string,
  activeWhenMatch: (route: any) => boolean,
  t: string => string
};

class ReadmeNavLink extends React.Component<Props> {
  render() {
    const { url, activeWhenMatch, t } = this.props;

    return (
      <NavLink
        to={`${url}/readme`}
        icon="fas fa-book-reader"
        label={t("scm-readme-plugin.navLink")}
        activeWhenMatch={activeWhenMatch}
      />
    );
  }
}

export default translate("plugins")(ReadmeNavLink);
