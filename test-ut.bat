cd %~dp0
mvn org.jacoco:jacoco-maven-plugin:prepare-agent install -Prun-unit sonar:sonar