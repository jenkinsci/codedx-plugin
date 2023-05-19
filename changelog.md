## Code Dx Jenkins Plugin 4.0.0 `Released 5/10/2023`

### Changes
- The "API Key" field now uses a `credential` instead of a `string` value
- The "API Key" field ID (`key`) has been renamed to `keyCredentialId`
- When configuring Freestyle projects, "Configure" permission on the project is required to make changes
- When using the Pipeline Syntax Generator tool, the global "Administer" permission is required to validate the form and list projects
  - It can still be used without these permissions, but the "Code Dx Project" field will need to be filled manually since the drop-down will remain empty

### Notice

This release was performed in cooperation with the Jenkins team and is a response to several reported vulnerabilities:

- [CSRF vulnerability and missing permission checks in Code Dx Plugin](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3118)
- [Missing permission checks in Code Dx Plugin](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3145)
- [API keys stored and displayed in plain text by Code Dx Plugin](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3146)

It is a breaking change and will require reconfiguring Pipeline and Freestyle Projects.

#### Freestyle Projects

API Keys must be stored in Jenkins Credentials as "Secret text". Each project must be reconfigured in order to assign an API Key Credential.

Plugin version 4.0.0 has several known bugs when configuring via a form in a Freestyle Project or the Pipeline Syntax Generator. In particular, when reconfiguring a Freestyle Project, **_the selected project may be reset and should be verified before saving your updated configuration._**

In future releases which fix these bugs, the selected project should still be confirmed before saving your changes. These concerns only apply when upgrading from plugin version 3.x or earlier to version 4.0.0.

#### Pipeline Projects

API Keys must be stored in Jenkins Credentials as "Secret text".

The `key` field has been replaced with `keyCredentialId`. For a configured Pipeline step such as:

```groovy
withCredentials([
  string(credentialsId: 'codedx-api-key', variable: 'API_KEY'),
  string(credentialsId: 'codedx-url', variable: 'CODEDX_URL'),
  ...
]) {
  step([
    $class: 'CodeDxPublisher',
    key: "$API_KEY",
    url: "$CODEDX_URL",
    ...
  ])
}
```

It should be updated as:

```groovy
withCredentials([
  // credential reference removed from `withCredentials`
  string(credentialsId: 'codedx-url', variable: 'CODEDX_URL'),
  ...
]) {
  step([
    $class: 'CodeDxPublisher',
    keyCredentialId: "API_KEY", // field name changed, value replaced with direct credential ID
    url: "$CODEDX_URL",
    ...
  ])
}
```

## Code Dx Jenkins Plugin 3.1.0 `Released 1/26/2023`

### Changes
- Added support for specifying a target branch in Code Dx when analyzing results
- Added support for Git source fetching during the Code Dx analysis
- Added support for checking policy violations when verifying build results
- Added an option to change error handling behavior for internal errors

### Fixes
- "Normal" build errors such as Failure conditions displayed a stack trace in the logs, giving the impression of an internal error instead of expected behavior

## Code Dx Jenkins Plugin 3.0.2 `Released 11/10/2020`

### Changes
- Adds `.zip` extension to source+binary package uploaded to Code Dx

### Fixes
- *(Major)* Fixed a bug causing the plugin to hang when uploading files to Code Dx. This affects users uploading multiple files using the plugin _and_ using a distributed build system (such that the plugin runs on the jenkins master node, but the files being uploaded were on a worker node)
- When "Wait for analysis Results" is enabled with "Build Failure" conditions assigned in a pipeline project, the plugin will now properly fail the job (whereas previously the build _result_ was set to "failure", but the build was not terminated)

## Code Dx Jenkins Plugin 3.0.0 `Released 5/26/2020`

### Changes
- Minimum jenkins version raised to 2.200 (or supported LTS release) as part of update to parent POM 4.2
- Added support for use in Pipelines projects via the `step` metastep

### Fixes
- Fixed graphs skipping the oldest build in its history
- Trend graphs now allow unrecognized finding statuses with a randomly-generated chart color

### Notes
- New plugin is compatible with data from previous versions, but pre-3.x plugin versions will not be able to read from 3.x versions
- If upgrading to 3.0.0 from an old version, graphs for a project will be unavailable (running the build again will fix the issue and allow displaying older data)

##Code Dx Jenkins Plugin 1.4.0 `Released 6/29/2015`

###Additions
- Added support for unverified certificate overrides for Code Dx server connections. You need to configure the Code Dx connection settings for your Jenkins job with the expected SSL certificate fingerprint if you expect that there will be a certificate validation issue (this for instance would happen if you self-sign your SSL certificates).

###Fixes
- Fixed the plugin name and description as it appears in the installed plugins list

###RC History
####RC1 `6/9/15`
####RC2 `6/23/15
- Fixed the certificate fingerprint matching to ignore all non-hex characters
- Fixed the plugin name and description as it appears in the installed plugins list`

####Final `6/29/2015`

##Code Dx Jenkins Plugin 1.3.0 `Released 3/28/2015`

###Changes
- Previously, if a build failed due to an error in Code Dx, only a generic error message (and a stack trace) was displayed in the console. More specific messages based on the http response code were added to make it easier for users to determine the cause of failure.
- The ordering of the entries in the trend charts were changed to order items in a sequential manner for severities and progressively for statuses.
- Improved the wording of configuration related error messages.
- Improved the adopted colors for the trend charts

###Fixes
- Fixed an issue causing a null pointer exception for projects with false positive weaknesses
- Fixed an issue that would sometimes cause gaps to appear in the trend charts
- Fixed incorrect example paths in the built-in help
- Fixed some incorrrect validation for tool output entries

###RC History
####RC1 `3/25/15`
####Final `3/28/15`
- Updated the order of the chart legends to match the order of the new ordering of the chart
- Fixed incorrect example paths in the built-in help
- Fixed some incorrrect validation for tool output entries

##Code Dx Jenkins Plugin 1.2.0 `Released 1/15/2015`

###RC History
####RC1 `11/26/14`
####RC2 `12/22/14`
- Fixed issue where Jenkins plugin wouldn't work outside dev environment.

###Final `1/15/15`
- Fixed a logging issue that was misleading about which files were included for the Code Dx analysis
- Fixed an issue where the source and binary zip was not being deleted after kicking off a Code Dx analysis
- Console failure messages are now more detailed
- Clicking to view the latest analysis in Code Dx will now open a new browser tab
- Fixed some presentation issues with the Code Dx analysis summary table
