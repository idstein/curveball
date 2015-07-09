#!/usr/bin/env bash

VERSION="3.0-SNAPSHOT"

function usage {
  cat <<USAGE

Usage: $0 <lwjgl_zip_url>
Arguments:
  lwjgl_zip_url - url pointing to the lwjgl zip archive to download and install in the local m2/repository
USAGE
}
 
function create_parent_pom {
  if [ -f pom.xml ]; then rm -f pom.xml; fi
  touch pom.xml
  tee pom.xml <<PARENT_POM
<project>
  <modelVersion>4.0.0</modelVersion>
  <groupId>org.lwjgl.lwjgl</groupId>
  <artifactId>parent</artifactId>
  <packaging>pom</packaging>
  <name>Lighweight Java Game Library</name>
  <version>$VERSION</version>
  <description>Lightweight Java Game Library</description>
  <url>http://www.lwjgl.org/</url>
  <licenses>
    <license>
      <name>BSD</name>
      <url>http://www.lwjgl.org/license</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://github.com/LWJGL/lwjgl3</url>
    <connection>https://github.com/LWJGL/lwjgl3.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Developers</id>
      <url>https://github.com/LWJGL/lwjgl3/graphs/contributors</url>
    </developer>
  </developers>
  <modules>
    <module>lwjgl</module>
    <module>lwjgl-platform</module>
  </modules>
</project>

PARENT_POM
}
 
function create_project_pom {
  if [ -f lwjgl/pom.xml ]; then rm -f lwjgl/pom.xml; fi
  touch lwjgl/pom.xml
  tee lwjgl/pom.xml <<PROJECT_POM
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.lwjgl.lwjgl</groupId>
        <artifactId>parent</artifactId>
        <version>$VERSION</version>
    </parent>
    <artifactId>lwjgl</artifactId>
    <packaging>pom</packaging>
    <name>Lighweight Java Game Library</name>
    <description>Lightweight Java Game Library</description>

    <dependencies>
        <dependency>
            <groupId>\${project.groupId}</groupId>
            <artifactId>lwjgl-platform</artifactId>
            <version>\${project.version}</version>
            <classifier>natives-windows</classifier>
        </dependency>
        <dependency>
            <groupId>\${project.groupId}</groupId>
            <artifactId>lwjgl-platform</artifactId>
            <version>\${project.version}</version>
            <classifier>natives-linux</classifier>
        </dependency>
        <dependency>
            <groupId>\${project.groupId}</groupId>
            <artifactId>lwjgl-platform</artifactId>
            <version>\${project.version}</version>
            <classifier>natives-osx</classifier>
        </dependency>       
    </dependencies>

    <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-install-plugin</artifactId>
              <version>2.5.1</version>              
              <executions>
                  <execution>
                      <id>default-install</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <file>\${project.basedir}/target/lwjgl-\${project.version}.jar</file>
                      </configuration>
                  </execution>
                  <execution>
                      <id>install-sources</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <classifier>sources</classifier>
                          <file>\${project.basedir}/target/lwjgl-\${project.version}-sources.jar</file>
                      </configuration>
                  </execution>
                  <execution>
                      <id>install-javadoc</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <classifier>javadoc</classifier>
                          <file>\${project.basedir}/target/lwjgl-\${project.version}-javadoc.jar</file>
                      </configuration>
                  </execution>                  
              </executions>
          </plugin>
        </plugins>
    </build>
</project>

PROJECT_POM
}
 
function create_platorm_pom {
  if [ -f lwjgl-platform/pom.xml ]; then rm -f lwjgl-platform/pom.xml; fi
  touch lwjgl-platform/pom.xml
  tee lwjgl-platform/pom.xml <<PLATFORM_POM
<project>
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.lwjgl.lwjgl</groupId>
        <artifactId>parent</artifactId>
        <version>$VERSION</version>
    </parent>
    <artifactId>lwjgl-platform</artifactId>
    <packaging>pom</packaging>
    <name>Lightweight Java Game Library - Platform</name>
    <description>Lighweight Java Game Library - Platform</description>
    <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-install-plugin</artifactId>
              <version>2.5.1</version>              
              <executions>
                  <execution>
                      <id>install-natives-linux</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl-platform</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <classifier>natives-linux</classifier>
                           <file>\${project.basedir}/target/lwjgl-platform-\${project.version}-natives-linux.jar</file>
                      </configuration>
                  </execution>
                  <execution>
                      <id>install-natives-osx</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl-platform</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <classifier>natives-osx</classifier>
                          <file>\${project.basedir}/target/lwjgl-platform-\${project.version}-natives-osx.jar</file>
                      </configuration>
                  </execution>
                  <execution>
                      <id>default-install</id>
                      <phase>install</phase>
                      <goals>
                          <goal>install-file</goal>
                      </goals>                      
                      <configuration>
                          <groupId>org.lwjgl.lwjgl</groupId>
                          <artifactId>lwjgl-platform</artifactId>
                          <version>\${project.version}</version>
                          <packaging>jar</packaging>
                          <classifier>natives-windows</classifier>
                          <file>\${project.basedir}/target/lwjgl-platform-\${project.version}-natives-windows.jar</file>
                      </configuration>
                  </execution>
              </executions>
          </plugin>
        </plugins>
    </build>
</project>

PLATFORM_POM
}
 
function create_lwjgl {
  mkdir lwjgl 
  mkdir lwjgl/target
 
  create_project_pom $1
 
  mv jar/lwjgl.jar lwjgl/target/lwjgl-${VERSION}.jar
  mv doc/javadoc.zip lwjgl/target/lwjgl-${VERSION}-javadoc.jar
  mv src.zip lwjgl/target/lwjgl-${VERSION}-sources.jar
}
 
function create_lwjgl_platform {
  mkdir lwjgl-platform;
  mkdir lwjgl-platform/target
  mkdir lwjgl-platform/target/osx
  mkdir lwjgl-platform/target/windows
  mkdir lwjgl-platform/target/linux
 
  create_platorm_pom
 
  mv native/*.dll lwjgl-platform/target/windows/
  mv native/*.dylib lwjgl-platform/target/osx
  mv native/*.so lwjgl-platform/target/linux


 
  pushd lwjgl-platform/target/linux/
  zip -m ../lwjgl-platform-${VERSION}-natives-linux.jar ./*
  popd
  pushd lwjgl-platform/target/osx/
  zip -m ../lwjgl-platform-${VERSION}-natives-osx.jar ./*
  popd    
  pushd lwjgl-platform/target/windows/
  zip -m ../lwjgl-platform-${VERSION}-natives-windows.jar ./*
  popd
 
  rmdir lwjgl-platform/target/linux/
  rmdir lwjgl-platform/target/osx/
  rmdir lwjgl-platform/target/windows/
}
 
if [ "$#" -ne 1 ]; then echo "Must provide 1 arguments"; usage; exit 1; fi
 
mkdir mvn_install_lwjgl_temp
pushd mvn_install_lwjgl_temp
 
curl $1 -o lwjgl.zip || { echo "wget call failed, exiting."; exit 1; }
if [ ! -f lwjgl.zip ]; then echo "Download failed, could not find file: lwjgl.zip"; exit 1; fi
 
unzip -o lwjgl.zip || { echo "unzip call failed, exiting."; exit 1; }
 
create_parent_pom
create_lwjgl
create_lwjgl_platform
 
mvn install -DskipTests -DskipJar
 
popd
rm -rf mvn_install_lwjgl_temp