#!/bin/sh

mvn deploy:deploy-file \
  -DgroupId=javax.activation \
  -DartifactId=activation \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=activation.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:activation

mvn deploy:deploy-file \
  -DgroupId=bsf \
  -DartifactId=bsf \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=bsf.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:bsf

mvn deploy:deploy-file \
  -DgroupId=cglib \
  -DartifactId=cglib \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=cglib2.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:cglib

mvn deploy:deploy-file \
  -DgroupId=commons-dbcp \
  -DartifactId=commons-dbcp \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=commons-dbcp.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:commons-dbcp


mvn deploy:deploy-file \
  -DgroupId=commons-logging \
  -DartifactId=commons-logging \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=commons-logging.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:commons-logging

mvn deploy:deploy-file \
  -DgroupId=commons-pool \
  -DartifactId=commons-pool \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=commons-pool.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:commons-pool

mvn deploy:deploy-file \
  -DgroupId=net.sf.ehcache \
  -DartifactId=ehcache \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=ehcache.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:ehcache

mvn deploy:deploy-file \
  -DgroupId=glue_stub \
  -DartifactId=glue_stub \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=glue_stub.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:glue_stub

mvn deploy:deploy-file \
  -DgroupId=javax.net \
  -DartifactId=jnet \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=jnet.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:jnet


mvn deploy:deploy-file \
  -DgroupId=javax.transaction \
  -DartifactId=jta \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=jta.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:jta

mvn deploy:deploy-file \
  -DgroupId=com.sun.mail \
  -DartifactId=mail \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=mail.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:mail

mvn deploy:deploy-file \
  -DgroupId=org.ofbiz.ofbcore \
  -DartifactId=ofbcore-entity \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=ofbcore-entity.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:ofbcore-entity

mvn deploy:deploy-file \
  -DgroupId=org.ofbiz.ofbcore \
  -DartifactId=ofbcore-share \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=ofbcore-share.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:ofbcore-share

mvn deploy:deploy-file \
  -DgroupId=opensymphony \
  -DartifactId=propertyset \
  -Dversion=1.4 \
  -Dpackaging=jar \
  -Dfile=propertyset-1.4.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:propertyset

mvn deploy:deploy-file \
  -DgroupId=simple-jndi \
  -DartifactId=simple-jndi \
  -Dversion=unknown \
  -Dpackaging=jar \
  -Dfile=simple-jndi.jar \
  -DrepositoryId=bintray-trunkplatform \
  -Durl=https://api.bintray.com/maven/trunkplatform/osworkflow/osworkflow:simple-jndi
