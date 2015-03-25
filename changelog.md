##Code Dx Jenkins Plugin 1.3.0-RC1 `Released 3/25/2015`

###Changes
- Previously, if a build failed due to an error in Code Dx, only a generic error message (and a stack trace) was displayed in the console. More specific messages based on the http response code were added to make it easier for users to determine the cause of failure.
- The ordering of the entries in the trend charts were changed to order items in a sequential manner for severities and progressively for statuses.
- Improved the wording of configuration related error messages.
- Improved the adopted colors for the trend charts

###Fixes
- Fixed an issue causing a null pointer exception for projects with false positive weaknesses
- Fixed an issue that would sometimes cause gaps to appear in the trend charts

###RC History
####RC1 `3/25/15`

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
