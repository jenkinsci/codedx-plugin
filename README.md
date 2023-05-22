# Code Dx Jenkins Plugin

Allows Jenkins to push source and build artifacts to [Code
Dx](https://www.synopsys.com/software-integrity/code-dx.html) and display the aggregated results of its [full
suite](https://community.synopsys.com/s/document-item?bundleId=codedx&topicId=user_guide%2FAnalysis%2Fimporting-scan-results.html&_LANG=enus)
of analysis tools.

#### Notes

- This plugin is maintained by Synopsys, Inc.
- Plugin version 3.0.0 and later support Pipelines projects as well as Freestyle
- **Plugin version 4.0.0 includes fixes for [CVE-2023-2195, CVE-2023-2631](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3118), [CVE-2023-2633, CVE-2023-2632](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3146), [CVE-2023-2196](https://www.jenkins.io/security/advisory/2023-05-16/#SECURITY-3145)**
  - This is a breaking change - see [changelog.md](changelog.md#code-dx-jenkins-plugin-400-released-5102023) for more info and guidance

## Features

-   Upload your source, binaries, and/or scan file(s) from your Jenkins
    jobs to your Code Dx installation easily
-   Scan your source and binaries for potential vulnerabilities and
    quality issues using Code Dx
-   Code Dx is an application vulnerability correlation and management
    system that supports C/C++, C\#, Java/JSP, Javascript, PHP, Python,
    Ruby on Rails, Scala, and Visual Basic .NET
-   See Code Dx finding trends right in Jenkins
-   Prevent serious issues from slipping through the cracks by
    configuring Jenkins to fail your builds with customizable Code Dx
    findings criteria

![](doc/results-tables.png)

![](doc/trend-graphs.png)

## Requirements

Jenkins 2.200 or later, and a [Code Dx server](https://www.synopsys.com/software-integrity/code-dx.html) deployment with an API Key
created from the Code Dx admin page.

## Documentation

Consult the latest [Code Dx online
documentation](https://community.synopsys.com/s/document-item?bundleId=codedx&topicId=plugins_guide%2FJenkins%2Foverview.html&_LANG=enus) for
instructions on how to configure your build.

[Changelog](./changelog.md)

## Contact

[Visit the Synopsys Community Forums for support.](https://community.synopsys.com/s/)
