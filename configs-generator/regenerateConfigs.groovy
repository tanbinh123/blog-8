def string = """
This is Groovy config generator
"""

println string


// usage:
// ./mvnw -pl configs-generator generate-resources
// after usage you should do Build -> Rebuild project
// documentation https://github.com/groovy/GMavenPlus/wiki


def BACKEND_MAIN_YML_FILE = "${project.basedir}/../backend/src/main/resources/config/application.yml";
def BACKEND_TEST_YML_FILE = "${project.basedir}/../backend/src/test/resources/config/application.yml";
def INTEGRATION_TEST_YML_FILE = "${project.basedir}/../webdriver-test/src/test/resources/config/application.yml";

class ExportedConstants {
    public static final def PROD_PORT = 8080
    public static final def TEST_PORT = 9080
    public static final def TEST_SMTP_PORT = 3025 // this is greenmail requirement
    public static final def TEST_IMAP_PORT = 3143
    public static final def TEST_EMAIL_USERNAME = "testEmailUsername"
    public static final def TEST_EMAIL_PASSWORD = "testEmailPassword"
    public static final def SCHEME = 'http'
}

def AUTOGENERATE_SNIPPET =
"""# This file was autogenerated via configs-generator
# Please do not edit it manually.
""";

def writeAndLog(filePath, content) {
    def file = new File(filePath);
    file.withWriter('UTF-8') { writer ->
        writer.write(content)
    }
    println("""File ${file.canonicalPath} was successfully saved!""");
};

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////// common snippets //////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

def DATA_STORE_SNIPPET = {boolean dropFirst, String ddlAuto ->
return """
spring.jpa:
  open-in-view: false
  properties:
    hibernate.use_sql_comments: true
    hibernate.format_sql: true
    hibernate.generate_statistics: true
    hibernate.temp.use_jdbc_metadata_defaults: false
  hibernate.ddl-auto: ${ddlAuto}

spring.datasource:
    name: blog_ds
    type: org.apache.tomcat.jdbc.pool.DataSource
    # https://jdbc.postgresql.org/documentation/head/connect.html#connection-parameters
    url: jdbc:postgresql://172.22.0.2:5432/blog?connectTimeout=10&socketTimeout=40
    username: blog
    password: "blogPazZw0rd"
    driverClassName: org.postgresql.Driver
    # https://docs.spring.io/spring-boot/docs/2.0.0.M7/reference/htmlsingle/#boot-features-connect-to-production-database
    # https://tomcat.apache.org/tomcat-8.5-doc/jdbc-pool.html#Common_Attributes
    # https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-connect-to-production-database
    tomcat:
      minIdle: 4
      maxIdle: 8
      maxActive: 10
      maxWait: 60000
      testOnBorrow: true
      testOnConnect: true
      testWhileIdle: true
      timeBetweenEvictionRunsMillis: 5000
      validationQuery: SELECT 1;
      validationQueryTimeout: 4
      logValidationErrors: true

# https://docs.spring.io/spring-boot/docs/2.0.0.M7/reference/htmlsingle/#howto-execute-flyway-database-migrations-on-startup
# https://flywaydb.org/documentation/configfiles
spring.flyway:
  locations: ${dropFirst ? 'classpath:/db/migration, classpath:/db/demo': 'classpath:/db/migration'}
  drop-first: ${dropFirst}
  schemas: migrations, auth, posts, images, settings
  out-of-order: true

spring.redis.url: redis://172.22.0.3:6379/0
spring.data.redis.repositories.enabled: false

spring.data.elasticsearch.cluster-name: elasticsearch
spring.data.elasticsearch.clusterNodes: 172.22.0.5:9300
spring.data.elasticsearch.repositories.enabled: false
spring.data.elasticsearch.properties.client.transport.nodes_sampler_interval: 40s
spring.data.elasticsearch.properties.client.transport.ping_timeout: 40s
"""};

def MANAGEMENT_SNIPPET = { boolean test ->

"""
management.endpoints.web.exposure.include: '*'
management.endpoint.health.show-details: always
management:
  server:
    port: ${test?'3011':'3010'}
    ssl:
      enabled: false
    add-application-context-header: false
"""
}

def WEBSERVER_SNIPPET =
"""
server.tomcat.basedir: \${java.io.tmpdir}/com.github.nkonev.tomcat
server.servlet.session.store-dir: \${server.tomcat.basedir}/sessions
""";

def TEST_USERS_SNIPPET=
"""custom.it.user: admin
custom.it.password: admin
""";

def common = { boolean test ->
"""
custom.base-url: "${ExportedConstants.SCHEME}://localhost:\${server.port}"

# https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-email.html
# https://yandex.ru/support/mail-new/mail-clients.html
# https://stackoverflow.com/questions/411331/using-javamail-with-tls
spring.mail:
  testConnection: false
  host: ${ !test ? "smtp.yandex.ru" : "127.0.0.1"} 
  port: ${ !test ? 465 : ExportedConstants.TEST_SMTP_PORT}
  username: ${ !test ? "username" : ExportedConstants.TEST_EMAIL_USERNAME} 
  password: ${ !test ? "password" : ExportedConstants.TEST_EMAIL_PASSWORD} 
  properties:
    # mail.smtp.starttls.enable: "true"
    ${ (!test ? '' : '# ') + 'mail.smtp.ssl.enable: "true"'}
    mail.smtp.connectiontimeout: 5000
    mail.smtp.timeout: 3000
    mail.smtp.writetimeout: 5000

custom.stomp.broker:
  host: "172.22.0.4"
  port: 61613
  virtual-host: /
  client-login: blog
  client-password: blogPazZw0rd
  system-login: blog
  system-password: blogPazZw0rd

# Postgres image store configuration
custom.image:
  max-bytes: 1048576 # 1 Mb. Must be < than tomcat file upload limit
  allowed-mime-types:
   - image/png
   - image/jpg
   - image/jpeg
  # value in seconds, passed in Cache-Control header
  max-age: 31536000
"""
}

def custom(boolean test) {
    def str = """
custom:
  email:
    from: ${!test ? '"username@yandex.ru"' : ExportedConstants.TEST_EMAIL_USERNAME+'@test.example.com'} 
  registration:
    email:  
      subject: "Registration confirmation"
      text-template: "Please open __REGISTRATION_LINK_PLACEHOLDER__ for complete registration __LOGIN__."
  confirmation:
    registration:
      token:
        ttl-minutes: 5
  password-reset:
    email:
      subject: "Password reset"
      text-template: "Link __PASSWORD_RESET_LINK_PLACEHOLDER__ for reset your password for account __LOGIN__. If you didn't issue password reset -- you can ignore this mail."
    token:
      ttl-minutes: 5
  tasks:
    enable: ${!test}
    poolSize: 10
    defaultLockAtMostForSec: 20
    defaultLockAtLeastForSec: 20
    images.clean:
      cron: "0 * * * * *"
    rendered.cache.refresh:
      cron: "0 */30 * * * *"
    index.refresh:
      cron: "0 0 */2 * * *"
  rendertron:
    serviceUrl: http://rendertron.example.com:3000/
  xss:
    iframe:
      allow:
        src:
          pattern: '^(https://www\\.youtube\\.com.*)|(https://coub\\.com/.*)|(https://player\\.vimeo\\.com.*)\$'

"""
    return str
}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////// config files ///////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

def BACKEND_MAIN_YML_CONTENT =
"""${AUTOGENERATE_SNIPPET}
logging.level.root: INFO
#logging.level.org.springframework.core.env.PropertySourcesPropertyResolver: DEBUG
logging.level.org.springframework.web.socket: WARN
logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener: WARN
#logging.level.org.apache.tomcat.jdbc.pool: TRACE
#logging.level.org.springframework.security: DEBUG
#logging.level.org.springframework.session: DEBUG
#logging.level.org.springframework.security.web: DEBUG
#logging.level.org.apache.catalina: TRACE
#logging.level.org.springframework.web: DEBUG
#logging.level.org.hibernate.SQL: DEBUG
#logging.level.org.hibernate.type: TRACE
${common(false)}
${custom(false)}
server.tomcat.accesslog.enabled: false
server.tomcat.accesslog.pattern: '%t %a "%r" %s (%D ms)'
server.port: ${ExportedConstants.PROD_PORT}
server.servlet.session.persistent: true
${WEBSERVER_SNIPPET}

# this is URL
spring.mvc.static-path-pattern: /**
# You need to remove "file:..." element for production or you can to remove spring.resources.static-locations
# first element - for eliminate manual restart app in IntelliJ for copy compiled js to target/classes, last slash is important,, second element - for documentation
spring.resources.static-locations: file:backend/src/main/resources/static/, classpath:/static/

${DATA_STORE_SNIPPET(false, 'validate')}
${MANAGEMENT_SNIPPET(false)}

facebook:
  client:
    clientId: 1684113965162824
    clientSecret: provide-it
    userAuthorizationUri: https://www.facebook.com/dialog/oauth
    accessTokenUri: https://graph.facebook.com/oauth/access_token
    tokenName: oauth_token
    authenticationScheme: query
    clientAuthenticationScheme: form
  resource:
    userInfoUri: https://graph.facebook.com/me?fields=id,name,picture
""";
writeAndLog(BACKEND_MAIN_YML_FILE, BACKEND_MAIN_YML_CONTENT);


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
def BACKEND_TEST_YML_CONTENT =
"""${AUTOGENERATE_SNIPPET}
logging.level.root: INFO
logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener: WARN
${common(true)}
${custom(true)}
server.port: ${ExportedConstants.TEST_PORT}
${WEBSERVER_SNIPPET}
${TEST_USERS_SNIPPET}
${DATA_STORE_SNIPPET(true, 'validate')}
${MANAGEMENT_SNIPPET(true)}
custom.rendertron.enable: true
""";
writeAndLog(BACKEND_TEST_YML_FILE, BACKEND_TEST_YML_CONTENT);

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
def WEBDRIVER_TEST_YML_CONTENT =
"""${AUTOGENERATE_SNIPPET}
logging.level.root: INFO
logging.level.org.hibernate.engine.internal.StatisticalLoggingSessionEventListener: WARN
${common(true)}
${custom(true)}
server.port: ${ExportedConstants.TEST_PORT}
${WEBSERVER_SNIPPET}
# this is URL
spring.mvc.static-path-pattern: /**
# You need to remove "file:..." element for production or you can to remove spring.resources.static-locations
# first element - for eliminate manual restart app in IntelliJ for copy compiled js to target/classes, last slash is important,, second element - for documentation
spring.resources.static-locations: file:../backend/src/main/resources/static/, classpath:/static/

custom.selenium.implicitly-wait-timeout: 10
custom.selenium.browser: CHROME
custom.selenium.window-height: 900
custom.selenium.window-width: 1600
custom.selenium.selenide-condition-timeout: 10
custom.selenium.selenide-collections-timeout: 10

custom.it.url.prefix: ${ExportedConstants.SCHEME}://localhost:\${server.port}
custom.it.user.id: 1
${TEST_USERS_SNIPPET}
${DATA_STORE_SNIPPET(true, 'none')}
${MANAGEMENT_SNIPPET(true)}

facebook:
  client:
    clientId: 1684113965162824
    clientSecret: provide-it
    userAuthorizationUri: http://127.0.0.1:10080/mock/facebook/dialog/oauth
    accessTokenUri: http://127.0.0.1:10080/mock/facebook/oauth/access_token
    tokenName: oauth_token
    authenticationScheme: query
    clientAuthenticationScheme: form
  resource:
    userInfoUri: http://127.0.0.1:10080/mock/facebook/me?fields=id,name,picture
""";
writeAndLog(INTEGRATION_TEST_YML_FILE, WEBDRIVER_TEST_YML_CONTENT);
