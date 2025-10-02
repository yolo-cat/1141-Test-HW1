#!/bin/bash
# Script to run JavaFX application with proper module path

# Set JavaFX library path (adjust this path based on your Maven repository)
JAVAFX_LIB_PATH="/Users/joseph-m2/.m2/repository/org/openjfx"

# Create classpath with all dependencies
CLASSPATH="/Users/joseph-m2/Dev/1141-Test-HW1/target/classes"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-context/6.0.9/spring-context-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-aop/6.0.9/spring-aop-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-beans/6.0.9/spring-beans-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-core/6.0.9/spring-core-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-jcl/6.0.9/spring-jcl-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-expression/6.0.9/spring-expression-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-web/6.0.9/spring-web-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/io/micrometer/micrometer-observation/1.10.7/micrometer-observation-1.10.7.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/io/micrometer/micrometer-commons/1.10.7/micrometer-commons-1.10.7.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/springframework/spring-webmvc/6.0.9/spring-webmvc-6.0.9.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/apache/logging/log4j/log4j-core/2.20.0/log4j-core-2.20.0.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/apache/logging/log4j/log4j-api/2.20.0/log4j-api-2.20.0.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/apache/logging/log4j/log4j-slf4j2-impl/2.20.0/log4j-slf4j2-impl-2.20.0.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/slf4j/slf4j-api/2.0.6/slf4j-api-2.0.6.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/hibernate/validator/hibernate-validator/8.0.1.Final/hibernate-validator-8.0.1.Final.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/org/jboss/logging/jboss-logging/3.4.3.Final/jboss-logging-3.4.3.Final.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/com/fasterxml/classmate/1.5.1/classmate-1.5.1.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar"
CLASSPATH="$CLASSPATH:/Users/joseph-m2/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.15.2/jackson-datatype-jsr310-2.15.2.jar"

# Add JavaFX to classpath
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-controls/21.0.1/javafx-controls-21.0.1.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-controls/21.0.1/javafx-controls-21.0.1-mac-aarch64.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-graphics/21.0.1/javafx-graphics-21.0.1.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-graphics/21.0.1/javafx-graphics-21.0.1-mac-aarch64.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-base/21.0.1/javafx-base-21.0.1.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-base/21.0.1/javafx-base-21.0.1-mac-aarch64.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-fxml/21.0.1/javafx-fxml-21.0.1.jar"
CLASSPATH="$CLASSPATH:$JAVAFX_LIB_PATH/javafx-fxml/21.0.1/javafx-fxml-21.0.1-mac-aarch64.jar"

# Run the JavaFX application with proper module path
/Users/joseph-m2/Library/Java/JavaVirtualMachines/openjdk-24.0.2/Contents/Home/bin/java \
    --module-path "$JAVAFX_LIB_PATH/javafx-controls/21.0.1:$JAVAFX_LIB_PATH/javafx-fxml/21.0.1:$JAVAFX_LIB_PATH/javafx-graphics/21.0.1:$JAVAFX_LIB_PATH/javafx-base/21.0.1" \
    --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base \
    --enable-native-access=javafx.graphics \
    -classpath "$CLASSPATH" \
    com.deliveryplatform.gui.ExceptionDemoApp
