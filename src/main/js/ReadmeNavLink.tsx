import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { NavLink, MenuContext } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  url: string;
  activeWhenMatch: (route: any) => boolean;
  collapsed?: boolean;
};

class ReadmeNavLink extends React.Component<Props> {
  render() {
    const { url, activeWhenMatch, t } = this.props;

    return (
      <MenuContext.Consumer>
        {({ menuCollapsed }) => (
          <NavLink
            to={`${url}/readme`}
            icon="fas fa-book-reader"
            label={t("scm-readme-plugin.navLink")}
            activeWhenMatch={activeWhenMatch}
            title={t("scm-readme-plugin.navLink")}
            collapsed={menuCollapsed}
          />
        )}
      </MenuContext.Consumer>
    );
  }
}

export default withTranslation("plugins")(ReadmeNavLink);
