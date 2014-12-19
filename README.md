# OpenSymphony workflow

Revamped version of the OpenSymphony workflow project. Built using a combination of Jcenter Mirror and
uploaded jar.

[ ![Download](https://api.bintray.com/packages/trunkplatform/osworkflow/osworkflow/images/download.svg) ](https://bintray.com/trunkplatform/osworkflow/osworkflow/_latestVersion)

## Build

Update your $USERHOME/.gradle/gradle.properties with your Bintray username and API key:

    bintrayUser=
    bintrayApiKey=



## Features

Removed features:

 * EJB
 * SOAP
 * Ofbiz

## Dependencies

Certain guesses were made in regards to the versions of certain dependencies which were available in JCenter:

 * opensymphony propertyset:1.5
 * ostermiller-syntax:1.1.1
 * opensymphony: 2.1.7
 * jdbc: 2.0
 * stax: 1.2.0

Removed transitive/unused dependencies:

 * com.sun.mail
 * aelfred
 * activation 1.1


Other jars which we are not sure of have been uploaded to [bintray](https://bintray.com/trunkplatform/osworkflow)



