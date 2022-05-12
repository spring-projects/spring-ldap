def projectProperties = [
	[$class: 'BuildDiscarderProperty',
		strategy: [$class: 'LogRotator', numToKeepStr: '5']],
	pipelineTriggers([cron('@daily')])
]
properties(projectProperties)

def SUCCESS = hudson.model.Result.SUCCESS.toString()
currentBuild.result = SUCCESS


def SPRING_SIGNING_SECRING = file(credentialsId: 'spring-signing-secring.gpg', variable: 'SIGNING_KEYRING_FILE')
def SPRING_GPG_PASSPHRASE = string(credentialsId: 'spring-gpg-passphrase', variable: 'SIGNING_PASSWORD')
def OSSRH_S01_CREDENTIALS = usernamePassword(credentialsId: 'oss-s01-token', passwordVariable: 'OSSRH_S01_TOKEN_PASSWORD', usernameVariable: 'OSSRH_S01_TOKEN_USERNAME')
def ARTIFACTORY_CREDENTIALS = usernamePassword(credentialsId: '02bd1690-b54f-4c9f-819d-a77cb7a9822c', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD')
def JENKINS_PRIVATE_SSH_KEY = file(credentialsId: 'docs.spring.io-jenkins_private_ssh_key', variable: 'DEPLOY_SSH_KEY')

try {
	parallel check: {
		stage('Check') {
			node {
				checkout scm
				sh "git clean -dfx"
				try {
					withCredentials([ARTIFACTORY_CREDENTIALS]) {
						withEnv(["JAVA_HOME=${ tool 'jdk11' }"]) {
							sh "./gradlew test -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD --refresh-dependencies --no-daemon --stacktrace"
						}
					}
				} catch(Exception e) {
					currentBuild.result = 'FAILED: check'
					throw e
				} finally {
					junit '**/build/test-results/*/*.xml'
				}
			}
		}
	}

	if(currentBuild.result == 'SUCCESS') {
		parallel artifacts: {
			stage('Deploy Artifacts') {
				node {
					checkout scm
					sh "git clean -dfx"
					withCredentials([SPRING_SIGNING_SECRING, SPRING_GPG_PASSPHRASE, OSSRH_S01_CREDENTIALS, ARTIFACTORY_CREDENTIALS]) {
						withEnv(["JAVA_HOME=${ tool 'jdk11' }"]) {
							sh "./gradlew publishArtifacts finalizeDeployArtifacts -Psigning.secretKeyRingFile=$SIGNING_KEYRING_FILE -Psigning.keyId=$SPRING_SIGNING_KEYID -Psigning.password='$SIGNING_PASSWORD' -PossrhUsername=$OSSRH_S01_TOKEN_USERNAME -PossrhPassword=$OSSRH_S01_TOKEN_PASSWORD -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD --refresh-dependencies --no-daemon --stacktrace"
						}
					}
				}
			}
		},
		docs: {
			stage('Deploy Docs') {
				node {
					checkout scm
					sh "git clean -dfx"
					withCredentials([JENKINS_PRIVATE_SSH_KEY, ARTIFACTORY_CREDENTIALS]) {
						withEnv(["JAVA_HOME=${ tool 'jdk11' }"]) {
							sh "./gradlew deployDocs -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD -PdeployDocsHost=docs-ip.spring.io -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME --refresh-dependencies --no-daemon --stacktrace"
						}
					}
				}
			}
		},
		schema: {
			stage('Deploy Schema') {
				node {
					checkout scm
					sh "git clean -dfx"
					withCredentials([JENKINS_PRIVATE_SSH_KEY, ARTIFACTORY_CREDENTIALS]) {
						withEnv(["JAVA_HOME=${ tool 'jdk11' }"]) {
							sh "./gradlew deploySchema -PartifactoryUsername=$ARTIFACTORY_USERNAME -PartifactoryPassword=$ARTIFACTORY_PASSWORD -PdeployDocsHost=docs-ip.spring.io -PdeployDocsSshKeyPath=$DEPLOY_SSH_KEY -PdeployDocsSshUsername=$SPRING_DOCS_USERNAME --refresh-dependencies --no-daemon --stacktrace"
						}
					}
				}
			}
		}
	}
} catch(Exception e) {
	currentBuild.result = 'FAILED: deploys'
	throw e
} finally {
	def buildStatus = currentBuild.result
	def buildNotSuccess =  !SUCCESS.equals(buildStatus)
	def lastBuildNotSuccess = !SUCCESS.equals(currentBuild.previousBuild?.result)

	if(buildNotSuccess || lastBuildNotSuccess) {

		stage('Notifiy') {
			node {
				final def RECIPIENTS = [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]

				def subject = "${buildStatus}: Build ${env.JOB_NAME} ${env.BUILD_NUMBER} status is now ${buildStatus}"
				def details = """The build status changed to ${buildStatus}. For details see ${env.BUILD_URL}"""

				emailext (
					subject: subject,
					body: details,
					recipientProviders: RECIPIENTS,
					to: "$SPRING_SECURITY_TEAM_EMAILS"
				)
			}
		}
	}
}
