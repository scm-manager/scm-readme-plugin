import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { SecondaryNavigationItem } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  url: string;
  activeWhenMatch: (route: any) => boolean;
  collapsed?: boolean;
};

class ReadmeNavLink extends React.Component<Props> {
  render() {
    const { url, activeWhenMatch, t } = this.props;

    return (
      <SecondaryNavigationItem
        to={`${url}/readme`}
        icon="fas fa-book-reader"
        label={t("scm-readme-plugin.navLink")}
        activeWhenMatch={activeWhenMatch}
        title={t("scm-readme-plugin.navLink")}
      />
    );
  }
}

export default withTranslation("plugins")(ReadmeNavLink);
