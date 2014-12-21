# OpenSymphony workflow

Revamped version of the OpenSymphony workflow project based on the 3.0 code base. Built using a combination of Jcenter Mirror and
uploaded jar.

<a href='https://bintray.com/trunkplatform/osworkflow/osworkflow/view?source=watch' alt='Get automatic notifications about new "osworkflow" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>
[ ![Download](https://api.bintray.com/packages/trunkplatform/osworkflow/osworkflow/images/download.svg) ](https://bintray.com/trunkplatform/osworkflow/osworkflow/_latestVersion)

## Usage

### Maven

    <repositories>
        <repository>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
            <id>central</id>
            <name>bintray</name>
            <url>http://dl.bintray.com/trunkplatform/osworkflow</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>osworkflow</groupId>
            <artifactId>osworkflow</artifactId>
            <version>2.7.1</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>

### Gradle

## Build

Update your $USERHOME/.gradle/gradle.properties with your Bintray username and API key:

    bintrayUser=
    bintrayApiKey=

Run gradle:

    ./gradlew build

Upload a new version:

    ./gradlew bintrayUpload

## Features

To ease maintenance, removed support for the following workflow features:

 * EJB
 * SOAP
 * Ofbiz

## Library Dependencies

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



