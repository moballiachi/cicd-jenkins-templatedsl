projectPath         = "template"
projectName         = "PROJECTNAME"
appEnvironment      = ['PRODUCTION', 'DEVELOP', 'STAGE']
appRepositoryURL    = "git@github.com:moballiachi/ring-connectfour-back.ms.git"
SshConnectionToken  = "908237u5rb3g73ytho345th32o"


pipelineJob("${projectPath}/${projectName}_Deploy") {
    displayName("${projectName}::Deploy")
    description("Pipeline <<${appRepositoryURL}>> \nDeploys an app inside the environment selected. ")
    parameters {
      stringParam('appVersion', '0.0.0', 'Application Version')
      choiceParam('appEnvironment', appEnvironment, 'Select environment to deploy ')
      choiceParam('appEnvironmentRegion', ['default', 'us-west-2', 'us-east-1'], 'Choose a region to deploy it.')
      booleanParam('remplace', true, 'Tap this attribute to remplace the app presents in the environment')
    }
    definition {
        cpsScm {
          scm {
            git {
              remote { 
                url("${appRepositoryURL}")
                credentials("${SshConnectionToken}")
              }
              branches("refs/remotes/origin/master")
              scriptPath('deploy/Jenkinsfile')
              extensions {
                localBranch {
    			  localBranch("master")
    			}
                messageExclusion { 
                  excludedMessage('(?s).*\\[maven-\\].*')
                }
              }
            }
          }
        }
    }
}

pipelineJob("${projectPath}/${projectName}_Release") {
    displayName("${projectName}::Release")
    description("Pipeline <<${appRepositoryURL}>> \nBuilds an app from the Main branch.")
    triggers {
       cron {
          spec('H H(18-18) * * *')
        }
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote { url("${appRepositoryURL}")
                        credentials("${SshConnectionToken}")
                        name('origin')
                    }
                    branches("refs/remotes/origin/main")
                    scriptPath('Jenkinsfile')
                    extensions {
                        cleanCheckout()
                        pruneStaleBranch()
                        localBranch {
                            localBranch('main')
                        }
                        messageExclusion {
                            excludedMessage('(?s).*\\[maven-\\].*')
                        }
                    }
                }
            }
        }
    }
}

pipelineJob("${projectPath}/${projectName}_Hotfix") {
    displayName("${projectName}::Hotfix")
    description("Pipeline <<${appRepositoryURL}>> \nBuilds an app from the main-hotfix branch.")
    logRotator {
        daysToKeep(90)
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        name('origin')
                        refspec("+refs/heads/main-hotfix:refs/remotes/origin/main-hotfix")
                        url("${appRepositoryURL}")
                        credentials("${SshConnectionToken}")
                    }
                    branches("refs/remotes/origin/main-hotfix")
                    scriptPath('Jenkinsfile')
                    extensions {
                        cleanCheckout()
                        pruneStaleBranch()
                        localBranch {
                            localBranch('main-hotfix')
                        }
                        messageExclusion {
                            excludedMessage('(?s).*\\[maven-\\].*')
                        }
                        userExclusion {
                            excludedUsers("moballiachi@gmail.com")
                        }
                    }
                }
            }
        }
    }
}

multibranchPipelineJob("${projectPath}/${projectName}_Feature") {
  //jobNames.add("${projectPath}/${projectName}_Feature")
  displayName("${projectName}::Feature")
  description("Pipeline <<${appRepositoryURL}>> \nBuilds an app from a commit made by Feature branches")
  branchSources {
    git {
    	id('123')
  		remote("${appRepositoryURL}")
        credentialsId("${SshConnectionToken}")
	    includes('feature*')
    }
    orphanedItemStrategy {
	   	discardOldItems {
	      	numToKeep(-1)
    	}
  	}
  }
}