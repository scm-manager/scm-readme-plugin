import { hri } from "human-readable-ids";
import { GitBuilder } from "./__helpers/git-builder";

describe("Render Readme", () => {
  let username: string;
  let password: string;
  let namespace: string;
  let name: string;
  let gitBuilder: GitBuilder;

  beforeEach(() => {
    username = hri.random();
    password = hri.random();
    cy.restCreateUser(username, password);
    cy.restLogin(username, password);

    namespace = hri.random();
    name = hri.random();
    cy.restCreateRepo("git", namespace, name, false);
    cy.restSetUserRepositoryRole(username, namespace, name, "WRITE");
    gitBuilder = new GitBuilder(namespace, name);
    gitBuilder.init().pushAllWithForce();
  });

  it("should not render readme file, since root is empty", () => {
    cy.visit(`/repo/${namespace}/${name}/code/sources/main/`);

    cy.get("#readme").should("not.exist");
    cy.get(".menu-list > li").should("not.contain", "Readme");
  });

  it("should render readme file in root directory", () => {
    gitBuilder.createAndCommitFile("README.md", "# README main /", "init commit").pushAllWithForce();
    cy.visit(`/repo/${namespace}/${name}/code/sources/main/`);

    cy.get("#readme")
      .should("exist")
      .contains("README main /");

    cy.get(".menu-list > li")
      .contains("Readme")
      .should("exist")
      .click();

    cy.url().should("include", `/repo/${namespace}/${name}/code/sources/main/#readme`);
  });

  it("should render readme file in subdirectory", () => {
    gitBuilder.createAndCommitFile("README.md", "# README main /", "init commit").pushAllWithForce();
    gitBuilder.createAndCommitFile("sub/README.md", "# README main /sub", "init commit").pushAllWithForce();
    cy.visit(`/repo/${namespace}/${name}/code/sources/main/sub`);

    cy.get("#readme")
      .should("exist")
      .contains("README main /sub");

    cy.get(".menu-list > li")
      .contains("Readme")
      .should("exist")
      .click();
  });

  it("should not render readme in subdirectory, because it does not contain readme", () => {
    gitBuilder.createAndCommitFile("README.md", "# README main /", "init commit").pushAllWithForce();
    gitBuilder.createAndCommitFile("sub/.scmkeep", "", "init commit").pushAllWithForce();
    cy.visit(`/repo/${namespace}/${name}/code/sources/main/sub`);

    cy.get("#readme").should("not.exist");

    cy.get(".menu-list > li")
      .contains("Readme")
      .should("exist")
      .click();
  });

  it("should render readme file of different branch", () => {
    gitBuilder.createAndCommitFile("README.md", "# README main /", "init commit").pushAllWithForce();
    gitBuilder
      .createAndCheckoutBranch("develop")
      .createAndCommitFile("README.md", "# README develop /", "init commit")
      .pushAllWithForce();

    cy.visit(`/repo/${namespace}/${name}/code/sources/develop`);

    cy.get("#readme")
      .should("exist")
      .contains("README develop /");

    cy.get(".menu-list > li")
      .contains("Readme")
      .should("exist")
      .click();
  });
});
