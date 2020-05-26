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
