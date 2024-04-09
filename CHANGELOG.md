# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.3.0 - 2024-04-09
### Added
- API to fetch the readme file of a certain revision and path

### Fixed
- The permalinks to the titles of a rendered readme are now correct
- The link to the readme of a repository is now a redirect to the code overview with an anchor tag

### Changed
- README files are now rendered below the code overview in the code section

## 2.2.0 - 2024-01-17
### Changed
- In the readme view its now possible to click on and save permalinks
- The plugin doesn't redirect to the readme page anymore upon loading a repository

## 2.1.0 - 2023-01-23
### Changed
- Render local images from repository correctly

## 2.0.3 - 2022-09-29
### Fixed
- Catch errors for repository link enricher to prevent breaking the repository overview ([#42](https://github.com/scm-manager/scm-readme-plugin/pull/42))

## 2.0.2 - 2022-01-05
### Fixed
- Skip directories matching readme filename pattern ([#29](https://github.com/scm-manager/scm-readme-plugin/pull/29))

## 2.0.1 - 2020-12-21
### Fixed
- Correct matching of readme navigation entry ([#23](https://github.com/scm-manager/scm-readme-plugin/pull/23))

## 2.0.0 - 2020-06-04
### Fixed
- Fixed usage of relative links in readme ([#5](https://github.com/scm-manager/scm-readme-plugin/pull/5))

### Changed
- Changeover to MIT license ([#4](https://github.com/scm-manager/scm-readme-plugin/pull/4))
- Rebuild for api changes from core

## 2.0.0-rc3 - 2020-03-13
### Added
- Add swagger rest annotations to generate openAPI specs for the scm-openapi-plugin. ([#2](https://github.com/scm-manager/scm-readme-plugin/pull/2))
- Make navigation item collapsable ([#3](https://github.com/scm-manager/scm-readme-plugin/pull/3))

## 2.0.0-rc2 - 2020-01-29
### Changed
- Improve performance ([#1](https://github.com/scm-manager/scm-readme-plugin/pull/1))

