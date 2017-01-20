# How to use this

### Detailed documentation
https://docs.google.com/document/d/1XHChN_zRxwnby6BSoz1QIlY9HJ-Kd5z1enWoT23A7DM/edit?usp=sharing

### Add following in project pom file:

```
 <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>exec-maven-plugin</artifactId>
      <version>1.4.0</version>
      <executions>
          <execution>
              <id>build-features-from-testrail</id>
              <phase>compile</phase>
              <goals>
                  <goal>java</goal>
              </goals>
              <configuration>
                  <includeProjectDependencies>true</includeProjectDependencies>
                  <executableDependency>
                      <groupId>edu.hbs.qa</groupId>
                      <artifactId>testrail_cucumber_testrunner</artifactId>
                  </executableDependency>
                  <mainClass>edu.hbs.qa.testrailrunner.BuildFeature</mainClass>
              </configuration>
          </execution>
          <execution>
              <id>post-results-to-testrail</id>
              <phase>verify</phase>
              <goals>
                  <goal>java</goal>
              </goals>
              <configuration>
                  <includeProjectDependencies>true</includeProjectDependencies>
                  <executableDependency>
                      <groupId>edu.hbs.qa</groupId>
                      <artifactId>testrail_cucumber_testrunner</artifactId>
                  </executableDependency>
                  <mainClass>edu.hbs.qa.testrailrunner.PostResults</mainClass>
              </configuration>
          </execution>
      </executions>
  </plugin>
```
