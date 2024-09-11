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
import styled from "styled-components";
import { Icon } from "@scm-manager/ui-core";

const PathComponent = styled.span`
  margin-left: 0.25rem;
`;

type Props = {
  path: string;
};

const ReadmeBreadcrumb: FC<Props> = ({ path }) => {
  const pathComponents = path.split("/");
  return (
    <div className="is-flex is-flex-row mx-2 my-4 is-ellipsis-overflow">
      <Icon>book-reader</Icon>
      <ul className="is-flex is-flex-row">
        {pathComponents.map((value, index) => (
          <li key={`${value}-${index}`}>
            <PathComponent aria-hidden={true}>/</PathComponent>
            <PathComponent>{value}</PathComponent>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default ReadmeBreadcrumb;
