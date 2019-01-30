def call() {
	return [
		containerTemplate(
			name: 'phonegap-android-api27-builder',
			// TODO: Move image to agiledigital org
			image: 'noxharmonium/phonegap-android-api27-builder',
			alwaysPullImage: true,
			command: 'cat',
			ttyEnabled: true
		)
	]
}