def call(Map config) {
  return [
    [
      path: '/home/jenkins/.npm',
      claimName: "${config.project}-home-jenkins-npm",
      sizeGiB: 1
    ],
    [
      path: '/home/jenkins/.gradle/caches',
      claimName: "${config.project}-home-jenkins-gradle-caches",
      sizeGiB: 1
    ],
    [
      path: '/home/jenkins/.m2/repository',
      claimName: "${config.project}-home-jenkins-m2-repository",
      sizeGiB: 1
    ]
  ]
}