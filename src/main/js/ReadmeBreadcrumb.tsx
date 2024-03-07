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
