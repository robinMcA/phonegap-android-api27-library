def call(Map config) {
  return [
    [
      path: '/home/jenkins/.npm',
      claimName: "${config.project}-home-jenkins-npm",
      sizeGiB: 1
    ],
    [
      path: '/home/jenkins/.gradle',
      claimName: "${config.project}-home-jenkins-gradle",
      sizeGiB: 1
    ],
    [
      path: '/home/jenkins/.m2',
      claimName: "${config.project}-home-jenkins-m2",
      sizeGiB: 1
    ]
  ]
}