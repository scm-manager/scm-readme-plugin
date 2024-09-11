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

import { Link, File, Repository } from "@scm-manager/ui-types";
import { ApiResult, apiClient } from "@scm-manager/ui-api";
import { useQuery, useQueryClient } from "react-query";

export type Readme = {
  revision: string;
  content: string;
  path: string;
};

export function getReadme(link: string): Promise<Readme> {
  return apiClient.get(link).then(resp => resp.json());
}

export function useReadme({ sources, repository }: { sources: File; repository: Repository }): ApiResult<Readme> {
  const queryClient = useQueryClient();
  const link = (sources._links.readme as Link).href;

  return useQuery<Readme, Error>(
    ["repository", repository.name, repository.namespace, "sources", sources.revision, sources.name, sources.path],
    () => apiClient.get(link).then(resp => resp.json()),
    {
      onSuccess: (readme: Readme) => {
        queryClient.setQueryData(
          [
            "repository",
            repository.name,
            repository.namespace,
            "sources",
            sources.revision,
            sources.name,
            sources.path
          ],
          readme
        );
      }
    }
  );
}
