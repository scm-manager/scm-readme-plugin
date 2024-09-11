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

export class GitBuilder {
  private readonly directory = `cypress/fixtures/generated/${this.namespace}-${this.name}`;
  private readonly gitDirectory = `./${this.directory}`;

  constructor(private readonly namespace: string, private readonly name: string, readonly defaultBranch = "main") {}

  init() {
    const [protocol, url] = Cypress.config("baseUrl").split("//");
    const credentials = `${Cypress.env("USERNAME")}:${Cypress.env("PASSWORD")}`;
    const urlWithCredentials = `${protocol}//${credentials}@${url}`;
    cy.exec(`git init -b ${this.defaultBranch} ./${this.gitDirectory}`);
    cy.exec(
      `git -C ${this.gitDirectory} remote add origin "${urlWithCredentials}/repo/${this.namespace}/${this.name}"`
    );
    return this;
  }

  createAndCommitFile(path: string, content: string, commitMessage: string) {
    cy.writeFile(`${this.directory}/${path}`, content);
    cy.exec(`git -C ${this.gitDirectory} add ${path}`);
    cy.exec(`git -C ${this.gitDirectory} commit -m "${commitMessage}"`);
    return this;
  }

  createAndCheckoutBranch(branchName: string) {
    cy.exec(`git -C ${this.gitDirectory} checkout -b "${branchName}"`);
    return this;
  }

  pushAllWithForce() {
    cy.exec(`git -C ${this.gitDirectory} push --all --force origin`);
    return this;
  }
}
