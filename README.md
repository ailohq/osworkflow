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
            <groupId>com.trunkplatform.opensymphony</groupId>
            <artifactId>osworkflow</artifactId>
            <version>3.1.0</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>

### Gradle

    repositories {
        jcenter()
        maven { url "http://dl.bintray.com/trunkplatform/osworkflow" }
    }
    dependencies {
        compile group: 'com.trunkplatform.opensymphony', name: 'osworkflow', version: '3.1.0'
    }

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
 * SendEmail
 * Spring
 * Hibernate
 * Prevayler
 * JMS

