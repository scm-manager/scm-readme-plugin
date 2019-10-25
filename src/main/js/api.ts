import { apiClient } from "@scm-manager/ui-components";

export function getReadme(link: string) {
  return apiClient.get(link).then(resp => resp.text());
}
