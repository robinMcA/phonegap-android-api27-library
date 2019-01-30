def call() {
	return [
		containerTemplate(
			name: 'phonegap-android-api28-builder',
			// TODO: Move image to agiledigital org
			image: 'noxharmonium/phonegap-android-api28-builder',
			alwaysPullImage: true,
			command: 'cat',
			ttyEnabled: true
		)
	]
}